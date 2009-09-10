package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import binloc.ProjectLocator;

import episimexceptions.ModelCompatibilityException;

import sim.SimStateServer;
import sim.app.episim.CompileWizard;
import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.TissueServer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartPanelAndSteppableServer;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;

import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.ModelController;
import sim.app.episim.snapshot.SnapshotLoader;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotReader;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculculationRegistry;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.engine.Schedule;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpidermisSimulator extends JFrame implements SimulationStateChangeListener, ClassLoaderChangeListener, SnapshotRestartListener{
	
	public static final String versionID = "1.1.1";
	
	private ExtendedFileChooser jarFileChoose;
	private ExtendedFileChooser tssFileChoose;
	
	
	
	private EpidermisGUIState epiUI;
	
	
	
	private JMenu fileMenu;
	private JMenuItem menuItemSetSnapshotPath;
	private JMenuItem menuItemLoadSnapshot;
	private JMenuItem menuItemOpen;
	private JMenuItem menuItemClose;
	private JMenuItem menuItemBuild;
	
	private JMenu chartMenu;
	private JMenuItem menuItemEditChartSet;
	private JMenuItem menuItemLoadChartSet;
	private JMenuItem menuItemNewChartSet;
	private JMenuItem menuItemCloseChartSet;
	private JMenuItem menuItemSelectDefaultCharts;
	
	private JMenu dataExportMenu;
	private JMenuItem menuItemEditDataExport;
	private JMenuItem menuItemLoadDataExport;
	private JMenuItem menuItemNewDataExport;
	private JMenuItem menuItemCloseDataExport;
	
	private JMenu infoMenu;
	private JMenuItem menuItemAboutMason;
	
	private StatusBar statusbar;
	
	private File actLoadedJarFile = null;
	private File actLoadedSnapshotFile = null;
	
	public EpidermisSimulator() {
		ExceptionDisplayer.getInstance().registerParentComp(this);
		
		statusbar = new StatusBar();
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
		}
		final EpidermisSimulator simulator = this;
		//--------------------------------------------------------------------------------------------------------------
		//Menü
		//--------------------------------------------------------------------------------------------------------------
		JMenuBar  menuBar = new JMenuBar();
		//--------------------------------------------------------------------------------------------------------------
		//Menü File
		//--------------------------------------------------------------------------------------------------------------
		this.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
		fileMenu = new JMenu("File");
		menuItemOpen = new JMenuItem("Open Episim-Cell-Model");
		menuItemOpen.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(ModelController.getInstance().isModelOpened()){
					int choice = JOptionPane.showConfirmDialog(simulator, "Do you really want to close the opened model?", "Close Model?", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.OK_OPTION){
						closeModel();
						openModel();
					}
				}
				else openModel();
				
			}
			
		});
		
		menuItemClose = new JMenuItem("Close Episim-Cell-Model");
		menuItemClose.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				if(ModelController.getInstance().isModelOpened()) closeModel();
				
			}
			
		});
		menuItemClose.setEnabled(false);
		menuItemSetSnapshotPath = new JMenuItem("Set Tissue-Snaphot-Path");
		menuItemSetSnapshotPath.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				setSnapshotPath();
				
			}
			
		});
	menuItemLoadSnapshot = new JMenuItem("Load Tissue-Snaphot");
		menuItemLoadSnapshot.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				loadSnapshot();
				
			}
			
		});
		menuItemBuild = new JMenuItem("Build Episim-Model-Archive");
		menuItemBuild.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				buildModelArchive();
				
			}
			
		});
		
		menuItemLoadSnapshot.setEnabled(true);
		
		fileMenu.add(menuItemOpen);
		fileMenu.add(menuItemSetSnapshotPath);
		fileMenu.add(menuItemLoadSnapshot);
		//fileMenu.addSeparator();
		//fileMenu.add(menuItemBuild);
		fileMenu.addSeparator();
		fileMenu.add(menuItemClose);
		
		menuBar.add(fileMenu);
		
		//--------------------------------------------------------------------------------------------------------------
		// Menü Charts
		//--------------------------------------------------------------------------------------------------------------
		
		chartMenu = new JMenu("Charting");
		chartMenu.setEnabled(false);
		
		menuItemNewChartSet = new JMenuItem("New Chart-Set");
		menuItemNewChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = ChartController.getInstance().showNewChartSetDialog(simulator);
				if(success){
					menuItemEditChartSet.setEnabled(true);
					menuItemCloseChartSet.setEnabled(true);
					menuItemNewChartSet.setEnabled(false);
					menuItemLoadChartSet.setEnabled(false);
				}
				
			}
			
		});
		
		menuItemLoadChartSet = new JMenuItem("Load Chart-Set");
		menuItemLoadChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = ChartController.getInstance().loadChartSet(simulator);
				
				if(success){ 
					ChartController.getInstance().showEditChartSetDialog(simulator);
					menuItemEditChartSet.setEnabled(true);
					menuItemCloseChartSet.setEnabled(true);
					menuItemNewChartSet.setEnabled(false);
					menuItemLoadChartSet.setEnabled(false);
				}
				else{ 
					if(!ChartController.getInstance().isAlreadyChartSetLoaded()) menuItemEditChartSet.setEnabled(false);
				}
			}
			
		});
		
		menuItemEditChartSet = new JMenuItem("Edit Chart-Set");
		menuItemEditChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ChartController.getInstance().showEditChartSetDialog(simulator);
			}
			
		});
		menuItemEditChartSet.setEnabled(false);
		
		menuItemCloseChartSet = new JMenuItem("Close Chart-Set");
		menuItemCloseChartSet.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				menuItemNewChartSet.setEnabled(true);
				menuItemLoadChartSet.setEnabled(true);
				menuItemCloseChartSet.setEnabled(false);
				menuItemEditChartSet.setEnabled(false);
				ChartController.getInstance().closeActLoadedChartSet();
				epiUI.removeAllChartInternalFrames();
			}
			
		});
		menuItemCloseChartSet.setEnabled(false);
		
		menuItemSelectDefaultCharts = new JMenuItem("Select Episim Default-Charts");
		menuItemSelectDefaultCharts.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				ChartController.getInstance().showDefaultChartsSelectionDialog(simulator);
				
			}
			
		});
		menuItemSelectDefaultCharts.setEnabled(true);
		
		chartMenu.add(menuItemNewChartSet);
		chartMenu.add(menuItemLoadChartSet);
		chartMenu.add(menuItemEditChartSet);
		chartMenu.addSeparator();
		chartMenu.add(menuItemCloseChartSet);
		chartMenu.addSeparator();
		chartMenu.add(menuItemSelectDefaultCharts);
		menuBar.add(chartMenu);
		
		//--------------------------------------------------------------------------------------------------------------
		// Menü DataExport
		//--------------------------------------------------------------------------------------------------------------
		
		dataExportMenu = new JMenu("Data Export");
		dataExportMenu.setEnabled(false);
		
		menuItemNewDataExport = new JMenuItem("New Data-Export-Definition-Set");
		menuItemNewDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				success = DataExportController.getInstance().showNewDataExportDefinitionSetDialog(simulator);
				if(success){
					menuItemEditDataExport.setEnabled(true);
					menuItemCloseDataExport.setEnabled(true);
					menuItemNewDataExport.setEnabled(false);
					menuItemLoadDataExport.setEnabled(false);
					statusbar.setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
				}
				
			}
			
		});
		
		menuItemLoadDataExport = new JMenuItem("Load Data-Export-Definition-Set");
		menuItemLoadDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = DataExportController.getInstance().loadDataExportDefinition(simulator);
				
				if(success){ 
					DataExportController.getInstance().showEditDataExportDefinitionDialog(simulator);
					menuItemEditDataExport.setEnabled(true);
					menuItemCloseDataExport.setEnabled(true);
					menuItemNewDataExport.setEnabled(false);
					menuItemLoadDataExport.setEnabled(false);
					statusbar.setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
				}
				else{ 
					if(!DataExportController.getInstance().isAlreadyDataExportSetLoaded()) menuItemEditDataExport.setEnabled(false);
				}
			}
			
		});
		
		menuItemEditDataExport = new JMenuItem("Edit Loaded Data-Export-Definition-Set");
		menuItemEditDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				DataExportController.getInstance().showEditDataExportDefinitionDialog(simulator);
				statusbar.setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
			}
			
		});
		menuItemEditDataExport.setEnabled(false);
		
		menuItemCloseDataExport = new JMenuItem("Close Loaded Data-Export-Definition-Set");
		menuItemCloseDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				menuItemNewDataExport.setEnabled(true);
				menuItemLoadDataExport.setEnabled(true);
				menuItemCloseDataExport.setEnabled(false);
				menuItemEditDataExport.setEnabled(false);
				DataExportController.getInstance().closeActLoadedDataExportDefinitonSet();
				statusbar.setMessage("");
			}
			
		});
		menuItemCloseDataExport.setEnabled(false);
		
		dataExportMenu.add(menuItemNewDataExport);
		dataExportMenu.add(menuItemLoadDataExport);
		dataExportMenu.add(menuItemEditDataExport);
		dataExportMenu.addSeparator();
		dataExportMenu.add(menuItemCloseDataExport);
		menuBar.add(dataExportMenu);
		
		
		//--------------------------------------------------------------------------------------------------------------
		
	
		//--------------------------------------------------------------------------------------------------------------
		// Menü Info
		//--------------------------------------------------------------------------------------------------------------
		
		infoMenu = new JMenu("Info");
		
		menuItemAboutMason = new JMenuItem("About MASON");
		
		menuItemAboutMason.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ConsoleHack.showAbout();
			}
			
		});
		
		infoMenu.add(menuItemAboutMason);
		menuBar.add(infoMenu);
		
		
		//--------------------------------------------------------------------------------------------------------------
		
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().setBackground(Color.LIGHT_GRAY);
		this.setJMenuBar(menuBar);
		
		
		this.getContentPane().add(statusbar, BorderLayout.SOUTH);
		
		jarFileChoose= new ExtendedFileChooser("jar");
		jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
		tssFileChoose = new ExtendedFileChooser("tss");
		
		this.setTitle("Epidermis Simulator");
		
		
		//TODO: to be changed for video recording
		
		this.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
				(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30));
		
	//	this.setPreferredSize(new Dimension(1280, 932));
		
		
		this.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				if(epiUI != null){
					epiUI.closeConsole();
					System.exit(0);
				}
				else System.exit(0);

			}
		});
		this.pack();
		this.setVisible(true);
	}
	
	public static void main(String[] args){
		EpidermisSimulator episim = new EpidermisSimulator();		
	}
	
	
	
	
	private void openModel(){
		ModelController.getInstance().setSimulationStartedOnce(false);
		TissueController.getInstance().getTissueBorder().loadStandardMebrane();
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		File file = null;
		File standartDir =new File("d:/");
		jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
		if(standartDir.exists())jarFileChoose.setCurrentDirectory(standartDir);
		if(jarFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			file = jarFileChoose.getSelectedFile();

			boolean success = false; 
			try{
	         success= ModelController.getInstance().getBioChemicalModelController().loadModelFile(file);
         }
         catch (ModelCompatibilityException e){
	        ExceptionDisplayer.getInstance().displayException(e);
	        JOptionPane.showMessageDialog(this, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
	        success = false;
         }
			
			if(success && SnapshotWriter.getInstance().getSnapshotPath() == null){
				JOptionPane.showMessageDialog(this, "Please specify snapshot path.", "Info", JOptionPane.INFORMATION_MESSAGE);
				setSnapshotPath();
				if(SnapshotWriter.getInstance().getSnapshotPath() == null)success = false;
			}
			//System.out.println(success);
			if(success){
				ChartController.getInstance().rebuildDefaultCharts();
				cleanUpContentPane();
				epiUI = new EpidermisGUIState(this);
				registerSimulationStateListeners(epiUI);
				this.validate();
				this.repaint();
				ModelController.getInstance().setModelOpened(true);
				menuItemSetSnapshotPath.setEnabled(true);
				menuItemLoadSnapshot.setEnabled(false);
				menuItemBuild.setEnabled(false);
				menuItemClose.setEnabled(true);
				chartMenu.setEnabled(true);
				dataExportMenu.setEnabled(true);
				this.actLoadedJarFile = file;
			}

		}
		
	}
	private void reloadModel(File modelFile, File snapshotPath){
		TissueController.getInstance().getTissueBorder().loadStandardMebrane();
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		boolean success = false; 
		try{
         success= ModelController.getInstance().getBioChemicalModelController().loadModelFile(modelFile);
      }
      catch(ModelCompatibilityException e){
        ExceptionDisplayer.getInstance().displayException(e);
        JOptionPane.showMessageDialog(this, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
        success = false;
      }
		
		if(success){
			
			setSnapshotPath(snapshotPath);
			
			
		//	System.out.println("Already Data Export Loaded: " + DataExportController.getInstance().isAlreadyDataExportSetLoaded());
			ChartController.getInstance().rebuildDefaultCharts();
			cleanUpContentPane();
			epiUI = new EpidermisGUIState(this);
			registerSimulationStateListeners(epiUI);
			if(ChartController.getInstance().isAlreadyChartSetLoaded() && GlobalClassLoader.getInstance().getMode().equals(GlobalClassLoader.IGNORECHARTSETMODE)){
				ChartController.getInstance().reloadCurrentlyLoadedChartSet();
				GlobalClassLoader.getInstance().resetMode();
			}
			if(DataExportController.getInstance().isAlreadyDataExportSetLoaded() && GlobalClassLoader.getInstance().getMode().equals(GlobalClassLoader.IGNOREDATAEXPORTMODE)){
				DataExportController.getInstance().reloadCurrentlyLoadedDataExportDefinitionSet();
				GlobalClassLoader.getInstance().resetMode();
			}
			this.actLoadedJarFile = modelFile;
			this.validate();
			this.repaint();
			ModelController.getInstance().setModelOpened(true);
			
		}
	}
	
	public void classLoaderHasChanged() {

	   if(ModelController.getInstance().isModelOpened()){
	   	
	         reloadModel(ModelController.getInstance().getBioChemicalModelController().getActLoadedModelFile(), SnapshotWriter.getInstance().getSnapshotPath());
        
	   }
	   
   }
	private void setSnapshotPath(){
		setSnapshotPath(null);
		
	}
	private void setSnapshotPath(File file){
		if(file == null){
			tssFileChoose.setDialogTitle("Set Snaphot-Path");
			if(tssFileChoose.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) file = tssFileChoose.getSelectedFile();
			
		}
		if(file != null){
			  this.setTitle("Epidermis Simulator"+ " - Snapshot-Path: "+file.getAbsolutePath());
			  SnapshotWriter.getInstance().setSnapshotPath(file);
			  SnapshotWriter.getInstance().resetCounter();
		}	
		
	}
	public void loadSnapshot() {
		TissueController.getInstance().getTissueBorder().loadStandardMebrane();
		File snapshotFile = null;
		File jarFile = null;
				
		jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model of the selected Snapshot");
		tssFileChoose.setDialogTitle("Load Snapshot");
		if(tssFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && jarFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			snapshotFile = tssFileChoose.getSelectedFile();
			jarFile = jarFileChoose.getSelectedFile();
			loadSnapshot(snapshotFile, jarFile, false);
		}
		
	}
	
	private void loadSnapshot(File snapshotFile, File jarFile, boolean snapshotRestart){
		boolean success = false;
		try{
         success = ModelController.getInstance().getBioChemicalModelController().loadModelFile(jarFile);
      }
      catch (ModelCompatibilityException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      	 JOptionPane.showMessageDialog(this, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
      	 success = false;
      }
		SnapshotLoader snapshotLoader = null;
		try{
			snapshotLoader = new SnapshotLoader(snapshotFile, jarFile);
		}
		catch(IllegalArgumentException ex){
			ExceptionDisplayer.getInstance().displayException(ex);
		}
		List<Double2D> woundRegionCoordinates = snapshotLoader.getWoundRegionCoordinates();		
		Epidermis epidermis = new Epidermis(System.currentTimeMillis());
		epidermis.addSnapshotLoadedCells(snapshotLoader.getLoadedCells());
		epidermis.setReloadedSnapshot(true);
		epidermis.setCellContinous2D(snapshotLoader.getCellContinous2D());
		epidermis.setSnapshotTimeSteps(snapshotLoader.getTimeSteps());
		
		TissueServer.getInstance().registerTissue(epidermis);		
		java.awt.geom.Rectangle2D.Double[] deltaInfo = snapshotLoader.getDeltaInfo();
				if(SnapshotWriter.getInstance().getSnapshotPath() == null){
					JOptionPane.showMessageDialog(this, "Please specify snapshot path.", "Info",
							JOptionPane.INFORMATION_MESSAGE);
					setSnapshotPath();
					if(SnapshotWriter.getInstance().getSnapshotPath() == null)success = false;
				}
				
				if(success){
				
					ChartController.getInstance().rebuildDefaultCharts();
					ModelController.getInstance().getBioChemicalModelController().
					                                          reloadCellDiffModelGlobalParametersObject(snapshotLoader.getEpisimCellDiffModelGlobalParameters());
					ModelController.getInstance().getBioMechanicalModelController().
							reloadMechanicalModelGlobalParametersObject(snapshotLoader.getEpisimMechanicalModelGlobalParameters());
					cleanUpContentPane();
					
					epiUI = new EpidermisGUIState(epidermis, this, true);
					registerSimulationStateListeners(epiUI);
					epiUI.addSnapshotRestartListener(this);
					epiUI.setReloadedSnapshot(true);
					if(epiUI.getWoundPortrayalDraw() !=null){
						
					  if(woundRegionCoordinates!= null) epiUI.getWoundPortrayalDraw().setWoundRegionCoordinates(woundRegionCoordinates);
					  if(deltaInfo!= null && deltaInfo.length >=2) 
						  epiUI.getWoundPortrayalDraw().setDeltaInfo(new DrawInfo2D(deltaInfo[0], deltaInfo[1]) );
					  SnapshotWriter.getInstance().addSnapshotListener(epiUI.getWoundPortrayalDraw());
					}
					this.validate();
					this.repaint();
					ModelController.getInstance().setModelOpened(success);
					menuItemSetSnapshotPath.setEnabled(true);
					menuItemClose.setEnabled(true);
					menuItemLoadSnapshot.setEnabled(false);
					menuItemBuild.setEnabled(false);
					chartMenu.setEnabled(true);
					dataExportMenu.setEnabled(true);
					this.actLoadedJarFile = jarFile;
					this.actLoadedSnapshotFile = snapshotFile;
				}
	}
	
	
	private void registerSimulationStateListeners(EpidermisGUIState guiState){
		epiUI.addSimulationStateChangeListener(this);
		epiUI.addSimulationStateChangeListener(CellEllipseIntersectionCalculculationRegistry.getInstance());
		epiUI.addSimulationStateChangeListener(SimStateServer.getInstance());
	}
	private void buildModelArchive(){
		CompileWizard wizard = new CompileWizard(this);
		
			wizard.showSelectFilesDialogs();
		
	}
	
	private void closeModel(){
		if(epiUI != null){
			
			epiUI.quit();
		}
		epiUI = null;
		System.gc();
		this.repaint();
		ModelController.getInstance().setModelOpened(false);
		menuItemLoadSnapshot.setEnabled(true);
		menuItemClose.setEnabled(false);
		menuItemBuild.setEnabled(true);
		chartMenu.setEnabled(false);
		dataExportMenu.setEnabled(false);
		statusbar.setMessage("");
		ChartController.getInstance().modelWasClosed();
		DataExportController.getInstance().modelWasClosed();
		
		
		//Menu-Items ChartSet
		this.menuItemEditChartSet.setEnabled(false);
		this.menuItemLoadChartSet.setEnabled(true);
		this.menuItemCloseChartSet.setEnabled(false);
		this.menuItemNewChartSet.setEnabled(true);
		
		//Menu-Items DataExport
		this.menuItemEditDataExport.setEnabled(false);
		this.menuItemLoadDataExport.setEnabled(true);
		this.menuItemCloseDataExport.setEnabled(false);
		this.menuItemNewDataExport.setEnabled(true);
		
		
		GlobalClassLoader.getInstance().destroyClassLoader();
		SnapshotWriter.getInstance().clearListeners();
		SnapshotWriter.getInstance().resetCounter();
		this.setTitle("Epidermis Simulator");
		SnapshotWriter.getInstance().setSnapshotPath(null);
		this.actLoadedJarFile = null;
		this.actLoadedSnapshotFile = null;
	}
	
	public void simulationWasStarted(){
		
		this.chartMenu.setEnabled(false);
		this.dataExportMenu.setEnabled(false);
	}
	
	public void simulationWasStopped(){
		this.chartMenu.setEnabled(true);
		this.dataExportMenu.setEnabled(true);
		DataExportController.getInstance().simulationWasStopped();
	}
	
	private void cleanUpContentPane(){
		Component[] comps = this.getContentPane().getComponents();
		for(int i = 0; i < comps.length; i++){
			if(!(comps[i] instanceof JMenuBar) && !(comps[i] instanceof StatusBar)) this.getContentPane().remove(i);
		}
			
	}

	public void snapShotRestart() {
		int option = JOptionPane.showConfirmDialog(this, "For restarting a Snapshot a full reload of the respective file is necessary. All charts will be closed! Continue?", "Snapshot Restart", JOptionPane.YES_NO_OPTION);
		if(option == JOptionPane.YES_OPTION){
			File jar = this.actLoadedJarFile;
			File snap = this.actLoadedSnapshotFile;
			closeModel();
		  if(jar != null && snap != null){
			  loadSnapshot(snap, jar, true);
		  }
		}
   }

	public void simulationWasPaused() {}

}
