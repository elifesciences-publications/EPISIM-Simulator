package episimfactories;

import javax.swing.JPanel;

import sim.app.episim.charts.EpisimChart;
import sim.app.episim.charts.EpisimChartSet;
import sim.engine.Steppable;


public abstract class AbstractChartSetFactory {
	
	
	public abstract EpisimChartSet getEpisimChartSet();
	public abstract JPanel getPanelForChart(EpisimChart chart);
   public abstract Steppable getSteppableForChart(EpisimChart chart);

}
