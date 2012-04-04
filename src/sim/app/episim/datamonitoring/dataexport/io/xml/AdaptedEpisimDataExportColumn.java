package sim.app.episim.datamonitoring.dataexport.io.xml;

import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import sim.app.episim.datamonitoring.xml.CalculationAlgorithmConfiguratorAdapter;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;


public class AdaptedEpisimDataExportColumn implements java.io.Serializable{
	
	private long id;
	private String name = null;
	private CalculationAlgorithmConfigurator calculationAlgorithmConfigurator;
	private Set<String> requiredClassesNameSet;
	
	public AdaptedEpisimDataExportColumn(){}

	
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
