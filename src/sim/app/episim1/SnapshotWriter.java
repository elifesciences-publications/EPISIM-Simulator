package sim.app.episim1;

import java.util.LinkedList;
import java.util.List;
import java.io.*;

import javax.swing.JOptionPane;


public class SnapshotWriter {
	
	private List<SnapshotListener> listeners;
	private File snapshotPath;
	private static SnapshotWriter instance;
	private int counter = 1;
	private SnapshotWriter(){
		listeners = new LinkedList<SnapshotListener>();
	} 
	
	public synchronized static SnapshotWriter getInstance(){
		if(instance == null) instance = new SnapshotWriter();
		
		return instance;
		
	}
	
	public void addSnapshotListener(SnapshotListener listener){
		listeners.add(listener);
	}
	
	public void writeSnapshot(){
		File actualSnapshotPath = snapshotPath;
		if(snapshotPath != null && !snapshotPath.isDirectory()){
		  try{
			  if(counter > 1 && snapshotPath.exists()){
				  
				  actualSnapshotPath = new File(getNewPath(snapshotPath.getAbsolutePath()));
				  counter++;
			  }
			  else if(snapshotPath.exists())counter++;
			FileOutputStream fOut = new FileOutputStream(actualSnapshotPath);
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			
			for(SnapshotListener listener : listeners){
				for(SnapshotObject object : listener.getSnapshotObjects()){
					if(object.getIdentifier().equals(SnapshotObject.EPIDERMIS)){
						System.out.println(((EpidermisClass)object.getSnapshotObject()).schedule.time());
					}
					oOut.writeObject(object);
				}
			}
			oOut.flush();
			oOut.close();
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
		}
		}
		else{
			ExceptionDisplayer.getInstance().displayException(new NullPointerException("SnapshotWriter: Filepath was null!"));
		}
		
	}

	private String getNewPath(String oldPath){
		return (oldPath.substring(0, oldPath.length()- 4) + "_"+ counter+oldPath.substring(oldPath.length()- 4, oldPath.length()));
	}
	public File getSnapshotPath() {
	
		return snapshotPath;
	}

	
	public void setSnapshotPath(File snapshotPath) {
	
		this.snapshotPath = snapshotPath;
	}
	
	
}
