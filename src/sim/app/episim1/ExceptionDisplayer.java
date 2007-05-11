package sim.app.episim1;

import java.awt.Component;

import javax.swing.JOptionPane;


public class ExceptionDisplayer {
	private static ExceptionDisplayer instance;
	
	private Component rootComp;
	private ExceptionDisplayer(){
		
	}

	public static synchronized ExceptionDisplayer getInstance(){
		if (instance == null) instance = new ExceptionDisplayer();
		return instance;
	}
	
	public synchronized void displayException(Exception ex){
		
		/*
		if(rootComp != null)
			*/
		ex.printStackTrace();
	}
	
	public void registerParentComp(Component comp){ this.rootComp = comp;}
	
}
