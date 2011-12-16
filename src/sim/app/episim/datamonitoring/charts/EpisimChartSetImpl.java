package sim.app.episim.datamonitoring.charts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;
import episiminterfaces.monitoring.EpisimDiffFieldChart;


public class EpisimChartSetImpl implements EpisimChartSet, java.io.Serializable {
	
	private ArrayList<EpisimChart> episimCharts;
	private ArrayList<EpisimDiffFieldChart> episimDiffFieldCharts;
	
	private String name;
	
	private File path;
	
	
	
	public EpisimChartSetImpl(){
		episimCharts = new ArrayList<EpisimChart>();
		episimDiffFieldCharts = new ArrayList<EpisimDiffFieldChart>();
		name = "";
	}

	public List<EpisimChart> getEpisimCharts() {		
		return episimCharts;
	}
	public List<EpisimDiffFieldChart> getEpisimDiffFieldCharts() {
		return episimDiffFieldCharts;
	}

	public void addEpisimChart(EpisimChart chart) {
	   this.episimCharts.add(chart);	   
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

	public void updateChart(EpisimDiffFieldChart chart) {
		EpisimDiffFieldChart oldChart = getEpisimDiffFieldChart(chart.getId());		
		int index = episimDiffFieldCharts.indexOf(oldChart);
		episimDiffFieldCharts.remove(index);
		episimDiffFieldCharts.add(index, chart);		
	}
	
	
	public void removeEpisimChart(long id) {	 
			  episimCharts.remove(getEpisimChart(id)); 
   }
	public void removeEpisimDiffFieldChart(long id) {
		episimDiffFieldCharts.remove(getEpisimDiffFieldChart(id));
	}
	
	
	public EpisimChart getEpisimChart(long id) {

		  for(EpisimChart chart: episimCharts){
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
		for(EpisimDiffFieldChart oldChart : this.getEpisimDiffFieldCharts()){
			newChartSet.removeEpisimDiffFieldChart(oldChart.getId());
			newChartSet.addEpisimChart(oldChart.clone());
		}	
		return newChartSet;
	}
}
