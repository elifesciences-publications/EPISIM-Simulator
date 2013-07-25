package sim.app.episim;

import java.awt.Component;

import javax.swing.JOptionPane;

import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;

import binloc.ProjectLocator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.*;

public class ExceptionDisplayer implements ClassLoaderChangeListener{
	private static ExceptionDisplayer instance;
	

	
	private Component rootComp;
	private ExceptionDisplayer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		
	}

	public static synchronized ExceptionDisplayer getInstance(){
		if (instance == null) instance = new ExceptionDisplayer();
		return instance;
	}
	
	public synchronized void displayException(Exception ex){
		
		displayException(((Throwable) ex));
	}
	
	public synchronized void displayException(Throwable t){
		
		EpisimLogger.getInstance().logException("", t);
	}
	
	public void registerParentComp(Component comp){ this.rootComp = comp; }


   public void classLoaderHasChanged() {
	   instance = null;
   }
	
}
