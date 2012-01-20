package sim.app.episim.model.visualization;

import java.awt.Color;
import java.awt.geom.Rectangle2D;


import episiminterfaces.EpisimPortrayal;
import sim.display3d.Display3DHack;
import sim.portrayal3d.grid.ObjectGridPortrayal3D;
import sim.portrayal3d.grid.SparseGridPortrayal3D;
import sim.portrayal3d.simple.CubePortrayal3D;
import sim.portrayal3d.simple.SpherePortrayal3D;


public class HexagonalCellGridPortrayal3D extends SparseGridPortrayal3D implements EpisimPortrayal{
	private static final String NAME = "Epithelial Cells";
	
	public HexagonalCellGridPortrayal3D(double scale){
		super();
		setPortrayalForAll(new HexagonalCellPortrayal3D());
		
		this.scale(scale);
		this.translate(scale/2d, scale/2d, scale/2d);
	}
	
	public String getPortrayalName() {

		return NAME;
	}

	public Rectangle2D.Double getViewPortRectangle() {
		return new Rectangle2D.Double(0d,0d,0d,0d);
	}

}
