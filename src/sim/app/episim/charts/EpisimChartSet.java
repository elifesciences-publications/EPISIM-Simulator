package sim.app.episim.charts;

import java.util.List;


public interface EpisimChartSet {
	
	String getName();
	List<EpisimChart> getEpisimCharts();
	
	void setName(String name);
	void addEpisimChart(EpisimChart chart);
	void removeEpisimChart(long id);

}
