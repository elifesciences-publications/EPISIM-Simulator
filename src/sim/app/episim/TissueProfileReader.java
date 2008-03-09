package sim.app.episim;

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
import java.util.Scanner;
import java.util.Map.Entry;

import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotReader;


public class TissueProfileReader {
private static TissueProfileReader instance;

public static final String TISSUEDESCRIPTION = "Tissue description";
public static final String IMAGEID = "Image ID";
public static final String RESOLUTION = "Resolution [µm/pix]";
public static final String BASALLAMINA = "Basal lamina";
public static final String SURFACE = "Surface";
public static final String FULLCONTOUR = "Full Contour";
public static final String EPIDERMALWIDTH = "Epidermal Width";
public static final String MEANEPIDERMALTHICKNESS = "Mean epidermal thickness";
public static final String MAXIMUMEPIDERMALTHICKNESS = "Maximum epidermal thickness";

private ArrayList<Point2D> basalLayerPoints;
private ArrayList<Point2D> surfacePoints;
private double resolution;
private String imageid;
private String tissueDescription;
private double epidermalWidth;
private double meanEpidermalThickness;
private double maximumEpidermalThickness;

private String scannerState = "";
	
	private TissueProfileReader(){
		basalLayerPoints = new ArrayList<Point2D>();
		surfacePoints = new ArrayList<Point2D>();
	} 
	
	public synchronized static TissueProfileReader getInstance(){
		if(instance == null) instance = new TissueProfileReader();
		
		return instance;
		
	}
	
	private void reset(){
		resolution = 0;
		imageid = "";
		tissueDescription="";
		basalLayerPoints.clear();
		surfacePoints.clear();
		epidermalWidth = 0;
		meanEpidermalThickness= 0;
		maximumEpidermalThickness=0;
	}
	
	public LoadedTissue loadTissue(File path){
		
		
		if(path != null ){
			reset();
			FileReader fileRead = null;
			BufferedReader bufferedReader = null;
			 try{
				fileRead = new FileReader(path);
			
			
			 bufferedReader = new BufferedReader(fileRead);
			 
			 
			 
		 
			 String line= null; 
		do{	
			 line = bufferedReader.readLine();
			
			if(line!= null){
				
				if(line.trim().equals(BASALLAMINA)) scannerState = BASALLAMINA; 
				else if(line.trim().equals(TISSUEDESCRIPTION)) scannerState = TISSUEDESCRIPTION;
				else if(line.trim().equals(IMAGEID)) scannerState = IMAGEID;
				else if(line.trim().equals(RESOLUTION)) scannerState = RESOLUTION;
				else if(line.trim().equals(SURFACE)) scannerState = SURFACE;
				else if(line.trim().equals(FULLCONTOUR)) scannerState = FULLCONTOUR;
				else if(line.trim().equals(EPIDERMALWIDTH)) scannerState = EPIDERMALWIDTH;
				else if(line.trim().equals(MEANEPIDERMALTHICKNESS)) scannerState = MEANEPIDERMALTHICKNESS;
				else if(line.trim().equals(MAXIMUMEPIDERMALTHICKNESS)) scannerState = MAXIMUMEPIDERMALTHICKNESS;
				else addValue(line);
	
			}
				
			
			
		}
		while(line !=null);
		
		    
			return new LoadedTissue(basalLayerPoints, surfacePoints, resolution, imageid, tissueDescription, epidermalWidth, meanEpidermalThickness, maximumEpidermalThickness);
			
		}
		  catch (EOFException e){
			  try{
				  bufferedReader.close();
			}
			catch (IOException e1){
				 ExceptionDisplayer.getInstance().displayException(e);
	      	  return null;
			}
			  return null;
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
			return null;
		}
	}
	
	private void addValue(String value){
		if(scannerState.equals(BASALLAMINA)){
			String[] strings = value.split("\t");
			if(strings.length == 2){
				basalLayerPoints.add(new Point2D.Double(Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
		
			} 
		}
		else if(scannerState.equals(TISSUEDESCRIPTION)) tissueDescription = value;
		else if(scannerState.equals(IMAGEID)) imageid = value;
		else if(scannerState.equals(RESOLUTION)){
			resolution = Double.parseDouble(value);
		}
		else if(scannerState.equals(SURFACE)){
			String[] strings = value.split("\t");
			if(strings.length == 2){
				surfacePoints.add(new Point2D.Double(Double.parseDouble(strings[0]), Double.parseDouble(strings[1])));
		
			} 
		}
		else if(scannerState.equals(FULLCONTOUR)){
			//Ignore
		}
		else if(scannerState.equals(EPIDERMALWIDTH)){
			epidermalWidth = Double.parseDouble(value);
		}
		else if(scannerState.equals(MEANEPIDERMALTHICKNESS)){ 
			meanEpidermalThickness = Double.parseDouble(value);
		}
		else if(scannerState.equals(MAXIMUMEPIDERMALTHICKNESS)){ 
			maximumEpidermalThickness = Double.parseDouble(value);
		}
		
		
	
}
}