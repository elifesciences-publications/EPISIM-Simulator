package sim.app.episim.datamonitoring.charts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import episimexceptions.MissingObjectsException;
import episimfactories.AbstractChartSetFactory;
import episiminterfaces.monitoring.GeneratedChart;

import sim.app.episim.AbstractCell;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;



public class ChartPanelAndSteppableServer {
	
	private Set<ChartSetChangeListener> listeners;
	private List<ChartPanel> customChartPanels;
	private List<JPanel> diffusionChartPanels;
	private List<ChartPanel> defaultChartPanels;
	private List<EnhancedSteppable> customSteppables;
	private List<EnhancedSteppable> defaultSteppables;
	private static ChartPanelAndSteppableServer instance = null;
	private AbstractChartSetFactory factory = null;
	GenericBag<AbstractCell> alreadyRegisteredVersionAllCells = null;
	
	private ChartPanelAndSteppableServer(){
		listeners = new HashSet<ChartSetChangeListener>();
		diffusionChartPanels = new ArrayList<JPanel>();
		customChartPanels = new ArrayList<ChartPanel>();
	}
	
	protected static synchronized ChartPanelAndSteppableServer getInstance(){
		if(instance == null) instance = new ChartPanelAndSteppableServer();
		return instance;
	}
	
	public void registerCustomChartPanelsAndSteppables(List<ChartPanel> chartPanels, List<JPanel> diffusionChartPanels, List<EnhancedSteppable> chartSteppables, AbstractChartSetFactory factory){
		if(chartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart panels to be registered must not be null!");
		if(diffusionChartPanels == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with diffusion chart panels to be registered must not be null!");
		if(chartSteppables == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: List with chart steppables to be registered must not be null!");
		if(factory == null) throw new IllegalArgumentException("ChartPanelAndSteppableServer: Chart-Set-Factory to be registered must not be null!");
		this.customSteppables = chartSteppables;
		this.customChartPanels = chartPanels;
		this.diffusionChartPanels = diffusionChartPanels;
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
	
	public List<JPanel> getDiffusionChartPanels(){	
		return this.diffusionChartPanels;
	}
	
	public List<EnhancedSteppable> getChartSteppables(GenericBag<AbstractCell> allCells, Object[] objects) throws MissingObjectsException{
		if(factory != null && alreadyRegisteredVersionAllCells != allCells){
			alreadyRegisteredVersionAllCells = allCells;
			factory.registerNecessaryObjects(allCells, objects);
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
		if(this.diffusionChartPanels != null)this.diffusionChartPanels.clear();
		if(this.defaultChartPanels != null)this.defaultChartPanels.clear();
		notifyListeners();
	}
	
	public void actLoadedChartSetWasClosed(){
		if(this.customChartPanels != null){ 
			this.customChartPanels.clear();
			
		}
		if(this.diffusionChartPanels != null){ 
			this.diffusionChartPanels.clear();
			
		}
		if(this.customSteppables != null){ 
			this.customSteppables.clear();
			
		}
		notifyListeners();
	}
	
	
	public void removeAllSteppables(){
		if(this.customSteppables != null)this.customSteppables.clear();
		if(this.defaultSteppables != null)this.defaultSteppables.clear();
	}
	
	public void clearAllDefaultChartSeries(){
		DefaultCharts.getInstance().clearSeries();
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
