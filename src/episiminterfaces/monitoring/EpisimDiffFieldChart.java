package episiminterfaces.monitoring;

import java.io.File;


public interface EpisimDiffFieldChart {
	
	long getId();
	
	void setChartTitle(String title);
	String getChartTitle();
	
	void setDiffusionFieldName(String name);
	String getDiffusionFieldName();
	
	void setPNGPrintingEnabled(boolean enabled);
	boolean isPNGPrintingEnabled();
	
	void setPNGPrintingFrequency(int frequency);
	int getPNGPrintingFrequency();
	
	void setChartUpdatingFrequency(int frequency);
	int getChartUpdatingFrequency();
	
	void setPNGPrintingPath(File path);
	File getPNGPrintingPath();
	
	boolean isDirty();
	void setIsDirty(boolean value);
	
	EpisimDiffFieldChart clone();
}
