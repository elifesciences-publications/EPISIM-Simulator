package sim.app.episim.visualization;




import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.portrayal.*;


import java.awt.*;

import java.awt.geom.*;

import episiminterfaces.EpisimPortrayal;



public class GridPortrayal2D extends AbstractSpatialityScalePortrayal2D implements EpisimPortrayal{
	
		 private final String NAME = "Grid";	
	    
	    private double gridResolution = 5.0;
	    public GridPortrayal2D() {
	   	 super();
	   	 	   	 
	    }
	    
	        
	    Rectangle2D.Double oldDraw = null;  
	    
	    // assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		if(getFirstInfo() == null){
			setFirstInfo(info); // is assigned during the first call of this method
			
		}
		setLastActualInfo(info);
		setWidth(INITIALWIDTH *getScaleFactorOfTheDisplay());
		setHeight(INITIALHEIGHT *getScaleFactorOfTheDisplay());
		
		if(getLastActualInfo() != null && getLastActualInfo().clip != null){ 			
			drawGrid(graphics, info);			
		}
	}
	
	public String getPortrayalName() {
	   return NAME;
   }
	    
	private void drawGrid(Graphics2D graphics, DrawInfo2D info){
	   	 
		graphics.setColor(new Color(192, 192, 192, 150));
		graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			 
		double minX = getMinX(info);
		double maxX = getMaxX(info);
		double minY = getMinY(info);
		double maxY = getMaxY(info);			
				
		double spaceBetweenSmallLines = getScaledNumberOfPixelPerMicrometer(info)*getResolutionInMikron();				
				
				
		graphics.setFont(new Font("Arial", Font.PLAIN, 10));
		
		//draw vertical lines
		for(double i = (minX+(spaceBetweenSmallLines*gridResolution)); i <= maxX; i += (spaceBetweenSmallLines*gridResolution))
			graphics.draw(new Line2D.Double(Math.round(i), minY, Math.round(i), maxY));
	
		//draw horizontal lines
		for(double i = (maxY-(spaceBetweenSmallLines*gridResolution)); i >= minY; i -= (spaceBetweenSmallLines*gridResolution))
			graphics.draw(new Line2D.Double(minX, Math.round(i), maxX , Math.round(i)));		
	}
	
	public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(0,0,guiState.EPIDISPLAYWIDTH+(2*guiState.DISPLAYBORDER), guiState.EPIDISPLAYHEIGHT+(2*guiState.DISPLAYBORDER));
 	   else return new Rectangle2D.Double(0,0,0, 0);
    }
}