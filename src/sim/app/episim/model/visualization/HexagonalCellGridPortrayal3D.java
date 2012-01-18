package sim.app.episim.model.visualization;

import java.awt.geom.Rectangle2D;


import episiminterfaces.EpisimPortrayal;
import sim.portrayal3d.grid.ObjectGridPortrayal3D;


public class HexagonalCellGridPortrayal3D extends ObjectGridPortrayal3D implements EpisimPortrayal{
	private static final String NAME = "Epithelial Cells";
	
	public HexagonalCellGridPortrayal3D(){
		super();
	}
	
	public String getPortrayalName() {

		return NAME;
	}

	public Rectangle2D.Double getViewPortRectangle() {
		return new Rectangle2D.Double(0d,0d,0d,0d);
	}

}
