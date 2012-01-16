package sim.app.episim.model.controller;
	
import java.awt.geom.GeneralPath;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import episimbiomechanics.EpisimModelConnector;
import episimexceptions.ModelCompatibilityException;

import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;

import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.biomechanics.BiomechanicalModelFacade;
import sim.app.episim.model.biomechanics.BiomechanicalModelLoader;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.persistence.SimulationStateData;


public class BiomechanicalModelController implements java.io.Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2406025736169916469L;
		private static BiomechanicalModelController instance;
		private BiomechanicalModelFacade biomechanicalModel;
		private String actLoadedBiomechanicalModelName = "";
		private String actLoadedBiomechanicalModelId = "";
		
		private EpisimBiomechanicalModelGlobalParameters dummyGlobalParameters;
		
	private BiomechanicalModelController(){
		dummyGlobalParameters = new CenterBasedMechanicalModelGP();
	}
	
	protected synchronized static BiomechanicalModelController getInstance(){
		if(instance == null) instance = new BiomechanicalModelController();
		return instance;
	}
		
	public EpisimBiomechanicalModel getNewEpisimBioMechanicalModelObject(AbstractCell cell) {
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
	
	protected EpisimBiomechanicalModelGlobalParameters getEpisimBioMechanicalModelGlobalParameters() {
		try{
			if(biomechanicalModel != null) return biomechanicalModel.getEpisimMechanicalModelGlobalParameters();
			else return this.dummyGlobalParameters;
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
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
			
			
			if(ModeServer.consoleInput()){
				ModelParameterModifier parameterModifier = new ModelParameterModifier();
				if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
					parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(BiomechanicalModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()
							, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP)));
				}
			}
			return true;
		}
		return false;
	}
	public void clearCellField(){
		biomechanicalModel.clearCellField();
	}
	public void removeCellsInWoundArea(GeneralPath woundArea){
		biomechanicalModel.removeCellsInWoundArea(woundArea);
	}
	
	public Object getCellField(){
		return biomechanicalModel.getCellField();
	}
	
	public void newSimStepGloballyFinished(long simStepNumber){
		biomechanicalModel.newSimStepGloballyFinished(simStepNumber);
	}
	public void setReloadedCellField(Object cellField){
		biomechanicalModel.setReloadedCellField(cellField);
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
}