package episiminterfaces.monitoring;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;



public interface EpisimDataExportColumn extends java.io.Serializable{
	
	long getId();
	String getName();
	CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator();	
	
	void setName(String val);
	void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator exp);
}
