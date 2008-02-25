package sim.app.episim.charts.build;

import java.awt.Color;


public interface EpisimChartSeries {
	
	long getId();
	
	String getName();
	Color getColor();
	double getWidth();
	double getStretch();
	float[] getDash();
	String getExpression();
	
	
	void setName(String name);
	void setColor(Color color);
	void setWidth(double width);
	void setDash(float[] dash);
	void setStretch(double val);
	void setExpression(String expression);

}
