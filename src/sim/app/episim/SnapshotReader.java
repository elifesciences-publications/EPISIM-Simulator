package sim.app.episim;




import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.io.*;


public class SnapshotReader {
	
	
	
	private static SnapshotReader instance;
	
	private SnapshotReader(){
		
	} 
	
	public synchronized static SnapshotReader getInstance(){
		if(instance == null) instance = new SnapshotReader();
		
		return instance;
		
	}
	
	
	
	public List<SnapshotObject> loadSnapshot(File snapshotPath){
		List<SnapshotObject> objects = new ArrayList<SnapshotObject>();
		if(snapshotPath != null ){
			FileInputStream fIn = null;
			ObjectInputStream oIn = null;
		  try{
			  fIn = new FileInputStream(snapshotPath);
			oIn = new ObjectInputStream(fIn);
			
			Object obj = null;
			obj = oIn.readObject();
			while(obj != null){
				if(obj instanceof SnapshotObject)objects.add(((SnapshotObject) obj));
				
				obj = oIn.readObject();
			}
			
			oIn.close();
			return objects;
		}
		  catch (EOFException e){
			  try{
				oIn.close();
			}
			catch (IOException e1){
				 ExceptionDisplayer.getInstance().displayException(e);
	      	  return null;
			}
			  return objects;
		  }
        catch (ClassNotFoundException e){
      	  ExceptionDisplayer.getInstance().displayException(e);
      	  return null;
		  }
		catch (IOException e){
			
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
		}
		else{
			ExceptionDisplayer.getInstance().displayException(new NullPointerException("SnapshotWriter: Filepath was null!"));
			return objects;
		}
	}

	
	

	
	
	
	
}
