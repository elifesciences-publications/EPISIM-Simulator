package sim.app.episim.datamonitoring.dataexport;

import java.awt.Frame;
import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;

import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import sim.app.episim.ExceptionDisplayer;



import sim.app.episim.datamonitoring.dataexport.io.EDEFileReader;
import sim.app.episim.datamonitoring.dataexport.io.EDEFileWriter;

import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.CompatibilityChecker;
import sim.app.episim.util.TissueCellDataFieldsInspector;

import episimexceptions.ModelCompatibilityException;
import episiminterfaces.EpisimDataExportDefinition;




public class DataExportController {
	
	private static DataExportController instance = null;
	
	
	
	private long nextDataExportId = 0;
	
	private TissueType dataExportMonitoredTissue;
	private EpisimDataExportDefinition actLoadedDataExport;
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
	
	protected EpisimDataExportDefinition showDataExportCreationWizard(Frame parent){
		return showDataExportCreationWizard(parent, null);
	}
	protected EpisimDataExportDefinition showDataExportCreationWizard(Frame parent, EpisimDataExportDefinition dataExport){
		DataExportCreationWizard wizard = new DataExportCreationWizard(parent, "Data-Export-Creation-Wizard", true, 
		new TissueCellDataFieldsInspector(this.dataExportMonitoredTissue, this.markerPrefixes, this.validDataTypes));
		
		if(this.dataExportMonitoredTissue != null){
			if(dataExport == null)wizard.showWizard();
			else wizard.showWizard(dataExport);
		}
			
		return wizard.getEpisimDataExport();	
	}
	
	public boolean loadDataExportDefinition(Frame parent){
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
		
		this.closeActLoadedDataExportDefiniton();
	}
	
	public boolean showNewChartSetDialog(Frame parent){
		
		
		if(this.dataExportMonitoredTissue != null){ 
			
			EpisimDataExportDefinition updatedDataExport =showDataExportCreationWizard(parent);
			if(updatedDataExport != null){ 
				this.actLoadedDataExport = updatedDataExport;
				return true;
			}
					
		}
		return false;
	}
	
	public void showEditDataExportDefinitionDialog(Frame parent){
		
		if(this.dataExportMonitoredTissue != null && this.actLoadedDataExport != null){ 
			
			EpisimDataExportDefinition updatedDataExport =showDataExportCreationWizard(parent, this.actLoadedDataExport);
			if(updatedDataExport != null){ 
				this.actLoadedDataExport = updatedDataExport;
			}		
		}
	}
	
	
	protected void storeDataExportDefinition(EpisimDataExportDefinition dataExport){
		EDEFileWriter fileWriter = new EDEFileWriter(dataExport.getDataExportDefinitionPath());
		fileWriter.createDataExportDefinitionArchive(dataExport);
		try{
	      loadEpisimChartSet(new File(dataExport.getDataExportDefinitionPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	private boolean loadEpisimChartSet(URL url){
		return loadEpisimChartSet(url, null);
	}

	private boolean loadEpisimChartSet(URL url, Frame parent){
		try{
			EDEFileReader ecsReader = new EDEFileReader(url);
			this.actLoadedDataExport = ecsReader.getEpisimDataExportDefinition();
			
			CompatibilityChecker checker = new CompatibilityChecker();
			checker.checkEpisimDataExportDefinitionForCompatibility(actLoadedDataExport, this.dataExportMonitoredTissue);
			return true;
		}
		catch (ModelCompatibilityException e){
			if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Data-Export Definition!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			ExceptionDisplayer.getInstance().displayException(e);
		}
		return false;
	}
	
	
	
	public void closeActLoadedDataExportDefiniton(){
		this.actLoadedDataExport = null;
	}
	
	
	
	
}