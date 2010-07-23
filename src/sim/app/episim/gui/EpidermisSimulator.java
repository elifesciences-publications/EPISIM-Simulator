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
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
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
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.TissueServer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartPanelAndSteppableServer;
import sim.app.episim.datamonitoring.charts.DefaultCharts;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenu;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenuItem;

import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.ModelController;
import sim.app.episim.snapshot.SnapshotLoader;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotReader;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ObservedByteArrayOutputStream;
import sim.app.episim.visualization.WoundPortrayal2D;
import sim.display.Console;
import sim.display.ConsoleHack;
import sim.engine.Schedule;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpidermisSimulator implements SimulationStateChangeListener, ClassLoaderChangeListener, SnapshotRestartListener{
	
	public static final String versionID = "1.1.1";
	
	private JFrame mainFrame;
	
	private ExtendedFileChooser jarFileChoose;
	private ExtendedFileChooser tssFileChoose;
	
	
	
	private EpidermisGUIState epiUI;
	
	public static final ObservedByteArrayOutputStream errorOutputStream = new ObservedByteArrayOutputStream();
	
	
	
	private StatusBar statusbar;
	
	private File actLoadedJarFile = null;
	private File actLoadedSnapshotFile = null;
	
	private boolean guiMode = true;
	
	private EpisimMenuBarFactory menuBarFactory;
	
	public EpidermisSimulator() {
		mainFrame = new JFrame();
		mainFrame.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
		
		
		ExceptionDisplayer.getInstance().registerParentComp(mainFrame);
		
		statusbar = new StatusBar();
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e){
			
			ExceptionDisplayer.getInstance().displayException(e);
		}
		
		//--------------------------------------------------------------------------------------------------------------
		//Menü
		//--------------------------------------------------------------------------------------------------------------
		this.menuBarFactory = new EpisimMenuBarFactory(this);
		
		
	
	
		//--------------------------------------------------------------------------------------------------------------
		
		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().setBackground(Color.LIGHT_GRAY);
		
		
		
		mainFrame.getContentPane().add(statusbar, BorderLayout.SOUTH);
		
		jarFileChoose= new ExtendedFileChooser("jar");
		jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
		tssFileChoose = new ExtendedFileChooser("tss");
		
		mainFrame.setTitle("Episim Simulator");
		
		
		//TODO: to be changed for video recording
		
		mainFrame.setPreferredSize(new Dimension((int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth()*0.95),
				(int) (java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight()*0.9)));
		
		
	//	this.setPreferredSize(new Dimension(1280, 932));
		
		
		mainFrame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {

				if(epiUI != null){
					epiUI.closeConsole();
					System.exit(0);
				}
				else System.exit(0);

			}
		});
		mainFrame.pack();
		centerMe(mainFrame);
		mainFrame.setVisible(true);
	}
	
	public static void main(String[] args){
		EpidermisSimulator episim = new EpidermisSimulator();
		
		String mode;
		if((mode=EpisimProperties.getProperty(EpisimProperties.EXCEPTION_DISPLAYMODE_PROP)) != null 
				&& mode.equals(EpisimProperties.SIMULATOR_EXCEPTION_DISPLAYMODE_VAL))  System.setErr(new PrintStream(errorOutputStream));
	}
	
	
	
	
	protected void openModel(File modelFile){
		ModelController.getInstance().setSimulationStartedOnce(false);
		TissueController.getInstance().getTissueBorder().loadStandardMebrane();
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		
		File standartDir =new File("d:/");
		jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
		if(standartDir.exists())jarFileChoose.setCurrentDirectory(standartDir);
		if(modelFile != null || jarFileChoose.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
			if(modelFile == null) modelFile = jarFileChoose.getSelectedFile();

			boolean success = false; 
			try{
	         success= ModelController.getInstance().getBioChemicalModelController().loadModelFile(modelFile);
         }
         catch (ModelCompatibilityException e){
	        ExceptionDisplayer.getInstance().displayException(e);
	        JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
	        success = false;
         }
			
			if(success && SnapshotWriter.getInstance().getSnapshotPath() == null){
				JOptionPane.showMessageDialog(mainFrame, "Please specify snapshot path.", "Info", JOptionPane.INFORMATION_MESSAGE);
				setSnapshotPath();
				if(SnapshotWriter.getInstance().getSnapshotPath() == null)success = false;
			}
			//System.out.println(success);
			if(success){
				ChartController.getInstance().rebuildDefaultCharts();
				cleanUpContentPane();
				epiUI = new EpidermisGUIState(mainFrame);
				registerSimulationStateListeners(epiUI);
				epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
				mainFrame.validate();
				mainFrame.repaint();
				ModelController.getInstance().setModelOpened(true);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_SNAPSHOT).setEnabled(false);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(false);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
				this.actLoadedJarFile = modelFile;
			}

		}
		
	}
	
	protected void openModel(){
		openModel(null);
	}
	
	
	protected void reloadModel(File modelFile, File snapshotPath){
		TissueController.getInstance().getTissueBorder().loadStandardMebrane();
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		boolean success = false; 
		try{
         success= ModelController.getInstance().getBioChemicalModelController().loadModelFile(modelFile);
      }
      catch(ModelCompatibilityException e){
        ExceptionDisplayer.getInstance().displayException(e);
        JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
        success = false;
      }
		
		if(success){
			
			setSnapshotPath(snapshotPath);
			
			
		//	System.out.println("Already Data Export Loaded: " + DataExportController.getInstance().isAlreadyDataExportSetLoaded());
			ChartController.getInstance().rebuildDefaultCharts();
			cleanUpContentPane();
			epiUI = new EpidermisGUIState(mainFrame);
			registerSimulationStateListeners(epiUI);
			epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
			if(ChartController.getInstance().isAlreadyChartSetLoaded() && GlobalClassLoader.getInstance().getMode().equals(GlobalClassLoader.IGNORECHARTSETMODE)){
				ChartController.getInstance().reloadCurrentlyLoadedChartSet();
				GlobalClassLoader.getInstance().resetMode();
			}
			if(DataExportController.getInstance().isAlreadyDataExportSetLoaded() && GlobalClassLoader.getInstance().getMode().equals(GlobalClassLoader.IGNOREDATAEXPORTMODE)){
				DataExportController.getInstance().reloadCurrentlyLoadedDataExportDefinitionSet();
				GlobalClassLoader.getInstance().resetMode();
			}
			this.actLoadedJarFile = modelFile;
			mainFrame.validate();
			mainFrame.repaint();
			ModelController.getInstance().setModelOpened(true);
			
		}
	}
	
	public void classLoaderHasChanged() {

	   if(ModelController.getInstance().isModelOpened()){
	   	
	         reloadModel(ModelController.getInstance().getBioChemicalModelController().getActLoadedModelFile(), SnapshotWriter.getInstance().getSnapshotPath());
        
	   }
	   
   }
	protected void setSnapshotPath(){
		setSnapshotPath(null);
		
	}
	protected void setSnapshotPath(File file){
		if(file == null){
			tssFileChoose.setDialogTitle("Set Snaphot-Path");
			if(tssFileChoose.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) file = tssFileChoose.getSelectedFile();
			
		}
		if(file != null){
			mainFrame.setTitle("Episim Simulator"+ " - Snapshot-Path: "+file.getAbsolutePath());
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
		if(tssFileChoose.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION && jarFileChoose.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
			snapshotFile = tssFileChoose.getSelectedFile();
			jarFile = jarFileChoose.getSelectedFile();
			loadSnapshot(snapshotFile, jarFile, false);
		}
		
	}
	
	protected void loadSnapshot(File snapshotFile, File jarFile, boolean snapshotRestart){
		boolean success = false;
		try{
         success = ModelController.getInstance().getBioChemicalModelController().loadModelFile(jarFile);
      }
      catch (ModelCompatibilityException e){
      	 ExceptionDisplayer.getInstance().displayException(e);
      	 JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
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
					JOptionPane.showMessageDialog(mainFrame, "Please specify snapshot path.", "Info",
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
					
					epiUI = new EpidermisGUIState(epidermis, mainFrame, true);
					registerSimulationStateListeners(epiUI);
					epiUI.addSnapshotRestartListener(this);
					epiUI.setReloadedSnapshot(true);
					if(epiUI.getWoundPortrayalDraw() !=null){
						
					  if(woundRegionCoordinates!= null) epiUI.getWoundPortrayalDraw().setWoundRegionCoordinates(woundRegionCoordinates);
					  if(deltaInfo!= null && deltaInfo.length >=2) 
						  epiUI.getWoundPortrayalDraw().setDeltaInfo(new DrawInfo2D(deltaInfo[0], deltaInfo[1]) );
					  SnapshotWriter.getInstance().addSnapshotListener(epiUI.getWoundPortrayalDraw());
					}
					epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
					mainFrame.validate();
					mainFrame.repaint();
					ModelController.getInstance().setModelOpened(success);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_SNAPSHOT).setEnabled(false);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(false);
					menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
					this.actLoadedJarFile = jarFile;
					this.actLoadedSnapshotFile = snapshotFile;
				}
	}
	
	
	protected void registerSimulationStateListeners(EpidermisGUIState guiState){
		epiUI.addSimulationStateChangeListener(this);
		epiUI.addSimulationStateChangeListener(CellEllipseIntersectionCalculationRegistry.getInstance());
		epiUI.addSimulationStateChangeListener(SimStateServer.getInstance());
	}
	protected void buildModelArchive(){
		CompileWizard wizard = new CompileWizard(mainFrame);
		
			wizard.showSelectFilesDialogs();
		
	}
	
	protected void closeModel(){
		if(epiUI != null){
			
			epiUI.quit();
		}
		epiUI = null;
		System.gc();
		mainFrame.repaint();
		ModelController.getInstance().setModelOpened(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_SNAPSHOT).setEnabled(true);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(true);
		menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(false);
		statusbar.setMessage("");
		ChartController.getInstance().modelWasClosed();
		DataExportController.getInstance().modelWasClosed();
		
		
		//Menu-Items ChartSet
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(true);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(true);
		
		//Menu-Items DataExport
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(true);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(true);
		
		
		GlobalClassLoader.getInstance().destroyClassLoader();
		SnapshotWriter.getInstance().clearListeners();
		SnapshotWriter.getInstance().resetCounter();
		mainFrame.setTitle("Episim Simulator");
		SnapshotWriter.getInstance().setSnapshotPath(null);
		this.actLoadedJarFile = null;
		this.actLoadedSnapshotFile = null;
	}
	
	public void simulationWasStarted(){
		
		this.menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.WINDOWS_MENU).setEnabled(false);
	}
	
	public void simulationWasStopped(){
		this.menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.WINDOWS_MENU).setEnabled(true);
		DataExportController.getInstance().simulationWasStopped();
	}
	
	private void cleanUpContentPane(){
		Component[] comps = mainFrame.getContentPane().getComponents();
		for(int i = 0; i < comps.length; i++){
			if(!(comps[i] instanceof JMenuBar) && !(comps[i] instanceof StatusBar)) mainFrame.getContentPane().remove(i);
		}
			
	}

	public void snapShotRestart() {
		int option = JOptionPane.showConfirmDialog(mainFrame, "For restarting a Snapshot a full reload of the respective file is necessary. All charts will be closed! Continue?", "Snapshot Restart", JOptionPane.YES_NO_OPTION);
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
	

	protected void removeAllChartInternalFrames(){
		if(guiMode && epiUI != null)epiUI.removeAllChartInternalFrames();
	}
	
	protected void setAutoArrangeWindows(boolean autoArrange){
		if(guiMode && epiUI != null) epiUI.setAutoArrangeWindows(autoArrange);
	}
	
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation(((int)((screenDim.getWidth() /2) - (frame.getPreferredSize().getWidth()/2))), 
			((int)((screenDim.getHeight() /2) - (frame.getPreferredSize().getHeight()/2))));
		}
	}

	
   public StatusBar getStatusbar() {   
   	return statusbar;
   }
	
   public JFrame getMainFrame(){ return mainFrame; }
	
	
	
}
