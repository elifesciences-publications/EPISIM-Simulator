package sim.app.episim.visualization;



import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.tissue.TissueBorder;

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



public class GridPortrayal2D extends SimplePortrayal2D{
	
	   
	    private double width;
	    private double height;
	    private final double INITIALWIDTH;
	    private final double INITIALHEIGHT;
	   
	    private DrawInfo2D lastActualInfo;
	    private DrawInfo2D firstInfo;
	    
	  


	 
	    
	    private static final int EMPTYBORDER = 10;
	    
	    
	    
	  
	    
	    
	   
	    private int border;
	    private int gridoffset;
	    private final int OFFSET = 0; //distance ruler <->tissue
	    
	    private double implicitScale;
	    
	    private double gridResolution = 5;
	    private double gridSize = 5;
	    
	    public GridPortrayal2D(double width, double height, int border, double implicitScale) {
	   	 this.width = width;
	   	 this.height = height;
	   	 this.INITIALWIDTH = width;
	   	 this.INITIALHEIGHT = height;
	   	
	   	 this.border = border;
	   	 this.implicitScale = implicitScale;
	   	 this.gridoffset = border - OFFSET;
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
			
			drawGrid(graphics, info);			
		}

	}
	    
	    private void drawGrid(Graphics2D graphics, DrawInfo2D info){
	   	 
	   	 graphics.setColor(new Color(192, 192, 192, 150));
			 graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			 
			 double minX = getMinX(info);
			 double maxX = getMaxX(info);
			 double minY = getMinY(info);
			 double maxY = getMaxY(info);
			
				
				double spaceBetweenSmallLines = getScaledNumberOfPixelPerMicrometer(info)*gridResolution;
				
				
				
				 graphics.setFont(new Font("Arial", Font.PLAIN, 10));
				//draw vertical lines
				for(double i = (minX+(spaceBetweenSmallLines*gridSize)); i <= maxX; i += spaceBetweenSmallLines*gridSize)
					graphics.draw(new Line2D.Double(Math.round(i), minY, Math.round(i), maxY));
	
				//draw horizontal lines
				for(double i = (maxY-(spaceBetweenSmallLines*gridSize)); i >= minY; i -= spaceBetweenSmallLines*gridSize)
					graphics.draw(new Line2D.Double(minX, Math.round(i), maxX , Math.round(i)));
		
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
		 return lastActualInfo.clip.getMinX() -getDeltaX() + (gridoffset*getScaleFactorOfTheDisplay(info));
	 }
	 
	 private double getMaxX(DrawInfo2D info){
		 return lastActualInfo.clip.getMinX()+width-getDeltaX()- (gridoffset*getScaleFactorOfTheDisplay(info));
	 }
	 private double getMinY(DrawInfo2D info){
		 return lastActualInfo.clip.getMinY()-getDeltaY()+ (gridoffset*getScaleFactorOfTheDisplay(info));
	 }
	 private double getMaxY(DrawInfo2D info){
	  return lastActualInfo.clip.getMinY()+height-getDeltaY()-(gridoffset*getScaleFactorOfTheDisplay(info));
	}
	
	


	private double getScaledNumberOfPixelPerMicrometer(DrawInfo2D info){
		return TissueBorder.getInstance().getNumberOfPixelsPerMicrometer()*implicitScale*getScaleFactorOfTheDisplay(info);
	}

	private double getScaleFactorOfTheDisplay(DrawInfo2D info){
		return Math.rint((info.draw.getWidth()/ firstInfo.draw.getWidth())*100)/100;
	}
	
	 
}