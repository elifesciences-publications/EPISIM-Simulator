package sim.app.episim.model.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimSbmlModelConnector;

import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.ModelInitialization;
public class ModelController implements java.io.Serializable{
	
	private static Semaphore sem = new Semaphore(1);
	
	private boolean modelOpened = false;
	
	private boolean simulationStartedOnce = false;
	
	private static ModelController instance;
	private ModelInitialization initializer;
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
	public EpisimBiomechanicalModelGlobalParameters getEpisimBioMechanicalModelGlobalParameters(){
		
		return BiomechanicalModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	}
	
	public EpisimCellBehavioralModel getNewEpisimCellBehavioralModelObject(){
		
		return CellBehavioralModelController.getInstance().getNewEpisimCellBehavioralModelObject();
	}
	
	public void standardInitializationOfModels(){
		 initializer = new ModelInitialization();
	}
	
	public void initializeModels(File file){
		 initializer = new ModelInitialization(file);
	}	

	public ArrayList<UniversalCell> getInitialCellEnsemble(){
		return initializer.getCells();
	}
	
	
	
	public EpisimBiomechanicalModel getNewBioMechanicalModelObject(AbstractCell cell){		
		return BiomechanicalModelController.getInstance().getNewEpisimBioMechanicalModelObject(cell);
	}
	
	public BiomechanicalModelController getBioMechanicalModelController(){ return BiomechanicalModelController.getInstance();}
	public CellBehavioralModelController getCellBehavioralModelController() { return CellBehavioralModelController.getInstance();}	
   public boolean isModelOpened(){ return modelOpened; }
   
   public boolean loadCellBehavioralModelFile(File modelFile) throws ModelCompatibilityException{
   	boolean success = CellBehavioralModelController.getInstance().loadModelFile(modelFile);
   	
   	if(success){
   		if(ModeServer.consoleInput()){
   			ModelParameterModifier parameterModifier = new ModelParameterModifier();
   			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
   				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(CellBehavioralModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters()
   						, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP)));
   			}   			
   			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP) != null){   				
   				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(MiscalleneousGlobalParameters.instance()
   						, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP)));
   			}
   			
   		}
   		success = BiomechanicalModelController.getInstance().loadModelFile(CellBehavioralModelController.getInstance().getNewEpisimCellBehavioralModelObject().getIdOfRequiredEpisimModelConnector());
   	}
   	
   	return success;
   }
   	
   
   
	
   public void setModelOpened(boolean modelOpened) {
   
   	this.modelOpened = modelOpened;
   }
   
   public void setSimulationStartedOnce(boolean val){ this.simulationStartedOnce = val;}
   
   public boolean isSimulationStartedOnce(){ return this.simulationStartedOnce;}
	
   public EpisimSbmlModelConnector getNewEpisimSbmlModelConnector(){
   	return SbmlModelController.getInstance().getNewEpisimSbmlModelConnector();
   }
   
}
