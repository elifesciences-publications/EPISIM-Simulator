package sim.app.episim.devBasalLayer;


import sim.app.episim.ExceptionDisplayer;
import sim.portrayal.*;


import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import sim.util.Double2D;



public class RulerPortrayal2D extends SimplePortrayal2D{
	
	   
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
	    
	    private static final int EMPTYBORDER = 10;
	    
	    private Point2D actMousePositionXY;
	    
	    private boolean crosshairsVisible = false;
	    
	    
	    private static final float DOT = 2;
	    private static final float SPACE = 4;
	    private int border;
	    private int ruleroffset;
	    private final int OFFSET = 0; //distance ruler <->tissue
	    
	    private double implicitScale;
	    
	    private double rulerResolution = 5;
	    
	    public RulerPortrayal2D(double width, double height, int border, double implicitScale) {
	   	 this.width = width;
	   	 this.height = height;
	   	 this.INITIALWIDTH = width;
	   	 this.INITIALHEIGHT = height;
	   	 this.cellPoints = new ArrayList<Point2D>();
	   	 this.border = border;
	   	 this.implicitScale = implicitScale;
	   	 this.ruleroffset = border - OFFSET;
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		if(firstInfo == null){
			firstInfo = info; // is assigned during the first call of this method
			
		}
		lastActualInfo = info;
		width = INITIALWIDTH *getScaleFactorOfTheDisplay(info);
		height = INITIALHEIGHT *getScaleFactorOfTheDisplay(info);
		
		if(lastActualInfo != null &&lastActualInfo.clip !=null){ 
			
			drawRuler(graphics, info);
			
			//Responsible for drawing the Crosshairs
			if(crosshairsVisible && actMousePositionXY!= null)drawCrosshairs(graphics);
		}

	}
	    
	    private void drawRuler(Graphics2D graphics, DrawInfo2D info){
	   	 graphics.setColor(Color.WHITE);
			 graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			 double scale = getScaleFactorOfTheDisplay(info);
			 double minX = lastActualInfo.clip.getMinX() -getDeltaX() + (ruleroffset*scale);
			 double maxX = lastActualInfo.clip.getMinX()+width-getDeltaX()- (ruleroffset*scale);
			 double minY = lastActualInfo.clip.getMinY()-getDeltaY()+ (ruleroffset*scale);
			 double maxY = lastActualInfo.clip.getMinY()+height-getDeltaY()-(ruleroffset*scale);
			
			 Line2D horizontalAxis = new Line2D.Double(minX, maxY, maxX, maxY);
			 
          
			 Line2D verticalAxis  = new Line2D.Double(minX, minY, minX, maxY);
			   
			   graphics.draw(horizontalAxis);
				graphics.draw(verticalAxis);
				
				double spaceBetweenSmallLines = TissueBorderDev.getInstance().getNumberOfPixelsPerMicrometer()*implicitScale*rulerResolution*scale;
				
				double smallLine = 3;
				double mediumLine = 8;
				double bigLine = 12;
				
				 graphics.setFont(new Font("Arial", Font.PLAIN, 10));
				//draw horizontal lines
				for(double i = minX, lineNumber = 0; i <= maxX; i += spaceBetweenSmallLines, lineNumber++){
					if((lineNumber%10) == 0)graphics.draw(new Line2D.Double(i, maxY, i, maxY+ bigLine));
					else if((lineNumber%5) == 0)graphics.draw(new Line2D.Double(i, maxY, i, maxY+ mediumLine));
					else graphics.draw(new Line2D.Double(i, maxY, i, maxY+ smallLine));
					
					if((lineNumber%10) == 0 || (lineNumber%5) == 0){
						String text = ""+((int)(lineNumber*rulerResolution));
						Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
						graphics.drawString(text, (float)(i - (stringBounds.getWidth()/2)), (float)(maxY+ bigLine+stringBounds.getHeight()));
					}
				}
				//draw vertical lines
				for(double i = maxY, lineNumber = 0; i >= minY; i -= spaceBetweenSmallLines, lineNumber++){
					if((lineNumber%10) == 0)graphics.draw(new Line2D.Double(minX, i, minX - bigLine, i));
					else if((lineNumber%5) == 0)graphics.draw(new Line2D.Double(minX, i, minX - mediumLine, i));
					else graphics.draw(new Line2D.Double(minX, i, minX - smallLine, i));
					
					if((lineNumber%10) == 0 || (lineNumber%5) == 0){
						String text = ""+((int)(lineNumber*rulerResolution));
						Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
						graphics.drawString(text, (float)(minX- bigLine-stringBounds.getWidth()), (float)(i + (stringBounds.getHeight()/3)));
					}
				}
				String text = "[Intervall: "+ rulerResolution+ "µm]";
				graphics.setFont(new Font("Arial", Font.PLAIN, 14));
				graphics.drawString(text, (float)(info.clip.getMinX() +10), (float)(info.clip.getMinY() +20));
	    }
	    
	    private void drawCrosshairs(Graphics2D graphics){
	   	   float[] dash = new float[]{ DOT, SPACE };
	   		
				graphics.setColor(Color.WHITE);
				graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,0,dash,0));
							
				
				
				Line2D horizontalLine = new Line2D.Double(lastActualInfo.clip.getMinX(), actMousePositionXY.getY(), 
						                                    lastActualInfo.clip.getMinX()+width,actMousePositionXY.getY());
				Line2D verticalLine = new Line2D.Double(actMousePositionXY.getX(), lastActualInfo.clip.getMinY(), 
						                               actMousePositionXY.getX(), lastActualInfo.clip.getMinY()+height);  
				graphics.draw(horizontalLine);
				graphics.draw(verticalLine);
	    }

	    
	    private double getDeltaX(){
	   	 if(lastActualInfo.clip.width< width){
	   		 return lastActualInfo.clip.getMinX();

	   		 
	   	 }
	   	 else return 0;
	    }
	    
	    private double getDeltaY(){
	   	 
	   	 if(lastActualInfo.clip.height < height){
	   		 return lastActualInfo.clip.getMinY();
	   	 }
	   	 else return 0;
	    }
	 
	
	public void setCrosshairsVisible(boolean visible){
		this.crosshairsVisible = visible;
	}
	
	public void setActMousePosition(Point2D mousePosition){
		
		if(mousePosition != null && lastActualInfo != null
		   && mousePosition.getX() >= lastActualInfo.clip.getMinX()
		   && mousePosition.getX() <= lastActualInfo.clip.getMaxX()
		   && mousePosition.getY() >= lastActualInfo.clip.getMinY()
		   && mousePosition.getY() <= lastActualInfo.clip.getMaxY()) 
				actMousePositionXY = mousePosition;
		else actMousePositionXY = null;
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
