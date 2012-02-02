package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import javax.swing.border.BevelBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.persistence.SimulationStateFile;


public class EpisimProgressWindow {
	private JLabel progressLabel;
	private JProgressBar progressBar;
	private JDialog progressWindow;
	public EpisimProgressWindow(Frame owner){
		progressWindow = new JDialog(owner,true);
		progressWindow.setUndecorated(true);
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
	
	public void setProgressText(String text){
		progressLabel.setText(text);
	}
	
	private boolean taskFinished = false;
	public void showProgressWindowForTask(final EpisimProgressWindowCallback callback){
		taskFinished = false;
		Runnable r = new Runnable(){			
			public void run() {				
				callback.executeTask();
				progressWindow.setVisible(false);
				callback.taskHasFinished();
				taskFinished = true;
         }
	
		};
		Thread t = new Thread(r);
		t.start();
		if(!taskFinished)progressWindow.setVisible(true);
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
