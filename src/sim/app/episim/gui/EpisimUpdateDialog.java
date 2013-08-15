package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.UIManager;

import org.jfree.ui.HorizontalAlignment;

import binloc.ProjectLocator;

import sim.app.episim.EpisimUpdater;
import sim.app.episim.EpisimUpdater.EpisimUpdateState;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.EpisimUpdater.EpisimUpdateCallback;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sun.net.ftp.FtpProtocolException;


public class EpisimUpdateDialog {
	private JDialog dialog;
	private JPanel progressPanel;
	private JLabel progressLabel;
	private JProgressBar progressBar;
	
	private JButton updateButton;
	private JButton cancelButton;
	private JButton downloadButton;
	private JButton restartButton;
	
	private final int DIALOG_WIDTH = 450;
	private final int DIALOG_HEIGHT = 230;
	private final int BORDER_SIZE = 20;
	
	private EpisimUpdater episimUpdater;
	
	private Frame owner;
	private boolean startup = false;
	private UpdateCancelledCallback cancelledCallback = null;
	public EpisimUpdateDialog(Frame owner){
		this(owner, false);
	}
	public EpisimUpdateDialog(Frame owner, boolean startup){
		this.owner = owner;
		this.startup = startup;
		createDialog();
	}
	
	private void createDialog(){
		dialog = new JDialog(owner,"Update EPISIM Simulator");
		dialog.setSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
		dialog.setResizable(false);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		buildGUI();
	}
	
	private void buildGUI(){
		dialog.getContentPane().setLayout(new BorderLayout());
		((JPanel) dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));		
		
		JPanel buttonPanel = new JPanel();
	
		BoxLayout boxLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(boxLayout);
			
