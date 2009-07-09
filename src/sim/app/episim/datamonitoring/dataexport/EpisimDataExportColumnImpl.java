package sim.app.episim.datamonitoring.dataexport;

import java.util.HashSet;
import java.util.Set;

import sim.app.episim.util.ObjectManipulations;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimDataExportColumn;


public class EpisimDataExportColumnImpl implements EpisimDataExportColumn{
	
	private final long id;
	private String name = null;
	private CalculationAlgorithmConfigurator calculationAlgorithmConfigurator;
	private Set<Class<?>> requiredClasses;
	
	public EpisimDataExportColumnImpl(long id){
		this.id = id;
		requiredClasses = new HashSet<Class<?>>();
	}
	

	public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator() { return this.calculationAlgorithmConfigurator; }

	public long getId() { return id; }

	public String getName(){ return this.name; }
   
	public void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator config) {

	   if(config == null) 
	   	throw new IllegalArgumentException("This Calculation Algorithm Configurator is null or does not contain proper caluculation expressions!");
	   else{
	   	this.calculationAlgorithmConfigurator = config;
	   }
	}

	public void setName(String val) {
	   if(val != null && !val.trim().equals("")) this.name = val; 	   
   }


	public Set<Class<?>> getRequiredClasses() {	  
	   return ObjectManipulations.cloneObject(requiredClasses);
   }


	public void setRequiredClasses(Set<Class<?>> requiredClasses) {
	   this.requiredClasses = requiredClasses;  
   }

}
