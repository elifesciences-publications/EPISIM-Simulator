package sim.app.episim.model.visualization;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import javax.media.j3d.PolygonAttributes;

import sim.portrayal3d.continuous.ContinuousPortrayal3D;
import episiminterfaces.EpisimPortrayal;


public class ContinuousUniversalCellPortrayal3D extends ContinuousPortrayal3D implements EpisimPortrayal {

	private final String NAME = "Epidermis";
	
	private PolygonAttributes polygonAttributes;
	public ContinuousUniversalCellPortrayal3D(){
		super();		
		polygonAttributes = new PolygonAttributes();
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_READ);
		polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		polygonAttributes.setPolygonOffsetFactor(1.2f);
		setPortrayalForAll(new UniversalCellPortrayal3D(polygonAttributes));
	}
	public String getPortrayalName() {
		return NAME;
	}

	public Double getViewPortRectangle() {
		return new Rectangle2D.Double(0,0,0, 0);
	}
}