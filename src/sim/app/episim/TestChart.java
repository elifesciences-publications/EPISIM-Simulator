package sim.app.episim;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import sim.app.episim.datamonitoring.charts.EpisimChartPanel;


public class TestChart {

	public static void main(String[] args) {
		final double width = 400;
		final double height = 200;
		XYSeriesCollection seriesColl = new XYSeriesCollection();
		seriesColl.addSeries(new XYSeries("Test"));
		JFreeChart chart = ChartFactory.createXYLineChart("Test Cell Visualization", "Y", "X", seriesColl, PlotOrientation.HORIZONTAL, false, false, false);
		XYPlot xyPlot = chart.getXYPlot();
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.getDomainAxis().setAutoRange(false);
		xyPlot.getDomainAxis().setRange(0, height);
		xyPlot.getRangeAxis().setAutoRange(false);
		xyPlot.getRangeAxis().setRange(0, width);
		XYShapeAnnotation annotation = new XYShapeAnnotation(new Ellipse2D.Double(100-10, 100-10, 20, 20), new BasicStroke(2), Color.GREEN, Color.BLUE);
		
		xyPlot.addAnnotation(annotation);
		final EpisimChartPanel panel = new EpisimChartPanel(chart, false, true);
		JFrame frame = new JFrame();
		JInternalFrame f;
	
		frame.addWindowStateListener(new WindowStateListener(){

			@Override
         public void windowStateChanged(WindowEvent e) {

	         panel.doLayout();
	         
         }});
		frame.addWindowListener(new WindowAdapter(){

			

			
         public void windowActivated(WindowEvent e) {

         	panel.doLayout();
	         
         }

			});
		
	/*panel.addComponentListener(new ComponentAdapter(){		
      public void componentResized(ComponentEvent e) {
      		double widthDelta = panel.getWidth()-panel.getChartRenderingInfo().getPlotInfo().getDataArea().getWidth();
      		double heightDelta = panel.getHeight()-panel.getChartRenderingInfo().getPlotInfo().getDataArea().getHeight();
      		//double height = panel.getHeight();
      		double ratio = (panel.getChartRenderingInfo().getPlotInfo().getDataArea().getWidth())/(panel.getChartRenderingInfo().getPlotInfo().getDataArea().getHeight());      		
      		System.out.println("Ratio: "+ ratio+" ("+(width/height)+")");	      
      }});*/
		
	((JPanel)frame.getContentPane()).setLayout(new FlowLayout());
		((JPanel)frame.getContentPane()).add(panel);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//XYPlot xyPlot;

	}

}
