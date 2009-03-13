package sim.app.episim.datamonitoring.steppables;

import java.util.Map;
import java.util.Set;
import sim.app.episim.CellType;
import sim.app.episim.util.Names;
import episimexceptions.CellNotValidException;
import episiminterfaces.CalculationHandler;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSeries;
import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.EpisimDataExportColumn;


public class CommonSteppableBuilder {
	
	public String buildCalculationHandler(String expr, Set<Class<?>> requiredClasses){
				
      StringBuffer handlerSource = new StringBuffer();
      handlerSource.append("new CalculationHandler(){\n");
      handlerSource.append("private long id;\n");
      handlerSource.append("  public long getID(){ return id; }\n");
          
      handlerSource.append("  public Class<? extends CellType> getRequiredCellType(){\n");
      boolean classFound = false;
      for(Class<?> actClass: requiredClasses){
			if(CellType.class.isAssignableFrom(actClass)){
				handlerSource.append("    return "+ actClass.getSimpleName()+ ".class));\n");
				classFound = true;
			}
		}
      if(!classFound) handlerSource.append("    return null;\n}\n");
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
	
	public void appendCalucationHandlerRegistration(EpisimChart chart, StringBuffer source, long baselineCalculationHandlerID, Map<Long, Long> seriesCalculationHandlerIDs){
		
		
		if(chart.getBaselineExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)){
			source.append("CalculationController.getInstance().registerForChartCalculationGradient(");
			source.append(Names.insertIDIntoCalculationHandlerAndRemovePrefix(chart.getBaselineExpression()[1], -1l)+", ((XYSeries) null));\n");
		}
		else if(chart.getBaselineExpression()[1].startsWith(Names.BUILDCELLHANDLER)){
			source.append("CalculationController.getInstance().registerForOneCellCalculation(");
			source.append(Names.insertIDIntoCalculationHandlerAndRemovePrefix(chart.getBaselineExpression()[1], baselineCalculationHandlerID)+", ((XYSeries) null));\n");
		}
		for(EpisimChartSeries actSeries: chart.getEpisimChartSeries()){
			if(actSeries.getExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)){
				source.append("CalculationController.getInstance().registerForChartCalculationGradient(");
				source.append(Names.insertIDIntoCalculationHandlerAndRemovePrefix(actSeries.getExpression()[1], -1l)+", ");
				source.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+");\n");
			}
			if(actSeries.getExpression()[1].startsWith(Names.BUILDCELLHANDLER)){
				source.append("CalculationController.getInstance().registerForOneCellCalculation(");
				source.append(Names.insertIDIntoCalculationHandlerAndRemovePrefix(actSeries.getExpression()[1], seriesCalculationHandlerIDs.get(actSeries.getId()))+", ");
				source.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+");\n");
			}
		}
	}
	
	public void appendCalucationHandlerRegistration(EpisimDataExportDefinition dataExportDef, StringBuffer source,  Map<Long, Long> columnCalculationHandlerIDs){
		
		for(EpisimDataExportColumn actColumn: dataExportDef.getEpisimDataExportColumns()){
			if(actColumn.getCalculationExpression()[1].startsWith(Names.BUILDGRADIENTHANDLER)) {
				
				//TODO: diese Zeile noch für DatenExport anpassen
				source.append("CalculationController.getInstance().registerForChartCalculationGradient(");
				source.append(Names.insertIDIntoCalculationHandlerAndRemovePrefix(actColumn.getCalculationExpression()[1], -1l)+", ");
				source.append(Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+");\n");
			}
			if(actColumn.getCalculationExpression()[1].startsWith(Names.BUILDCELLHANDLER)) {
				source.append("CalculationController.getInstance().registerForOneCellCalculation(");
				source.append(Names.insertIDIntoCalculationHandlerAndRemovePrefix(actColumn.getCalculationExpression()[1], columnCalculationHandlerIDs.get(actColumn.getId()))+", ");
				source.append(Names.convertClassToVariable(Names.cleanString(actColumn.getName())+actColumn.getId())+");\n");
			}
		}
	}

}
