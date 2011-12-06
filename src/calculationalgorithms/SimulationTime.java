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


public class SimulationTime extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	public  SimulationTime(){
		observers = new HashMap<Long, TissueObserver>();
	}
	
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm provides the simulation time (simstep number * time scaling factor).";
         }

			public int getID() { return _id; }

			public String getName() { return "Simulation Time"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return false; }
			
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				params.put(CalculationAlgorithm.SIMSTEPTIMESCALINGFACTOR, Double.TYPE);
						        
	         return params;
         }
	   };
	}

	public void reset(){
		observers.clear();		
   }

	public void restartSimulation(){ 
		
	}

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {		
			double scalingFactor = (Double) handler.getParameters().get(CalculationAlgorithm.SIMSTEPTIMESCALINGFACTOR);			
			results.add1DValue(((double)SimStateServer.getInstance().getSimStepNumber())*scalingFactor);
	}
}