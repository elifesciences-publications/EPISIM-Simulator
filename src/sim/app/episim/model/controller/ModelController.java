package sim.app.episim.model.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;
import episiminterfaces.EpisimSbmlModelConnector;

import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.ModelInitialization;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.portrayal.Portrayal;
public class ModelController implements java.io.Serializable, ClassLoaderChangeListener{
	
	private static Semaphore sem = new Semaphore(1);
	
	private boolean modelOpened = false;
	
	private boolean simulationStartedOnce = false;
	private boolean storedSimStateLoaded = false;
	
	private static ModelController instance;
	private ModelInitialization initializer;
	
	private ModelController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
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
		 storedSimStateLoaded = false;
		 initializer = new ModelInitialization();
	}
	
	public boolean isStoredSimStateLoaded(){ return storedSimStateLoaded; }
	
	public void initializeModels(SimulationStateData simStateData){
		 storedSimStateLoaded = true;
		 initializer = new ModelInitialization(simStateData);
	}	

	public ArrayList<UniversalCell> getInitialCellEnsemble(){
		return initializer.getCells();
	}
	
	public boolean testCBMFileLoadedSimStateCompatibility(){
		return initializer.testCBMFileLoadedSimStateCompatibility();
	}
	
	
	public EpisimPortrayal getCellPortrayal(){
		return initializer.getCellPortrayal();
	}
	
	public EpisimPortrayal[] getExtraCellularDiffusionPortrayals(){
		return initializer.getExtraCellularDiffusionPortrayals();
	}
	
	public EpisimPortrayal[] getAdditionalPortrayalsCellBackground(){
		return initializer.getAdditionalPortrayalsCellBackground();
	}
	
	public EpisimPortrayal[] getAdditionalPortrayalsCellForeground(){
		return initializer.getAdditionalPortrayalsCellForeground();
	}
	
	public EpisimBiomechanicalModel getNewBioMechanicalModelObject(AbstractCell cell){		
		return BiomechanicalModelController.getInstance().getNewEpisimBioMechanicalModelObject(cell);
	}
	
	public BiomechanicalModelController getBioMechanicalModelController(){ return BiomechanicalModelController.getInstance();}
	public CellBehavioralModelController getCellBehavioralModelController() { return CellBehavioralModelController.getInstance();}
	public ExtraCellularDiffusionController getExtraCellularDiffusionController(){ return ExtraCellularDiffusionController.getInstance();}
	
   public boolean isModelOpened(){ return modelOpened; }
   
   public boolean loadCellBehavioralModelFile(File modelFile) throws ModelCompatibilityException{
   	this.initializer = null;
   	boolean success = CellBehavioralModelController.getInstance().loadModelFile(modelFile);   	
   	if(success){
   			ModelParameterModifier parameterModifier = new ModelParameterModifier();
   			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
   				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(CellBehavioralModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters()
   						, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP)));
   			}   			
   			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP) != null){   				
   				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(MiscalleneousGlobalParameters.getInstance()
   						, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP)));
   			}
   			
   		
   		success = BiomechanicalModelController.getInstance().loadModelFile(CellBehavioralModelController.getInstance().getNewEpisimCellBehavioralModelObject().getIdOfRequiredEpisimModelConnector());
   		ExtraCellularDiffusionController.getInstance().newCellBehavioralModelLoaded();
   	}
   	
   	return success;
   }
   
   public ModelDimensionality getModelDimensionality(){ return getEpisimBioMechanicalModelGlobalParameters().getModelDimensionality();}
   	
   public boolean isStandardKeratinocyteModel(){
   	return CellBehavioralModelController.getInstance().isStandardKeratinocyteModel();
   }
   
	
   public void setModelOpened(boolean modelOpened) {
   
   	this.modelOpened = modelOpened;
   }
   
   public void setSimulationStartedOnce(boolean val){ this.simulationStartedOnce = val;}
   
   public boolean isSimulationStartedOnce(){ return this.simulationStartedOnce;}
	
   public EpisimSbmlModelConnector getNewEpisimSbmlModelConnector(){
   	return SbmlModelController.getInstance().getNewEpisimSbmlModelConnector();
   }
   public void classLoaderHasChanged() {
	   instance = null;
   }
   
}
