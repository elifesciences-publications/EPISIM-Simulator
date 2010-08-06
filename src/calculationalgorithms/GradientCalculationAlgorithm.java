package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jfree.data.xy.XYSeries;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.util.ResultSet;
import sim.app.episim.util.Sorting;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.TissueObserver;
import episiminterfaces.calc.marker.TissueObserverAlgorithm;

public class GradientCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm, TissueObserverAlgorithm{
		
		private Map<Long, TissueObserver> observers;
		public  GradientCalculationAlgorithm(){
			observers = new HashMap<Long, TissueObserver>();
		}
		
		
		public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
			final int _id = id;
		   
		   return new CalculationAlgorithmDescriptor(){

				public String getDescription() {	         
		         return "This algorithm calculates a very simple gradient over the thickest part of the tissue using the defined mathematical expression.";
	         }

				public int getID() { return _id; }

				public String getName() { return "Gradient Calculator"; }

				public CalculationAlgorithmType getType() { return CalculationAlgorithmType.TWODIMDATASERIESRESULT; }

				public boolean hasCondition() { return false; }
				
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

		   // TODO Auto-generated method stub
		   
	   }

		public void calculate(CalculationHandler handler, ResultSet<Double> results) {
			try{
			
				notifyTissueObserver(handler.getID());
				
				Map<Double, Double> resultMap = new LinkedHashMap<Double, Double>();
				
				for(CellType actCell: allCells){
					if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){
						EpisimCellBehavioralModel cellBehaviour = actCell.getEpisimCellBehavioralModelObject();
						if(cellBehaviour.getX() >= GlobalStatistics.getInstance().getGradientMinX()
								&& cellBehaviour.getX() <= GlobalStatistics.getInstance().getGradientMaxX()
								&& cellBehaviour.getY() >= GlobalStatistics.getInstance().getGradientMinY()
								&& cellBehaviour.getY() <= GlobalStatistics.getInstance().getGradientMaxY()){						
								
								resultMap.put(actCell.getEpisimCellBehavioralModelObject().getY(), handler.calculate(actCell));						
						}
					}
				}
			   Sorting.sort2DMapValuesIntoResultSet(resultMap, results);							
			}
			catch(CellNotValidException ex){
				ExceptionDisplayer.getInstance().displayException(ex);
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
		
	}