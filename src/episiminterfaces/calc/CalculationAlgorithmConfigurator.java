package episiminterfaces.calc;

import java.util.Map;


public interface CalculationAlgorithmConfigurator {

	String[] getBooleanExpression();
	String[] getArithmeticExpression();
	Map<String, Object> getParameters();	
	
}
