package sim.app.episim.datamonitoring.charts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartPanel;

import sim.app.episim.util.EnhancedSteppable;



public class ChartPanelAndSteppableServer {
	
	private ArrayList<ChartSetChangeListener> listeners;
	private List<ChartPanel> customChartPanels;
	private List<ChartPanel> defaultChartPanels;
	private List<EnhancedSteppable> customSteppables;
	private List<EnhancedSteppable> defaultSteppables;
	private static ChartPanelAndSteppableServer instance = null;
	private ChartPanelAndSteppableServer(){
		listeners = new ArrayList<ChartSetChangeListener>();
	}
	
	protected static synchronized ChartPanelAndSteppableServer getInstance(){
		if(instance == null) instance = new ChartPanelAndSteppableServer();
		return instance;
	}
	
	public void registerCustomChartPanelsAndSteppables(List<ChartPanel> chartPanels, List<EnhancedSteppable> chartSteppables){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		if(chartSteppables == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart steppables to be registered must not be null!");
		this.customSteppables = chartSteppables;
		this.customChartPanels = chartPanels;
		notifyListeners();
		
	}
	
	public void registerDefaultChartPanelsAndSteppables(List<ChartPanel> chartPanels, List<EnhancedSteppable> chartSteppables){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		if(chartSteppables == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart steppables to be registered must not be null!");
		this.defaultSteppables = chartSteppables;
		this.defaultChartPanels = chartPanels;
		notifyListeners();
		
	}
		
	public List<ChartPanel> getChartPanels(){
		List<ChartPanel> allPanels = new LinkedList<ChartPanel>();
		if(this.customChartPanels != null)allPanels.addAll(this.customChartPanels);
		if(this.defaultChartPanels != null)allPanels.addAll(this.defaultChartPanels);
		return allPanels;
	}
	
	public List<EnhancedSteppable> getChartSteppables(){
		List<EnhancedSteppable> allSteppables = new LinkedList<EnhancedSteppable>();
		if(this.customSteppables != null)allSteppables.addAll(this.customSteppables);
		if(this.defaultSteppables != null)allSteppables.addAll(this.defaultSteppables);
		return allSteppables;
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
