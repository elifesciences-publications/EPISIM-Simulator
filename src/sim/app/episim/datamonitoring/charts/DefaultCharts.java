package sim.app.episim.datamonitoring.charts;
//Charts
import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.ModelController;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.snapshot.SnapshotWriter;
import sim.app.episim.util.EnhancedSteppable;
import sim.engine.SimState;
import sim.engine.Steppable;

public class DefaultCharts implements SnapshotListener,java.io.Serializable{
	
	
	private double TIMEFACTOR=0.5;   // conversion from timeticks to h for all diagrams: 2 time ticks mean 1 hour
	
	
   //Schl�ssel setzt sich XYSeriesCollection-Name Position 0 und XYSeries-Name zusammen
	private HashMap<String[], XYSeries> xySeries = new HashMap<String[], XYSeries>();
	private HashMap<String, XYSeriesCollection> xySeriesCollections = new HashMap<String, XYSeriesCollection>();
	private HashMap<String, DefaultCategoryDataset> categoryDatasets = new HashMap<String, DefaultCategoryDataset>();
	private HashMap<String, ChartPanel> chartsMap = new HashMap<String, ChartPanel>();
	private HashMap<String, EnhancedSteppable> steppablesMap = new HashMap<String, EnhancedSteppable>();
	private HashMap<String, Boolean> chartEnabled = new HashMap<String, Boolean>();
	
	
	
	//Available Default Charts
	private final String PERFORMANCE = "Performance";
	private final String CELLCOUNTS = "Cell Counts";
	private final String TISSUEKINETICPARAMETERS = "Tissue Kinetic Parameters";
	private final String PARTICLECONCENTRATIONSINBARRIER = "Particle Concentrations in Barrier";
	private final String CELLDEATH = "Cell Death";
	private final String PARTICLESPERCELLTYPE = "Particles per Cell Type";
	private final String AGEGRADIENT = "Age Gradient";
	
	
	private static  DefaultCharts instance;
	
