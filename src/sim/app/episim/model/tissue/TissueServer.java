package sim.app.episim.model.tissue;

import java.util.concurrent.Semaphore;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbtractTissue;
import sim.app.episim.model.UniversalTissue;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.NoEpidermalTissueAvailableException;


public class TissueServer implements ClassLoaderChangeListener{
	
	private AbtractTissue actTissue;
	
	private static TissueServer instance;
	private static Semaphore sem = new Semaphore(1);
	private TissueServer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	public static TissueServer getInstance(){
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
	
	public void registerTissue(AbtractTissue tissue){
		this.actTissue = tissue;
	}

	public UniversalTissue getActEpidermalTissue() throws NoEpidermalTissueAvailableException{
		if(actTissue != null && actTissue instanceof UniversalTissue) return (UniversalTissue) actTissue;
		else throw new NoEpidermalTissueAvailableException("There is no epidermal tissue registered!");
	}
	
	public AbtractTissue getActTissue(){
		return actTissue;
	}
	
   public void classLoaderHasChanged() {
   	instance = null; 
   }
}
