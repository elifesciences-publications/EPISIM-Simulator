package sim.app.episim.model;

import java.io.File;
import java.util.concurrent.Semaphore;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
public class ModelController implements java.io.Serializable{
	
	private static Semaphore sem = new Semaphore(1);
	
	private boolean modelOpened = false;
	
	private boolean simulationStartedOnce = false;
	
	private static ModelController instance;
	private ModelController(){
	
	}
	
	
	public static ModelController getInstance(){
		
		try{
		   sem.acquire();
	      if(instance == null) instance = new ModelController();
			sem.release();
		}
      catch (InterruptedException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
		return instance;
	}
	
	public EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters(){
	
		return CellBehavioralModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
	}
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters(){
		
		return BioMechanicalModelController.getInstance().getEpisimMechanicalModelGlobalParameters();
	}
	
	public EpisimCellBehavioralModel getNewEpisimStateModelObject(){
		
		return CellBehavioralModelController.getInstance().getNewEpisimCellBehavioralModelObject();
	}

	public EpisimMechanicalModel getMechanicalModel(){
		
		return BioMechanicalModelController.getInstance().getEpisimMechanicalModel();
	}
	
	public BioMechanicalModelController getBioMechanicalModelController(){ return BioMechanicalModelController.getInstance();}
	public CellBehavioralModelController getCellBehavioralModelController() { return CellBehavioralModelController.getInstance();}

	
   public boolean isModelOpened() {
   
   	return modelOpened;
   }
   
   public boolean loadCellBehavioralModelFile(File modelFile) throws ModelCompatibilityException{
   	boolean success = CellBehavioralModelController.getInstance().loadModelFile(modelFile);
   	
   	if(success){
   		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP)!= null &&
   				EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL)){
   			ModelParameterModifier parameterModifier = new ModelParameterModifier();
   			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
   				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(BioMechanicalModelController.getInstance().getEpisimMechanicalModelGlobalParameters()
   						, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP)));
   			}
   			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
   				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(CellBehavioralModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters()
   						, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP)));
   			}
   			
   		}
   	}
   	
   	return success;
   }
   	
   
   
	
   public void setModelOpened(boolean modelOpened) {
   
   	this.modelOpened = modelOpened;
   }
   
   public void setSimulationStartedOnce(boolean val){ this.simulationStartedOnce = val;}
   
   public boolean isSimulationStartedOnce(){ return this.simulationStartedOnce;}
	
}
