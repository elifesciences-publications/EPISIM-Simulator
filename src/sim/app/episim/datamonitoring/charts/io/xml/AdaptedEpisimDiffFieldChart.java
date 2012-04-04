package sim.app.episim.datamonitoring.charts.io.xml;

import java.io.File;


public class AdaptedEpisimDiffFieldChart implements java.io.Serializable{
	
	private long id;
	private String title;
	private String diffFieldName;
	
	private boolean pngPrintingEnabled = false;
	
	private int pngPrintingFrequency = 100;
	private int chartUpdatingFrequency = 100;
	
	private File pngPrintingPath;
	
	private boolean isDirty = false;
	
	public AdaptedEpisimDiffFieldChart(){}

	
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

	
   public String getDiffFieldName() {
   
   	return diffFieldName;
   }

	
   public void setDiffFieldName(String diffFieldName) {
   
   	this.diffFieldName = diffFieldName;
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

	
   public boolean isDirty() {
   
   	return isDirty;
   }

	
   public void setDirty(boolean isDirty) {
   
   	this.isDirty = isDirty;
   }

}
