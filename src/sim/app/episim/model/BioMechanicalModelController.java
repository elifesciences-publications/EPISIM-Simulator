package sim.app.episim.model;
	
import java.util.concurrent.ConcurrentHashMap;

import episimbiomechanics.EpisimModelIntegrator;

import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

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
		
	public EpisimMechanicalModel getNewEpisimMechanicalModelObject(){
		try{
			return biomechanicalModel.getEpisimNewMechanicalModelObject();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	
	public EpisimMechanicalModel getNewEpisimMechanicalModelObject(AbstractCell cell) {
		try{
			return biomechanicalModel.getNewEpisimMechanicalModelObject(cell);
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	public EpisimModelIntegrator getEpisimModelIntegrator() {
		try{
			return biomechanicalModel.getEpisimModelIntegrator();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	public EpisimMechanicalModelGlobalParameters getEpisimMechanicalModelGlobalParameters() {
		try{
			return biomechanicalModel.getEpisimMechanicalModelGlobalParameters();
		}
		catch (Exception e){
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
	}
	
	public void reloadMechanicalModelGlobalParametersObject(EpisimMechanicalModelGlobalParameters parametersObject){
		if(parametersObject != null) biomechanicalModel.reloadMechanicalModelGlobalParametersObject(parametersObject);
	}
	
	public void resetInitialGlobalValues(){		
			biomechanicalModel.resetInitialGlobalValues();		
	}
}