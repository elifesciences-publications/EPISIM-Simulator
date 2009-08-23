package sim.app.episim.datamonitoring.calc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Random;

import javax.swing.JFrame;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;



public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		JFrame frame = new JFrame("Test");
		JFreeChart chart;
		SimpleHistogramDataset dataSet = new SimpleHistogramDataset("Ernie");
		SimpleHistogramDataset dataSet2 = new SimpleHistogramDataset("Bert");
		SimpleHistogramDataset dataSet3 = new SimpleHistogramDataset("Lilo");
		
		SimpleHistogramBin[] bins = buildBins(0, 3, 100);
		for(SimpleHistogramBin bin: bins) dataSet.addBin(bin);
		//for(SimpleHistogramBin bin: buildBins(0, 100, 100)) dataSet2.addBin(bin);
		//for(SimpleHistogramBin bin: buildBins(0, 100, 100)) dataSet3.addBin(bin);
		
		
		
		chart = ChartFactory.createHistogram("Test", "TestX", "TestY", null, PlotOrientation.VERTICAL, true, true, false);
		
		XYPlot plot = chart.getXYPlot();
		plot.setDataset(0, dataSet);
		//plot.setDataset(1, dataSet2);
		//plot.setDataset(2, dataSet3);
		XYBarRenderer renderer = new XYBarRenderer();
	//	XYBarRenderer renderer2 = new XYBarRenderer();
	//	XYBarRenderer renderer3 = new XYBarRenderer();
		renderer.setSeriesPaint(0, Color.BLUE);
	//	renderer2.setSeriesPaint(0, Color.RED);
		//renderer3.setSeriesPaint(0, Color.YELLOW);
		plot.setRenderer(0, renderer);
	//	plot.setRenderer(1, renderer2);
	//	plot.setRenderer(2, renderer3);
		
		plot.setForegroundAlpha(0.6F);
frame.getContentPane().add(new ChartPanel(chart), BorderLayout.CENTER);
		
		frame.setSize(1000, 500);
		
		frame.setVisible(true);
	
		
	//	XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
		
		
		
		Random rand = new Random();
		
	
		frame.repaint();
		try{
	      Thread.sleep(5000);
      }
      catch (InterruptedException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		for(int i = 0; i< 100; i++) dataSet.addObservation(i*0.03);
		frame.repaint();
		
		int totalItems = 0;
		for(int i = 0; i < bins.length; i++){
			System.out.println("Bins number "+i+" contains "+ bins[i].getItemCount()+ "items");
			totalItems += bins[i].getItemCount();
		}
		System.out.println("Total number of items " + totalItems);
		
		try{
	      Thread.sleep(3000);
      }
      catch (InterruptedException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		
		dataSet.setAdjustForBinSize(false);
		frame.repaint();
	/*	try{
	      Thread.sleep(3000);
      }
      catch (InterruptedException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
        
		for(int i = 0; i< 100; i++) dataSet2.addObservation(rand.nextInt(100));
		frame.repaint();
		try{
	      Thread.sleep(3000);
      }
      catch (InterruptedException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
		for(int i = 0; i< 100; i++) dataSet3.addObservation(rand.nextInt(100));
		frame.repaint();
		
		
		
		try{
	      Thread.sleep(3000);
      }
      catch (InterruptedException e){
	      // TODO Auto-generated catch block
	      e.printStackTrace();
      }
      dataSet.clearObservations();
      frame.repaint(); */
          
	}
	
	private static SimpleHistogramBin[] buildBins(double minValue, double maxValue, int numberOfBins){
				
		if(minValue > maxValue){
			double tmp = minValue;
			minValue = maxValue;
			maxValue = tmp;
		}		
		double binSize = Math.abs(maxValue - minValue) / numberOfBins;
		SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins];		
		
		for(int i = 0; i < numberOfBins; i ++){
			if(i == 0) bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, true);
			else bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), false, true);
		}
		
		return bins;
	}

}
