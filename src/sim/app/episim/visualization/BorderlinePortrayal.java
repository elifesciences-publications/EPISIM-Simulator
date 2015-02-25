package sim.app.episim.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.portrayal.DrawInfo2D;
import episiminterfaces.EpisimPortrayal;


public class BorderlinePortrayal  extends AbstractSpatialityScalePortrayal2D implements EpisimPortrayal{
	
	private final String NAME;	
	private BorderlineConfig borderlineConfig;
	private Color color;
	
	public class BorderlineConfig{
		public double x1_InMikron;
		public double y1_InMikron;
		public double x2_InMikron;
		public double y2_InMikron;
	}
	 
	 
   
   public BorderlinePortrayal(String name, Color color, double x1_InMikron, double y1_InMikron, double x2_InMikron, double y2_InMikron) {
  	 	super();
  	 	this.NAME = name;
  	 	borderlineConfig = new BorderlineConfig();
  	 	borderlineConfig.x1_InMikron = x1_InMikron;
  	 	borderlineConfig.y1_InMikron = y1_InMikron;
  	 	borderlineConfig.x2_InMikron = x2_InMikron;
  	 	borderlineConfig.y2_InMikron = y2_InMikron;
  	 	this.color = color;
   }
   
   public BorderlineConfig getBorderlineConfig(){
   	return this.borderlineConfig;
   }
   
       
   Rectangle2D.Double oldDraw = null;  
   
   // assumes the graphics already has its color set
   public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
   	graphics.setStroke(new BasicStroke((int)(2*SimStateServer.getInstance().getEpisimGUIState().getDisplay().getDisplayScale()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
   	graphics.setPaint(this.color);
   	SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
   	graphics.drawLine((int)(info.draw.x+(props.displayScaleX*borderlineConfig.x1_InMikron)),
   			(int)(info.draw.y+(props.displayScaleY*borderlineConfig.y1_InMikron)),
   			(int)(info.draw.x+(props.displayScaleX*borderlineConfig.x2_InMikron)),
   			(int)(info.draw.y+(props.displayScaleY*borderlineConfig.y2_InMikron)));
   }

	public String getPortrayalName() {

		
		return NAME;
	}

	public Double getViewPortRectangle() {

		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT, guiState.DISPLAY_BORDER_TOP, guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
 	   else return new Rectangle2D.Double(0,0,0, 0);
	}

}
