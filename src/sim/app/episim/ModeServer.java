package sim.app.episim;


public class ModeServer {
	
	private boolean guiMode = true;
	private boolean consoleInput = false;
	
	private static final ModeServer server = new ModeServer();
	
	private ModeServer(){
		consoleInput =  (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL));
		if(consoleInput){
			guiMode = ((EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
					&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.ON_SIMULATOR_GUI_VAL) && consoleInput) 
					|| (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP)== null));
		}
	}
	
	public static boolean consoleInput(){ return server.consoleInput;}
	public static boolean guiMode(){ return server.guiMode;}

}
