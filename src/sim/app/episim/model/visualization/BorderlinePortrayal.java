package sim.app.episim.model.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.visualization.AbstractSpatialityScalePortrayal2D;
import sim.portrayal.DrawInfo2D;
import episiminterfaces.EpisimPortrayal;


public class BorderlinePortrayal  extends AbstractSpatialityScalePortrayal2D implements EpisimPortrayal{
	
	 private final String NAME = "Borderline";	
   
   private double gridResolution = 5.0;
   public BorderlinePortrayal() {
  	 super();
  	 	   	 
   }
   
       
   Rectangle2D.Double oldDraw = null;  
   
   // assumes the graphics already has its color set
   public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
   	graphics.setStroke(new BasicStroke((int)(2*this.getScaleFactorOfTheDisplay()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
   	graphics.setPaint(Color.BLUE);
   	
   	graphics.drawLine((int)(info.draw.x+(this.getScaledNumberOfPixelPerMicrometer(info)*500)),
   			(int)(info.draw.y),
   			(int)(info.draw.x+(this.getScaledNumberOfPixelPerMicrometer(info)*500)),
   			(int)(info.draw.y + info.draw.getHeight()));
   }

	public String getPortrayalName() {

		
		return NAME;
	}

	public Double getViewPortRectangle() {

		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAYBORDER, guiState.DISPLAYBORDER, guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
 	   else return new Rectangle2D.Double(0,0,0, 0);
	}

}
