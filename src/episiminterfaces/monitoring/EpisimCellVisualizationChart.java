package episiminterfaces.monitoring;

import java.io.File;
import java.util.Set;

import sim.app.episim.util.ProjectionPlane;
import episiminterfaces.calc.CellColoringConfigurator;


public interface EpisimCellVisualizationChart extends Cloneable{
	

	
	ProjectionPlane getCellProjectionPlane();
	long getId();
	String getTitle();
	String getXLabel();
	String getYLabel();
	boolean getDefaultColoring();
	CellColoringConfigurator getCellColoringConfigurator();
	boolean isPNGPrintingEnabled();
	int getPNGPrintingFrequency();
	int getChartUpdatingFrequency();
	
	double getMinXMikron();
	double getMinYMikron();
	double getMinZMikron();
	
	double getMaxXMikron();
	double getMaxYMikron();
	double getMaxZMikron();
	
	
	File getPNGPrintingPath();
	
	Set<Class<?>> getRequiredClasses();
	
	void setTitle(String title);
	void setXLabel(String xLabel);
	void setYLabel(String yLabel);
	void setDefaultColoring(boolean val);
	void setPNGPrintingEnabled(boolean val);
	void setPNGPrintingFrequency(int frequency);
	void setChartUpdatingFrequency(int frequency);
	void setCellColoringConfigurator(CellColoringConfigurator val);
	void setRequiredClasses(Set<Class<?>> requiredClasses);
	void setPNGPrintingPath(File path);
	void setCellProjectionPlane(ProjectionPlane plane);
	
	void setMinXMikron(double minXMikron);
	void setMinYMikron(double minYMikron);
	void setMinZMikron(double minZMikron);
	
	void setMaxXMikron(double maxXMikron);
	void setMaxYMikron(double maxYMikron);
	void setMaxZMikron(double maxZMikron);

	boolean isDirty();
	void setIsDirty(boolean value);
	
	EpisimCellVisualizationChart clone();
}
