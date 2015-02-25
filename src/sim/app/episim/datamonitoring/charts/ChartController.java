package sim.app.episim.datamonitoring.charts;

import java.awt.Component;
import java.awt.Frame;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.jfree.chart.ChartPanel;

import episimexceptions.CompilationFailedException;
import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;
import episimexceptions.PropertyException;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CellColoringConfigurator;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimChartSet;
import episiminterfaces.monitoring.EpisimDiffFieldChart;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.datamonitoring.parser.*;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.datamonitoring.charts.ChartController.ChartType;
import sim.app.episim.datamonitoring.charts.io.ECSFileReader;
import sim.app.episim.datamonitoring.charts.io.ECSFileWriter;
import sim.app.episim.datamonitoring.charts.io.PNGPrinter;
import sim.app.episim.datamonitoring.parser.DataMonitoringExpressionChecker;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.CompatibilityChecker;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.field.continuous.Continuous2D;
public class ChartController implements ClassLoaderChangeListener{
	
	private static ChartController instance = null;
	
	public enum ChartType {
		REGULAR_2D_CHART("2D Chart"),
		CELL_VISUALIZATION_CHART("2D Cell Visualization Chart"),
		DIFF_FIELD_CHART("3D-Diffusion-Field Chart");
		
		private String chartType;
		private ChartType(String type){ this.chartType = type; }
		public String toString(){ return this.chartType;}		
	}
	
	private static Semaphore sem = new Semaphore(1);
	
	
	private long nextChartId = 0;
	
	private TissueType chartMonitoredTissue;
	private static EpisimChartSet actLoadedChartSet;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validDataTypes;
	private Set<Class<?>> validDataTypesCellVisualization;
	private ExtendedFileChooser ecsChooser = null;
	
	private ChartController(){
		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		if(ModeServer.guiMode()) ecsChooser = new ExtendedFileChooser("ecs");
		markerPrefixes = new HashSet<String>();
		validDataTypes = new HashSet<Class<?>>();
		validDataTypesCellVisualization = new HashSet<Class<?>>();
		
		markerPrefixes.add("get");
		markerPrefixes.add("is");
		
		validDataTypes.add(Integer.TYPE);
		validDataTypes.add(Short.TYPE);
		validDataTypes.add(Byte.TYPE);
		validDataTypes.add(Long.TYPE);
		validDataTypes.add(Float.TYPE);
		validDataTypes.add(Double.TYPE);
		validDataTypes.add(Boolean.TYPE);
		validDataTypes.add(EpisimCellType.class);
		validDataTypes.add(EpisimDifferentiationLevel.class);
		
		validDataTypesCellVisualization.add(Integer.TYPE);
		validDataTypesCellVisualization.add(Short.TYPE);
		validDataTypesCellVisualization.add(Byte.TYPE);
		validDataTypesCellVisualization.add(Long.TYPE);
		validDataTypesCellVisualization.add(Float.TYPE);
		validDataTypesCellVisualization.add(Double.TYPE);
	}
	
	public boolean isAlreadyChartSetLoaded(){
		if(this.actLoadedChartSet != null) return true;
		
		return false;
	}
	
	protected long getNextChartId(){
	
		return System.currentTimeMillis() + (this.nextChartId++);
	}
		
