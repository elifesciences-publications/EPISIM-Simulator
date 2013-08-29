package sim.app.episim.tissue;

import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import episimexceptions.NoEpidermalTissueAvailableException;


public class TissueServer implements ClassLoaderChangeListener{
	
	private TissueType actTissue;
	
	private static TissueServer instance;
	
	private TissueServer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	}
	
	protected static synchronized TissueServer getInstance(){
				if(instance == null) instance = new TissueServer();
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
