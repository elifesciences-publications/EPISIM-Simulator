package sim.app.episim.tissueimport;

import java.util.concurrent.Semaphore;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.NoEpidermalTissueAvailableException;


public class TissueServer implements ClassLoaderChangeListener{
	
	private TissueType actTissue;
	
	private static TissueServer instance;
	private static Semaphore sem = new Semaphore(1);
	private TissueServer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	protected static TissueServer getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new TissueServer();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
	
	public void registerTissue(TissueType tissue){
		this.actTissue = tissue;
	}

	public UniversalTissue getActEpidermalTissue() throws NoEpidermalTissueAvailableException{
		if(actTissue != null && actTissue instanceof UniversalTissue) return (UniversalTissue) actTissue;
		else throw new NoEpidermalTissueAvailableException("There is no epidermal tissue registered!");
	}
	
	public TissueType getActTissue(){
		return actTissue;
	}
	
   public void classLoaderHasChanged() {
   	instance = null; 
   }
}
