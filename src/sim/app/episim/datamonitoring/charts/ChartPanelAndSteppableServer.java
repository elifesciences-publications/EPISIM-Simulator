package sim.app.episim.datamonitoring.charts;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartPanel;

import episimexceptions.MissingObjectsException;
import episimfactories.AbstractChartSetFactory;
import episiminterfaces.monitoring.GeneratedChart;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;



public class ChartPanelAndSteppableServer {
	
	private Set<ChartSetChangeListener> listeners;
	private List<ChartPanel> customChartPanels;
	private List<ChartPanel> defaultChartPanels;
	private List<EnhancedSteppable> customSteppables;
	private List<EnhancedSteppable> defaultSteppables;
	private static ChartPanelAndSteppableServer instance = null;
	private AbstractChartSetFactory factory = null;
	private ChartPanelAndSteppableServer(){
		listeners = new HashSet<ChartSetChangeListener>();
	}
	
	protected static synchronized ChartPanelAndSteppableServer getInstance(){
		if(instance == null) instance = new ChartPanelAndSteppableServer();
		return instance;
	}
	
	public void registerCustomChartPanelsAndSteppables(List<ChartPanel> chartPanels, List<EnhancedSteppable> chartSteppables, AbstractChartSetFactory factory){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		if(chartSteppables == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart steppables to be registered must not be null!");
		if(factory == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: Chart-Set-Factory to be registered must not be null!");
		this.customSteppables = chartSteppables;
		this.customChartPanels = chartPanels;
		this.factory = factory;
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
	
	public List<EnhancedSteppable> getChartSteppables(GenericBag<CellType> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException{
		if(factory != null){
			factory.registerNecessaryObjects(allCells, continuous, objects);
			CalculationController.getInstance().registerCells(allCells);
		}
		List<EnhancedSteppable> allSteppables = new LinkedList<EnhancedSteppable>();
		if(this.customSteppables != null)allSteppables.addAll(this.customSteppables);
		if(this.defaultSteppables != null)allSteppables.addAll(this.defaultSteppables);
		return allSteppables;
	}
	
	public boolean registerChartSetChangeListener(ChartSetChangeListener listener){
		
		cleanListeners(listener.getClass().getName());
		return listeners.add(listener);
	}
	
	public void removeAllListeners(){
		this.listeners.clear();
	}
	
	public void removeAllChartPanels(){
		if(this.customChartPanels != null)this.customChartPanels.clear();
		if(this.defaultChartPanels != null)this.defaultChartPanels.clear();
		notifyListeners();
	}
	
	public void actLoadedChartSetWasClosed(){
		if(this.customChartPanels != null){ 
			this.customChartPanels.clear();
			this.customChartPanels = null;
		}
		if(this.customSteppables != null){ 
			this.customSteppables.clear();
			this.customSteppables = null;
		}
		notifyListeners();
	}
	
	
	public void removeAllSteppables(){
		if(this.customSteppables != null)this.customSteppables.clear();
		if(this.defaultSteppables != null)this.defaultSteppables.clear();
	}
	
	public void clearAllSeries(){
		DefaultCharts.getInstance().clearSeries();
		if(this.customChartPanels != null){
			for(ChartPanel pan: this.customChartPanels){
				if(pan.getChart() instanceof GeneratedChart) ((GeneratedChart) pan.getChart()).clearAllSeries();
			}
		}
	}
	
	
	
	private void notifyListeners(){
		for(ChartSetChangeListener actListener : this.listeners) actListener.chartSetHasChanged();
	}
	
	private void cleanListeners(String className){
		for(ChartSetChangeListener actListener: listeners){
			if(actListener.getClass().getName().equals(className)){
				listeners.remove(actListener);
				return;
			}
		}
	}

}
