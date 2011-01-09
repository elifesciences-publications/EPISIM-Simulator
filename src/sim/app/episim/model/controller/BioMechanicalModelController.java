package sim.app.episim.model.controller;
	
import java.util.concurrent.ConcurrentHashMap;

import episimbiomechanics.EpisimModelConnector;

import episiminterfaces.EpisimBioMechanicalModel;
import episiminterfaces.EpisimBioMechanicalModelGlobalParameters;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;


public class BioMechanicalModelController implements java.io.Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2406025736169916469L;
		private static BioMechanicalModelController instance;
		private BiomechanicalModel biomechanicalModel;
		
		
		private BioMechanicalModelController(){
				biomechanicalModel = new BiomechanicalModel();			
		}
	protected synchronized static BioMechanicalModelController getInstance(){
		if(instance == null) instance = new BioMechanicalModelController();
		return instance;
	}
		
	public EpisimBioMechanicalModel getNewEpisimBioMechanicalModelObject(){
		try{
			return biomechanicalModel.getEpisimNewMechanicalModelObject();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	
	public EpisimBioMechanicalModel getNewEpisimBioMechanicalModelObject(AbstractCell cell) {
		try{
			return biomechanicalModel.getNewEpisimMechanicalModelObject(cell);
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
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimBioMechanicalModelGlobalParameters parametersObject){
		if(parametersObject != null) biomechanicalModel.reloadMechanicalModelGlobalParametersObject(parametersObject);
	}
	
	public void resetInitialGlobalValues(){		
			biomechanicalModel.resetInitialGlobalValues();		
	}
}