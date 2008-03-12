package sim.app.episim.charts;
//Charts
import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jfree.data.xy.XYSeries;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ChartFactory;

import org.jfree.data.statistics.HistogramDataset; // Histogram

import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;

import org.jfree.data.statistics.HistogramType;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import org.jfree.data.category.DefaultCategoryDataset;

import org.jfree.chart.*;

import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.engine.Steppable;

public class DefaultCharts implements SnapshotListener,java.io.Serializable{
	
   //Schlüssel setzt sich XYSeriesCollection-Name Position 0 und XYSeries-Name zusammen
	private HashMap<String[], XYSeries> xySeries = new HashMap<String[], XYSeries>();
	private HashMap<String, XYSeriesCollection> xySeriesCollections = new HashMap<String, XYSeriesCollection>();
	private HashMap<String, DefaultCategoryDataset> categoryDatasets = new HashMap<String, DefaultCategoryDataset>();
	private HashMap<String, JFreeChart> charts = new HashMap<String, JFreeChart>();
	
	private List<EnhancedSteppable> defaultSteppables;
	
	
	private static  DefaultCharts instance;
	
	private DefaultCharts() {
		defaultSteppables = new ArrayList<EnhancedSteppable>();
		addDefaultSteppables();
	SnapshotWriter.getInstance().addSnapshotListener(this);
		XYLineAndShapeRenderer lineShapeRenderer;
		JFreeChart chart;
		XYPlot xyPlot;
		ValueAxis yAxis;
		NumberAxis axis2;
		XYItemRenderer rendererXYItem;
		CategoryPlot categoryPlot;
		BarRenderer barRenderer;
		NumberAxis rangeAxis;
		
      /////////////////////////////////////
		// Charts: Performance Statistics
		/////////////////////////////////////

		xySeries.put(new String[] { "Steps_Time", "Performance_Series" }, new XYSeries("Steps / Time"));
		
		xySeries.put(new String[] { "Num_Cells_Steps", "Performance_Series_Num_Cells" }, new XYSeries("Steps / Time"));
		
		xySeriesCollections.put("Performance_Series", new XYSeriesCollection());
		xySeriesCollections.put("Performance_Series_Num_Cells", new XYSeriesCollection());
		

		chart = ChartFactory.createXYLineChart("Performance", "Steps", "Steps per time", 
				xySeriesCollections.get("Performance_Series"), PlotOrientation.VERTICAL, true, true, false); 		
		
		chart.setBackgroundPaint(Color.white);
		
		xyPlot = chart.getXYPlot();
	   yAxis = xyPlot.getRangeAxis();
		
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.red);
		
		//	 Second Vertical Axis
		axis2 = new NumberAxis("Number of Cells");
		axis2.setLabelPaint(Color.darkGray);
		axis2.setTickLabelPaint(Color.darkGray);
		xyPlot.setRangeAxis(1, axis2);
		xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

		// Second Dataset
		xyPlot.setDataset(1, xySeriesCollections.get("Performance_Series_Num_Cells"));
		xyPlot.mapDatasetToRangeAxis(1, 1);

		// Renderer for Second Dataset
		rendererXYItem = new StandardXYItemRenderer();
		rendererXYItem.setSeriesPaint(0, Color.darkGray);
		xyPlot.setRenderer(1, rendererXYItem);
		
		
		
		charts.put("Performance", chart);

		// ///////////////////////////////////
		// Charts: NumCells
		// ///////////////////////////////////

		xySeries.put(new String[] { "ChartSeries_KCyte_All", "ChartSeries_KCytes" }, new XYSeries("All Cells"));
		xySeries.put(new String[] { "ChartSeries_KCyte_Spi", "ChartSeries_KCytes" }, new XYSeries("Early Spinosum"));
		xySeries.put(new String[] { "ChartSeries_KCyte_LateSpi", "ChartSeries_KCytes" }, new XYSeries("Late Spinosum"));
		xySeries.put(new String[] { "ChartSeries_KCyte_Granu", "ChartSeries_KCytes" }, new XYSeries("Granulosum"));
		xySeries.put(new String[] { "ChartSeries_KCyte_TA", "ChartSeries_KCytes" }, new XYSeries("Transit Amplifying"));
		xySeries.put(new String[] { "ChartSeries_KCyte_NoNuc", "ChartSeries_KCytes" }, new XYSeries("NoNucleus"));
		xySeries.put(new String[] { "ChartSeries_KCyte_MeanAgeDate", "ChartSeries_MeanAgeColl" },
				new XYSeries("Mean Age"));
		xySeriesCollections.put("ChartSeries_KCytes", new XYSeriesCollection());
		xySeriesCollections.put("ChartSeries_MeanAgeColl", new XYSeriesCollection());

