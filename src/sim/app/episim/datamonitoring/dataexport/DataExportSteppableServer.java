package sim.app.episim.datamonitoring.dataexport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartPanel;

import sim.app.episim.AbstractCell;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.datamonitoring.charts.ChartPanelAndSteppableServer;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;
import episimexceptions.MissingObjectsException;
import episimfactories.AbstractChartSetFactory;
import episimfactories.AbstractDataExportFactory;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.GeneratedChart;
import episiminterfaces.monitoring.GeneratedDataExport;


public class DataExportSteppableServer {
	
	private Set<DataExportChangeListener> listeners;
	private List<GeneratedDataExport> customDataExportDefinitions;
	private List<EnhancedSteppable> customSteppables;
	private static DataExportSteppableServer instance = null;
	private AbstractDataExportFactory factory = null;
	GenericBag<AbstractCell> alreadyRegisteredVersionAllCells = null;
	private DataExportSteppableServer(){
		listeners = new HashSet<DataExportChangeListener>();
	}
	
	protected static synchronized DataExportSteppableServer getInstance(){
		if(instance == null) instance = new DataExportSteppableServer();
		return instance;
	}
	
	public void registerCustomDataExportSteppables(List<GeneratedDataExport> dataExportDefinitions, List<EnhancedSteppable> dataExportSteppables, AbstractDataExportFactory factory){
		if(dataExportDefinitions == null) throw new IllegalArgumentException("DataExportSteppableServer: List with data export definitions to be registered must not be null!");
		if(dataExportSteppables == null) throw new IllegalArgumentException("DataExportSteppableServer: List with data export definition steppables to be registered must not be null!");
		if(factory == null) throw new IllegalArgumentException("DataExportSteppableServer: Data-Export-Factory to be registered must not be null!");
		this.customSteppables = dataExportSteppables;
		this.customDataExportDefinitions = dataExportDefinitions;
		this.factory = factory;
		notifyListeners();
		
	}
	
	public List<GeneratedDataExport> getDataExports(){
				
		return customDataExportDefinitions;
	}	
	
	public List<EnhancedSteppable> getDataExportSteppables(GenericBag<AbstractCell> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException{
		if(factory != null || alreadyRegisteredVersionAllCells != allCells){
			alreadyRegisteredVersionAllCells = allCells;
			factory.registerNecessaryObjects(allCells, continuous, objects);
			CalculationController.getInstance().registerCells(allCells);
		}
		return customSteppables;
	}
	
	public void registerDataExportChangeListener(DataExportChangeListener listener){
		cleanListeners(listener.getClass().getName());
		listeners.add(listener);
	}
	
	public void removeAllListeners(){
		this.listeners.clear();
	}
	public void removeAllSteppables(){
		if(this.customSteppables != null)this.customSteppables.clear();
	}
	public void removeAllDataExports(){
		if(this.customDataExportDefinitions != null)this.customDataExportDefinitions.clear();
	}
	
	public void newSimulationRun(){
		
		if(this.customDataExportDefinitions != null){
			for(GeneratedDataExport dataExport: this.customDataExportDefinitions){
				dataExport.getCSVWriter().simulationWasStarted();
			}
		}
	}
	
	public void simulationWasStopped(){
		
		if(this.customDataExportDefinitions != null){
			for(GeneratedDataExport dataExport: this.customDataExportDefinitions){
				dataExport.getCSVWriter().simulationWasStopped();
			}
		}
	}
	
	private void notifyListeners(){
		for(DataExportChangeListener actListener : this.listeners) actListener.dataExportHasChanged();
	}
	
	private void cleanListeners(String className){
		for(DataExportChangeListener actListener: listeners){
			if(actListener.getClass().getName().equals(className)){
				listeners.remove(actListener);
				return;
			}
		}
	}

}
