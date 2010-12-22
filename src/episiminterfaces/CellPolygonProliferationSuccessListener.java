package episiminterfaces;

import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;


public interface CellPolygonProliferationSuccessListener {
	
	void proliferationCompleted(CellPolygon pol);
	void apoptosisCompleted(CellPolygon pol);

}
