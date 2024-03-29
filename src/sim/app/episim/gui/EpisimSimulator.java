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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

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

import com.dropbox.core.DbxException;

import episimexceptions.ModelCompatibilityException;
import episimexceptions.PropertyException;
import episimexceptions.SimulationTriggerException;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimUpdater;
import sim.app.episim.EpisimUpdater.EpisimUpdateCallback;
import sim.app.episim.EpisimUpdater.EpisimUpdateState;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.ModeServer;
import sim.app.episim.SimStateServer;
import sim.app.episim.SimStateChangeListener;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenu;
import sim.app.episim.gui.EpisimMenuBarFactory.EpisimMenuItem;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.gui.EpisimUpdateDialog.UpdateCancelledCallback;
import sim.app.episim.model.AbtractTissue;
import sim.app.episim.model.UniversalTissue;
import sim.app.episim.model.controller.BiomechanicalModelController;
import sim.app.episim.model.controller.CellBehavioralModelController;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.ModelParameterModifier;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionFieldBCConfigRW;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.tissue.TissueServer;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.SimulationStateFile;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.EpisimUpdateDialogText;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.ObservedByteArrayOutputStream;
import sim.app.episim.util.SimulationTrigger;
import sim.app.episim.util.SimulationTriggerFileReader;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class EpisimSimulator implements SimStateChangeListener, ClassLoaderChangeListener{
	
	public static final String versionID = "1.5.2.2.1";

	
	private static final String SIMULATOR_TITLE = "EPISIM Simulator v. "+ versionID+" ";
	
	private static final String CB_FILE_PARAM_PREFIX = "-cb";
	private static final String BM_FILE_PARAM_PREFIX = "-bm";
	private static final String M_FILE_PARAM_PREFIX = "-mp";
	private static final String SIM_ID_PARAM_PREFIX = "-id";
	private static final String CONFIGURATION_FILE_PATH_PREFIX = "-cf";
	private static final String TRIGGER_FILE_PATH_PREFIX = "-tf";
	private static final String TISSUE_SIMULATION_SNAPSHOT_FILE_PATH_PREFIX = "-ts";
	private static final String DATA_EXPORT_FOLDER_PARAM_PREFIX = "-ef";
	private static final String IMAGE_EXPORT_FOLDER_PARAM_PREFIX = "-ie";
	private static final String UDATE_SIMULATOR = "-update";
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

	private boolean dataExtractionFromSimulationSnapshotMode = false;
	
	public EpisimSimulator() {
		
		if(ModeServer.guiMode()){
			
			mainFrame = new JFrame();			
			
			List<Image> icons = new ArrayList<Image>();
			icons.add(new ImageIcon(ImageLoader.class.getResource("episim_16_16.png")).getImage());
			icons.add(new ImageIcon(ImageLoader.class.getResource("episim_32_32.png")).getImage());
			icons.add(new ImageIcon(ImageLoader.class.getResource("episim_48_48.png")).getImage());
			
			mainFrame.setIconImages(icons);
						
			EpisimExceptionHandler.getInstance().registerParentComp(mainFrame);
			
			EpisimUpdater updater = new EpisimUpdater();
			EpisimUpdateState state = null;
			
			try{
				state = updater.checkForUpdates();
			}
			catch (IOException e){
				if(!(e instanceof UnknownHostException)){
					EpisimExceptionHandler.getInstance().displayException(e);
				}
			}
         catch (DbxException e){
         	EpisimExceptionHandler.getInstance().displayException(e);
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
				
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}
		
		//--------------------------------------------------------------------------------------------------------------
		//Men�
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
			if(System.getProperty("os.name").toLowerCase().contains("windows"))checkForCOPASIInstallation();
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
					File tissueSnapShotFileToBeLoaded = null;
					File cellbehavioralModelFile = null;
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_LOAD_PATH_PROP) != null){
						tissueSnapShotFileToBeLoaded = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_LOAD_PATH_PROP));
						if(!tissueSnapShotFileToBeLoaded.exists() || !tissueSnapShotFileToBeLoaded.isFile()){	
							String path = tissueSnapShotFileToBeLoaded.getAbsolutePath();
							tissueSnapShotFileToBeLoaded=null;
							throw new PropertyException("No existing Tissue Simulation Snaphot File specified: "+path);							
						}
					}
					else{
						if(EpisimProperties.getProperty(EpisimProperties.MODEL_CELL_BEHAVIORAL_MODEL_PATH_PROP) != null){					
							cellbehavioralModelFile = new File(EpisimProperties.getProperty(EpisimProperties.MODEL_CELL_BEHAVIORAL_MODEL_PATH_PROP));
							if(!cellbehavioralModelFile.exists() || !cellbehavioralModelFile.isFile()){
								String path =cellbehavioralModelFile.getAbsolutePath();
								cellbehavioralModelFile=null;
								throw new PropertyException("No existing Cell Behavioral Model File defined: "+path);
								
							}
						}
						if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_STORAGE_PATH_PROP) != null
								&& tissueSnapShotFileToBeLoaded==null){
							File snapshotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_STORAGE_PATH_PROP));
							if(snapshotPath.isDirectory()){
								snapshotPath = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATION_SNAPSHOT_STORAGE_PATH_PROP, "EpisimSnapshot", "xml");
							}
							setTissueExportPath(snapshotPath, false);
						}
					}
					if(cellbehavioralModelFile != null || tissueSnapShotFileToBeLoaded != null){
						
							if(tissueSnapShotFileToBeLoaded != null)loadSimulationStateFile(tissueSnapShotFileToBeLoaded,false,false);
							else if(cellbehavioralModelFile != null) openModel(cellbehavioralModelFile, null, false);				
							if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTSETPATH) != null){
								File chartSetFile = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTSETPATH));
								if(!chartSetFile.exists() || !chartSetFile.isFile()) throw new PropertyException("No existing Chart-Set File defined: "+chartSetFile.getAbsolutePath());
								else{
									boolean loadSuccess = false;
									if(ModeServer.guiMode()) loadSuccess = ChartController.getInstance().loadChartSet(chartSetFile, mainFrame);
									else loadSuccess = ChartController.getInstance().loadChartSet(chartSetFile);
									if(ModeServer.guiMode()){
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(!loadSuccess);
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(!loadSuccess);
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(loadSuccess);
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(loadSuccess);
									}
								}
							}
							if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTPATH) != null){
								File dataExportDefinitionFile = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DATAEXPORTPATH));
								if(!dataExportDefinitionFile.exists() || !dataExportDefinitionFile.isFile()) throw new PropertyException("No existing Data Export Definition File defined: "+dataExportDefinitionFile.getAbsolutePath());
								else{
									boolean loadSuccess = false;
									if(ModeServer.guiMode()) loadSuccess = DataExportController.getInstance().loadDataExportDefinition(dataExportDefinitionFile, mainFrame);
									else loadSuccess = DataExportController.getInstance().loadDataExportDefinition(dataExportDefinitionFile);
									if(ModeServer.guiMode()){
										this.getStatusbar().setMessage("Loaded Data Export: "+ DataExportController.getInstance().getActLoadedDataExportsName());
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(!loadSuccess);
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(!loadSuccess);
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(loadSuccess);
										menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(loadSuccess);
									}
								}
							}					
						
					}
				
	}
	private void autoStartSimulation(){
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_MAX_STEPS_PROP) != null){
				long steps = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATION_MAX_STEPS_PROP));
				if(epiUI != null && steps > 0) epiUI.setMaxSimulationSteps(steps);
			}			
			if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_AUTOSTART_AND_STOP_PROP) != null && 
					EpisimProperties.getProperty(EpisimProperties.SIMULATION_AUTOSTART_AND_STOP_PROP).equals(EpisimProperties.ON)){
				if(epiUI != null){ 
					Runnable r  = new Runnable(){

						public void run() {

							epiUI.startSimulation();
							if(!ModeServer.guiMode()){
								System.out.print(" ---------------------------------------------");
								for(int i =0; i< EpisimSimulator.versionID.length(); i++)System.out.print("-");
								System.out.print("\n");
								System.out.println("|              EPISIM SIMULATOR (v. "+EpisimSimulator.versionID+")         |");
								System.out.print(" ---------------------------------------------");
								for(int i =0; i< EpisimSimulator.versionID.length(); i++)System.out.print("-");
								System.out.print("\n\n");
								System.out.println("------------Simulation Started------------");
							}
							epiUI.scheduleAtEnd(new Steppable(){

								public void step(SimState state) {
									 Runnable runnable = new Runnable(){

										public void run() {
											try{
												long waitingTimeInMs=100000;
												if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SHUTDOWN_WAIT_MS) != null){
													try{
														waitingTimeInMs = Long.parseLong(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SHUTDOWN_WAIT_MS));
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
	                                 EpisimExceptionHandler.getInstance().displayException(e);
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
	
	private void checkForCOPASIInstallation(){
		 Map<String, String> env = System.getenv();
		 boolean copasiDetected = false;
		 String copasiKey = "";
	    for (String envName : env.keySet()) {
	      	if(envName.toLowerCase().contains("copasidir")){
	      		copasiKey = envName;
	      		copasiDetected=true;
	      		break;
	      	}	      	
	    }
//	    if(copasiDetected){
//	   	 if(env.get(copasiKey) != null && new File(env.get(copasiKey)).exists()){
//	   		 JOptionPane.showMessageDialog(this.mainFrame, "EPISIM Simulator detected that you have installed COPASI\non your Windows OS:\n\n"+env.get(copasiKey)+"\n\nCOPASI installations with version numbers higher than 4.8.35\nlead to an EPISIM Simulator crash for EPISIM models containing\nan SBML-based submodel.\nEPISIM Simulator will patch the file EpisimSimulator.exe.\nMake sure that your computer has internet connection, and press ok.\nEPISIM Simulator will shut down. Restart it manually.\nWe are sorry for this inconvenience.", "COPASI installation detected", JOptionPane.WARNING_MESSAGE);
//	   		 installEpisimEXEPatch();
//	   	 }
	    }
//	}
	
	private void installEpisimEXEPatch(){
		final EpisimUpdater updater = new EpisimUpdater();
		try{
	      updater.downloadEXEPatch(new EpisimUpdateCallback(){      	
	      	public void updateHasFinished() {	      	
	      		
	               try{
	                  updater.installEXEPatch(new EpisimUpdateCallback() {						
	                  	
	                  	public void updateHasFinished() {
	                  		System.exit(0);
	                  	}
	                  	
	                  	@Override
	                  	public void sizeOfUpdate(int size) {}
	                  	
	                  	
	                  	public void progressOfUpdate(int progress) {}
	                  }, true);
                  }
                  catch (IOException | URISyntaxException e){
                  	EpisimExceptionHandler.getInstance().displayException(e);             	
                  }
              	
	      	}      	
	      	public void sizeOfUpdate(int size) {			
	      		System.out.println("EXE-Path size: "+ size+ "bytes");				
	      	}      	
	      	public void progressOfUpdate(int progress) {		
	      		
	      	}
	      }, true);
      }
      catch (IOException e){
	     EpisimExceptionHandler.getInstance().displayException(e);
      }
		catch (DbxException e){
      	EpisimExceptionHandler.getInstance().displayException(e);
      }
	}
	
	public static void main(String[] args){
		boolean onlyHelpWanted = false;
		boolean onlyUpdate = false;
		if(args.length >= 1 && args[0].equals(EpisimSimulator.HELP)) onlyHelpWanted = true;
		if(args.length >= 1 && args[0].equals(EpisimSimulator.UDATE_SIMULATOR)){
			if(!ModeServer.guiMode()){
				onlyUpdate = true;
				EpisimUpdateDialogText updateText = new EpisimUpdateDialogText();
				updateText.showUpdateTextDialog();
				System.exit(0);
			}	
		}
		else{
			
			//check for config file property
			for(int i = 0; i < args.length; i++){
				if(args[i].equals(EpisimSimulator.CONFIGURATION_FILE_PATH_PREFIX)){
					if((i+1) >= args.length) throw new PropertyException("Missing value after parameter: "+ args[i]);
					if(EpisimProperties.loadCustomConfigPropertiesFile(args[i+1])){
						System.out.println("Custom EPISIM Simulator Configuration File successfully loaded: "+args[i+1]);
					}
					else System.out.println("Custom EPISIM Simulator Configuration File cannot be loaded: "+args[i+1]+"\nDefault Configuration File loaded!");
				}
			}
		
			for(int i = 0; i < args.length; i++){
				if(args[i].equals(EpisimSimulator.BM_FILE_PARAM_PREFIX) 
						|| args[i].equals(EpisimSimulator.CB_FILE_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.SIM_ID_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.M_FILE_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.DATA_EXPORT_FOLDER_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.TRIGGER_FILE_PATH_PREFIX)
						|| args[i].equals(EpisimSimulator.IMAGE_EXPORT_FOLDER_PARAM_PREFIX)
						|| args[i].equals(EpisimSimulator.TISSUE_SIMULATION_SNAPSHOT_FILE_PATH_PREFIX)){
					
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
					else if(args[i].equals(EpisimSimulator.TRIGGER_FILE_PATH_PREFIX)){
						File path = new File(args[i+1]);							
						if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to an existing file" + args[i]);						
						EpisimProperties.setProperty(EpisimProperties.SIMULATION_TRIGGER_FILE_PATH_PROP, path.getAbsolutePath());							
					}
					else if(args[i].equals(EpisimSimulator.TISSUE_SIMULATION_SNAPSHOT_FILE_PATH_PREFIX)){
						File path = new File(args[i+1]);							
						if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to an existing file" + args[i]);						
						EpisimProperties.setProperty(EpisimProperties.SIMULATION_SNAPSHOT_LOAD_PATH_PROP, path.getAbsolutePath());							
					}
					else if(args[i].equals(EpisimSimulator.DATA_EXPORT_FOLDER_PARAM_PREFIX)){
							File path = new File(args[i+1]);							
							if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to an existing folder" + args[i]);						
							EpisimProperties.setProperty(EpisimProperties.SIMULATOR_DATAEXPORT_CSV_OVERRIDE_FOLDER, path.getAbsolutePath());							
					}
					else if(args[i].equals(EpisimSimulator.IMAGE_EXPORT_FOLDER_PARAM_PREFIX)){
						File path = new File(args[i+1]);							
						if(!path.exists() || !path.isDirectory()) new PropertyException("Path: " + args[i+1] + " doesn't point to an existing folder" + args[i]);						
						EpisimProperties.setProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH, path.getAbsolutePath());
						EpisimProperties.setProperty(EpisimProperties.SIMULATION_PNG_PATH, path.getAbsolutePath());		
					}					
					else if(args[i].equals(EpisimSimulator.SIM_ID_PARAM_PREFIX)){
						EpisimProperties.setProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID, args[i+1].trim());
					}
				}			
			}
		}
		String mode;
		if((mode=EpisimProperties.getProperty(EpisimProperties.EXCEPTION_OUTPUT_PROP)) != null 
				&& mode.equals(EpisimProperties.SIMULATOR))  System.setErr(new PrintStream(errorOutputStream));
		
		if((mode=EpisimProperties.getProperty(EpisimProperties.STANDARD_OUTPUT)) != null 
				&& mode.equals(EpisimProperties.SIMULATOR))  System.setOut(new PrintStream(standardOutputStream));
		
		if(!onlyHelpWanted){
			//if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP) != null 
				//	&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_GUI_PROP).equals(EpisimProperties.OFF_SIMULATOR_GUI_VAL))
				//System.setProperty("java.awt.headless", "true");
			if(!onlyUpdate){
				System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
				SwingUtilities.invokeLater(new Runnable() {
			      @Override
			      public void run() {
			      		final EpisimSimulator episim = new EpisimSimulator();
			      }
			     });
			}
		       
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
		sb.append("------------------------- EPISIM Simulator (v. "+EpisimSimulator.versionID+") Help -------------------------\n\n");
		sb.append("The EPISIM Simulator supports the following input parameters:\n");
		sb.append("\t[-update] check for updates at startup\n");
		sb.append("\t[-bm path] to the biomedical model parameters file\n");
		sb.append("\t[-cb path] to the cell behavioral model parameters file\n");
		sb.append("\t[-mp path] to the miscellaneous parameters file\n");
		sb.append("\t[-id identifier] of the current simulation run\n");
		sb.append("\t[-ef path] of the data export folder used to override the originally defined one\n");
		sb.append("\t[-ie path] of the image export folder used to override the originally defined one\n");
		sb.append("\t[-cf path] of the custom EPISIM Simulator configuration file to be used instead of the default one\n");
		sb.append("\t[-tf path] of the file containing the triggers for global simulation parameter changes\n");
		sb.append("\t[-ts path] of the EPISIM Tissue Simulation Snaphot file to be loaded after startup\n");
		System.out.println(sb.toString());
	}
	
	
	protected boolean openModel(File modelFile, SimulationStateData simulationStateData, boolean snapshotDataExportLoad){
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
	        EpisimExceptionHandler.getInstance().displayException(e);
	        if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
	        success = false;
         }
			catch(NoClassDefFoundError e2){
				if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame, "The model file cannot be opened. Re-compilation of the graphical model with the latest version of EPISIM Modeller might solve the problem", "Model-File-Error", JOptionPane.ERROR_MESSAGE);
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
				loadSimulationTrigger();
				ChartController.getInstance().rebuildDefaultCharts();
				cleanUpContentPane();
				
				if(!snapshotDataExportLoad){
					if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
					else epiUI = new EpisimGUIState(noGUIModeMainPanel);
					registerSimulationStateListeners(epiUI);
					epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
				}
				
				if(actLoadedSimulationStateData != null)  SimStateServer.getInstance().setSimStepNumberAtStart(actLoadedSimulationStateData.getSimStepNumber());
				ModelController.getInstance().setModelOpened(true);
				
				if(ModeServer.guiMode()){
					mainFrame.validate();
					mainFrame.repaint();
					if(!snapshotDataExportLoad)mainFrame.setTitle(getEpisimSimulatorTitle());
				}
				else{
					noGUIModeMainPanel.validate();
					noGUIModeMainPanel.repaint();
				}
				if(!snapshotDataExportLoad){
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.SET_SNAPSHOT_PATH).setEnabled(true);
									
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.UPDATE_EPISIM_SIMULATOR).setEnabled(false);
					menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.DATA_EXPORT_SIMULATION_SNAPSHOT).setEnabled(false);
				}
			}

		}
		return success;
	}
	
	private void loadSimulationTrigger(){
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_TRIGGER_FILE_PATH_PROP)!= null){
			SimulationTriggerFileReader fileReader = new SimulationTriggerFileReader(new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_TRIGGER_FILE_PATH_PROP)));
			try{
				ArrayList<SimulationTrigger> triggerList = fileReader.getSimulationTrigger();
				SimStateServer.getInstance().registerSimulationTrigger(triggerList);
			}
			catch(SimulationTriggerException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}
	}
	
	private void initializeGlobalObjects(SimulationStateData simulationStateData){
		EpisimCellBehavioralModelGlobalParameters globalBehave = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
		EpisimBiomechanicalModelGlobalParameters globalMech = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(simulationStateData.getEpisimBioMechanicalModelGlobalParameters() != null) simulationStateData.getEpisimBioMechanicalModelGlobalParameters().copyValuesToTarget(globalMech);
		if(simulationStateData.getEpisimCellBehavioralModelGlobalParameters() != null) simulationStateData.getEpisimCellBehavioralModelGlobalParameters().copyValuesToTarget(globalBehave);
		if(simulationStateData.getMiscalleneousGlobalParameters() != null) simulationStateData.getMiscalleneousGlobalParameters().copyValuesToTarget(MiscalleneousGlobalParameters.getInstance());
		if(simulationStateData.getTissueBorder() != null) simulationStateData.getTissueBorder().copyValuesToTarget(TissueController.getInstance().getTissueBorder());
		
		ModelParameterModifier parameterModifier = new ModelParameterModifier();
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
				parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(globalMech, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP)));
		}
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP) != null){
			parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(globalBehave, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP)));
		}   			
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP) != null){   				
			parameterModifier.setGlobalModelPropertiesToValuesInPropertiesFile(MiscalleneousGlobalParameters.getInstance()
					, new File(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_MISCPARAMETERSFILE_PROP)));
		}
	}
	
	
	protected void openModel(){
		openModel(null, null, false);
	}
	
	
	protected void reloadModel(File modelFile, File snapshotPath){
		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		boolean success = false; 
		try{
         success= ModelController.getInstance().loadCellBehavioralModelFile(modelFile);
      }
      catch(ModelCompatibilityException e){
        EpisimExceptionHandler.getInstance().displayException(e);
        if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Model-File-Error", JOptionPane.ERROR_MESSAGE);
        success = false;
      }
		
		if(success){
			 
			if(actLoadedSimulationStateData == null)ModelController.getInstance().standardInitializationOfModels();
			else{ 
				initializeGlobalObjects(actLoadedSimulationStateData);
				ModelController.getInstance().initializeModels(actLoadedSimulationStateData);
			}
			SimStateServer.getInstance().removeAllSimulationTrigger();
			loadSimulationTrigger();
			setTissueExportPath(snapshotPath, true);			
			ChartController.getInstance().rebuildDefaultCharts();
			
			if(!dataExtractionFromSimulationSnapshotMode){
				cleanUpContentPane();
				if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
				else epiUI = new EpisimGUIState(noGUIModeMainPanel);			
				registerSimulationStateListeners(epiUI);
				epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
			}
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
	      	
	      	closeModel(false, false);
	      	
	      	GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
	   		boolean success = false; 
	   		try{
	            success= ModelController.getInstance().loadCellBehavioralModelFile(loadedModelFile);
	         }
	         catch(ModelCompatibilityException e){
	           EpisimExceptionHandler.getInstance().displayException(e);
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
	   			
	   			loadSimulationTrigger();
	   			setTissueExportPath(snapshotPath, true);			
	   			ChartController.getInstance().rebuildDefaultCharts();
	   			cleanUpContentPane();
	   			if(ModeServer.guiMode())epiUI = new EpisimGUIState(mainFrame);
	   			else epiUI = new EpisimGUIState(noGUIModeMainPanel);
	   			
	   			registerSimulationStateListeners(epiUI);
	   			epiUI.setAutoArrangeWindows(menuBarFactory.getEpisimMenuItem(EpisimMenuItem.AUTO_ARRANGE_WINDOWS).isSelected());
	   			if(simulationStateData != null)  SimStateServer.getInstance().setSimStepNumberAtStart(simulationStateData.getSimStepNumber());			
	   			boolean chartSetloaded = currentlyLoadedChartSet != null;
	   			if(chartSetloaded){
	   				boolean reloadSuccess = false;
	   				if(ModeServer.guiMode()){
	   					reloadSuccess = ChartController.getInstance().loadChartSet(currentlyLoadedChartSet, mainFrame);
	   				}
	   				else{
	   					reloadSuccess = ChartController.getInstance().loadChartSet(currentlyLoadedChartSet);
	   				}
	   				if(!reloadSuccess) ChartController.getInstance().closeActLoadedChartSet();
	   				chartSetloaded = reloadSuccess;
	   			}
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_CHART_SET).setEnabled(chartSetloaded);
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_CHART_SET).setEnabled(chartSetloaded);
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_CHART_SET).setEnabled(!chartSetloaded);
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_CHART_SET).setEnabled(!chartSetloaded);
	   			
	   			boolean dataExportLoaded = currentlyLoadedDataExportDefinitionSet !=null;
	   			if(dataExportLoaded){
	   				boolean reloadSuccess = false;
	   				if(ModeServer.guiMode()){
	   					reloadSuccess = DataExportController.getInstance().loadDataExportDefinition(currentlyLoadedDataExportDefinitionSet, mainFrame);
	   				}
	   				else{
	   					reloadSuccess = DataExportController.getInstance().loadDataExportDefinition(currentlyLoadedDataExportDefinitionSet);
	   				}
	   				if(!reloadSuccess) DataExportController.getInstance().closeActLoadedDataExportDefinitonSet();
	   				dataExportLoaded = reloadSuccess;
	   			}
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.EDIT_DATA_EXPORT).setEnabled(dataExportLoaded);
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(dataExportLoaded);
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(!dataExportLoaded);
	   			menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(!dataExportLoaded);
	   			
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
					
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.UPDATE_EPISIM_SIMULATOR).setEnabled(false);
					menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);
					menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
					menuBarFactory.getEpisimMenuItem(EpisimMenuItem.DATA_EXPORT_SIMULATION_SNAPSHOT).setEnabled(false);
					
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
	         EpisimExceptionHandler.getInstance().displayException(e);
         }
			SimulationStateFile.setTissueExportPath(file);			 
		}		
	}
	
	protected void registerSimulationStateListeners(EpisimGUIState guiState){
		epiUI.addSimulationStateChangeListener(SimStateServer.getInstance());
		
		SimStateServer.getInstance().addSimulationStateChangeListener(this);
		SimStateServer.getInstance().addSimulationStateChangeListener(CellEllipseIntersectionCalculationRegistry.getInstance());
	}
	protected void closeModel(){
		closeModel(true, false);
	}
	
	public void closeModelAfterSnapshotDataExport(){
		closeModel(false, true);
		dataExtractionFromSimulationSnapshotMode = false;
	}
	
	protected void closeModel(boolean storeEcdfBCConfigs, boolean closeAfterSnapshotDataExport){
		if(storeEcdfBCConfigs){
		ExtraCellularDiffusionFieldBCConfigRW configRW = new ExtraCellularDiffusionFieldBCConfigRW(ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile());
			try{
	         configRW.saveBCConfigs(ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularFieldBCConfigurationsMap());
	      }
	      catch (Exception e){
	      	EpisimExceptionHandler.getInstance().displayException(e);
	      }
		}
		if(epiUI != null && !closeAfterSnapshotDataExport){
			
			epiUI.quit();
		}
		epiUI = null;
		System.gc();
		if(ModeServer.guiMode())mainFrame.repaint();
		else noGUIModeMainPanel.repaint();
		ModelController.getInstance().setModelOpened(false);
		TissueController.getInstance().resetTissueSettings();
		SimStateServer.getInstance().removeAllSimulationTrigger();
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_MODEL_FILE).setEnabled(false);
			
		menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(false);
		menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(false);
		menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
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
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.LOAD_DATA_EXPORT).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.CLOSE_DATA_EXPORT).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.NEW_DATA_EXPORT).setEnabled(false);
		menuBarFactory.getEpisimMenuItem(EpisimMenuItem.DATA_EXPORT_SIMULATION_SNAPSHOT).setEnabled(true);
		
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
        EpisimExceptionHandler.getInstance().displayException(e);
       
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
				EpisimExceptionHandler.getInstance().displayException(e);
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
		DataExportController.getInstance().simulationWasStopped();
		this.menuBarFactory.getEpisimMenu(EpisimMenu.CHART_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.DATAEXPORT_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.WINDOWS_MENU).setEnabled(true);
		this.menuBarFactory.getEpisimMenu(EpisimMenu.PARAMETERS_SCAN).setEnabled(true);		
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

		
	
	
	private boolean snapshotLoadSuccess=false;
	public boolean loadSimulationStateFile(File f, final boolean guiLoad, final boolean snapshotDataExportLoad){
		snapshotLoadSuccess=false;
		dataExtractionFromSimulationSnapshotMode = false;
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
										return false;
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
					/*	if(ModeServer.guiMode() && false){
							final Semaphore sem = new Semaphore(1);
							EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback() {											
								public void taskHasFinished() {
									sem.release();
								}
								public void executeTask() {
									boolean success = openModel(simStateData.getLoadedModelFile(), simStateData, snapshotDataExportLoad);
									if(success && epiUI != null && guiLoad && !snapshotDataExportLoad){
										int option =JOptionPane.showConfirmDialog(mainFrame, "Start simulation to show loaded cells?\nPlease be aware of (significantly) prolonged simulation starting times\nafter loading a tissue simulation snapshot.\nThe tissue simulation snaphot is restored each time you (re-)start the simulation.", "Start simulation?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
										if(option==JOptionPane.YES_OPTION) epiUI.pressStartAfterLoadingSimulationState();
										
									}
									snapshotLoadSuccess=success;
									dataExtractionFromSimulationSnapshotMode = success && snapshotDataExportLoad;
								}
							};
							try{
	                     sem.acquire();                     
	                     EpisimProgressWindow.showProgressWindowForTask(mainFrame, "Load Episim Simulation State...", cb);
	                     sem.acquire();
							}
                     catch (InterruptedException e){
	                    ExceptionDisplayer.getInstance().displayException(e);
                     }
							return snapshotLoadSuccess;
						}
						else{*/
						if(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_STORAGE_PATH_PROP) != null){
							File snapshotPath = new File(EpisimProperties.getProperty(EpisimProperties.SIMULATION_SNAPSHOT_STORAGE_PATH_PROP));
							if(snapshotPath.isDirectory()){
								snapshotPath = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATION_SNAPSHOT_STORAGE_PATH_PROP, "EpisimSnapshot", "xml");
							}
							setTissueExportPath(snapshotPath, false);
						}
						
							boolean success = openModel(simStateData.getLoadedModelFile(), simStateData, snapshotDataExportLoad);
							dataExtractionFromSimulationSnapshotMode = success && snapshotDataExportLoad;
							return success;
					//	}
						
					}
					
            }
        }
        catch (ParserConfigurationException e1){
        		EpisimExceptionHandler.getInstance().displayException(e1);
        		return false;
        }
        catch (SAXParseException e1){
        	if(ModeServer.guiMode())JOptionPane.showMessageDialog(mainFrame,"systemId: "+e1.getSystemId()+"; lineNumber: "+e1.getLineNumber()+"; columnNumber: "+e1.getColumnNumber()+"\n"+e1.getMessage());
        	else System.out.println("systemId: "+e1.getSystemId()+"; lineNumber: "+e1.getLineNumber()+"; columnNumber: "+e1.getColumnNumber()+"\n"+e1.getMessage());
        	return false;
        } 
		 catch(SAXException e1){
			 EpisimExceptionHandler.getInstance().displayException(e1);
			 return false;
		 }
		  catch (IOException e1) {
			  EpisimExceptionHandler.getInstance().displayException(e1);
			  return false;
		  }
		 return false;
	}	
	
}