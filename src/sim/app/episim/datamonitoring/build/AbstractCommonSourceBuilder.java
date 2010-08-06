package sim.app.episim.datamonitoring.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;

public abstract class  AbstractCommonSourceBuilder {	
	
	
	
	protected StringBuffer generatedSourceCode;
	
	protected void appendRegisterObjectsMethod(Set<Class<?>> requiredClasses){
		if(requiredClasses != null){
			this.generatedSourceCode.append("  public void registerRequiredObjects(");
			for(Class<?> actClass: requiredClasses){
				if(!EpisimCellBehavioralModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
					generatedSourceCode.append(actClass.getSimpleName() + " " + Names.convertClassToVariable(actClass.getSimpleName())+", ");
				}
			}
			this.generatedSourceCode.append("GenericBag<CellType> allCells, Continuous2D cellContinuous){\n");
			this.generatedSourceCode.append("    this.allCells = allCells;\n");
			this.generatedSourceCode.append("    this.cellContinuous = cellContinuous;\n");
			for(Class<?> actClass: requiredClasses){
				if(!EpisimCellBehavioralModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
					this.generatedSourceCode.append("    this." + Names.convertClassToVariable(actClass.getSimpleName())+" = "
							+ Names.convertClassToVariable(actClass.getSimpleName())+";\n");
				}
			}	
			this.generatedSourceCode.append(" }\n");
		}
		
	}
	
	private void appendParameterMapReproduction(StringBuffer source, String parameterMapDataFieldName, Map<String, Object> parameterValueMap){
		source.append(parameterMapDataFieldName+ " = new HashMap<String, Object>();\n");
		
		for(String key: parameterValueMap.keySet()){
			Object val = parameterValueMap.get(key);
			if(val!= null && (val instanceof Number || val instanceof Boolean)){				
				source.append(parameterMapDataFieldName+".put(\""+ key+"\", "+val+");\n"); 
			}
		}
		
	}
		
	protected void appendDataFields(){
	   generatedSourceCode.append("  private EnhancedSteppable steppable;\n");
	   generatedSourceCode.append("  private ArrayList<CalculationCallBack> "+Names.CALCULATIONCALLBACKLIST+" = new ArrayList<CalculationCallBack>();\n");
	}
	
	protected void appendStandardMethods(){
		generatedSourceCode.append("public EnhancedSteppable getSteppable(){return steppable;}\n");
		
	}
	
	protected void appendEnd(){
		generatedSourceCode.append("}");
	}
	
	
	protected String buildCalculationHandler(long id, long baselineHandlerId, boolean isBaselineHandler, CalculationAlgorithmConfigurator config, Set<Class<?>> requiredClasses){
		
      StringBuffer handlerSource = new StringBuffer();
      handlerSource.append("new CalculationHandler(){\n");
      handlerSource.append("  private Map<String, Object> params;\n");
      handlerSource.append("  {\n");
      appendParameterMapReproduction(handlerSource, "params", config.getParameters());
      handlerSource.append("  }\n");
      
      
      handlerSource.append("  public long getID(){ return "+id+"l; }\n");
      handlerSource.append("  public long getCorrespondingBaselineCalculationHandlerID(){ return "+baselineHandlerId+"l; }\n");
      handlerSource.append("  public int getCalculationAlgorithmID(){ return "+config.getCalculationAlgorithmID()+"; }\n");
      handlerSource.append("  public Map<String, Object> getParameters(){ return params; }\n");
      handlerSource.append("  public boolean isBaselineValue(){ return "+ isBaselineHandler+"; }\n");
          
      handlerSource.append("  public Class<? extends CellType> getRequiredCellType(){\n");
      boolean classFound = false;
      for(Class<?> actClass: requiredClasses){
			if(CellType.class.isAssignableFrom(actClass)){
				handlerSource.append("    return "+ actClass.getSimpleName()+ ".class;\n}\n");
				classFound = true;
				
				break;
			}
		}
      if(!classFound) handlerSource.append("    return null;\n}\n");
      appendCellValidCheck(handlerSource, requiredClasses);
     
      handlerSource.append("  public double calculate(CellType cellTypeLocal) throws CellNotValidException{\n");
      handlerSource.append("    EpisimCellBehavioralModel cellBehaviour = cellTypeLocal.getEpisimCellBehavioralModelObject();\n");
      handlerSource.append("    Object cellTypeLocalObj = cellTypeLocal;\n");
      appendLocalVars(requiredClasses, handlerSource);
      appendAssignmentCheck("cellBehaviour", requiredClasses, handlerSource);
      appendAssignmentCheck("cellTypeLocalObj", requiredClasses, handlerSource);
      handlerSource.append("if(isValidCell(cellTypeLocal))");
      handlerSource.append("    return (double)("+ config.getArithmeticExpression()[1]+");\n");
      handlerSource.append("else throw new CellNotValidException(\"Cell is not Valid: \"+ cellTypeLocal.getCellName());\n");
      handlerSource.append("  }\n");
         
      handlerSource.append("  public boolean conditionFulfilled(CellType cellTypeLocal) throws CellNotValidException{\n");
      if(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID()).hasCondition()){
	      handlerSource.append("    EpisimCellBehavioralModel cellBehaviour = cellTypeLocal.getEpisimCellBehavioralModelObject();\n");
	      handlerSource.append("    Object cellTypeLocalObj = cellTypeLocal;\n");
	      appendLocalVars(requiredClasses, handlerSource);
	      appendAssignmentCheck("cellBehaviour", requiredClasses, handlerSource);
	      appendAssignmentCheck("cellTypeLocalObj", requiredClasses, handlerSource);
	      handlerSource.append("if(isValidCell(cellTypeLocal))");
	      handlerSource.append("    return (boolean)("+ config.getBooleanExpression()[1]+");\n");
	      handlerSource.append("else throw new CellNotValidException(\"Cell is not Valid: \"+ cellTypeLocal.getCellName());\n");
		}
      else 
      {
      	handlerSource.append("return true;");
      }
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
			if(EpisimCellBehavioralModel.class.isAssignableFrom(actClass) || CellType.class.isAssignableFrom(actClass))				
				source.append(actClass.getSimpleName()+ " " + Names.convertClassToVariable(actClass.getSimpleName())+" = null;\n");
		}
	}
	private void appendAssignmentCheck(String varName, Set<Class<?>> requiredClasses, StringBuffer source){
		boolean firstLoop = true;
		
		for(Class<?> actClass: requiredClasses){
			if(EpisimCellBehavioralModel.class.isAssignableFrom(actClass) || CellType.class.isAssignableFrom(actClass)){				
				
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
