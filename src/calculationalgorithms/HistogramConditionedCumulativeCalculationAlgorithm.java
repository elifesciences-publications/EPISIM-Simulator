package calculationalgorithms;

import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
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
	         return "This algorithms calculates a histogram based on the defined mathematical expression for all cells which fulfil the defined condition. The results are summed up over all calculation cycles. binSize = (maxValue-minValue)/numberOfBins";
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
				
		try{
	      return !Double.isNaN(result) && handler.conditionFulfilled(cell);
      }
      catch (CellNotValidException e){
	      EpisimExceptionHandler.getInstance().displayException(e);
	      return false;
      }
	}	
}