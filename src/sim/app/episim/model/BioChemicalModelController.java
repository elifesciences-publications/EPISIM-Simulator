package sim.app.episim.model;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;

import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.KCyte;


public class BioChemicalModelController implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2406025736169916469L;
	private static BioChemicalModelController instance;
	private BiochemicalModel biochemicalModel;
	
	
	private boolean caching = true;
	
	
	
	private BioChemicalModelController(){
		
		
	}
	protected synchronized static BioChemicalModelController getInstance(){
		if(instance == null) instance = new BioChemicalModelController();
		return instance;
	}
	
	public void reloadCellDiffModelGlobalParametersObject(EpisimCellDiffModelGlobalParameters parametersObject){
		if(biochemicalModel != null) biochemicalModel.reloadCellDiffModelGlobalParametersObject(parametersObject);
	}
	
	
	public boolean loadModelFile(File modelFile){
		try{
			ModelJarClassLoader jarLoader = new ModelJarClassLoader(modelFile.toURI().toURL());
			if(jarLoader.isDiffModel()){
				biochemicalModel = new BiochemicalModel(jarLoader.getModelClass(EpisimCellDiffModel.class), 
																	jarLoader.getGlobalParametersObject());
				return true;
			}
			else throw new Exception("Model ist not a compatible Differentiation Model");
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return false;
		}
		
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
