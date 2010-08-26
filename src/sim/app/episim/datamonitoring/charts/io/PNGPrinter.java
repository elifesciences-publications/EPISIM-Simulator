package sim.app.episim.datamonitoring.charts.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.engine.SimState;


public class PNGPrinter {
	
	private static PNGPrinter instance = null;
	
	
	private static final int PNG_CHARTWIDTH=400;
	private static final int PNG_CHARTHEIGHT=300;
	private static final int PNG_CHARTWIDTH_LARGE=600;
	private static final int PNG_CHARTHEIGHT_LARGE=400;
	private static final String FILEEXTENSION = ".png";
	private Set<String> filenameSet;
	private Map<Long, String> fileNameMap;
	
	private int simulationCycleCounter = 1;
	
	private PNGPrinter(){
		reset();
	}
	
	public static synchronized PNGPrinter getInstance(){
		if(instance == null) instance = new PNGPrinter();
		return instance;
	}
	
	public void printChartAsPng(long chartId, File directory, String fileName, JFreeChart chart, SimState state){
		if(!this.fileNameMap.keySet().contains(chartId)) this.fileNameMap.put(chartId, findFileName(fileName));
		
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP) != null
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CONSOLE_INPUT_PROP).equals(EpisimProperties.ON_CONSOLE_INPUT_VAL)
				&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null
				&& chart != null  && state != null){
			
			fileName = fileName.replace(' ', '_');
			
			File pngFile = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH, fileName, FILEEXTENSION);
			pngFile = new File(pngFile.getAbsolutePath().substring(0, (pngFile.getAbsolutePath().length()-FILEEXTENSION.length()))+"(SimulationStep " +state.schedule.getSteps()+ ")"+FILEEXTENSION);
			
			try{
            ChartUtilities.saveChartAsPNG(pngFile, chart, PNG_CHARTWIDTH, PNG_CHARTHEIGHT);
         }
         catch (IOException e){
           ExceptionDisplayer.getInstance().displayException(e);
         }
		}		
		else if(directory != null && directory.isDirectory() && chart != null && state != null){
			
			File pngFile = new File(directory.getAbsolutePath()+File.separatorChar + this.fileNameMap.get(chartId) + 
         		"(SimulationStep " +state.schedule.getSteps()+ ")"+FILEEXTENSION);	
			
			pngFile = checkFile(pngFile);
			
				try{
	            ChartUtilities.saveChartAsPNG(pngFile, chart, PNG_CHARTWIDTH, PNG_CHARTHEIGHT);
            }
            catch (IOException e){
	           ExceptionDisplayer.getInstance().displayException(e);
            }
		}
		
	}
	
	private File checkFile(File file){
		for(int i = 2;file.exists(); i++){
			file = new File(file.getAbsolutePath().substring(0, (file.getAbsolutePath().length()-FILEEXTENSION.length()))+"_"+ i+FILEEXTENSION);
		}
		return file;
	}
	public void reset(){
		this.filenameSet = new HashSet<String>();
		this.fileNameMap = new HashMap<Long, String>();
		simulationCycleCounter = 1;
	}
	
	private String findFileName(String name){
		int i = 2;
		if(this.filenameSet.contains(name)){ 
			for( ; filenameSet.contains((name + i)); i++ );
			
			name += i;
		}
		this.filenameSet.add(name);
		return name;
	}
	
}
