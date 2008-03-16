package sim.app.episim.datamonitoring.dataexport;

import java.awt.Frame;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jfree.chart.ChartPanel;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.CompatibilityChecker;

import sim.app.episim.datamonitoring.charts.ChartCreationWizard;
import sim.app.episim.datamonitoring.charts.ChartPanelAndSteppableServer;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.charts.ChartSetDialog;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.charts.io.ECSFileReader;
import sim.app.episim.datamonitoring.charts.io.ECSFileWriter;
import sim.app.episim.datamonitoring.parser.DataMonitoringExpressionChecker;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;
import episiminterfaces.EpisimDataExport;




public class DataExportController {
	
	private static DataExportController instance = null;
	
	
	
	private long nextDataExportId = 0;
	
	private TissueType dataExportMonitoredTissue;
	private EpisimDataExport actLoadedDataExport;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validDataTypes;
	private ExtendedFileChooser edeChooser = new ExtendedFileChooser("ede");
	private DataExportController(){
		
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
		
	}
	
	public boolean isAlreadyDataExportLoaded(){
		if(this.actLoadedDataExport != null) return true;
		
		return false;
	}
	
	protected long getNextDataExportId(){
	
		return System.currentTimeMillis() + (this.nextDataExportId++);
	}
		
	public synchronized static DataExportController getInstance(){
		if(instance == null) instance = new DataExportController();
		
		return instance;
	}
	   
	public void setDataExportMonitoredTissue(TissueType tissue){
		this.dataExportMonitoredTissue = tissue;
	}
	
	protected EpisimDataExport showDataExportCreationWizard(Frame parent){
		return showDataExportCreationWizard(parent, null);
	}
	protected EpisimDataExport showDataExportCreationWizard(Frame parent, EpisimDataExport dataExport){
		DataExportCreationWizard wizard = new DataExportCreationWizard(parent, "Data-Export-Creation-Wizard", true, 
		new TissueCellDataFieldsInspector(this.dataExportMonitoredTissue, this.markerPrefixes, this.validDataTypes));
		
		if(this.dataExportMonitoredTissue != null){
			if(dataExport == null)wizard.showWizard();
			else wizard.showWizard(dataExport);
		}
			
		return wizard.getEpisimDataExport();	
	}
	
	public boolean loadChartSet(Frame parent){
		edeChooser.setDialogTitle("Load Already Defined DataExport");
		if(edeChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
			try{
				return loadEpisimChartSet(edeChooser.getSelectedFile().toURI().toURL(), parent);	
			}
			catch (MalformedURLException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		return false;
	}
	
	
	
	public void modelWasClosed(){
		//
	}
	
	public boolean showNewChartSetDialog(Frame parent){
		
		
		if(this.dataExportMonitoredTissue != null){ 
			
			EpisimDataExport updatedDataExport =showDataExportCreationWizard(parent);
			if(updatedDataExport != null){ 
				this.actLoadedDataExport = updatedDataExport;
				return true;
			}
					
		}
		return false;
	}
	/*
	public void showEditDataExportDialog(Frame parent){
		ChartSetDialog dialog = new ChartSetDialog(parent, "Data-Export", true);
		
		if(this.dataExportMonitoredTissue != null){ 
			
			EpisimChartSet updatedChartSet =dialog.showChartSet(actLoadedDataExport);
			if(updatedChartSet != null){ 
				this.actLoadedDataExport = updatedChartSet;
				
			}
		}
	}*/
	/*
	protected void storeDataExport(EpisimChartSet chartSet){
		ECSFileWriter fileWriter = new ECSFileWriter(chartSet.getPath());
		fileWriter.createChartSetArchive(chartSet);
		try{
	      loadEpisimChartSet(new File(chartSet.getPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	private boolean loadEpisimChartSet(URL url){
		return loadEpisimChartSet(url, null);
	}
	*/
	private boolean loadEpisimChartSet(URL url, Frame parent){
	//	try{
		/*	ECSFileReader ecsReader = new ECSFileReader(url);
			this.actLoadedDataExport = ecsReader.getEpisimChartSet();
			ChartPanelAndSteppableServer.getInstance().registerChartPanels(ecsReader.getChartPanels());
			CompatibilityChecker checker = new CompatibilityChecker();
			checker.checkEpisimChartSetForCompatibility(actLoadedDataExport, this.dataExportMonitoredTissue);*/
			return true;
		//}
		/*catch (ModelCompatibilityException e){
			if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Chart-Set!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			ExceptionDisplayer.getInstance().displayException(e);
		}
		return false;*/
	}
	
	
	
	public void closeActLoadedChartSet(){
		this.actLoadedDataExport = null;
	}
	
	protected String checkChartExpression(String expression, TissueCellDataFieldsInspector tissueDataFieldsInspector) throws ParseException,TokenMgrError{
		
		String result = "";
		
	   if(expression != null && tissueDataFieldsInspector != null){
		 StringReader sr = new java.io.StringReader(expression);
	    Reader r = new java.io.BufferedReader( sr );
	    DataMonitoringExpressionChecker parser = new DataMonitoringExpressionChecker(r);
	    result = parser.check(tissueDataFieldsInspector);
	    
	   }
		return result;
	}
	
	
}