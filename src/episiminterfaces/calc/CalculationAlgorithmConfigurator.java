package episiminterfaces.calc;

import java.util.Map;


public interface CalculationAlgorithmConfigurator {
	int getCalculationAlgorithmID();
	String[] getBooleanExpression();
	String[] getArithmeticExpression();
	Map<String, Object> getParameters();	
	
}
