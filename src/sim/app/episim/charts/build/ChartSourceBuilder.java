package sim.app.episim.charts.build;

import sim.app.episim.charts.EpisimChart;


public class ChartSourceBuilder {
	
	private StringBuffer chartSource;
	
	public ChartSourceBuilder(){
		
	}
	
	public String buildEpisimChartSource(EpisimChart episimChart){
		chartSource = new StringBuffer();
				
		return chartSource.toString();
	}
	
	private void appendHeaderAndConstructor(){
		
	}

}
