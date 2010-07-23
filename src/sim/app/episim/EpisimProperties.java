package sim.app.episim;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import binloc.ProjectLocator;


public class EpisimProperties {
	
	private static final EpisimProperties instance = new EpisimProperties();
	
	private Properties properties;
	
	private static final String CONFIG = "config";
	private static final String FILENAME = "episimconfig.properties";
	
	public static final String EXCEPTION_LOGGING_PROP = "exception.logging";
	public static final String EPISIMBUILD_JARNAME_PROP = "episimbuild.jarname";
	public static final String EXCEPTION_DISPLAYMODE_PROP = "exception.displaymode";
	public static final String SIMULATOR_GUI_PROP = "simulator.gui";
	public static final String SIMULATOR_CONSOLEMODE_PROP = "simulator.consolemode";
	public static final String MOVIE_PATH_PROP = "moviepath";
	
	
	public static final String ON_EXCEPTION_LOGGING_VAL = "on";
	public static final String OFF_EXCEPTION_LOGGING_VAL = "off";
	
	public static final String ON_SIMULATOR_VAL = "on";
	public static final String OFF_SIMULATOR_VAL = "off";
		
	public static final String ON_CONSOLEMODE_VAL = "on";
	public static final String OFF_CONSOLEMODE_VAL = "off";
	
	public static final String ECLIPSE_EXCEPTION_DISPLAYMODE_VAL = "eclipse";
	public static final String SIMULATOR_EXCEPTION_DISPLAYMODE_VAL = "simulator";
	
	
	
	private EpisimProperties(){
		properties = new Properties();
		FileInputStream stream;
      try{
	      stream = new FileInputStream(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat(FILENAME));
         properties.load(stream);
         stream.close();
      }
      catch (IOException e1){
	      ExceptionDisplayer.getInstance().displayException(e1);
      }
      catch (URISyntaxException e2){
      	ExceptionDisplayer.getInstance().displayException(e2);
      }      
		
	}
	
	public static String getProperty(String name){
		return instance.getProperties().getProperty(name);
	}
	
	private Properties getProperties(){ return properties;}
	
	

}
