package sim.app.episim.datamonitoring.build;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.util.Names;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;

public abstract class  AbstractCommonSourceBuilder {	
	
	
	
	protected StringBuffer generatedSourceCode;
	
	private static int counter = 1;
	
	
	protected static synchronized long getNextCalculationHandlerId(){
		long id = System.currentTimeMillis();
		id += counter;
		counter++;
		return id;
	}
	
	
	
	protected void appendRegisterObjectsMethod(Set<Class<?>> requiredClasses){
		if(requiredClasses != null){
			this.generatedSourceCode.append("  public void registerRequiredObjects(");
			for(Class<?> actClass: requiredClasses){
				if(isRequiredClassNecessary(actClass)){
					generatedSourceCode.append(actClass.getSimpleName() + " " + Names.convertClassToVariable(actClass.getSimpleName())+", ");
				}
			}
			this.generatedSourceCode.append("GenericBag<AbstractCell> allCells){\n");
			this.generatedSourceCode.append("    this.allCells = allCells;\n");
			for(Class<?> actClass: requiredClasses){
				if(!EpisimBiomechanicalModel.class.isAssignableFrom(actClass) && !EpisimCellBehavioralModel.class.isAssignableFrom(actClass) && !AbstractCell.class.isAssignableFrom(actClass)){
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
	   generatedSourceCode.append("  private ArrayList<CalculationCallBack> "+Names.CALCULATION_CALLBACK_LIST+" = new ArrayList<CalculationCallBack>();\n");
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
      if(config.isBooleanExpressionOnlyInitiallyChecked()){
      	handlerSource.append("  private Map<Long, Boolean> wasAlreadyCheckedOnceMap = new HashMap<Long, Boolean>();\n");
      	handlerSource.append("  private long lastSimStepNo = -1;\n");
      }
      handlerSource.append("  private Map<String, Object> params;\n");
      handlerSource.append("  {\n");
      appendParameterMapReproduction(handlerSource, "params", config.getParameters());
      handlerSource.append("  }\n");
      
      
      handlerSource.append("  public long getID(){ return "+id+"l; }\n");
      handlerSource.append("  public long getCorrespondingBaselineCalculationHandlerID(){ return "+baselineHandlerId+"l; }\n");
      handlerSource.append("  public int getCalculationAlgorithmID(){ return "+config.getCalculationAlgorithmID()+"; }\n");
      handlerSource.append("  public Map<String, Object> getParameters(){ return params; }\n");
      handlerSource.append("  public boolean isBaselineValue(){ return "+ isBaselineHandler+"; }\n");
          
      handlerSource.append("  public Class<? extends AbstractCell> getRequiredCellType(){\n");
      boolean classFound = false;
      for(Class<?> actClass: requiredClasses){
			if(AbstractCell.class.isAssignableFrom(actClass)){
				handlerSource.append("    return "+ actClass.getSimpleName()+ ".class;\n}\n");
				classFound = true;				
				break;
			}
		}
      if(!classFound) handlerSource.append("    return null;\n}\n");
      appendCellValidCheck(handlerSource, requiredClasses);
     
      handlerSource.append("  public double calculate(AbstractCell cellTypeLocal) throws CellNotValidException{\n");
      if(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID()).hasMathematicalExpression()){
	      handlerSource.append("    EpisimCellBehavioralModel cellBehaviour = cellTypeLocal.getEpisimCellBehavioralModelObject();\n");
	      handlerSource.append("    EpisimBiomechanicalModel biomechanics = cellTypeLocal.getEpisimBioMechanicalModelObject();\n");
	      handlerSource.append("    Object cellTypeLocalObj = cellTypeLocal;\n");
	      appendLocalVars(requiredClasses, handlerSource);
	      appendAssignmentCheck("cellBehaviour", requiredClasses, handlerSource);
	      appendAssignmentCheck("biomechanics", requiredClasses, handlerSource);
	      appendAssignmentCheck("cellTypeLocalObj", requiredClasses, handlerSource);
	      handlerSource.append("if(isValidCell(cellTypeLocal))");
	      handlerSource.append("    return (double)("+ config.getArithmeticExpression()[1]+");\n");
	      handlerSource.append("else throw new CellNotValidException(\"Cell is not Valid: \"+ cellTypeLocal.getCellName());\n");
      }
      else handlerSource.append("return Double.NEGATIVE_INFINITY;");
      handlerSource.append("  }\n");
         
      handlerSource.append("  public boolean conditionFulfilled(AbstractCell cellTypeLocal) throws CellNotValidException{\n");
      if(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID()).hasCondition()){
	      handlerSource.append("    EpisimCellBehavioralModel cellBehaviour = cellTypeLocal.getEpisimCellBehavioralModelObject();\n");
	      handlerSource.append("    EpisimBiomechanicalModel biomechanics = cellTypeLocal.getEpisimBioMechanicalModelObject();\n");
	      handlerSource.append("    Object cellTypeLocalObj = cellTypeLocal;\n");
	      appendLocalVars(requiredClasses, handlerSource);
	      appendAssignmentCheck("cellBehaviour", requiredClasses, handlerSource);
	      appendAssignmentCheck("biomechanics", requiredClasses, handlerSource);
	      appendAssignmentCheck("cellTypeLocalObj", requiredClasses, handlerSource);
	      handlerSource.append("if(isValidCell(cellTypeLocal)){\n");
	      if(config.isBooleanExpressionOnlyInitiallyChecked()){
	      	handlerSource.append("  if(lastSimStepNo < SimStateServer.getInstance().getSimStepNumber()){ lastSimStepNo = SimStateServer.getInstance().getSimStepNumber();}\n");
	      	handlerSource.append("  else if(lastSimStepNo > SimStateServer.getInstance().getSimStepNumber()){\n");
	      	handlerSource.append("    wasAlreadyCheckedOnceMap.clear();\n");
	      	handlerSource.append("    lastSimStepNo = SimStateServer.getInstance().getSimStepNumber();\n");
	      	handlerSource.append("  }\n");
	      	handlerSource.append("  if(!wasAlreadyCheckedOnceMap.containsKey(cellTypeLocal.getID())||(wasAlreadyCheckedOnceMap.containsKey(cellTypeLocal.getID())&&!wasAlreadyCheckedOnceMap.get(cellTypeLocal.getID()))){\n");
	      	handlerSource.append("    boolean result = (boolean)("+ config.getBooleanExpression()[1]+");\n");
	      	handlerSource.append("    if(result){\n");
	      	handlerSource.append("      wasAlreadyCheckedOnceMap.put(cellTypeLocal.getID(), true);\n");
	      	handlerSource.append("    }\n");
	      	handlerSource.append("    return result;\n");
	      	handlerSource.append("  }\n");
	      	handlerSource.append("  else{return true;}\n");
	      }
	      else handlerSource.append("    return (boolean)("+ config.getBooleanExpression()[1]+");\n");
	      handlerSource.append("}\n");
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
		source.append("  private boolean isValidCell(AbstractCell cellType){\n");
		for(Class<?> actClass: requiredClasses){
			if(AbstractCell.class.isAssignableFrom(actClass)){
				source.append("    if("+actClass.getSimpleName()+ ".class.isAssignableFrom(cellType.getClass())) return true;\n");
				classFound = true;
			}
		}
		if(classFound)source.append("    return false;\n");
		else source.append("    return true;\n");
		source.append("  }\n");
	}
	
	private void appendLocalVars(Set<Class<?>> requiredClasses, StringBuffer source){
		for(Class<?> actClass: requiredClasses){
			if(EpisimBiomechanicalModel.class.isAssignableFrom(actClass) ||EpisimCellBehavioralModel.class.isAssignableFrom(actClass) || AbstractCell.class.isAssignableFrom(actClass))				
				source.append(actClass.getSimpleName()+ " " + Names.convertClassToVariable(actClass.getSimpleName())+" = null;\n");
		}
	}
	private void appendAssignmentCheck(String varName, Set<Class<?>> requiredClasses, StringBuffer source){
		boolean firstLoop = true;
		
		for(Class<?> actClass: requiredClasses){
			if(EpisimBiomechanicalModel.class.isAssignableFrom(actClass) || EpisimCellBehavioralModel.class.isAssignableFrom(actClass) || AbstractCell.class.isAssignableFrom(actClass)){				
				
				if(firstLoop){
					source.append("if("+ actClass.getSimpleName()+ ".class.isAssignableFrom("+varName+ ".getClass())) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
					firstLoop = false;
					
				}
				else{
					source.append("else if("+ actClass.getSimpleName()+ ".class.isAssignableFrom("+varName+ ".getClass())) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
				}
				
			}
		}
	}
	protected boolean isRequiredClassNecessary(Class<?> actClass){
		return !EpisimCellBehavioralModel.class.isAssignableFrom(actClass)
				&& !EpisimBiomechanicalModel.class.isAssignableFrom(actClass)
		  		&& !AbstractCell.class.isAssignableFrom(actClass) 
		  		&& !EpisimCellType.class.isAssignableFrom(actClass)
		  		&& !EpisimDifferentiationLevel.class.isAssignableFrom(actClass);
	}
	
	

}
