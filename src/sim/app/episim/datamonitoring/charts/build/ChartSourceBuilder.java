package sim.app.episim.datamonitoring.charts.build;


import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;

import episiminterfaces.*;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;



import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.build.AbstractCommonSourceBuilder;
import sim.app.episim.datamonitoring.steppables.SteppableCodeFactory;
import sim.app.episim.util.Names;
import sim.engine.Steppable;


public class ChartSourceBuilder extends AbstractCommonSourceBuilder{
	
	
	public static final String CHARTDATAFIELDNAME = "chart";
	public static final String PNGDIRECTORYDATAFIELDNAME = "pngPrintingDirectory";
	
	public static final String PARAMETERSNAME = "calculationAlgorithmParameterValues";
	
	private EpisimChart actChart;
	public ChartSourceBuilder(){
	
	}
	
	public String buildEpisimChartSource(EpisimChart episimChart){
		if(episimChart ==  null) throw new IllegalArgumentException("Episim-Chart was null!");
		this.actChart = episimChart;
		generatedSourceCode = new StringBuffer();
		
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendStandardMethods();
		appendRegisterObjectsMethod(episimChart.getAllRequiredClasses());
		appendClearSeriesMethod();
		appendEnd();
		return generatedSourceCode.toString();
	}
	
	private void appendHeader(){
		
		generatedSourceCode.append("package "+ Names.GENERATEDCHARTSPACKAGENAME +";\n");
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
		generatedSourceCode.append("import episiminterfaces.calc.*;\n");
		generatedSourceCode.append("import episiminterfaces.monitoring.*;\n");
		generatedSourceCode.append("import episiminterfaces.*;\n");
		generatedSourceCode.append("import episimexceptions.*;\n");
		generatedSourceCode.append("import episimfactories.*;\n");
		
		generatedSourceCode.append("import java.awt.*;\n");
		generatedSourceCode.append("import java.io.*;\n");
		generatedSourceCode.append("import java.util.*;\n");
		generatedSourceCode.append("import sim.app.episim.util.*;\n");
		generatedSourceCode.append("import sim.engine.Steppable;\n");
		generatedSourceCode.append("import sim.app.episim.util.GenericBag;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.GlobalStatistics;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.calc.*;\n");
		generatedSourceCode.append("import sim.app.episim.datamonitoring.charts.io.*;\n");
		generatedSourceCode.append("import sim.app.episim.CellType;\n");
		generatedSourceCode.append("import sim.engine.SimState;\n");
		generatedSourceCode.append("import sim.field.continuous.*;\n");
		for(Class<?> actClass: this.actChart.getAllRequiredClasses()){
			generatedSourceCode.append("import " + actClass.getCanonicalName()+";\n");	
		}
		
		generatedSourceCode.append("public class " +Names.convertVariableToClass(Names.cleanString(this.actChart.getTitle())+ this.actChart.getId())+" implements GeneratedChart{\n");
	
	}
	
