package sim.app.episim.datamonitoring.steppables;

import java.util.Set;
import sim.app.episim.CellType;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChart;


public class CommonSteppableBuilder {
	
	public String buildCalculationHandler(String expr, Set<Class<?>> requiredClasses){
      StringBuffer handlerSource = new StringBuffer();
      handlerSource.append("new CalculationHandler(){\n");
      appendCellValidCheck(handlerSource, requiredClasses);
      handlerSource.append("  public double calculate(CellType cellTypeLocal) throws CellNotValidException{\n");
      handlerSource.append("    EpisimCellDiffModel cellDiff = cellTypeLocal.getEpisimCellDiffModelObject();\n");
      handlerSource.append("    Object cellTypeLocalObj = cellTypeLocal;\n");
      appendLocalVars(requiredClasses, handlerSource);
      appendAssignmentCheck("cellDiff", requiredClasses, handlerSource);
      appendAssignmentCheck("cellTypeLocalObj", requiredClasses, handlerSource);
      handlerSource.append("if(isValidCell(cellTypeLocal))");
      handlerSource.append("    return (double)("+ expr+");\n");
      handlerSource.append("else throw new CellNotValidException(\"Cell is not Valid: \"+ cellTypeLocal.getCellName());\n");
      handlerSource.append("  }\n");
      handlerSource.append("}\n");
      return handlerSource.toString();
	}
		
	private void appendCellValidCheck(StringBuffer source, Set<Class<?>> requiredClasses){
		boolean classFound = false;
		source.append("  private boolean isValidCell(CellType cellType){\n");
		for(Class<?> actClass: requiredClasses){
			if(CellType.class.isAssignableFrom(actClass)){
				source.append("    if(cellType.getClass().isAssignableFrom("+ actClass.getSimpleName()+ ".class)) return true;\n");
				classFound = true;
			}
		}
		if(classFound)source.append("    return false;\n");
		else source.append("    return true;\n");
		source.append("  }\n");
	}
	
	private void appendLocalVars(Set<Class<?>> requiredClasses, StringBuffer source){
		for(Class<?> actClass: requiredClasses){
			if(EpisimCellDiffModel.class.isAssignableFrom(actClass) || CellType.class.isAssignableFrom(actClass))				
				source.append(actClass.getSimpleName()+ " " + Names.convertClassToVariable(actClass.getSimpleName())+" = null;\n");
		}
	}
	private void appendAssignmentCheck(String varName, Set<Class<?>> requiredClasses, StringBuffer source){
		boolean firstLoop = true;
		
		for(Class<?> actClass: requiredClasses){
			if(EpisimCellDiffModel.class.isAssignableFrom(actClass) || CellType.class.isAssignableFrom(actClass)){				
				
				if(firstLoop){
					source.append("if("+varName+ ".getClass().isAssignableFrom("+ actClass.getSimpleName()+ ".class)) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
					firstLoop = false;
					
				}
				else{
					source.append("else if("+varName+ ".getClass().isAssignableFrom("+ actClass.getSimpleName()+ ".class)) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
				}
				
			}
		}
	}

}
