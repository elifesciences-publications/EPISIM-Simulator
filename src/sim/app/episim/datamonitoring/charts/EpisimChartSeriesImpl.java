package sim.app.episim.datamonitoring.charts;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;



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
	private transient Set<Class<?>> requiredClasses;
	private Set<String> requiredClassesNameSet;
	
	public EpisimChartSeriesImpl(long id){
		this.id = id;
		requiredClasses = new HashSet<Class<?>>();
		requiredClassesNameSet = new HashSet<String>();
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
		if(this.requiredClasses == null) this.requiredClasses = new HashSet<Class<?>>();
	   return ObjectManipulations.cloneObject(requiredClasses);
   }
	
	public Set<String> getRequiredClassesNameSet() {		  
	   return ObjectManipulations.cloneObject(requiredClassesNameSet);
   }

	public void setRequiredClasses(Set<Class<?>> classes){
	   if(classes != null){
	   	requiredClasses = classes;
	   	this.requiredClassesNameSet.clear();
	   	for(Class<?> actClass : this.requiredClasses){
	   		this.requiredClassesNameSet.add(actClass.getName());
	   	}
	   }
	   else{
	   	this.requiredClasses = new HashSet<Class<?>>();
	   	this.requiredClassesNameSet = new HashSet<String>();
	   }
   }

	public EpisimChartSeries clone(){
		EpisimChartSeries newSeries = ObjectManipulations.cloneObject(this);
		HashSet<Class<?>> newRequiredClasses = new HashSet<Class<?>>();
		newRequiredClasses.addAll(this.requiredClasses);
		newSeries.setRequiredClasses(newRequiredClasses);
		return newSeries;
	}
	
}
