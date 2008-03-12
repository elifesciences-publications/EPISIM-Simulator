package sim.app.episim.charts.build;


import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;

import episiminterfaces.*;



import sim.app.episim.CellType;
import sim.app.episim.util.Names;
import sim.engine.Steppable;


public class ChartSourceBuilder {
	
	
	private StringBuffer chartSource;
	
	private EpisimChart actChart;
	public ChartSourceBuilder(){
		
	}
	
	public String buildEpisimChartSource(EpisimChart episimChart){
		if(episimChart ==  null) throw new IllegalArgumentException("Episim-Chart was null!");
		this.actChart = episimChart;
		chartSource = new StringBuffer();
		
		appendHeader();
		appendDataFields();
		appendConstructor();
		appendStandardMethods();
		appendRegisterObjectsMethod();
		appendEnd();
		return chartSource.toString();
	}
	
	private void appendHeader(){
		
		chartSource.append("package "+ Names.GENERATEDCHARTSPACKAGENAME +";\n");
		chartSource.append("import org.jfree.chart.*;\n");
		chartSource.append("import org.jfree.chart.block.*;\n");
		chartSource.append("import org.jfree.chart.event.*;\n");
		chartSource.append("import org.jfree.chart.plot.*;\n");
		chartSource.append("import org.jfree.chart.renderer.xy.*;\n");
		chartSource.append("import org.jfree.chart.title.*;\n");
		chartSource.append("import org.jfree.data.general.*;\n");
		chartSource.append("import org.jfree.data.xy.*;\n");
		chartSource.append("import org.jfree.ui.*;\n");
		chartSource.append("import episiminterfaces.*;\n");
		chartSource.append("import episimexceptions.*;\n");
		chartSource.append("import episimfactories.*;\n");
		chartSource.append("import java.awt.*;\n");
		chartSource.append("import sim.app.episim.util.EnhancedSteppable;\n");
		chartSource.append("import sim.engine.Steppable;\n");
		
		chartSource.append("import sim.util.Bag;\n");
		chartSource.append("import sim.field.continuous.*;\n");
		for(Class<?> actClass: this.actChart.getRequiredClasses()){
			chartSource.append("import " + actClass.getCanonicalName()+";\n");	
		}
		
		chartSource.append("public class " +Names.convertVariableToClass(Names.cleanString(this.actChart.getTitle())+ this.actChart.getId())+" implements GeneratedChart{\n");
	
	}
	
	private void appendDataFields(){
				   
		   chartSource.append("  private JFreeChart chart;\n");
		   chartSource.append("  private Continuous2D cellContinuous;\n");
		   chartSource.append("  private Bag allCells;\n");
		   chartSource.append("  private ChartPanel chartPanel;\n");
		   chartSource.append("  private XYSeriesCollection dataset = new XYSeriesCollection();\n");
		   for(EpisimChartSeries actSeries: this.actChart.getEpisimChartSeries()){
		   	chartSource.append("  private XYSeries "+Names.cleanString(actSeries.getName())+actSeries.getId()+
		   			" = new XYSeries(\""+Names.cleanString(actSeries.getName())+"\", false);\n");
		   }
		   for(Class<?> actClass : this.actChart.getRequiredClasses())
				this.chartSource.append("  private "+ actClass.getSimpleName()+ " "+actClass.getSimpleName().substring(0, 1).toLowerCase() +
						actClass.getSimpleName().substring(1)+ ";\n");
	}
	
	private void appendConstructor(){
		chartSource.append("public " +Names.convertVariableToClass(Names.cleanString(this.actChart.getTitle())+ this.actChart.getId())+"(){\n");
		
		chartSource.append("  chart = ChartFactory.createXYLineChart(\""+actChart.getTitle()+"\",\""+actChart.getXLabel()+"\",\""+
				actChart.getYLabel()+"\",dataset,"+"PlotOrientation.VERTICAL, "+actChart.isLegendVisible()+", true, false);\n");
		chartSource.append("  ((XYLineAndShapeRenderer)(((XYPlot)(chart.getPlot())).getRenderer())).setDrawSeriesLineAsPath(true);\n");
		chartSource.append("  chart.setAntiAlias("+actChart.isAntialiasingEnabled()+");\n");
		chartSource.append("  chartPanel = new ChartPanel(chart, true);\n");
		chartSource.append("  chartPanel.setPreferredSize(new java.awt.Dimension(640,480));\n");
		chartSource.append("  chartPanel.setMinimumDrawHeight(10);\n");
		chartSource.append("  chartPanel.setMaximumDrawHeight(2000);\n");
		chartSource.append("  chartPanel.setMinimumDrawWidth(20);\n");
		chartSource.append("  chartPanel.setMaximumDrawWidth(2000);\n\n");
		
		if(actChart.isLegendVisible()) appendLegend();
		appendChartSeriesInit();
		
		chartSource.append("}\n");
	}
	private void appendEnd(){
		chartSource.append("}");
	}
	
