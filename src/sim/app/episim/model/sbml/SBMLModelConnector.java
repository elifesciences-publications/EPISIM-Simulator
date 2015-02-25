package sim.app.episim.model.sbml;

import java.util.HashMap;
import java.util.Map;




import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.controller.ModelController;


import episiminterfaces.EpisimSbmlModelConfiguration;
import episiminterfaces.EpisimSbmlModelConfigurationEx;
import episiminterfaces.EpisimSbmlModelConnector;
import episiminterfaces.InterfaceVersion;

public class SBMLModelConnector implements EpisimSbmlModelConnector{
	
	private HashMap<String, EpisimSbmlModelConfiguration> sbmlModelConfigurationMap; 
	private HashMap<String, SBMLModelState> sbmlModelStates;
	private HashMap<String, Boolean> sbmlModelSimulationEnabled;
		
	public SBMLModelConnector(){		
		sbmlModelConfigurationMap = new HashMap<String, EpisimSbmlModelConfiguration>();
		sbmlModelStates = new HashMap<String, SBMLModelState>();
		sbmlModelSimulationEnabled = new HashMap<String, Boolean>();
	}

	public void setEpisimModelConfigurations(EpisimSbmlModelConfiguration[] episimModelConfigurations){
		if(episimModelConfigurations != null){
			try{			
				for(EpisimSbmlModelConfiguration actConfig : episimModelConfigurations){					
					sbmlModelConfigurationMap.put(actConfig.getModelFilename(), actConfig);
					sbmlModelStates.put(actConfig.getModelFilename(), new SBMLModelState());
					sbmlModelSimulationEnabled.put(actConfig.getModelFilename(), true);
					COPASIConnector.getInstance().registerNewCopasiDataModelWithSbmlFile(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile(), actConfig.getModelFilename());						
					 if(actConfig instanceof EpisimSbmlModelConfigurationEx){
						 EpisimSbmlModelConfigurationEx actConfigEx = (EpisimSbmlModelConfigurationEx) actConfig;
			      	 if(actConfig.getClass().isAnnotationPresent(InterfaceVersion.class)){
			      		 InterfaceVersion version = actConfig.getClass().getAnnotation(InterfaceVersion.class);
			      		 if(version.number()>=2){
			      			  if(!actConfigEx.isSimulationOnByDefault()){
			      				  switchSbmlModelSimulationOnOrOff(actConfig.getModelFilename(), false);
			      			  }
			      		 }
			      	 }						
					}
				}
			}
			catch(Exception e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}
   }

	public void setParameterValue(String originalName, String sbmlFile, double value){
		if(this.sbmlModelStates.containsKey(sbmlFile)){			
			this.sbmlModelStates.get(sbmlFile).addParameterValue(new SBMLModelEntity(originalName, value, 0));			
		}
   }
	
	public void setSpeciesQuantity(String originalName, String sbmlFile, double value){
		if(this.sbmlModelStates.containsKey(sbmlFile)){
			this.sbmlModelStates.get(sbmlFile).addSpeciesValue(new SBMLModelEntity(originalName,0, value));
		}
   }

	public void setSpeciesInitialQuantity(String originalName, String sbmlFile, double value){
		if(this.sbmlModelStates.containsKey(sbmlFile)){
			this.sbmlModelStates.get(sbmlFile).addSpeciesValue(new SBMLModelEntity(originalName,0, value));
		}
   }

	
	public double getSpeciesValue(String originalName, String sbmlFile){
		 if(this.sbmlModelStates.containsKey(sbmlFile)){
		  	return this.sbmlModelStates.get(sbmlFile).getSpeciesConcentration(originalName);
		}
		return 0;
   }

	public double getParameterValue(String originalName, String sbmlFile){
	   if(this.sbmlModelStates.containsKey(sbmlFile)){
	   	return this.sbmlModelStates.get(sbmlFile).getParameterValue(originalName);
	   }
	   return 0;
   }

	public double getFluxValue(String originalName, String sbmlFile) {
		if(this.sbmlModelStates.containsKey(sbmlFile)){
			return this.sbmlModelStates.get(sbmlFile).getReactionValue(originalName);
		}
		else return 0;
   }
	
	public HashMap<String, SBMLModelState> getSBMLModelStateMap(){ return this.sbmlModelStates;}
	
	public void initializeSBMLModelsWithCellAge(int ageInSimSteps){
		for(String actSbmlFile : this.sbmlModelConfigurationMap.keySet()){
			EpisimSbmlModelConfiguration actConfig = this.sbmlModelConfigurationMap.get(actSbmlFile);
			boolean onByDefault = true;
			if(actConfig instanceof EpisimSbmlModelConfigurationEx){
				 EpisimSbmlModelConfigurationEx actConfigEx = (EpisimSbmlModelConfigurationEx) actConfig;
	      	 if(actConfig.getClass().isAnnotationPresent(InterfaceVersion.class)){
	      		 InterfaceVersion version = actConfig.getClass().getAnnotation(InterfaceVersion.class);
	      		 if(version.number()>=2){
	      			 onByDefault=actConfigEx.isSimulationOnByDefault();
	      		 }
	      	 }						
			}
			if(onByDefault) COPASIConnector.getInstance().simulateSBMLModel(this.sbmlModelConfigurationMap.get(actSbmlFile), this.sbmlModelStates.get(actSbmlFile), ageInSimSteps);
		}
	}	

	public void simulateSbmlModels(){		
		for(String actSbmlFile : this.sbmlModelConfigurationMap.keySet()){
			if(sbmlModelSimulationEnabled.get(actSbmlFile)){
				COPASIConnector.getInstance().simulateSBMLModel(this.sbmlModelConfigurationMap.get(actSbmlFile), this.sbmlModelStates.get(actSbmlFile));
			}			
		}
   }
   public void switchSbmlModelSimulationOnOrOff(String sbmlModelFile, boolean isSimulationOn){
   	sbmlModelSimulationEnabled.put(sbmlModelFile, isSimulationOn);
   }
   public Map<String, Boolean> getSbmlModelSimulationEnabledMap(){
   	return this.sbmlModelSimulationEnabled;
   }
}
