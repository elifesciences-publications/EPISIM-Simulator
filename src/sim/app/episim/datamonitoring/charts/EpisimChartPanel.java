package sim.app.episim.datamonitoring.charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.ExtensionFileFilter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import sim.SimStateServer;
import sim.app.episim.EpisimProperties;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.ModeServer;


public class EpisimChartPanel extends ChartPanel {
	
	private static final int PNG_CHARTWIDTH=600;
	private static final int PNG_CHARTHEIGHT=450;
	
	private double widthToHeightScale = 2;
	private boolean registeredAtParent = false;
	private boolean autoScaleToRatio = false;
	public EpisimChartPanel(JFreeChart chart){
		super(chart);
		
	}
	
	public EpisimChartPanel(JFreeChart chart, boolean useBuffer){
		this(chart, useBuffer, false);
	}
	
	public EpisimChartPanel(JFreeChart chart, boolean useBuffer, boolean autoScaleToRatio){
		super(chart, useBuffer);
		this.autoScaleToRatio = autoScaleToRatio;
		if(autoScaleToRatio){
			this.addHierarchyListener(new HierarchyListener(){
            public void hierarchyChanged(HierarchyEvent e) {

	            if(EpisimChartPanel.this.getParent() != null && !registeredAtParent){
	            	EpisimChartPanel.this.getParent().addComponentListener(new ComponentAdapter() {							
							public void componentResized(ComponentEvent e) {						
								setToScale(EpisimChartPanel.this.getParent().getWidth(), EpisimChartPanel.this.getParent().getHeight());							
							}				
						});
	            	
	            }	            
            }				
			});
		}
	}
	
	public void setWidthToHeightScale(double scale){
		this.widthToHeightScale = scale;
	}
	public boolean isAutoScaleToRatio(){ return this.autoScaleToRatio;}
	public void doLayout(){
		if(this.getParent()!=null && autoScaleToRatio)
			setToScale(EpisimChartPanel.this.getParent().getWidth(), EpisimChartPanel.this.getParent().getHeight());
		super.doLayout();
	}
	private void setToScale(double parentWidth, double parentHeight){		
		
		if(this.getChartRenderingInfo()!=null && this.getChartRenderingInfo().getPlotInfo()!= null && this.getChartRenderingInfo().getPlotInfo().getDataArea()!=null){
			final double delta = 10; //absolute height to width bias with default settings 
					
			if((parentWidth*(1/widthToHeightScale))>= parentHeight){
				this.setBounds((int)(((parentWidth-(parentHeight*widthToHeightScale))/2)-delta),0,(int)(parentHeight*widthToHeightScale), (int)parentHeight);
			}
			else{
				this.setBounds(0,0,(int)parentWidth, (int)(((parentWidth*(1/widthToHeightScale)))+delta));
			}
		}
	}
	
	
	
	public void doSaveAs(){
		int width = PNG_CHARTWIDTH;
		int height = PNG_CHARTHEIGHT;
		if(this.autoScaleToRatio){
			width= this.getBounds().width;
			height= this.getBounds().height;
		}
		int[] resolution = null;	
		if(ModeServer.guiMode()) resolution = ChartImageResolutionDialog.showDialog((Frame)SimStateServer.getInstance().getEpisimGUIState().getMainGUIComponent(), width, height);
		if(!ModeServer.guiMode() || resolution != null){
			 if(resolution != null){
				 width = resolution[0];
				 height = resolution[1];
			 }
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
		          ChartUtilities.saveChartAsPNG(pngFile,getChart(), width, height);
		          if(EpisimProperties.getProperty(EpisimProperties.IMAGE_SAVESVGCOPYOFPNG) != null 
		          		&& EpisimProperties.getProperty(EpisimProperties.IMAGE_SAVESVGCOPYOFPNG).equalsIgnoreCase(EpisimProperties.ON)){
		          	saveSVGImageOfJFreeChart(pngFile, width, height);
		          }
			     }
			     catch(IOException e){
			         ExceptionDisplayer.getInstance().displayException(e);
			     }
	       }
		}
	}
	
	private void saveSVGImageOfJFreeChart(File pngFile, int width, int height) throws IOException{
		changeChartAxisColorsToBlack(getChart());
		File svgFile = new File(pngFile.getAbsolutePath().substring(0, pngFile.getAbsolutePath().length()-3)+"svg");
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

     // Create an instance of org.w3c.dom.Document.
     String svgNS = "http://www.w3.org/2000/svg";
     Document document = domImpl.createDocument(svgNS, "svg", null);

     // Create an instance of the SVG Generator.
     SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

     ChartPanel chartPanel = new ChartPanel(getChart(), width, height, this.getMinimumDrawWidth(), this.getMinimumDrawHeight(), this.getMaximumDrawWidth(), this.getMaximumDrawHeight(), false, false, false,false, false, false);
     chartPanel.setSize(width, height);
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
