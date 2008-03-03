package episimfactories;

import java.io.File;
import java.io.InputStream;

import javax.swing.JPanel;

import sim.app.episim.charts.EpisimChart;
import sim.app.episim.charts.EpisimChartSet;
import sim.engine.Steppable;


public abstract class AbstractChartSetFactory {
	
	public abstract String getEpisimChartSetBinaryName();
	public abstract EpisimChartSet getEpisimChartSet(InputStream stream);
	public abstract JPanel getPanelForChart(EpisimChart chart);
   public abstract Steppable getSteppableForChart(EpisimChart chart);

}
