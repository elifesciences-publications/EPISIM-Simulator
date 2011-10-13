package sim.app.episim.visualization;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
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
	  graphics.setStroke(new BasicStroke((int)(1*this.getScaleFactorOfTheDisplay()), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
  	graphics.setPaint(color);
  	
  	double width = widthInMikron < 0 ? info.draw.width :this.getScaledNumberOfPixelPerMicrometer(info)*widthInMikron;
  	double height = heightInMikron < 0 ? info.draw.height :this.getScaledNumberOfPixelPerMicrometer(info)*heightInMikron;
  	
  	graphics.fillRect((int)(info.draw.x+(this.getScaledNumberOfPixelPerMicrometer(info)*startXInMikron)),
  			(int)(info.draw.y+(this.getScaledNumberOfPixelPerMicrometer(info)*startYInMikron)),
  			(int)(width),
  			(int)(height));
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