	private DefaultCharts() {
		initChartActivationMap();
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
		
		xySeries.put(new String[] { "Num_Cells_Steps", "Performance_Series_Num_Cells" }, new XYSeries("Number Of Cells"));
		
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
		
		xySeries.put(new String[] { "ChartSeries_Kinetics_GrowthFraction", "ChartSeries_Kinetics100Coll" }, new XYSeries( "Growth Fraction" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_MeanCycleTime", "ChartSeries_Kinetics100Coll" }, new XYSeries( "Mean Cell Cycle Time" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_Turnover", "ChartSeries_Kinetics2000Coll" }, new XYSeries( "Turnover Time" ));
		
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
	   
	   xySeries.put(new String[] { "ChartSeries_Barrier_Calcium", "ChartSeries_Barrier" }, new XYSeries( "Barrier Calcium (mg/kg)" ));
	   xySeries.put(new String[] { "ChartSeries_Barrier_Lamella", "ChartSeries_Barrier" }, new XYSeries( "Barrier Lamella" ));
	   xySeries.put(new String[] { "ChartSeries_Barrier_Lipids", "ChartSeries_Barrier" }, new XYSeries( "Barrier Lipids" ));
	   
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
		 
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_Basal", "ChartSeries_Apoptosis" }, new XYSeries( "Basal" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_EarlySpi", "ChartSeries_Apoptosis" }, new XYSeries( "EarlySpi" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_LateSpi", "ChartSeries_Apoptosis" }, new XYSeries( "LateSpi" ));
		 xySeries.put(new String[] { "ChartSeries_Apoptosis_Granu", "ChartSeries_Apoptosis" }, new XYSeries( "Granu" ));
		 
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
	  
      xySeries.put(new String[] { "ExtCalConcAvg", "CollPartDist" }, new XYSeries("Mean Ext. Calcium (mg/kg)"));
      xySeries.put(new String[] { "LamellaConcAvg", "CollPartDist" }, new XYSeries("Mean Lamella"));
      xySeries.put(new String[] { "LipidsConcAvg", "CollPartDist" }, new XYSeries("Mean Lipids"));
     
      xySeries.put(new String[] { "Num", "CollNum" }, new XYSeries("Num Cells")); 
        
      xySeriesCollections.put("CollPartDist", new XYSeriesCollection());
      
      xySeriesCollections.put("CollNum", new XYSeriesCollection());
      
      chart = ChartFactory.createXYLineChart("Particle Gradients", "Depth (�m)", "Concentration", 
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
      
      chartsMap.put("LineChartParticleDistributions", new ChartPanel(chart));
		
 	  ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      
      ///////////////////////////////////////////////////
      // Charts: LineChart AgeDistribution
      /////////////////////////////////////////////////////
      xySeries.put(new String[] { "AgeAvg", "CollAge" }, new XYSeries("Mean Age")); 
      
      xySeriesCollections.put("CollAge", new XYSeriesCollection());
      
      chart = ChartFactory.createXYLineChart(AGEGRADIENT, "Depth (�m)", "Age in h",
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
		//chartEnabled.put(CELLCOUNTS, false);
		chartEnabled.put(TISSUEKINETICPARAMETERS, false);
		/*chartEnabled.put(PARTICLECONCENTRATIONSINBARRIER, false);
		chartEnabled.put(CELLDEATH, false);
		chartEnabled.put(PARTICLESPERCELLTYPE, false);
		chartEnabled.put(AGEGRADIENT, false);*/
	}
	
	public Map<String, Boolean> getNamesAndActivationStatusOfAvailableDefaultCharts(){
		return (Map<String, Boolean>) this.chartEnabled.clone();
	}
	/**
	 * F�gt die Series Objekte in die Collections ein
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
			if(key.length ==2 && key[0].equals(name)) return xySeries.get(key);
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
	public List<SnapshotObject> collectSnapshotObjects() {

		List<SnapshotObject> list = new LinkedList<SnapshotObject>();
		
		
		
			
		
		list.add(new SnapshotObject(SnapshotObject.CHARTS, this));
		return list;
	}
	
	protected static void setInstance(DefaultCharts charts){
		instance = charts;
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
	   		   //getXYSeries("Num_Cells_Steps").add(state.schedule.getSteps(), actualKCytes);
	   		   }
	   		   }	
	   		   
	             
	         }

			public double getInterval() {

	         // TODO Auto-generated method stub
	         return 0;
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
                 meanCycleTime=(GlobalStatistics.getInstance().getActualNumberStemCells()*ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getCellCycleStem()
               		             +GlobalStatistics.getInstance().getActualNumberTASells()*ModelController.getInstance().getBioChemicalModelController().getEpisimCellDiffModelGlobalParameters().getCellCycleTA())
               		             /(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTASells());
                 getXYSeries("ChartSeries_Kinetics_MeanCycleTime").add((double)(state.schedule.time()*TIMEFACTOR), meanCycleTime*TIMEFACTOR);
                 if (GlobalStatistics.getInstance().getActualBasalStatisticsCells()>0)
                     gStatistics_GrowthFraction=100*(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTASells())
                                                      /GlobalStatistics.getInstance().getActualBasalStatisticsCells();
                 if (gStatistics_GrowthFraction>100) gStatistics_GrowthFraction=100;
                 //ChartSeries_Kinetics_GrowthCells.add((double)(state.schedule.time()), growthFraction);                
                 getXYSeries("ChartSeries_Kinetics_GrowthFraction").add((double)(state.schedule.time()*TIMEFACTOR), gStatistics_GrowthFraction);                
                 if (meanCycleTime>0) 
                     gStatistics_TurnoverTime=(GlobalStatistics.getInstance().getActualNumberKCytes())*meanCycleTime/(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTASells()); // Number of cells producing X mean production per time
                 else
                     gStatistics_TurnoverTime=0;
                 getXYSeries("ChartSeries_Kinetics_Turnover").add((double)(state.schedule.time()*TIMEFACTOR), gStatistics_TurnoverTime*TIMEFACTOR);
             }
         }
         public double getInterval() {

	         return 100;
         }
     });
	}
	
}
