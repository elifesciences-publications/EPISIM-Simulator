package sim.app.episim;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import episimexceptions.PropertyException;

import binloc.ProjectLocator;


public class EpisimProperties {
	
	private static EpisimProperties instance;
	
	private Properties properties;
	
	private static final String CONFIG = "config";
	private static final String FILENAME = "episimconfig.properties";
	
	public static final String EXCEPTION_LOGGING_PROP = "exception.logging";
	public static final String EPISIMBUILD_JARNAME_PROP = "episimbuild.jarname";
	public static final String EXCEPTION_DISPLAYMODE_PROP = "exception.displaymode";
	public static final String STANDARD_OUTPUT = "standardoutput";
	public static final String SIMULATOR_GUI_PROP = "simulator.gui";
	public static final String SIMULATOR_CONSOLE_INPUT_PROP = "simulator.consoleinput";
	public static final String MOVIE_PATH_PROP = "moviepath";
	public static final String FRAMES_PER_SECOND_PROP = "framespersecond";
	public static final String SIMULATOR_CELL_BEHAVIORAL_MODEL_PATH_PROP = "simulator.cellbehavioralmodelpath";
	public static final String SIMULATOR_SNAPSHOT_PATH_PROP = "simulator.snapshotpath";
	public static final String SIMULATOR_AUTOSTART_AND_STOP_PROP = "simulator.autostartandstop";
	public static final String SIMULATOR_MAX_SIMULATION_STEPS_PROP = "simulator.maxsimulationsteps";
	public static final String SIMULATOR_SEND_RECEIVE_ALGORITHM = "simulator.sendreceivealgorithm";
	
	
	public static final String SIMULATOR_CELLBEHAVIORALMODEL_GLOBALPARAMETERSFILE_PROP = "simulator.cellbehavioralmodel.globalparametersfile";
	public static final String SIMULATOR_BIOMECHNICALMODEL_GLOBALPARAMETERSFILE_PROP = "simulator.biomechanicalmodel.globalparametersfile";
	public static final String SIMULATOR_MISCPARAMETERSFILE_PROP = "simulator.miscparametersfile";
	public static final String SIMULATOR_CHARTSETPATH = "simulator.chartsetpath";
	public static final String SIMULATOR_DATAEXPORTPATH = "simulator.dataexportpath";
	public static final String SIMULATOR_CHARTPNGPRINTPATH = "simulator.chartpngprintpath";	
	public static final String SIMULATOR_CHARTPNGPRINTFREQ = "simulator.chartpngprintfreq";
	
	public static final String SIMULATOR_SIMULATION_RUN_ID = "simulator.simulationrun.id";
	
	public static final String SIMULATOR_TESTMENU = "simulator.testmenu";

	
	public static final String ON = "on";
	public static final String OFF = "off";
		
	public static final String CONSOLE= "console";
	public static final String SIMULATOR = "simulator";	
	
	private EpisimProperties(){
		properties = new Properties();
		FileInputStream stream;
      try{
	      stream = new FileInputStream(ProjectLocator.getPathOf(CONFIG).getAbsolutePath().concat(System.getProperty("file.separator")).concat(FILENAME));
         properties.load(stream);
         stream.close();
      }
      catch (IOException e1){
	      ExceptionDisplayer.getInstance().displayException(e1);
      }
      catch (URISyntaxException e2){
      	ExceptionDisplayer.getInstance().displayException(e2);
      }      
		
	}
	
	public static String getProperty(String name){
		if(instance == null) instance = new EpisimProperties();
		return instance.getProperties().getProperty(name);
	}
	
	public static void setProperty(String name, String val){
		 if(instance == null) instance = new EpisimProperties();	
		 instance.getProperties().setProperty(name, val);
	}
	
	private Properties getProperties(){ return properties;}
	
	public static File getFileForPathOfAProperty(final String property, final String filename, final String fileExtension){
	
	   	String path = EpisimProperties.getProperty(property);
	   	File f = new File(path);
	   	if(!f.exists() || !f.isDirectory() || !f.canWrite()) throw new PropertyException("Property -  " + property +": " + f.getAbsolutePath() + " is not an (existing or accessable) directory!");
	   	GregorianCalendar cal = new GregorianCalendar();
	   	cal.setTime(new Date());
	   	File file = new File(f.getAbsolutePath()+System.getProperty("file.separator")
	   										+ cal.get(Calendar.YEAR)+"_"
	   										+ cal.get(Calendar.MONTH)+ "_"
	   										+ cal.get(Calendar.DAY_OF_MONTH)+ "_"
	   										+ cal.get(Calendar.HOUR_OF_DAY)+ "_"
	   										+ cal.get(Calendar.MINUTE)+ "_"
	   										+ cal.get(Calendar.SECOND)+ "_"
	   										+ (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID) != null ? (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID).trim() +"_"):"")
	   										+ filename 
	   										+(fileExtension.startsWith(".") ? fileExtension : ("."+fileExtension)));
	   	int index = 1;
	   	while(file.exists()){
	   		file = new File(f.getAbsolutePath()+System.getProperty("file.separator")
						+ cal.get(Calendar.YEAR)+"_"
						+ cal.get(Calendar.MONTH)+ "_"
						+ cal.get(Calendar.DAY_OF_MONTH)+ "_"
						+ cal.get(Calendar.HOUR_OF_DAY)+ "_"
						+ cal.get(Calendar.MINUTE)+ "_"
						+ cal.get(Calendar.SECOND)+ "_"
						+ (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID) != null ? (EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SIMULATION_RUN_ID).trim() +"_"):"")
						+ filename  +"_"+(index++)+ (fileExtension.startsWith(".") ? fileExtension : ("."+fileExtension)));
	   	}
	   	return file;
	   
	}

}
