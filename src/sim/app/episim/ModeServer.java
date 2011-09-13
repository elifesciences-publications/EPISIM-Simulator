package sim.app.episim;


public class ModeServer {
	
	private boolean guiMode = true;
	private boolean consoleInput = false;
	
	private static final ModeServer server = new ModeServer();
	
	private ModeServer(){
		consoleInput =  (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null 
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON));
		if(consoleInput){
			guiMode = ((EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
					&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.ON) && consoleInput) 
					|| (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP)== null));
		}
	}
	
	public static boolean consoleInput(){ return server.consoleInput;}
	public static boolean guiMode(){ return server.guiMode;}

}
