package episiminterfaces;

import java.io.File;
import java.util.List;



public interface EpisimChartSet {
	
	String getName();
	File getPath();
	List<EpisimChart> getEpisimCharts();
	
	void setName(String name);
	void setPath(File path);
	void addEpisimChart(EpisimChart chart);
	void updateChart(EpisimChart chart);
	EpisimChart getEpisimChart(long id);
	void removeEpisimChart(long id);

}
