package sim.app.episim.charts;

import java.util.List;

public class ChartController {
	
	private static ChartController instance = null;
	
	private List <ChartMonitoredCellType> chartMonitoredClasses;
	
	private ChartController(){}
	
	
	public synchronized static ChartController getInstance(){
		if(instance == null) instance = new ChartController();
		
		return instance;
	}
	
	public void registerCelltypeForChartMonitoring(){
		
	}
   
	public void registerTissueForChartMonitoring(){
		
	}

}
