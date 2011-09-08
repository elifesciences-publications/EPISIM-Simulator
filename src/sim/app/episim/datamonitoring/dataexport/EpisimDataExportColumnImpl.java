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
	private transient Set<Class<?>> requiredClasses;
	private Set<String> requiredClassesNameSet;
	
	public EpisimDataExportColumnImpl(long id){
		this.id = id;
		requiredClasses = new HashSet<Class<?>>();
		requiredClassesNameSet = new HashSet<String>();
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
		if(this.requiredClasses == null) this.requiredClasses = new HashSet<Class<?>>();
	   return ObjectManipulations.cloneObject(requiredClasses);
   }
	
	public Set<String> getRequiredClassesNameSet() {		  
	   return ObjectManipulations.cloneObject(requiredClassesNameSet);
   }


	public void setRequiredClasses(Set<Class<?>> classes) {
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

}
