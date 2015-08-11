package sim.app.episim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Properties;

import episimexceptions.PropertyException;
import binloc.ProjectLocator;


public class EpisimProperties {
	
	private static EpisimProperties instance;
	
	private Properties properties;
	
	/**
	 * @deprecate Legacy Constant, not in use anymore
	 */
	public static final String SIMULATOR_CONSOLE_INPUT_PROP = "simulator.consoleinput";
	
	private static final String CONFIG = "config";
	private static final String FILENAME = "episimconfig.properties";
	
	private static final String[][] OLD_NEW_PROPERTY_NAMES = new String[][]{{"exception.displaymode",							"exception.output"},
																									{"simulator.gui",										"gui"},
																									{"standardoutput",									"standard.output"},
																									{"moviepath",											"movie.path"},
																									{"framespersecond",									"movie.frames.persecond"},
																									{"simulator.cellbehavioralmodelpath",			"model.path"},
																									{"simulator.snapshotpath",							"simulation.snapshotpath"},
																									{"simulator.standardfilepath",					"dialog.standardpath"},
																									{"simulator.autostartandstop",					"simulation.autostartandstop"},
																									{"simulator.maxsimulationsteps",					"simulation.steps.max"},
																									{"simulator.waitingTimeBeforeShutdownInMs",	"simulation.shutdown.waitms"},
																									{"simulator.sendreceivealgorithm",				"model.sendreceive"},
																									{"simulator.simstepmode",							"simulation.steps.mode"},
																									{"simulator.diffusionfield.3dvisualization",	"display.3d.diffusionfield"},
																									{"simulator.chartsetpath",							"charts.path"},
																									{"simulator.dataexportpath",						"dataexport.path"},
																									{"simulator.chartpngprintpath",					"charts.png.path"},
																									{"simulator.chartpngprintfreq",					"charts.png.printfreq"},
																									{"simulator.chartupdatefreq",						"charts.updatefreq"},
																									{"simulator.dataexportupdatefreq",				"dataexport.updatefreq"},
																									{"simulator.saveSVGCopyOfEachPNG",				"image.svgcopy"},
																									{"simulator.display3d.rotation.x",				"display.3d.rotation.x"},
																									{"simulator.display3d.rotation.y",				"display.3d.rotation.y"},
																									{"simulator.display3d.rotation.z",				"display.3d.rotation.z"},
																									{"simulator.display3d.rotation.persecond",	"display.3d.rotation.persecond"},
																									{"simulator.displaysize.width",					"display.size.width"},
																									{"simulator.displaysize.height",					"display.size.height"},
																									{"simulator.movieframesperfile",					"movie.frames.perfile"},
																									{"simulator.cell.randomageinit",					"model.cell.randomageinit"}
		                                                    		};
	
	
	public static final String GUI_PROP = "gui";
	public static final String EPISIMBUILD_JARNAME_PROP = "episimbuild.jarname";
	public static final String DIALOG_STANDARDFILEPATH = "dialog.standardpath";
	
	public static final String STANDARD_OUTPUT="standard.output";
	public static final String INFO_LOGGING_PROP = "info.logging";
	public static final String EXCEPTION_OUTPUT_PROP = "exception.output";	
	public static final String EXCEPTION_LOGGING_PROP = "exception.logging";
	
	public static final String MODEL_CELL_BEHAVIORAL_MODEL_PATH_PROP = "model.path";
	public static final String MODEL_SEND_RECEIVE_ALGORITHM = "model.sendreceive";
	public static final String MODEL_RANDOM_CELL_AGE_INIT="model.cell.randomageinit";
	
