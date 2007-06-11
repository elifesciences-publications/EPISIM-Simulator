package sim.app.episim.devBasalLayer;



import sim.app.episim.ExceptionDisplayer;
import sim.portrayal.*;


import java.awt.*;
import java.awt.geom.*;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import sim.util.Double2D;



public class BasementMembranePortrayal2DDev extends SimplePortrayal2D{
	
	   
	    private double width;
	    private double height;
	    private DrawInfo2D lastActualInfo;
	    private DrawInfo2D deltaInfo;
	    private ArrayList<Point2D> interpolationPoints;
	    private HashSet<Double> xValues;
	    private boolean hitAndButtonPressed = false;
	    
	    private Point2D actDraggedPoint = null;
	
	    private static final double DELTACROSS = 10;
	    private static final double DELTAPOINT = 10;
	    
	    public BasementMembranePortrayal2DDev(double width, double height) {
	   	 this.width = width;
	   	 this.height = height;
	   	
	   	 
	   	 
	   	 xValues = new HashSet<Double>();
	   	
	   	
	   	 
	   
	   	 
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	    {
	       if(info != null && interpolationPoints.size() > 0){
	      	 if(deltaInfo == null) deltaInfo = info; //wird beim ersten Aufruf gesetzt.
	      	 lastActualInfo = info;
	      	 
	      	 Collections.sort(interpolationPoints, new InterpolationPointComparator());
	      	
//      	drawInterpolationPoints(graphics);
	      	 graphics.setColor(new Color(255, 99, 0));
	      	  //graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
					GeneralPath polygon = new GeneralPath();
			polygon.moveTo(interpolationPoints.get(0).getX(), interpolationPoints.get(0).getY());
	      	 for(int i = 0; i < interpolationPoints.size(); i++){
	      		 
	      		 Point2D actPoint = interpolationPoints.get(i);
	     
	      		 
	      		 
	      		polygon.lineTo(interpolationPoints.get(i).getX(), interpolationPoints.get(i).getY());
	      			 
	      			
	      		 
	      	 }
	      	  
	      	  
	     	 
	      	//  graphics.setColor(new Color(255, 99, 0));
	      	  //graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	      	  
	      	 AffineTransform transform = new AffineTransform();
    	      
  	        
	      	// transform.translate(0, 100);
	      	double scaleX = width / polygon.getBounds2D().getWidth();
	      	
  	       transform.scale(scaleX, scaleX);
  	       polygon = (GeneralPath) polygon.createTransformedShape(transform);
  	     
  	     transform.setToTranslation(lastActualInfo.clip.getMinX()-getDeltaX(), 
  	   		lastActualInfo.clip.getMinY()-getDeltaY());
 	    
	     polygon = (GeneralPath) polygon.createTransformedShape(transform);
	      	        graphics.draw(polygon);
				
				   
	      	       
	       }     
	      	
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
	 private void drawInterpolationPoints(Graphics2D graphics) {

		if(graphics != null){
			for(Point2D point : interpolationPoints){
				GeneralPath polygon = new GeneralPath();
				polygon.moveTo(lastActualInfo.clip.getMinX() - getDeltaX() + point.getX() - DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + point.getY() - DELTACROSS);
				polygon.lineTo(lastActualInfo.clip.getMinX() - getDeltaX() + point.getX() + DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + point.getY() + DELTACROSS);
				polygon.moveTo(lastActualInfo.clip.getMinX() - getDeltaX() + point.getX() + DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + point.getY() - DELTACROSS);
				polygon.lineTo(lastActualInfo.clip.getMinX() - getDeltaX() + point.getX() - DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + point.getY() + DELTACROSS);

				graphics.setColor(Color.RED);
				graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

				graphics.draw(polygon);
			}
		}
	}
	 
	public boolean getInterpolationPoint(Point2D mouseposition){
		if(mouseposition != null && lastActualInfo != null){
	   	
		mouseposition = new Point2D.Double(mouseposition.getX()-lastActualInfo.clip.getMinX()+getDeltaX(),
		                             mouseposition.getY()-lastActualInfo.clip.getMinY()+getDeltaY());
		
		for(Point2D point: interpolationPoints){
			
		
			if(mouseposition.getX() > (point.getX() - DELTAPOINT) 
				&& mouseposition.getX() < (point.getX() + DELTAPOINT)
				&& mouseposition.getY() > (point.getY() - DELTAPOINT)
				&& mouseposition.getY() < (point.getY() + DELTAPOINT)
				&& !hitAndButtonPressed){
				
				if(!xValues.contains(mouseposition.getX()))point.setLocation(mouseposition.getX(), mouseposition.getY());
				hitAndButtonPressed=true;
				actDraggedPoint = point;
				return true;
			}
			else if(hitAndButtonPressed){
				if(!xValues.contains(mouseposition.getX()))actDraggedPoint.setLocation(mouseposition.getX(), mouseposition.getY());
				return true;
			}
			
		}
		}
		return false;
	
	}
	public void addInterpolationPoint(Point2D mouseposition){
	   
		if(mouseposition != null && lastActualInfo != null){
	   	
		mouseposition = new Point2D.Double(mouseposition.getX()-lastActualInfo.clip.getMinX()+getDeltaX(),
		                             mouseposition.getY()-lastActualInfo.clip.getMinY()+getDeltaY());
		

		if(!xValues.contains(mouseposition.getX())){
			interpolationPoints.add(mouseposition);
			xValues.add(mouseposition.getX());
		}
		
		
		}
		
	}




	
	public boolean isHitAndButtonPressed() {
	
		return hitAndButtonPressed;
	}




	
	public void setHitAndButtonPressed(boolean hitAndButtonPressed) {
		if(!hitAndButtonPressed) actDraggedPoint = null;
		this.hitAndButtonPressed = hitAndButtonPressed;
	}   
}
