package sim.app.episim;


public class ModeServer {
	
	private boolean guiMode = true;
	private boolean consoleInput = false;
	private boolean useMonteCarloSteps = false;
	private static final ModeServer server = new ModeServer();
	
	private ModeServer(){
		consoleInput =  (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON));
		if(consoleInput){
			guiMode = ((EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
					&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.ON) && consoleInput) 
					|| (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP)== null));
		}
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIM_STEP_MODE) != null
			&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIM_STEP_MODE).equals(EpisimProperties.SIMULATOR_SIM_STEP_MODE_MONTE_CARLO)){
			useMonteCarloSteps = true;
		}
	}
	
	public static boolean consoleInput(){ return server.consoleInput;}
	public static boolean guiMode(){ return server.guiMode;}
	public static boolean useMonteCarloSteps(){ return server.useMonteCarloSteps; }

}
