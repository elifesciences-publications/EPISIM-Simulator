package sim.app.episim.datamonitoring.charts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.datamonitoring.charts.io.xml.EpisimCellVisualizationChartAdapter;
import sim.app.episim.datamonitoring.charts.io.xml.EpisimChartAdapter;
import sim.app.episim.datamonitoring.charts.io.xml.EpisimDiffFieldChartAdapter;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;
import episiminterfaces.monitoring.EpisimDiffFieldChart;

@XmlRootElement
public class EpisimChartSetImpl implements EpisimChartSet, java.io.Serializable {
	
	private ArrayList<EpisimChart> episimCharts;
	private ArrayList<EpisimCellVisualizationChart> episimCellVisualizationCharts;
	private ArrayList<EpisimDiffFieldChart> episimDiffFieldCharts;
	
	private String name;
	
	private File path;
	
	
	
	public EpisimChartSetImpl(){
		episimCharts = new ArrayList<EpisimChart>();
		episimCellVisualizationCharts = new ArrayList<EpisimCellVisualizationChart>();
		episimDiffFieldCharts = new ArrayList<EpisimDiffFieldChart>();
		name = "";
	}
	
	@XmlElement
	@XmlJavaTypeAdapter(EpisimChartAdapter.class)
	public List<EpisimChart> getEpisimCharts() {		
		return episimCharts;
	}
	
	@XmlElement
	@XmlJavaTypeAdapter(EpisimCellVisualizationChartAdapter.class)
	public List<EpisimCellVisualizationChart> getEpisimCellVisualizationCharts() {
		return episimCellVisualizationCharts;
	}
	
	@XmlElement
	@XmlJavaTypeAdapter(EpisimDiffFieldChartAdapter.class)
	public List<EpisimDiffFieldChart> getEpisimDiffFieldCharts() {
		return episimDiffFieldCharts;
	}

	public void addEpisimChart(EpisimChart chart) {
	   this.episimCharts.add(chart);	   
   }
	public void addEpisimChart(EpisimCellVisualizationChart chart) {
		this.episimCellVisualizationCharts.add(chart);
	}
	public void addEpisimChart(EpisimDiffFieldChart chart) {
		this.episimDiffFieldCharts.add(chart);
	}

	public String getName() {	   
	   return name;
   }

	public void updateChart(EpisimChart chart){		
		EpisimChart oldChart = getEpisimChart(chart.getId());		
		int index = episimCharts.indexOf(oldChart);
		episimCharts.remove(index);
		episimCharts.add(index, chart);
	}

	public void updateChart(EpisimCellVisualizationChart chart) {
		EpisimCellVisualizationChart oldChart = getEpisimCellVisualizationChart(chart.getId());		
		int index = episimCellVisualizationCharts.indexOf(oldChart);
		episimCellVisualizationCharts.remove(index);
		episimCellVisualizationCharts.add(index, chart);		
	}
	
	public void updateChart(EpisimDiffFieldChart chart) {
		EpisimDiffFieldChart oldChart = getEpisimDiffFieldChart(chart.getId());		
		int index = episimDiffFieldCharts.indexOf(oldChart);
		episimDiffFieldCharts.remove(index);
		episimDiffFieldCharts.add(index, chart);		
	}
	
	
	public void removeEpisimChart(long id) {
		EpisimChart chart = getEpisimChart(id);
		if(chart != null)episimCharts.remove(chart); 
   }
	public void removeEpisimCellVisualizationChart(long id) {
		EpisimCellVisualizationChart chart =  getEpisimCellVisualizationChart(id);
		if(chart != null)episimCellVisualizationCharts.remove(chart);
	}
	public void removeEpisimDiffFieldChart(long id) {
		EpisimDiffFieldChart chart =  getEpisimDiffFieldChart(id);
		if(chart != null)episimDiffFieldCharts.remove(chart);
	}
	
	public EpisimChart getEpisimChart(long id) {

		  for(EpisimChart chart: episimCharts){
			  if(chart.getId() == id){ 
				 return chart;
			  }
		  }
		   return null;
	}
	public EpisimCellVisualizationChart getEpisimCellVisualizationChart(long id) {
		  for(EpisimCellVisualizationChart chart: episimCellVisualizationCharts){
			  if(chart.getId() == id){ 
				 return chart;
			  }
		  }
		  return null;
	}
	public EpisimDiffFieldChart getEpisimDiffFieldChart(long id) {
		  for(EpisimDiffFieldChart chart: episimDiffFieldCharts){
			  if(chart.getId() == id){ 
				 return chart;
			  }
		  }
		  return null;
	}

	public void setName(String name) {
	 this.name = name;	   
   }

	public File getPath() {	   
	   return path;
   }

	public void setPath(File path) {
	   this.path = path;	   
   }
	
	public boolean isOneOfTheChartsDirty(){
		for(EpisimChart chart: episimCharts){
			if(chart.isDirty()) return true;			
		}
		for(EpisimCellVisualizationChart chart: episimCellVisualizationCharts){
			if(chart.isDirty()) return true;			
		}
		for(EpisimDiffFieldChart chart: episimDiffFieldCharts){
			if(chart.isDirty()) return true;			
		}
		return false;
	}
	
	public EpisimChartSet clone(){
		EpisimChartSet newChartSet = ObjectManipulations.cloneObject(this);
		for(EpisimChart oldChart : this.getEpisimCharts()){
			newChartSet.removeEpisimChart(oldChart.getId());
			newChartSet.addEpisimChart(oldChart.clone());
		}
		for(EpisimCellVisualizationChart oldChart : this.getEpisimCellVisualizationCharts()){
			newChartSet.removeEpisimCellVisualizationChart(oldChart.getId());
			newChartSet.addEpisimChart(oldChart.clone());
		}
		for(EpisimDiffFieldChart oldChart : this.getEpisimDiffFieldCharts()){
			newChartSet.removeEpisimDiffFieldChart(oldChart.getId());
			newChartSet.addEpisimChart(oldChart.clone());
		}
		return newChartSet;
	}
}
