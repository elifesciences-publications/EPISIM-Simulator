package sim.app.episim.visualization;

import sendreceive.TestFrame;
import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.Scale;
import sim.field.continuous.Continuous2D;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Double2D;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import episiminterfaces.EpisimPortrayal;

public class BasementMembranePortrayal2D extends ContinuousPortrayal2D implements EpisimPortrayal{
	
   
	private final String NAME = "Basement Membrane";  
  
  
   public BasementMembranePortrayal2D() {
   	
	  	 Continuous2D field = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidthInMikron() + 2, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron() + 2, 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
	  	 
	  	 field.setObjectLocation("DummyObject", new Double2D(50, 50));
	  	 this.setField(field);
	  	 
   }
   
       
   Rectangle2D.Double oldDraw = null; 
   
   public String getPortrayalName() {
	   return NAME;
   }
   
   // assumes the graphics already has its color set
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	
		GeneralPath polygon = TissueController.getInstance().getTissueBorder().getBasalLayerDrawPolygon();
		if(polygon != null){
			{
				if(info != null && polygon.getBounds().getWidth() > 0){
					
					 Stroke oldStroke = graphics.getStroke();
				
					graphics.setColor(new Color(255, 99, 0));
					graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			
					AffineTransform transform = new AffineTransform();
					
					
					EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();
					SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));								
					
					if(TissueController.getInstance().getTissueBorder().isStandardMembraneLoaded()){
						 graphics.setStroke(new BasicStroke((int)(0.8*props.displayScaleX), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						// scaleX *= 1.06;
					}
				
					
				transform.scale(props.displayScaleX, props.displayScaleY);
				polygon.transform(transform);
				transform = new AffineTransform();
				transform.setToTranslation(props.offsetX, props.offsetY);
				polygon.transform(transform);
				
										
					graphics.draw(polygon);
				
					graphics.setStroke(oldStroke);					
				}
			}
		}
	} 

   public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
 	   else return new Rectangle2D.Double(0,0,0, 0);
    }
	
	
}


