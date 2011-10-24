package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import episimexceptions.ModelCompatibilityException;
import episimexceptions.PropertyException;

import sim.SimStateServer;
import sim.app.episim.CompileWizard;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenu;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenuItem;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotLoader;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.tissue.Epidermis;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ObservedByteArrayOutputStream;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpidermisSimulator implements SimulationStateChangeListener, ClassLoaderChangeListener, SnapshotRestartListener, SnapshotListener{
	
	public static final String versionID = "1.3";
	
	private static final String CB_FILE_PARAM_PREFIX = "-cb";
	private static final String BM_FILE_PARAM_PREFIX = "-bm";
	private static final String M_FILE_PARAM_PREFIX = "-mp";
	private static final String SIM_ID_PARAM_PREFIX = "-id";
	private static final String HELP = "-help";
	
	private JFrame mainFrame;
	private JPanel noGUIModeMainPanel;
	
	private ExtendedFileChooser jarFileChoose;
	private ExtendedFileChooser tssFileChoose;
	
	
	
	private EpisimGUIState epiUI;
	
	public static final ObservedByteArrayOutputStream errorOutputStream = new ObservedByteArrayOutputStream();
	public static final ObservedByteArrayOutputStream standardOutputStream = new ObservedByteArrayOutputStream();
	
	
	
	private StatusBar statusbar;
	
	private File actLoadedJarFile = null;
	private File actLoadedSnapshotFile = null;
	
	
	
	private EpisimMenuBarFactory menuBarFactory;
	
	public EpidermisSimulator() {
	
		
		 SnapshotWriter.getInstance().addSnapshotListener(this);
		if(ModeServer.guiMode()){
			mainFrame = new JFrame();
			mainFrame.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon.gif")).getImage());
			ExceptionDisplayer.getInstance().registerParentComp(mainFrame);
		}
		else{
			noGUIModeMainPanel = new JPanel();
			
		}
			
		statusbar = new StatusBar();
		if(ModeServer.guiMode()){
			try{
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
			catch (Exception e){
				
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		
		//--------------------------------------------------------------------------------------------------------------
		//Menü
		//--------------------------------------------------------------------------------------------------------------
		this.menuBarFactory = new EpisimMenuBarFactory(this);	
	
		//--------------------------------------------------------------------------------------------------------------
		if(ModeServer.guiMode()){
			mainFrame.getContentPane().setLayout(new BorderLayout());
			mainFrame.getContentPane().setBackground(Color.LIGHT_GRAY);			
			mainFrame.getContentPane().add(statusbar, BorderLayout.SOUTH);
			jarFileChoose= new ExtendedFileChooser("jar");
			jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
			tssFileChoose = new ExtendedFileChooser("tss");
			mainFrame.setTitle("Episim Simulator");
		}
		else{
			noGUIModeMainPanel.setLayout(new BorderLayout());
			noGUIModeMainPanel.setBackground(Color.LIGHT_GRAY);			
			noGUIModeMainPanel.add(statusbar, BorderLayout.SOUTH);
		}
		
		
		
		
		
		
		//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		
		if(ModeServer.consoleInput()){
			
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SNAPSHOT_PATH_PROP) != null){
				File snapshotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SNAPSHOT_PATH_PROP));
				if(snapshotPath.isDirectory()){
					snapshotPath = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATOR_SNAPSHOT_PATH_PROP, "EpisimSnapshot", "tss");
				}
				setSnapshotPath(snapshotPath, false);
			}
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELL_BEHAVIORAL_MODEL_PATH_PROP) != null){
				File cellbehavioralModelFile = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELL_BEHAVIORAL_MODEL_PATH_PROP));
				if(!cellbehavioralModelFile.exists() || !cellbehavioralModelFile.isFile()) throw new PropertyException("No existing Cell Behavioral Model File specified: "+cellbehavioralModelFile.getAbsolutePath());
				else{
					openModel(cellbehavioralModelFile, true);
					
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTSETPATH) != null){
						File chartSetFile = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTSETPATH));
						if(!chartSetFile.exists() || !chartSetFile.isFile()) throw new PropertyException("No existing Chart-Set File specified: "+chartSetFile.getAbsolutePath());
						else{
							ChartController.getInstance().loadChartSet(chartSetFile);
							if(ModeServer.guiMode()){
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(false);
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(false);
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(true);
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(true);
							}
						}
					}
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTPATH) != null){
						File dataExportDefinitionFile = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTPATH));
						if(!dataExportDefinitionFile.exists() || !dataExportDefinitionFile.isFile()) throw new PropertyException("No existing Data Export Definition File specified: "+dataExportDefinitionFile.getAbsolutePath());
						else{
							DataExportController.getInstance().loadDataExportDefinition(dataExportDefinitionFile);
							if(ModeServer.guiMode()){
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(false);
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(false);
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(true);
								menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(true);
							}
						}
					}
					
				}
			}
			
		}
		//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//        TODO: to be changed for video recording
		//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		if(ModeServer.guiMode()){
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
		else{
			noGUIModeMainPanel.setPreferredSize(new Dimension(1900, 1200));		
			noGUIModeMainPanel.setVisible(true);
		}
		if(ModeServer.consoleInput()){			
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MAX_SIMULATION_STEPS_PROP) != null){
				long steps = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MAX_SIMULATION_STEPS_PROP));
				if(epiUI != null && steps > 0) epiUI.setMaxSimulationSteps(steps);
			}			
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_AUTOSTART_AND_STOP_PROP) != null && 
					EpisimProperties.getProperty(EpisimProperties.SIMULATOR_AUTOSTART_AND_STOP_PROP).equals(EpisimProperties.ON)){
				if(epiUI != null){ 
					Runnable r  = new Runnable(){

						public void run() {

							epiUI.startSimulation();
							if(!ModeServer.guiMode()){
								System.out.println(" ----------------------------------------");
								System.out.println("|              EPISIM SIMULATOR          |");
								System.out.println(" ----------------------------------------\n");
								System.out.println("------------Simulation Started------------");
							}
							epiUI.scheduleAtEnd(new Steppable(){

								public void step(SimState state) {
									 Runnable runnable = new Runnable(){

										public void run() {
											try{
	                                 Thread.sleep(30000);
	                                 System.out.println("\n======================= Shutting down EPISIM Simulator =======================");
	      	                        if(epiUI != null){
	      	         						epiUI.closeConsole();
	      	         						System.exit(0);
	      	         					}
	      	         					else System.exit(0);
                                 }
                                 catch (InterruptedException e){
	                                 ExceptionDisplayer.getInstance().displayException(e);
                                 }                              
	                              
                              }										
									};

									Thread t = new Thread(runnable);
									t.start();
	                        
                        }});
	                  
                  }};
                  SwingUtilities.invokeLater(r);
				}
			}
		}
	}
	
	public static void main(String[] args){
		boolean onlyHelpWanted = false;
		
		if(args.length >= 1 && args[0].equals(EpidermisSimulator.HELP)) onlyHelpWanted = true;
		else{
		
			for(int i = 0; i < args.length; i++){
				if(args[i].equals(EpidermisSimulator.BM_FILE_PARAM_PREFIX) 
						|| args[i].equals(EpidermisSimulator.CB_FILE_PARAM_PREFIX)
						|| args[i].equals(EpidermisSimulator.SIM_ID_PARAM_PREFIX)
						|| args[i].equals(EpidermisSimulator.M_FILE_PARAM_PREFIX)){
					
					if((i+1) >= args.length) throw new PropertyException("Missing value after parameter: "+ args[i]);
					if(args[i].equals(EpidermisSimulator.BM_FILE_PARAM_PREFIX) 
						|| args[i].equals(EpidermisSimulator.CB_FILE_PARAM_PREFIX)
						|| args[i].equals(EpidermisSimulator.M_FILE_PARAM_PREFIX)){
						File path = new File(args[i+1]);
						
						if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to a property file for parameter " + args[i]);
						
						if(args[i].equals(EpidermisSimulator.BM_FILE_PARAM_PREFIX)){
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP, path.getAbsolutePath());
						}
						else if(args[i].equals(EpidermisSimulator.CB_FILE_PARAM_PREFIX)){
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP, path.getAbsolutePath());
						}
						else if(args[i].equals(EpidermisSimulator.M_FILE_PARAM_PREFIX)){
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP, path.getAbsolutePath());
						}
					}
					else if(args[i].equals(EpidermisSimulator.SIM_ID_PARAM_PREFIX)){
						EpisimProperties.setProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID, args[i+1].trim());
					}
				}			
			}
		}
		String mode;
		if((mode=EpisimProperties.getProperty(EpisimProperties.EXCEPTION_DISPLAYMODE_PROP)) != null 
				&& mode.equals(EpisimProperties.SIMULATOR))  System.setErr(new PrintStream(errorOutputStream));
		
		if((mode=EpisimProperties.getProperty(EpisimProperties.STANDARD_OUTPUT)) != null 
				&& mode.equals(EpisimProperties.SIMULATOR))  System.setOut(new PrintStream(standardOutputStream));
		
		if(!onlyHelpWanted){
			//if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
				//	&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.OFF_SIMULATOR_GUI_VAL))
				//System.setProperty("java.awt.headless", "true"); 
			EpidermisSimulator episim = new EpidermisSimulator();
		}
		else printHelpTextOnConsole();
		
	}
	
	public static void printHelpTextOnConsole(){
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------- EPISIM Simulator Help -------------------------\n\n");
		sb.append("The EPISIM Simulator supports the following input parameters:\n");
		sb.append("\t[-bm path] to the biomedical model parameters file\n");
		sb.append("\t[-cb path] to the cell behavioral model parameters file\n");
		sb.append("\t[-mp path] to the miscellaneous parameters file\n");
		sb.append("\t[-id identifier] of the current simulation run\n");
		System.out.println(sb.toString());
	}
	
	
	protected void openModel(File modelFile, boolean standardInitialization){
		ModelController.getInstance().setSimulationStartedOnce(false);
		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		
		File standardDir =new File("d:/");
		if(ModeServer.guiMode()){
			jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
			if(standardDir.exists())jarFileChoose.setCurrentDirectory(standardDir);
		}
		
		
		
		if((modelFile != null || (jarFileChoose.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION && ModeServer.guiMode()))){
			if(modelFile == null) modelFile = jarFileChoose.getSelectedFile();
			boolean success = false; 
			try{
	         success= ModelController.getInstance().loadCellBehavioralModelFile(modelFile);
         }
         catch (ModelCompatibilityException e){
	        ExceptionDisplayer.getInstance().displayException(e);
	        if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
	        success = false;
         }
			
			
			//System.out.println(success);
			if(success){
				if(standardInitialization)ModelController.getInstance().standardInitializationOfModels();
				ChartController.getInstance().rebuildDefaultCharts();
				cleanUpContentPane();
				if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
				else epiUI = new EpisimGUIState(noGUIModeMainPanel);
				registerSimulationStateListeners(epiUI);
				epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
				if(ModeServer.guiMode()){
					mainFrame.validate();
					mainFrame.repaint();
				}
				else{
					noGUIModeMainPanel.validate();
					noGUIModeMainPanel.repaint();
				}
				
				ModelController.getInstance().setModelOpened(true);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_SNAPSHOT).setEnabled(false);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(false);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
				this.actLoadedJarFile = modelFile;
			}

		}
		
	}
	
	protected void openModel(){
		openModel(null, true);
	}
	
	
	protected void reloadModel(File modelFile, File snapshotPath, boolean standardInitialization){
		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		boolean success = false; 
		try{
         success= ModelController.getInstance().loadCellBehavioralModelFile(modelFile);
      }
      catch(ModelCompatibilityException e){
        ExceptionDisplayer.getInstance().displayException(e);
        if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
        success = false;
      }
		
		if(success){
			
			setSnapshotPath(snapshotPath, true);
			
			//TODO: add consideration of initialization filepath if set
			if(standardInitialization)ModelController.getInstance().standardInitializationOfModels();
		//	System.out.println("Already Data Export Loaded: " + DataExportController.getInstance().isAlreadyDataExportSetLoaded());
			ChartController.getInstance().rebuildDefaultCharts();
			cleanUpContentPane();
			if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
			else epiUI = new EpisimGUIState(noGUIModeMainPanel);
			
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
			if(ModeServer.guiMode()){
				mainFrame.validate();
				mainFrame.repaint();
			}
			else{
				noGUIModeMainPanel.validate();
				noGUIModeMainPanel.repaint();
			}
			
			ModelController.getInstance().setModelOpened(true);
			
		}
	}
	
	public void classLoaderHasChanged() {

	   if(ModelController.getInstance().isModelOpened()){
	   	
	   		//TODO: Diese Stelle anpassen wenn das Laden von SimulationStateFile funktioniert
	         reloadModel(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile(), SnapshotWriter.getInstance().getSnapshotPath(), true);
        
	   }
	   
   }
	public void setSnapshotPath(){
		setSnapshotPath(null, false);
		
	}
	protected void setSnapshotPath(File file, boolean modelReload){
		if(file == null && !modelReload){
			tssFileChoose.setDialogTitle("Set Snaphot-Path");
			if(ModeServer.guiMode()){
				if(tssFileChoose.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) file = tssFileChoose.getSelectedFile();
			}
			
		}
		if(file != null){
			try{
	        if(ModeServer.guiMode()) mainFrame.setTitle("Episim Simulator"+ " - Snapshot-Path: "+file.getCanonicalPath());
         }
         catch (IOException e){
	         ExceptionDisplayer.getInstance().displayException(e);
         }
			  SnapshotWriter.getInstance().setSnapshotPath(file);
			  SnapshotWriter.getInstance().resetCounter();
		}	
		
	}
	
	public void loadSnapshot() {
		//TODO: Loading of standard membrane has to be considered here (eventually)
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
         success = ModelController.getInstance().loadCellBehavioralModelFile(jarFile);
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
		
		epidermis.setSnapshotTimeSteps(snapshotLoader.getTimeSteps());
		
		TissueController.getInstance().registerTissue(epidermis);		
		java.awt.geom.Rectangle2D.Double[] deltaInfo = snapshotLoader.getDeltaInfo();
				
				
				if(success){
					
					ModelController.getInstance().getBioMechanicalModelController().setReloadedCellField(snapshotLoader.getCellContinous2D());
					//TODO: add consideration of initialization filepath if set
					ModelController.getInstance().standardInitializationOfModels();
					ChartController.getInstance().rebuildDefaultCharts();
					ModelController.getInstance().getCellBehavioralModelController().
					                                          reloadCellBehavioralModelGlobalParametersObject(snapshotLoader.getEpisimCellBehavioralModelGlobalParameters());
					ModelController.getInstance().getBioMechanicalModelController().
							reloadMechanicalModelGlobalParametersObject(snapshotLoader.getEpisimMechanicalModelGlobalParameters());
					cleanUpContentPane();
					
					if(ModeServer.guiMode())epiUI = new EpisimGUIState(epidermis, mainFrame, true);
					else epiUI = new EpisimGUIState(epidermis, noGUIModeMainPanel, true);
					registerSimulationStateListeners(epiUI);
					epiUI.addSnapshotRestartListener(this);
					epiUI.setReloadedSnapshot(true);
					if(epiUI.getWoundPortrayalDraw() !=null){
						
					  if(woundRegionCoordinates!= null) epiUI.getWoundPortrayalDraw().setWoundRegionCoordinates(woundRegionCoordinates);
					  if(deltaInfo!= null && deltaInfo.length >=2) 
						  epiUI.getWoundPortrayalDraw().setDeltaInfo(new DrawInfo2D(null, null, deltaInfo[0], deltaInfo[1]) );
					  SnapshotWriter.getInstance().addSnapshotListener(epiUI.getWoundPortrayalDraw());
					}
					epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
					if(ModeServer.guiMode()){
						mainFrame.validate();
						mainFrame.repaint();
					}
					else{
						noGUIModeMainPanel.validate();
						noGUIModeMainPanel.repaint();
					}
					ModelController.getInstance().setModelOpened(success);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_SNAPSHOT).setEnabled(false);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(false);
					menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
					this.actLoadedJarFile = jarFile;
					this.actLoadedSnapshotFile = snapshotFile;
				}
	}
	
	
	protected void registerSimulationStateListeners(EpisimGUIState guiState){
		epiUI.addSimulationStateChangeListener(SimStateServer.getInstance());
		
		SimStateServer.getInstance().addSimulationStateChangeListener(this);
		SimStateServer.getInstance().addSimulationStateChangeListener(CellEllipseIntersectionCalculationRegistry.getInstance());
	}
	protected void buildModelArchive(){
		if(ModeServer.guiMode()){
			CompileWizard wizard = new CompileWizard(mainFrame);
			wizard.showSelectFilesDialogs();
		}
		
	}
	
	protected void closeModel(){
		if(epiUI != null){
			
			epiUI.quit();
		}
		epiUI = null;
		System.gc();
		if(ModeServer.guiMode())mainFrame.repaint();
		else noGUIModeMainPanel.repaint();
		ModelController.getInstance().setModelOpened(false);
		TissueController.getInstance().resetTissueSettings();
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_SNAPSHOT).setEnabled(true);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(true);
		menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(false);
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
		if(ModeServer.guiMode())mainFrame.setTitle("Episim Simulator");
		SnapshotWriter.getInstance().setSnapshotPath(null);
		this.actLoadedJarFile = null;
		this.actLoadedSnapshotFile = null;
	}
	
	public void simulationWasStarted(){
		
		this.menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.WINDOWS_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(false);
	}
	
	public void simulationWasStopped(){
		this.menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.WINDOWS_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
		DataExportController.getInstance().simulationWasStopped();
	}
	
	public void close(){ System.exit(0);}
	
	private void cleanUpContentPane(){
		if(ModeServer.guiMode()){
			Component[] comps = mainFrame.getContentPane().getComponents();
			for(int i = 0; i < comps.length; i++){
				if(!(comps[i] instanceof JMenuBar) && !(comps[i] instanceof StatusBar)) mainFrame.getContentPane().remove(i);
			}
		}
		else{
			Component[] comps = noGUIModeMainPanel.getComponents();
			for(int i = 0; i < comps.length; i++){
				if(!(comps[i] instanceof JMenuBar) && !(comps[i] instanceof StatusBar)) noGUIModeMainPanel.remove(i);
			}
		}
			
	}

	public void snapShotRestart() {
		int option = Integer.MIN_VALUE;
		if(ModeServer.guiMode()) option = JOptionPane.showConfirmDialog(mainFrame, "For restarting a Snapshot a full reload of the respective file is necessary. All charts will be closed! Continue?", "Snapshot Restart", JOptionPane.YES_NO_OPTION);
		else option = JOptionPane.YES_OPTION;
		
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
		if(epiUI != null)epiUI.removeAllChartInternalFrames();
	}
	
	protected void setAutoArrangeWindows(boolean autoArrange){
		if(epiUI != null) epiUI.setAutoArrangeWindows(autoArrange);
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
	
   public Component getMainFrame(){ 
   	return ModeServer.guiMode() ? mainFrame : noGUIModeMainPanel; }

	public List<SnapshotObject> collectSnapshotObjects() {
			//does nothing, is required to Connect the Episim Simulator class to SnapshotWriter, if snapshot-path is not set
		return new ArrayList<SnapshotObject>();
   }
	
	protected void saveSimulationStateFile(File f){
		try{
			
            (new SimulationStateFile()).saveData(f);
        }
        catch (ParserConfigurationException e1){
           ExceptionDisplayer.getInstance().displayException(e1);
        }
        catch (SAXException e1){
        	ExceptionDisplayer.getInstance().displayException(e1);
        }
	}
	
	protected void loadSimulationStateFile(File f){
		try{
            SimulationStateData simStateData = new SimulationStateFile(f).loadData();
            openModel(simStateData.getLoadedModelFile(), false);
    		ModelController.getInstance().initializeModels(simStateData);
        }
        catch (ParserConfigurationException e1){
        	ExceptionDisplayer.getInstance().displayException(e1);
        }
        catch (SAXException e1){
        	ExceptionDisplayer.getInstance().displayException(e1);
        } 
		  catch (IOException e1) {
			ExceptionDisplayer.getInstance().displayException(e1);
		} 
	}

	public File getActLoadedJarFile() {
		return actLoadedJarFile;
	}
}