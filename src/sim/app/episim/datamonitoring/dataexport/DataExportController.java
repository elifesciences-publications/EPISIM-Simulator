package sim.app.episim.datamonitoring.dataexport;

import java.awt.Frame;
import java.io.File;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.bind.JAXBException;

import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;



import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartPanelAndSteppableServer;
import sim.app.episim.datamonitoring.charts.ChartSetChangeListener;
import sim.app.episim.datamonitoring.dataexport.io.EDEFileReader;
import sim.app.episim.datamonitoring.dataexport.io.EDEFileWriter;
import sim.app.episim.datamonitoring.parser.ParseException;

import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.CompatibilityChecker;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.field.continuous.Continuous2D;

import episimexceptions.CompilationFailedException;
import episimexceptions.MissingObjectsException;
import episimexceptions.ModelCompatibilityException;
import episimexceptions.PropertyException;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;
import episiminterfaces.monitoring.EpisimDiffFieldDataExport;




public class DataExportController implements ClassLoaderChangeListener{
	
	public enum DataExportType {
		REGULAR_DATA_EXPORT("Regular Data Export"),
		DIFF_FIELD_DATA_EXPORT("Diffusion-Field Data Export");
		
		private String dataExportType;
		private DataExportType(String type){ this.dataExportType = type; }
		public String toString(){ return this.dataExportType;}		
	}
	
	
	private static DataExportController instance = null;
	
	
	
	private long nextDataExportId = 0;
	
	private TissueType dataExportMonitoredTissue;
	private EpisimDataExportDefinitionSet actLoadedDataExportSet;
	private Set<String> markerPrefixes;
	private Set<Class<?>> validDataTypes;
	private ExtendedFileChooser edeChooser = null;
	
	private DataExportController(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		if(ModeServer.guiMode()) edeChooser = new ExtendedFileChooser("ede");
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
		validDataTypes.add(EpisimCellType.class);
		validDataTypes.add(EpisimDifferentiationLevel.class);
		
	}
	
