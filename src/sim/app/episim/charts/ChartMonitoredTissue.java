package sim.app.episim.charts;


import java.util.List;
public interface ChartMonitoredTissue extends ChartMonitoredClass{
	
	
	String getTissueName();
	
	List <ChartMonitoredCellType> getChartMonitoredCellTypes();

}
