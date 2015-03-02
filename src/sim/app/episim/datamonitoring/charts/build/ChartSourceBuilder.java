package sim.app.episim.datamonitoring.charts.build;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.StandardTickUnitSource;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.xy.XYSeries;

import calculationalgorithms.HistogramCalculationAlgorithm;
import episiminterfaces.*;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;



import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.build.AbstractCommonSourceBuilder;
import sim.app.episim.datamonitoring.build.SteppableCodeFactory;
import sim.app.episim.datamonitoring.build.SteppableCodeFactory.SteppableType;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.util.Names;
import sim.app.episim.util.ProjectionPlane;
import sim.engine.Steppable;


public class ChartSourceBuilder extends AbstractCommonSourceBuilder{
	
	
	public static final String CHARTDATAFIELDNAME = "chart";
	public static final String PNGDIRECTORYDATAFIELDNAME = "pngPrintingDirectory";
	
	public static final String PARAMETERSNAME = "calculationAlgorithmParameterValues";
	
	
	
	
	protected enum ChartSourceBuilderMode {XYSERIESMODE, HISTOGRAMMODE};
	
	private ChartSourceBuilderMode mode = ChartSourceBuilderMode.XYSERIESMODE;
	
	public ChartSourceBuilder(){
		
	}
	
	
	
	
	public String buildEpisimChartSource(EpisimChart episimChart){
		if(episimChart ==  null) throw new IllegalArgumentException("Episim-Chart was null!");
	
		generatedSourceCode = new StringBuffer();
		
		if(episimChart.getEpisimChartSeries().size() >= 1){
			CalculationAlgorithmType type = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(episimChart.getEpisimChartSeries().get(0).getCalculationAlgorithmConfigurator().getCalculationAlgorithmID()).getType();
			if(type == CalculationAlgorithmType.HISTOGRAMRESULT) this.mode = ChartSourceBuilderMode.HISTOGRAMMODE;
			else this.mode = ChartSourceBuilderMode.XYSERIESMODE;
		}		
		appendHeader(episimChart.getId(), episimChart.getTitle(), episimChart.getAllRequiredClasses());
		appendDataFields(episimChart);
		appendConstructor(episimChart);
		appendStandardMethods();
		appendRegisterObjectsMethod(episimChart.getAllRequiredClasses());
		appendClearSeriesMethod(episimChart);
		if(mode == ChartSourceBuilderMode.HISTOGRAMMODE) appendBuildBinsMethod();
		appendEnd();
		return generatedSourceCode.toString();
	}
	
	public String buildEpisimCellVisualizationChartSource(EpisimCellVisualizationChart episimChart){
		if(episimChart ==  null) throw new IllegalArgumentException("Episim-Chart was null!");	
		generatedSourceCode = new StringBuffer();
		this.mode = ChartSourceBuilderMode.XYSERIESMODE;
		appendHeader(episimChart.getId(), episimChart.getTitle(), episimChart.getRequiredClasses());
		appendDataFields(episimChart);		
		appendConstructor(episimChart);
		appendStandardMethods();
		appendRegisterObjectsMethod(episimChart.getRequiredClasses());
		appendCellColoringMethod(episimChart);
		appendClearSeriesMethod(episimChart);
		appendEnd();
		return generatedSourceCode.toString();
	}
	
	
	