	public static final String SIMULATION_AUTOSTART_AND_STOP_PROP = "simulation.autostartandstop";
	public static final String SIMULATION_PARALLELIZATION = "simulation.parallelization";
	public static final String SIMULATION_PARALLELIZATION_THREAD_NO = "simulation.parallelization.threads";
	public static final String SIMULATION_MAX_STEPS_PROP = "simulation.steps.max";
	public static final String SIMULATION_SIM_STEP_MODE = "simulation.steps.mode";
	public static final String SIMULATION_SHUTDOWN_WAIT_MS ="simulation.shutdown.waitms";
	public static final String SIMULATION_SNAPSHOT_STORAGE_PATH_PROP = "simulation.snapshotpath";
	public static final String SIMULATION_SNAPSHOT_LOAD_PATH_PROP = "simulation.snapshotpath.load";
	public static final String SIMULATION_SNAPSHOT_SAVE_FREQUENCY = "simulation.snapshotfreq";
	public static final String SIMULATION_PNG_PATH = "simulation.png.path";
	public static final String SIMULATION_PNG_PRINT_FREQUENCY = "simulation.png.printfreq";
	public static final String SIMULATION_PNG_PRINT_SEQUENCE = "simulation.png.printseq";
	public static final String SIMULATION_PNG_PRINT_DELAY_IN_MS = "simulation.png.printdelayinms";
	
	public static final String SIMULATION_TRIGGER_FILE_PATH_PROP ="simulation.trigger.file.path";
	
	public static final String SIMULATOR_CHARTSETPATH = "charts.path";
	public static final String SIMULATOR_CHARTPNGPRINTPATH = "charts.png.path";
	public static final String SIMULATOR_CHARTPNGPRINTFREQ = "charts.png.printfreq";
	public static final String SIMULATOR_CHARTUPDATEFREQ = "charts.updatefreq";
	
	public static final String SIMULATOR_DATAEXPORTPATH = "dataexport.path";	
	public static final String SIMULATOR_DATAEXPORTUPDATEFREQ = "dataexport.updatefreq";
	
	
	public static final String IMAGE_SAVESVGCOPYOFPNG = "image.svgcopy";
	
	public static final String MOVIE_PATH_PROP = "movie.path";
	public static final String MOVIE_FRAMES_PER_SECOND_PROP = "movie.frames.persecond";
	public static final String MOVIE_FRAMES_PER_FILE="movie.frames.perfile";
	
	public static final String DISPLAY_DIFFUSION_FIELD_3DVISUALIZATION ="display.3d.diffusionfield";	
	public static final String DISPLAY_3D_ROTATION_X="display.3d.rotation.x";
	public static final String DISPLAY_3D_ROTATION_Y="display.3d.rotation.y";
	public static final String DISPLAY_3D_ROTATION_Z="display.3d.rotation.z";
	public static final String DISPLAY_3D_ROTATION_PERSECOND="display.3d.rotation.persecond";
	public static final String DISPLAY_3D_CROSSSECTION_STACK="display.3d.crosssectionstack";
	public static final String DISPLAY_3D_CROSSSECTION_STACK_WAITMS="display.3d.crosssectionstack.waitms";
	
	public static final String DISPLAY_SIZE_WIDTH = "display.size.width";
	public static final String DISPLAY_SIZE_HEIGHT = "display.size.height";
	public static final String DISPLAY_COLORMODE_FREQ = "display.colormode.freq";
	public static final String DISPLAY_COLORMODE_MIN = "display.colormode.min";	
	public static final String DISPLAY_COLORMODE_MAX = "display.colormode.max";
	public static final String DISPLAY_COLORMODE_INCR = "display.colormode.incr";
	public static final String DISPLAY_3D_DRAW_INNER_CELL_SURFACE="display.3d.drawinnercellsurface";
	
	
	public static final String SIMULATOR_DIFFUSION_FIELD_TESTMODE = "simulator.diffusionfield.testmode";
	
	
	
	
	
	

//-----------------------------------------------------------------------------------------------------------------------------------------------------
// CONSOLE PARAMETERS
//-----------------------------------------------------------------------------------------------------------------------------------------------------	
	
	public static final String SIMULATOR_SIMULATION_RUN_ID = "simulator.simulationrun.id";
	public static final String SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP = "simulator.cellbehavioralmodel.globalparametersfile";
	public static final String SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP = "simulator.biomechanicalmodel.globalparametersfile";
	public static final String SIMULATOR_MISCPARAMETERSFILE_PROP = "simulator.miscparametersfile";
	public static final String SIMULATOR_CONFIG_FILE_PROP = "simulator.configpropertyfile";
	public static final String SIMULATOR_DATAEXPORT_CSV_OVERRIDE_FOLDER = "simulator.dataexport.csvoverridefolder";
	
	
//-----------------------------------------------------------------------------------------------------------------------------------------------------
// VALUES
//-----------------------------------------------------------------------------------------------------------------------------------------------------
	public static final String ON = "on";
	public static final String OFF = "off";
	
