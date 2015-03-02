package episiminterfaces;

import sim.app.episim.model.biomechanics.vertexbased2d.geom.CellPolygon;


public interface CellPolygonProliferationSuccessListener {
	
	void proliferationCompleted(CellPolygon oldCell, CellPolygon newCell);
	void maturationCompleted(CellPolygon pol);
	void apoptosisCompleted(CellPolygon pol);

}
