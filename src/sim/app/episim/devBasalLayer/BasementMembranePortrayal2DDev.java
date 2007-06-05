package sim.app.episim.devBasalLayer;



import sim.portrayal.*;


import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;
import sim.util.Double2D;


public class BasementMembranePortrayal2DDev extends SimplePortrayal2D{
	
	   
	    private double width;
	    private double height;
	    private DrawInfo2D lastActualInfo;
	    private DrawInfo2D deltaInfo;
	    private ArrayList<Point2D> interpolationPoints;
	    
	    private boolean hitAndButtonPressed = false;
	    
	    private Point2D actDraggedPoint = null;
	
	    private static final double DELTACROSS = 10;
	    private static final double DELTAPOINT = 10;
	    
	    public BasementMembranePortrayal2DDev(double width, double height) {
	   	 this.width = width;
	   	 this.height = height;
	   	 interpolationPoints = new ArrayList<Point2D>();
	   	 interpolationPoints.add(new Point2D.Double(50,50));
	   	 interpolationPoints.add(new Point2D.Double(100,10));
	   	 interpolationPoints.add(new Point2D.Double(70,70));
	   	
	   	 interpolationPoints.add(new Point2D.Double(100,100));
	   	 
	   
	   	 
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	    {            
	       if(info != null && interpolationPoints.size() > 0 && (interpolationPoints.size() -1) % 3 == 0){
	   	
	      	  lastActualInfo = info;
	      	  if(deltaInfo == null) deltaInfo = info; //wird beim ersten Aufruf gesetzt.
	      	  GeneralPath polygon = new GeneralPath();
	      	  Point2D actPoint = null, actPoint2 = null,actPoint3 = null;
	      	 for(int i = 0; i < interpolationPoints.size(); i++){
	      		 actPoint = interpolationPoints.get(i);
	      		 drawInterpolationPoint(graphics, info,  actPoint);
	      		 if(i == 0) polygon.moveTo(lastActualInfo.clip.getMinX()-getDeltaX() + actPoint.getX(), 
	      				 lastActualInfo.clip.getMinY()-getDeltaY() + actPoint.getY());
	      		 
	      		 else{
	      			 actPoint2 = interpolationPoints.get(i+1);
	      			 actPoint3 = interpolationPoints.get(i+2);
	      			 i += 2;
	      			 polygon.curveTo(lastActualInfo.clip.getMinX()-getDeltaX() + actPoint.getX(), 
	      					 lastActualInfo.clip.getMinY()-getDeltaY() + actPoint.getY(), 
	      					 lastActualInfo.clip.getMinX()-getDeltaX() + actPoint2.getX(), 
	      					 lastActualInfo.clip.getMinY()-getDeltaY() + actPoint2.getY(), 
	      					 lastActualInfo.clip.getMinX()-getDeltaX() + actPoint3.getX(), 
	      					 lastActualInfo.clip.getMinY()-getDeltaY() + actPoint3.getY());
	      			 drawInterpolationPoint(graphics, info,  actPoint2);
	      			 drawInterpolationPoint(graphics, info,  actPoint3);
	      		 }
	      	 }
	      	  
	      	  
	     	 	/*	final int STEPSIZE = 1;
	     	 		((GeneralPath)polygon).moveTo(0, BasementMembraneDev.lowerBound(0));
	     	 		for(double i = 0; i <= BasementMembraneDev.getWidth()+10; i += STEPSIZE){
	     	 		((GeneralPath)polygon).lineTo(i, BasementMembraneDev.lowerBound(i));
	     	 		}
	
	      	  */
	      	  graphics.setColor(new Color(255, 99, 0));
	      	  graphics.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	      	  
	      	      
		      	     
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
	 private void drawInterpolationPoint(Graphics2D graphics, DrawInfo2D info, Point2D point) {

		GeneralPath polygon = new GeneralPath();
		polygon.moveTo(lastActualInfo.clip.getMinX()-getDeltaX() + point.getX() - DELTACROSS, 
				lastActualInfo.clip.getMinY()-getDeltaY() + point.getY() - DELTACROSS);
		polygon.lineTo(lastActualInfo.clip.getMinX()-getDeltaX() + point.getX() + DELTACROSS, 
				lastActualInfo.clip.getMinY()-getDeltaY() + point.getY() + DELTACROSS);
		polygon.moveTo(lastActualInfo.clip.getMinX()-getDeltaX() + point.getX() + DELTACROSS, 
				lastActualInfo.clip.getMinY()-getDeltaY() + point.getY() - DELTACROSS);
		polygon.lineTo(lastActualInfo.clip.getMinX()-getDeltaX() + point.getX() - DELTACROSS, 
				lastActualInfo.clip.getMinY()-getDeltaY() + point.getY() + DELTACROSS);

		graphics.setColor(Color.RED);
		graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		

		graphics.draw(polygon);

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
				
				point.setLocation(mouseposition.getX(), mouseposition.getY());
				hitAndButtonPressed=true;
				actDraggedPoint = point;
				return true;
			}
			else if(hitAndButtonPressed){
				actDraggedPoint.setLocation(mouseposition.getX(), mouseposition.getY());
				return true;
			}
			
		}
		}
		return false;
	
	}




	
	public boolean isHitAndButtonPressed() {
	
		return hitAndButtonPressed;
	}




	
	public void setHitAndButtonPressed(boolean hitAndButtonPressed) {
		if(!hitAndButtonPressed) actDraggedPoint = null;
		this.hitAndButtonPressed = hitAndButtonPressed;
	}   
}
