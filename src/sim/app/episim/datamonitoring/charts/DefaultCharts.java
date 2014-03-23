package sim.app.episim.datamonitoring.charts;
//Charts
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.Ellipse2D;
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
import org.jfree.chart.annotations.XYShapeAnnotation;
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

import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.engine.SimState;
import sim.engine.Steppable;

public class DefaultCharts implements java.io.Serializable, ClassLoaderChangeListener{
	
	
		
   //Schlüssel setzt sich XYSeriesCollection-Name Position 0 und XYSeries-Name zusammen
	private Map<String[], XYSeries> xySeries; 
	private Map<String, XYSeriesCollection> xySeriesCollections = new HashMap<String, XYSeriesCollection>();
		private Map<String, ChartPanel> chartsMap = new HashMap<String, ChartPanel>();
	private Map<String, EnhancedSteppable> steppablesMap = new HashMap<String, EnhancedSteppable>();
	private HashMap<String, Boolean> chartEnabled = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> chartEnabledOld = new HashMap<String, Boolean>();
	
	private Color[] colors = new Color[]{Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.PINK, Color.ORANGE, Color.DARK_GRAY, Color.LIGHT_GRAY};
	
	//Available Default Charts
	private final String PERFORMANCE = "Performance";
	private final String VISUALIZATION = "Alternative Cell Visualization";
	private final String CELLCOUNTS = "Cell Counts";
//	private final String TISSUEKINETICPARAMETERS = "Tissue Kinetic Parameters";
	private final String CELLDEATH = "Cell Death";	
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
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
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
		// Charts: Alternative Visualization
		////////////////////////////////////////////
		xySeries.put(new String[] { "Cell_Visualization", "Cell_Visualization_Series", "0"}, new XYSeries("Cell Visualization"));	
		xySeriesCollections.put("Visualization_Series", new XYSeriesCollection());
		
		
	
		chart = ChartFactory.createXYLineChart(VISUALIZATION, "X", "Y", 
				xySeriesCollections.get("Visualization_Series"), PlotOrientation.VERTICAL, false, false, false); 		
		
		chart.setBackgroundPaint(Color.white);
		
