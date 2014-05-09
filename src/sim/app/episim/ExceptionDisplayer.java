package sim.app.episim;

import java.awt.Component;

import javax.swing.JOptionPane;

import episimexceptions.ZeroNeighbourCellsAccessException;
import sim.SimStateServer;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import binloc.ProjectLocator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

public class ExceptionDisplayer implements ClassLoaderChangeListener{
	
	
	private static ExceptionDisplayer instance;
	private static Semaphore sem = new Semaphore(1);
	
	private Component rootComp;
	private ExceptionDisplayer(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		
	}

	public static ExceptionDisplayer getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new ExceptionDisplayer();				
				sem.release();
         }
         catch (InterruptedException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
	
	public synchronized void displayException(Exception ex){
		
		displayException(((Throwable) ex));
	}
	
	public synchronized void displayException(Throwable t){
		if(t instanceof ZeroNeighbourCellsAccessException){
			if(ModeServer.guiMode()){
				JOptionPane.showMessageDialog(rootComp, "A cell with no neighbours tried to access parameters of a neighboring cell.\nDefine checks for the existance of neighbouring cells (numberOfNeighbours>0)\nin your graphical cell behavioral model before accessing parameters of neighbouring cells!\nEPISIM Simulator will shut down...", "Invalid neighbour cell access", JOptionPane.ERROR_MESSAGE);
			}
			EpisimLogger.getInstance().logException("", t);
			System.exit(0);
			
		}
		else EpisimLogger.getInstance().logException("", t);
	}
	
	public void registerParentComp(Component comp){ this.rootComp = comp; }


   public void classLoaderHasChanged() {
	   instance = null;
   }
	
}
