package sim.app.episim.datamonitoring.charts;

import java.io.File;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.monitoring.EpisimDiffFieldChart;


public class EpisimDiffFieldChartImpl implements EpisimDiffFieldChart, java.io.Serializable{
	
	private long id;
	private String title;
	private String diffFieldName;
	
	private boolean pngPrintingEnabled = false;
	
	private int pngPrintingFrequency = 1;
	private int chartUpdatingFrequency = 1;
	
	private File pngPrintingPath;
	
	private boolean isDirty = false;
	
	public EpisimDiffFieldChartImpl(long id){
		this.id = id;
	}	

	public long getId() {
		return this.id;
	}

	public void setChartTitle(String title) {
		this.title = title;
	}

	public String getChartTitle() {
		return this.title;
	}

	public void setDiffusionFieldName(String name) {
		this.diffFieldName = name;
	}

	public String getDiffusionFieldName() {
		return this.diffFieldName;
	}

	public void setPNGPrintingEnabled(boolean enabled) {
		this.pngPrintingEnabled = enabled;
	}

	public boolean isPNGPrintingEnabled() {
		return this.pngPrintingEnabled;
	}

	public void setPNGPrintingFrequency(int frequency) {
		this.pngPrintingFrequency = frequency;
	}

	public int getPNGPrintingFrequency() {
		return this.pngPrintingFrequency;
	}

	public void setChartUpdatingFrequency(int frequency) {
		this.chartUpdatingFrequency = frequency;
	}

	public int getChartUpdatingFrequency() {
		return this.chartUpdatingFrequency; 
	}

	public void setPNGPrintingPath(File path) {
		this.pngPrintingPath = path;
	}

	public File getPNGPrintingPath() {
		return this.pngPrintingPath;
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public void setIsDirty(boolean value) {
		this.isDirty = value;
	}
	
	public EpisimDiffFieldChart clone(){
		EpisimDiffFieldChart newChart = ObjectManipulations.cloneObject(this);
		return newChart;
	}

}
