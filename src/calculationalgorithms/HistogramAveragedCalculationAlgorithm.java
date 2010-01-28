package calculationalgorithms;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;

import sim.app.episim.CellType;
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


public class HistogramAveragedCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, TissueObserverAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	
	private Map<Long, Integer> calculationNumberMap;
	private Map<Long, SimpleHistogramDataset> datasetMap;
	private Map<Long, SimpleHistogramBin[]> binMap;
	
	public  HistogramAveragedCalculationAlgorithm(){
		observers = new HashMap<Long, TissueObserver>();
		calculationNumberMap = new HashMap<Long, Integer>();
		datasetMap = new HashMap<Long, SimpleHistogramDataset>();
		binMap = new HashMap<Long, SimpleHistogramBin[]>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates a histogram on the basis of the defined mathematical expression for all cells. Only results within the specified interval [min value, max value] are included. The results are averaged over all calculation cycles.";
         }

			public int getID() { return _id; }

			public String getName() { return "Histogram Unconditioned and Averaged"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.HISTOGRAMRESULT; }

			public boolean hasCondition() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				
				
				params.put(CalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER, Double.TYPE);
				params.put(CalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER, Double.TYPE);
				params.put(CalculationAlgorithm.HISTOGRAMNUMBEROFBINSPARAMETER, Integer.TYPE);
				
	         
	        
	         return params;
         }
	   };
	}

	public void reset() {

	   this.observers.clear();
	   restartSimulation();
   }

	public void restartSimulation() {

	  this.calculationNumberMap.clear();
	  this.datasetMap.clear();
	  this.binMap.clear(); 
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		try{
		
			notifyTissueObserver(handler.getID());
			
			if(this.calculationNumberMap.containsKey(handler.getID())) this.calculationNumberMap.put(handler.getID(), (this.calculationNumberMap.get(handler.getID()).intValue()+1));
			else this.calculationNumberMap.put(handler.getID(), 1);			 
			
			if(!this.datasetMap.containsKey(handler.getID())) buildHistogramDataset(handler);
			
			for(CellType cell: allCells){ 
				 if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(cell.getClass())){
					 double result = handler.calculate(cell);
					 if(checkCondition(result, handler, cell)) this.datasetMap.get(handler.getID()).addObservation(result);
				 }
				 
			 }
			
			
			calculateAveragedResults(handler, results);
			
		}
		catch(CellNotValidException ex){
			ExceptionDisplayer.getInstance().displayException(ex);
		}
	   
   }
	
	
	private void calculateAveragedResults(CalculationHandler handler, ResultSet<Double> results){
		for(SimpleHistogramBin bin : this.binMap.get(handler.getID())){
			int noOfResultsToRecalculate = Math.round(bin.getItemCount() / this.calculationNumberMap.get(handler.getID()).intValue());
			double virtualResult = (bin.getUpperBound()+bin.getLowerBound()) / 2;
			for(int i = 0; i < noOfResultsToRecalculate; i++){
				results.add1DValue(virtualResult);
			}
		}
	}
	
	
	
	private void buildHistogramDataset(CalculationHandler handler){
		double min = (Double) handler.getParameters().get(HISTOGRAMMINVALUEPARAMETER);
		double max = (Double) handler.getParameters().get(HISTOGRAMMAXVALUEPARAMETER);
		int noBins = (Integer) handler.getParameters().get(HISTOGRAMNUMBEROFBINSPARAMETER);
		
		SimpleHistogramDataset dataset = new SimpleHistogramDataset(""+handler.getID());
		dataset.setAdjustForBinSize(false);
		
		this.binMap.put(handler.getID(), buildBins(min, max, noBins));
		
		for(SimpleHistogramBin bin: this.binMap.get(handler.getID())) dataset.addBin(bin);
		
		this.datasetMap.put(handler.getID(), dataset);
		
	}
	
	protected boolean checkCondition(double result, CalculationHandler handler, CellType cell){
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
	
	private SimpleHistogramBin[] buildBins(double minValue, double maxValue, int numberOfBins){
	     if(minValue > maxValue){
	       double tmp = minValue;
	       minValue = maxValue;
	       maxValue = tmp;
	     }	
	     if(minValue == maxValue)maxValue = (minValue + 1);
	     if(numberOfBins < 0)numberOfBins = Math.abs(numberOfBins);
	     if(numberOfBins == 0)numberOfBins = 1;
	     double binSize = Math.abs(maxValue - minValue) / numberOfBins;
	     SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins];				
	     for(int i = 0; i < numberOfBins; i ++){
	       if(i == 0) bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, true);
	       else bins[i] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), false, true);
	     }		
	     return bins;
	   }
	
}