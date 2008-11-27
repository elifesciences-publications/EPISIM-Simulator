package sim.app.episim.visualization;

import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.portrayal.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class BasementMembranePortrayal2D extends SimplePortrayal2D{
	
   
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
   
   private final double XSHIFTCORRECTION = 3; //Corrects error of affine Transformation
   
   
   
   public BasementMembranePortrayal2D(double width, double height, int border) {
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
	
		GeneralPath polygon = TissueController.getInstance().getTissueBorder().getBasementMembraneDrawPolygon();
		if(polygon != null){
			{
				if(info != null && polygon.getBounds().getWidth() > 0){
					 Stroke oldStroke = graphics.getStroke();
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
					
					if(TissueController.getInstance().getTissueBorder().isStandardMembraneLoaded()){
						 graphics.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						// scaleX *= 1.06;
					}
					
					
					transform.scale(scaleX, scaleX);
					polygon = (GeneralPath) polygon.createTransformedShape(transform);
				
						transform.setToTranslation(lastActualInfo.clip.getMinX()-getDeltaX()+ (border*dispScale)-XSHIFTCORRECTION, 
	                     lastActualInfo.clip.getMinY()-getDeltaY()+(border*dispScale));
					
					polygon = (GeneralPath) polygon.createTransformedShape(transform);
					
					
					
					graphics.draw(polygon);
					graphics.setStroke(oldStroke);
					drawCellPoints(graphics, scaleX);
					
				}
			}
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
		
					if(TissueController.getInstance().getTissueBorder().isOverBasalLayer(new Point2D.Double(
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
	
	
	 private double getTranslationX(DrawInfo2D info){
		   
   	 if(info.clip.width< width){
   		 
   		 return 0;
   	 }
   	 else return info.clip.getMinX();
   	 
    }
    
    private double getTranslationY(DrawInfo2D info){
   	 final int BIAS = 20;
   	 if(info.clip.height < height){
   		 return BIAS;
   	 }
   	 else return info.clip.getMinY()+BIAS;
    }
	
	
}


