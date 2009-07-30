package sim.app.episim.datamonitoring.calc;

import java.util.HashMap;
import java.util.Map;
import sim.app.episim.util.ResultSet;
import episiminterfaces.calc.CalculationCallBack;
import episiminterfaces.calc.CalculationHandler;

public class CalculationHandlerAndDataManagerRegistry implements java.io.Serializable{
	
	private transient Map<Long, CalculationHandler> calculationHandlerRegistry;
	private transient Map<Long, CalculationDataManager<Double, Double>> dataManagerRegistry;
	private transient Map<Long, ResultSet<? extends Number>> baselineResultTempRegistry;
	
		
	private static CalculationHandlerAndDataManagerRegistry instance = new CalculationHandlerAndDataManagerRegistry();	
	private CalculationHandlerAndDataManagerRegistry(){		
		calculationHandlerRegistry = new HashMap<Long, CalculationHandler>();
		dataManagerRegistry = new HashMap<Long, CalculationDataManager<Double, Double>>();
		baselineResultTempRegistry = new HashMap<Long, ResultSet<? extends Number>>();
		
	}	
	protected static CalculationHandlerAndDataManagerRegistry getInstance(){ return instance;}
	
	public CalculationCallBack registerCalculationHanderAndDataManager(CalculationHandler handler, CalculationDataManager<Double, Double> manager){
		if(handler == null || (!handler.isBaselineValue() && manager == null)) throw new IllegalArgumentException("Parameter value null is not allowed!");
		if(handler != null && manager != null && handler.getID() != manager.getID()) throw new IllegalArgumentException("CalculationHandlerID: " + handler.getID() + " and DataManagerID: " + manager.getID() + " don't match. They should be equal.");
		 if(manager != null) CalculationAlgorithmServer.getInstance().registerDataManagerAtCalculationAlgorithm(handler.getCalculationAlgorithmID(), manager);
		 calculationHandlerRegistry.put(handler.getID(), handler);
		 if(manager != null) dataManagerRegistry.put(manager.getID(), manager);
		 
		long managerID = Long.MIN_VALUE; 
		if(manager != null) managerID = manager.getID();
		 
		return buildCalculationCallBack(handler.getID(), managerID);
	}
	
	private CalculationCallBack buildCalculationCallBack(final long handlerID, final long managerID){
		
		return new CalculationCallBack(){

			public void calculate(long simStep) {
	                 calculateValues(simStep, handlerID, managerID);
         }
			
		};
		
	}
	
	private void calculateValues(long timeStep, long handlerID, long managerID){
		 System.out.println("timestep: " + timeStep + " handlerID: " + handlerID + " managerID: " + managerID);
	}
	
	
	
	
	

}
