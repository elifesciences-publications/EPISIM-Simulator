package sim.app.episim.model;




	import java.io.File;
	import java.io.IOException;
	import java.lang.reflect.InvocationTargetException;
	import java.net.MalformedURLException;
	import java.util.HashMap;
	import java.util.Iterator;
	import java.util.Map;
	import java.util.concurrent.ConcurrentHashMap;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModel;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

	import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.UniversalCell;
import sim.app.episim.tissue.Epidermis;


	public class BioMechanicalModelController implements java.io.Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 2406025736169916469L;
		private static BioMechanicalModelController instance;
		private BiomechanicalModel biomechanicalModel;
		
		private ConcurrentHashMap<String, Object> cache;
		private boolean caching = true;
		
		private double[][] adhesionValues = null;
		private double[][] resetAdhesionValues = null;
		private ConcurrentHashMap<String, Object> resetCache;
		private BioMechanicalModelController(){
			cache = new ConcurrentHashMap<String, Object>();
			biomechanicalModel = new BiomechanicalModel();
			
		}
		protected synchronized static BioMechanicalModelController getInstance(){
			if(instance == null) instance = new BioMechanicalModelController();
			return instance;
		}
		
	public EpisimMechanicalModel getEpisimMechanicalModel() {

		try{
			return biomechanicalModel.getEpisimMechanicalModel();
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