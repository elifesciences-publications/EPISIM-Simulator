package sim.app.episim.charts;

import java.awt.Color;



public class EpisimChartSeriesImpl implements EpisimChartSeries{
	
	private long id;
	
	private String name = "";
	private Color color = null;
	private double thickness = 0;
	private double stretch = 0;
	private float[] dash= null;
	private String expression = "";
	
	public EpisimChartSeriesImpl(long id){
		this.id = id;
	}

	public long getId(){
		return this.id;
	}
	
	public String getName() {
	
		return name;
	}

	
	public void setName(String name) {
	
		this.name = name;
	}

	
	public Color getColor() {
	
		return color;
	}

	
	public void setColor(Color color) {
	
		this.color = color;
	}

	
	public double getThickness() {
	
		return thickness;
	}

	
	public void setThickness(double thickness) {
	
		this.thickness = thickness;
	}

	
	public double getStretch() {
	
		return stretch;
	}

	
	public void setStretch(double stretch) {
	
		this.stretch = stretch;
	}

	
	public float[] getDash() {
	
		return dash;
	}

	
	public void setDash(float[] dash) {
	
		this.dash = dash;
	}

	
	public String getExpression() {
	
		return expression;
	}

	
	public void setExpression(String expression) {
	
		this.expression = expression;
	}

}
