package episiminterfaces;

import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;


public interface CellPolygonProliferationSuccessListener {
	
	void proliferationCompleted(CellPolygon oldCell, CellPolygon newCell);
	void apoptosisCompleted(CellPolygon pol);

}
