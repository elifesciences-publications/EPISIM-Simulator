package sim.app.episim.visualization;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
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
	
  
   
  
   
   private final double XSHIFTCORRECTION = 3; //Corrects error of affine Transformation
   
   private EpisimGUIState guiState;
   
   public BasementMembranePortrayal2D() {
   	 guiState = SimStateServer.getInstance().getEpisimGUIState();  	 
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
	
		GeneralPath polygon = TissueController.getInstance().getTissueBorder().getFullContourDrawPolygon();
		if(polygon != null){
			{
				if(info != null && polygon.getBounds().getWidth() > 0){
					 Stroke oldStroke = graphics.getStroke();
				
					graphics.setColor(new Color(255, 99, 0));
					graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			
					AffineTransform transform = new AffineTransform();
					
					
					EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();
					double displayScale = guiState.getDisplay().getDisplayScale();
					double scaleX = (guiState.EPIDISPLAYWIDTH / TissueController.getInstance().getTissueBorder().getWidthInMikron())*displayScale;
					double scaleY = (guiState.EPIDISPLAYHEIGHT / TissueController.getInstance().getTissueBorder().getHeightInMikron())*displayScale;
					
					double x = 0;
					double y = 0;
				
					x+=guiState.DISPLAY_BORDER_LEFT*displayScale;
					y+=guiState.DISPLAY_BORDER_TOP*displayScale;
					
					double differenceX = (info.clip.width-((guiState.EPIDISPLAYWIDTH)*displayScale));
					double differenceY = (info.clip.height-((guiState.EPIDISPLAYHEIGHT)*displayScale));
					
					double startX =0; 
					double startY =0; 
					if(info != null){
						startX =differenceX >= 0 ? info.clip.x:0;
						startY =differenceY >= 0 ? info.clip.y:0;
					}
					
					x+=startX;
					y+=startY;				
					
					if(TissueController.getInstance().getTissueBorder().isStandardMembraneLoaded()){
						 graphics.setStroke(new BasicStroke((int)(0.8*scaleX), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						// scaleX *= 1.06;
					}					
					
					transform.scale(scaleX, scaleY);
					Shape tissueBorder = transform.createTransformedShape(polygon);
					transform.setToTranslation(x, y);
					tissueBorder = transform.createTransformedShape(tissueBorder);
					//polygon = (GeneralPath) polygon.createTransformedShape(transform);
								
					//transform.setToTranslation(x, y);
					
					//polygon = (GeneralPath) polygon.createTransformedShape(transform);
					
					
					
					graphics.draw(tissueBorder);
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


