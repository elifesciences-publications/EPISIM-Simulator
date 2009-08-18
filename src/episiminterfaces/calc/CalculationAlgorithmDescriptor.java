package episiminterfaces.calc;

import java.util.Map;

import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public interface CalculationAlgorithmDescriptor extends java.io.Serializable{
	
	int getID();
	CalculationAlgorithmType getType();
	String getName();
	String getDescription();
	/*
	 * PLEASE NOTE: If you want the parameters to appear in the order of insertion to the map please use the class LinkedHashMap
	 */
	Map<String, Class<?>> getParameters();
	boolean hasCondition();
	

}
