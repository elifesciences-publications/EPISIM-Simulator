package sim.app.episim.visualization.twodim;

import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.tissue.StandardMembrane;
import sim.app.episim.model.tissue.TissueBorder;
import sim.app.episim.util.Scale;
import sim.app.episim.visualization.EpisimDrawInfo;
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
						double strokeFact = 4;
						StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
						if(membrane!= null && membrane.isDiscretizedMembrane())graphics.setStroke(new BasicStroke((int)(strokeFact*props.displayScaleX), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						else graphics.setStroke(new BasicStroke((int)(0.8*props.displayScaleX), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
						// scaleX *= 1.06;
					}
				
					
					transform.scale(props.displayScaleX, props.displayScaleY);
					polygon.transform(transform);
					transform = new AffineTransform();
					transform.setToTranslation(props.offsetX, props.offsetY);
					polygon.transform(transform);
					StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
					if(membrane != null && membrane.isDiscretizedMembrane()){
						ArrayList<Double> contactTimeList = membrane.getContactTimeToMembraneSegmentList2D();
						PathIterator iterator = polygon.getPathIterator(new AffineTransform());
						double[] coordinatesNew = new double[6];
						double[] coordinatesOld = new double[6];
						iterator.currentSegment(coordinatesOld);
						int type = 0;
						int segmentCounter = 0;
						double threshold = membrane.getCellContactTimeThreshold();
						do{							
							iterator.next();
							type=iterator.currentSegment(coordinatesNew);
							if(type==PathIterator.SEG_LINETO){
								double contactTime = Double.POSITIVE_INFINITY;
								if(segmentCounter < contactTimeList.size()){
									contactTime = contactTimeList.get(segmentCounter);
								}
								Color c = getColor(contactTime, threshold);
								graphics.setColor(c);
								graphics.drawLine((int)coordinatesOld[0], (int)coordinatesOld[1], (int)coordinatesNew[0], (int)coordinatesNew[1]);
								coordinatesOld = coordinatesNew;
								coordinatesNew = new double[6];
								segmentCounter++;
							}
						}while(!iterator.isDone());
					}
					else graphics.draw(polygon);
					
				
					graphics.setStroke(oldStroke);					
				}
			}
		}
	} 
	private Color getColor(double contactTime, double threshold){
		if(threshold > 0){
		contactTime = contactTime < 0 ? 0 : contactTime > threshold ? threshold: contactTime;
				
		return new Color(255, (int) (255- 156*(contactTime/threshold)), (int) (255- 255*(contactTime/threshold)));
		}
		return  new Color(255, 99, 0);
	}
   public Rectangle2D.Double getViewPortRectangle() {
 		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.getEpiDisplayWidth(), guiState.getEpiDisplayHeight());
 	   else return new Rectangle2D.Double(0,0,0, 0);
    }
	
	
}


