package sim.app.episim.datamonitoring.build;

import java.util.Set;

import sim.app.episim.CellType;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimChartSeries;


public abstract class  AbstractCommonSourceBuilder {
	
	protected StringBuffer generatedSourceCode;
	
	protected void appendRegisterObjectsMethod(Set<Class<?>> requiredClasses){
		if(requiredClasses != null){
			this.generatedSourceCode.append("  public void registerRequiredObjects(");
			for(Class<?> actClass: requiredClasses){
				if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
					generatedSourceCode.append(actClass.getSimpleName() + " " + Names.convertClassToVariable(actClass.getSimpleName())+", ");
				}
			}
			this.generatedSourceCode.append("GenericBag<CellType> allCells, Continuous2D cellContinuous){\n");
			this.generatedSourceCode.append("    this.allCells = allCells;\n");
			this.generatedSourceCode.append("    this.cellContinuous = cellContinuous;\n");
			for(Class<?> actClass: requiredClasses){
				if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
					this.generatedSourceCode.append("    this." + Names.convertClassToVariable(actClass.getSimpleName())+" = "
							+ Names.convertClassToVariable(actClass.getSimpleName())+";\n");
				}
			}
			
			
			this.generatedSourceCode.append("  }\n");
		}
		
	}
	protected void appendDataFields(){
	   generatedSourceCode.append("  private EnhancedSteppable steppable;\n");
	}
	
	protected void appendStandardMethods(){
		generatedSourceCode.append("public EnhancedSteppable getSteppable(){return steppable;}\n");
		
	}
	
	protected void appendEnd(){
		generatedSourceCode.append("}");
	}

}
