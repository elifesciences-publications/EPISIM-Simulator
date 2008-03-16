package episiminterfaces;

import java.io.File;
import java.util.List;
import java.util.Set;



public interface EpisimChart{
	
	long getId();
	String getTitle();
	String getXLabel();
	String getYLabel();
	String[] getBaselineExpression();
	boolean isLegendVisible();
	boolean isAntialiasingEnabled();
	boolean isPDFPrintingEnabled();
	int getPDFPrintingFrequency();
	int getChartUpdatingFrequency();
	List<EpisimChartSeries> getEpisimChartSeries();
	EpisimChartSeries getEpisimChartSeries(long no);
	File getPDFPrintingPath();
	Set<Class<?>> getRequiredClasses();
	
	void setTitle(String title);
	void setXLabel(String xLabel);
	void setYLabel(String yLabel);
	void setLegendVisible(boolean val);
	void setAntialiasingEnabled(boolean val);
	void setPDFPrintingEnabled(boolean val);
	void setPDFPrintingFrequency(int frequency);
	void setChartUpdatingFrequency(int frequency);
	void setBaselineExpression(String[] val);
	void addEpisimChartSeries(EpisimChartSeries chartSeries);
	void addRequiredClass(Class<?> requiredClass);
	void setPDFPrintingPath(File path);
	
	
	void removeChartSeries(long id);
	
	
}
