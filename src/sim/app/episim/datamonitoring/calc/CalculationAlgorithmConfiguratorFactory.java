package sim.app.episim.datamonitoring.calc;

import java.util.Map;

import sim.app.episim.util.ObjectManipulations;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;


public abstract class CalculationAlgorithmConfiguratorFactory implements java.io.Serializable{
	
	public static CalculationAlgorithmConfigurator createCalculationAlgorithmConfiguratorObject(final int _algorithmId, final String[] _arithmeticExp, final String[] _booleanExp, final Map<String, Object> _parameters){
		return new  CalculationAlgorithmConfigurator(){
			private String[] arithmeticExp = ObjectManipulations.cloneObject(_arithmeticExp);
			private String[] booleanExp = ObjectManipulations.cloneObject(_booleanExp);
			private int algorithmId = _algorithmId;
			private Map<String, Object> parameters = ObjectManipulations.cloneObject(_parameters);
			public String[] getArithmeticExpression() { return arithmeticExp; }
			public String[] getBooleanExpression() { return  booleanExp; }
			public int getCalculationAlgorithmID() { return algorithmId; }
			public Map<String, Object> getParameters() { return  parameters; }
	};
	}
}
