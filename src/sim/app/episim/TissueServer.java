package sim.app.episim;

import episimexceptions.NoEpidermalTissueAvailableException;
import sim.app.episim.tissue.Epidermis;
import sim.app.episim.tissue.TissueType;


public class TissueServer {
	
	private TissueType actTissue;
	
	private static TissueServer instance;
	
	private TissueServer(){
		
	}
	
	public static synchronized TissueServer getInstance(){
				if(instance == null) instance = new TissueServer();
				return instance;
	}
	
	public void registerTissue(TissueType tissue){
		this.actTissue = tissue;
	}

	public Epidermis getActEpidermalTissue() throws NoEpidermalTissueAvailableException{
		if(actTissue instanceof Epidermis) return (Epidermis) actTissue;
		else throw new NoEpidermalTissueAvailableException("There is no epidermal tissue registered!");
	}
	
	public TissueType getActTissue(){
		return actTissue;
	}
}
