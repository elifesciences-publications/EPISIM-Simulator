package sim.app.episim.model;

import java.io.File;

import java.net.MalformedURLException;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;


import sim.app.episim.ExceptionDisplayer;




public class BioChemicalModelController implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2406025736169916469L;
	private static BioChemicalModelController instance;
	private BiochemicalModel biochemicalModel;
	
	
	private boolean caching = true;
	
	private File actLoadedCellDiffFile;
	
	private BioChemicalModelController(){
		
		
	}
	protected synchronized static BioChemicalModelController getInstance(){
		if(instance == null) instance = new BioChemicalModelController();
		return instance;
	}
	
	public void reloadCellDiffModelGlobalParametersObject(EpisimCellDiffModelGlobalParameters parametersObject){
		if(biochemicalModel != null) biochemicalModel.reloadCellDiffModelGlobalParametersObject(parametersObject);
	}
	
	
	public boolean loadModelFile(File modelFile) throws ModelCompatibilityException{
		
			ModelJarClassLoader jarLoader = null;
         try{
	         jarLoader = new ModelJarClassLoader(modelFile.toURI().toURL());
         }
         catch (MalformedURLException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
			if(jarLoader.isDiffModel()){
				biochemicalModel = new BiochemicalModel(jarLoader.getModelClass(EpisimCellDiffModel.class), 
																	jarLoader.getGlobalParametersObject());
				this.actLoadedCellDiffFile = modelFile;
				return true;
			}
			else{
				this.actLoadedCellDiffFile = null;
				throw new ModelCompatibilityException("Model ist not a compatible Differentiation Model");
			}
		
		
		
	}
	
	public File getActLoadedModelFile(){
		return this.actLoadedCellDiffFile;
	}
	
	public EpisimCellDiffModel getNewEpisimCellDiffModelObject(){
	
				return biochemicalModel.getNewEpisimCellDiffModelObject();
	}
	
	public EpisimCellDiffModelGlobalParameters getEpisimCellDiffModelGlobalParameters(){
		return biochemicalModel.getEpisimCellDiffModelGlobalParameters();
	}
	
	public void resetInitialGlobalValues(){
		try{
			biochemicalModel.resetInitialGlobalValues();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
	}
	
	
}
