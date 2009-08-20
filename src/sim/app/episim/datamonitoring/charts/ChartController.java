package sim.app.episim.datamonitoring.charts;

import java.awt.Frame;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartPanel;

import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;

import sim.app.episim.datamonitoring.parser.*;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.datamonitoring.charts.io.ECSFileReader;
import sim.app.episim.datamonitoring.charts.io.ECSFileWriter;
import sim.app.episim.datamonitoring.charts.io.PNGPrinter;
import sim.app.episim.datamonitoring.parser.DataMonitoringExpressionChecker;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.CompatibilityChecker;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.field.continuous.Continuous2D;
public class ChartController {
	
	private static ChartController instance = null;
	
	
	
	private long nextChartId = 0;
	
	private TissueType chartMonitoredTissue;
	private EpisimChartSet actLoadedChartSet;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validDataTypes;
	private ExtendedFileChooser ecsChooser = new ExtendedFileChooser("ecs");
	private ChartController(){
		
		markerPrefixes = new HashSet<String>();
		validDataTypes = new HashSet<Class<?>>();
		
		markerPrefixes.add("get");
		markerPrefixes.add("is");
		
		validDataTypes.add(Integer.TYPE);
		validDataTypes.add(Short.TYPE);
		validDataTypes.add(Byte.TYPE);
		validDataTypes.add(Long.TYPE);
		validDataTypes.add(Float.TYPE);
		validDataTypes.add(Double.TYPE);
		validDataTypes.add(Boolean.TYPE);
	}
	
	public boolean isAlreadyChartSetLoaded(){
		if(this.actLoadedChartSet != null) return true;
		
		return false;
	}
	
	protected long getNextChartId(){
	
		return System.currentTimeMillis() + (this.nextChartId++);
	}
		
	public synchronized static ChartController getInstance(){
		if(instance == null){ 
			
			instance = new ChartController();
		}
		
		return instance;
	}
	   
	public void setChartMonitoredTissue(TissueType tissue){
		this.chartMonitoredTissue = tissue;
	}
	
	protected EpisimChart showChartCreationWizard(Frame parent){
		return showChartCreationWizard(parent, null);
	}
	protected EpisimChart showChartCreationWizard(Frame parent, EpisimChart chart){
		ChartCreationWizard wizard = new ChartCreationWizard(parent, "Chart-Creation-Wizard", true, 
		new TissueCellDataFieldsInspector(this.chartMonitoredTissue, this.markerPrefixes, this.validDataTypes));
		
		if(this.chartMonitoredTissue != null){
			if(chart == null)wizard.showWizard();
			else wizard.showWizard(chart);
		}
			
		return wizard.getEpisimChart();
	}
	
