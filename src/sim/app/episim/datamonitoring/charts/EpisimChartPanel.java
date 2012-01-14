package sim.app.episim.datamonitoring.charts;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JFileChooser;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ExtensionFileFilter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;


public class EpisimChartPanel extends ChartPanel {
	
	public EpisimChartPanel(JFreeChart chart){
		super(chart);
		
	}
	
	public EpisimChartPanel(JFreeChart chart, boolean useBuffer){
		super(chart, useBuffer);
	}
	
	public void doSaveAs(){
		 JFileChooser fileChooser = new JFileChooser();
       fileChooser.setCurrentDirectory(getDefaultDirectoryForSaveAs());
       ExtensionFileFilter filter = new ExtensionFileFilter(
               localizationResources.getString("PNG_Image_Files"), ".png");
       fileChooser.addChoosableFileFilter(filter);

       int option = fileChooser.showSaveDialog(this);
       if(option == JFileChooser.APPROVE_OPTION) {
	        String filename = fileChooser.getSelectedFile().getPath();
	        if (isEnforceFileExtensions()) {
	            if (!filename.endsWith(".png")) {
	                filename = filename + ".png";
	            }
	        }
	        try{
	      	 File pngFile = new File(filename);
	          ChartUtilities.saveChartAsPNG(pngFile,getChart(), getWidth(), getHeight());
	          if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SAVESVGCOPYOFPNG) != null 
	          		&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_SAVESVGCOPYOFPNG).equalsIgnoreCase(EpisimProperties.ON)){
	          	saveSVGImageOfJFreeChart(pngFile);
	          }
		     }
		     catch(IOException e){
		         ExceptionDisplayer.getInstance().displayException(e);
		     }
       }		
	}
	
	private void saveSVGImageOfJFreeChart(File pngFile) throws IOException{
		changeChartAxisColorsToBlack(getChart());
		File svgFile = new File(pngFile.getAbsolutePath().substring(0, pngFile.getAbsolutePath().length()-3)+"svg");
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

     // Create an instance of org.w3c.dom.Document.
     String svgNS = "http://www.w3.org/2000/svg";
     Document document = domImpl.createDocument(svgNS, "svg", null);

     // Create an instance of the SVG Generator.
     SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

     ChartPanel chartPanel = new ChartPanel(getChart(), this.getWidth(), this.getHeight(), this.getMinimumDrawWidth(), this.getMinimumDrawHeight(), this.getMaximumDrawWidth(), this.getMaximumDrawHeight(), false, false, false,false, false, false);
     chartPanel.setSize(this.getWidth(), this.getHeight());
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
}
