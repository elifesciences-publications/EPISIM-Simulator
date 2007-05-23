package sim.app.episim1;

import sim.portrayal.*;
import sim.util.*;

import java.util.*;
import java.util.List;

import java.awt.*;
import java.awt.geom.*;


public class WoundPortrayal2D extends SimplePortrayal2D implements SnapshotListener{
	
	   
	  

	    
	    /**
	 * 
	 */
	private static final long serialVersionUID = -569327606127370200L;
		private List<Double2D> woundRegionCoordinates = new ArrayList<Double2D>();
	    private boolean closeWoundRegionPath = false;
	    
	    private double width;
	    private double height;
	    
	    private DrawInfo2D lastActualInfo;
	    
	    private DrawInfo2D deltaInfo;
	    
	    private GeneralPath polygon;
	    
	    private boolean refreshInfo = true;
	    
	    public WoundPortrayal2D(double width, double height) {
	   	 
	   	 this.width = width;
	   	 this.height = height;
	   	 SnapshotWriter.getInstance().addSnapshotListener(this);
	   	 
	    }
	    private void createPolygon(DrawInfo2D info){
	   	 {
	   		 
	   		 polygon = new GeneralPath();
	   		 ((GeneralPath)polygon).moveTo(lastActualInfo.clip.getMinX()+ woundRegionCoordinates.get(0).x - getDeltaX(), 
	   				 lastActualInfo.clip.getMinY()+ woundRegionCoordinates.get(0).y- getDeltaY());
	   		 for(Double2D coord : woundRegionCoordinates){
	   			polygon.lineTo(lastActualInfo.clip.getMinX() - getDeltaX()+ coord.x, lastActualInfo.clip.getMinY() - getDeltaY() + coord.y);
	   			
	   		 }
	   		if(closeWoundRegionPath)polygon.closePath();
	    }
	    }
	        
	    
	    // assumes the graphics already has its color set
	    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	    {         
	   	 lastActualInfo = info;
	   	 graphics.setColor(Color.red);
  		  
     	  graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	       
	   	  graphics = (Graphics2D) graphics.create(); 
	   	 
	   	  if(woundRegionCoordinates.size() > 1) createPolygon(info);
	      
	   	  else polygon = null;
	      	 
   	     
   	        
              
      	     
	      	  if(polygon != null && lastActualInfo.clip.contains(polygon.getBounds2D())){
	      		 // AffineTransform transform = new AffineTransform();
	       	      
		      	//  transform.setToTranslation(getTranslationX(info), getTranslationY(info));
	   	        //polygon = (GeneralPath) polygon.createTransformedShape(transform);
	      		  
	      		  graphics.draw(polygon);
	      	  }
	   	 
	      	    
	       
	   	 
	    }
	    
	    public void addMouseCoordinate(Double2D double2d){
	   	 deltaInfo = lastActualInfo;
	   	if(double2d != null && lastActualInfo != null){
	   	 Double2D newDouble2d = new Double2D(double2d.x-lastActualInfo.clip.getMinX(),
	   			 										 double2d.y-lastActualInfo.clip.getMinY());
	   	
	   	 woundRegionCoordinates.add(newDouble2d);
	   	}
	    }
	    
	    public void closeWoundRegionPath(boolean closewoundRegionPath){
	   	 refreshInfo =false;
	   	 this.closeWoundRegionPath =closewoundRegionPath;
	   	 
	    }
	    
	    public GeneralPath getWoundRegion(){
	   	 return polygon;
	    }
	    public void clearWoundRegionCoordinates(){ 
	   	 woundRegionCoordinates.clear();
	    }
	    private double getDeltaX(){
	   	 if(lastActualInfo.clip.width< width){
	   		 return lastActualInfo.clip.getMinX() - deltaInfo.clip.getMinX();
	   	 }
	   	 else return 0;
	    }
	    
	    private double getDeltaY(){
	   	 
	   	 if(lastActualInfo.clip.height < height){
	   		 return lastActualInfo.clip.getMinY() - deltaInfo.clip.getMinY();
	   	 }
	   	 else return 0;
	    }
		public List<SnapshotObject> getSnapshotObjects() {

			List<SnapshotObject> list = new ArrayList<SnapshotObject>();
			list.add(new SnapshotObject(SnapshotObject.WOUND, woundRegionCoordinates));
			return list;
		}
		public void setWoundRegionCoordinates(List<Double2D> woundRegionCoordinates){
			this.woundRegionCoordinates.clear();
			this.woundRegionCoordinates = woundRegionCoordinates;
			this.closeWoundRegionPath = true;
		}
	   
}