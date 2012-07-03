package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.SimStateServer;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;


public class ComputationTimePerSimStep extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	public static final String COMPUTATIONTIMESCALINGFACTOR = "computation time scaling factor";
	private Map<Long, TissueObserver> observers;
	
	private Map<Long, Long> timeStampMap;
	public  ComputationTimePerSimStep(){
		observers = new HashMap<Long, TissueObserver>();
		timeStampMap=new HashMap<Long, Long>();
	}
	
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm provides the computation time in milliseconds per simulation step (time in milliseconds * computation time scaling factor).";
         }

			public int getID() { return _id; }

			public String getName() { return "Comp. Time per Sim Step"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return false; }
			
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				params.put(ComputationTimePerSimStep.COMPUTATIONTIMESCALINGFACTOR, Double.TYPE);
						        
	         return params;
         }
	   };
	}

	public void reset(){
		observers.clear();
		timeStampMap.clear();
   }

	public void restartSimulation(){ 
		timeStampMap.clear();
	}
	
	
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {		
			double scalingFactor = (Double) handler.getParameters().get(ComputationTimePerSimStep.COMPUTATIONTIMESCALINGFACTOR);
			long systemTime = System.currentTimeMillis();
			long result = timeStampMap.containsKey(handler.getID()) ? (systemTime - timeStampMap.get(handler.getID())) : 0;
			timeStampMap.put(handler.getID(), systemTime);
			results.add1DValue((double)(result*scalingFactor));
	}
}