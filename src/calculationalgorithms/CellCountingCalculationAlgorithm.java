package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.GlobalStatistics;
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


public class CellCountingCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	public  CellCountingCalculationAlgorithm(){
		observers = new HashMap<Long, TissueObserver>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm calculates the number of cells that fulfill a given condition.";
         }

			public int getID() { return _id; }

			public String getName() { return "Cell Counter"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }
			
			public boolean hasMathematicalExpression() { return false; }
			
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

	   // do nothing
	   
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		try{	
			double cellCounter = 0;
			for(AbstractCell actCell: allCells){
				if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){
					if(handler.conditionFulfilled(actCell)) cellCounter++;
				}
			}
			results.add1DValue(cellCounter);
			
		}
		catch(CellNotValidException ex){
			ExceptionDisplayer.getInstance().displayException(ex);
		}		   
   }
	
	
	
}