package episiminterfaces.monitoring;

import org.jfree.chart.ChartPanel;

import sim.app.episim.util.EnhancedSteppable;




public interface GeneratedChart {
	
	ChartPanel getChartPanel();
	
	EnhancedSteppable getSteppable();
	
	EnhancedSteppable getPNGSteppable();
	
	void clearAllSeries();

}
