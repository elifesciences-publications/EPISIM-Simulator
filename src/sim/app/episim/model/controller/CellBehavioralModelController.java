package sim.app.episim.model.controller;

import java.io.File;

import java.net.MalformedURLException;
import java.util.ArrayList;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;


import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.initialization.CellBehavioralModelInitializer;




public class CellBehavioralModelController implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2406025736169916469L;
	private static CellBehavioralModelController instance;
	private CellBehavioralModel cellBehavioralModel;
	
	
	private boolean caching = true;
	
	private File actLoadedCellBehavioralFile;
	
	private CellBehavioralModelController(){
		
		
	}
	
	protected synchronized static CellBehavioralModelController getInstance(){
		if(instance == null) instance = new CellBehavioralModelController();
		return instance;
	}
	
	public void reloadCellBehavioralModelGlobalParametersObject(EpisimCellBehavioralModelGlobalParameters parametersObject){
		if(cellBehavioralModel != null) cellBehavioralModel.reloadCellBehavioralModelGlobalParametersObject(parametersObject);
	}
	
	
	
	
	
	protected boolean loadModelFile(File modelFile) throws ModelCompatibilityException{
		
			CellBehavioralModelJarClassLoader jarLoader = null;
         try{
	         jarLoader = new CellBehavioralModelJarClassLoader(modelFile.toURI().toURL());
         }
         catch (MalformedURLException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
			if(jarLoader.isDiffModel()){
				cellBehavioralModel = new CellBehavioralModel(jarLoader.getModelClass(EpisimCellBehavioralModel.class), 
																	jarLoader.getGlobalParametersObject());
				this.actLoadedCellBehavioralFile = modelFile;
				return true;
			}
			else{
				this.actLoadedCellBehavioralFile = null;
				throw new ModelCompatibilityException("Model ist not a compatible Differentiation Model");
			}
		
		
		
	}
	
	public File getActLoadedModelFile(){
		return this.actLoadedCellBehavioralFile;
	}
	
	public EpisimCellBehavioralModel getNewEpisimCellBehavioralModelObject(){
	
				return cellBehavioralModel.getNewEpisimCellBehavioralModelObject();
	}
	
	public EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters(){
		return cellBehavioralModel.getEpisimCellBehavioralModelGlobalParameters();
	}
	
	public CellBehavioralModelInitializer getCellBehavioralModelInitializer(){
		return this.cellBehavioralModel.getCellBehavioralModelInitializer();
	}
	
	public CellBehavioralModelInitializer getCellBehavioralModelInitializer(File modelInitializationFile){
		return this.cellBehavioralModel.getCellBehavioralModelInitializer(modelInitializationFile);
	}
	
	public EpisimCellType[] getAvailableCellTypes(){ return getEpisimCellBehavioralModelGlobalParameters().getAvailableCellTypes(); }
	public EpisimCellType getCellTypeForOrdinal(int ordinal){ return getEpisimCellBehavioralModelGlobalParameters().getCellTypeForOrdinal(ordinal); }
	
	public EpisimDifferentiationLevel[] getAvailableDifferentiationLevels(){ return getEpisimCellBehavioralModelGlobalParameters().getAvailableDifferentiationLevels(); }
	public EpisimDifferentiationLevel getDifferentiationLevelForOrdinal(int ordinal){ return getEpisimCellBehavioralModelGlobalParameters().getDifferentiationLevelForOrdinal(ordinal); }
	
	
	public void resetInitialGlobalValues(){
		try{
			cellBehavioralModel.resetInitialGlobalValues();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
	}
	
	
}
