package sim.app.episim;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import binloc.ProjectLocator;

import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;


public class EpisimLogger implements ClassLoaderChangeListener{
	private static EpisimLogger instance;
	
	private static Logger logger;
	private static FileHandler fh;
	private boolean exceptionLoggingState;
	private boolean infoLoggingState;
	
	private EpisimLogger(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		exceptionLoggingState=((EpisimProperties.getProperty(EpisimProperties.EXCEPTION_LOGGING_PROP) != null) 
				&& EpisimProperties.getProperty(EpisimProperties.EXCEPTION_LOGGING_PROP).equals(EpisimProperties.ON));
		infoLoggingState=((EpisimProperties.getProperty(EpisimProperties.INFO_LOGGING_PROP) != null) 
				&& EpisimProperties.getProperty(EpisimProperties.INFO_LOGGING_PROP).equals(EpisimProperties.ON));
		
		if(exceptionLoggingState || infoLoggingState){
			 
			 try{
				File logPath = ProjectLocator.getPathOf("log");
				
				if(!logPath.exists()){ 
					logPath.mkdir();
				}
	         fh = new FileHandler(logPath.getAbsolutePath()+System.getProperty("file.separator")+"episimlog.log", true);
	         fh.setFormatter(new SimpleFormatter()); 
	         logger = Logger.getLogger("EPISIM Log");
	         logger.addHandler(fh);
	         logger.setLevel(Level.ALL);
	      }
         catch (SecurityException e){
	         ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (IOException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
         catch (URISyntaxException e){
         	ExceptionDisplayer.getInstance().displayException(e);
         }
		}
	}
	
	public static synchronized EpisimLogger getInstance(){
		if (instance == null) instance = new EpisimLogger();
		return instance;
	}
	
	public void classLoaderHasChanged() {
	   instance = null;
   }
	
	public void logException(String message, Throwable t){
		if(exceptionLoggingState){ 
			logger.log(Level.SEVERE, "An error occurred during runtime of Episim-Simulator: "+message, t);
		}
		else t.printStackTrace();
	}
	public void logInfo(String message){
		if(infoLoggingState){ 
			logger.log(Level.INFO, message);
		}	
	}
	
	
}
