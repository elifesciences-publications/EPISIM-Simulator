package sim.app.episim.datamonitoring.charts.io.xml;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import sim.app.episim.datamonitoring.xml.CalculationAlgorithmConfiguratorAdapter;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChartSeries;


public class AdaptedEpisimChart implements java.io.Serializable{
	
	private long id;
	private String title = "";
	private String xLabel = "";
	private String yLabel = "";
	private boolean antiAliasingEnabled = false;
	private boolean legendVisible = false;
	private boolean pngPrintingEnabled = false;
	private int pngPrintingFrequency = 100;
	private int chartUpdatingFrequency = 100;
	private boolean xAxisLogarithmic = false;
	private boolean yAxisLogarithmic = false;	
	
	private File pngPrintingPath = null;
	
	private CalculationAlgorithmConfigurator baselineCalculationAlgorithmConfigurator = null;
	
	private boolean isDirty = false;
	
	private List<EpisimChartSeries> episimChartSeries;
	
	private Set<String> requiredClassesForBaselineNameSet;

	
	public AdaptedEpisimChart(){}
	
   public long getId() {
   
   	return id;
   }

	
   public void setId(long id) {
   
   	this.id = id;
   }

	
   public String getTitle() {
   
   	return title;
   }

	
   public void setTitle(String title) {
   
   	this.title = title;
   }

	
   public String getxLabel() {
   
   	return xLabel;
   }

	
   public void setxLabel(String xLabel) {
   
   	this.xLabel = xLabel;
   }

	
   public String getyLabel() {
   
   	return yLabel;
   }

	
   public void setyLabel(String yLabel) {
   
   	this.yLabel = yLabel;
   }

	
   public boolean isAntiAliasingEnabled() {
   
   	return antiAliasingEnabled;
   }

	
   public void setAntiAliasingEnabled(boolean antiAliasingEnabled) {
   
   	this.antiAliasingEnabled = antiAliasingEnabled;
   }

	
   public boolean isLegendVisible() {
   
   	return legendVisible;
   }

	
   public void setLegendVisible(boolean legendVisible) {
   
   	this.legendVisible = legendVisible;
   }

	
   public boolean isPngPrintingEnabled() {
   
   	return pngPrintingEnabled;
   }

	
   public void setPngPrintingEnabled(boolean pngPrintingEnabled) {
   
   	this.pngPrintingEnabled = pngPrintingEnabled;
   }

	
   public int getPngPrintingFrequency() {
   
   	return pngPrintingFrequency;
   }

	
   public void setPngPrintingFrequency(int pngPrintingFrequency) {
   
   	this.pngPrintingFrequency = pngPrintingFrequency;
   }

	
   public int getChartUpdatingFrequency() {
   
   	return chartUpdatingFrequency;
   }

	
   public void setChartUpdatingFrequency(int chartUpdatingFrequency) {
   
   	this.chartUpdatingFrequency = chartUpdatingFrequency;
   }

	
   public boolean isxAxisLogarithmic() {
   
   	return xAxisLogarithmic;
   }

	
   public void setxAxisLogarithmic(boolean xAxisLogarithmic) {
   
   	this.xAxisLogarithmic = xAxisLogarithmic;
   }

	
   public boolean isyAxisLogarithmic() {
   
   	return yAxisLogarithmic;
   }

	
   public void setyAxisLogarithmic(boolean yAxisLogarithmic) {
   
   	this.yAxisLogarithmic = yAxisLogarithmic;
   }

	
   public File getPngPrintingPath() {
   
   	return pngPrintingPath;
   }

	
   public void setPngPrintingPath(File pngPrintingPath) {
   
   	this.pngPrintingPath = pngPrintingPath;
   }

   @XmlJavaTypeAdapter(CalculationAlgorithmConfiguratorAdapter.class)
   public CalculationAlgorithmConfigurator getBaselineCalculationAlgorithmConfigurator() {
   
   	return baselineCalculationAlgorithmConfigurator;
   }

	
   public void setBaselineCalculationAlgorithmConfigurator(
         CalculationAlgorithmConfigurator baselineCalculationAlgorithmConfigurator) {
   
   	this.baselineCalculationAlgorithmConfigurator = baselineCalculationAlgorithmConfigurator;
   }

	
   public boolean isDirty() {
   
   	return isDirty;
   }

	
   public void setDirty(boolean isDirty) {
   
   	this.isDirty = isDirty;
   }

   @XmlElement
   public Set<String> getRequiredClassesForBaselineNameSet() {
   
   	return requiredClassesForBaselineNameSet;
   }

	
   public void setRequiredClassesForBaselineNameSet(Set<String> requiredClassesForBaselineNameSet) {
   
   	this.requiredClassesForBaselineNameSet = requiredClassesForBaselineNameSet;
   }

	@XmlElement
	@XmlJavaTypeAdapter(EpisimChartSeriesAdapter.class)
   public List<EpisimChartSeries> getEpisimChartSeries() {
   
   	return episimChartSeries;
   }

	
   public void setEpisimChartSeries(List<EpisimChartSeries> episimChartSeries) {
   
   	this.episimChartSeries = episimChartSeries;
   }

}
