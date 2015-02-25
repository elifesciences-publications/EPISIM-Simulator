package sim.app.episim.visualization;


import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.Scale;
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

import episiminterfaces.EpisimPortrayal;
import sim.util.Double2D;



public class RulerPortrayal2D extends AbstractSpatialityScalePortrayal2D implements EpisimPortrayal{
	
	 private final String NAME = "Ruler";  
	   
	    
	 private static final float DOT = 2;
	 private static final float SPACE = 4;
	   
	 private boolean hitAndButtonPressed = false;   
	 private Point2D actMousePositionXY;
	    
	 private boolean crosshairsVisible = false;
	    
	 private Rectangle2D.Double oldDraw = null;  
	    
	 private double rulerResolutionFact = 1;  
	    
	public RulerPortrayal2D(){
		super();
	}
	
	public String getPortrayalName() {
	   return NAME;
   }
	
	    
	    // assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		if(getFirstInfo() == null){
			setFirstInfo(info); // is assigned during the first call of this method			
		}
		
		setLastActualInfo(info);
		
	
		
		if(getLastActualInfo() != null && getLastActualInfo().clip !=null){ 
			
			drawRuler(graphics, info);
			
			//Responsible for drawing the Crosshairs
			if(crosshairsVisible && actMousePositionXY!= null 
					&& actMousePositionXY.getX() >= getMinX(info)
					&& actMousePositionXY.getX() <= getMaxX(info)
					&& actMousePositionXY.getY() >= getMinY(info)
					&& actMousePositionXY.getY() <= getMaxY(info))drawCrosshairs(graphics, info);
			showTissueInformationLine(graphics,  info);
			showSimStepInformationLine(graphics, info);
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
				SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
				double spaceBetweenSmallLinesX = props.displayScaleX*getResolutionInMikron();
				double spaceBetweenSmallLinesY = props.displayScaleY*getResolutionInMikron();
				double factX =1, factY = 1;
				if(spaceBetweenSmallLinesX < MIN_PIXEL_RESOLUTION){					
					factX = MIN_PIXEL_RESOLUTION / spaceBetweenSmallLinesX;
					spaceBetweenSmallLinesX*=factX;
					spaceBetweenSmallLinesY*=factX;
				}
				if(spaceBetweenSmallLinesY < MIN_PIXEL_RESOLUTION){					
					factY = MIN_PIXEL_RESOLUTION / spaceBetweenSmallLinesY;
					spaceBetweenSmallLinesX*=factY;
					spaceBetweenSmallLinesY*=factY;
				}
				rulerResolutionFact =(factX*factY);
				
				if(((getResolutionInMikron()*rulerResolutionFact) % 5) != 0){
					double modul = (getResolutionInMikron()*rulerResolutionFact)%5;
					modul = 5 - modul;
					modul /= getResolutionInMikron();
					rulerResolutionFact+=modul;
				}
				
				spaceBetweenSmallLinesX = props.displayScaleX*getResolutionInMikron()*rulerResolutionFact;
				spaceBetweenSmallLinesY = props.displayScaleY*getResolutionInMikron()*rulerResolutionFact;
				
				
				
				double smallLine = 3;
				double mediumLine = 8;
				double bigLine = 12;
				
				 graphics.setFont(new Font("Arial", Font.PLAIN, 10));
				//draw lines on horizontal Axis
				for(double i = minX, lineNumber = 0; i <= maxX; i += spaceBetweenSmallLinesX, lineNumber++){
					if((lineNumber%10) == 0)graphics.draw(new Line2D.Double(i, maxY, i, maxY+ bigLine));
					else if((lineNumber%5) == 0)graphics.draw(new Line2D.Double(i, maxY, i, maxY+ mediumLine));
					else graphics.draw(new Line2D.Double(i, maxY, i, maxY+ smallLine));
					
					if((lineNumber%10) == 0 || (lineNumber%5) == 0){
						String text = ""+((int)(lineNumber*(getResolutionInMikron()*rulerResolutionFact)));
						Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
						graphics.drawString(text, (float)(i - (stringBounds.getWidth()/2)), (float)(maxY+ bigLine+stringBounds.getHeight()));
					}
				}
				//draw lines on vertical Axis
				for(double i = maxY, lineNumber = 0; i >= minY; i -= spaceBetweenSmallLinesY, lineNumber++){
					if((lineNumber%10) == 0)graphics.draw(new Line2D.Double(minX, i, minX - bigLine, i));
					else if((lineNumber%5) == 0)graphics.draw(new Line2D.Double(minX, i, minX - mediumLine, i));
					else graphics.draw(new Line2D.Double(minX, i, minX - smallLine, i));
					
					if((lineNumber%10) == 0 || (lineNumber%5) == 0){
						String text = ""+((int)(lineNumber*(getResolutionInMikron()*rulerResolutionFact)));
						Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
						graphics.drawString(text, (float)(minX- bigLine-stringBounds.getWidth()), (float)(i + (stringBounds.getHeight()/3)));
					}
				}
				//end of horizontal Axis
				graphics.draw(new Line2D.Double(maxX, maxY, maxX, maxY+ bigLine));
				
		/*		String text = ""+Math.round((maxX-minX)/props.displayScaleX);
				Rectangle2D stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
				graphics.drawString(text, (float)(maxX - (stringBounds.getWidth()/2)), (float)(maxY+ bigLine+stringBounds.getHeight()));*/
				
				//end of vertical Axis
				graphics.draw(new Line2D.Double(minX, minY, minX - bigLine, minY));
		/*		text = ""+Math.round((maxY-minY)/props.displayScaleY);
				stringBounds =graphics.getFontMetrics().getStringBounds(text, graphics);
				graphics.drawString(text, (float)(minX- bigLine-stringBounds.getWidth()), (float)(minY + (stringBounds.getHeight()/3)));*/
	    }
	    
	    private void showTissueInformationLine(Graphics2D graphics, DrawInfo2D info){
	   	 double minX = getMinX(info);
			 double maxX = getMaxX(info);
			 double minY = getMinY(info);
			 double maxY = getMaxY(info);
			 StringBuffer text = new StringBuffer();
			 double resolution = (getResolutionInMikron()*rulerResolutionFact)*10;
			 resolution = Math.round(resolution);
			 resolution/=10;
			 text.append("Interval: "+ (resolution)+ " µm");
				graphics.setFont(new Font("Arial", Font.PLAIN, 12));
				
		/*	 text.append("    Tissue ID: " + TissueController.getInstance().getTissueBorder().getTissueID());
			 text.append("    Tissue Description: " + TissueController.getInstance().getTissueBorder().getTissueDescription());*/
				
				if(actMousePositionXY!= null){
					
					if(actMousePositionXY.getX() >= getMinX(info)
							&& actMousePositionXY.getX() <= getMaxX(info)
							&& actMousePositionXY.getY() >= getMinY(info)
							&& actMousePositionXY.getY() <= getMaxY(info)){
						SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));		
							text.append("    Position in µm: "+ 
									Math.round((actMousePositionXY.getX()- minX)/props.displayScaleX)+
									", "
									+Math.abs(Math.round((actMousePositionXY.getY()- maxY)/props.displayScaleY)));
					}
					else{
						text.append("    Position in µm: (out of bounds)");
					}
					
				}
				graphics.drawString(text.toString(), (float)(info.clip.getMinX() +10), (float)(info.clip.getMinY() +20));
	    }
	    private void showSimStepInformationLine(Graphics2D graphics, DrawInfo2D info){
	   	 StringBuffer text = new StringBuffer();
		    if(ModeServer.useMonteCarloSteps()){
				 text.append("Total MC Sim Step No: " + SimStateServer.getInstance().getSimStepNumber());
			 }
			 else text.append("Total Sim Step No: " + SimStateServer.getInstance().getSimStepNumber());
		    Rectangle2D stringBounds = graphics.getFontMetrics().getStringBounds(text.toString(), graphics);
		    graphics.drawString(text.toString(), (float)(info.clip.getMaxX() -(stringBounds.getWidth()+10)), (float)(info.clip.getMinY() +20));
	    }
	    private void drawCrosshairs(Graphics2D graphics, DrawInfo2D info){
	   	   float[] dash = new float[]{ DOT, SPACE };
	   		
				graphics.setColor(Color.WHITE);
				graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,0,dash,0));
							
				
				
				Line2D horizontalLine = new Line2D.Double(getMinX(info), actMousePositionXY.getY(), getMaxX(info),actMousePositionXY.getY());
				Line2D verticalLine = new Line2D.Double(actMousePositionXY.getX(), getMinY(info), 
						                               actMousePositionXY.getX(), getMaxY(info));  
				graphics.draw(horizontalLine);
				graphics.draw(verticalLine);
	    }
	    
	   
	
	public void setCrosshairsVisible(boolean visible){
		this.crosshairsVisible = visible;
	}
	
	public void setActMousePosition(Point2D mousePosition){		
		if(mousePosition != null && getLastActualInfo() != null
		   && mousePosition.getX() >= getLastActualInfo().clip.getMinX()
		   && mousePosition.getX() <= getLastActualInfo().clip.getMaxX()
		   && mousePosition.getY() >= getLastActualInfo().clip.getMinY()
		   && mousePosition.getY() <= getLastActualInfo().clip.getMaxY()) 
				actMousePositionXY = mousePosition;
		else actMousePositionXY = null;
	}
	
		
	
	public boolean isHitAndButtonPressed(){
			return hitAndButtonPressed;
	}

	public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(0,0,guiState.EPIDISPLAYWIDTH+(guiState.DISPLAY_BORDER_LEFT+guiState.DISPLAY_BORDER_RIGHT), guiState.EPIDISPLAYHEIGHT+(guiState.DISPLAY_BORDER_TOP+guiState.DISPLAY_BORDER_BOTTOM));
 	   else return new Rectangle2D.Double(0,0,0, 0);
    }
		
}
