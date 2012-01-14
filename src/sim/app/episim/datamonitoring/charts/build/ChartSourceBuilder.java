package sim.app.episim.datamonitoring.charts.build;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.xy.XYSeries;

import episiminterfaces.*;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;



import sim.app.episim.AbstractCell;
import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.build.AbstractCommonSourceBuilder;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.datamonitoring.steppables.SteppableCodeFactory;
import sim.app.episim.util.Names;
import sim.engine.Steppable;


public class ChartSourceBuilder extends AbstractCommonSourceBuilder{
	
	
	public static final String CHARTDATAFIELDNAME = "chart";
	public static final String PNGDIRECTORYDATAFIELDNAME = "pngPrintingDirectory";
	
	public static final String PARAMETERSNAME = "calculationAlgorithmParameterValues";
	
	private EpisimChart actChart;
	
	
	protected enum ChartSourceBuilderMode {XYSERIESMODE, HISTOGRAMMODE};
	
	private ChartSourceBuilderMode mode = ChartSourceBuilderMode.XYSERIESMODE;
	
	public ChartSourceBuilder(){
		
	}
	
	
	
	
	public String buildEpisimChartSource(EpisimChart episimChart){
		if(episimChart ==  null) throw new IllegalArgumentException("Episim-Chart was null!");
		this.actChart = episimChart;
		generatedSourceCode = new StringBuffer();
		
		if(this.actChart.getEpisimChartSeries().size() >= 1){
			CalculationAlgorithmType type = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(this.actChart.getEpisimChartSeries().get(0).getCalculationAlgorithmConfigurator().getCalculationAlgorithmID()).getType();
			if(type == CalculationAlgorithmType.HISTOGRAMRESULT) this.mode = ChartSourceBuilderMode.HISTOGRAMMODE;
			else this.mode = ChartSourceBuilderMode.XYSERIESMODE;
		}		
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendStandardMethods();
		appendRegisterObjectsMethod(episimChart.getAllRequiredClasses());
		appendClearSeriesMethod();
		if(mode == ChartSourceBuilderMode.HISTOGRAMMODE) appendBuildBinsMethod();
		appendEnd();
		return generatedSourceCode.toString();
	}
	
	private void appendHeader(){
		
		generatedSourceCode.append("package "+ Names.GENERATED_CHARTS_PACKAGENAME +";\n");		
		
		generatedSourceCode.append("import org.jfree.chart.*;\n");
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
		generatedSourceCode.append("import java.io.*;\n");
		generatedSourceCode.append("import java.util.*;\n");
		generatedSourceCode.append("import sim.*;\n");
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
	
		for(Class<?> actClass: this.actChart.getAllRequiredClasses()){
			generatedSourceCode.append("import " + actClass.getCanonicalName()+";\n");	
		}
		
		generatedSourceCode.append("public class " +Names.convertVariableToClass(Names.cleanString(this.actChart.getTitle())+ this.actChart.getId())+" implements GeneratedChart{\n");
	
	}
	
