package sim.app.episim;

import java.awt.Component;

import javax.swing.JOptionPane;

import binloc.ProjectLocator;

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
		
		if((loggingState=EpisimProperties.getProperty("exception.logging")) != null 
				&& loggingState.equals("on")){
			 logger = Logger.getLogger("Episim Exceptions");
			 try{
	         fh = new FileHandler(ProjectLocator.getPathOf("log").getAbsolutePath()+System.getProperty("file.separator")+"episimlog.log");
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
		
		/*
		if(rootComp != null)
			*/
		if(loggingState.equals("on")) logger.log(Level.WARNING, "An error occurred during runtime of Episim-Simulator", ex);

		else ex.printStackTrace();
	}
	
	public void registerParentComp(Component comp){ this.rootComp = comp; }
	
}
