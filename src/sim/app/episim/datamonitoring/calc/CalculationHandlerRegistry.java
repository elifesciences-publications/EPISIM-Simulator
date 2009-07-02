package sim.app.episim.datamonitoring.calc;


public class CalculationHandlerRegistry {
	
	private int actID = 1;
	
	private static CalculationHandlerRegistry instance = new CalculationHandlerRegistry();
	
	private CalculationHandlerRegistry(){}
	
	protected static CalculationHandlerRegistry getInstance(){ return instance;}
	
	protected int getNextCalculationHandlerID(){ return actID++;}

}