	public static ChartController getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new ChartController();				
				sem.release();
         }
         catch (InterruptedException e){
	        EpisimExceptionHandler.getInstance().displayException(e);
         }
				
		}
		return instance;
	}
	   
	public void setChartMonitoredTissue(TissueType tissue){
		this.chartMonitoredTissue = tissue;
	}
	
	protected EpisimChart showChartCreationWizard(Frame parent){
		return showChartCreationWizard(parent, null);
	}
	
	protected EpisimDiffFieldChart showDiffFieldChartCreationWizard(Frame parent){
		return showDiffFieldChartCreationWizard(parent, null);
	}
	
	protected EpisimCellVisualizationChart showCellVisualizationChartCreationWizard(Frame parent){
		return showCellVisualizationChartCreationWizard(parent, null);
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
	
	protected EpisimCellVisualizationChart showCellVisualizationChartCreationWizard(Frame parent, EpisimCellVisualizationChart chart){
		CellVisualizationChartCreationWizard wizard = new CellVisualizationChartCreationWizard(parent, "Chart Creation Wizard", true, 
		new TissueCellDataFieldsInspector(this.chartMonitoredTissue, this.markerPrefixes, this.validDataTypesCellVisualization));
		
		if(this.chartMonitoredTissue != null){
			if(chart == null)wizard.showWizard();
			else wizard.showWizard(chart);
		}			
		return wizard.getEpisimCellVisualizationChart();
	}
	
	protected EpisimDiffFieldChart showDiffFieldChartCreationWizard(Frame parent, EpisimDiffFieldChart chart){
		DiffFieldChartCreationWizard wizard = new DiffFieldChartCreationWizard(parent, "Chart-Creation-Wizard", true);
		
		if(this.chartMonitoredTissue != null){
			if(chart == null)wizard.showWizard();
			else wizard.showWizard(chart);
		}			
		return wizard.getEpisimDiffFieldChart();
	}
	
	public boolean loadChartSet(Component parent){
		ecsChooser.setDialogTitle("Load Episim-Chartset");
		if(ecsChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
			try{
				return loadEpisimChartSet(ecsChooser.getSelectedFile().toURI().toURL(), parent);	
			}
			catch (MalformedURLException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}
		return false;
	}
	
	public void loadChartSet(File file){
		try{
			loadEpisimChartSet(file.toURI().toURL(), null);	
		}
		catch (MalformedURLException e){
			EpisimExceptionHandler.getInstance().displayException(new PropertyException("The Chart-Set " +file.getAbsolutePath()+" specified in the Properties-File cannot be loaded. Detailed Error-Message: "+e.getMessage()));
		}
	}
	
	public File getCurrentlyLoadedChartsetFile(){
		if(this.actLoadedChartSet != null)	return this.actLoadedChartSet.getPath();
		return null;
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
	public  List<JPanel> getDiffusionChartPanelsofActLoadedChartSet(){
		return ChartPanelAndSteppableServer.getInstance().getDiffusionChartPanels();
	}
	
	
	
	public void modelWasClosed(){
		ChartPanelAndSteppableServer.getInstance().removeAllListeners();
		ChartPanelAndSteppableServer.getInstance().removeAllChartPanels();
		ChartPanelAndSteppableServer.getInstance().removeAllSteppables();
		this.closeActLoadedChartSet();
	}
	
	
	
	public void showEditChartSetDialog(Frame parent){
		ChartSetDialog dialog = new ChartSetDialog(parent, "EPISIM-Chart-Set", true);
		
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
	
	protected void storeEpisimChartSet(EpisimChartSet chartSet) throws CompilationFailedException{
		ECSFileWriter fileWriter = new ECSFileWriter(chartSet.getPath());
		
		try{
			fileWriter.createChartSetArchive(chartSet);
	      loadEpisimChartSet(new File(chartSet.getPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      EpisimExceptionHandler.getInstance().displayException(e);
      }
      catch (JAXBException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
	}
	
	public void reloadCurrentlyLoadedChartSet(){
	
		try{
	      loadEpisimChartSet(new File(this.actLoadedChartSet.getPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      EpisimExceptionHandler.getInstance().displayException(e);
      }
	}
	
	private boolean loadEpisimChartSet(URL url){
		return loadEpisimChartSet(url, null);
	}
	
	public List<EnhancedSteppable> getChartSteppablesOfActLoadedChartSet(GenericBag<AbstractCell> allCells, Object[] objects) throws MissingObjectsException{
		return ChartPanelAndSteppableServer.getInstance().getChartSteppables(allCells, objects );
	}
	
	public List<EnhancedSteppable> getPNGWriterSteppablesOfActLoadedChartSet() {
		return ChartPanelAndSteppableServer.getInstance().getPNGWriterSteppables();
	}
	
	
	private boolean loadEpisimChartSet(URL url, Component parent){
		try{
			PNGPrinter.getInstance().reset();
			CalculationController.getInstance().reset();
			ECSFileReader ecsReader = new ECSFileReader(url);
			ChartController.getInstance().actLoadedChartSet = ecsReader.getEpisimChartSet();
			if(ChartController.getInstance().actLoadedChartSet != null){
				ChartController.getInstance().actLoadedChartSet.setPath(new File(url.toURI()));
				if(!ChartController.getInstance().actLoadedChartSet.isOneOfTheChartsDirty() && !ECSFileReader.foundDirtyChartSeriesDuringImport){
					ECSFileReader.foundDirtyChartSeriesDuringImport = false;
					ChartPanelAndSteppableServer.getInstance().registerCustomChartPanelsAndSteppables(ecsReader.getChartPanels(), ecsReader.getDiffusionChartPanels(), ecsReader.getChartSteppables(),ecsReader.getPNGWriterSteppables(), ecsReader.getChartSetFactory());
					CompatibilityChecker checker = new CompatibilityChecker();			
					checker.checkEpisimChartSetForCompatibility(ChartController.getInstance().actLoadedChartSet, ChartController.getInstance().chartMonitoredTissue);
				}
				else{
					ECSFileReader.foundDirtyChartSeriesDuringImport = false;
					for(EpisimChart chart : ChartController.getInstance().actLoadedChartSet.getEpisimCharts()) ChartController.getInstance().updateExpressionsInChart(chart);
					for(EpisimCellVisualizationChart chart : ChartController.getInstance().actLoadedChartSet.getEpisimCellVisualizationCharts()) ChartController.getInstance().updateExpressionsInCellVisualizationChart(chart);
					ChartController.getInstance().resetChartDirtyStatus();
					ChartController.getInstance().storeEpisimChartSet(ChartController.getInstance().actLoadedChartSet);					
				}
				
				return true;
			}
		}
		catch (ModelCompatibilityException e){
			if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Chart-Set!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			EpisimExceptionHandler.getInstance().displayException(e);
			return false;
		}
      catch (URISyntaxException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
			return false;
      }
      catch (CompilationFailedException e){
      	if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Chart-Set!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			EpisimExceptionHandler.getInstance().displayException(e);
			return false;
      }
		return false;
	}
	
	private void updateExpressionsInChart(EpisimChart chart){
		int sessionID = ExpressionCheckerController.getInstance().getCheckSessionId();
		TissueCellDataFieldsInspector inspector = new TissueCellDataFieldsInspector(this.chartMonitoredTissue, this.markerPrefixes, this.validDataTypes);
		CalculationAlgorithmConfigurator config =  chart.getBaselineCalculationAlgorithmConfigurator();
		try{
			if(config != null){
				if(config.getArithmeticExpression()!= null && config.getArithmeticExpression()[0] != null){
					String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, config.getArithmeticExpression()[0], inspector);
					config.getArithmeticExpression()[0] = result[0];
					config.getArithmeticExpression()[1] = result[1];
				}
				if(config.getBooleanExpression()!= null && config.getBooleanExpression()[0] != null){
					String[] result = ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(sessionID, config.getBooleanExpression()[0], inspector);
					config.getBooleanExpression()[0] = result[0];
					config.getBooleanExpression()[1] = result[1];
				}
			}
			for(EpisimChartSeries series : chart.getEpisimChartSeries()){
				sessionID = ExpressionCheckerController.getInstance().getCheckSessionId();
				config = series.getCalculationAlgorithmConfigurator();
				if(config != null){
					if(config.getArithmeticExpression()!= null && config.getArithmeticExpression()[0] != null){
						String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, config.getArithmeticExpression()[0], inspector);
						config.getArithmeticExpression()[0] = result[0];
						config.getArithmeticExpression()[1] = result[1];
					}
					if(config.getBooleanExpression()!= null && config.getBooleanExpression()[0] != null){
						String[] result = ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(sessionID, config.getBooleanExpression()[0], inspector);
						config.getBooleanExpression()[0] = result[0];
						config.getBooleanExpression()[1] = result[1];
					}
				}
			}
			
		}
		catch(ParseException e){
			EpisimExceptionHandler.getInstance().displayException(e);
		}
	}
	
	private void updateExpressionsInCellVisualizationChart(EpisimCellVisualizationChart chart){
		int sessionID = ExpressionCheckerController.getInstance().getCheckSessionId();
		TissueCellDataFieldsInspector inspector = new TissueCellDataFieldsInspector(this.chartMonitoredTissue, this.markerPrefixes, this.validDataTypesCellVisualization);
		CellColoringConfigurator config =  chart.getCellColoringConfigurator();
		try{
			if(config != null){
				if(config.getArithmeticExpressionColorR()!= null && config.getArithmeticExpressionColorR()[0] != null){
					String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, config.getArithmeticExpressionColorR()[0], inspector);
					config.getArithmeticExpressionColorR()[0] = result[0];
					config.getArithmeticExpressionColorR()[1] = result[1];
				}
				if(config.getArithmeticExpressionColorG()!= null && config.getArithmeticExpressionColorG()[0] != null){
					String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, config.getArithmeticExpressionColorG()[0], inspector);
					config.getArithmeticExpressionColorG()[0] = result[0];
					config.getArithmeticExpressionColorG()[1] = result[1];
				}
				if(config.getArithmeticExpressionColorB()!= null && config.getArithmeticExpressionColorB()[0] != null){
					String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, config.getArithmeticExpressionColorB()[0], inspector);
					config.getArithmeticExpressionColorB()[0] = result[0];
					config.getArithmeticExpressionColorB()[1] = result[1];
				}				
			}			
		}
		catch(ParseException e){
			EpisimExceptionHandler.getInstance().displayException(e);
		}
	}
	
	
	
	private void resetChartDirtyStatus(){
		 if(this.actLoadedChartSet != null){
		   for(EpisimChart actChart: this.actLoadedChartSet.getEpisimCharts()) {
		   	actChart.setIsDirty(false);
		   }
		   for(EpisimCellVisualizationChart actChart: this.actLoadedChartSet.getEpisimCellVisualizationCharts()) {
		   	actChart.setIsDirty(false);
		   }
		   for(EpisimDiffFieldChart actChart: this.actLoadedChartSet.getEpisimDiffFieldCharts()) {
		   	actChart.setIsDirty(false);
		   }
		 }
	}
	
	public boolean showNewChartSetDialog(Frame parent){
		ChartSetDialog dialog = new ChartSetDialog(parent, "EPISIM-Chart-Set", true);
		
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
	
   public void classLoaderHasChanged() {
		instance = null;
   }
}