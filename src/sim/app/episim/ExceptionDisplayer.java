package sim.app.episim;

import java.awt.Component;

import javax.swing.JOptionPane;

import binloc.ProjectLocator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.*;

public class ExceptionDisplayer {
	private static ExceptionDisplayer instance;
	
	private static Logger logger;
	private static FileHandler fh;
	private String loggingState;
	
	private Component rootComp;
	private ExceptionDisplayer(){
		
		if((loggingState=EpisimProperties.getProperty(EpisimProperties.EXCEPTION_LOGGING_PROP)) != null 
				&& loggingState.equals(EpisimProperties.ON_EXCEPTION_LOGGING_VAL)){
			 logger = Logger.getLogger("Episim Exceptions");
			 try{
				File logPath = ProjectLocator.getPathOf("log");
				if(!logPath.exists() && logPath.isDirectory()) logPath.mkdir();
	         fh = new FileHandler(logPath.getAbsolutePath()+System.getProperty("file.separator")+"episimlog.log");
	         fh.setFormatter(new SimpleFormatter());
	         logger.addHandler(fh);
	         logger.setLevel(Level.ALL);

         }
         catch (SecurityException e){
	         this.displayException(e);
         }
         catch (IOException e){
         	this.displayException(e);
         }
         catch (URISyntaxException e){
         	this.displayException(e);
         }
		}
	}

	public static synchronized ExceptionDisplayer getInstance(){
		if (instance == null) instance = new ExceptionDisplayer();
		return instance;
	}
	
	public synchronized void displayException(Exception ex){
		
		displayException(((Throwable) ex));
	}
	
	public synchronized void displayException(Throwable t){
		
		/*
		if(rootComp != null)
			*/
		if(loggingState.equals(EpisimProperties.ON_EXCEPTION_LOGGING_VAL)) logger.log(Level.WARNING, "An error occurred during runtime of Episim-Simulator", t);

		else t.printStackTrace();
	}
	
	public void registerParentComp(Component comp){ this.rootComp = comp; }
	
}
