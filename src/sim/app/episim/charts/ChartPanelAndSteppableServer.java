package sim.app.episim.charts;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartPanel;



public class ChartPanelAndSteppableServer {
	
	private ArrayList<ChartSetChangeListener> listeners;
	private List<ChartPanel> chartPanels;
	private static ChartPanelAndSteppableServer instance = null;
	private ChartPanelAndSteppableServer(){
		listeners = new ArrayList<ChartSetChangeListener>();
	}
	
	protected static synchronized ChartPanelAndSteppableServer getInstance(){
		if(instance == null) instance = new ChartPanelAndSteppableServer();
		return instance;
	}
	
	public void registerChartPanels(List<ChartPanel> chartPanels){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		this.chartPanels = chartPanels;
		notifyListeners();
		
	}
	
	public List<ChartPanel> getChartPanels(){
		return this.chartPanels;
	}
	
	public void registerChartSetChangeListener(ChartSetChangeListener listener){
		listeners.add(listener);
	}
	
	public void removeAllListeners(){
		this.listeners.clear();
	}
	
	private void notifyListeners(){
		for(ChartSetChangeListener actListener : this.listeners) actListener.chartSetHasChanged();
	}
	

}