		updateButton = new JButton("Update");
		updateButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e){			
				downloadUpdate(episimUpdater);
				cancelButton.setEnabled(false);
				updateButton.setEnabled(false);
			}
		});		
		updateButton.setVisible(false);
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e){			
				dialog.setVisible(false);
				if(cancelledCallback != null) cancelledCallback.updateWasCancelled();
			}
		});		
		
		downloadButton = new JButton("Open Download Site");
		downloadButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e){			
				dialog.setVisible(false);
				if(Desktop.isDesktopSupported())
				{
					try{
						Desktop.getDesktop().browse(new URI("http://tigacenter.bioquant.uni-heidelberg.de/downloads.html"));
	            }
	            catch (Exception ex){
		            ex.printStackTrace();
	            }           
				}
			}
		});
		downloadButton.setVisible(false);
		
		restartButton = new JButton("Restart");
		restartButton.addActionListener(new ActionListener(){			
			public void actionPerformed(ActionEvent e){			
				try{
		         episimUpdater.restartApplication();
		      }
		      catch (Exception ex){
		      	progressLabel.setText("    Error while restarting EPISIM Simulator");
		        	progressLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
		        	ExceptionDisplayer.getInstance().displayException(ex);
		      	cancelButton.setEnabled(true);
		      	cancelButton.setVisible(true);
		      	restartButton.setVisible(false);
		      }
			}
		});
		restartButton.setVisible(false);
				
		
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(updateButton);
		buttonPanel.add(downloadButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(20, 10)));
		buttonPanel.add(cancelButton);
		buttonPanel.add(restartButton);
		
		
		
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		progressPanel = new JPanel(new BorderLayout(15, 15));
		
		
		progressLabel = new JLabel(" ");
		
		progressLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
	
		progressPanel.add(progressLabel, BorderLayout.CENTER);
		progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
		progressBar = new JProgressBar();
		
		progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width,progressBar.getPreferredSize().height+10));
		progressPanel.add(progressBar, BorderLayout.SOUTH);
		
		
		dialog.getContentPane().add(progressPanel, BorderLayout.CENTER);
		
	}
	
	
	
	public void showUpdateDialog(){
		showUpdateDialog(null);
	}
	public void showUpdateDialog(UpdateCancelledCallback callback){
		this.cancelledCallback = callback;
		createDialog();
		// if not on screen right now, move to center of screen
			  if (!dialog.isVisible())
			   {
				  if(dialog.getParent() == null){
						Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
						dialog.setLocation(((int)(screenDim.getWidth() /2) - (dialog.getWidth()/2)), 
						((int)(screenDim.getHeight() /2) - (dialog.getHeight()/2)));
					}
					else{
						Dimension parentDim = dialog.getParent().getSize();
						dialog.setLocation(((int)(dialog.getParent().getLocation().getX()+((parentDim.getWidth() /2) - (dialog.getWidth()/2)))), 
						((int)(dialog.getParent().getLocation().getY()+((parentDim.getHeight() /2) - (dialog.getHeight()/2)))));
					}
			   }
			  
			  // show it!
			 if(!startup) dialog.setVisible(true);
			  checkForUpdates();
	}
	
	
	
	private boolean operationSuccessful = false;
	
	private void checkForUpdates(){
		episimUpdater = new EpisimUpdater();
		EpisimUpdateState updateState= null;
		try{
	      
	      updateState= episimUpdater.checkForUpdates();
      }
      catch (Exception e){
      	progressLabel.setText("    Cannot connect to EPISIM update server");
      	progressLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
	      ExceptionDisplayer.getInstance().displayException(e);
	      updateState = null;
      }
		
	
		if(updateState != null){
			if(updateState == EpisimUpdateState.POSSIBLE){
				updateButton.setVisible(true);
				updateButton.setEnabled(true);
				progressLabel.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;There are updates available.<br>&nbsp;&nbsp;&nbsp;&nbsp;Your version: "+ EpisimSimulator.versionID + "<br>&nbsp;&nbsp;&nbsp;&nbsp;New Version: "+ episimUpdater.getMostCurrentVersion()+"</html>");
				if(startup) dialog.setVisible(true);
			}
			else if(updateState == EpisimUpdateState.NOTPOSSIBLE && !startup){
				progressLabel.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Your version ("+ EpisimSimulator.versionID +") is too old to update.<br>&nbsp;&nbsp;&nbsp;&nbsp;Please download the newest version and re-install EPISIM Simulator<br>&nbsp;&nbsp;&nbsp;&nbsp;manually.</html>");
				downloadButton.setEnabled(true);
				downloadButton.setVisible(true);
			}
			else if(updateState == EpisimUpdateState.NOTAVAILABLE && !startup){
				cancelButton.setText("OK");
				progressBar.setVisible(false);
				progressLabel.setText("    There are no updates available");			
			}
		}
      
	}
		
	
	
	private void downloadUpdate(final EpisimUpdater updater){
		progressLabel.setText("    Connect to EPISIM update server");
		Runnable r = new Runnable(){			
			public void run() {
				
									
											
					try{
						updater.downloadUpdate(new EpisimUpdateCallback(){
							         	 
							    public void sizeOfUpdate(int size){
							     		progressBar.setMaximum(size);
							    		progressBar.setMinimum(0);
							   		progressBar.setValue(0);
							     		progressBar.setStringPainted(true);  
							     		progressLabel.setText("    Downloading update bundle: "+updater.readableFileSize());
							    }
							       		
							    public void progressOfUpdate(int progress){
							    		progressBar.setValue(progressBar.getValue()+progress);
							    }
							       		
							    public void updateHasFinished(){
							   	 	progressLabel.setText("    Disconnect from EPISIM update server");
							   	 	try{
								        
								         operationSuccessful=true;
								         installUpdate(updater);
							         }
							         catch (Exception e){
							         	progressLabel.setText("    Error while installing EPISIM update ");
							         	progressLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
							         	ExceptionDisplayer.getInstance().displayException(e);
							         	cancelButton.setEnabled(true);
							         }
							    }
							    });
					     }
					     catch (Exception e){
					       	progressLabel.setText("    Error while downloading update from EPISIM update server");
					        	progressLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
					        	ExceptionDisplayer.getInstance().displayException(e);
					        	cancelButton.setEnabled(true);
					     }				
					}								
         	
		};
		Thread t = new Thread(r);
		t.start();
	}
	
	private void installUpdate(final EpisimUpdater updater){
		
		Runnable r = new Runnable(){			
			public void run() {
				try{
					updater.installUpdate(new EpisimUpdateCallback(){							         	 
						    public void sizeOfUpdate(int size){
						     		progressBar.setMaximum(size);
						    		progressBar.setMinimum(0);
						   		progressBar.setValue(0);
						     		progressBar.setStringPainted(true);  
						     		progressLabel.setText("    Installing EPISIM update");
						    }							       		
						    public void progressOfUpdate(int progress){
						    		progressBar.setValue(progressBar.getValue()+progress);
						    }
						       		
						    public void updateHasFinished(){
						   	 	progressLabel.setText("    You have to restart EPISIM Simulator to complete the update process.");
						   	 	progressBar.setValue(progressBar.getMaximum());
						   	 	updateButton.setVisible(false);
						   	 	cancelButton.setVisible(false);
						   	 	restartButton.setVisible(true);
						    }
						    });
				     }
				     catch (Exception e){
				       	progressLabel.setText("    Error while installing EPISIM update");
				        	progressLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
				        	ExceptionDisplayer.getInstance().displayException(e);
				        	cancelButton.setEnabled(true);
				     }				
				}       	
		};
		Thread t = new Thread(r);
		t.start();
	}
	public interface UpdateCancelledCallback{
		void updateWasCancelled();
	}

}
