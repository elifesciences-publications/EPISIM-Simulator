package calculationalgorithms;

import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class HistogramConditionedCumulativeCalculationAlgorithm extends HistogramCumulativeCalculationAlgorithm{
	
	
	public  HistogramConditionedCumulativeCalculationAlgorithm(){
		super();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates a histogram based on the defined mathematical expression for all cells which fulfil the defined condition. Only results within the specified interval [min value, max value] are included. The results are summed up over all calculation cycles.";
         }

			public int getID() { return _id; }

			public String getName() { return "Histogram Conditioned and Cumulative"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.HISTOGRAMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return true; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				
				params.put(HistogramCalculationAlgorithm.HISTOGRAM_COUNT_CELL_ONCE_PARAMETER, Boolean.TYPE);
				params.put(HistogramCalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER, Double.TYPE);
				params.put(HistogramCalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER, Double.TYPE);
				params.put(HistogramCalculationAlgorithm.HISTOGRAMNUMBEROFBINSPARAMETER, Integer.TYPE);
				
	         
	        
	         return params;
         }
	   };
	}

	
	protected boolean checkCondition(double result, CalculationHandler handler, AbstractCell cell){
		double min = (Double) handler.getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER);
		double max = (Double) handler.getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER);
		
		try{
	      return result >= min && result <= max && handler.conditionFulfilled(cell);
      }
      catch (CellNotValidException e){
	      ExceptionDisplayer.getInstance().displayException(e);
	      return false;
      }
	}	
}
