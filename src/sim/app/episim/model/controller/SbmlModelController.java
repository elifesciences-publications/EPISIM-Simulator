package sim.app.episim.model.controller;

import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import episiminterfaces.EpisimSbmlModelConnector;


public class SbmlModelController implements ClassLoaderChangeListener{
	
	private static SbmlModelController instance;
	
	private SbmlModelController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	protected static synchronized SbmlModelController getInstance(){
		if(instance == null){
			instance = new SbmlModelController();
		}
		return instance;
	}
	
	protected EpisimSbmlModelConnector getNewEpisimSbmlModelConnector(){
		return new SbmlModelConnector();
	}

	public void classLoaderHasChanged() {
		instance = null;
	   
   }
	
	
	

}
