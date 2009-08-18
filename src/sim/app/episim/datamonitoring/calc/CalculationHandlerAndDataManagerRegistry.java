package sim.app.episim.datamonitoring.calc;

import java.util.HashMap;
import java.util.Map;
import sim.app.episim.util.ResultSet;
import episiminterfaces.calc.CalculationCallBack;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;

public class CalculationHandlerAndDataManagerRegistry implements java.io.Serializable{
	
	private transient Map<Long, CalculationHandler> calculationHandlerRegistry;
	private transient Map<Long, CalculationDataManager<Double>> dataManagerRegistry;
	private transient Map<Long, ResultSet<Double>> baselineResultTempRegistry;
	
		
	private static CalculationHandlerAndDataManagerRegistry instance = new CalculationHandlerAndDataManagerRegistry();	
	private CalculationHandlerAndDataManagerRegistry(){		
		calculationHandlerRegistry = new HashMap<Long, CalculationHandler>();
		dataManagerRegistry = new HashMap<Long, CalculationDataManager<Double>>();
		baselineResultTempRegistry = new HashMap<Long, ResultSet<Double>>();
		
	}	
	protected static CalculationHandlerAndDataManagerRegistry getInstance(){ return instance;}
	
	public CalculationCallBack registerCalculationHanderAndDataManager(CalculationHandler handler, CalculationDataManager<Double> manager){
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
		if(calculationHandlerRegistry.containsKey(handlerID)){
			CalculationHandler handler = calculationHandlerRegistry.get(handlerID);
			if(handler.isBaselineValue()){
				if((baselineResultTempRegistry.containsKey(handler.getID()) && baselineResultTempRegistry.get(handler.getID()).getTimeStep() != timeStep)
						|| !baselineResultTempRegistry.containsKey(handler.getID())){	
					
					this.baselineResultTempRegistry.put(handler.getID(), calculate(timeStep, handler));
				}
			}
			else{
				CalculationAlgorithmType type = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(handler.getCalculationAlgorithmID()).getType();
				if(type == CalculationAlgorithmType.ONEDIMDATASERIESRESULT || type == CalculationAlgorithmType.ONEDIMRESULT){
					if((baselineResultTempRegistry.containsKey(handler.getCorrespondingBaselineCalculationHandlerID()) 
							&& baselineResultTempRegistry.get(handler.getCorrespondingBaselineCalculationHandlerID()).getTimeStep() != timeStep)
							|| !baselineResultTempRegistry.containsKey(handler.getCorrespondingBaselineCalculationHandlerID())){
						calculateValues(timeStep, handler.getCorrespondingBaselineCalculationHandlerID(), Long.MIN_VALUE);
					}
					
				}
			}
			
		}
		 System.out.println("timestep: " + timeStep + " handlerID: " + handlerID + " managerID: " + managerID);
	}
	
	private ResultSet<Double> calculate(long timeStep, CalculationHandler handler){
		ResultSet<Double> results = ResultSetManager.createResultSetForCalculationAlgorithm(handler.getCalculationAlgorithmID());
		CalculationAlgorithmServer.getInstance().calculateValues(handler, results);
		results.setTimeStep(timeStep);
		return results;
	}
	
	
	
}
