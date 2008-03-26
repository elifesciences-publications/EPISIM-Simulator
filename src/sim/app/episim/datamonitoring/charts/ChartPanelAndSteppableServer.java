package sim.app.episim.datamonitoring.charts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartPanel;



public class ChartPanelAndSteppableServer {
	
	private ArrayList<ChartSetChangeListener> listeners;
	private List<ChartPanel> customChartPanels;
	private List<ChartPanel> defaultChartPanels;
	private static ChartPanelAndSteppableServer instance = null;
	private ChartPanelAndSteppableServer(){
		listeners = new ArrayList<ChartSetChangeListener>();
	}
	
	protected static synchronized ChartPanelAndSteppableServer getInstance(){
		if(instance == null) instance = new ChartPanelAndSteppableServer();
		return instance;
	}
	
	public void registerCustomChartPanels(List<ChartPanel> chartPanels){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		this.customChartPanels = chartPanels;
		notifyListeners();
		
	}
	
	public void registerDefaultChartPanels(List<ChartPanel> chartPanels){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		this.defaultChartPanels = chartPanels;
		notifyListeners();
		
	}
	
	public List<ChartPanel> getChartPanels(){
		List<ChartPanel> allPanels = new LinkedList<ChartPanel>();
		if(this.customChartPanels != null)allPanels.addAll(this.customChartPanels);
		if(this.defaultChartPanels != null)allPanels.addAll(this.defaultChartPanels);
		return allPanels;
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
