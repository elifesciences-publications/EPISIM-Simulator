package sim.app.episim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import binloc.ProjectLocator;


public class EpisimProperties {
	
	private static final EpisimProperties instance = new EpisimProperties();
	
	private Properties properties;
	
	private static final String CONFIG = "config";
	private static final String FILENAME = "episimconfig.properties";
	
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
