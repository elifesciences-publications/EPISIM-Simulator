package sim.app.episim.datamonitoring.calc;

import sim.app.episim.util.ObjectManipulations;
import episiminterfaces.calc.CellColoringConfigurator;


public class CellColoringConfigurationFactory implements java.io.Serializable{
	
	public static CellColoringConfigurator createCellColoringConfiguratorObject(final String[] _arithmeticExpColorR, final String[] _arithmeticExpColorG, final String[] _arithmeticExpColorB){
		return new  CellColoringConfigurator(){			
			private String[] arithmeticExpColorR = ObjectManipulations.cloneObject(_arithmeticExpColorR);
			private String[] arithmeticExpColorG = ObjectManipulations.cloneObject(_arithmeticExpColorG);
			private String[] arithmeticExpColorB = ObjectManipulations.cloneObject(_arithmeticExpColorB);			
			
			public String[] getArithmeticExpressionColorR() { return arithmeticExpColorR; }
			public String[] getArithmeticExpressionColorG() { return arithmeticExpColorG; }
			public String[] getArithmeticExpressionColorB() { return arithmeticExpColorB; }
		};
	}
}