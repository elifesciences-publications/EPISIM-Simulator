package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

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


public class CellSetValuesConditionedCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	public  CellSetValuesConditionedCalculationAlgorithm(){
		observers = new HashMap<Long, TissueObserver>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm outputs the result of the mathematical expression for each cell holding the specified condition";
         }

			public int getID() { return _id; }

			public String getName() { return "Cell-Set Values Conditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.MULTIDIMDATASERIESRESULT; }

			public boolean hasCondition() { return true; }
			
			public boolean hasMathematicalExpression() { return true; }
			
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
			Vector<Double> resultingValues = new Vector<Double>();
			
			for(AbstractCell actCell: allCells){
				
				if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){
					if(handler.conditionFulfilled(actCell)) resultingValues.add(handler.calculate(actCell));
				}
			}
			
			results.addMultiDimValue(resultingValues);			
		}
		catch(CellNotValidException ex){
			EpisimExceptionHandler.getInstance().displayException(ex);
		}		   
   }
	
	
	
}