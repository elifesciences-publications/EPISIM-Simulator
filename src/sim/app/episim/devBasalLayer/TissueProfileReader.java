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


public class TissueProfileReader {
private static TissueProfileReader instance;

public static final String TISSUEDESCRIPTION = "Tissue description";
public static final String IMAGEID = "Image ID";
public static final String RESOLUTION = "Resolution [µm/pix]";
public static final String BASALLAMINA = "Basal lamina";
public static final String SURFACE = "Surface";
public static final String FULLCONTOUR = "Full Contour";
public static final String EPIDERMALWIDTH = "Epidermal Width";
public static final String MEANEPIDERMALWIDTH = "Mean epidermal thickness";
public static final String MAXIMUMEPIDERMALWIDTH = "Maximum epidermal thickness";
	
	private TissueProfileReader(){
		
	} 
	
	public synchronized static TissueProfileReader getInstance(){
		if(instance == null) instance = new TissueProfileReader();
		
		return instance;
		
	}
	
	
	
	public List<Point2D> loadBasalLayer(File path){
		List<Point2D> points = new ArrayList<Point2D>();
		
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
					points.add(new Point2D.Double(Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
				}
				
			}
			
		}
		while(line !=null);
		
		    
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
