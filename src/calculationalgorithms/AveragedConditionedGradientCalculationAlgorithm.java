package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.util.ResultSet;
import sim.app.episim.util.Sorting;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;


public class AveragedConditionedGradientCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, TissueObserverAlgorithm{
	public static final String Y_AXIS_BIN_SIZE_IN_MIKRON = "y axis bin size in mikron";
	
		private Map<Long, TissueObserver> observers;
		public  AveragedConditionedGradientCalculationAlgorithm(){
			observers = new HashMap<Long, TissueObserver>();
		}
		
		
		public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
			final int _id = id;
		   
		   return new CalculationAlgorithmDescriptor(){
		   	

				public String getDescription() {	         
		         return "This algorithm calculates a gradient over the discretized y-Axis of the tissue. The y-axis is discretized in bins with the given size in micron. The results of the defined mathematical expression are averaged over all cells within a particular y-axis bin.";
	         }

				public int getID() { return _id; }

				public String getName() { return "Averaged Conditioned Gradient Calculator"; }

				public CalculationAlgorithmType getType() { return CalculationAlgorithmType.TWODIMDATASERIESRESULT; }

				public boolean hasCondition() { return true; }
				
				public boolean hasMathematicalExpression() { return true; }
				
				public Map<String, Class<?>> getParameters() {
					Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
					params.put(AveragedConditionedGradientCalculationAlgorithm.Y_AXIS_BIN_SIZE_IN_MIKRON, Double.TYPE);
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
				
				Map<Double, Double> resultMap = new LinkedHashMap<Double, Double>();
				final double binSizeMikron = (Double) handler.getParameters().get(Y_AXIS_BIN_SIZE_IN_MIKRON);
				final int arraySize = ((int)(TissueController.getInstance().getTissueBorder().getHeightInMikron() / binSizeMikron)+1);
				double[] cummulativeValues = new double[arraySize];
				double[] numberOfCellsInBin = new double[arraySize];
				
				for(AbstractCell actCell: allCells){
					if((handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())) && handler.conditionFulfilled(actCell)){
						EpisimBiomechanicalModel biomech = actCell.getEpisimBioMechanicalModelObject();
						int index = calculateBinIndex(biomech.getY(), binSizeMikron);
						double result = handler.calculate(actCell);
						numberOfCellsInBin[index] += 1;
						cummulativeValues[index] += result;
					}
				}
				
				for(double i = 0; i < arraySize; i++){
					if(numberOfCellsInBin[(int)i] > 0){
						resultMap.put(i*binSizeMikron, cummulativeValues[(int)i] / numberOfCellsInBin[(int)i]);
					}			
				}
			   Sorting.sort2DMapValuesIntoResultSet(resultMap, results);							
			}
			catch(CellNotValidException ex){
				EpisimExceptionHandler.getInstance().displayException(ex);
			}		   
	   }
		private int calculateBinIndex(double cellYPos, double binSizeMikron){
			double index = cellYPos / binSizeMikron;
			return (int) index;
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