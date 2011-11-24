package sim.app.episim.model.sbml;

import java.util.HashMap;



import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.controller.ModelController;


import episiminterfaces.EpisimSbmlModelConfiguration;
import episiminterfaces.EpisimSbmlModelConnector;

public class SbmlModelConnector implements EpisimSbmlModelConnector{
	
	private HashMap<String, EpisimSbmlModelConfiguration> sbmlModelConfigurationMap; 
	private HashMap<String, SBMLModelState> sbmlModelStates;
	private HashMap<String, Boolean> sbmlModelSimulationEnabled;
		
	public SbmlModelConnector(){		
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
				}
			}
			catch(Exception e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
   }

	public void setParameterValue(String originalName, String sbmlFile, double value){
		if(this.sbmlModelStates.containsKey(sbmlFile)){
			this.sbmlModelStates.get(sbmlFile).addParameterValue(new SBMLModelEntity(originalName, value, 0));			
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
			COPASIConnector.getInstance().simulateSBMLModel(this.sbmlModelConfigurationMap.get(actSbmlFile), this.sbmlModelStates.get(actSbmlFile), ageInSimSteps);
		
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
}
