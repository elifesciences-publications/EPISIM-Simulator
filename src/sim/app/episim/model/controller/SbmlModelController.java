package sim.app.episim.model.controller;

import java.util.concurrent.Semaphore;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import episiminterfaces.EpisimSbmlModelConnector;


public class SbmlModelController implements ClassLoaderChangeListener{
	
	private static SbmlModelController instance = new SbmlModelController();
	private static Semaphore sem = new Semaphore(1);
	private SbmlModelController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	protected static SbmlModelController getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new SbmlModelController();				
				sem.release();
         }
         catch (InterruptedException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }				
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