	public static final String SIM_STEP_MODE_MONTE_CARLO="montecarlo";
	public static final String SIM_STEP_MODE_MASON="mason";
	
	public static final String DISPLAY_DF_3DVISUALIZATION_BLOCK_MODE = "block";
	public static final String DISPLAY_DF_3DVISUALIZATION_CROSSSECTION_MODE = "crosssection";
	
	public static final String CONSOLE= "console";
	public static final String SIMULATOR = "simulator";
//-----------------------------------------------------------------------------------------------------------------------------------------------------
	
	private EpisimProperties(){
		loadStandardConfigFile();		
	}
	
	public static boolean loadCustomConfigPropertiesFile(String path){
		if(instance == null) instance = new EpisimProperties();
		return instance.loadCustomConfigFile(path);
	}
	private boolean loadCustomConfigFile(String path){
		
		if(path != null && !path.trim().isEmpty()){
			File configFilePath = new File(path);
			if(configFilePath.exists() && !configFilePath.isDirectory()){
				properties = new Properties();
				FileInputStream stream=null;
				try{
				      stream = new FileInputStream(configFilePath);
			         properties.load(stream);
			         stream.close();
			         getProperties().setProperty(SIMULATOR_CONSOLE_INPUT_PROP, ON);
			         return true;
			   }
			   catch (Exception e){
				  e.printStackTrace();
				  if(stream != null)
	            try{
	               stream.close();
               }
               catch (IOException e1){
	              
	               e1.printStackTrace();
               }
				  loadStandardConfigFile();
				  return false;
			   }
			       
			}
		}
		loadStandardConfigFile();
		return false;
	}
	
	private void loadStandardConfigFile(){
		properties = new Properties();
		FileInputStream stream;
      try{
	      stream = new FileInputStream(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat(FILENAME));
         properties.load(stream);
         stream.close();
         getProperties().setProperty(SIMULATOR_CONSOLE_INPUT_PROP, ON);
        if(isConfigFileConversationRequired()) convertPropertiesToNewFormat();
      }
      catch (IOException e1){
	      EpisimExceptionHandler.getInstance().displayException(e1);
      }
      catch (URISyntaxException e2){
      	EpisimExceptionHandler.getInstance().displayException(e2);
      }      
	}
	
	private void convertPropertiesToNewFormat(){
		 if(saveOldPropertiesFile()){		 
			for(int i = 0 ; i< EpisimProperties.OLD_NEW_PROPERTY_NAMES.length; i++){
				String actOldKey  = EpisimProperties.OLD_NEW_PROPERTY_NAMES[i][0];
				if(this.properties.containsKey(actOldKey)){
					String value = this.properties.getProperty(actOldKey);
					this.properties.remove(actOldKey);
					this.properties.put(EpisimProperties.OLD_NEW_PROPERTY_NAMES[i][1], value);
				}
			}
			FileOutputStream stream;
         try{
	         stream = new FileOutputStream(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat(FILENAME));
	         
	      	this.properties.store(stream, "");
				stream.close();
         }
         catch (FileNotFoundException e){
         	System.out.println("Property File conversion aborted due to error!");
         	e.printStackTrace();
         }
         catch (URISyntaxException e){
         	System.out.println("Property File conversion aborted due to error!");
         	e.printStackTrace();
         }
         catch (IOException e){
         	System.out.println("Property File conversion aborted due to error!");
         	e.printStackTrace();
         }		
		 }
		 else{
			 System.out.println("Old Property File cannot be renamed. Property File conversion aborted.");
		 }
	}
	
