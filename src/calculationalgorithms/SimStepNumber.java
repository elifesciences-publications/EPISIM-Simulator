package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateServer;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;


public class SimStepNumber extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	public  SimStepNumber(){
		observers = new HashMap<Long, TissueObserver>();
	}
	
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm provides the sim step number.";
         }

			public int getID() { return _id; }

			public String getName() { return "Simulation Step Number"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return false; }
			
			public boolean hasMathematicalExpression() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
						        
	         return params;
         }
	   };
	}

	public void reset() {

		observers.clear();
		
   }

	public void restartSimulation() {

	   
	
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results){		
			results.add1DValue((double)SimStateServer.getInstance().getSimStepNumber());	
   }
	
	
	
}