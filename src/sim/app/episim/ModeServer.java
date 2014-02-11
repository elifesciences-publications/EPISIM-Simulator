package sim.app.episim;


public class ModeServer {
	
	private boolean guiMode = true;
	private boolean useMonteCarloSteps = false;
	private static final ModeServer server = new ModeServer();
	
	private ModeServer(){
		
	
		guiMode = ((EpisimProperties.getProperty(EpisimProperties.GUI_PROP) != null 
					&& EpisimProperties.getProperty(EpisimProperties.GUI_PROP).equals(EpisimProperties.ON)) 
					|| (EpisimProperties.getProperty(EpisimProperties.GUI_PROP)== null));
		
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SIM_STEP_MODE) != null
			&& EpisimProperties.getProperty(EpisimProperties.SIMULATION_SIM_STEP_MODE).equals(EpisimProperties.SIM_STEP_MODE_MONTE_CARLO)){
			useMonteCarloSteps = true;
		}
	}
	
	public static boolean guiMode(){ return server.guiMode;}
	public static boolean useMonteCarloSteps(){ return server.useMonteCarloSteps; }

}
