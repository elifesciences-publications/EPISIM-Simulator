package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
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

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SwingSVGPrettyPrint;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import episimexceptions.ModelCompatibilityException;
import episimexceptions.PropertyException;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;

import sim.SimStateServer;
import sim.app.episim.CompileWizard;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimUpdater;
import sim.app.episim.EpisimUpdater.EpisimUpdateState;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenu;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenuItem;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.gui.EpisimUpdateDialog.UpdateCancelledCallback;

import sim.app.episim.model.biomechanics.hexagonbased.twosurface.HexagonBasedMechanicalModelTwoSurface;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.SimulationStateFile;

import sim.app.episim.tissue.UniversalTissue;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.tissue.TissueType;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ObservedByteArrayOutputStream;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpisimSimulator implements SimulationStateChangeListener, ClassLoaderChangeListener{
	
	public static final String versionID = "1.4.1.0.8";
	
	private static final String SIMULATOR_TITLE = "EPISIM Simulator v. "+ versionID+" ";
	
	private static final String CB_FILE_PARAM_PREFIX = "-cb";
	private static final String BM_FILE_PARAM_PREFIX = "-bm";
	private static final String M_FILE_PARAM_PREFIX = "-mp";
	private static final String SIM_ID_PARAM_PREFIX = "-id";
	private static final String DATA_EXPORT_FOLDER_PARAM_PREFIX = "-ef";
	private static final String HELP = "-help";
	
	private JFrame mainFrame;
	private JPanel noGUIModeMainPanel;
	
	private ExtendedFileChooser jarFileChoose;
	private ExtendedFileChooser tissueExportFileChoose;
	
	public static final double MAINFRAME_WIDTH_FACT = 0.95;
	public static final double MAINFRAME_HEIGHT_FACT = 0.9;
	
	private EpisimGUIState epiUI;
	
	public static final ObservedByteArrayOutputStream errorOutputStream = new ObservedByteArrayOutputStream();
	public static final ObservedByteArrayOutputStream standardOutputStream = new ObservedByteArrayOutputStream();
	
	
	
	private StatusBar statusbar;
	
	
	private File previouslyLoadedModelFile = null;
	
	private SimulationStateData actLoadedSimulationStateData = null;
	
	
	
	private EpisimMenuBarFactory menuBarFactory;
	
	
	private boolean updateAvailable=false;

	
	
	public EpisimSimulator() {
		
		if(ModeServer.guiMode()){
			mainFrame = new JFrame();	
			
			mainFrame.setIconImage(new ImageIcon(ImageLoader.class.getResource("icon_old.gif")).getImage());
	
			
			ExceptionDisplayer.getInstance().registerParentComp(mainFrame);
			
			EpisimUpdater updater = new EpisimUpdater();
			EpisimUpdateState state = null;
			try{
				state = updater.checkForUpdates();
			}
			catch (IOException e){
				if(!(e instanceof UnknownHostException)){
					ExceptionDisplayer.getInstance().displayException(e);
				}
			}
			this.updateAvailable = (state==EpisimUpdateState.POSSIBLE);
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
			tissueExportFileChoose = new ExtendedFileChooser(SimulationStateFile.FILEEXTENSION);
			mainFrame.setTitle(EpisimSimulator.getEpisimSimulatorTitle());			
		}
		else{
			noGUIModeMainPanel.setLayout(new BorderLayout());
			noGUIModeMainPanel.setBackground(Color.LIGHT_GRAY);			
			noGUIModeMainPanel.add(statusbar, BorderLayout.SOUTH);
		}		
		
		//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		if(!updateAvailable) loadPresetFiles();
		
		//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		//        TODO: to be changed for video recording
		//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		if(ModeServer.guiMode()){
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			mainFrame.setPreferredSize(new Dimension((int) (dim.getWidth()*MAINFRAME_WIDTH_FACT),
					(int) (dim.getHeight()*MAINFRAME_HEIGHT_FACT)));
			
			
		//	this.setPreferredSize(new Dimension(1280, 932));
			
			mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
	
				public void windowClosing(WindowEvent e) {
					if(ModelController.getInstance().isModelOpened()){
						int choice = JOptionPane.showConfirmDialog(mainFrame, "Do you really want to close the opened model?", "Close Model?", JOptionPane.YES_NO_OPTION);
						if(choice == JOptionPane.OK_OPTION){
							closeModel();	
							close(0);
						}
					}
					else close(0);
	
				}
			});
			mainFrame.pack();
			centerMe(mainFrame);
			mainFrame.setVisible(true);
			if(updateAvailable){
				EpisimUpdateDialog updateDialog = new EpisimUpdateDialog(mainFrame);
				updateDialog.showUpdateDialog(new UpdateCancelledCallback(){				
					public void updateWasCancelled() {				
						loadPresetFiles();
						autoStartSimulation();					
					}
				});
			}
		}
		else{
			noGUIModeMainPanel.setPreferredSize(new Dimension(1900, 1200));		
			noGUIModeMainPanel.setVisible(true);
		}
		if(!updateAvailable) autoStartSimulation();
	}
	private void loadPresetFiles(){
		if(ModeServer.consoleInput()){
					
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SNAPSHOT_PATH_PROP) != null){
						File snapshotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SNAPSHOT_PATH_PROP));
						if(snapshotPath.isDirectory()){
							snapshotPath = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATOR_SNAPSHOT_PATH_PROP, "EpisimSnapshot", "xml");
						}
						setTissueExportPath(snapshotPath, false);
					}
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELL_BEHAVIORAL_MODEL_PATH_PROP) != null){
						File cellbehavioralModelFile = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELL_BEHAVIORAL_MODEL_PATH_PROP));
						if(!cellbehavioralModelFile.exists() || !cellbehavioralModelFile.isFile()) throw new PropertyException("No existing Cell Behavioral Model File specified: "+cellbehavioralModelFile.getAbsolutePath());
						else{
							openModel(cellbehavioralModelFile, null);
							//TODO: implement the case that a snapshot path is set
							
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
										this.getStatusbar().setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
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
	}
	private void autoStartSimulation(){
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
												long waitingTimeInMs=100000;
												if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_WAITINGTIME_BEFORE_SHUTDOWN_IN_MS) != null){
													try{
														waitingTimeInMs = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_WAITINGTIME_BEFORE_SHUTDOWN_IN_MS));
													}
													catch(NumberFormatException e){}
												}
												
	                                 Thread.sleep(waitingTimeInMs);
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
		
		if(args.length >= 1 && args[0].equals(EpisimSimulator.HELP)) onlyHelpWanted = true;
		else{
		
			for(int i = 0; i < args.length; i++){
				if(args[i].equals(EpisimSimulator.BM_FILE_PARAM_PREFIX) 
						|| args[i].equals(EpisimSimulator.CB_FILE_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.SIM_ID_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.M_FILE_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.DATA_EXPORT_FOLDER_PARAM_PREFIX)){
					
					if((i+1) >= args.length) throw new PropertyException("Missing value after parameter: "+ args[i]);
					if(args[i].equals(EpisimSimulator.BM_FILE_PARAM_PREFIX) 
						|| args[i].equals(EpisimSimulator.CB_FILE_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.M_FILE_PARAM_PREFIX)){
						File path = new File(args[i+1]);
						
						if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to a property file for parameter " + args[i]);
						
						if(args[i].equals(EpisimSimulator.BM_FILE_PARAM_PREFIX)){
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP, path.getAbsolutePath());
						}
						else if(args[i].equals(EpisimSimulator.CB_FILE_PARAM_PREFIX)){
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP, path.getAbsolutePath());
						}
						else if(args[i].equals(EpisimSimulator.M_FILE_PARAM_PREFIX)){
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP, path.getAbsolutePath());
						}
					}
					else if(args[i].equals(EpisimSimulator.DATA_EXPORT_FOLDER_PARAM_PREFIX)){
							File path = new File(args[i+1]);							
							if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to an existing folder" + args[i]);						
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_DATAEXPORT_CSV_OVERRIDE_FOLDER, path.getAbsolutePath());							
					}
					else if(args[i].equals(EpisimSimulator.SIM_ID_PARAM_PREFIX)){
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
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
		      	  		EpisimSimulator episim = new EpisimSimulator();
		       
		}
		else printHelpTextOnConsole();
		
	}
	
	public static String getEpisimSimulatorTitle(){
		StringBuffer title = new StringBuffer();
		title.append(EpisimSimulator.SIMULATOR_TITLE);
		if(ModelController.getInstance().isModelOpened()){
			title.append("- ");
			title.append(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile().getName());
			title.append(" ");
		}
		return title.toString();
	}
	
	public static void printHelpTextOnConsole(){
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------- EPISIM Simulator Help -------------------------\n\n");
		sb.append("The EPISIM Simulator supports the following input parameters:\n");
		sb.append("\t[-bm path] to the biomedical model parameters file\n");
		sb.append("\t[-cb path] to the cell behavioral model parameters file\n");
		sb.append("\t[-mp path] to the miscellaneous parameters file\n");
		sb.append("\t[-id identifier] of the current simulation run\n");
		sb.append("\t[-ef path] of the data export folder used to override the originally defined one\n");
		System.out.println(sb.toString());
	}
	
	
	protected boolean openModel(File modelFile, SimulationStateData simulationStateData){
		boolean success = false; 
		ModelController.getInstance().setSimulationStartedOnce(false);		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		
		
		
		if(ModeServer.guiMode()){
			jarFileChoose.setDialogTitle("Open Episim Cell Behavioral Model");
			
		}		
		
		
		if(((modelFile != null && modelFile.exists()) || (jarFileChoose.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION && ModeServer.guiMode()))){
			if(modelFile == null || !modelFile.exists()) modelFile = jarFileChoose.getSelectedFile();
			
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
				this.previouslyLoadedModelFile = modelFile;
				actLoadedSimulationStateData = simulationStateData;
				if(simulationStateData == null)ModelController.getInstance().standardInitializationOfModels();
				else{
					initializeGlobalObjects(simulationStateData);
					ModelController.getInstance().initializeModels(simulationStateData);
				}
				ChartController.getInstance().rebuildDefaultCharts();
				cleanUpContentPane();
				if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
				else epiUI = new EpisimGUIState(noGUIModeMainPanel);
				registerSimulationStateListeners(epiUI);
				epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
				if(actLoadedSimulationStateData != null)  SimStateServer.getInstance().setSimStepNumberAtStart(actLoadedSimulationStateData.getSimStepNumber());
				ModelController.getInstance().setModelOpened(true);
				
				if(ModeServer.guiMode()){
					mainFrame.validate();
					mainFrame.repaint();
					mainFrame.setTitle(getEpisimSimulatorTitle());
				}
				else{
					noGUIModeMainPanel.validate();
					noGUIModeMainPanel.repaint();
				}				
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(false);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
				menuBarFactory.getEpisimMenuItem(EpisimMenuItem.UPDATE_EPISIM_SIMULATOR).setEnabled(false);
				menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
				menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
				
			}

		}
		return success;
	}
	
	private void initializeGlobalObjects(SimulationStateData simulationStateData){
		EpisimCellBehavioralModelGlobalParameters globalBehave = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		EpisimBiomechanicalModelGlobalParameters globalMech = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(simulationStateData.getEpisimBioMechanicalModelGlobalParameters() != null)simulationStateData.getEpisimBioMechanicalModelGlobalParameters().copyValuesToTarget(globalMech);
		if(simulationStateData.getEpisimCellBehavioralModelGlobalParameters() != null)simulationStateData.getEpisimCellBehavioralModelGlobalParameters().copyValuesToTarget(globalBehave);
		if(simulationStateData.getMiscalleneousGlobalParameters() != null)simulationStateData.getMiscalleneousGlobalParameters().copyValuesToTarget(MiscalleneousGlobalParameters.getInstance());
		if(simulationStateData.getTissueBorder() != null)simulationStateData.getTissueBorder().copyValuesToTarget(TissueController.getInstance().getTissueBorder());
	}
	
	
	protected void openModel(){
		openModel(null, null);
	}
	
	
	protected void reloadModel(File modelFile, File snapshotPath){
		
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
			 
			if(actLoadedSimulationStateData == null)ModelController.getInstance().standardInitializationOfModels();
			else{ 
				initializeGlobalObjects(actLoadedSimulationStateData);
				ModelController.getInstance().initializeModels(actLoadedSimulationStateData);
			}
			setTissueExportPath(snapshotPath, true);			
			ChartController.getInstance().rebuildDefaultCharts();
			cleanUpContentPane();
			if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
			else epiUI = new EpisimGUIState(noGUIModeMainPanel);
			
			registerSimulationStateListeners(epiUI);
			epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
			if(actLoadedSimulationStateData != null)  SimStateServer.getInstance().setSimStepNumberAtStart(actLoadedSimulationStateData.getSimStepNumber());			
			if(ChartController.getInstance().isAlreadyChartSetLoaded() && GlobalClassLoader.getInstance().getMode().equals(GlobalClassLoader.IGNORECHARTSETMODE)){
				ChartController.getInstance().reloadCurrentlyLoadedChartSet();
				GlobalClassLoader.getInstance().resetMode();
			}
			if(DataExportController.getInstance().isAlreadyDataExportSetLoaded() && GlobalClassLoader.getInstance().getMode().equals(GlobalClassLoader.IGNOREDATAEXPORTMODE)){
				DataExportController.getInstance().reloadCurrentlyLoadedDataExportDefinitionSet();
				GlobalClassLoader.getInstance().resetMode();
			}
			ModelController.getInstance().setModelOpened(true);
			if(ModeServer.guiMode()){
				mainFrame.validate();
				mainFrame.repaint();
				mainFrame.setTitle(getEpisimSimulatorTitle());
			}
			else{
				noGUIModeMainPanel.validate();
				noGUIModeMainPanel.repaint();
			}			
		}
	}
	
	public void reloadCurrentlyLoadedModel(){
		 if(this.previouslyLoadedModelFile != null){
	   	
	   		File snapshotPath =SimulationStateFile.getTissueExportPath();    
	      	SimulationStateData simulationStateData = actLoadedSimulationStateData;
	      	File currentlyLoadedChartSet = null;
	      	File currentlyLoadedDataExportDefinitionSet= null;
	      	File loadedModelFile = this.previouslyLoadedModelFile;
	      	if(ChartController.getInstance().isAlreadyChartSetLoaded()){
	      		currentlyLoadedChartSet = ChartController.getInstance().getCurrentlyLoadedChartsetFile();   				
	      	}
	      	if(DataExportController.getInstance().isAlreadyDataExportSetLoaded()){
 				currentlyLoadedDataExportDefinitionSet=DataExportController.getInstance().getCurrentlyLoadedDataExportDefinitionSet();   				
	      	}   	
	      	
	      	closeModel();
	      	
	      	GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	   		boolean success = false; 
	   		try{
	            success= ModelController.getInstance().loadCellBehavioralModelFile(loadedModelFile);
	         }
	         catch(ModelCompatibilityException e){
	           ExceptionDisplayer.getInstance().displayException(e);
	           if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
	           success = false;
	         }
	   		
	   		if(success){
	   			this.previouslyLoadedModelFile = loadedModelFile;
					actLoadedSimulationStateData = simulationStateData;
	   			if(simulationStateData == null)ModelController.getInstance().standardInitializationOfModels();
	   			else{ 
	   				initializeGlobalObjects(simulationStateData);
	   				ModelController.getInstance().initializeModels(simulationStateData);
	   			}
	   			setTissueExportPath(snapshotPath, true);			
	   			ChartController.getInstance().rebuildDefaultCharts();
	   			cleanUpContentPane();
	   			if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
	   			else epiUI = new EpisimGUIState(noGUIModeMainPanel);
	   			
	   			registerSimulationStateListeners(epiUI);
	   			epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
	   			if(simulationStateData != null)  SimStateServer.getInstance().setSimStepNumberAtStart(simulationStateData.getSimStepNumber());			
	   			if(currentlyLoadedChartSet != null){
	   				ChartController.getInstance().loadChartSet(currentlyLoadedChartSet);	   			
	   			}
	   			if(currentlyLoadedDataExportDefinitionSet !=null){
	   				DataExportController.getInstance().loadDataExportDefinition(currentlyLoadedDataExportDefinitionSet);	   				
	   			}
	   			ModelController.getInstance().setModelOpened(true);
	   			if(ModeServer.guiMode()){
	   				mainFrame.validate();
	   				mainFrame.repaint();
	   				mainFrame.setTitle(getEpisimSimulatorTitle());
	   			}
	   			else{
	   				noGUIModeMainPanel.validate();
	   				noGUIModeMainPanel.repaint();
	   			} 
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(false);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.UPDATE_EPISIM_SIMULATOR).setEnabled(false);
					menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
	   		}
	   }	   
	}
	
	public void classLoaderHasChanged() {

	   if(this.previouslyLoadedModelFile != null){	   	
	         reloadModel(this.previouslyLoadedModelFile, SimulationStateFile.getTissueExportPath());        
	   }	   
   }
	public void setTissueExportPath(){
		setTissueExportPath(null, false);
		
	}
	protected void setTissueExportPath(File file, boolean modelReload){
		if(file == null && !modelReload){
			tissueExportFileChoose.setDialogTitle("Set Tissue-Export-Path");
			if(ModeServer.guiMode()){
				if(tissueExportFileChoose.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) file = tissueExportFileChoose.getSelectedFile();
			}
			
		}
		if(file != null){
			try{
	        if(ModeServer.guiMode()) mainFrame.setTitle(EpisimSimulator.getEpisimSimulatorTitle()+ "- Tissue-Export-Path: "+file.getCanonicalPath());
         }
         catch (IOException e){
	         ExceptionDisplayer.getInstance().displayException(e);
         }
			SimulationStateFile.setTissueExportPath(file);			 
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
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.BUILD_MODEL_ARCHIVE).setEnabled(true);
		menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(false);
		menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(false);
		
		statusbar.setMessage("Ready");
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
		
		//Menu-Items Info
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.UPDATE_EPISIM_SIMULATOR).setEnabled(true);
		
		
		//never change the order of the following two commands:
		this.previouslyLoadedModelFile = null;
		GlobalClassLoader.getInstance().destroyClassLoader(true);
		
		if(ModeServer.guiMode())mainFrame.setTitle(getEpisimSimulatorTitle());
		
		SimulationStateFile.setTissueExportPath(null);
		this.actLoadedSimulationStateData = null;
	}
	
	private boolean testSimStateAndCBMCompatibility(File cbmFile, SimulationStateData simStateData){
		boolean compatibilityTestResult = true;
		//-----------------------------------------------------------------------------
		// Open Model
		//-----------------------------------------------------------------------------
		ModelController.getInstance().setSimulationStartedOnce(false);		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		boolean success = false;
		try{
         success= ModelController.getInstance().loadCellBehavioralModelFile(cbmFile);
      }
      catch (ModelCompatibilityException e){
        ExceptionDisplayer.getInstance().displayException(e);
       
        success = false;
      }
		if(success){
			ModelController.getInstance().setModelOpened(true);
		
		
			//-----------------------------------------------------------------------------
			// Check Compatibility
			//-----------------------------------------------------------------------------			
			
			try{
				initializeGlobalObjects(simStateData);
				ModelController.getInstance().initializeModels(simStateData);
				TissueController.getInstance().registerTissue(new UniversalTissue(System.currentTimeMillis()));
				compatibilityTestResult = ModelController.getInstance().testCBMFileLoadedSimStateCompatibility();
			}
			catch(Exception e){
				compatibilityTestResult = false;
			}
			
			
			
		}
		//-----------------------------------------------------------------------------
		// Close Model
		//-----------------------------------------------------------------------------
		System.gc();
		
		TissueController.getInstance().resetTissueSettings();
		ChartController.getInstance().modelWasClosed();
		DataExportController.getInstance().modelWasClosed();
		this.previouslyLoadedModelFile = null;
		GlobalClassLoader.getInstance().destroyClassLoader(true);
		
		return compatibilityTestResult;
	}
	
	
	
	
	
	public void simulationWasStarted(){		
		this.menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.WINDOWS_MENU).setEnabled(false);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(false);
	}
	public void simulationWasPaused() {
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
	
	public void close(final int exitCode){
		 SwingUtilities.invokeLater(new Runnable() {

	        public void run() {

					mainFrame.dispose();
					System.exit(exitCode);
	        }
	    });
	}
	
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

	

	
	

	protected void removeAllChartInternalFrames(){
		if(epiUI != null)epiUI.removeAllChartInternalFrames();
	}
	
	protected void setAutoArrangeWindows(boolean autoArrange){
		if(epiUI != null) epiUI.setAutoArrangeWindows(autoArrange);
	}
	
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();	
			
			frame.setLocation(((int)((dim.getWidth()/2)-(frame.getPreferredSize().getWidth()/2))), 
			((int)((dim.getHeight()/2)-(frame.getPreferredSize().getHeight()/2))));
		}
	}

	
   public StatusBar getStatusbar() {   
   	return statusbar;
   }
	
   public Component getMainFrame(){ 
   	return ModeServer.guiMode() ? mainFrame : noGUIModeMainPanel; 
   }

		
	
	
	
	protected void loadSimulationStateFile(File f){
		 
		 try{
            if(f != null){
            	boolean load = true;
            	boolean compatible = true;
            	
            	setTissueExportPath(f, false);
            	final SimulationStateFile simulationStateFile = new SimulationStateFile(f);
					final SimulationStateData simStateData = simulationStateFile.loadData();   
					
					if(simStateData.getLoadedModelFile()==null || !simStateData.getLoadedModelFile().exists()) {
						load = false;						
						if(ModeServer.guiMode()){
							JOptionPane.showMessageDialog(mainFrame, "Cannot find the cell behavioral model file defined in the export:\n" + simStateData.getLoadedModelFile(), "Error", JOptionPane.WARNING_MESSAGE);
							jarFileChoose.setDialogTitle("Load the Corresponding Episim Cell Behavioral Model");
							if(jarFileChoose.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
								File modelFile = jarFileChoose.getSelectedFile();
								if(modelFile != null && modelFile.exists()){
									simStateData.setLoadedModelFile(modelFile.getAbsolutePath());
									SimulationStateFile.setTissueExportPath(f);
									simulationStateFile.saveModelFilePathCorrectedVersion(modelFile);
									load =true;									
								}
							}
						}			
					}
					
					if(load){
						do{
							compatible=testSimStateAndCBMCompatibility(simStateData.getLoadedModelFile(), simStateData);
							if(!compatible){
								if(ModeServer.guiMode()){
									JOptionPane.showMessageDialog(mainFrame, "The loaded cell behavioral model file: " + simStateData.getLoadedModelFile()+" is not compatible with this simulation state.", "Error", JOptionPane.WARNING_MESSAGE);
									jarFileChoose.setDialogTitle("Load the Corresponding Episim Cell Behavioral Model");
									int fileChooserResult = jarFileChoose.showOpenDialog(mainFrame);
									if(fileChooserResult == JFileChooser.APPROVE_OPTION){
										File modelFile = jarFileChoose.getSelectedFile();
										if(modelFile != null && modelFile.exists()){
											simStateData.setLoadedModelFile(modelFile.getAbsolutePath());
											SimulationStateFile.setTissueExportPath(f);
											simulationStateFile.saveModelFilePathCorrectedVersion(modelFile);
											load =true;									
										}
									}
									else if(fileChooserResult == JFileChooser.ABORT || fileChooserResult == JFileChooser.CANCEL_OPTION){
										SimulationStateFile.setTissueExportPath(null);
										this.actLoadedSimulationStateData = null;
										if(ModeServer.guiMode())mainFrame.setTitle(EpisimSimulator.getEpisimSimulatorTitle());
										return;
									}
								}
								else{
									close(-1);
								}
							}
						}while(!compatible);
					}
					
					
					if(load && compatible){ 
						setTissueExportPath(f, false);
						if(ModeServer.guiMode()){
							
							EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() {											
								public void taskHasFinished() {
									
								}
								public void executeTask() {
									boolean success = openModel(simStateData.getLoadedModelFile(), simStateData);
									if(success && epiUI != null){
										epiUI.pressPauseAfterLoadingSimulationState();
									}
									
								}
							};
							EpisimProgressWindow.showProgressWindowForTask(mainFrame, "Load Episim Simulation State...", cb);
						
						}
						else{
							openModel(simStateData.getLoadedModelFile(), simStateData);							
						}
					}
					
            }
        }
        catch (ParserConfigurationException e1){
        		ExceptionDisplayer.getInstance().displayException(e1);
        }
        catch (SAXParseException e1){
        	if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame,"systemId: "+e1.getSystemId()+"; lineNumber: "+e1.getLineNumber()+"; columnNumber: "+e1.getColumnNumber()+"\n"+e1.getMessage());
        	else System.out.println("systemId: "+e1.getSystemId()+"; lineNumber: "+e1.getLineNumber()+"; columnNumber: "+e1.getColumnNumber()+"\n"+e1.getMessage());
        } 
		 catch(SAXException e1){
			 ExceptionDisplayer.getInstance().displayException(e1);
		 }
		  catch (IOException e1) {
			  ExceptionDisplayer.getInstance().displayException(e1);
		  } 
	}	
	
}