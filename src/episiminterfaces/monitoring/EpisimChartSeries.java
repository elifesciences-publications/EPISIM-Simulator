package episiminterfaces.monitoring;

import java.awt.Color;
import java.util.Set;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;


public interface EpisimChartSeries extends Cloneable{
	
	long getId();
	
	String getName();
	Color getColor();
	double getThickness();
	double getStretch();
	float[] getDash();
	CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator();
	Set<Class<?>> getRequiredClasses();
	
	void setName(String name);
	void setColor(Color color);
	void setThickness(double width);
	void setDash(float[] dash);
	void setStretch(double val);
	void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator config);
	void setRequiredClasses(Set<Class<?>> classes);
	
	EpisimChartSeries clone();
}
