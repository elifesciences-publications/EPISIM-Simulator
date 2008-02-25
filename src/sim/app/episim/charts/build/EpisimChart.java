package sim.app.episim.charts.build;

import java.util.List;

public interface EpisimChart {
	
	long getId();
	String getTitle();
	String getXLabel();
	String getYLabel();
	String getBaselineExpression();
	boolean isLegendVisible();
	boolean isAntialiasingEnabled();
	boolean isPDFPrintingEnabled();
	int getPDFPrintingFrequency();
	List<EpisimChartSeries> getEpisimChartSeries();
	
	void setTitle(String title);
	void setXLabel(String xLabel);
	void setYLabel(String yLabel);
	void setLegendVisible(boolean val);
	void setAntialiasingEnabled(boolean val);
	void setPDFPrintingEnabled(boolean val);
	void setPDFPrintingFrequency(int frequency);
	void setBaselineExpression(String val);
	void addEpisimChartSeries(EpisimChartSeries chartSeries);
	
	void removeChartSeries(long id);
}
