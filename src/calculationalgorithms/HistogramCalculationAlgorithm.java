package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.ResultSet;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.SingleCellObserver;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;


public class HistogramCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, TissueObserverAlgorithm{
	
	public static final String HISTOGRAMMINVALUEPARAMETER = "min value";
	public static final String HISTOGRAMMAXVALUEPARAMETER = "max value";
	public static final String HISTOGRAMNUMBEROFBINSPARAMETER = "number of bins";
	
	private Map<Long, TissueObserver> observers;
	public  HistogramCalculationAlgorithm(){
		observers = new HashMap<Long, TissueObserver>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm calculates a histogram on the basis of the defined mathematical expression for all cells. Only results within the specified interval [min value, max value] are included.";
         }

			public int getID() { return _id; }

			public String getName() { return "Histogram Unconditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.HISTOGRAMRESULT; }

			public boolean hasCondition() { return false; }
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

	public void reset() {

		observers.clear();
	   
   }

	public void restartSimulation() {

	   // do nothing
	   
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		try{
		
			notifyTissueObserver(handler.getID());
		
			 for(AbstractCell cell: allCells){ 
				 if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(cell.getClass())){
					 double result = handler.calculate(cell);
					 if(checkCondition(result, handler, cell)) results.add1DValue(result);
				 }
				 
			 }
			
		}
		catch(CellNotValidException ex){
			ExceptionDisplayer.getInstance().displayException(ex);
		}
	   
   }
	
	protected boolean checkCondition(double result, CalculationHandler handler, AbstractCell cell){
		double min = (Double) handler.getParameters().get(HISTOGRAMMINVALUEPARAMETER);
		double max = (Double) handler.getParameters().get(HISTOGRAMMAXVALUEPARAMETER);
		
		return result >= min && result <= max;
	}

	private void notifyTissueObserver(long id){
		if(this.observers.containsKey(id)){
			this.observers.get(id).observedTissueHasChanged();
		}
	}
	
	public void addTissueObserver(long[] calculationHandlerIds, TissueObserver observer) {
		if(calculationHandlerIds != null && calculationHandlerIds.length >0){
			for(long id : calculationHandlerIds){
				this.observers.put(id, observer);
			}
		}
   }
	
}
