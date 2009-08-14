package sim.app.episim.datamonitoring.calc;

import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;



public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JFrame frame = new JFrame("Test");
		JFreeChart chart;
		HistogramDataset dataSet = new HistogramDataset();
		
		dataSet.setType(HistogramType.FREQUENCY);
		chart = ChartFactory.createHistogram("Test", "TestX", "TestY", dataSet, PlotOrientation.VERTICAL, true, true, false);
		
		XYPlot yourXYPlot = (XYPlot)chart.getPlot();

		yourXYPlot.setForegroundAlpha(0.6F);
		
		
		Random rand = new Random();
		
		double[] vals = new double[100];
		
		for(int i = 0; i< vals.length; i++) vals[i]= rand.nextInt(100);
		
		
		
		Random rand2 = new Random();
		double[] vals2 = new double[100];
		for(int i = 0; i< vals2.length; i++) vals2[i]= rand2.nextDouble();
		
		
		frame.getContentPane().add(new ChartPanel(chart), BorderLayout.CENTER);
		
		frame.setSize(1000, 500);
		
		frame.setVisible(true);
		
		System.out.println("Hallo");
		dataSet.addSeries("Bert", vals, 100, 0, 1);
		dataSet.addSeries("Ernie", vals2, 100, 0, 100);
		double[] vals3 = new double[100];
		for(int i = 0; i< vals3.length; i++) vals3[i]= rand.nextInt(100);
		dataSet.addSeries("Lilo", vals3, 100, 0, 100);
		yourXYPlot.getRangeAxis().setAutoRange(false);
		yourXYPlot.getRangeAxis().setAutoRange(true);
		yourXYPlot.getDomainAxis().setAutoRange(false);
		yourXYPlot.getDomainAxis().setAutoRange(true);
		
		
		
	}

}
