package sim.app.episim.devBasalLayer;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SnapshotObject;
import sim.app.episim.SnapshotReader;


public class BasalLayerReader {
private static BasalLayerReader instance;
	
	private BasalLayerReader(){
		
	} 
	
	public synchronized static BasalLayerReader getInstance(){
		if(instance == null) instance = new BasalLayerReader();
		
		return instance;
		
	}
	
	
	
	public ArrayList<Point2D> loadBasalLayer(File path){
		ArrayList<Point2D> points = new ArrayList<Point2D>();
		HashMap<Double, Double> values = new HashMap<Double, Double>();
		if(path != null ){
			FileReader fileRead = null;
			BufferedReader bufferedReader = null;
			 try{
				fileRead = new FileReader(path);
			
			
			 bufferedReader = new BufferedReader(fileRead);
		 
			 String line= null; 
		do{	
			 line = bufferedReader.readLine();
			
			if(line!= null){
				String[] strings = line.split("\t");
				if(strings.length == 2){
					values.put(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]));
				}
				
			}
			
		}
		while(line !=null);
		
		    Iterator<Entry<Double, Double>> iter =values.entrySet().iterator();
		    while(iter.hasNext()){
		   	 Entry<Double, Double> entry = iter.next();
		   	 points.add(new Point2D.Double(entry.getKey(), entry.getValue()));
		    }
			return points;
		}
		  catch (EOFException e){
			  try{
				  bufferedReader.close();
			}
			catch (IOException e1){
				 ExceptionDisplayer.getInstance().displayException(e);
	      	  return null;
			}
			  return points;
		  }
		  catch (FileNotFoundException e){
				ExceptionDisplayer.getInstance().displayException(e);
				return null;
			}
       
		catch (IOException e){
			
			ExceptionDisplayer.getInstance().displayException(e);
			return null;
		}
		
		}
		else{
			ExceptionDisplayer.getInstance().displayException(new NullPointerException("BasalLayerReader: Filepath was null!"));
			return points;
		}
	}

	
}
