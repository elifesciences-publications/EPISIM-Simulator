package sim.app.episim.datamonitoring.charts;

import java.util.List;

import javax.swing.JPanel;

import sim.app.episim.util.EnhancedSteppable;

import episiminterfaces.monitoring.EpisimDiffFieldChart;


public class DiffusionChartFactory {
	
	private List<EpisimDiffFieldChart> diffFieldCharts;
	
	public DiffusionChartFactory(List<EpisimDiffFieldChart> diffFieldCharts){
		this.diffFieldCharts = diffFieldCharts;
	}
	
	public List<JPanel> getDiffusionChartPanels(){
		return null;
	}
	
	public List<EnhancedSteppable> getDiffusionChartSteppables(){
		return null;
	}
}
