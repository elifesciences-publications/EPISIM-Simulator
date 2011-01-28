package sim.app.episim.model.controller;
	
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import episimbiomechanics.EpisimModelConnector;
import episimexceptions.ModelCompatibilityException;

import episiminterfaces.EpisimBioMechanicalModel;
import episiminterfaces.EpisimBioMechanicalModelGlobalParameters;

import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.AbstractBiomechanicalModelInitializer;


public class BioMechanicalModelController implements java.io.Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2406025736169916469L;
		private static BioMechanicalModelController instance;
		private BiomechanicalModel biomechanicalModel;
		private String actLoadedBiomechanicalModelName = "";
		private String actLoadedBiomechanicalModelId = "";
		
		
	private BioMechanicalModelController(){}
	
	protected synchronized static BioMechanicalModelController getInstance(){
		if(instance == null) instance = new BioMechanicalModelController();
		return instance;
	}
		
	public EpisimBioMechanicalModel getNewEpisimBioMechanicalModelObject(AbstractCell cell) {
		try{
			return biomechanicalModel.getNewEpisimBiomechanicalModelObject(cell);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	public synchronized EpisimModelConnector getNewEpisimModelConnector() {
		try{
			return biomechanicalModel.getNewEpisimModelConnector();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	public EpisimBioMechanicalModelGlobalParameters getEpisimBioMechanicalModelGlobalParameters() {
		try{
			return biomechanicalModel.getEpisimMechanicalModelGlobalParameters();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}	
	
	public AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(){
		return this.biomechanicalModel.getBiomechanicalModelInitializer();
	}
	
	public AbstractBiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile){
		return this.biomechanicalModel.getBiomechanicalModelInitializer(modelInitializationFile);
	}
	
	protected boolean loadModelFile(String requiredModelConnectorID) throws ModelCompatibilityException{
		
		BiomechanicalModelLoader modelLoader = new BiomechanicalModelLoader(requiredModelConnectorID);
		
		boolean success =  modelLoader.getEpisimBiomechanicalModelClass() != null && modelLoader.getEpisimModelConnectorClass() != null && modelLoader.getEpisimBiomechnicalModelGlobalParametersObject()!= null;
		if(success){
			
			this.biomechanicalModel = new BiomechanicalModel(modelLoader.getEpisimBiomechanicalModelClass(), modelLoader.getEpisimModelConnectorClass(), modelLoader.getEpisimBiomechnicalModelGlobalParametersObject());
			
			
			if(ModeServer.consoleInput()){
				ModelParameterModifier parameterModifier = new ModelParameterModifier();
				if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
					parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(BioMechanicalModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()
							, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP)));
				}
			}
			return true;
		}
		return false;
	}
	
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimBioMechanicalModelGlobalParameters parametersObject){
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
}