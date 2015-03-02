package sim.app.episim.model.controller;
	
import java.awt.geom.GeneralPath;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episimmcc.EpisimModelConnector;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.BiomechanicalModelFacade;
import sim.app.episim.model.biomechanics.BiomechanicalModelLoader;
import sim.app.episim.model.biomechanics.centerbased2D.oldmodel.CenterBased2DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.engine.SimState;


public class BiomechanicalModelController implements java.io.Serializable, ClassLoaderChangeListener{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2406025736169916469L;
		private static BiomechanicalModelController instance= new BiomechanicalModelController();;
		private BiomechanicalModelFacade biomechanicalModel;
		private String actLoadedBiomechanicalModelName = "";
		private String actLoadedBiomechanicalModelId = "";
		
		private EpisimBiomechanicalModelGlobalParameters dummyGlobalParameters;
		private static Semaphore sem = new Semaphore(1);
	private BiomechanicalModelController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		dummyGlobalParameters = new CenterBased2DModelGP();
	}
	
	protected static BiomechanicalModelController getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new BiomechanicalModelController();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
		
	public EpisimBiomechanicalModel getNewEpisimBioMechanicalModelObject(AbstractCell cell) {
		try{
			return biomechanicalModel.getNewEpisimBiomechanicalModelObject(cell);
		}
		catch (Exception e){
			EpisimExceptionHandler.getInstance().displayException(e);
			return null;
		}
	}
	
	public synchronized EpisimModelConnector getNewEpisimModelConnector() {
		try{
			return biomechanicalModel.getNewEpisimModelConnector();
		}
		catch (Exception e){
			EpisimExceptionHandler.getInstance().displayException(e);
			return null;
		}
	}
	
	protected EpisimBiomechanicalModelGlobalParameters getEpisimBioMechanicalModelGlobalParameters() {
		try{
			if(biomechanicalModel != null) return biomechanicalModel.getEpisimMechanicalModelGlobalParameters();
			else return this.dummyGlobalParameters;
		}
		catch (Exception e){
			EpisimExceptionHandler.getInstance().displayException(e);
			return null;
		}
	}	
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(){
		return this.biomechanicalModel.getBiomechanicalModelInitializer();
	}
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(SimulationStateData simStateData){
		return this.biomechanicalModel.getBiomechanicalModelInitializer(simStateData);
	}
	
	public boolean loadModelFile(String requiredModelConnectorID) throws ModelCompatibilityException{
		
		BiomechanicalModelLoader modelLoader = new BiomechanicalModelLoader(requiredModelConnectorID);
		
		boolean success =  modelLoader.getBiomechanicalModelInitializerClass() != null && modelLoader.getEpisimBiomechanicalModelClass() != null && modelLoader.getEpisimModelConnectorClass() != null && modelLoader.getEpisimBiomechnicalModelGlobalParametersObject()!= null;
		if(success){
			
			this.biomechanicalModel = new BiomechanicalModelFacade(modelLoader.getEpisimBiomechanicalModelClass(), modelLoader.getEpisimModelConnectorClass(), modelLoader.getEpisimBiomechnicalModelGlobalParametersObject(), modelLoader.getBiomechanicalModelInitializerClass());
			
			
			
			ModelParameterModifier parameterModifier = new ModelParameterModifier();
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
					parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(BiomechanicalModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()
							, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP)));
			}
			return true;
		}
		return false;
	}
	public void clearCellField(){
		biomechanicalModel.clearCellField();
	}
	public void resetCellField(){
		biomechanicalModel.resetCellField();
	}
	public void removeCellsInWoundArea(GeneralPath woundArea){
		biomechanicalModel.removeCellsInWoundArea(woundArea);
	}
	
	public Object getCellField(){
		return biomechanicalModel.getCellField();
	}
	
	public void newSimStepGloballyFinished(long simStepNumber, SimState state){
		biomechanicalModel.newSimStepGloballyFinished(simStepNumber, state);
	}
	public void newGlobalSimStep(long simStepNumber, SimState state){
		biomechanicalModel.newGlobalSimStep(simStepNumber, state);
	}
	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimBiomechanicalModelGlobalParameters parametersObject){
		if(parametersObject != null) biomechanicalModel.reloadMechanicalModelGlobalParametersObject(parametersObject);
	}
	
	public void resetInitialGlobalValues(){		
			biomechanicalModel.resetInitialGlobalValues();		
	}
	
   public String getActLoadedBiomechanicalModelName() {
   
   	return actLoadedBiomechanicalModelName;
   }
	
   public String getActLoadedBiomechanicalModelId() {
   
   	return actLoadedBiomechanicalModelId;
   }

	public void classLoaderHasChanged() {
	   instance = null;
   }
}