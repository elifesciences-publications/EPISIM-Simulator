package sim.app.episim.model.visualization;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import sim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import episiminterfaces.EpisimPortrayal;


public class ContinuousUniversalCellPortrayal2D extends ContinuousPortrayal2D implements EpisimPortrayal {

	private final String NAME = "Epidermis";
	
	public ContinuousUniversalCellPortrayal2D(){
		super();
	}
	public String getPortrayalName() {
		return NAME;
	}

	public Double getViewPortRectangle() {
		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();	   
 	   if(guiState != null)return new Rectangle2D.Double(guiState.DISPLAY_BORDER_LEFT,guiState.DISPLAY_BORDER_TOP,guiState.EPIDISPLAYWIDTH, guiState.EPIDISPLAYHEIGHT);
 	   else return new Rectangle2D.Double(0,0,0, 0);
	}
	
	

}
