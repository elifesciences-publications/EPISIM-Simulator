package sim.app.episim.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ChartController {
	
	private static ChartController instance = null;
	
	private List <ChartMonitoredCellType> chartMonitoredClasses;
	
	private ConcurrentHashMap<String, ChartMonitoredCellType> chartMonitoredCellTypes;
	
	private ConcurrentHashMap<String, ChartMonitoredTissue> chartMonitoredTissues;
	
	
	private ChartController(){
		chartMonitoredClasses = new ArrayList<ChartMonitoredCellType>();
		chartMonitoredCellTypes = new ConcurrentHashMap<String, ChartMonitoredCellType>();
		chartMonitoredTissues = new ConcurrentHashMap<String, ChartMonitoredTissue>();
	}
	
	
	public synchronized static ChartController getInstance(){
		if(instance == null) instance = new ChartController();
		
		return instance;
	}
	
	public void registerCelltypeForChartMonitoring(ChartMonitoredCellType celltype){
		if(chartMonitoredCellTypes.containsKey(celltype.getClass().getSimpleName()))
				chartMonitoredCellTypes.put(celltype.getClass().getSimpleName(), celltype);
		
	}
   
	public void registerTissueForChartMonitoring(ChartMonitoredTissue tissue){
		if(chartMonitoredTissues.containsKey(tissue.getClass().getSimpleName()))
			chartMonitoredTissues.put(tissue.getClass().getSimpleName(), tissue);
	}

}