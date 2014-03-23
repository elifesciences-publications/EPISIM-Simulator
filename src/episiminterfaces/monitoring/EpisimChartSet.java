package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;
import java.util.Set;




public interface EpisimChartSet extends Cloneable{
	
	String getName();
	File getPath();
	List<EpisimChart> getEpisimCharts();
	List<EpisimCellVisualizationChart> getEpisimCellVisualizationCharts();
	List<EpisimDiffFieldChart> getEpisimDiffFieldCharts();
	
	
	void setName(String name);
	void setPath(File path);

	void addEpisimChart(EpisimChart chart);
	void updateChart(EpisimChart chart);
	void addEpisimChart(EpisimCellVisualizationChart chartConfig);
	void updateChart(EpisimCellVisualizationChart chartConfig);
	void addEpisimChart(EpisimDiffFieldChart chartConfig);
	void updateChart(EpisimDiffFieldChart chartConfig);
	
	EpisimChart getEpisimChart(long id);
	EpisimCellVisualizationChart getEpisimCellVisualizationChart(long id);
	EpisimDiffFieldChart getEpisimDiffFieldChart(long id);
	
	void removeEpisimChart(long id);
	void removeEpisimDiffFieldChart(long id);
	void removeEpisimCellVisualizationChart(long id);
	
	boolean isOneOfTheChartsDirty();
	
	EpisimChartSet clone();
}
