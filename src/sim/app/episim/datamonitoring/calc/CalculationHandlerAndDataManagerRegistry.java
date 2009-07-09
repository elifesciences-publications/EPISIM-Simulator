package sim.app.episim.datamonitoring.calc;

import episiminterfaces.calc.CalculationCallBack;
import episiminterfaces.calc.CalculationHandler;


public class CalculationHandlerAndDataManagerRegistry {
		
	private static CalculationHandlerAndDataManagerRegistry instance = new CalculationHandlerAndDataManagerRegistry();	
	private CalculationHandlerAndDataManagerRegistry(){}	
	protected static CalculationHandlerAndDataManagerRegistry getInstance(){ return instance;}
	
	public CalculationCallBack registerCalculationHanderAndDataManager(CalculationHandler handler, CalculationDataManager manager){
		
		return null;
	}
	
	

}
