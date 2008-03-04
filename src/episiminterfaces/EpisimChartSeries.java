package episiminterfaces;

import java.awt.Color;


public interface EpisimChartSeries extends Cloneable{
	
	long getId();
	
	String getName();
	Color getColor();
	double getThickness();
	double getStretch();
	float[] getDash();
	String[] getExpression();
	
	
	void setName(String name);
	void setColor(Color color);
	void setThickness(double width);
	void setDash(float[] dash);
	void setStretch(double val);
	void setExpression(String[] expression);
	EpisimChartSeries clone();
}
