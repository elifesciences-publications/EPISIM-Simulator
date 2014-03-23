package sim.app.episim.datamonitoring.charts.io.xml;

import java.io.File;

import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sim.app.episim.datamonitoring.xml.CellColoringConfiguratorAdapter;
import sim.app.episim.util.ProjectionPlane;
import episiminterfaces.calc.CellColoringConfigurator;



public class AdaptedEpisimCellVisualizationChart {

	
	private long id;
	private String title = "";
	
	private ProjectionPlane cellProjectionPlane = ProjectionPlane.XY_PLANE;
	private boolean pngPrintingEnabled = false;
	private int pngPrintingFrequency = 100;
	private int chartUpdatingFrequency = 10;

	private double minXMikron = Double.NEGATIVE_INFINITY;
	private double minYMikron = Double.NEGATIVE_INFINITY;
	private double minZMikron = Double.NEGATIVE_INFINITY;
	
	private double maxXMikron = Double.POSITIVE_INFINITY;
	private double maxYMikron = Double.POSITIVE_INFINITY;
	private double maxZMikron = Double.POSITIVE_INFINITY;
	
	private String xLabel = "";
	private String yLabel = "";

	private File pngPrintingPath = null;

	private CellColoringConfigurator cellColoringConfigurator = null;
	private boolean defaultColoring = false;
	
	private boolean isDirty = false;
	
	private Set<String> requiredClassesNameSet;

	public AdaptedEpisimCellVisualizationChart(){}
	
	public long getId() {   
   	return id;
   }

	
   public void setId(long id){   
   	this.id = id;
   }

	
   public String getTitle() {   
   	return title;
   }

	
   public void setTitle(String title) {   
   	this.title = title;
   }

	
   public String getXLabel() {
   
   	return xLabel;
   }

	
   public void setXLabel(String xLabel) {
   
   	this.xLabel = xLabel;
   }

	
   public String getYLabel() {
   
   	return yLabel;
   }

	
   public void setYLabel(String yLabel) {
   
   	this.yLabel = yLabel;
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

	public File getPngPrintingPath() {
   
   	return pngPrintingPath;
   }

	
   public void setPngPrintingPath(File pngPrintingPath) {
   
   	this.pngPrintingPath = pngPrintingPath;
   }

   @XmlJavaTypeAdapter(CellColoringConfiguratorAdapter.class)
   public CellColoringConfigurator getCellColoringConfigurator() {
   
   	return cellColoringConfigurator;
   }

	
   public void setCellColoringConfigurator(CellColoringConfigurator cellColoringConfigurator) {
   
   	this.cellColoringConfigurator = cellColoringConfigurator;
   }

	
   public boolean isDirty() {
   
   	return isDirty;
   }

	
   public void setDirty(boolean isDirty) {
   
   	this.isDirty = isDirty;
   }
   
   @XmlElement
   public Set<String> getRequiredClassesNameSet() {
   
   	return requiredClassesNameSet;
   }	
   public void setRequiredClassesNameSet(Set<String> requiredClassesNameSet) {
   
   	this.requiredClassesNameSet = requiredClassesNameSet;
   }

	@XmlElement
	public ProjectionPlane getCellProjectionPlane() {
   
   	return cellProjectionPlane;
   }
	
   public void setCellProjectionPlane(ProjectionPlane cellProjectionPlane) {
   
   	this.cellProjectionPlane = cellProjectionPlane;
   }

	
   public double getMinXMikron() {
   
   	return minXMikron;
   }

	
   public void setMinXMikron(double minXMikron) {
   
   	this.minXMikron = minXMikron;
   }

	
   public double getMinYMikron() {
   
   	return minYMikron;
   }

	
   public void setMinYMikron(double minYMikron) {
   
   	this.minYMikron = minYMikron;
   }

	
   public double getMinZMikron() {
   
   	return minZMikron;
   }

	
   public void setMinZMikron(double minZMikron) {
   
   	this.minZMikron = minZMikron;
   }

	
   public double getMaxXMikron() {
   
   	return maxXMikron;
   }

	
   public void setMaxXMikron(double maxXMikron) {
   
   	this.maxXMikron = maxXMikron;
   }

	
   public double getMaxYMikron() {
   
   	return maxYMikron;
   }

	
   public void setMaxYMikron(double maxYMikron) {
   
   	this.maxYMikron = maxYMikron;
   }

	
   public double getMaxZMikron() {
   
   	return maxZMikron;
   }

	
   public void setMaxZMikron(double maxZMikron) {
   
   	this.maxZMikron = maxZMikron;
   }

	
   public boolean getDefaultColoring() {
   
   	return defaultColoring;
   }

	
   public void setDefaultColoring(boolean defaultColoring) {
   
   	this.defaultColoring = defaultColoring;
   }
}
