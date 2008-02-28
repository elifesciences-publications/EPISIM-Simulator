package sim.app.episim.charts;

import java.awt.Color;
import java.io.File;

import javax.swing.JFileChooser;



public class EpisimChartSeriesImpl implements EpisimChartSeries, java.io.Serializable{
	
	private long id;
	
	private String name = "";
	private Color color = null;
	private double thickness = 0;
	private double stretch = 0;
	private float[] dash= null;
	private String[] expression = null;
	
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

	
	public String[] getExpression() {
	
		return expression;
	}

	
	public void setExpression(String[] expression) {
	
		this.expression = expression;
	}

	
	public EpisimChartSeries clone(){
		EpisimChartSeries clone = new EpisimChartSeriesImpl(this.id);
		clone.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue()));
		clone.setDash(this.dash.clone());
		clone.setExpression(this.expression.clone());
		clone.setName(this.name);
		clone.setStretch(this.stretch);
		clone.setThickness(this.thickness);
		
		return clone;
	}
	
	
}
