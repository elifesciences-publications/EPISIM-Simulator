package sim.app.episim.visualization.twodim;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.portrayal.DrawInfo2D;
import episiminterfaces.EpisimPortrayal;


public class SurfacePortrayal extends AbstractSpatialityScalePortrayal2D implements EpisimPortrayal{
	
  private final String NAME;	
  private double startXInMikron = 0;
  private double startYInMikron = 0;
  private double widthInMikron = -1;
  private double heightInMikron = -1;
  private Color color = Color.BLACK;
  
  public static final double MAX_HEIGHT_FLAG = -1;
  public static final double MAX_WIDTH_FLAG = -1;
  
  
  public SurfacePortrayal(String name, Color color){
	  this(name, color, 0, 0,-1,-1);
  }
  
   
  
  public SurfacePortrayal(String name, Color color, double startXInMikron, double startYInMikron, double widthInMikron, double heightInMikron) {
 	 super();
 	 this.NAME = name;
 	 this.startXInMikron = startXInMikron;
 	 this.startYInMikron = startYInMikron;
 	 this.widthInMikron = widthInMikron;
 	 this.heightInMikron = heightInMikron;
 	 this.color = color;
  }
  
      
  Rectangle2D.Double oldDraw = null;  
  
  // assumes the graphics already has its color set
  public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	  graphics.setStroke(new BasicStroke((int)(1*SimStateServer.getInstance().getEpisimGUIState().getDisplay().getDisplayScale()), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
  	graphics.setPaint(color);
  	SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
  	double width = widthInMikron < 0 ? info.draw.width :props.displayScaleX*widthInMikron;
  	double height = heightInMikron < 0 ? info.draw.height :props.displayScaleY*heightInMikron;
  	
  	graphics.fillRect((int)(info.draw.x+(props.displayScaleX*startXInMikron)),
  			(int)(info.draw.y+(props.displayScaleY*startYInMikron)),
  			(int)(width),
  			(int)(height));
  }

	public String getPortrayalName() {

		
		return NAME;
	}

	public Double getViewPortRectangle() {
		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT, guiState.DISPLAY_BORDER_TOP, guiState.getEpiDisplayWidth(), guiState.getEpiDisplayHeight());
	   else return new Rectangle2D.Double(0,0,0, 0);
	}

}
