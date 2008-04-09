package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import episimexceptions.ModelCompatibilityException;

import sim.app.episim.CompileWizard;
import sim.app.episim.Epidermis;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SimulationStateChangeListener;
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
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.engine.Schedule;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpidermisSimulator extends JFrame implements SimulationStateChangeListener, ClassLoaderChangeListener{
	
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
	
	
	public EpidermisSimulator() {
		ExceptionDisplayer.getInstance().registerParentComp(this);
		
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
		fileMenu = new JMenu("File");
		menuItemOpen = new JMenuItem("Open Episim-Cell-Diff-Model");
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
		
		menuItemClose = new JMenuItem("Close Episim-Cell-Diff-Model");
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
		fileMenu.addSeparator();
		fileMenu.add(menuItemBuild);
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
		
		menuItemNewDataExport = new JMenuItem("New Data-Export-Definition");
		menuItemNewDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				success = DataExportController.getInstance().showNewChartSetDialog(simulator);
				if(success){
					menuItemEditDataExport.setEnabled(true);
					menuItemCloseDataExport.setEnabled(true);
					menuItemNewDataExport.setEnabled(false);
					menuItemLoadDataExport.setEnabled(false);
				}
				
			}
			
		});
		
		menuItemLoadDataExport = new JMenuItem("Load Data-Export-Definition");
		menuItemLoadDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean success = DataExportController.getInstance().loadDataExportDefinition(simulator);
				
				if(success){ 
					DataExportController.getInstance().showEditDataExportDefinitionDialog(simulator);
					menuItemEditDataExport.setEnabled(true);
					menuItemCloseDataExport.setEnabled(true);
					menuItemNewDataExport.setEnabled(false);
					menuItemLoadDataExport.setEnabled(false);
				}
				else{ 
					if(!DataExportController.getInstance().isAlreadyDataExportLoaded()) menuItemEditDataExport.setEnabled(false);
				}
			}
			
		});
		
		menuItemEditDataExport = new JMenuItem("Edit Loaded Data-Export-Definition");
		menuItemEditDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				DataExportController.getInstance().showEditDataExportDefinitionDialog(simulator);
			}
			
		});
		menuItemEditDataExport.setEnabled(false);
		
		menuItemCloseDataExport = new JMenuItem("Close Loaded Data-Export-Definition");
		menuItemCloseDataExport.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				menuItemNewDataExport.setEnabled(true);
				menuItemLoadDataExport.setEnabled(true);
				menuItemCloseDataExport.setEnabled(false);
				menuItemEditDataExport.setEnabled(false);
				DataExportController.getInstance().closeActLoadedDataExportDefiniton();
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
		
		jarFileChoose= new ExtendedFileChooser("jar");
		jarFileChoose.setDialogTitle("Open Episim Cell Differentiation Model");
		tssFileChoose = new ExtendedFileChooser("tss");
		
		this.setTitle("Epidermis Simulator");
		
		this.setPreferredSize(new Dimension((int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
				(int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 30));
		
		
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
		TissueBorder.getInstance().loadStandardMebrane();
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		File file = null;
		File standartDir =new File("d:/");
		jarFileChoose.setDialogTitle("Open Episim Cell Differentiation Model");
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
				epiUI.addSimulationStateChangeListener(this);
				this.validate();
				this.repaint();
				ModelController.getInstance().setModelOpened(true);
				menuItemSetSnapshotPath.setEnabled(true);
				menuItemLoadSnapshot.setEnabled(false);
				menuItemBuild.setEnabled(false);
				menuItemClose.setEnabled(true);
				chartMenu.setEnabled(true);
				dataExportMenu.setEnabled(true);
			}

		}
		
	}
	private void reloadModel(File modelFile, File snapshotPath){
		TissueBorder.getInstance().loadStandardMebrane();
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
			ChartController.getInstance().rebuildDefaultCharts();
			cleanUpContentPane();
			epiUI = new EpidermisGUIState(this);
			epiUI.addSimulationStateChangeListener(this);
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
		TissueBorder.getInstance().loadStandardMebrane();
		File snapshotFile = null;
		File jarFile = null;
		boolean success = false;
		
		
		
		jarFileChoose.setDialogTitle("Open Episim Cell Differentiation Model of the selected Snapshot");
		tssFileChoose.setDialogTitle("Load Snapshot");
		if(tssFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION && jarFileChoose.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
			snapshotFile = tssFileChoose.getSelectedFile();
			jarFile = jarFileChoose.getSelectedFile();
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
			Epidermis epidermis = snapshotLoader.getEpidermis();
			
			//TODO: implement Reload Charts from Snapshot
			//DefaultCharts.setInstance(snapshotLoader.getCharts());
			java.awt.geom.Rectangle2D.Double[] deltaInfo = snapshotLoader.getDeltaInfo();
					if(SnapshotWriter.getInstance().getSnapshotPath() == null){
						JOptionPane.showMessageDialog(this, "Please specify snapshot path.", "Info",
								JOptionPane.INFORMATION_MESSAGE);
						setSnapshotPath();
						if(SnapshotWriter.getInstance().getSnapshotPath() == null)success = false;
					}
					
					if(success){
					
						// EpiSimCharts.rebuildCharts();
						ModelController.getInstance().getBioChemicalModelController().
						                                          reloadCellDiffModelGlobalParametersObject(snapshotLoader.getEpisimCellDiffModelGlobalParameters());
						ModelController.getInstance().getBioMechanicalModelController().
								reloadMechanicalModelGlobalParametersObject(snapshotLoader.getEpisimMechanicalModelGlobalParameters());
						cleanUpContentPane();
						epidermis.setModelController(ModelController.getInstance());
						epiUI = new EpidermisGUIState(epidermis, this, true);
						epiUI.addSimulationStateChangeListener(this);
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
					}
				

			}
		
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
		ChartController.getInstance().modelWasClosed();
		DataExportController.getInstance().modelWasClosed();
		this.menuItemEditChartSet.setEnabled(false);
		this.menuItemLoadChartSet.setEnabled(true);
		this.menuItemCloseChartSet.setEnabled(false);
		this.menuItemNewChartSet.setEnabled(true);
		GlobalClassLoader.getInstance().destroyClassLoader();
		SnapshotWriter.getInstance().clearListeners();
		SnapshotWriter.getInstance().resetCounter();
		 this.setTitle("Epidermis Simulator");
		  SnapshotWriter.getInstance().setSnapshotPath(null);
	}
	
	public void simulationWasStarted(){
		
		this.chartMenu.setEnabled(false);
		this.dataExportMenu.setEnabled(false);
	}
	
	public void simulationWasStopped(){
		this.chartMenu.setEnabled(true);
		this.dataExportMenu.setEnabled(true);
	}
	
	private void cleanUpContentPane(){
		Component[] comps = this.getContentPane().getComponents();
		for(int i = 0; i < comps.length; i++){
			if(!(comps[i] instanceof JMenuBar)) this.getContentPane().remove(i);
		}
			
	}

}
