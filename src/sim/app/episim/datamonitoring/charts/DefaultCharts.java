package sim.app.episim.datamonitoring.charts;
//Charts
import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import org.jfree.data.category.DefaultCategoryDataset;

import org.jfree.chart.*;

import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;

import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.controller.ModelController;

import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.engine.Steppable;

public class DefaultCharts implements java.io.Serializable{
	
	
	private double TIMEFACTOR=0.5;   // conversion from timeticks to h for all diagrams: 2 time ticks mean 1 hour
	
	private int CORNEUMY=20;
	
   //Schlüssel setzt sich XYSeriesCollection-Name Position 0 und XYSeries-Name zusammen
	private Map<String[], XYSeries> xySeries; 
	private Map<String, XYSeriesCollection> xySeriesCollections = new HashMap<String, XYSeriesCollection>();
	private Map<String, DefaultCategoryDataset> categoryDatasets = new HashMap<String, DefaultCategoryDataset>();
	private Map<String, ChartPanel> chartsMap = new HashMap<String, ChartPanel>();
	private Map<String, EnhancedSteppable> steppablesMap = new HashMap<String, EnhancedSteppable>();
	private HashMap<String, Boolean> chartEnabled = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> chartEnabledOld = new HashMap<String, Boolean>();
	
	
	
	//Available Default Charts
	private final String PERFORMANCE = "Performance";
	private final String CELLCOUNTS = "Cell Counts";
	private final String TISSUEKINETICPARAMETERS = "Tissue Kinetic Parameters";
	private final String PARTICLECONCENTRATIONSINBARRIER = "Particle Concentrations in Barrier";
	private final String CELLDEATH = "Cell Death";
	private final String PARTICLESPERCELLTYPE = "Particles per Cell Type";
	private final String PARTICLEGRADIENTS = "Particle Gradients";
	private final String AGEGRADIENT = "Age Gradient";
	
	private final String DNAHISTOGRAMM = "DNA Histogramm";
	private final String DNAHISTOGRAMMAVG = "DNA Histogramm Averaged";
	
	private static  DefaultCharts instance;
	
	
	private class SeriesComparator implements Comparator<String[]>, java.io.Serializable{

		public int compare(String[] o1, String[] o2) {

         if(o1.length < 3 || o2.length < 3) return 0;
         else return (o1[2].concat(o1[0])).compareTo(o2[2].concat(o2[0]));
      }
	}
	
	
	private DefaultCharts() {
		initChartActivationMap();
		addDefaultSteppables();
		
		
		
		
		
		XYLineAndShapeRenderer lineShapeRenderer;
		JFreeChart chart;
		XYPlot xyPlot;
		ValueAxis yAxis;
		NumberAxis axis2;
		XYItemRenderer rendererXYItem;
		CategoryPlot categoryPlot;
		BarRenderer barRenderer;
		NumberAxis rangeAxis;
		
		//---------------------------------------------------------------------------------------------------------------------------
		// Initialize TreeMap
		//-----------------------------------------------------------------------------------------------------------------------
		
	this.xySeries = new TreeMap<String[], XYSeries>(new SeriesComparator());
		
		////////////////////////////////////////////
		// Charts: DNA content Histogramm Averaged
		////////////////////////////////////////////
		xySeries.put(new String[] { "DNA_Content_AVG", "DNA_Content_Series_AVG", "0"}, new XYSeries("DNA content averaged"));
		
		xySeriesCollections.put("DNA_Content_Series_AVG", new XYSeriesCollection());
		
		

		chart = ChartFactory.createXYLineChart(DNAHISTOGRAMMAVG, "DNA content averaged", "number of cells", 
				xySeriesCollections.get("DNA_Content_Series_AVG"), PlotOrientation.VERTICAL, true, true, false); 		
		
		chart.setBackgroundPaint(Color.white);
		chart.setAntiAlias(true);
		
		
		xyPlot = chart.getXYPlot();
	   yAxis = xyPlot.getRangeAxis();
		//xyPlot.setRangeAxis(new LogarithmicAxis(yAxis.getLabel()));
	   
	   
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.black);
		
		chartsMap.put(DNAHISTOGRAMMAVG, new ChartPanel(chart));
		
		
		
		/////////////////////////////////////
		// Charts: DNA content Histogramm
		/////////////////////////////////////
		xySeries.put(new String[] { "DNA_Content", "DNA_Content_Series", "0"}, new XYSeries("DNA content"));
		
