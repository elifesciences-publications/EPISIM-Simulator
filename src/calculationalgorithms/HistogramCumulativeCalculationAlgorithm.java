package calculationalgorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import sim.app.episim.EpisimExceptionHandler;
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


public class HistogramCumulativeCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, TissueObserverAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	
	private Map<Long, Integer> calculationNumberMap;
	private Map<Long, SimpleHistogramDataset> datasetMap;
	private Map<Long, SimpleHistogramBin[]> binMap;
	private Map<Long, Set<Long>> alreadyCountedCellIds;
	public  HistogramCumulativeCalculationAlgorithm(){
		observers = new HashMap<Long, TissueObserver>();
		calculationNumberMap = new HashMap<Long, Integer>();
		datasetMap = new HashMap<Long, SimpleHistogramDataset>();
		binMap = new HashMap<Long, SimpleHistogramBin[]>();
		alreadyCountedCellIds = new HashMap<Long, Set<Long>>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates a histogram based on the defined mathematical expression for all cells. The results are summed up over all calculation cycles. binSize = (maxValue-minValue)/numberOfBins";
         }

			public int getID() { return _id; }

			public String getName() { return "Histogram Unconditioned and Cumulative"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.HISTOGRAMRESULT; }

			public boolean hasCondition() { return false; }
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

	public void reset() {

	   this.observers.clear();
	   restartSimulation();
   }

	public void restartSimulation() {

	  this.calculationNumberMap.clear();
	  this.datasetMap.clear();
	  this.binMap.clear();
	  this.alreadyCountedCellIds.clear();
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results){
		boolean countCellOnce = (Boolean)handler.getParameters().get(HistogramCalculationAlgorithm.HISTOGRAM_COUNT_CELL_ONCE_PARAMETER);
		try{		
			
			notifyTissueObserver(handler.getID());
			if(!this.alreadyCountedCellIds.containsKey(handler.getID())) this.alreadyCountedCellIds.put(handler.getID(), new HashSet<Long>());
			if(this.calculationNumberMap.containsKey(handler.getID())) this.calculationNumberMap.put(handler.getID(), (this.calculationNumberMap.get(handler.getID()).intValue()+1));
			else this.calculationNumberMap.put(handler.getID(), 1);
			
			if(!this.datasetMap.containsKey(handler.getID())) buildHistogramDataset(handler);
			
			for(AbstractCell cell: allCells){ 
				 if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(cell.getClass())){
					 if((countCellOnce && !this.alreadyCountedCellIds.get(handler.getID()).contains(cell.getID())) ||!countCellOnce){
						 double result = handler.calculate(cell);
						 if(checkCondition(result, handler, cell)){ 
							 this.datasetMap.get(handler.getID()).addObservation(result);
							 this.alreadyCountedCellIds.get(handler.getID()).add(cell.getID());
						 }
					 }
				 }				 
			}			
			calculateCumulativeResults(handler, results);			
		}
		catch(CellNotValidException ex){
			EpisimExceptionHandler.getInstance().displayException(ex);
		}
	   
   }
	
	
	private void calculateCumulativeResults(CalculationHandler handler, ResultSet<Double> results){
		for(SimpleHistogramBin bin : this.binMap.get(handler.getID())){
			int noOfResultsToRecalculate = bin.getItemCount();
			double virtualResult = (bin.getUpperBound()+bin.getLowerBound()) / 2;
			for(int i = 0; i < noOfResultsToRecalculate; i++){
				results.add1DValue(virtualResult);
			}
		}
	}
	
	
	
	private void buildHistogramDataset(CalculationHandler handler){
		double min = (Double) handler.getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER);
		double max = (Double) handler.getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER);
		int noBins = (Integer) handler.getParameters().get(HistogramCalculationAlgorithm.HISTOGRAMNUMBEROFBINSPARAMETER);
		
		SimpleHistogramDataset dataset = new SimpleHistogramDataset(""+handler.getID());
		dataset.setAdjustForBinSize(false);
		
		this.binMap.put(handler.getID(), buildBins(min, max, noBins));
		
		for(SimpleHistogramBin bin: this.binMap.get(handler.getID())) dataset.addBin(bin);
		
		this.datasetMap.put(handler.getID(), dataset);
		
	}
	
	protected boolean checkCondition(double result, CalculationHandler handler, AbstractCell cell){
		
		return !Double.isNaN(result);
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
		     double binSize = (Math.abs(maxValue - minValue)) / ((double)numberOfBins);
		    
		     SimpleHistogramBin[]  bins = new SimpleHistogramBin[numberOfBins+2];
		     bins[0] = new SimpleHistogramBin(Double.NEGATIVE_INFINITY, minValue, true, false);
		     for(int i = 0; i < numberOfBins; i ++){
		       if(i< (numberOfBins-1))bins[i+1] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, false);
		       else bins[i+1] = new SimpleHistogramBin((minValue + i*binSize), (minValue + (i+1)*binSize), true, true);
		     }
		     bins[numberOfBins+1] = new SimpleHistogramBin(maxValue, Double.POSITIVE_INFINITY, false, true);
		     return bins;
	   }
	
}