	protected void appendDataFields(){
			super.appendDataFields();	
			generatedSourceCode.append("  private EnhancedSteppable pngSteppable = null;\n");
		   generatedSourceCode.append("  private JFreeChart "+CHARTDATAFIELDNAME+";\n");
		   generatedSourceCode.append("  private Continuous2D cellContinuous;\n");
		   generatedSourceCode.append("  private GenericBag<CellType> allCells;\n");
		   generatedSourceCode.append("  private ChartPanel chartPanel;\n");
		   generatedSourceCode.append("  private XYSeriesCollection dataset = new XYSeriesCollection();\n");
		
		   
		   for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
		   	generatedSourceCode.append("  private XYSeries "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+
		   			" = new XYSeries(\""+Names.cleanString(actSeries.getName())+"\", false);\n");
		   }
		   for(Class<?> actClass : this.actChart.getAllRequiredClasses())
				this.generatedSourceCode.append("  private "+ Names.convertVariableToClass(actClass.getSimpleName())+ " "
						+Names.convertClassToVariable(actClass.getSimpleName())+ ";\n");
		   
	}
	
	private void appendConstructor(){
		generatedSourceCode.append("public " +Names.convertVariableToClass(Names.cleanString(this.actChart.getTitle())+ this.actChart.getId())+"(){\n");
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+" = ChartFactory.createXYLineChart(\""+actChart.getTitle()+"\",\""+actChart.getXLabel()+"\",\""+
				actChart.getYLabel()+"\",dataset,"+"PlotOrientation.VERTICAL, "+actChart.isLegendVisible()+", true, false);\n");
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setBackgroundPaint(Color.white);\n");
		generatedSourceCode.append("  ((XYLineAndShapeRenderer)(((XYPlot)("+CHARTDATAFIELDNAME+".getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);\n");
		generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".setAntiAlias("+actChart.isAntialiasingEnabled()+");\n");
		if(actChart.isXAxisLogarithmic()){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".getXYPlot().setDomainAxis(new LogarithmicAxis(\""+ actChart.getXLabel()+"\"));\n");
		}
		if(actChart.isYAxisLogarithmic()){
			generatedSourceCode.append("  "+CHARTDATAFIELDNAME+".getXYPlot().setRangeAxis(new LogarithmicAxis(\""+ actChart.getYLabel()+"\"));\n");
		}
		
		generatedSourceCode.append("  chartPanel = new ChartPanel("+CHARTDATAFIELDNAME+", true);\n");
		generatedSourceCode.append("  chartPanel.setPreferredSize(new java.awt.Dimension(640,480));\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawHeight(10);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawHeight(2000);\n");
		generatedSourceCode.append("  chartPanel.setMinimumDrawWidth(20);\n");
		generatedSourceCode.append("  chartPanel.setMaximumDrawWidth(2000);\n\n");
		
		
		
		
		if(actChart.isLegendVisible()) appendLegend();
		appendChartSeriesInit();
		
		
		long counter = 1;
		long baselineCalculationHandlerID = System.currentTimeMillis();
		Map <Long, Long> seriesCalculationHandlerIDs = new HashMap<Long, Long>();
		if(this.actChart.getBaselineCalculationAlgorithmConfigurator() == null)  baselineCalculationHandlerID = Long.MIN_VALUE;
		for(EpisimChartSeries series: actChart.getEpisimChartSeries()){
		
			seriesCalculationHandlerIDs.put(series.getId(), (System.currentTimeMillis()+ counter));
			counter++;
		}
		
		appendHandlerRegistration(baselineCalculationHandlerID, seriesCalculationHandlerIDs);
		appendSteppable(baselineCalculationHandlerID, seriesCalculationHandlerIDs);
		if(actChart.isPNGPrintingEnabled())appendPNGSteppable();
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
	
	private void appendSteppable(long baselineCalculationHandlerID, Map<Long, Long> seriesCalculationHandlerIDs){
		
		generatedSourceCode.append("steppable = "+SteppableCodeFactory.getEnhancedSteppableSourceCode(Names.CALCULATIONCALLBACKLIST, this.actChart.getChartUpdatingFrequency())+";\n");
	}
	
	private void appendPNGSteppable(){
		
		generatedSourceCode.append("pngSteppable = "+SteppableCodeFactory.getEnhancedSteppableForPNGPrinting(actChart)+";\n");
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
	   	generatedSourceCode.append("    "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId())+".clear();\n");
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
			generatedSourceCode.append(Names.CALCULATIONCALLBACKLIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			generatedSourceCode.append(buildCalculationHandler(baselineCalculationHandlerID, baselineCalculationHandlerID, true, config, this.actChart.getRequiredClassesForBaseline())
					+", ((XYSeries) null), "+ this.actChart.isXAxisLogarithmic() + ", " + this.actChart.isYAxisLogarithmic()+"));\n");
		}
		for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
			
			generatedSourceCode.append(Names.CALCULATIONCALLBACKLIST+".add(");
			generatedSourceCode.append("CalculationController.getInstance().registerAtCalculationAlgorithm(");
			generatedSourceCode.append(buildCalculationHandler(seriesCalculationHandlerIDs.get(actSeries.getId()), 
					                                             baselineCalculationHandlerID, false, actSeries.getCalculationAlgorithmConfigurator(), 
					                                             actSeries.getRequiredClasses()));
			generatedSourceCode.append(", "+Names.convertClassToVariable(Names.cleanString(actSeries.getName())+actSeries.getId()));
			generatedSourceCode.append(", "+ this.actChart.isXAxisLogarithmic() + ", " + this.actChart.isYAxisLogarithmic()+"));\n");
				
		}
	}

}
