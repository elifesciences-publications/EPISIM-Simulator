package sim.app.episim.util;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ModelParameterModifier;


public class SimulationTrigger {
	public enum TriggerType { CBM, BM };
	
	private TriggerType triggerType;
	private String globalParameterName;
	private double doubleValue = Double.NaN;
	private boolean booleanValue;
	private long simStep;
	
	public SimulationTrigger(TriggerType triggerType, long simStep, String globalParameterName, double doubleValue){
		this.triggerType = triggerType;
		this.simStep = simStep;
		this.globalParameterName = globalParameterName;
		this.doubleValue = doubleValue;		
	}
	public SimulationTrigger(TriggerType triggerType, long simStep, String globalParameterName, boolean booleanValue){
		this.triggerType = triggerType;
		this.simStep = simStep;
		this.globalParameterName = globalParameterName;
		this.booleanValue = booleanValue;		
	}
	
   public TriggerType getTriggerType() {   
   	return triggerType;
   }
	
   public String getGlobalParameterName() {   
   	return globalParameterName;
   }
	
   public double getDoubleValue() {   
   	return doubleValue;
   }
	
   public boolean isBooleanValue() {   
   	return booleanValue;
   }
	
   public long getSimStep() {   
   	return simStep;
   }
	
	
   public boolean isExecutable(long simStep){ return this.simStep == simStep; }
   
   public void execute(){
   	ModelParameterModifier parameterModifier = new ModelParameterModifier();
   	if(this.triggerType==TriggerType.CBM){
   		EpisimCellBehavioralModelGlobalParameters globalBehave = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
   		if(Double.isNaN(doubleValue))parameterModifier.setParameterValue(globalBehave, globalParameterName, ""+booleanValue);
   		else parameterModifier.setParameterValue(globalBehave, globalParameterName, ""+doubleValue);
   	}
   	else if(this.triggerType==TriggerType.BM){
   		EpisimBiomechanicalModelGlobalParameters globalMech = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   		if(Double.isNaN(doubleValue))parameterModifier.setParameterValue(globalMech, globalParameterName, ""+booleanValue);
   		else parameterModifier.setParameterValue(globalMech, globalParameterName, ""+doubleValue);
   	}		
   }  
}
