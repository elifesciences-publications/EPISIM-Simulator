package sim.app.episim.charts;

import java.util.ArrayList;
import java.util.List;


public class EpisimChartSetImpl implements EpisimChartSet {
	
	private List<EpisimChart> episimCharts;
	
	private String name;
	
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

	public void removeEpisimChart(long id) {

	  for(EpisimChart chart: episimCharts){
		  if(chart.getId() == id){ 
			  episimCharts.remove(chart);
			  break;
		  }
	  }
	   
   }

	public void setName(String name) {

	 this.name = name;
	   
   }

}
