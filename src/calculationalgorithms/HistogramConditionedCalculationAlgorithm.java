package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;

public class HistogramConditionedCalculationAlgorithm extends HistogramCalculationAlgorithm{
	
	
	public  HistogramConditionedCalculationAlgorithm(){
		super();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates a histogram on the basis of the defined mathematical expression for all cells which fulfil the defined condition. binSize = (maxValue-minValue)/numberOfBins";
         }

			public int getID() { return _id; }

			public String getName() { return "Histogram Conditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.HISTOGRAMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return true; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				
				
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
	      ExceptionDisplayer.getInstance().displayException(e);
	      return false;
      }
	}	
}
