package sim.app.episim.model.biomechanics.vertexbased;

import java.util.HashMap;


public class CellPolygonRegistry {
	
	private static HashMap<Long, Long> motherCellId_simStep_map = new HashMap<Long,Long>();
	private static HashMap<Long, CellPolygon> motherCellId_cellPolygon_map = new HashMap<Long,CellPolygon>();
	
	public static void registerNewCellPolygon(long motherCellId, long simstep, CellPolygon cellPolygon){
		motherCellId_simStep_map.put(motherCellId, simstep);
		motherCellId_cellPolygon_map.put(motherCellId, cellPolygon);
	}
	
	protected static CellPolygon getNewCellPolygon(long motherCellId, long simStep){
		if(motherCellId_cellPolygon_map.containsKey(motherCellId) && motherCellId_simStep_map.containsKey(motherCellId) 
				&& motherCellId_simStep_map.get(motherCellId) == simStep){
			return motherCellId_cellPolygon_map.get(motherCellId);
		}
		return null;
	}

}
