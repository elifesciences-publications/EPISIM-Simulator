package sim.app.episim.datamonitoring.charts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.util.ProjectionPlane;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CellColoringConfigurator;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSeries;


public class EpisimCellVisualizationChartImpl implements EpisimCellVisualizationChart, java.io.Serializable{
	
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

private transient Set<Class<?>> requiredClasses;
private Set<String> requiredClassesNameSet;

public EpisimCellVisualizationChartImpl(long id){
	this.id = id;				
	this.requiredClasses = new HashSet<Class<?>>();
	this.requiredClassesNameSet = new HashSet<String>();
}

public long getId(){
	return this.id;
}
	
public int getPNGPrintingFrequency() {	
	return pngPrintingFrequency;
}

public int getChartUpdatingFrequency() {	
	return chartUpdatingFrequency;
}

public String getTitle() {
	return title;
}

public String getXLabel() {
	return xLabel;
}

public String getYLabel() {
	return yLabel;
}

public boolean isPNGPrintingEnabled() {	
	return pngPrintingEnabled;
}

public void setPNGPrintingEnabled(boolean val) {
	this.pngPrintingEnabled = val;	
}

public void setPNGPrintingFrequency(int frequency) {
	this.pngPrintingFrequency = frequency;	
}

public void setChartUpdatingFrequency(int frequency) {
	this.chartUpdatingFrequency = frequency;	
}

public void setTitle(String title) {
	if(title != null && !title.trim().equals("")) this.title = title;	
}

public void setXLabel(String label) {
	this.xLabel = label;	
}

public void setYLabel(String label) {
	this.yLabel = label;	
}

public ProjectionPlane getCellProjectionPlane(){
	return this.cellProjectionPlane;
}

public CellColoringConfigurator getCellColoringConfigurator() {
	return cellColoringConfigurator;
}

public void setCellColoringConfigurator(CellColoringConfigurator val) {
	this.cellColoringConfigurator = val;
}

public File getPNGPrintingPath() { 
   return this.pngPrintingPath;
}

public void setPNGPrintingPath(File path) {
  this.pngPrintingPath = path;   
}

public void setCellProjectionPlane(ProjectionPlane projectionPlane){
	this.cellProjectionPlane = projectionPlane;
}

public boolean isDirty() {
	return isDirty;
}


public void setIsDirty(boolean isDirty) {
	this.isDirty = isDirty;
}

public Set<Class<?>> getRequiredClasses(){
	if(this.requiredClasses == null) this.requiredClasses = new HashSet<Class<?>>();
   return ObjectManipulations.cloneObject(requiredClasses);
}	

public Set<String> getRequiredClassesNameSet(){	   
   return ObjectManipulations.cloneObject(this.requiredClassesNameSet);
}

public void setRequiredClasses(Set<Class<?>> requiredClasses) {
	if(requiredClasses != null){
		this.requiredClasses = requiredClasses;
		this.requiredClassesNameSet.clear();
		for(Class<?> actClass : this.requiredClasses){
			this.requiredClassesNameSet.add(actClass.getName());
		}
	}
	else{
		this.requiredClasses = new HashSet<Class<?>>();
		this.requiredClassesNameSet = new HashSet<String>();
	}
}
public EpisimCellVisualizationChart clone(){
	EpisimCellVisualizationChart newChart = ObjectManipulations.cloneObject(this);
	HashSet<Class<?>> newRequiredClasses = new HashSet<Class<?>>();
	newRequiredClasses.addAll(this.requiredClasses);
	newChart.setRequiredClasses(newRequiredClasses);	
	return newChart;
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

public boolean getDefaultColoring(){
	return defaultColoring;
}

public void setDefaultColoring(boolean defaultColoring){
	this.defaultColoring = defaultColoring;
}

}
