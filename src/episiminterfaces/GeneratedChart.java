package episiminterfaces;

import org.jfree.chart.ChartPanel;

import sim.app.episim.util.EnhancedSteppable;




public interface GeneratedChart {
	
	ChartPanel getChartPanel();
	
	EnhancedSteppable getSteppable();
	
	void clearAllSeries();

}
