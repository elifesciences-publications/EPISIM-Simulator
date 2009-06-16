package episiminterfaces.monitoring;

import java.awt.Color;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;


public interface EpisimChartSeries{
	
	long getId();
	
	String getName();
	Color getColor();
	double getThickness();
	double getStretch();
	float[] getDash();
	CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator();
	
	
	void setName(String name);
	void setColor(Color color);
	void setThickness(double width);
	void setDash(float[] dash);
	void setStretch(double val);
	void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator config);
	
}
