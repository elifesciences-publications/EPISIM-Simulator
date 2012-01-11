package episiminterfaces.calc;

import java.util.Map;


public interface CalculationAlgorithmConfigurator extends java.io.Serializable{
	int getCalculationAlgorithmID();
	String[] getBooleanExpression();
	String[] getArithmeticExpression();
	Map<String, Object> getParameters();	
	boolean isBooleanExpressionOnlyInitiallyChecked();
	
}
