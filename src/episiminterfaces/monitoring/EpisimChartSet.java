package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;
import java.util.Set;




public interface EpisimChartSet extends Cloneable{
	
	String getName();
	File getPath();
	List<EpisimChart> getEpisimCharts();
	
	
	void setName(String name);
	void setPath(File path);

	void addEpisimChart(EpisimChart chart);
	void updateChart(EpisimChart chart);
	EpisimChart getEpisimChart(long id);
	void removeEpisimChart(long id);
	boolean isOneOfTheChartsDirty();
	
	EpisimChartSet clone();
}