		xyPlot = chart.getXYPlot();
		xyPlot.setDomainGridlinesVisible(false);
		xyPlot.setRangeGridlinesVisible(false);
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.getRangeAxis().setAutoRange(false);
		xyPlot.getDomainAxis().setAutoRange(false);
		xyPlot.getDomainAxis().setRange(0, TissueController.getInstance().getTissueBorder().getWidthInMikron());
		xyPlot.getRangeAxis().setRange(0, TissueController.getInstance().getTissueBorder().getHeightInMikron());
		
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.red);
		
			
		
		
		chartsMap.put(VISUALIZATION, new EpisimChartPanel(chart));
	
	
		
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
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.BLACK);
		xyPlot.setRangeGridlinePaint(Color.BLACK);
	   yAxis = xyPlot.getRangeAxis();
		//xyPlot.setRangeAxis(new LogarithmicAxis(yAxis.getLabel()));
	   
	   
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.black);
		
		chartsMap.put(DNAHISTOGRAMMAVG, new EpisimChartPanel(chart));
		
		
		
		/////////////////////////////////////
		// Charts: DNA content Histogramm
		/////////////////////////////////////
		xySeries.put(new String[]{"DNA_Content", "DNA_Content_Series", "0"}, new XYSeries("DNA content"));
		
		xySeriesCollections.put("DNA_Content_Series", new XYSeriesCollection());
		
		

		chart = ChartFactory.createXYLineChart(DNAHISTOGRAMM, "DNA content", "number of cells", 
				xySeriesCollections.get("DNA_Content_Series"), PlotOrientation.VERTICAL, true, true, false); 		
		
		chart.setBackgroundPaint(Color.white);
		
		xyPlot = chart.getXYPlot();
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.BLACK);
		xyPlot.setRangeGridlinePaint(Color.BLACK);
	   yAxis = xyPlot.getRangeAxis();
		
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		lineShapeRenderer.setSeriesPaint(0, Color.black);
		
		chartsMap.put(DNAHISTOGRAMM, new EpisimChartPanel(chart));
		
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
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.BLACK);
		xyPlot.setRangeGridlinePaint(Color.BLACK);
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
		
		
		
		chartsMap.put(PERFORMANCE, new EpisimChartPanel(chart));

		// ///////////////////////////////////
		// Charts: NumCells
		// ///////////////////////////////////
		xySeriesCollections.put("ChartSeries_CellCount", new XYSeriesCollection());
		chart = ChartFactory.createXYLineChart(CELLCOUNTS, "time in sim steps", "cell number", 
				xySeriesCollections.get("ChartSeries_CellCount"), PlotOrientation.VERTICAL, true, true, false); 		
		
		chart.setBackgroundPaint(Color.white);		
		xyPlot = chart.getXYPlot();
		xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.BLACK);
		xyPlot.setRangeGridlinePaint(Color.BLACK);
	   yAxis = xyPlot.getRangeAxis();
		
	   lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		
		
		xySeries.put(new String[] { "CellCount-Cells_All", "ChartSeries_CellCount", "0" }, new XYSeries("All Cells"));
		lineShapeRenderer.setSeriesPaint(0, colors[0]);
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		
		int seriesIndex = 1;
		for(EpisimCellType cellType: cellTypes){
			xySeries.put(new String[] { "CellCount-"+cellType.toString(), "ChartSeries_CellCount", ""+seriesIndex }, new XYSeries(cellType.toString()));
			if(seriesIndex < colors.length){
				lineShapeRenderer.setSeriesPaint(seriesIndex, colors[seriesIndex]);
			}
			else{
				org.jzy3d.colors.Color randCol =org.jzy3d.colors.Color.random();
				lineShapeRenderer.setSeriesPaint(seriesIndex, new Color(randCol.r, randCol.g, randCol.b));
			}
			seriesIndex++;
		}
		for(EpisimDifferentiationLevel diffLevel: diffLevels){
			xySeries.put(new String[] { "CellCount-"+diffLevel.toString(), "ChartSeries_CellCount", ""+seriesIndex }, new XYSeries(diffLevel.toString()));
			if(seriesIndex < colors.length){
				lineShapeRenderer.setSeriesPaint(seriesIndex, colors[seriesIndex]);
			}
			else{
				org.jzy3d.colors.Color randCol =org.jzy3d.colors.Color.random();
				lineShapeRenderer.setSeriesPaint(seriesIndex, new Color(randCol.r, randCol.g, randCol.b));
			}
			seriesIndex++;
		}		
				
		chartsMap.put(CELLCOUNTS, new EpisimChartPanel(chart));
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		 /////////////////////////////////////
	   // Charts: Kinetics Statistics
	   /////////////////////////////////////
		
	/*	xySeries.put(new String[] { "ChartSeries_Kinetics_GrowthFraction", "ChartSeries_Kinetics100Coll", "0" }, new XYSeries( "Growth Fraction" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_MeanCycleTime", "ChartSeries_Kinetics100Coll", "1" }, new XYSeries( "Mean Cell Cycle Time" ));
		xySeries.put(new String[] { "ChartSeries_Kinetics_Turnover", "ChartSeries_Kinetics2000Coll", "2" }, new XYSeries( "Turnover Time" ));
		
		xySeriesCollections.put("ChartSeries_Kinetics100Coll", new XYSeriesCollection());
		xySeriesCollections.put("ChartSeries_Kinetics2000Coll", new XYSeriesCollection());
	   
		chart = ChartFactory.createXYLineChart(TISSUEKINETICPARAMETERS,  "Time in h", "Fraction (%) / Time", 
		   		xySeriesCollections.get("ChartSeries_Kinetics100Coll"), PlotOrientation.VERTICAL, true, true, false);                                               
    
      chart.setBackgroundPaint(Color.white);
              
      xyPlot = chart.getXYPlot();
      xyPlot.setBackgroundPaint(Color.WHITE);
		xyPlot.setDomainGridlinePaint(Color.BLACK);
		xyPlot.setRangeGridlinePaint(Color.BLACK);
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
	   
	   chartsMap.put(TISSUEKINETICPARAMETERS, new EpisimChartPanel(chart));*/
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		    
		 /////////////////////////////////////
		 // Charts: Apopotosis
		 /////////////////////////////////////
		xySeriesCollections.put("ChartSeries_Apoptosis", new XYSeriesCollection());
		 chart = ChartFactory.createXYLineChart(CELLDEATH, "time in sim steps", "percentage", xySeriesCollections.get("ChartSeries_Apoptosis"), PlotOrientation.VERTICAL, true, true, false);   
		 chart.setBackgroundPaint(Color.white);
		 xyPlot = chart.getXYPlot();
		 xyPlot.setBackgroundPaint(Color.white);
		 xyPlot.setDomainGridlinePaint(Color.white);
		 xyPlot.setDomainGridlinesVisible(true);
		 xyPlot.setRangeGridlinePaint(Color.white);
		  
		
		 yAxis = xyPlot.getRangeAxis();
		 lineShapeRenderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
		
		
		seriesIndex =0;
		for(EpisimCellType cellType: cellTypes){
			xySeries.put(new String[] { "Apoptosis-"+cellType.toString(), "ChartSeries_Apoptosis", ""+seriesIndex }, new XYSeries(cellType.toString()));
			if(seriesIndex < colors.length){
				lineShapeRenderer.setSeriesPaint(seriesIndex, colors[seriesIndex]);
			}
			else{
				org.jzy3d.colors.Color randCol =org.jzy3d.colors.Color.random();
				lineShapeRenderer.setSeriesPaint(seriesIndex, new Color(randCol.r, randCol.g, randCol.b));
			}
			seriesIndex++;
		}
		for(EpisimDifferentiationLevel diffLevel: diffLevels){
			xySeries.put(new String[] { "Apoptosis-"+diffLevel.toString(), "ChartSeries_Apoptosis", ""+seriesIndex }, new XYSeries(diffLevel.toString()));
			if(seriesIndex < colors.length){
				lineShapeRenderer.setSeriesPaint(seriesIndex, colors[seriesIndex]);
			}
			else{
				org.jzy3d.colors.Color randCol =org.jzy3d.colors.Color.random();
				lineShapeRenderer.setSeriesPaint(seriesIndex, new Color(randCol.r, randCol.g, randCol.b));
			}
			seriesIndex++;
		}	
		
		chartsMap.put(CELLDEATH, new EpisimChartPanel(chart));
			
		 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////   
		addXYSeriesToCollections();
	}
	
	private void initChartActivationMap(){
		
		chartEnabled.put(PERFORMANCE, false);
		chartEnabled.put(VISUALIZATION, false);
		chartEnabled.put(CELLCOUNTS, false);
//		chartEnabled.put(TISSUEKINETICPARAMETERS, false);
		chartEnabled.put(CELLDEATH, false);
		chartEnabled.put(DNAHISTOGRAMM, false);
		chartEnabled.put(DNAHISTOGRAMMAVG, false);
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
		   		   getXYSeries("Num_Cells_Steps").add(state.schedule.getSteps(), GlobalStatistics.getInstance().getNumberOfAllLivingCells());
	   		   }
	   		}            
	      }
			public double getInterval(){	         
	         return 100;
         }
			
	   });
		
		this.steppablesMap.put(this.VISUALIZATION, new EnhancedSteppable()
	    {
	        
	   	  
	   	  public void step(SimState state)
	        {   
	   		  JFreeChart chart = chartsMap.get(VISUALIZATION).getChart();
	   		  XYPlot plot = chart.getXYPlot();
	   		  plot.clearAnnotations();
	   		  GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
	   		  for(AbstractCell actCell : allCells){
	   			  EpisimBiomechanicalModel bmModel = actCell.getEpisimBioMechanicalModelObject();
	   			  CellBoundaries cb = bmModel.getCellBoundariesInMikron(0);
	   			  double width = cb.getMaxXInMikron()-cb.getMinXInMikron();
	   			  double height = cb.getMaxYInMikron()-cb.getMinYInMikron();
	   				
	   				plot.addAnnotation(new XYShapeAnnotation(new Ellipse2D.Double(bmModel.getX()-(width/2), bmModel.getY()-(height/2), width, height), new BasicStroke(1), Color.black, actCell.getCellColoring()));
	   		  }
	        }
	   	  public double getInterval(){	         
	   		  return 1;
	   	  }			
	   });
		//chartUpdaterKinetics
	/*	this.steppablesMap.put(this.TISSUEKINETICPARAMETERS, new EnhancedSteppable()
				{
         @SuppressWarnings("deprecation")
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
                 meanCycleTime=(GlobalStatistics.getInstance().getActualNumberStemCells()*ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getCellCycleStem()
               		             +GlobalStatistics.getInstance().getActualNumberTACells()*ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getCellCycleTA())
               		             /(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTACells());
                 getXYSeries("ChartSeries_Kinetics_MeanCycleTime").add((double)(state.schedule.getTime()*TIMEFACTOR), meanCycleTime*TIMEFACTOR);
                 if (GlobalStatistics.getInstance().getActualNumberOfBasalStatisticsCells()>0)
                     gStatistics_GrowthFraction=100*(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTACells())
                                                      /GlobalStatistics.getInstance().getActualBasalStatisticsCells();
                 if (gStatistics_GrowthFraction>100) gStatistics_GrowthFraction=100;
                 //ChartSeries_Kinetics_GrowthCells.add((double)(state.schedule.time()), growthFraction);                
                 getXYSeries("ChartSeries_Kinetics_GrowthFraction").add((double)(state.schedule.getTime()*TIMEFACTOR), gStatistics_GrowthFraction);                
                 if (meanCycleTime>0) 
                     gStatistics_TurnoverTime=(GlobalStatistics.getInstance().getActualNumberKCytes())*meanCycleTime/(GlobalStatistics.getInstance().getActualNumberStemCells()+GlobalStatistics.getInstance().getActualNumberTACells()); // Number of cells producing X mean production per time
                 else
                     gStatistics_TurnoverTime=0;
                 getXYSeries("ChartSeries_Kinetics_Turnover").add((double)(state.schedule.getTime()*TIMEFACTOR), gStatistics_TurnoverTime*TIMEFACTOR);
             }
         }
         public double getInterval() {

	         return 100;
         }
     });*/
		
		  //////////////////////////////////////
	     // CHART Updating Num Cell Chart
	     //////////////////////////////////////
	     
	     
			this.steppablesMap.put(this.CELLCOUNTS, new EnhancedSteppable()
			{
	         public void step(SimState state)
	         {            	
	         	// add a new (X,Y) point on the graph, with X = the time step and Y = the number of live cells
	         	 
	         
	         	
	         	getXYSeries("CellCount-Cells_All").add((double)(state.schedule.getSteps()), GlobalStatistics.getInstance().getNumberOfAllLivingCells());
	         	
	         	HashMap<EpisimCellType, Integer> cellTypeCounts = GlobalStatistics.getInstance().getCellTypeLivingCounterMap();
	         	HashMap<EpisimDifferentiationLevel, Integer> diffLevelCounts = GlobalStatistics.getInstance().getDiffLevelLivingCounterMap();
	         	for(EpisimCellType cellType: cellTypeCounts.keySet()){
	         		getXYSeries("CellCount-"+cellType.toString()).add((double)(state.schedule.getSteps()), cellTypeCounts.get(cellType));
	         	}
	         	for(EpisimDifferentiationLevel diffLevel: diffLevelCounts.keySet()){
	         		getXYSeries("CellCount-"+diffLevel.toString()).add((double)(state.schedule.getSteps()), diffLevelCounts.get(diffLevel));
	         	}
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
		         	
		            	 
		         	
		         	HashMap<EpisimCellType, Double> cellTypeApoptosisStatistics = GlobalStatistics.getInstance().getCellTypeApoptosisStatisticsMap();
		         	HashMap<EpisimDifferentiationLevel, Double> diffLevelApoptosisStatistics = GlobalStatistics.getInstance().getDiffLevelApoptosisStatisticsMap();
		         	for(EpisimCellType cellType: cellTypeApoptosisStatistics.keySet()){
		         		getXYSeries("Apoptosis-"+cellType.toString()).add((double)(state.schedule.getSteps()), cellTypeApoptosisStatistics.get(cellType));
		         	}
		         	for(EpisimDifferentiationLevel diffLevel: diffLevelApoptosisStatistics.keySet()){
		         		getXYSeries("Apoptosis-"+diffLevel.toString()).add((double)(state.schedule.getSteps()), diffLevelApoptosisStatistics.get(diffLevel));
		         	}		             
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
	
   public void classLoaderHasChanged() {
		instance = null;
   }
	
}
