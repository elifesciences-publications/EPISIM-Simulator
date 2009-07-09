package sim.app.episim.datamonitoring.charts;

import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChartSeries;



public class EpisimChartSeriesImpl implements EpisimChartSeries, java.io.Serializable{
	
	private long id;
	
	private String name = "";
	private Color color = null;
	private double thickness = 0;
	private double stretch = 0;
	private float[] dash= null;
	private CalculationAlgorithmConfigurator calculationAlgorithmConfigurator = null;
	private Set<Class<?>> requiredClasses;
	
	public EpisimChartSeriesImpl(long id){
		this.id = id;
		requiredClasses = new HashSet<Class<?>>();
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

	
	public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator() {
	
		return calculationAlgorithmConfigurator;
	}

	
	public void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator config) {
	
		this.calculationAlgorithmConfigurator = config;
	}

	public Set<Class<?>> getRequiredClasses() {

	  
	   return ObjectManipulations.cloneObject(requiredClasses);
   }

	public void setRequiredClasses(Set<Class<?>> classes) {

	   requiredClasses = classes;
	   
   }

	
	
}
