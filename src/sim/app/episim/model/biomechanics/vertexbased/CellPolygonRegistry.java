package sim.app.episim.model.biomechanics.vertexbased;

import java.util.HashMap;


public class CellPolygonRegistry {
	
	private static HashMap<Long, CellPolygon> motherCellId_cellPolygon_map = new HashMap<Long,CellPolygon>();
	
	public static void registerNewCellPolygon(long motherCellId, CellPolygon cellPolygon){
		motherCellId_cellPolygon_map.put(motherCellId, cellPolygon);
	}
	
	protected static CellPolygon getNewCellPolygon(long motherCellId){
		if(motherCellId_cellPolygon_map.containsKey(motherCellId)){			
			CellPolygon pol = motherCellId_cellPolygon_map.get(motherCellId);
			motherCellId_cellPolygon_map.remove(motherCellId);			
			return pol;
		}
		return null;
	}
	
	protected static boolean isWaitingForCellProliferation(long motherCellId){
		return motherCellId_cellPolygon_map.containsKey(motherCellId);
	}

}
