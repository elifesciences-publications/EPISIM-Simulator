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
	    
	   


	    private boolean hitAndButtonPressed = false;
	    
	   
	    
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
			if(crosshairsVisible && actMousePositionXY!= null 
					&& actMousePositionXY.getX() >= getMinX(info)
					&& actMousePositionXY.getX() <= getMaxX(info)
					&& actMousePositionXY.getY() >= getMinY(info)
					&& actMousePositionXY.getY() <= getMaxY(info))drawCrosshairs(graphics);
			showTissueInformationLine( graphics,  info);
		}

	}
	    
	    private void drawRuler(Graphics2D graphics, DrawInfo2D info){
	   	 graphics.setColor(Color.WHITE);
			 graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			 double minX = getMinX(info);
			 double maxX = getMaxX(info);
			 double minY = getMinY(info);
			 double maxY = getMaxY(info);
			
			 Line2D horizontalAxis = new Line2D.Double(minX, maxY, maxX, maxY);
			 
          
			 Line2D verticalAxis  = new Line2D.Double(minX, minY, minX, maxY);
			   
			   graphics.draw(horizontalAxis);
				graphics.draw(verticalAxis);
				
				double spaceBetweenSmallLines = getScaledNumberOfPixelPerMicrometer(info)*rulerResolution;
				
				double smallLine = 3;
				double mediumLine = 8;
				double bigLine = 12;
				
				 graphics.setFont(new Font("Arial", Font.PLAIN, 10));
				//draw lines on horizontal Axis
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
				//draw lines on vertical Axis
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
				//end of horizontal Axis
				graphics.draw(new Line2D.Double(maxX, maxY, maxX, maxY+ bigLine));
				
				String text = ""+Math.round((maxX-minX)/getScaledNumberOfPixelPerMicrometer(info));
				Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
				graphics.drawString(text, (float)(maxX - (stringBounds.getWidth()/2)), (float)(maxY+ bigLine+stringBounds.getHeight()));
				
				//end of vertical Axis
				graphics.draw(new Line2D.Double(minX, minY, minX - bigLine, minY));
				text = ""+Math.round((maxY-minY)/getScaledNumberOfPixelPerMicrometer(info));
				stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
				graphics.drawString(text, (float)(minX- bigLine-stringBounds.getWidth()), (float)(minY + (stringBounds.getHeight()/3)));
	    }
	    
	    private void showTissueInformationLine(Graphics2D graphics, DrawInfo2D info){
	   	 double minX = getMinX(info);
			 double maxX = getMaxX(info);
			 double minY = getMinY(info);
			 double maxY = getMaxY(info);
			 StringBuffer text = new StringBuffer();
			 
			 text.append("[Intervall: "+ rulerResolution+ "µm]");
				graphics.setFont(new Font("Arial", Font.PLAIN, 12));
				
			 text.append("    Tissue ID: " + TissueBorderDev.getInstance().getTissueID());
			 text.append("    Tissue Decription: " + TissueBorderDev.getInstance().getTissueDescription());
				
				if(actMousePositionXY!= null){
					
					if(actMousePositionXY.getX() >= getMinX(info)
							&& actMousePositionXY.getX() <= getMaxX(info)
							&& actMousePositionXY.getY() >= getMinY(info)
							&& actMousePositionXY.getY() <= getMaxY(info)){
							text.append("    Position in µm: "+ 
									Math.round((actMousePositionXY.getX()- minX)/getScaledNumberOfPixelPerMicrometer(info))+
									", "
									+Math.abs(Math.round((actMousePositionXY.getY()- maxY)/getScaledNumberOfPixelPerMicrometer(info))));
					}
					else{
						text.append("    Position in µm: (out of bounds)");
					}
					
				}
				graphics.drawString(text.toString(), (float)(info.clip.getMinX() +10), (float)(info.clip.getMinY() +20));
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
	 private double getMinX(DrawInfo2D info){
		 return lastActualInfo.clip.getMinX() -getDeltaX() + (ruleroffset*getScaleFactorOfTheDisplay(info));
	 }
	 
	 private double getMaxX(DrawInfo2D info){
		 return lastActualInfo.clip.getMinX()+width-getDeltaX()- (ruleroffset*getScaleFactorOfTheDisplay(info));
	 }
	 private double getMinY(DrawInfo2D info){
		 return lastActualInfo.clip.getMinY()-getDeltaY()+ (ruleroffset*getScaleFactorOfTheDisplay(info));
	 }
	 private double getMaxY(DrawInfo2D info){
	  return lastActualInfo.clip.getMinY()+height-getDeltaY()-(ruleroffset*getScaleFactorOfTheDisplay(info));
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


	private double getScaledNumberOfPixelPerMicrometer(DrawInfo2D info){
		return TissueBorderDev.getInstance().getNumberOfPixelsPerMicrometer()*implicitScale*getScaleFactorOfTheDisplay(info);
	}

	
	public boolean isHitAndButtonPressed() {
			return hitAndButtonPressed;
	}



	private double getScaleFactorOfTheDisplay(DrawInfo2D info){
		return Math.rint((info.draw.getWidth()/ firstInfo.draw.getWidth())*100)/100;
	}
		
}
