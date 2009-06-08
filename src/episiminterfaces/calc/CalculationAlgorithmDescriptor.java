package episiminterfaces.calc;

import java.util.Map;


public interface CalculationAlgorithmDescriptor {
	
	long getID();
	int getType();
	String getName();
	String getDescription();
	Map<String, Class<?>> getParameters();
	boolean hasCondition();
	

}