		xySeriesCollections.put("DNA_Content_Series", new XYSeriesCollection());
		
		

		chart = ChartFactory.createXYLineChart(DNAHISTOGRAMM, "DNA content", "number of cells", 
				xySeriesCollections.get("DNA_Content_Series"), PlotOrientation.VERTICAL, true, true, false); 		
		
		chart.setBackgroundPaint(Color.white);
		
		xyPlot = chart.getXYPlot();
	   yAxis = xyPlot.getRangeAxis();
		
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.black);
		
		chartsMap.put(DNAHISTOGRAMM, new ChartPanel(chart));
		
      /////////////////////////////////////
		// Charts: Performance Statistics
		/////////////////////////////////////

		xySeries.put(new String[] { "Steps_Time", "Performance_Series", "0"}, new XYSeries("Steps / Time"));
		
		xySeries.put(new String[] { "Num_Cells_Steps", "Performance_Series_Num_Cells", "1"}, new XYSeries("Number Of Cells"));
		
		xySeriesCollections.put("Performance_Series", new XYSeriesCollection());
		xySeriesCollections.put("Performance_Series_Num_Cells", new XYSeriesCollection());
		

		chart = ChartFactory.createXYLineChart(PERFORMANCE, "Steps", "Steps per time", 
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
		
		
		
		chartsMap.put(PERFORMANCE, new ChartPanel(chart));

		// ///////////////////////////////////
		// Charts: NumCells
		// ///////////////////////////////////

		xySeries.put(new String[] { "ChartSeries_KCyte_All", "ChartSeries_KCytes", "0" }, new XYSeries("All Cells"));
		xySeries.put(new String[] { "ChartSeries_KCyte_Spi", "ChartSeries_KCytes", "1" }, new XYSeries("Early Spinosum"));
		xySeries.put(new String[] { "ChartSeries_KCyte_LateSpi", "ChartSeries_KCytes", "2" }, new XYSeries("Late Spinosum"));
		xySeries.put(new String[] { "ChartSeries_KCyte_Granu", "ChartSeries_KCytes", "3" }, new XYSeries("Granulosum"));
		xySeries.put(new String[] { "ChartSeries_KCyte_TA", "ChartSeries_KCytes", "4" }, new XYSeries("Transit Amplifying"));
		xySeries.put(new String[] { "ChartSeries_KCyte_MeanAgeDate", "ChartSeries_MeanAgeColl", "5" },
				new XYSeries("Mean Age"));
		xySeriesCollections.put("ChartSeries_KCytes", new XYSeriesCollection());
		xySeriesCollections.put("ChartSeries_MeanAgeColl", new XYSeriesCollection());

		chart = ChartFactory.createXYLineChart(CELLCOUNTS, "Time in h", "Cell Number", 
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
		
		
		chartsMap.put(CELLCOUNTS, new ChartPanel(chart));
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		 /////////////////////////////////////
	   // Charts: Kinetics Statistics
	   /////////////////////////////////////
		
		xySeries.put(new String[] { "ChartSeries_Kinetics_GrowthFraction", "ChartSeries_Kinetics100Coll", "0" }, new XYSeries( "Growth Fraction" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_MeanCycleTime", "ChartSeries_Kinetics100Coll", "1" }, new XYSeries( "Mean Cell Cycle Time" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_Turnover", "ChartSeries_Kinetics2000Coll", "2" }, new XYSeries( "Turnover Time" ));
		
		xySeriesCollections.put("ChartSeries_Kinetics100Coll", new XYSeriesCollection());
		xySeriesCollections.put("ChartSeries_Kinetics2000Coll", new XYSeriesCollection());
	   
		chart = ChartFactory.createXYLineChart(TISSUEKINETICPARAMETERS,  "Time in h", "Fraction (%) / Time", 
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
	   
	   chartsMap.put(TISSUEKINETICPARAMETERS, new ChartPanel(chart));
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	   /////////////////////////////////////
	   // Charts: Barrier
	   /////////////////////////////////////
	   
	   xySeries.put(new String[] { "ChartSeries_Barrier_Calcium", "ChartSeries_Barrier", "0" }, new XYSeries( "Barrier Calcium (mg/kg)" ));
	   xySeries.put(new String[] { "ChartSeries_Barrier_Lamella", "ChartSeries_Barrier", "1" }, new XYSeries( "Barrier Lamella" ));
	   xySeries.put(new String[] { "ChartSeries_Barrier_Lipids", "ChartSeries_Barrier", "2" }, new XYSeries( "Barrier Lipids" ));
	   
	   xySeriesCollections.put("ChartSeries_Barrier", new XYSeriesCollection());
	   
	   chart = ChartFactory.createXYLineChart(PARTICLECONCENTRATIONSINBARRIER, "Time in h", "Concentration", 
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
		 
		 chartsMap.put(PARTICLECONCENTRATIONSINBARRIER, new ChartPanel(chart));
			
		 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    
		 /////////////////////////////////////
		 // Charts: Apopotosis
		 /////////////////////////////////////
		 
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_Basal", "ChartSeries_Apoptosis", "0" }, new XYSeries( "Basal" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_EarlySpi", "ChartSeries_Apoptosis", "1" }, new XYSeries( "EarlySpi" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_LateSpi", "ChartSeries_Apoptosis", "2" }, new XYSeries( "LateSpi" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_Granu", "ChartSeries_Apoptosis", "3" }, new XYSeries( "Granu" ));
		 
		 xySeriesCollections.put("ChartSeries_Apoptosis", new XYSeriesCollection());
		 
		 chart = ChartFactory.createXYLineChart(CELLDEATH, "Time in h", "Percentage",  
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
	  
		 chartsMap.put(CELLDEATH, new ChartPanel(chart));
			
		 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
		 /////////////////////////////////////
		 // Charts: ParticleCellType
		 /////////////////////////////////////	
		 categoryDatasets.put("particleCellTypeDataset", new DefaultCategoryDataset()); 
		 categoryDatasets.get("particleCellTypeDataset").clear();
	    
		 chart = ChartFactory.createBarChart(PARTICLESPERCELLTYPE, "Cell Type", "Concentration",  
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
	  
	  chartsMap.put(PARTICLESPERCELLTYPE, new ChartPanel(chart));
				
	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	  	///////////////////////////////////////////////////
	   // Charts: LineChartParticleDistributions
	   ///////////////////////////////////////////////////
	  
      xySeries.put(new String[] { "ExtCalConcAvg", "CollPartDist", "0" }, new XYSeries("Mean Ext. Calcium (mg/kg)"));
      xySeries.put(new String[] { "LamellaConcAvg", "CollPartDist", "1" }, new XYSeries("Mean Lamella"));
      xySeries.put(new String[] { "LipidsConcAvg", "CollPartDist", "2" }, new XYSeries("Mean Lipids"));     
      xySeries.put(new String[] { "Num", "CollNum", "3"}, new XYSeries("Num Cells")); 
        
      xySeriesCollections.put("CollPartDist", new XYSeriesCollection());
      
      xySeriesCollections.put("CollNum", new XYSeriesCollection());
      
      chart = ChartFactory.createXYLineChart(PARTICLEGRADIENTS, "Depth (µm)", "Concentration", 
      		xySeriesCollections.get("CollPartDist"), PlotOrientation.VERTICAL, true, true, false);
      
      chart.setBackgroundPaint(Color.white);
      
      xyPlot = chart.getXYPlot();
      
      xyPlot.setDomainGridlinesVisible(true);
      
      rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
      rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // change the auto tick unit selection to integer units only...
      rangeAxis.setLabelPaint(Color.black);

      // Line Renderer for First Dataset
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
      
      renderer.setSeriesPaint(0, Color.blue);     // 0 = Ext Calcium
      renderer.setSeriesPaint(1, Color.green);   // 1 = Lamelle
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
      
      chartsMap.put(PARTICLEGRADIENTS, new ChartPanel(chart));
		
 	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
      ///////////////////////////////////////////////////
      // Charts: LineChart AgeDistribution
      /////////////////////////////////////////////////////
      xySeries.put(new String[] { "AgeAvg", "CollAge", "0" }, new XYSeries("Mean Age")); 
      
      xySeriesCollections.put("CollAge", new XYSeriesCollection());
      
      chart = ChartFactory.createXYLineChart(AGEGRADIENT, "Depth (µm)", "Age in h",
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
      
      chartsMap.put(AGEGRADIENT, new ChartPanel(chart));
		
  	   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
       
      
		addXYSeriesToCollections();
	}
	
	private void initChartActivationMap(){
		
		chartEnabled.put(PERFORMANCE, false);
		chartEnabled.put(CELLCOUNTS, false);
		chartEnabled.put(TISSUEKINETICPARAMETERS, false);
		chartEnabled.put(PARTICLECONCENTRATIONSINBARRIER, false);
		chartEnabled.put(CELLDEATH, false);
		chartEnabled.put(PARTICLESPERCELLTYPE, false);
		chartEnabled.put(PARTICLEGRADIENTS, false);
		chartEnabled.put(DNAHISTOGRAMM, false);
		chartEnabled.put(DNAHISTOGRAMMAVG, false);
		
		//chartEnabled.put(AGEGRADIENT, false);
	}
	
	protected HashMap<String, Boolean> getNamesAndActivationStatusOfAvailableDefaultCharts(){
		this.chartEnabledOld = (HashMap<String, Boolean>)this.chartEnabled.clone();
		return (HashMap<String, Boolean>)this.chartEnabled.clone();
		
	}
	/**
	 * Fügt die Series Objekte in die Collections ein
	 */
	private void addXYSeriesToCollections(){
		Set keySetXYSeries = xySeries.keySet();
		Iterator<String[]> iter = keySetXYSeries.iterator();
		String [] actKey;
		while(iter.hasNext()){
			actKey = iter.next();
			if(actKey.length >= 2 && xySeriesCollections.containsKey(actKey[1])){ 
				if(xySeriesCollections.get(actKey[1]) != null){					
					xySeriesCollections.get(actKey[1]).addSeries(xySeries.get(actKey));
				}
			}
			
		}
		
	}
	
	protected void resetToOldSelectionValues(){
		if(chartEnabledOld != null) this.chartEnabled = chartEnabledOld;
	}
	
	protected void activateDefaultChart(String name){
		if(this.chartEnabled.containsKey(name)) this.chartEnabled.put(name, true);
	}
	
	protected void deactivateDefaultChart(String name){
		if(this.chartEnabled.containsKey(name)) this.chartEnabled.put(name, false);
	}
	
	private XYSeries getXYSeries(String name){
		Set keySet = xySeries.keySet();
		Iterator <String[]> iter = keySet.iterator();
		
		
		while(iter.hasNext()){
			String[] key = iter.next();
			if(key.length >=2 && key[0].equals(name)) return xySeries.get(key);
		}
		
		return null;
	}
	
	private XYSeriesCollection getXYSeriesCollection(String name){
		return xySeriesCollections.get(name);
	}
	
	
	
       
   private DefaultCategoryDataset getDefaultCategoryDataset(String name){
   	return categoryDatasets.get(name);
   }
	
	protected static synchronized DefaultCharts getInstance(){
		if(instance == null) instance = new DefaultCharts();
		
		return instance;
	}
	
	protected void clearSeries(){
	 Collection<XYSeries> col =	xySeries.values();
	 Iterator<XYSeries> iter = col.iterator();
	 while(iter.hasNext()) iter.next().clear();
	}
	
	protected static synchronized void  rebuildCharts(){
		instance = new DefaultCharts();
	}
	
	
	
	
	
	protected List<EnhancedSteppable> getSteppablesOfActivatedDefaultCharts(){
		
		return getActivatedElements(this.steppablesMap);
	}
	protected List<ChartPanel> getChartPanelsOfActivatedDefaultCharts(){
		
		return getActivatedElements(this.chartsMap);
	}
	
	private <T> List<T> getActivatedElements(Map<String, T> elementsMap){
		List<T> elements = new LinkedList<T>();
		for(String actChartName: this.chartEnabled.keySet()){
			if(this.chartEnabled.get(actChartName)){
				elements.add(elementsMap.get(actChartName));
			}
		}
		return elements;
		
	}
	
	private void addDefaultSteppables(){
		this.steppablesMap.put(this.PERFORMANCE, new EnhancedSteppable()
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
	   		   getXYSeries("Num_Cells_Steps").add(state.schedule.getSteps(), GlobalStatistics.getInstance().getActualNumberKCytes());
	   		   }
	   		   }	
	   		   
	             
	         }

			public double getInterval() {

	         
	         return 100;
         }
	     });
		//chartUpdaterKinetics
		this.steppablesMap.put(this.TISSUEKINETICPARAMETERS, new EnhancedSteppable()
				{
         public void step(SimState state)
         {            	
         	// add a new (X,Y) point on the graph, with X = the time step and Y = the number of live cells
         	//ChartSeries_KCyte_All.add((double)(state.schedule.time()), actualKCytes);    
             double meanCycleTime=0;
             double turnover=0;
             double gStatistics_GrowthFraction = 0;
             double gStatistics_TurnoverTime = 0;
             //double growthFraction=0; // instead globally defined
             if (GlobalStatistics.getInstance().getActualNumberKCytes()>0)
             {
                 meanCycleTime=(GlobalStatistics.getInstance().getActualNumberStemCells()*ModelController.getInstance().getCellBehavioralModelController().getEpisimCellBehavioralModelGlobalParameters().getCellCycleStem()
               		             +GlobalStatistics.getInstance().getActualNumberTACells()*ModelController.getInstance().getCellBehavioralModelController().getEpisimCellBehavioralModelGlobalParameters().getCellCycleTA())
               		             /(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTACells());
                 getXYSeries("ChartSeries_Kinetics_MeanCycleTime").add((double)(state.schedule.time()*TIMEFACTOR), meanCycleTime*TIMEFACTOR);
                 if (GlobalStatistics.getInstance().getActualBasalStatisticsCells()>0)
                     gStatistics_GrowthFraction=100*(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTACells())
                                                      /GlobalStatistics.getInstance().getActualBasalStatisticsCells();
                 if (gStatistics_GrowthFraction>100) gStatistics_GrowthFraction=100;
                 //ChartSeries_Kinetics_GrowthCells.add((double)(state.schedule.time()), growthFraction);                
                 getXYSeries("ChartSeries_Kinetics_GrowthFraction").add((double)(state.schedule.time()*TIMEFACTOR), gStatistics_GrowthFraction);                
                 if (meanCycleTime>0) 
                     gStatistics_TurnoverTime=(GlobalStatistics.getInstance().getActualNumberKCytes())*meanCycleTime/(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTACells()); // Number of cells producing X mean production per time
                 else
                     gStatistics_TurnoverTime=0;
                 getXYSeries("ChartSeries_Kinetics_Turnover").add((double)(state.schedule.time()*TIMEFACTOR), gStatistics_TurnoverTime*TIMEFACTOR);
             }
         }
         public double getInterval() {

	         return 100;
         }
     });
		
		  //////////////////////////////////////
	     // CHART Updating Num Cell Chart
	     //////////////////////////////////////
	     
	     
			this.steppablesMap.put(this.CELLCOUNTS, new EnhancedSteppable()
			{
	         public void step(SimState state)
	         {            	
	         	// add a new (X,Y) point on the graph, with X = the time step and Y = the number of live cells
	         	 getXYSeries("ChartSeries_KCyte_All").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getActualNumberKCytes());
	         	 getXYSeries("ChartSeries_KCyte_TA").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getActualNumberTACells());
	         	 getXYSeries("ChartSeries_KCyte_Spi").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getActualNumberEarlySpiCells());
	         	 getXYSeries("ChartSeries_KCyte_LateSpi").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getActualNumberLateSpi());
	         	 getXYSeries("ChartSeries_KCyte_Granu").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getActualGranuCells());
	         	 getXYSeries("ChartSeries_KCyte_MeanAgeDate").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getMeanAgeOfAllCells()*TIMEFACTOR);
	         }

				public double getInterval() {
	            return 100;
            }
			});
			
			
			
			this.steppablesMap.put(this.PARTICLEGRADIENTS , new EnhancedSteppable()
		    {
		             public void step(SimState state)
		             {
		            	  getXYSeries("ExtCalConcAvg").clear();
		            	  getXYSeries("LamellaConcAvg").clear(); 
		            	  getXYSeries("LipidsConcAvg").clear();
		            	  getXYSeries("AgeAvg").clear();
		            	  getXYSeries("Num").clear();
		                 
		            	  int MAX_YBINS=30; // for every 10 y coordinates one bin
		                 int[] HistoExtCalConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
		                 int[] HistoLamellaConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
		                 int[] HistoLipidsConc=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
		                 int[] HistoAgeAvg=new int[MAX_YBINS]; // Concentrations *10 = 0 to 200
		                 int[] HistoNum=new int[MAX_YBINS];  // Number of Cells in this bin
		                
		                 
		                 for (int k=0; k<MAX_YBINS; k++)
		                 {
		                     HistoExtCalConc[k]=0;
		                     HistoNum[k]=0;
		                 }
		                 
		                 for (int i=0; i< GlobalStatistics.getInstance().getCells().size(); i++)
		                 {
		                     // iterate through all cells
		                     UniversalCell act=(UniversalCell)GlobalStatistics.getInstance().getCells().get(i);
		                     if (act.isInNirvana()) continue;
		                     // is a living cell..
		                     int histobin=(int)(act.getEpisimCellBehavioralModelObject().getY()/7);
		                     HistoNum[histobin]++;
		                     HistoExtCalConc[histobin]+=act.getEpisimCellBehavioralModelObject().getCa();
		                     HistoLamellaConc[histobin]+=act.getEpisimCellBehavioralModelObject().getLam();
		                     HistoLipidsConc[histobin]+=act.getEpisimCellBehavioralModelObject().getLip();
		                     
		                     int diffLevel =  act.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();
		          			  
		          				  if(diffLevel == EpisimDifferentiationLevel.STEMCELL){
		          					 HistoAgeAvg[histobin]+=act.getEpisimCellBehavioralModelObject().getAge();
		          				  }
		          		  }
		                 
		                 ///////////////////////////////////////////
		                 // Cell Type Statistics
		                 ///////////////////////////////////////////
		                             
		                 // Make Chartdata from Histo
		                 double concExtCal=0;   // averaged concentrations
		                 double concLamella=0;
		                 double concLipids=0;
		                 double avgAge=0;
		                 for (int j=0; j<MAX_YBINS; j++)
		                 {                        
		                     if (HistoNum[j]>=10)
		                     {
		                         concExtCal=HistoExtCalConc[j]/ HistoNum[j];
		                         concLamella=HistoLamellaConc[j] /HistoNum[j];
		                         concLipids=HistoLipidsConc[j]/ HistoNum[j];
		                         avgAge=HistoAgeAvg[j]/ HistoNum[j];		                         
		                     }
		                     else
		                     {
		                         concExtCal=0;
		                         concLamella=0;
		                         concLipids=0;
		                         avgAge=0;
		                     }
		                     if (HistoNum[j]>=10)
		                     {
		                     	 getXYSeries("ExtCalConcAvg").add(j*7-CORNEUMY, concExtCal);
		                     	 getXYSeries("LamellaConcAvg").add(j*7-CORNEUMY, concLamella);
		                     	 getXYSeries("LipidsConcAvg").add(j*7-CORNEUMY, concLipids);
		                     	 getXYSeries("AgeAvg").add(j*7-CORNEUMY, avgAge*TIMEFACTOR);
		                     	 getXYSeries("Num").add(j*7-CORNEUMY, HistoNum[j]);
		                     }
		                 }
		            }
						public double getInterval() {
	                  return 100;
                  }
		    });
			
			this.steppablesMap.put(this.PARTICLESPERCELLTYPE , new EnhancedSteppable()
		    {
		             public void step(SimState state)
		             {
		            	  double ExtCal_TA=0;                    
		                 double ExtCal_Spi=0;
		                 double ExtCal_LateSpi=0;
		                 double ExtCal_Granu=0; 
		                 
		                 double Lam_TA=0;                    
		                 double Lam_Spi=0;
		                 double Lam_LateSpi=0;
		                 double Lam_Granu=0;                    
		                 
		                 double Lip_TA=0;                    
		                 double Lip_Spi=0;
		                 double Lip_LateSpi=0;
		                 double Lip_Granu=0;
		                 
		                            
		                 for (int i=0; i< GlobalStatistics.getInstance().getCells().size(); i++)
		                 {
		                     // iterate through all cells
		                     UniversalCell act=(UniversalCell)GlobalStatistics.getInstance().getCells().get(i);
		                     if (act.isInNirvana()) continue;
		                     // is a living cell..
		                    
		                     
		                     int diffLevel =  act.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();
		          			  switch(diffLevel){
		          				  case EpisimDifferentiationLevel.EARLYSPICELL:{
		          					  	ExtCal_Spi+=act.getEpisimCellBehavioralModelObject().getCa(); 
			                     	Lam_Spi+=act.getEpisimCellBehavioralModelObject().getLam(); 
			                     	Lip_Spi+=act.getEpisimCellBehavioralModelObject().getLip(); 
		          				  }
		          				  break;
		          				  case EpisimDifferentiationLevel.GRANUCELL:{
		          					  	ExtCal_Granu+=act.getEpisimCellBehavioralModelObject().getCa(); 
			                     	Lam_Granu+=act.getEpisimCellBehavioralModelObject().getLam(); 
			                     	Lip_Granu+=act.getEpisimCellBehavioralModelObject().getLip();
		          				  }
		          				  break;
		          				  case EpisimDifferentiationLevel.LATESPICELL:{
		          					  	ExtCal_LateSpi+=act.getEpisimCellBehavioralModelObject().getCa(); 
			                     	Lam_LateSpi+=act.getEpisimCellBehavioralModelObject().getLam(); 
			                     	Lip_LateSpi+=act.getEpisimCellBehavioralModelObject().getLip(); 
		          				  }
		          				  break;
		          				  case EpisimDifferentiationLevel.TACELL:{
		          					  ExtCal_TA+=act.getEpisimCellBehavioralModelObject().getCa(); 
			                       Lam_TA+=act.getEpisimCellBehavioralModelObject().getLam(); 
			                       Lip_TA+=act.getEpisimCellBehavioralModelObject().getLip();  
		          				  }
		          				  break;
		          			  }		                                     
		                 }
		                 
		                 ///////////////////////////////////////////
		                 // Cell Type Statistics
		                 ///////////////////////////////////////////
		                 
		                 if (GlobalStatistics.getInstance().getActualNumberTACells()>3) ExtCal_TA/=GlobalStatistics.getInstance().getActualNumberTACells();
		                 if (GlobalStatistics.getInstance().getActualNumberEarlySpiCells()>3) ExtCal_Spi/=GlobalStatistics.getInstance().getActualNumberEarlySpiCells();
		                 if (GlobalStatistics.getInstance().getActualNumberLateSpi()>3) ExtCal_LateSpi/=GlobalStatistics.getInstance().getActualNumberLateSpi();
		                 if (GlobalStatistics.getInstance().getActualGranuCells()>3) ExtCal_Granu/=GlobalStatistics.getInstance().getActualGranuCells();                    
		                                     
                     

		                 if (GlobalStatistics.getInstance().getActualNumberTACells()>3) Lam_TA/=GlobalStatistics.getInstance().getActualNumberTACells();
		                 if (GlobalStatistics.getInstance().getActualNumberEarlySpiCells()>3) Lam_Spi/=GlobalStatistics.getInstance().getActualNumberEarlySpiCells();
		                 if (GlobalStatistics.getInstance().getActualNumberLateSpi()>3) Lam_LateSpi/=GlobalStatistics.getInstance().getActualNumberLateSpi();
		                 if (GlobalStatistics.getInstance().getActualGranuCells()>3) Lam_Granu/=GlobalStatistics.getInstance().getActualGranuCells();
		                                     
		                 
		                 if (GlobalStatistics.getInstance().getActualNumberTACells()>3) Lip_TA/=GlobalStatistics.getInstance().getActualNumberTACells();
		                 if (GlobalStatistics.getInstance().getActualNumberEarlySpiCells()>3) Lip_Spi/=GlobalStatistics.getInstance().getActualNumberEarlySpiCells();
		                 if (GlobalStatistics.getInstance().getActualNumberLateSpi()>3) Lip_LateSpi/=GlobalStatistics.getInstance().getActualNumberLateSpi();
		                 if (GlobalStatistics.getInstance().getActualGranuCells()>3) Lip_Granu/=GlobalStatistics.getInstance().getActualGranuCells();
		                                     
		                 
		                 // row keys...
		                 String sExtCal = "Ext Cal (mg/kg)";
		                 String sLam= "Lamella";
		                 String sLip = "Lipids";

		                 // column keys...
		                 String cTA = "TA";
		                 String cSpi = "EarlySpinosum";
		                 String cLateSpi = "LateSpinosum";
		                 String cGranu = "Granulosum";
		                 		                 
		                 getDefaultCategoryDataset("particleCellTypeDataset").clear();
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_TA, sExtCal, cTA);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_Spi, sExtCal, cSpi);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_LateSpi, sExtCal, cLateSpi);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(ExtCal_Granu, sExtCal, cGranu);                    
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_TA, sLam, cTA);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_Spi, sLam, cSpi);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_LateSpi, sLam, cLateSpi);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lam_Granu, sLam, cGranu);                    
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_TA, sLip, cTA);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_Spi, sLip, cSpi);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_LateSpi, sLip, cLateSpi);
		                 getDefaultCategoryDataset("particleCellTypeDataset").addValue(Lip_Granu, sLip, cGranu);                              
		                               
		            }
						public double getInterval() {
	                  return 100;
                 }
		    });
			
				//////////////////////////////////////
		     // CHART Updating Apoptosis Chart
		     //////////////////////////////////////
		       
			this.steppablesMap.put(this.CELLDEATH , new EnhancedSteppable()
		   {
		         public void step(SimState state)
		         {
		         	
		            	 getXYSeries("ChartSeries_Apoptosis_Basal").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getApoptosis_Basal_Statistics());
		            	
		             
		         }
					public double getInterval() {
	               return 100;
               }
		     });
	     
			  
		    
			  //////////////////////////////////////
		     // CHART Updating Barrier Chart
		     //////////////////////////////////////
		    
			this.steppablesMap.put(this.PARTICLECONCENTRATIONSINBARRIER, new EnhancedSteppable()
		    {
		         public void step(SimState state)
		         {          
		         	
		         	if(GlobalStatistics.getInstance().getBarrier_ExtCalcium_Statistics() < 10){
		         		//System.out.println("CA Warnung");
		         	}
		         	
		         	getXYSeries("ChartSeries_Barrier_Calcium").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getBarrier_ExtCalcium_Statistics());
		         	getXYSeries("ChartSeries_Barrier_Lamella").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getBarrier_Lamella_Statistics());
		         	getXYSeries("ChartSeries_Barrier_Lipids").add((double)(state.schedule.time()*TIMEFACTOR), GlobalStatistics.getInstance().getBarrier_Lipids_Statistics());
		         }

					public double getInterval() {
						return 100;
               }
		     });
			
			
			  //////////////////////////////////////
		     // CHART DNA Content
		     //////////////////////////////////////
			
			this.steppablesMap.put(this.DNAHISTOGRAMM, new EnhancedSteppable()
		    {
		         public void step(SimState state)
		         {  
		         	
		         	getXYSeries("DNA_Content").clear();
		         	double [] dnaContents = GlobalStatistics.getInstance().getDNAContents();
		         	double intervalSize = GlobalStatistics.getInstance().getBucketIntervalSize();
		         	getXYSeries("DNA_Content").add(0, 0);
		         	getXYSeries("DNA_Content").add((GlobalStatistics.FIRSTBUCKETAMOUNT-intervalSize), 0);
		         	
		         	
		         	for(int i = 0; i < dnaContents.length; i++){
							double dnaContent = (GlobalStatistics.FIRSTBUCKETAMOUNT + i * intervalSize);
							
							getXYSeries("DNA_Content").add(dnaContent, dnaContents[i]);
							
						}
		         	
		         	getXYSeries("DNA_Content").add((GlobalStatistics.LASTBUCKETAMOUNT+1), 0);
		         	
		         }

					public double getInterval() {
						return 100;
              }
		     });
			
			  //////////////////////////////////////
		     // CHART DNA Content Averaged
		     //////////////////////////////////////
			
			this.steppablesMap.put(this.DNAHISTOGRAMMAVG, new EnhancedSteppable()
		    {
		         public void step(SimState state)
		         {  
		         	
		         	getXYSeries("DNA_Content_AVG").clear();
		         	double [] dnaContentsAVG = GlobalStatistics.getInstance().getDNAContentsAveraged();
		         	double intervalSize = GlobalStatistics.getInstance().getBucketIntervalSize();
		         	getXYSeries("DNA_Content_AVG").add(0, 0);
		         	getXYSeries("DNA_Content_AVG").add((GlobalStatistics.FIRSTBUCKETAMOUNT-intervalSize), 0);
		         	
		         	
		         	for(int i = 0; i < dnaContentsAVG.length; i++){
							double dnaContent = (GlobalStatistics.FIRSTBUCKETAMOUNT + i * intervalSize);
							
							getXYSeries("DNA_Content_AVG").add(dnaContent, dnaContentsAVG[i]);
						
						}
		         	
		         	getXYSeries("DNA_Content_AVG").add((GlobalStatistics.LASTBUCKETAMOUNT+1), 0);
		         	
		         }

					public double getInterval() {
						return 100;
            }
		     });
			
			
	}
	
}
