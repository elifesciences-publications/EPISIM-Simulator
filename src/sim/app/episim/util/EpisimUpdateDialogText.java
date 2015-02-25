package sim.app.episim.util;

import java.io.IOException;

import javax.swing.UIManager;

import sim.app.episim.EpisimUpdater;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateServer;
import sim.app.episim.EpisimUpdater.EpisimUpdateCallback;
import sim.app.episim.EpisimUpdater.EpisimUpdateState;
import sim.app.episim.gui.EpisimSimulator;


public class EpisimUpdateDialogText {
	private EpisimUpdater episimUpdater = null;
	public EpisimUpdateDialogText(){
		
	}
	public void showUpdateTextDialog(){
		
		checkForUpdates();
	}
	
	private void checkForUpdates(){
		episimUpdater = new EpisimUpdater();
		EpisimUpdateState updateState= null;
		System.out.println("Checking for EPISIM Simulator updates...");
		try{	      
	      updateState= episimUpdater.checkForUpdates();
      }
      catch (Exception e){
      	System.out.println("Cannot connect to EPISIM update server");
	      EpisimExceptionHandler.getInstance().displayException(e);
	      updateState = null;
      }
		
	
		if(updateState != null){
			if(updateState == EpisimUpdateState.POSSIBLE){
				System.out.println("There are updates available. Your EPISIM Simulator version: "+ EpisimSimulator.versionID + "   New EPISIM Simulator version: "+ episimUpdater.getMostCurrentVersion());
				String input="";
				do{
				input = getUserInput();
				}
				while(input.trim().length()!=1 || (input.trim().length()==1&&!(input.trim().equalsIgnoreCase("y")||input.trim().equalsIgnoreCase("n"))));
				if(input.trim().equalsIgnoreCase("y"))downloadUpdate(episimUpdater);
			}
			else if(updateState == EpisimUpdateState.NOTPOSSIBLE){
				System.out.println("Your EPISIM Simulator version ("+ EpisimSimulator.versionID +") is too old to update.");
				System.out.println("Please download the newest version and re-install EPISIM Simulator manually.");
			}
			else if(updateState == EpisimUpdateState.NOTAVAILABLE ){
				System.out.println("There are no updates available");			
			}
		}      
	}
	
	private boolean updateFinished = false;
	
	private void downloadUpdate(final EpisimUpdater updater){
		System.out.println("Connect to EPISIM update server");
		updateFinished = false;
		Runnable r = new Runnable(){			
			public void run() {	
											
					try{
						updater.downloadUpdate(new EpisimUpdateCallback(){
	
							 	private int updateSize = 0;	
							 	private int updateProgress = 0;     	 
							    public void sizeOfUpdate(int size){
							   	 	updateSize = size >0 ?size:1;
							     		System.out.print("\r");	         
							         System.out.print("Downloading update bundle: "+updater.readableFileSize()+" ("+(int)(((double)updateProgress/(double)updateSize) *100)+"%)");
							    }
							       		
							    public void progressOfUpdate(int progress){
							   	 updateProgress += progress;
							   	 System.out.print("\r");	         
							       System.out.print("Downloading update bundle: "+updater.readableFileSize()+" ("+(int)(((double)updateProgress/(double)updateSize) *100) +"%)");
							    }
							       		
							    public void updateHasFinished(){
							   	 	System.out.print("\r");	         
							   	 	System.out.println("Downloading update bundle: "+updater.readableFileSize()+" (Complete)");
							   	 	System.out.println("Disconnect from EPISIM update server");
							   	 	try{						         
								         installUpdate(updater);
								         updateFinished=true;
							         }
							         catch (Exception e){
							         	System.out.println("Error while installing EPISIM update ");
							         	EpisimExceptionHandler.getInstance().displayException(e);
							         	updateFinished=true;
							         }
							    }
							    }, false);
					     }
					     catch (Exception e){
					   	  System.out.println("Error while downloading update from EPISIM update server");					        	
					        EpisimExceptionHandler.getInstance().displayException(e);
					        updateFinished=true;
					     }				
					}								
	      	
		};
		Thread t = new Thread(r);
		t.start();
		int i = 0;
		while(!updateFinished){
			i++;
			try{
	         Thread.sleep(10);
         }
         catch (InterruptedException e){
	         // TODO Auto-generated catch block
	         e.printStackTrace();
         }
		}
	}


	private boolean installationFinished = false;
	private void installUpdate(final EpisimUpdater updater){
			installationFinished = false;
			Runnable r = new Runnable(){			
				public void run() {
					try{
						updater.installUpdate(new EpisimUpdateCallback(){
							
								 private int updateSize = 0;	
								 private int updateProgress = 0;
							    public void sizeOfUpdate(int size){
							     		updateSize = size >0 ?size:1;
							     		System.out.print("\r");	         
							         System.out.print("Installing EPISIM update: " +(int)(((double)updateProgress/(double)updateSize) *100)+"%");
							    }							       		
							    public void progressOfUpdate(int progress){
							   	 updateProgress += progress;
							   	 System.out.print("\r");	         
							       System.out.print("Installing EPISIM update: " +(int)(((double)updateProgress/(double)updateSize) *100)+"%");
							    }
							       		
							    public void updateHasFinished(){
							   	 System.out.print("\r");	         
							       System.out.print("Installing EPISIM update: Completed");
							       installationFinished=true;
							    }
							    }, false);
					     }
					     catch (Exception e){
					   	  System.out.println();
					   	  System.out.println("Error while installing EPISIM update");				        	
					        EpisimExceptionHandler.getInstance().displayException(e);
					        installationFinished=true;
					     }				
					}       	
			};
			Thread t = new Thread(r);
			t.start();
			int i = 0;
			while(!installationFinished){
				try{
		         Thread.sleep(10);
	         }
	         catch (InterruptedException e){
		         // TODO Auto-generated catch block
		         e.printStackTrace();
	         }				
			}
		}
	
	private static final int BUFFERSIZE = 1024;
	
	
	private String getUserInput(){
		System.out.print("Update Now? (y/n)> ");
		byte[] buffer = new byte[BUFFERSIZE];  // Zeichenpuffer
	      String input = "";
	      int read;
	     
	        try {
	          // Einlesen der Zeichen
	          read = System.in.read(buffer, 0, BUFFERSIZE);
	          // Umwandeln des Pufferinhaltes in einen String
	          input = new String(buffer, 0, read);
	          
	        }
	        catch(IOException e) {
	          e.printStackTrace();
	        }
	        return input;
	}
}
