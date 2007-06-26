package sim.app.episim.charts;


import java.util.Set;

public class ChartController {
	
	private static ChartController instance = null;
	
	private Set <ChartMonitoredCellType> chartMonitoredCellTypes;
	
	private ChartController(){}
	
	
	public synchronized static ChartController getInstance(){
		if(instance == null) instance = new ChartController();
		
		return instance;
	}
	
	public void registerCelltypeForChartMonitoring(ChartMonitoredCellType cellType){
		
	}
   
	public void registerTissueForChartMonitoring(){
		
	}

}