	private void appendHeader(long chartId, String chartTitle, Set<Class<?>> requiredClasses){
		
		
		generatedSourceCode.append("package "+ Names.GENERATED_CHARTS_PACKAGENAME +";\n");		
		
		generatedSourceCode.append("import org.jfree.chart.*;\n");
		generatedSourceCode.append("import org.jfree.chart.annotations.*;\n");
		generatedSourceCode.append("import org.jfree.chart.block.*;\n");
		generatedSourceCode.append("import org.jfree.chart.event.*;\n");
		generatedSourceCode.append("import org.jfree.chart.plot.*;\n");
		generatedSourceCode.append("import org.jfree.chart.renderer.xy.*;\n");
		generatedSourceCode.append("import org.jfree.chart.title.*;\n");
		generatedSourceCode.append("import org.jfree.data.general.*;\n");
		generatedSourceCode.append("import org.jfree.data.xy.*;\n");
		generatedSourceCode.append("import org.jfree.chart.axis.*;\n");
		generatedSourceCode.append("import org.jfree.ui.*;\n");
		generatedSourceCode.append("import org.jfree.data.statistics.*;\n");
		generatedSourceCode.append("import episiminterfaces.calc.*;\n");
		generatedSourceCode.append("import episiminterfaces.monitoring.*;\n");
		generatedSourceCode.append("import episiminterfaces.*;\n");
		generatedSourceCode.append("import episimexceptions.*;\n");
		generatedSourceCode.append("import episimfactories.*;\n");
		
		generatedSourceCode.append("import java.awt.*;\n");
		generatedSourceCode.append("import javax.swing.*;\n");
		generatedSourceCode.append("import java.awt.geom.*;\n");
		generatedSourceCode.append("import java.io.*;\n");
		generatedSourceCode.append("import java.util.*;\n");
		generatedSourceCode.append("import java.lang.*;\n");
		generatedSourceCode.append("import sim.*;\n");
		generatedSourceCode.append("import sim.app.episim.*;\n");
		generatedSourceCode.append("import sim.app.episim.model.biomechanics.*;\n");
		generatedSourceCode.append("import sim.app.episim.util.*;\n");
		generatedSourceCode.append("import sim.engine.Steppable;\n");
		generatedSourceCode.append("import sim.app.episim.util.GenericBag;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.GlobalStatistics;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.calc.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.charts.io.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.charts.*;\n");
		generatedSourceCode.append("import sim.app.episim.AbstractCell;\n");
		generatedSourceCode.append("import sim.app.episim.EpisimProperties;\n");
		generatedSourceCode.append("import sim.engine.SimState;\n");	
		for(Class<?> actClass: requiredClasses){
			generatedSourceCode.append("import " + actClass.getCanonicalName()+";\n");	
		}		
		generatedSourceCode.append("public class " +Names.convertVariableToClass(Names.cleanString(chartTitle)+chartId)+" implements GeneratedChart{\n");
	
	}
	
