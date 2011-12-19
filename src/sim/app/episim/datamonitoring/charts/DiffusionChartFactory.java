package sim.app.episim.datamonitoring.charts;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import sim.app.episim.util.EnhancedSteppable;
import episiminterfaces.monitoring.EpisimDiffFieldChart;

public class DiffusionChartFactory {
	
	private List<EnhancedSteppable> chartSteppables;
	private List<JPanel> diffusionChartPanels;
	
	
	public DiffusionChartFactory(List<EpisimDiffFieldChart> diffFieldCharts){
		chartSteppables = new ArrayList<EnhancedSteppable>();
		diffusionChartPanels = new ArrayList<JPanel>();
		buildChartsAndSteppables(diffFieldCharts);
	}
	
	private void buildChartsAndSteppables(List<EpisimDiffFieldChart> diffFieldCharts){
		if(diffFieldCharts != null && !diffFieldCharts.isEmpty()){
			for(EpisimDiffFieldChart diffFieldChart: diffFieldCharts){
				DiffusionChartGUI chartGUI = new DiffusionChartGUI(diffFieldChart);
				chartSteppables.add(chartGUI.getChartSteppable());
				diffusionChartPanels.add(chartGUI.getChartPanel());
			}			
		}
	}	
	
	public List<JPanel> getDiffusionChartPanels(){
		return this.diffusionChartPanels;
	}
	
	public List<EnhancedSteppable> getDiffusionChartSteppables(){
		return this.chartSteppables;
	}
}
