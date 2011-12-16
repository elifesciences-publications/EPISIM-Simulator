package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;
import java.util.Set;




public interface EpisimChartSet extends Cloneable{
	
	String getName();
	File getPath();
	List<EpisimChart> getEpisimCharts();
	List<EpisimDiffFieldChart> getEpisimDiffFieldCharts();
	
	
	void setName(String name);
	void setPath(File path);

	void addEpisimChart(EpisimChart chart);
	void updateChart(EpisimChart chart);
	void addEpisimChart(EpisimDiffFieldChart chartConfig);
	void updateChart(EpisimDiffFieldChart chartConfig);
	
	EpisimChart getEpisimChart(long id);
	EpisimDiffFieldChart getEpisimDiffFieldChart(long id);
	
	void removeEpisimChart(long id);
	void removeEpisimDiffFieldChart(long id);	
	
	boolean isOneOfTheChartsDirty();
	
	EpisimChartSet clone();
}