		chart = ChartFactory.createXYLineChart("Cell Counts", "Time in h", "Cell Number", 
				xySeriesCollections.get("ChartSeries_KCytes"), PlotOrientation.VERTICAL, true, true, false); 		
		
		chart.setBackgroundPaint(Color.white);
		
		xyPlot = chart.getXYPlot();
	   yAxis = xyPlot.getRangeAxis();
		
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.red);
		lineShapeRenderer.setSeriesPaint(1, Color.green);
		lineShapeRenderer.setSeriesPaint(2, Color.orange);
		lineShapeRenderer.setSeriesPaint(3, Color.blue);
		lineShapeRenderer.setSeriesPaint(4, Color.lightGray);
		
		// Second Vertical Axis
		axis2 = new NumberAxis("Mean Age in h");
		axis2.setLabelPaint(Color.darkGray);
		axis2.setTickLabelPaint(Color.darkGray);
		xyPlot.setRangeAxis(1, axis2);
		xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

		// Second Dataset
		xyPlot.setDataset(1, xySeriesCollections.get("ChartSeries_MeanAgeColl"));
		xyPlot.mapDatasetToRangeAxis(1, 1);

		// Renderer for Second Dataset
		rendererXYItem = new StandardXYItemRenderer();
		rendererXYItem.setSeriesPaint(0, Color.darkGray);
		xyPlot.setRenderer(1, rendererXYItem);
		
		
		charts.put("NumCells", chart);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		 /////////////////////////////////////
	   // Charts: Kinetics Statistics
	   /////////////////////////////////////
		
		xySeries.put(new String[] { "ChartSeries_Kinetics_GrowthFraction", "ChartSeries_Kinetics100Coll" }, new XYSeries( "Growth Fraction" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_MeanCycleTime", "ChartSeries_Kinetics100Coll" }, new XYSeries( "Mean Cell Cycle Time" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_Turnover", "ChartSeries_Kinetics2000Coll" }, new XYSeries( "Turnover Time" ));
		
		xySeriesCollections.put("ChartSeries_Kinetics100Coll", new XYSeriesCollection());
		xySeriesCollections.put("ChartSeries_Kinetics2000Coll", new XYSeriesCollection());
	   
		chart = ChartFactory.createXYLineChart("Tissue Kinetic Parameters",  "Time in h", "Fraction (%) / Time", 
		   		xySeriesCollections.get("ChartSeries_Kinetics100Coll"), PlotOrientation.VERTICAL, true, true, false);                                               
    
      chart.setBackgroundPaint(Color.white);
              
      xyPlot = chart.getXYPlot();
      yAxis = xyPlot.getRangeAxis();
      yAxis.setLabelPaint(Color.red);
      lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
      lineShapeRenderer.setSeriesPaint(0, Color.red);
      lineShapeRenderer.setSeriesPaint(1, Color.green);   
    
      // Second Vertical Axis
      axis2 = new NumberAxis("Turnover time in h");
      axis2.setLabelPaint(Color.darkGray);
      axis2.setTickLabelPaint(Color.darkGray);        
      xyPlot.setRangeAxis(1, axis2);
	   xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
	
	   // Second Dataset
	   xyPlot.setDataset(1, xySeriesCollections.get("ChartSeries_Kinetics2000Coll"));
	   xyPlot.mapDatasetToRangeAxis(1, 1);

	   // Renderer for Second Dataset
	   rendererXYItem = new StandardXYItemRenderer();
	   rendererXYItem.setSeriesPaint(0, Color.darkGray);
	   xyPlot.setRenderer(1, rendererXYItem);        
	   
	   charts.put("KineticsStatistics", chart);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	   /////////////////////////////////////
	   // Charts: Barrier
	   /////////////////////////////////////
	   
	   xySeries.put(new String[] { "ChartSeries_Barrier_Calcium", "ChartSeries_Barrier" }, new XYSeries( "Barrier Calcium (mg/kg)" ));
	   xySeries.put(new String[] { "ChartSeries_Barrier_Lamella", "ChartSeries_Barrier" }, new XYSeries( "Barrier Lamella" ));
	   xySeries.put(new String[] { "ChartSeries_Barrier_Lipids", "ChartSeries_Barrier" }, new XYSeries( "Barrier Lipids" ));
	   
	   xySeriesCollections.put("ChartSeries_Barrier", new XYSeriesCollection());
	   
	   chart = ChartFactory.createXYLineChart("Particle Concentrations in Barrier", "Time in h", "Concentration", 
	   		 xySeriesCollections.get("ChartSeries_Barrier"), PlotOrientation.VERTICAL, true, true, false);                                               

		chart.setBackgroundPaint(Color.white);
		 
		 xyPlot = chart.getXYPlot();
		 xyPlot.setBackgroundPaint(Color.white);
		 xyPlot.setDomainGridlinePaint(Color.white);
		 xyPlot.setDomainGridlinesVisible(true);
		 xyPlot.setRangeGridlinePaint(Color.white);
		
		 yAxis = xyPlot.getRangeAxis();
		 lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		 lineShapeRenderer.setSeriesPaint(0, Color.blue);
		 lineShapeRenderer.setSeriesPaint(1, Color.green);   // 0 = Calcium
		 lineShapeRenderer.setSeriesPaint(2, Color.red);    // 2 = Lamella
		 
		 charts.put("Barrier", chart);
			
		 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    
		 /////////////////////////////////////
		 // Charts: Apopotosis
		 /////////////////////////////////////
		 
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_Basal", "ChartSeries_Apoptosis" }, new XYSeries( "Basal" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_EarlySpi", "ChartSeries_Apoptosis" }, new XYSeries( "EarlySpi" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_LateSpi", "ChartSeries_Apoptosis" }, new XYSeries( "LateSpi" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_Granu", "ChartSeries_Apoptosis" }, new XYSeries( "Granu" ));
		 
		 xySeriesCollections.put("ChartSeries_Apoptosis", new XYSeriesCollection());
		 
		 chart = ChartFactory.createXYLineChart("Cell Death", "Time in h", "Percentage",  
				 xySeriesCollections.get("ChartSeries_Apoptosis"), PlotOrientation.VERTICAL, true, true, false);   
		
		 chart.setBackgroundPaint(Color.white);
		 
		 xyPlot = chart.getXYPlot();
		 xyPlot.setBackgroundPaint(Color.white);
		 xyPlot.setDomainGridlinePaint(Color.white);
		 xyPlot.setDomainGridlinesVisible(true);
		 xyPlot.setRangeGridlinePaint(Color.white);
		  
		
		 yAxis = xyPlot.getRangeAxis();
		 lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		 lineShapeRenderer.setSeriesPaint(0, Color.blue);
		 lineShapeRenderer.setSeriesPaint(1, Color.magenta);
		 lineShapeRenderer.setSeriesPaint(2, Color.red);
		 lineShapeRenderer.setSeriesPaint(3, Color.green);
	  
		 charts.put("Apoptosis", chart);
			
		 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		 /////////////////////////////////////
		 // Charts: ParticleCellType
		 /////////////////////////////////////	
		 categoryDatasets.put("particleCellTypeDataset", new DefaultCategoryDataset()); 
		 categoryDatasets.get("particleCellTypeDataset").clear();
	    
		 chart = ChartFactory.createBarChart("Particles per Cell Type", "Cell Type", "Concentration",  
				 categoryDatasets.get("particleCellTypeDataset"), PlotOrientation.VERTICAL, true, true, false);

	    chart.setBackgroundPaint(Color.white);

	      
	    categoryPlot = chart.getCategoryPlot();
	    categoryPlot.setBackgroundPaint(Color.white);
	    categoryPlot.setDomainGridlinePaint(Color.white);
	    categoryPlot.setDomainGridlinesVisible(true);
	    categoryPlot.setRangeGridlinePaint(Color.white);

	       
	    rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
	    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	     

	       // disable bar outlines...
	       barRenderer =  (BarRenderer) categoryPlot.getRenderer();
	       barRenderer.setDrawBarOutline(false);
	       
	       // set up gradient paints for series...
	       GradientPaint gp0 = new GradientPaint(
	           0.0f, 0.0f, Color.blue, 
	           0.0f, 0.0f, new Color(0, 0, 64)
	       );
	       GradientPaint gp1 = new GradientPaint(
	           0.0f, 0.0f, Color.green, 
	           0.0f, 0.0f, new Color(0, 64, 0)
	       );
	       GradientPaint gp2 = new GradientPaint(
	           0.0f, 0.0f, Color.red, 
	           0.0f, 0.0f, new Color(64, 0, 0)
	       );
	       barRenderer.setSeriesPaint(0, gp0);    // External Calcium
	       barRenderer.setSeriesPaint(1, gp1);
	       barRenderer.setSeriesPaint(2, gp2);

	       CategoryAxis domainAxis = categoryPlot.getDomainAxis();
	       domainAxis.setCategoryLabelPositions(
	           CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
	       );
	  
	  charts.put("ParticleCellType", chart);
				
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	  	///////////////////////////////////////////////////
	   // Charts: LineChartParticleDistributions
	   ///////////////////////////////////////////////////
	  
      xySeries.put(new String[] { "ExtCalConcAvg", "CollPartDist" }, new XYSeries("Mean Ext. Calcium (mg/kg)"));
      xySeries.put(new String[] { "LamellaConcAvg", "CollPartDist" }, new XYSeries("Mean Lamella"));
      xySeries.put(new String[] { "LipidsConcAvg", "CollPartDist" }, new XYSeries("Mean Lipids"));
     
      xySeries.put(new String[] { "Num", "CollNum" }, new XYSeries("Num Cells")); 
        
      xySeriesCollections.put("CollPartDist", new XYSeriesCollection());
      
      xySeriesCollections.put("CollNum", new XYSeriesCollection());
      
      chart = ChartFactory.createXYLineChart("Particle Gradients", "Depth (µm)", "Concentration", 
      		xySeriesCollections.get("CollPartDist"), PlotOrientation.VERTICAL, true, true, false);
      
      chart.setBackgroundPaint(Color.white);
      
      xyPlot = chart.getXYPlot();
      
      xyPlot.setDomainGridlinesVisible(true);
      
      rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // change the auto tick unit selection to integer units only...
      rangeAxis.setLabelPaint(Color.black);

      // Line Renderer for First Dataset
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
      
      renderer.setSeriesPaint(0, Color.blue);    // 0 = Ext Calcium
      renderer.setSeriesPaint(1, Color.green);      // 1 = Lamelle
      renderer.setSeriesPaint(2, Color.red);    // 2 = Lipids        
 
      // Second Vertical Axis
      axis2 = new NumberAxis("Number of Cells");
      axis2.setLabelPaint(Color.black);
      axis2.setTickLabelPaint(Color.black);
      xyPlot.setRangeAxis(1, axis2);
      xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

      // Second Dataset
      xyPlot.setDataset(1, xySeriesCollections.get("CollNum"));
      xyPlot.mapDatasetToRangeAxis(1, 1);

      // Renderer for Second Dataset
      StandardXYItemRenderer rendererXYItem2 = new StandardXYItemRenderer();
      rendererXYItem2.setSeriesPaint(0, Color.black);
      xyPlot.setRenderer(1, rendererXYItem2);
      
      charts.put("LineChartParticleDistributions", chart);
		
 	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
      ///////////////////////////////////////////////////
      // Charts: LineChart AgeDistribution
      /////////////////////////////////////////////////////
      xySeries.put(new String[] { "AgeAvg", "CollAge" }, new XYSeries("Mean Age")); 
      
      xySeriesCollections.put("CollAge", new XYSeriesCollection());
      
      chart = ChartFactory.createXYLineChart("Age Gradient", "Depth (µm)", "Age in h",
      		xySeriesCollections.get("CollAge"), PlotOrientation.VERTICAL, true, true, false);

      chart.setBackgroundPaint(Color.white);
      
      xyPlot = chart.getXYPlot();
      xyPlot.setDomainGridlinesVisible(true);
      
      // First Vertical Axis
      rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      rangeAxis.setLabelPaint(Color.blue);

      // Line Renderer for First Dataset
      lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
      lineShapeRenderer.setSeriesPaint(0, Color.blue);    // 0 = Calcium
      
      
      // Second Vertical Axis
      axis2 = new NumberAxis("Number of Cells");
      axis2.setLabelPaint(Color.black);
      axis2.setTickLabelPaint(Color.black);
      xyPlot.setRangeAxis(1, axis2);
      xyPlot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

      // Second Dataset
      xyPlot.setDataset(1, xySeriesCollections.get("CollNum"));
      xyPlot.mapDatasetToRangeAxis(1, 1);

      // Renderer for Second Dataset
      XYItemRenderer renderer2 = new StandardXYItemRenderer();
      renderer2.setSeriesPaint(0, Color.black);
      xyPlot.setRenderer(1, renderer2);
      
      charts.put("AgeDistribution", chart);
		
  	   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       
      
		addXYSeriesToCollections();
	}
	/**
	 * Fügt die Series Objekte in die Collections ein
	 *
	 */
	private void addXYSeriesToCollections(){
		Set keySetXYSeries = xySeries.keySet();
		Iterator<String[]> iter = keySetXYSeries.iterator();
		String [] actKey;
		while(iter.hasNext()){
			actKey = iter.next();
			if(actKey.length == 2 && xySeriesCollections.containsKey(actKey[1])){ 
				if(xySeriesCollections.get(actKey[1]) != null){ 
					
					xySeriesCollections.get(actKey[1]).addSeries(xySeries.get(actKey));
				}
			}
			
		}
		
	}
	
	public XYSeries getXYSeries(String name){
		Set keySet = xySeries.keySet();
		Iterator <String[]> iter = keySet.iterator();
		
		
		while(iter.hasNext()){
			String[] key = iter.next();
			if(key.length ==2 && key[0].equals(name)) return xySeries.get(key);
		}
		
		return null;
	}
	
	public XYSeriesCollection getXYSeriesCollection(String name){
		return xySeriesCollections.get(name);
	}
	
	
	/////////////////////////////////////
   // Charts: Steps / tick
   /////////////////////////////////////
  
   public JFreeChart getPerformanceChart()
   {
         
       return charts.get("Performance");
   }
	
	
	
	
	/////////////////////////////////////
   // Charts: NumCells
   /////////////////////////////////////
  
   public JFreeChart getNumCellsChart()
   {
         
       return charts.get("NumCells");
   }

   /////////////////////////////////////
   // Charts: Kinetics Statistics
   /////////////////////////////////////
      
   public JFreeChart getKineticsChart()
   {
       
       return charts.get("KineticsStatistics");
   }
   
   /////////////////////////////////////
   // Charts: Barrier
   /////////////////////////////////////
   
   public JFreeChart getBarrierChart()
   {
       return charts.get("Barrier");
   }
   
   /////////////////////////////////////
   // Charts: Apopotosis
   /////////////////////////////////////
   
   public JFreeChart getApoptosisChart()
   {
         return charts.get("Apoptosis");
   }

   /////////////////////////////////////
   // Charts: ParticleCellType
   /////////////////////////////////////	
       
   public JFreeChart getParticleCellTypeChart() {
 
       return charts.get("ParticleCellType");
   }
      
   ///////////////////////////////////////////////////
   // Charts: LineChart Particle Distributions
   /////////////////////////////////////////////////////

   public JFreeChart getParticleDistribution() {
   
   	return charts.get("LineChartParticleDistributions");
   }
   
   ///////////////////////////////////////////////////
   // Charts: LineChart Age Distribution
   /////////////////////////////////////////////////////
   
   public JFreeChart getAgeDistribution() {
       
       return charts.get("AgeDistribution");
   }
       
   public DefaultCategoryDataset getDefaultCategoryDataset(String name){
   	return categoryDatasets.get(name);
   }
	
	protected static synchronized DefaultCharts getInstance(){
		if(instance == null) instance = new DefaultCharts();
		
		return instance;
	}
	
	public void clearSeries(){
	 Collection<XYSeries> col =	xySeries.values();
	 Iterator<XYSeries> iter = col.iterator();
	 while(iter.hasNext()) iter.next().clear();
	}
	
	public static synchronized void  rebuildCharts(){
		instance = new DefaultCharts();
	}
	public List<SnapshotObject> getSnapshotObjects() {

		List<SnapshotObject> list = new LinkedList<SnapshotObject>();
		
		
		
			
		
		list.add(new SnapshotObject(SnapshotObject.CHARTS, this));
		return list;
	}
	
	protected static void setInstance(DefaultCharts charts){
		instance = charts;
	}
	
	private void addDefaultSteppables(){
		this.defaultSteppables.add(new EnhancedSteppable()
	    {
	        private long previousTime = 0;
	        private long previousSteps = 0;
	   	  
	   	  public void step(SimState state)
	         {   
	         	
	   		   if(state.schedule.getSteps() > 400){
	   		   	long actTime = System.currentTimeMillis()/1000;
	         	long actSteps = state.schedule.getSteps();
	   		   long deltaTime = actTime - previousTime;
	   		   long deltaSteps = actSteps - previousSteps;
	   		   
	   		   previousTime = actTime;
	   		   previousSteps = actSteps;
	   		   if(deltaTime > 0){
	   		   double stepsPerTime = deltaSteps/deltaTime;
	         	getXYSeries("Steps_Time").add(state.schedule.getSteps(), stepsPerTime);
	   		   //getXYSeries("Num_Cells_Steps").add(state.schedule.getSteps(), actualKCytes);
	   		   }
	   		   }	
	   		   
	             
	         }

			public double getInterval() {

	         // TODO Auto-generated method stub
	         return 0;
         }
	     });
	}
	
}
