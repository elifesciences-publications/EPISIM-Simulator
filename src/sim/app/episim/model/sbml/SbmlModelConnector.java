package sim.app.episim.model.sbml;

import java.util.HashMap;

import org.COPASI.CCopasiDataModel;
import org.COPASI.CCopasiObjectName;
import org.COPASI.CReaction;
import org.COPASI.CTrajectoryTask;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.controller.ModelController;

import episiminterfaces.EpisimSbmlModelConfiguration;
import episiminterfaces.EpisimSbmlModelConnector;

public class SbmlModelConnector implements EpisimSbmlModelConnector{
	
	private HashMap<String, EpisimSbmlModelConfiguration> sbmlModelConfigurationMap; 
	private HashMap<String, CCopasiDataModel> copasiModelMap;	
	private HashMap<String, CTrajectoryTask> trajectoryTaskMap;
	private HashMap<String, CReaction> reactionNameReactionMap;
	
	private boolean firstSimulationRun = true;
	
	public SbmlModelConnector(){
		
		sbmlModelConfigurationMap = new HashMap<String, EpisimSbmlModelConfiguration>();
		
		copasiModelMap = new HashMap<String, CCopasiDataModel>();		

		reactionNameReactionMap = new HashMap<String, CReaction>();
		
		trajectoryTaskMap = new HashMap<String, CTrajectoryTask>();
	}

	public void setEpisimModelConfigurations(EpisimSbmlModelConfiguration[] episimModelConfigurations){
		if(episimModelConfigurations != null){
			try{			
				for(EpisimSbmlModelConfiguration actConfig : episimModelConfigurations){
					
					sbmlModelConfigurationMap.put(actConfig.getModelFilename(), actConfig);
					CCopasiDataModel dataModel = COPASIConnector.getInstance().getNewCopasiDataModelForSbmlFile(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile(), actConfig.getModelFilename());
					buildReactionMap(actConfig.getModelFilename(), dataModel);
					copasiModelMap.put(actConfig.getModelFilename(), dataModel);
					
				}
			}
			catch(Exception e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
   }

	public void setParameterValue(String originalName, String sbmlFile, double value){
		if(this.copasiModelMap.containsKey(sbmlFile)){
			COPASIConnector.getInstance().setInitialValueOfParameter(this.copasiModelMap.get(sbmlFile), originalName, value);
		}
   }

	public void setSpeciesInitialQuantity(String originalName, String sbmlFile, double value){
		if(this.copasiModelMap.containsKey(sbmlFile)){
			COPASIConnector.getInstance().setInitialConcentrationOfSpecies(this.copasiModelMap.get(sbmlFile), originalName, value);
		}
   }

	
	public double getSpeciesValue(String originalName, String sbmlFile){
		 if(this.copasiModelMap.containsKey(sbmlFile)){
		   	CCopasiDataModel dataModel = this.copasiModelMap.get(sbmlFile);
		   	long index = -1;
		   	index = dataModel.getModel().findMetabByName(originalName);
		   	return dataModel.getModel().getMetabolite(index).getConcentration();
		}
		return 0;
   }

	public double getParameterValue(String originalName, String sbmlFile){
	   if(this.copasiModelMap.containsKey(sbmlFile)){
	   	CCopasiDataModel dataModel = this.copasiModelMap.get(sbmlFile);
	   	if(dataModel.getModel().getModelValue(originalName) != null){
	   		return dataModel.getModel().getModelValue(originalName).getValue();
	   	}
	   }
	   return 0;
   }

	public double getFluxValue(String originalName, String sbmlFile) {
		String reactionKey = sbmlFile + "_" + originalName;
		if(this.reactionNameReactionMap.containsKey(reactionKey)){
			return this.reactionNameReactionMap.get(reactionKey).getFlux();
		}
		else return 0;
   }
	
	public void initializeSBMLModelsWithCellAge(int ageInSimSteps){
		for(int i = 0; i < ageInSimSteps; i++){	
			simulateSbmlModels();
		}
	}
	

	public void simulateSbmlModels(){		
		for(String actSbmlFile : this.copasiModelMap.keySet()){
			
			if(!this.trajectoryTaskMap.containsKey(actSbmlFile)){
				this.trajectoryTaskMap.put(actSbmlFile, COPASIConnector.getInstance().getNewTrajectoryTask(this.copasiModelMap.get(actSbmlFile), this.sbmlModelConfigurationMap.get(actSbmlFile)));
			}
			try{
	         this.trajectoryTaskMap.get(actSbmlFile).process(firstSimulationRun);
         }
         catch (Exception e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }						
		}
	   firstSimulationRun = false;
   }
	
	private void buildReactionMap(String sbmlFileName, CCopasiDataModel dataModel){
		long noOfReactions = dataModel.getModel().getNumReactions();
			
		for(long i = 0; i < noOfReactions;i++){
			CReaction reaction = dataModel.getModel().getReaction(i);
			if(reaction != null){				
				this.reactionNameReactionMap.put(sbmlFileName+"_"+reaction.getObjectName().trim(), reaction);
			}
		}			
	}

}
