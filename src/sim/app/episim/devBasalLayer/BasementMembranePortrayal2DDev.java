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
	    private final double INITIALWIDTH;
	    private final double INITIALHEIGHT; 
	    private DrawInfo2D lastActualInfo;
	    private DrawInfo2D firstInfo;
	    
	    private List<Point2D> cellPoints;


	    private boolean hitAndButtonPressed = false;
	    
	    private Point2D actDraggedPoint = null;
	
	    private static final double DELTACROSS = 10;
	    private static final double DELTAPOINT = 10;
	    
	    private int border = 0;
	    
	    private final double XSHIFTCORRECTION = 1; //Corrects error of affine Transformation
	    
	    
	    
	    public BasementMembranePortrayal2DDev(double width, double height, int border) {
	   	 this.width = width;
	   	 this.height = height;
	   	 this.INITIALWIDTH = width;
	   	 this.INITIALHEIGHT = height;
	   	 this.border = border;
	   	 cellPoints = new ArrayList<Point2D>();
	   	 
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {

		GeneralPath polygon = TissueBorderDev.getInstance().getBasementMembraneDrawPolygon();
		if(info != null && polygon.getBounds().getWidth() > 0){
			if(firstInfo == null)
				firstInfo = info; // wird beim ersten Aufruf gesetzt.
			lastActualInfo = info;

			graphics.setColor(new Color(255, 99, 0));
			graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

			AffineTransform transform = new AffineTransform();
			
			double dispScale = getScaleFactorOfTheDisplay(info);
			
			width = (INITIALWIDTH -2*border)*dispScale ;
			height = (INITIALHEIGHT-2*border)*dispScale;
			double scaleX = (width / polygon.getBounds2D().getWidth());
			
			
			
			
			transform.scale(scaleX, scaleX);
			polygon = (GeneralPath) polygon.createTransformedShape(transform);

			transform.setToTranslation(lastActualInfo.clip.getMinX()-getDeltaX()+ (border*dispScale)-XSHIFTCORRECTION, 
					                     lastActualInfo.clip.getMinY()-getDeltaY()+(border*dispScale));

			polygon = (GeneralPath) polygon.createTransformedShape(transform);
			graphics.draw(polygon);

			drawCellPoints(graphics, scaleX);
			
			
			

		}

	}
	    
	    

	    
	    private double getDeltaX(){
	   	 if(lastActualInfo.clip.width< (INITIALWIDTH *getScaleFactorOfTheDisplay(lastActualInfo))){
	   		 return lastActualInfo.clip.getMinX();

	   		 
	   	 }
	   	 else return 0;
	    }
	    
	    private double getDeltaY(){
	   	 
	   	 if(lastActualInfo.clip.height < (INITIALHEIGHT *getScaleFactorOfTheDisplay(lastActualInfo))){
	   		 return lastActualInfo.clip.getMinY();
	   	 }
	   	 else return 0;
	    }
	 private void drawCellPoints(Graphics2D graphics, double scaleX) {

		if(graphics != null){
			for(Point2D point : cellPoints){
				GeneralPath polygon = new GeneralPath();
				polygon.moveTo(lastActualInfo.clip.getMinX() - getDeltaX() + (point.getX()*getScaleFactorOfTheDisplay(lastActualInfo)) - DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + (point.getY()*getScaleFactorOfTheDisplay(lastActualInfo)) - DELTACROSS);
				polygon.lineTo(lastActualInfo.clip.getMinX() - getDeltaX() +(point.getX()*getScaleFactorOfTheDisplay(lastActualInfo)) + DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + (point.getY()*getScaleFactorOfTheDisplay(lastActualInfo)) + DELTACROSS);
				polygon.moveTo(lastActualInfo.clip.getMinX() - getDeltaX() + (point.getX()*getScaleFactorOfTheDisplay(lastActualInfo)) + DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + (point.getY()*getScaleFactorOfTheDisplay(lastActualInfo)) - DELTACROSS);
				polygon.lineTo(lastActualInfo.clip.getMinX() - getDeltaX() + (point.getX()*getScaleFactorOfTheDisplay(lastActualInfo)) - DELTACROSS, lastActualInfo.clip
						.getMinY()
						- getDeltaY() + (point.getY()*getScaleFactorOfTheDisplay(lastActualInfo)) + DELTACROSS);

				if(TissueBorderDev.getInstance().isOverBasalLayer(new Point2D.Double(
						(((point.getX()-border) /scaleX)),
						(((point.getY()-border)/scaleX)))))
						graphics.setColor(Color.GREEN);
				else graphics.setColor(Color.RED);
				graphics.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

				graphics.draw(polygon);
			}
		}
	}
	 
	public boolean getCellPoint(Point2D mouseposition){
		if(mouseposition != null && lastActualInfo != null){
	   	
		mouseposition = new Point2D.Double((mouseposition.getX()-lastActualInfo.clip.getMinX()+getDeltaX()),
		                             (mouseposition.getY()-lastActualInfo.clip.getMinY()+getDeltaY()));
		
		for(Point2D point: cellPoints){
			
		
			if(mouseposition.getX() > ((point.getX()*getScaleFactorOfTheDisplay(lastActualInfo)) - DELTAPOINT) 
				&& mouseposition.getX() < ((point.getX()*getScaleFactorOfTheDisplay(lastActualInfo)) + DELTAPOINT)
				&& mouseposition.getY() > ((point.getY()*getScaleFactorOfTheDisplay(lastActualInfo)) - DELTAPOINT)
				&& mouseposition.getY() < ((point.getY()*getScaleFactorOfTheDisplay(lastActualInfo)) + DELTAPOINT)
				&& !hitAndButtonPressed){
				
				point.setLocation(mouseposition.getX()/getScaleFactorOfTheDisplay(lastActualInfo), mouseposition.getY()/getScaleFactorOfTheDisplay(lastActualInfo));
				hitAndButtonPressed=true;
				actDraggedPoint = point;
				return true;
			}
			else if(hitAndButtonPressed){
				actDraggedPoint.setLocation(mouseposition.getX()/getScaleFactorOfTheDisplay(lastActualInfo), mouseposition.getY()/getScaleFactorOfTheDisplay(lastActualInfo));
				return true;
			}
			
		}
		}
		return false;
	
	}
	public void addCellPoint(Point2D mouseposition){
	   
		if(mouseposition != null && lastActualInfo != null){
	   	
		mouseposition = new Point2D.Double((mouseposition.getX()-lastActualInfo.clip.getMinX()+getDeltaX())/getScaleFactorOfTheDisplay(lastActualInfo),
		                             (mouseposition.getY()-lastActualInfo.clip.getMinY()+getDeltaY())/getScaleFactorOfTheDisplay(lastActualInfo));
		

		
			cellPoints.add(mouseposition);
		
		
		
		}
		
	}




	
	public boolean isHitAndButtonPressed() {
	
		return hitAndButtonPressed;
	}



	private double getScaleFactorOfTheDisplay(DrawInfo2D info){
		return Math.rint((info.draw.getWidth()/ firstInfo.draw.getWidth())*100)/100;
	}
	
	public void setHitAndButtonPressed(boolean hitAndButtonPressed) {
		if(!hitAndButtonPressed) actDraggedPoint = null;
		this.hitAndButtonPressed = hitAndButtonPressed;
	}   
}