	protected void appendDataFields(){
			super.appendDataFields();	
			generatedSourceCode.append("  private EnhancedSteppable pngSteppable = null;\n");
		   generatedSourceCode.append("  private JFreeChart "+CHARTDATAFIELDNAME+";\n");
		   generatedSourceCode.append("  private GenericBag<AbstractCell> allCells;\n");
		   generatedSourceCode.append("  private ChartPanel chartPanel;\n");
		  if(mode == ChartSourceBuilderMode.XYSERIESMODE){ 
			  	generatedSourceCode.append("  private XYSeriesCollection dataset = new XYSeriesCollection();\n");
		  				   
			   for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
			   	
			   	generatedSourceCode.append("  private XYSeries "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+
			   			" = new XYSeries(\""+Names.cleanString(actSeries.getName())+"\", false);\n");
			   }
		  }
		  if(mode == ChartSourceBuilderMode.HISTOGRAMMODE){ 
			  
		  				   
			   for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
			   	
			   	generatedSourceCode.append("  private SimpleHistogramDataset "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+
			   			" = new SimpleHistogramDataset(\""+Names.cleanString(actSeries.getName())+"\");\n");
			   }
		  }
		  
		  
		   for(Class<?> actClass : this.actChart.getAllRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
		   
	}
	
	private void appendConstructor(){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(this.actChart.getTitle())+ this.actChart.getId())+"(){\n");
		
		if(mode == ChartSourceBuilderMode.XYSERIESMODE){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+" = ChartFactory.createXYLineChart(\""+actChart.getTitle()+"\",\""+actChart.getXLabel()+"\",\""+
					actChart.getYLabel()+"\",dataset,"+"PlotOrientation.VERTICAL, "+actChart.isLegendVisible()+", true, false);\n");
			generatedSourceCode.append("  ((XYLineAndShapeRenderer)(((XYPlot)("+CHARTDATAFIELDNAME+".getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);\n");
		}
		if(mode == ChartSourceBuilderMode.HISTOGRAMMODE){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+" = ChartFactory.createHistogram(\""+actChart.getTitle()+"\",\""+actChart.getXLabel()+"\",\""+
					actChart.getYLabel()+"\", null,"+"PlotOrientation.VERTICAL, "+actChart.isLegendVisible()+", true, false);\n");
		}	
		
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setBackgroundPaint(Color.white);\n");
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setAntiAlias("+actChart.isAntialiasingEnabled()+");\n");
		generatedSourceCode.append("   XYPlot xyPlot = (XYPlot) "+CHARTDATAFIELDNAME+".getXYPlot();\n");
		generatedSourceCode.append("   xyPlot.setBackgroundPaint(Color.WHITE);\n");
		generatedSourceCode.append("   xyPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);\n");
		generatedSourceCode.append("   xyPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);\n");
		
		if(actChart.isXAxisLogarithmic()){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".getXYPlot().setDomainAxis(new LogarithmicAxis(\""+ actChart.getXLabel()+"\"));\n");
		}
		if(actChart.isYAxisLogarithmic()){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".getXYPlot().setRangeAxis(new LogarithmicAxis(\""+ actChart.getYLabel()+"\"));\n");
		}
		
		generatedSourceCode.append("  chartPanel = new EpisimChartPanel("+CHARTDATAFIELDNAME+", true);\n");
		generatedSourceCode.append("  chartPanel.setPreferredSize(new java.awt.Dimension(640,480));\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawHeight(10);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawHeight(2000);\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawWidth(20);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawWidth(2000);\n\n");
		
		
		
		
		if(actChart.isLegendVisible()) appendLegend();
		
		if(mode == ChartSourceBuilderMode.XYSERIESMODE) appendChartSeriesInit();
		if(mode == ChartSourceBuilderMode.HISTOGRAMMODE) appendHistogramDatasetInit();
		
		
		
		long baselineCalculationHandlerID = AbstractCommonSourceBuilder.getNextCalculationHandlerId();
		Map <Long, Long> seriesCalculationHandlerIDs = new HashMap<Long, Long>();
		if(this.actChart.getBaselineCalculationAlgorithmConfigurator() == null)  baselineCalculationHandlerID = Long.MIN_VALUE;
		for(EpisimChartSeries series: actChart.getEpisimChartSeries()){
		
			seriesCalculationHandlerIDs.put(series.getId(), AbstractCommonSourceBuilder.getNextCalculationHandlerId());
			
		}
		
		appendHandlerRegistration(baselineCalculationHandlerID, seriesCalculationHandlerIDs);
		appendSteppable(baselineCalculationHandlerID, seriesCalculationHandlerIDs);
		appendPNGSteppable();
		generatedSourceCode.append("}\n");
	}
	
	
	private void appendChartSeriesInit(){
		generatedSourceCode.append("  float[] newDash = null;\n");
		generatedSourceCode.append("  XYItemRenderer renderer = null;\n");
		int i = 0;
		for(EpisimChartSeries actSeries: actChart.getEpisimChartSeries()){
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
	
	private void appendHistogramDatasetInit(){
		
		
		
		generatedSourceCode.append("  XYPlot plot = "+CHARTDATAFIELDNAME+".getXYPlot();\n");
		
		
		generatedSourceCode.append("  XYBarRenderer renderer = null;\n");
		int i = 0;
		for(EpisimChartSeries actSeries: actChart.getEpisimChartSeries()){
			
			generatedSourceCode.append("  plot.setDataset("+ i + ", "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+ ");\n");
			generatedSourceCode.append("  renderer = new XYBarRenderer();\n");
			generatedSourceCode.append("  renderer.setSeriesPaint(0, new Color("+actSeries.getColor().getRed()+", "+
					actSeries.getColor().getGreen()+", "+actSeries.getColor().getBlue()+"));\n");
		
			generatedSourceCode.append("  for(SimpleHistogramBin bin: buildBins("+actSeries.getCalculationAlgorithmConfigurator().getParameters().get(CalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER)
					+", "+actSeries.getCalculationAlgorithmConfigurator().getParameters().get(CalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER)
					+","+actSeries.getCalculationAlgorithmConfigurator().getParameters().get(CalculationAlgorithm.HISTOGRAMNUMBEROFBINSPARAMETER)+")) "
					+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".addBin(bin);\n");
			
			generatedSourceCode.append(Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".setAdjustForBinSize(false);");
			generatedSourceCode.append("  plot.setRenderer("+i+", renderer);\n");	
			
			i++;
		}
		if(actChart.getEpisimChartSeries().size() > 1) 	generatedSourceCode.append("  plot.setForegroundAlpha(0.6F);\n");
	}
	
	
	
	
	private void appendSteppable(long baselineCalculationHandlerID, Map<Long, Long> seriesCalculationHandlerIDs){
		
		generatedSourceCode.append("steppable = "+SteppableCodeFactory.getEnhancedSteppableSourceCode(Names.CALCULATION_CALLBACK_LIST, this.actChart.getChartUpdatingFrequency())+";\n");
	}
	
	private void appendPNGSteppable(){
		
		generatedSourceCode.append("pngSteppable = "+ SteppableCodeFactory.getEnhancedSteppableForPNGPrinting(actChart)+";\n");
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
	
	private void appendClearSeriesMethod(){
		
		
		
		generatedSourceCode.append("  public void clearAllSeries(){\n");
		
		for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
			if(mode == ChartSourceBuilderMode.XYSERIESMODE) generatedSourceCode.append("    "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".clear();\n");
			if(mode == ChartSourceBuilderMode.HISTOGRAMMODE) generatedSourceCode.append("    "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".clearObservations();\n");
	   }
		generatedSourceCode.append("  }\n");
	}
	
	
		
	protected void appendStandardMethods(){
		super.appendStandardMethods();
		generatedSourceCode.append("public ChartPanel getChartPanel(){ return this.chartPanel;}\n");
		
		generatedSourceCode.append("public EnhancedSteppable getPNGSteppable(){ return this.pngSteppable;}\n");
		
	}
	
	private void appendHandlerRegistration(long baselineCalculationHandlerID, Map<Long, Long> seriesCalculationHandlerIDs){
		CalculationAlgorithmConfigurator config = this.actChart.getBaselineCalculationAlgorithmConfigurator();
		if(config != null){
			generatedSourceCode.append(Names.CALCULATION_CALLBACK_LIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			if(mode == ChartSourceBuilderMode.XYSERIESMODE){
				generatedSourceCode.append(buildCalculationHandler(baselineCalculationHandlerID, baselineCalculationHandlerID, true, config, this.actChart.getRequiredClassesForBaseline())
						+", ((XYSeries)null), "+ this.actChart.isXAxisLogarithmic() + ", " + this.actChart.isYAxisLogarithmic()+"));\n");
			}
			else if(mode == ChartSourceBuilderMode.HISTOGRAMMODE){
				generatedSourceCode.append(buildCalculationHandler(baselineCalculationHandlerID, baselineCalculationHandlerID, true, config, this.actChart.getRequiredClassesForBaseline())
						+", ((SimpleHistogramDataset) null), "+ this.actChart.isXAxisLogarithmic() + ", " + this.actChart.isYAxisLogarithmic()+"));\n");
			}
		}
		for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
			
			generatedSourceCode.append(Names.CALCULATION_CALLBACK_LIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			generatedSourceCode.append(buildCalculationHandler(seriesCalculationHandlerIDs.get(actSeries.getId()), 
					                                             baselineCalculationHandlerID, false, actSeries.getCalculationAlgorithmConfigurator(), 
					                                             actSeries.getRequiredClasses()));
			generatedSourceCode.append(", "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId()));
			generatedSourceCode.append(", "+ this.actChart.isXAxisLogarithmic() + ", " + this.actChart.isYAxisLogarithmic()+"));\n");
				
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
	   generatedSourceCode.append("  double binSize = (Math.abs(maxValue - minValue)+1) / ((double)numberOfBins);\n");
	   generatedSourceCode.append("  SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins];\n");				
	   generatedSourceCode.append("  for(int i = 0; i < numberOfBins; i ++){\n");
	   generatedSourceCode.append("    bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, false);\n");
	   generatedSourceCode.append("  }\n");		
	   generatedSourceCode.append("  return bins;\n");
	   generatedSourceCode.append("}\n");
	}

}
