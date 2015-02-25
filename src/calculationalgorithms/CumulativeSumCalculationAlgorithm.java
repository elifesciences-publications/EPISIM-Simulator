package calculationalgorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.CallMeEverySimStep;


public class CumulativeSumCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, CallMeEverySimStep{
	
	public static final String RESET_AFTER_CALCULATION_CYCLE = "reset after calculation cycle";
	
	
	private HashMap<Long, Double> cumulativeResults = new HashMap<Long, Double>();
	private HashSet<CalculationHandler> calcHandlers = new HashSet<CalculationHandler>();
	
	public CumulativeSumCalculationAlgorithm(){}	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates the sum of the defined mathematical expression over all cells. Only cells which hold the specified condition are considered. The resulting sums are accumulated over all simulation steps. A calculation cycle correspondes to the update frequency of a chart or the data export frequency in simulation steps.";
         }

			public int getID() { return _id; }

			public String getName() { return "Cumulative Sum Calculator"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return true; }
			
			public Map<String, Class<?>> getParameters() {
				
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();	
	         params.put(RESET_AFTER_CALCULATION_CYCLE, Boolean.TYPE);
	         
	        
	         return params;
         }
	   };
	}

	public void reset() {
		cumulativeResults.clear();
		calcHandlers.clear();
	}

	public void restartSimulation() {
		cumulativeResults.clear();
		calcHandlers.clear();
	}
	
	private double getSum(CalculationHandler handler){
		double sum = 0;
		double result = 0;
		for(AbstractCell actCell : allCells){
			result = 0;
			try{
				if(handler.conditionFulfilled(actCell)){
					result = handler.calculate(actCell);
					sum += result;
				}
			}
			catch (CellNotValidException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}		
		return sum;
	}
	public void newSimStep(){ 
		for(CalculationHandler handler : calcHandlers){
			double accumulatedSum = cumulativeResults.get(handler.getID());
			accumulatedSum += getSum(handler);
			cumulativeResults.put(handler.getID(), accumulatedSum);
		}
	}
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {		
		boolean resetAfterCalculationCycle = ((Boolean)handler.getParameters().get(RESET_AFTER_CALCULATION_CYCLE)).booleanValue();
		if(!cumulativeResults.keySet().contains(handler.getID())){
			
			double sum = getSum(handler);			
			if(sum != 0) results.add1DValue(sum);
			else results.add1DValue(0d);
			
			if(resetAfterCalculationCycle) cumulativeResults.put(handler.getID(), 0d);
			else cumulativeResults.put(handler.getID(), sum);
			
			calcHandlers.add(handler);
		}
		else{
			double result = cumulativeResults.get(handler.getID());
			if(result != 0) results.add1DValue(result);
			else results.add1DValue(0d);			
			if(resetAfterCalculationCycle) cumulativeResults.put(handler.getID(), 0d);
		}	   
   }		
}