	public boolean loadChartSet(Frame parent){
		ecsChooser.setDialogTitle("Load Episim-Chartset");
		if(ecsChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
			try{
				return loadEpisimChartSet(ecsChooser.getSelectedFile().toURI().toURL(), parent);	
			}
			catch (MalformedURLException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		return false;
	}
	
	
	
	public void registerChartSetChangeListener(ChartSetChangeListener changeListener){
		ChartPanelAndSteppableServer.getInstance().registerChartSetChangeListener(changeListener);
		//closeActLoadedChartSet();
	}
	
	public void activateDefaultChart(String name){
		DefaultCharts.getInstance().activateDefaultChart(name);
	}
	
	public void deactivateDefaultChart(String name){
		DefaultCharts.getInstance().deactivateDefaultChart(name);
	}
	
	public void showDefaultChartsSelectionDialog(Frame parent){
		DefaultChartSelectDialog dialog = new DefaultChartSelectDialog(parent, "Select Episim-Default-Charts", true, DefaultCharts.getInstance().getNamesAndActivationStatusOfAvailableDefaultCharts());
		dialog.setVisible(true);
	}
	
	public  List<ChartPanel> getChartPanelsofActLoadedChartSet(){
		return ChartPanelAndSteppableServer.getInstance().getChartPanels();
	}
	
	public void modelWasClosed(){
		ChartPanelAndSteppableServer.getInstance().removeAllListeners();
		ChartPanelAndSteppableServer.getInstance().removeAllChartPanels();
		ChartPanelAndSteppableServer.getInstance().removeAllSteppables();
		this.closeActLoadedChartSet();
	}
	
	
	
	public void showEditChartSetDialog(Frame parent){
		ChartSetDialog dialog = new ChartSetDialog(parent, "Episim-Chart-Set", true);
		
		if(this.chartMonitoredTissue != null){ 
			
			EpisimChartSet updatedChartSet =dialog.showChartSet(actLoadedChartSet);
			if(updatedChartSet != null){
				this.actLoadedChartSet = updatedChartSet;
				
			}
		}
	}
	
	public void rebuildDefaultCharts(){
		DefaultCharts.rebuildCharts();
	}
	
	protected void resetToOldDefaultChartSelectionValues(){
		DefaultCharts.getInstance().resetToOldSelectionValues();
	}
	
	protected void registerDefaultChartsAtServer(){
		ChartPanelAndSteppableServer.getInstance().registerDefaultChartPanelsAndSteppables(DefaultCharts.getInstance().getChartPanelsOfActivatedDefaultCharts(), DefaultCharts.getInstance().getSteppablesOfActivatedDefaultCharts());
	}
	
	protected void storeEpisimChartSet(EpisimChartSet chartSet){
		ECSFileWriter fileWriter = new ECSFileWriter(chartSet.getPath());
		fileWriter.createChartSetArchive(chartSet);
		try{
	      loadEpisimChartSet(new File(chartSet.getPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	public void reloadCurrentlyLoadedChartSet(){
	
		try{
	      loadEpisimChartSet(new File(this.actLoadedChartSet.getPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	private boolean loadEpisimChartSet(URL url){
		return loadEpisimChartSet(url, null);
	}
	
	public List<EnhancedSteppable> getChartSteppablesOfActLoadedChartSet(GenericBag<CellType> allCells, Continuous2D continuous, Object[] objects) throws MissingObjectsException{
		return ChartPanelAndSteppableServer.getInstance().getChartSteppables(allCells, continuous, objects );
	}
	
	
	private boolean loadEpisimChartSet(URL url, Frame parent){
		try{
			PNGPrinter.getInstance().reset();
			CalculationController.getInstance().reset();
			ECSFileReader ecsReader = new ECSFileReader(url);
			this.actLoadedChartSet = ecsReader.getEpisimChartSet();
			ChartPanelAndSteppableServer.getInstance().registerCustomChartPanelsAndSteppables(ecsReader.getChartPanels(), ecsReader.getChartSteppables(), ecsReader.getChartSetFactory());
			CompatibilityChecker checker = new CompatibilityChecker();
			checker.checkEpisimChartSetForCompatibility(actLoadedChartSet, this.chartMonitoredTissue);
			return true;
		}
		catch (ModelCompatibilityException e){
			if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Chart-Set!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			ExceptionDisplayer.getInstance().displayException(e);
		}
		return false;
	}
	
	public boolean showNewChartSetDialog(Frame parent){
		ChartSetDialog dialog = new ChartSetDialog(parent, "Episim-Chart-Set", true);
		
		if(this.chartMonitoredTissue != null){ 
			
			EpisimChartSet updatedChartSet =dialog.showNewChartSet();
			if(updatedChartSet != null){ 
				
				this.actLoadedChartSet = updatedChartSet;
				return true;
			}
					
		}
		return false;
	}
	
	public void closeActLoadedChartSet(){
		this.actLoadedChartSet = null;
		ChartPanelAndSteppableServer.getInstance().actLoadedChartSetWasClosed();	
	}
	
	
	
	public void newSimulationRun(){
		CalculationController.getInstance().restartSimulation();
		ChartPanelAndSteppableServer.getInstance().clearAllDefaultChartSeries();
	}
}