package sim.app.episim.datamonitoring.charts.io.xml;

import java.awt.Color;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import sim.app.episim.datamonitoring.xml.CalculationAlgorithmConfiguratorAdapter;
import sim.app.episim.datamonitoring.xml.ColorAdapter;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;


public class AdaptedEpisimChartSeries implements java.io.Serializable{
	
	private long id;
	
	private String name = "";
	private Color color = null;
	private double thickness = 0;
	private double stretch = 0;
	private float[] dash= null;
	private CalculationAlgorithmConfigurator calculationAlgorithmConfigurator = null;
	private Set<String> requiredClassesNameSet;
	
	public AdaptedEpisimChartSeries(){}

	
   public long getId() {
   
   	return id;
   }

	
   public void setId(long id) {
   
   	this.id = id;
   }

	
   public String getName() {
   
   	return name;
   }

	
   public void setName(String name) {
   
   	this.name = name;
   }

   @XmlJavaTypeAdapter(ColorAdapter.class)
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

   @XmlJavaTypeAdapter(CalculationAlgorithmConfiguratorAdapter.class)
   public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator() {
   
   	return calculationAlgorithmConfigurator;
   }

	
   public void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator calculationAlgorithmConfigurator) {
   
   	this.calculationAlgorithmConfigurator = calculationAlgorithmConfigurator;
   }

	@XmlElement
   public Set<String> getRequiredClassesNameSet() {
   
   	return requiredClassesNameSet;
   }

	
   public void setRequiredClassesNameSet(Set<String> requiredClassesNameSet) {
   
   	this.requiredClassesNameSet = requiredClassesNameSet;
   }

}
