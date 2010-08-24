package sim.app.episim.model;

import java.io.File;

import java.net.MalformedURLException;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;


import sim.app.episim.ExceptionDisplayer;




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
		
			ModelJarClassLoader jarLoader = null;
         try{
	         jarLoader = new ModelJarClassLoader(modelFile.toURI().toURL());
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
	
	
	
	public void resetInitialGlobalValues(){
		try{
			cellBehavioralModel.resetInitialGlobalValues();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
	}
	
	
}
