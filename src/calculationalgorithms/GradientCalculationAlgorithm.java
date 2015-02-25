package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.ResultSet;
import sim.app.episim.util.Sorting;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;

public class GradientCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, TissueObserverAlgorithm{
	public static final String GRADIENT_MIN_X_PARAMETER = "min X coord.";
	public static final String GRADIENT_MAX_X_PARAMETER = "max X coord.";
		private Map<Long, TissueObserver> observers;
		public  GradientCalculationAlgorithm(){
			observers = new HashMap<Long, TissueObserver>();
		}
		
		
		public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
			final int _id = id;
		   
		   return new CalculationAlgorithmDescriptor(){

				public String getDescription() {	         
		         return "This algorithm calculates a very simple gradient over the given X-Interval of the tissue using the defined mathematical expression.";
	         }

				public int getID() { return _id; }

				public String getName() { return "Gradient Calculator"; }

				public CalculationAlgorithmType getType() { return CalculationAlgorithmType.TWODIMDATASERIESRESULT; }

				public boolean hasCondition() { return false; }
				
				public boolean hasMathematicalExpression() { return true; }
				
				public Map<String, Class<?>> getParameters() {
					Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
					params.put(GradientCalculationAlgorithm.GRADIENT_MIN_X_PARAMETER, Double.TYPE);
					params.put(GradientCalculationAlgorithm.GRADIENT_MAX_X_PARAMETER, Double.TYPE);
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
				
				for(AbstractCell actCell: allCells){
					if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){
						EpisimBiomechanicalModel biomech = actCell.getEpisimBioMechanicalModelObject();
						double min = (Double) handler.getParameters().get(GRADIENT_MIN_X_PARAMETER);
						double max = (Double) handler.getParameters().get(GRADIENT_MAX_X_PARAMETER);
						if(max < min){
							double tmp = max;
							max = min;
							min = tmp;
						}
						if(biomech.getX() >= min
								&& biomech.getX() <= max){						
								
								resultMap.put(biomech.getY(), handler.calculate(actCell));
						}
					}
				}
			   Sorting.sort2DMapValuesIntoResultSet(resultMap, results);							
			}
			catch(CellNotValidException ex){
				EpisimExceptionHandler.getInstance().displayException(ex);
			}		   
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
		
		private double getGradientMinX(){
			return 30;
		}
		private double getGradientMaxX(){
			return 40;
		}
		private double getGradientMinY(){
			return 0;
		}
		private double getGradientMaxY(){
			return TissueController.getInstance().getTissueBorder().getHeightInMikron();
		}
		
	}