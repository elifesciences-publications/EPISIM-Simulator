package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;

import javax.swing.border.BevelBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.persistence.SimulationStateFile;


public class EpisimProgressWindow {
	private JLabel progressLabel;
	private JProgressBar progressBar;
	private JWindow progressWindow;
	private boolean taskHasStarted = false;
	private EpisimProgressWindow(Frame owner){
		progressWindow = new JWindow(owner);

		progressWindow.getContentPane().setLayout(new BorderLayout(5, 5));
		if(progressWindow.getContentPane() instanceof JPanel)
			((JPanel)progressWindow.getContentPane()).setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(10,10, 10, 10)));
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressLabel = new JLabel();
		progressWindow.getContentPane().add(progressLabel, BorderLayout.NORTH);
		progressWindow.getContentPane().add(progressBar, BorderLayout.CENTER);
		
		progressWindow.setSize(400, 65);
		
		progressWindow.setLocation(owner.getLocation().x + (owner.getWidth()/2) - (progressWindow.getWidth()/2), 
				owner.getLocation().y + (owner.getHeight()/2) - (progressWindow.getHeight()/2));
		progressWindow.setAlwaysOnTop(true);
	}
	
	private void setProgressText(String text){
		progressLabel.setText(text);
	}	
	
		
	private synchronized void showProgressWindowForTask(final EpisimProgressWindowCallback callback){
		taskHasStarted=false;
		Runnable r = new Runnable(){			
			public void run() {
				taskHasStarted = true;
				callback.executeTask();
				progressWindow.setVisible(false);			
				callback.taskHasFinished();
         }	
		};
		Thread t = new Thread(r);
		t.start();
		while(!taskHasStarted){System.out.print("");/*wait*/}
		progressWindow.setVisible(true);
	}
	
	
	
	public static synchronized void showProgressWindowForTask(Frame owner, String text, EpisimProgressWindowCallback callback){
		EpisimProgressWindow window = new EpisimProgressWindow(owner);
		window.setProgressText(text);
		window.showProgressWindowForTask(callback);
	}
	
	
	
	public interface EpisimProgressWindowCallback {
		
		/**
		 * This method is called to execute the task the progress window is responsible for
		 */
		void executeTask();
		
		/**
		 * This method is called after the task has finished
		 */
		void taskHasFinished();
		
	}
	
	
}
