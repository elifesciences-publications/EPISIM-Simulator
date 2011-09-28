package episiminterfaces.monitoring;

import java.util.Set;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;



public interface EpisimDataExportColumn extends java.io.Serializable, Cloneable{
	
	long getId();
	String getName();
	Set<Class<?>> getRequiredClasses();
	CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator();	
	
	void setName(String val);
	void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator exp);
	void setRequiredClasses(Set<Class<?>> requiredClasses);
	
	EpisimDataExportColumn clone();
}
