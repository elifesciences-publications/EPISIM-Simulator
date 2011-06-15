package sim.app.episim.model.controller;

import sim.app.episim.model.sbml.SbmlModelConnector;
import episiminterfaces.EpisimSbmlModelConnector;


public class SbmlModelController {
	
	private static SbmlModelController instance = new SbmlModelController();
	
	private SbmlModelController(){}
	
	protected static synchronized SbmlModelController getInstance(){
		return instance;
	}
	
	protected EpisimSbmlModelConnector getNewEpisimSbmlModelConnector(){
		return new SbmlModelConnector();
	}
	
	
	

}