	protected void appendDataFields(EpisimChart episimChart){
		  super.appendDataFields();	
		  generatedSourceCode.append("  private EnhancedSteppable pngSteppable = null;\n");
		  generatedSourceCode.append("  private JFreeChart "+CHARTDATAFIELDNAME+";\n");
		  generatedSourceCode.append("  private GenericBag<AbstractCell> allCells;\n");
		  generatedSourceCode.append("  private EpisimChartPanel chartPanel;\n");
		  if(mode == ChartSourceBuilderMode.XYSERIESMODE){ 
			  	generatedSourceCode.append("  private XYSeriesCollection dataset = new XYSeriesCollection();\n");		  				   
			   for(EpisimChartSeries actSeries: episimChart.getEpisimChartSeries()){			   	
			   	generatedSourceCode.append("  private XYSeries "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+
			   			" = new XYSeries(\""+actSeries.getName()+"\", false);\n");
			   }
		  }
		  if(mode == ChartSourceBuilderMode.HISTOGRAMMODE){	  				   
			   for(EpisimChartSeries actSeries: episimChart.getEpisimChartSeries()){			   	
			   	generatedSourceCode.append("  private OutlierSimpleHistogramDataset "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+
			   			" = new OutlierSimpleHistogramDataset(\""+actSeries.getName()+"\");\n");
			   }
		  }
		  for(Class<?> actClass : episimChart.getAllRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");		   
	}
	
	protected void appendDataFields(EpisimCellVisualizationChart episimChart){
		  super.appendDataFields();	
		  generatedSourceCode.append("  private EnhancedSteppable pngSteppable = null;\n");
		  generatedSourceCode.append("  private JFreeChart "+CHARTDATAFIELDNAME+";\n");
		  generatedSourceCode.append("  private GenericBag<AbstractCell> allCells;\n");
		  generatedSourceCode.append("  private EpisimChartPanel chartPanel;\n");
		  if(mode == ChartSourceBuilderMode.XYSERIESMODE){ 
			  	generatedSourceCode.append("  private XYSeriesCollection dataset = new XYSeriesCollection();\n");		  				   
		  }
		  for(Class<?> actClass : episimChart.getRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");		   
	}
	
	
	private void appendConstructor(EpisimChart episimChart){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(episimChart.getTitle())+ episimChart.getId())+"(){\n");
		
		if(mode == ChartSourceBuilderMode.XYSERIESMODE){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+" = ChartFactory.createXYLineChart(\""+episimChart.getTitle()+"\",\""+episimChart.getXLabel()+"\",\""+
					episimChart.getYLabel()+"\",dataset,"+"PlotOrientation.VERTICAL, "+episimChart.isLegendVisible()+", true, false);\n");
			generatedSourceCode.append("  ((XYLineAndShapeRenderer)(((XYPlot)("+CHARTDATAFIELDNAME+".getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);\n");
		}
		if(mode == ChartSourceBuilderMode.HISTOGRAMMODE){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+" = ChartFactory.createHistogram(\""+episimChart.getTitle()+"\",\""+episimChart.getXLabel()+"\",\""+
					episimChart.getYLabel()+"\", null,"+"PlotOrientation.VERTICAL, "+episimChart.isLegendVisible()+", true, false);\n");
		}	
		
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setBackgroundPaint(Color.white);\n");
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setAntiAlias("+episimChart.isAntialiasingEnabled()+");\n");
		generatedSourceCode.append("   XYPlot xyPlot = (XYPlot) "+CHARTDATAFIELDNAME+".getXYPlot();\n");
		generatedSourceCode.append("   xyPlot.setBackgroundPaint(Color.WHITE);\n");
		generatedSourceCode.append("   xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);\n");
		generatedSourceCode.append("   xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);\n");
		
		if(episimChart.isXAxisLogarithmic()){			
			generatedSourceCode.append("  LogarithmicAxis xLogAxis =new LogarithmicAxis(\""+ episimChart.getXLabel()+"\");\n");
			generatedSourceCode.append("  xLogAxis.setExpTickLabelsFlag(true);\n");			
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".getXYPlot().setDomainAxis(xLogAxis);\n");			
		}
		if(episimChart.isYAxisLogarithmic()){
			generatedSourceCode.append("  LogarithmicAxis yLogAxis =new LogarithmicAxis(\""+ episimChart.getYLabel()+"\");\n");
			generatedSourceCode.append("  yLogAxis.setExpTickLabelsFlag(true);\n");			
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".getXYPlot().setRangeAxis(yLogAxis);\n");		
		}
		
		generatedSourceCode.append("  chartPanel = new EpisimChartPanel("+CHARTDATAFIELDNAME+", true);\n");
		generatedSourceCode.append("  chartPanel.setPreferredSize(new java.awt.Dimension(640,480));\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawHeight(10);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawHeight(2000);\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawWidth(20);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawWidth(2000);\n\n");
		
		if(episimChart.isLegendVisible()) appendLegend();
		
		if(mode == ChartSourceBuilderMode.XYSERIESMODE) appendChartSeriesInit(episimChart);
		if(mode == ChartSourceBuilderMode.HISTOGRAMMODE) appendHistogramDatasetInit(episimChart);		
		
		long baselineCalculationHandlerID = AbstractCommonSourceBuilder.getNextCalculationHandlerId();
		Map <Long, Long> seriesCalculationHandlerIDs = new HashMap<Long, Long>();
		if(episimChart.getBaselineCalculationAlgorithmConfigurator() == null)  baselineCalculationHandlerID = Long.MIN_VALUE;
		for(EpisimChartSeries series: episimChart.getEpisimChartSeries()){		
			seriesCalculationHandlerIDs.put(series.getId(), AbstractCommonSourceBuilder.getNextCalculationHandlerId());			
		}
		
		appendHandlerRegistration(baselineCalculationHandlerID, seriesCalculationHandlerIDs,episimChart);
		appendSteppable(baselineCalculationHandlerID, seriesCalculationHandlerIDs,episimChart);
		appendPNGSteppable(episimChart);
		generatedSourceCode.append("}\n");
	}
	
	private void appendConstructor(EpisimCellVisualizationChart episimChart){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(episimChart.getTitle())+ episimChart.getId())+"(){\n");
		
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+" = ChartFactory.createXYLineChart(\""+episimChart.getTitle()+"\",\""+episimChart.getXLabel()+"\",\""+
					episimChart.getYLabel()+"\",dataset,"+"PlotOrientation.VERTICAL, false, false, false);\n");
			generatedSourceCode.append("  ((XYLineAndShapeRenderer)(((XYPlot)("+CHARTDATAFIELDNAME+".getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);\n");
		
		
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setBackgroundPaint(Color.white);\n");
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setAntiAlias(true);\n");	
		generatedSourceCode.append("   XYPlot xyPlot = (XYPlot) "+CHARTDATAFIELDNAME+".getXYPlot();\n");
		generatedSourceCode.append("   xyPlot.setBackgroundPaint(Color.WHITE);\n");
		generatedSourceCode.append("   xyPlot.setDomainGridlinesVisible(false);\n");
		generatedSourceCode.append("   xyPlot.setRangeGridlinesVisible(false);\n");
		generatedSourceCode.append("   xyPlot.getRangeAxis().setAutoRange(false);\n");
		generatedSourceCode.append("   xyPlot.getDomainAxis().setAutoRange(false);\n");
		double width = 1;
		double height = 1;
		if(episimChart.getCellProjectionPlane() == ProjectionPlane.XY_PLANE){
			width=TissueController.getInstance().getTissueBorder().getWidthInMikron();
			height=TissueController.getInstance().getTissueBorder().getHeightInMikron();
		}
		else if(episimChart.getCellProjectionPlane() == ProjectionPlane.XZ_PLANE){
			width=TissueController.getInstance().getTissueBorder().getWidthInMikron();
			height=TissueController.getInstance().getTissueBorder().getLengthInMikron();
		}
		else if(episimChart.getCellProjectionPlane() == ProjectionPlane.YZ_PLANE){
			width = TissueController.getInstance().getTissueBorder().getLengthInMikron();
			height = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		}
		generatedSourceCode.append("   xyPlot.getDomainAxis().setRange(0, "+width+");\n");
		generatedSourceCode.append("   xyPlot.getRangeAxis().setRange(0, "+height+");\n");
		generatedSourceCode.append("  chartPanel = new EpisimChartPanel("+CHARTDATAFIELDNAME+", true, true);\n");
		generatedSourceCode.append("  chartPanel.setWidthToHeightScale("+(width/height)+");\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawHeight(10);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawHeight(2000);\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawWidth(20);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawWidth(2000);\n\n");
	
		appendSteppable(episimChart);
		appendPNGSteppable(episimChart, (width/height));
		generatedSourceCode.append("}\n");
	}	
	
	private void appendChartSeriesInit(EpisimChart episimChart){
		generatedSourceCode.append("  float[] newDash = null;\n");
		generatedSourceCode.append("  XYItemRenderer renderer = null;\n");
		int i = 0;
		for(EpisimChartSeries actSeries: episimChart.getEpisimChartSeries()){
			generatedSourceCode.append("  dataset.addSeries("+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+");\n");			
			generatedSourceCode.append("  newDash = new float[]{");
			for(int x=0;x<actSeries.getDash().length;x++){
				generatedSourceCode.append(""+((float)(actSeries.getDash()[x] * actSeries.getStretch()* actSeries.getThickness())));
				generatedSourceCode.append("f");
			   if(x != (actSeries.getDash().length-1)) generatedSourceCode.append(", ");
			}
			generatedSourceCode.append("};\n");
			generatedSourceCode.append("  renderer = (XYItemRenderer)(chartPanel.getChart().getXYPlot().getRenderer());\n");
			generatedSourceCode.append("  renderer.setSeriesStroke("+i+", new BasicStroke("+actSeries.getThickness()+"f, BasicStroke.CAP_ROUND, "+ 
		                                                "BasicStroke.JOIN_ROUND,0f,newDash,0f));\n");
			generatedSourceCode.append("  renderer.setSeriesPaint("+i+", new Color("+actSeries.getColor().getRed()+", "+
					actSeries.getColor().getGreen()+", "+actSeries.getColor().getBlue()+"));\n");
			i++;
		}
	}
	
	private void appendHistogramDatasetInit(EpisimChart episimChart){		
		generatedSourceCode.append("  XYPlot plot = "+CHARTDATAFIELDNAME+".getXYPlot();\n");
		generatedSourceCode.append("  XYBarRenderer renderer = null;\n");
		int i = 0;
		for(EpisimChartSeries actSeries: episimChart.getEpisimChartSeries()){
			
			generatedSourceCode.append("  plot.setDataset("+ i + ", "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+ ");\n");
			generatedSourceCode.append("  renderer = new XYBarRenderer();\n");
			generatedSourceCode.append("  renderer.setShadowVisible(false);\n");
			generatedSourceCode.append("  renderer.setBarPainter(new StandardXYBarPainter());\n");
			generatedSourceCode.append("  renderer.setGradientPaintTransformer(null);\n");
			generatedSourceCode.append("  renderer.setSeriesPaint(0, new Color("+actSeries.getColor().getRed()+", "+
					actSeries.getColor().getGreen()+", "+actSeries.getColor().getBlue()+"));\n");
		
			generatedSourceCode.append("  for(SimpleHistogramBin bin: buildBins("+actSeries.getCalculationAlgorithmConfigurator().getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER)
					+", "+actSeries.getCalculationAlgorithmConfigurator().getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER)
					+","+actSeries.getCalculationAlgorithmConfigurator().getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMNUMBEROFBINSPARAMETER)+")) "
					+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".addBin(bin);\n");
			
			generatedSourceCode.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".setAdjustForBinSize(false);");
			generatedSourceCode.append("  plot.setRenderer("+i+", renderer);\n");	
			
			i++;
		}
		if(episimChart.getEpisimChartSeries().size() > 1) 	generatedSourceCode.append("  plot.setForegroundAlpha(0.6F);\n");
	}
	
	
	
	
	private void appendSteppable(long baselineCalculationHandlerID, Map<Long, Long> seriesCalculationHandlerIDs, EpisimChart episimChart){
		
		generatedSourceCode.append("steppable = "+SteppableCodeFactory.getEnhancedSteppableSourceCode(Names.CALCULATION_CALLBACK_LIST, episimChart.getChartUpdatingFrequency(), SteppableType.CHART)+";\n");
	}
	
	private void appendSteppable(EpisimCellVisualizationChart episimChart){
		
		generatedSourceCode.append("steppable = "+SteppableCodeFactory.getEnhancedSteppableSourceCode(episimChart)+";\n");
	}
	
	private void appendPNGSteppable(EpisimChart episimChart){
		
		generatedSourceCode.append("pngSteppable = "+ SteppableCodeFactory.getEnhancedSteppableForPNGPrinting(episimChart)+";\n");
	}
	private void appendPNGSteppable(EpisimCellVisualizationChart episimChart, double widthToHeightScale){
		
		generatedSourceCode.append("pngSteppable = "+ SteppableCodeFactory.getEnhancedSteppableForPNGPrinting(episimChart, widthToHeightScale)+";\n");
	}
	
	
	private void appendLegend(){
		generatedSourceCode.append("  LegendTitle title = new LegendTitle((XYItemRenderer) (chart.getXYPlot().getRenderer()));\n");
		generatedSourceCode.append("  title.setLegendItemGraphicPadding(new org.jfree.ui.RectangleInsets(0, 8, 0, 4));\n");
		generatedSourceCode.append("  title.setLegendItemGraphicAnchor(RectangleAnchor.BOTTOM);\n");
		generatedSourceCode.append("  title.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));\n");
		generatedSourceCode.append("  title.setFrame(new LineBorder());\n");
		generatedSourceCode.append("  title.setBackgroundPaint(Color.white);\n");
		generatedSourceCode.append("  title.setPosition(RectangleEdge.BOTTOM);\n");
	}
	
	private void appendClearSeriesMethod(EpisimChart episimChart){	
		generatedSourceCode.append("  public void clearAllSeries(){\n");
		
		for(EpisimChartSeries actSeries: episimChart.getEpisimChartSeries()){
			if(mode == ChartSourceBuilderMode.XYSERIESMODE) generatedSourceCode.append("    "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".clear();\n");
			if(mode == ChartSourceBuilderMode.HISTOGRAMMODE) generatedSourceCode.append("    "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".clearObservations();\n");
	   }
		generatedSourceCode.append("  }\n");
	}
	
	private void appendClearSeriesMethod(EpisimCellVisualizationChart episimChart){	
		generatedSourceCode.append("  public void clearAllSeries(){\n");		
		
		generatedSourceCode.append("  }\n");
	}
	
		
	protected void appendStandardMethods(){
		super.appendStandardMethods();
		generatedSourceCode.append("public ChartPanel getChartPanel(){ return this.chartPanel;}\n");
		
		generatedSourceCode.append("public EnhancedSteppable getPNGSteppable(){ return this.pngSteppable;}\n");
		
	}
	
	private void appendHandlerRegistration(long baselineCalculationHandlerID, Map<Long, Long> seriesCalculationHandlerIDs, EpisimChart episimChart){
		CalculationAlgorithmConfigurator config = episimChart.getBaselineCalculationAlgorithmConfigurator();
		if(config != null){
			generatedSourceCode.append(Names.CALCULATION_CALLBACK_LIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			if(mode == ChartSourceBuilderMode.XYSERIESMODE){
				generatedSourceCode.append(buildCalculationHandler(baselineCalculationHandlerID, baselineCalculationHandlerID, true, config, episimChart.getRequiredClassesForBaseline())
						+", ((XYSeries)null), "+ episimChart.isXAxisLogarithmic() + ", " + episimChart.isYAxisLogarithmic()+"));\n");
			}
			else if(mode == ChartSourceBuilderMode.HISTOGRAMMODE){
				generatedSourceCode.append(buildCalculationHandler(baselineCalculationHandlerID, baselineCalculationHandlerID, true, config, episimChart.getRequiredClassesForBaseline())
						+", ((OutlierSimpleHistogramDataset) null), "+ episimChart.isXAxisLogarithmic() + ", " + episimChart.isYAxisLogarithmic()+"));\n");
			}
		}
		for(EpisimChartSeries actSeries: episimChart.getEpisimChartSeries()){
			
			generatedSourceCode.append(Names.CALCULATION_CALLBACK_LIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			generatedSourceCode.append(buildCalculationHandler(seriesCalculationHandlerIDs.get(actSeries.getId()), 
					                                             baselineCalculationHandlerID, false, actSeries.getCalculationAlgorithmConfigurator(), 
					                                             actSeries.getRequiredClasses()));
			generatedSourceCode.append(", "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId()));
			generatedSourceCode.append(", "+ episimChart.isXAxisLogarithmic() + ", " + episimChart.isYAxisLogarithmic()+"));\n");
				
		}
	}
	
	private void appendBuildBinsMethod(){
		generatedSourceCode.append("private SimpleHistogramBin[] buildBins(double minValue, double maxValue, int numberOfBins){\n");
	   generatedSourceCode.append("  if(minValue > maxValue){\n");
	   generatedSourceCode.append("    double tmp = minValue;\n");
	   generatedSourceCode.append("    minValue = maxValue;\n");
	   generatedSourceCode.append("    maxValue = tmp;\n");
	   generatedSourceCode.append("  }\n");	
	   generatedSourceCode.append("  if(minValue == maxValue)maxValue = (minValue + 1);\n");
	   generatedSourceCode.append("  if(numberOfBins < 0)numberOfBins = Math.abs(numberOfBins);\n");
	   generatedSourceCode.append("  if(numberOfBins == 0)numberOfBins = 1;\n");
	   generatedSourceCode.append("  double binSize = (Math.abs(maxValue - minValue)) / ((double)numberOfBins);\n");
	   generatedSourceCode.append("  SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins+2];\n");
	   generatedSourceCode.append("  bins[0] = new SimpleHistogramBin(Double.NEGATIVE_INFINITY, minValue, true, false);\n");
	   generatedSourceCode.append("  for(int i = 0; i < numberOfBins; i ++){\n");
	   generatedSourceCode.append("    if(i< (numberOfBins-1))bins[i+1] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, false);\n");
	   generatedSourceCode.append("    else bins[i+1] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, true);\n");
	   generatedSourceCode.append("  }\n");
	   generatedSourceCode.append("  bins[numberOfBins+1] = new SimpleHistogramBin(maxValue, Double.POSITIVE_INFINITY, false, true);\n");
	   generatedSourceCode.append("  return bins;\n");
	   generatedSourceCode.append("}\n");     
	}
	
	
	private void appendCellColoringMethod(EpisimCellVisualizationChart episimChart){
		
      StringBuffer handlerSource = generatedSourceCode;
      handlerSource.append("protected Color getCellColoring(AbstractCell cellTypeLocal) throws CellNotValidException{\n");
      if(episimChart.getDefaultColoring()){
      	handlerSource.append("  return cellTypeLocal.getCellColoring();\n");
      	handlerSource.append("}\n");
      }
      else if (episimChart.getCellColoringConfigurator()!= null){      	
   	   handlerSource.append("    EpisimCellBehavioralModel cellBehaviour = cellTypeLocal.getEpisimCellBehavioralModelObject();\n");
   	   handlerSource.append("    EpisimBiomechanicalModel biomechanics = cellTypeLocal.getEpisimBioMechanicalModelObject();\n");
   	   handlerSource.append("    Object cellTypeLocalObj = cellTypeLocal;\n");
   	   appendLocalVars(episimChart.getRequiredClasses(), handlerSource);
   	   appendAssignmentCheck("cellBehaviour", episimChart.getRequiredClasses(), handlerSource);
   	   appendAssignmentCheck("biomechanics", episimChart.getRequiredClasses(), handlerSource);
   	   appendAssignmentCheck("cellTypeLocalObj", episimChart.getRequiredClasses(), handlerSource);
   	   handlerSource.append("    if(isValidCell(cellTypeLocal)){\n");
   	   handlerSource.append("       int colorR = (int)("+ episimChart.getCellColoringConfigurator().getArithmeticExpressionColorR()[1]+");\n");
   	   handlerSource.append("       int colorG = (int)("+ episimChart.getCellColoringConfigurator().getArithmeticExpressionColorG()[1]+");\n");
   	   handlerSource.append("       int colorB = (int)("+ episimChart.getCellColoringConfigurator().getArithmeticExpressionColorB()[1]+");\n");
   	   handlerSource.append("       colorR = Math.min(Math.max(0, colorR), 255);\n");
   	   handlerSource.append("       colorG = Math.min(Math.max(0, colorG), 255);\n");
   	   handlerSource.append("       colorB = Math.min(Math.max(0, colorB), 255);\n");
   	   handlerSource.append("       return new Color(colorR, colorG, colorB);\n");
         handlerSource.append("    }\n");
   	   handlerSource.append("    else throw new CellNotValidException(\"Cell is not Valid: \"+ cellTypeLocal.getCellName());\n");        
         handlerSource.append("}\n");
      	appendCellValidCheck(handlerSource, episimChart.getRequiredClasses());
      }  
      else{
      	handlerSource.append("  return Color.WHITE;\n");
      	handlerSource.append("}\n");
      }   
     
	}	
		
	private void appendCellValidCheck(StringBuffer source, Set<Class<?>> requiredClasses){
		boolean classFound = false;
		source.append("  private boolean isValidCell(AbstractCell cellType){\n");
		for(Class<?> actClass: requiredClasses){
			if(AbstractCell.class.isAssignableFrom(actClass)){
				source.append("    if("+actClass.getSimpleName()+ ".class.isAssignableFrom(cellType.getClass())) return true;\n");
				classFound = true;
			}
		}
		if(classFound)source.append("    return false;\n");
		else source.append("    return true;\n");
		source.append("  }\n");
	}
	
	private void appendLocalVars(Set<Class<?>> requiredClasses, StringBuffer source){
		for(Class<?> actClass: requiredClasses){
			if(EpisimBiomechanicalModel.class.isAssignableFrom(actClass) ||EpisimCellBehavioralModel.class.isAssignableFrom(actClass) || AbstractCell.class.isAssignableFrom(actClass))				
				source.append(actClass.getSimpleName()+ " " + Names.convertClassToVariable(actClass.getSimpleName())+" = null;\n");
		}
	}
	private void appendAssignmentCheck(String varName, Set<Class<?>> requiredClasses, StringBuffer source){
		boolean firstLoop = true;
		
		for(Class<?> actClass: requiredClasses){
			if(EpisimBiomechanicalModel.class.isAssignableFrom(actClass) || EpisimCellBehavioralModel.class.isAssignableFrom(actClass) || AbstractCell.class.isAssignableFrom(actClass)){				
				
				if(firstLoop){
					source.append("if("+ actClass.getSimpleName()+ ".class.isAssignableFrom("+varName+ ".getClass())) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
					firstLoop = false;
					
				}
				else{
					source.append("else if("+ actClass.getSimpleName()+ ".class.isAssignableFrom("+varName+ ".getClass())) " + 
							Names.convertClassToVariable(actClass.getSimpleName())+"= ("+ actClass.getSimpleName()+ ")"+varName+ ";\n");
				}
				
			}
		}
	}

}
