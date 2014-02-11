package sim.app.episim.datamonitoring.charts.io;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jzy3d.chart.Chart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import sim.SimStateServer;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.engine.SimState;


public class PNGPrinter implements ClassLoaderChangeListener{
	
	private static PNGPrinter instance = null;
	
	
	private static final int PNG_CHARTWIDTH=600;
	private static final int PNG_CHARTHEIGHT=450;

	private static final String FILEEXTENSION = ".png";
	private Set<String> filenameSet;
	private Map<Long, String> fileNameMap;
	
	private HashSet<Long> chartRecoloringRegistry;
	
	private PNGPrinter(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		reset();
		chartRecoloringRegistry = new HashSet<Long>();
	}
	
	public static synchronized PNGPrinter getInstance(){
		if(instance == null) instance = new PNGPrinter();
		return instance;
	}
	
	public void printChartAsPng(long chartId, File directory, String fileName, JFreeChart chart, SimState state){
		if(chart != null){
			if(!chartRecoloringRegistry.contains(chartId)){
				changeChartAxisColorsToBlack(chart);
				chartRecoloringRegistry.add(chartId);
			}
			File pngFile = getPNGFile(chartId, directory, fileName, state);
			if(pngFile != null){
				saveJFreeChart(chart, pngFile);
			}
		}		
	}	
	
	public void printChartAsPng(long chartId, File directory, String fileName, Chart chart, SimState state){
		if(chart != null){
			File pngFile = getPNGFile(chartId, directory, fileName, state);
			if(pngFile != null){
				saveDiffusion3DChart(chart, pngFile);
			}
		}		
	}
	
	private File getPNGFile(long chartId, File directory, String fileName, SimState state){
		if(!this.fileNameMap.keySet().contains(chartId)) this.fileNameMap.put(chartId, findFileName(fileName));
		
		if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH) != null
				&& state != null){
			
			fileName = fileName.replace(' ', '_');
			
			File pngFile = EpisimProperties.getFileForPathOfAProperty(EpisimProperties.SIMULATOR_CHARTPNGPRINTPATH, fileName, FILEEXTENSION);
			pngFile = new File(pngFile.getAbsolutePath().substring(0, (pngFile.getAbsolutePath().length()-FILEEXTENSION.length()))+"(SimulationStep " +SimStateServer.getInstance().getSimStepNumber()+ ")"+FILEEXTENSION);
			
			return pngFile;
		}		
		else if(directory != null && directory.isDirectory() && state != null){
			
			File pngFile = new File(directory.getAbsolutePath()+File.separatorChar + this.fileNameMap.get(chartId) + 
         		"(SimulationStep " +SimStateServer.getInstance().getSimStepNumber()+ ")"+FILEEXTENSION);	
			
			pngFile = checkFile(pngFile);
			
			return pngFile;			
		}
		return null;
	}
	
	private void saveJFreeChart(JFreeChart chart, File pngFile){
		try{
			
			
			ChartUtilities.saveChartAsPNG(pngFile, chart, PNG_CHARTWIDTH, PNG_CHARTHEIGHT);
         if(EpisimProperties.getProperty(EpisimProperties.IMAGE_SAVESVGCOPYOFPNG) != null 
         		&& EpisimProperties.getProperty(EpisimProperties.IMAGE_SAVESVGCOPYOFPNG).equalsIgnoreCase(EpisimProperties.ON)){
         	saveSVGImageOfJFreeChart(chart, pngFile);
         }
      }
      catch (Exception e){
        ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	private void saveDiffusion3DChart(Chart chart, File pngFile){
		try{
			chart.screenshot(pngFile);
      }
      catch (IOException e){
        ExceptionDisplayer.getInstance().displayException(e);
      }
	}
	
	
	private void saveSVGImageOfJFreeChart(JFreeChart chart, File pngFile) throws IOException{
		File svgFile = new File(pngFile.getAbsolutePath().substring(0, pngFile.getAbsolutePath().length()-3)+"svg");
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

     // Create an instance of org.w3c.dom.Document.
     String svgNS = "http://www.w3.org/2000/svg";
     Document document = domImpl.createDocument(svgNS, "svg", null);

     // Create an instance of the SVG Generator.
     SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

     ChartPanel chartPanel = new ChartPanel(chart,PNG_CHARTWIDTH,PNG_CHARTHEIGHT,PNG_CHARTWIDTH,PNG_CHARTHEIGHT,PNG_CHARTWIDTH,PNG_CHARTHEIGHT, false, false, false,false, false, false);
     chartPanel.setSize(PNG_CHARTWIDTH, PNG_CHARTHEIGHT);
     chartPanel.paint(svgGenerator);
     // Finally, stream out SVG to the standard output using
     // UTF-8 encoding.
     boolean useCSS = true; // we want to use CSS style attributes
     FileOutputStream fileOut = new FileOutputStream(svgFile);
     Writer out = new OutputStreamWriter(fileOut, "UTF-8");
     svgGenerator.stream(out, useCSS);
     fileOut.flush();
     fileOut.close();
	}
	
	private void changeChartAxisColorsToBlack(JFreeChart chart){
		XYPlot p = chart.getXYPlot();
		p.getRangeAxis().setAxisLinePaint(Color.BLACK);
		p.getRangeAxis().setLabelPaint(Color.BLACK);
		p.getRangeAxis().setTickLabelPaint(Color.BLACK);
		p.getRangeAxis().setTickMarkPaint(Color.BLACK);
		
		p.getDomainAxis().setAxisLinePaint(Color.BLACK);
		p.getDomainAxis().setLabelPaint(Color.BLACK);
		p.getDomainAxis().setTickLabelPaint(Color.BLACK);
		p.getDomainAxis().setTickMarkPaint(Color.BLACK);
		if(chart.getLegend()!= null){
			chart.getLegend().setItemPaint(Color.BLACK);
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
		this.chartRecoloringRegistry = new HashSet<Long>();
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
	
   public void classLoaderHasChanged() {
		instance = null;	   
   }
	
}
