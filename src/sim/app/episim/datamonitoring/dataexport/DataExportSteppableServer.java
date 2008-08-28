package sim.app.episim.datamonitoring.dataexport;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartPanel;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.datamonitoring.charts.ChartPanelAndSteppableServer;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;
import episimexceptions.MissingObjectsException;
import episimfactories.AbstractChartSetFactory;
import episimfactories.AbstractDataExportFactory;
import episiminterfaces.EpisimDataExportDefinition;
import episiminterfaces.GeneratedChart;
import episiminterfaces.GeneratedDataExport;


public class DataExportSteppableServer {
	
	private ArrayList<DataExportChangeListener> listeners;
	private List<GeneratedDataExport> customDataExportDefinitions;
	private List<EnhancedSteppable> customSteppables;
	private static DataExportSteppableServer instance = null;
	private AbstractDataExportFactory factory = null;
	private DataExportSteppableServer(){
		listeners = new ArrayList<DataExportChangeListener>();
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
	
	public List<GeneratedDataExport> getChartPanels(){
				
		return customDataExportDefinitions;
	}	
	
	public List<EnhancedSteppable> getDataExportSteppables(GenericBag<CellType> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException{
		if(factory != null){
			factory.registerNecessaryObjects(allCells, continuous, objects);
			CalculationController.getInstance().registerCells(allCells);
		}
		
		return customSteppables;
	}
	
	public void registerDataExportChangeListener(DataExportChangeListener listener){
		listeners.add(listener);
	}
	
	public void removeAllListeners(){
		this.listeners.clear();
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

}
