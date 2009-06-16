package episiminterfaces.monitoring;

import java.io.File;
import java.util.List;
import java.util.Set;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;




public interface EpisimChart{
	
	long getId();
	String getTitle();
	String getXLabel();
	String getYLabel();
	CalculationAlgorithmConfigurator getBaselineCalculationAlgorithmConfigurator();
	boolean isLegendVisible();
	boolean isXAxisLogarithmic();
	boolean isYAxisLogarithmic();
	boolean isAntialiasingEnabled();
	boolean isPNGPrintingEnabled();
	int getPNGPrintingFrequency();
	int getChartUpdatingFrequency();
	List<EpisimChartSeries> getEpisimChartSeries();
	EpisimChartSeries getEpisimChartSeries(long no);
	File getPNGPrintingPath();
	Set<Class<?>> getRequiredClasses();
	
	void setTitle(String title);
	void setXLabel(String xLabel);
	void setYLabel(String yLabel);
	void setLegendVisible(boolean val);
	void setAntialiasingEnabled(boolean val);
	void setPNGPrintingEnabled(boolean val);
	void setPNGPrintingFrequency(int frequency);
	void setChartUpdatingFrequency(int frequency);
	void setBaselineCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator val);
	void addEpisimChartSeries(EpisimChartSeries chartSeries);
	void addRequiredClass(Class<?> requiredClass);
	void setPNGPrintingPath(File path);
	void setXAxisLogarithmic(boolean val);
	void setYAxisLogarithmic(boolean val);
	
	void removeChartSeries(long id);
	
	boolean isDirty();
	void setIsDirty(boolean value);
	
	
}
