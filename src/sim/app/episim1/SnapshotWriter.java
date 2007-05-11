package sim.app.episim1;

import java.util.LinkedList;
import java.util.List;
import java.io.*;


public class SnapshotWriter {
	
	private List<SnapshotListener> listeners;
	
	private static SnapshotWriter instance;
	
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
	
	public void writeSnapshot(File file){
		if(file != null ){
		  try{
			FileOutputStream fOut = new FileOutputStream(file);
			ObjectOutputStream oOut = new ObjectOutputStream(fOut);
			
			for(SnapshotListener listener : listeners){
				for(SnapshotObject object : listener.deliverObjects()){
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
	
	
}
