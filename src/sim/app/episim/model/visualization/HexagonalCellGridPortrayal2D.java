package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import episiminterfaces.EpisimPortrayal;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.field.grid.Grid2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Portrayal;
import sim.portrayal.grid.HexaObjectGridPortrayal2D;
import sim.portrayal.simple.HexagonalPortrayal2D;
import sim.util.Double2D;


public class HexagonalCellGridPortrayal2D extends HexaObjectGridPortrayal2D implements EpisimPortrayal{
	private static final String NAME = "Corneal Epithelial Cells";
	private HexagonalCellPortrayal2D defaultHexPortrayal;
	
	 private Paint paint;  
       
    
   
    
    private static long actSimStep;
    

	
	
	
	public HexagonalCellGridPortrayal2D(){
		this(Color.BLACK); 
	}
	
	 
      
    public HexagonalCellGridPortrayal2D(Paint paint){ 
   	super();   	
   	this.paint = paint;   
   	
	  	defaultHexPortrayal = new HexagonalCellPortrayal2D(this, false);
    }    
	
	
	 public Portrayal getDefaultPortrayal()
    {
		 return defaultHexPortrayal;
    }
	 
	 public String getPortrayalName() {
	 	   return NAME;
	 }



	public Rectangle2D.Double getViewPortRectangle() {
		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
	   else return new Rectangle2D.Double(0,0,0, 0);
   }
	
}