	private void appendChartSeriesInit(){
		chartSource.append("  float[] newDash = null;\n");
		chartSource.append("  XYItemRenderer renderer = null;\n");
		int i = 0;
		for(EpisimChartSeries actSeries: actChart.getEpisimChartSeries()){
			
			chartSource.append("  dataset.addSeries("+Names.cleanString(actSeries.getName())+actSeries.getId()+");\n");
			
			chartSource.append("  newDash = new float[]{");
			for(int x=0;x<actSeries.getDash().length;x++){
				chartSource.append(""+((float)(actSeries.getDash()[x] * actSeries.getStretch()* actSeries.getThickness())));
				chartSource.append("f");
			   if(x != (actSeries.getDash().length-1)) chartSource.append(", ");
			}
			chartSource.append("};\n");
			chartSource.append("  renderer = (XYItemRenderer)(chartPanel.getChart().getXYPlot().getRenderer());\n");
			chartSource.append("  renderer.setSeriesStroke("+i+", new BasicStroke("+actSeries.getThickness()+"f, BasicStroke.CAP_ROUND, "+ 
		                                                "BasicStroke.JOIN_ROUND,0f,newDash,0f));\n");
			chartSource.append("  renderer.setSeriesPaint("+i+", new Color("+actSeries.getColor().getRed()+", "+
					actSeries.getColor().getGreen()+", "+actSeries.getColor().getBlue()+"));\n");
			i++;
		}
	}
	
	private void appendLegend(){
		chartSource.append("  LegendTitle title = new LegendTitle((XYItemRenderer) (chart.getXYPlot().getRenderer()));\n");
		chartSource.append("  title.setLegendItemGraphicPadding(new org.jfree.ui.RectangleInsets(0, 8, 0, 4));\n");
		chartSource.append("  title.setLegendItemGraphicAnchor(RectangleAnchor.BOTTOM);\n");
		chartSource.append("  title.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));\n");
		chartSource.append("  title.setFrame(new LineBorder());\n");
		chartSource.append("  title.setBackgroundPaint(Color.white);\n");
		chartSource.append("  title.setPosition(RectangleEdge.BOTTOM);\n");
	}
	
	private void appendRegisterObjectsMethod(){
		this.chartSource.append("  public void registerRequiredObjects(");
		for(Class<?> actClass: this.actChart.getRequiredClasses()){
			if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
				chartSource.append(actClass.getSimpleName() + " " + Names.convertClassToVariable(actClass.getSimpleName())+", ");
			}
		}
		this.chartSource.append("Bag allCells, Continuous2D cellContinuous){\n");
		this.chartSource.append("    this.allCells = allCells;\n");
		this.chartSource.append("    this.cellContinuous = cellContinuous;\n");
		for(Class<?> actClass: this.actChart.getRequiredClasses()){
			if(!EpisimCellDiffModel.class.isAssignableFrom(actClass) && !CellType.class.isAssignableFrom(actClass)){
				this.chartSource.append("    this." + Names.convertClassToVariable(actClass.getSimpleName())+" = "
						+ Names.convertClassToVariable(actClass.getSimpleName())+";\n");
			}
		}
		
		
		this.chartSource.append("  }\n");
		
	}
	
	private void appendStandardMethods(){
		chartSource.append("public ChartPanel getChartPanel(){ return this.chartPanel;}\n");
		chartSource.append("public EnhancedSteppable getSteppable(){return null;}\n");
		
	}
	

}