	public boolean isAlreadyDataExportSetLoaded(){
		if(this.actLoadedDataExportSet != null) return true;
		
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
	
	protected EpisimDataExportDefinitionSet showDataExportSetDialog(Frame parent){
		return showDataExportSetDialog(parent, null);
	}
	protected EpisimDataExportDefinitionSet showDataExportSetDialog(Frame parent, EpisimDataExportDefinitionSet dataExportSet){
		
		DataExportDefinitionSetDialog dialog = new DataExportDefinitionSetDialog(parent, "EPISIM Data-Export-Set", true);		
		if(this.dataExportMonitoredTissue != null){
			if(dataExportSet == null) return dialog.showNewDataExportDefinitionSet();
			else return dialog.showDataExportDefinitionSet(dataExportSet);
		}			
		return null;
		
	}
	
	public String getActLoadedDataExportsName(){
		if(actLoadedDataExportSet == null){
			return "";
		}
		else return actLoadedDataExportSet.getName();
	}
	
	public EpisimDataExportDefinition showDataExportCreationWizard(Frame parent){
		return showDataExportCreationWizard(parent, null);
	}
	public EpisimDiffFieldDataExport showDiffFieldDataExportCreationWizard(Frame parent){
		return showDiffFieldDataExportCreationWizard(parent, null);
	}
	
	public EpisimDataExportDefinition showDataExportCreationWizard(Frame parent, EpisimDataExportDefinition exportDefinition){
		
		DataExportCreationWizard creationWizard = new DataExportCreationWizard(parent, "EPISIM Data-Export", true, 
				new TissueCellDataFieldsInspector(this.dataExportMonitoredTissue, this.markerPrefixes, this.validDataTypes));
		
		if(exportDefinition == null) creationWizard.showWizard();
		
		else creationWizard.showWizard(exportDefinition);
		
		return creationWizard.getEpisimDataExport();
	}
	
	public EpisimDiffFieldDataExport showDiffFieldDataExportCreationWizard(Frame parent, EpisimDiffFieldDataExport exportDefinition){
		
		DiffFieldDataExportCreationWizard creationWizard = new DiffFieldDataExportCreationWizard(parent, " EPISIM Data-Export", true);
		
		if(exportDefinition == null) creationWizard.showWizard();
		
		else creationWizard.showWizard(exportDefinition);
		
		return creationWizard.getEpisimDiffFieldDataExport();
	}
	
	public boolean loadDataExportDefinition(Frame parent){
		edeChooser.setDialogTitle("Load Already Defined Data Export Set");
		if(edeChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION){
			try{
				return loadDataExportDefinition(edeChooser.getSelectedFile().toURI().toURL(), parent);	
			}
			catch (MalformedURLException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		return false;
	}
	
	public void loadDataExportDefinition(File file){		
		try{
			loadDataExportDefinition(file.toURI().toURL(), null);	
		}
		catch (MalformedURLException e){
			ExceptionDisplayer.getInstance().displayException(new PropertyException("The Data-Export-Definition " +file.getAbsolutePath()+" specified in the Properties-File cannot be loaded. Detailed Error-Message: "+e.getMessage()));
		}		
	}
	
	
	
	public void modelWasClosed(){
		DataExportSteppableServer.getInstance().removeAllSteppables();
		DataExportSteppableServer.getInstance().removeAllListeners();
		DataExportSteppableServer.getInstance().removeAllDataExports();
		this.closeActLoadedDataExportDefinitonSet();
	}
	
	public boolean showNewDataExportDefinitionSetDialog(Frame parent){
		
		
		if(this.dataExportMonitoredTissue != null){ 
			
			EpisimDataExportDefinitionSet updatedDataExport =showDataExportSetDialog(parent);
			if(updatedDataExport != null){ 
				this.actLoadedDataExportSet = updatedDataExport;
				return true;
			}
					
		}
		return false;
	}
	
	public void showEditDataExportDefinitionDialog(Frame parent){
		
		if(this.dataExportMonitoredTissue != null && this.actLoadedDataExportSet != null){ 
			
			EpisimDataExportDefinitionSet updatedDataExportSet =showDataExportSetDialog(parent, this.actLoadedDataExportSet);
			if(updatedDataExportSet != null){ 
				this.actLoadedDataExportSet = updatedDataExportSet;
			}		
		}
	}
	
	
	public List<EnhancedSteppable> getDataExportSteppablesOfActLoadedDataExport(GenericBag<AbstractCell> allCells, Object[] objects) throws MissingObjectsException{
		return DataExportSteppableServer.getInstance().getDataExportSteppables(allCells, objects );
	}
	
	public void registerDataExportChangeListener(DataExportChangeListener changeListener){
		DataExportSteppableServer.getInstance().registerDataExportChangeListener(changeListener);
	   //	closeActLoadedDataExportDefinitonSet();
	}
	
	
	
	
	protected void storeDataExportDefinitionSet(EpisimDataExportDefinitionSet dataExportSet) throws CompilationFailedException{
		EDEFileWriter fileWriter = new EDEFileWriter(dataExportSet.getPath());
		
		try{
			fileWriter.createDataExportDefinitionSetArchive(dataExportSet);
	      loadDataExportDefinitionSet(new File(dataExportSet.getPath().getAbsolutePath()).toURI().toURL());
	    
      }
      catch (MalformedURLException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      }
      catch (JAXBException e){
      	  ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	private boolean loadDataExportDefinitionSet(URL url){
		return loadDataExportDefinition(url, null);
	}
	
	public void reloadCurrentlyLoadedDataExportDefinitionSet(){
		
		try{
			loadDataExportDefinitionSet(new File(this.actLoadedDataExportSet.getPath().getAbsolutePath()).toURI().toURL());
      }
      catch (MalformedURLException e){
	      ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	

	private boolean loadDataExportDefinition(URL url, Frame parent){
		try{
			CalculationController.getInstance().reset();
			EDEFileReader edeReader = new EDEFileReader(url);
			DataExportController.getInstance().actLoadedDataExportSet = edeReader.getEpisimDataExportDefinitionSet();
			if(DataExportController.getInstance().actLoadedDataExportSet != null){
				DataExportController.getInstance().actLoadedDataExportSet.setPath(new File(url.toURI()));
				if(!DataExportController.getInstance().actLoadedDataExportSet.isOneOfTheDataExportDefinitionsDirty() && !EDEFileReader.foundDirtyDataExportColumnDuringImport){
					EDEFileReader.foundDirtyDataExportColumnDuringImport = false;
					DataExportSteppableServer.getInstance().registerCustomDataExportSteppables(edeReader.getDataExports(), edeReader.getDiffusionFieldDataExports(), edeReader.getDataExportSteppables(), edeReader.getDataExportFactory());
					CompatibilityChecker checker = new CompatibilityChecker();
					checker.checkEpisimDataExportDefinitionSetForCompatibility(DataExportController.getInstance().actLoadedDataExportSet, DataExportController.getInstance().dataExportMonitoredTissue);
				}else{
					EDEFileReader.foundDirtyDataExportColumnDuringImport = false;
					for(EpisimDataExportDefinition def : DataExportController.getInstance().actLoadedDataExportSet.getEpisimDataExportDefinitions()) DataExportController.getInstance().updateExpressionsInDataExportDefinition(def);
					DataExportController.getInstance().resetChartDirtyDataExports();
					DataExportController.getInstance().storeDataExportDefinitionSet(DataExportController.getInstance().actLoadedDataExportSet);
				}
				
				return true;
			}
		}
		catch (ModelCompatibilityException e){
			if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Data-Export Definition!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			ExceptionDisplayer.getInstance().displayException(e);
			return false;
		}
      catch (URISyntaxException e){
      	ExceptionDisplayer.getInstance().displayException(e);
			return false;
      }
      catch (CompilationFailedException e){
      	if(parent != null) JOptionPane.showMessageDialog(parent, "The currently loaded Cell-Diff-Model ist not compatible with this Data-Export Definition!", "Incompatibility Error", JOptionPane.ERROR_MESSAGE);
			ExceptionDisplayer.getInstance().displayException(e);
			return false;
      }
		return false;
	}
	private void updateExpressionsInDataExportDefinition(EpisimDataExportDefinition definition){
		int sessionID = 0;
		TissueCellDataFieldsInspector inspector = new TissueCellDataFieldsInspector(this.dataExportMonitoredTissue, this.markerPrefixes, this.validDataTypes);
		CalculationAlgorithmConfigurator config =  null;
		try{
			for(EpisimDataExportColumn column : definition.getEpisimDataExportColumns()){
				sessionID = ExpressionCheckerController.getInstance().getCheckSessionId();
				config = column.getCalculationAlgorithmConfigurator();
				if(config.getArithmeticExpression()!= null && config.getArithmeticExpression()[0] != null){
					config.getArithmeticExpression()[1] = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, config.getArithmeticExpression()[0], inspector);
				}
				if(config.getBooleanExpression()!= null && config.getBooleanExpression()[0] != null){
					config.getBooleanExpression()[1] = ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(sessionID, config.getBooleanExpression()[0], inspector);
				}
			}
			
		}
		catch(ParseException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
	}
	
	private void resetChartDirtyDataExports(){
	  for(EpisimDataExportDefinition actDef: this.actLoadedDataExportSet.getEpisimDataExportDefinitions()) {
		  actDef.setIsDirty(false);
	  }
	  for(EpisimDiffFieldDataExport actDef: this.actLoadedDataExportSet.getEpisimDiffFieldDataExportDefinitions()) {
		  actDef.setIsDirty(false);
	  }
	}
	
	public void newSimulationRun(){
		CalculationController.getInstance().restartSimulation();
		DataExportSteppableServer.getInstance().newSimulationRun();
	}
	
	public void simulationWasStopped(){
		
		DataExportSteppableServer.getInstance().simulationWasStopped();
	}
	
	
	
	public void closeActLoadedDataExportDefinitonSet(){
		this.actLoadedDataExportSet = null;
		DataExportSteppableServer.getInstance().actLoadedDataExportDefinitionSetWasClosed();
	}


   public void classLoaderHasChanged() {
   	instance = null;	   
   }
	
	
	
	
}