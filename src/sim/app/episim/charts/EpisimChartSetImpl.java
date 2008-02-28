package sim.app.episim.charts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class EpisimChartSetImpl implements EpisimChartSet, java.io.Serializable {
	
	private List<EpisimChart> episimCharts;
	
	private String name;
	
	private File path;
	
	public EpisimChartSetImpl(){
		episimCharts = new ArrayList<EpisimChart>();
		name = "";
	}

	public List<EpisimChart> getEpisimCharts() {

		
		return episimCharts;
	}

	public void addEpisimChart(EpisimChart chart) {

	   this.episimCharts.add(chart);
	   
   }

	public String getName() {

	   
	   return name;
   }

	public void updateChart(EpisimChart chart){
		removeEpisimChart(chart.getId());
		addEpisimChart(chart);
	}
	
	public void removeEpisimChart(long id) {

	 
			  episimCharts.remove(getEpisimChart(id));
			
	   
   }
	
	public EpisimChart getEpisimChart(long id) {

		  for(EpisimChart chart: episimCharts){
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

}
