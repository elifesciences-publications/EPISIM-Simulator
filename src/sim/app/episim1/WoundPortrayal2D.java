package sim.app.episim1;

import sim.portrayal.*;
import sim.util.*;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.geom.*;


public class WoundPortrayal2D extends SimplePortrayal2D{
	
	   
	  

	    
	    private List<Double2D> woundRegionCoordinates = new ArrayList<Double2D>();
	    private boolean closeWoundRegionPath = false;
	    
	    private double width;
	    
	    private DrawInfo2D lastActualInfo;
	    
	    private GeneralPath polygon;
	    
	    public WoundPortrayal2D(double width, double heigth) {
	   	 
	   	 this.width = width;
	   	 
	    }
	    private void createPolygon(DrawInfo2D info){
	   	 if(woundRegionCoordinates.size() > 1){
		   		
	   		 polygon = new GeneralPath();
	   		 ((GeneralPath)polygon).moveTo(lastActualInfo.clip.getMinX()+ woundRegionCoordinates.get(0).x, 
	   				 lastActualInfo.clip.getMinY()+ woundRegionCoordinates.get(0).y);
	   		 for(Double2D coord : woundRegionCoordinates){
	   			polygon.lineTo(info.clip.getMinX() + coord.x, info.clip.getMinY() + coord.y);
	   			
	   		 }
	   		if(closeWoundRegionPath)polygon.closePath();
	    }
	    }
	        
	    
	    // assumes the graphics already has its color set
	    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	    {            
	       graphics = (Graphics2D) graphics.create(); 
	   	 
	      	  createPolygon(info);
	      
	   		  graphics.setColor(Color.red);
	   		  
	      	  graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	      	  if(polygon != null && info.clip.contains(polygon.getBounds2D()))graphics.draw(polygon);
	   	 
	      	     
	       lastActualInfo = info;
	    }
	    
	    public void addMouseCoordinate(Double2D double2d){
	   	if(double2d != null && lastActualInfo != null){
	   	 Double2D newDouble2d = new Double2D(double2d.x - lastActualInfo.clip.getMinX(),
	   			 										 double2d.y - lastActualInfo.clip.getMinY());
	   	
	   	 woundRegionCoordinates.add(newDouble2d);
	   	}
	    }
	    
	    public void closeWoundRegionPath(boolean closewoundRegionPath){
	   	 
	   	 this.closeWoundRegionPath =closewoundRegionPath;
	    }
	    
	    public void clearWoundRegionCoordinates(){ 
	   	 woundRegionCoordinates.clear();
	    }
	   
}