	private boolean saveOldPropertiesFile(){
		 File oldFile = getOldPropertyFilename();
		 if(oldFile != null){
			 try {
				 Files.move((new File(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat(FILENAME))).toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				 return true;
			 } catch (IOException e) {
				 e.printStackTrace();
				 return false;
			  }
         catch (URISyntaxException e){
         	e.printStackTrace();
         	return false;
         }
		 }
		 return false;		  
	}
	
	private File getOldPropertyFilename(){
		try{
	      File file = new File(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat("old-"+FILENAME));
	      int i=2;
	      while(file.exists()){
	      	file = new File(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat("old-"+i+"-"+FILENAME));
	      	i++;
	      }
	      return file;
      }
      catch (URISyntaxException e){
      	e.printStackTrace();
	      return null;
      }
	}
	
	private boolean isConfigFileConversationRequired(){
		HashSet<String> oldPropertyNames = new HashSet<String>();
		for(int i = 0 ; i< EpisimProperties.OLD_NEW_PROPERTY_NAMES.length; i++) oldPropertyNames.add(EpisimProperties.OLD_NEW_PROPERTY_NAMES[i][0]);
		if(this.properties != null){
			for(Object object: this.properties.keySet()){
				if(object instanceof String){
					if(oldPropertyNames.contains((String)object)) return true;
				}
			}
		}
		return false;
	}
	
	public static String getProperty(String name){
		if(instance == null) instance = new EpisimProperties();
		return instance.getProperties().getProperty(name);
	}
	
	public static void setProperty(String name, String val){
		 if(instance == null) instance = new EpisimProperties();	
		 instance.getProperties().setProperty(name, val);
	}
	
	public static void removeProperty(String name){
		 if(instance == null) instance = new EpisimProperties();	
		 if(instance.getProperties().getProperty(name) != null){
			 instance.getProperties().remove(name);
		 }
	}
	
	private Properties getProperties(){ return properties;}
	
	public static File getFileForPathOfAProperty(final String property, final String filename, final String fileExtension){
		return getFileForPathOfAProperty(property, filename, fileExtension, Long.MAX_VALUE);
	}
	
	public static File getFileForPathOfAProperty(final String property, final String filename, final String fileExtension, long counter){
		String path = EpisimProperties.getProperty(property);
		File f = new File(path);
		if(!f.exists() || !f.isDirectory() || !f.canWrite()) throw new PropertyException("Property -  " + property +": " + f.getAbsolutePath() + " is not an (existing or accessable) directory!");
		return getFileForDirectoryPath(path, filename, fileExtension, counter);
	}
	
	public static File getFileForDirectoryPath(final String dirPath, final String filename, final String fileExtension, long counter){	
	   	
	   	File f = new File(dirPath);
	   	if(!f.exists() || !f.isDirectory() || !f.canWrite()) throw new PropertyException(f.getAbsolutePath() + " is not an (existing or accessable) directory!");
	   	GregorianCalendar cal = new GregorianCalendar();
	   	cal.setTime(new Date());
	   	String date = cal.get(Calendar.YEAR)+"_"
					+ cal.get(Calendar.MONTH)+ "_"
					+ cal.get(Calendar.DAY_OF_MONTH)+ "_"
					+ cal.get(Calendar.HOUR_OF_DAY)+ "_"
					+ cal.get(Calendar.MINUTE)+ "_"
					+ cal.get(Calendar.SECOND)+ "_";
	   	String counterStr = counter != Long.MAX_VALUE ? (counter)+"_":"";
	   	File file = new File(f.getAbsolutePath()+System.getProperty("file.separator")
	   										+ counterStr
	   										+ (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID) != null ? (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID).trim() +"_"):date)
	   										+ filename 
	   										+(fileExtension.startsWith(".") ? fileExtension : ("."+fileExtension)));
	   	int index = 1;
	   	while(file.exists()){	   		
	   		file = new File(f.getAbsolutePath()+System.getProperty("file.separator")						
						+ (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID) != null ? (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID).trim() +"_"):date)
						+ filename  +"_"+(index++)+ (fileExtension.startsWith(".") ? fileExtension : ("."+fileExtension)));
	   	}
	   	return file;	   
	}
}
