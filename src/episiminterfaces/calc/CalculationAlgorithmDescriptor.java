package episiminterfaces.calc;

import java.util.Map;

import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public interface CalculationAlgorithmDescriptor extends java.io.Serializable{
	
	int getID();
	CalculationAlgorithmType getType();
	String getName();
	String getDescription();
	Map<String, Class<?>> getParameters();
	boolean hasCondition();
	

}
