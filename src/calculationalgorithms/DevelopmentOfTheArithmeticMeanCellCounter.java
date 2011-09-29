package calculationalgorithms;

import java.util.ArrayList;
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


public class DevelopmentOfTheArithmeticMeanCellCounter extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	private Map<Long, TissueObserver> observers;
	private ArrayList<Double> cellCounts;
	public  DevelopmentOfTheArithmeticMeanCellCounter(){
		observers = new HashMap<Long, TissueObserver>();
		cellCounts = new ArrayList<Double>();
	}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithm calculates the development arithmetic mean of the number of cells that fulfill a given condition.\n"
	               +"mean_n=(cell_count_1 + ...  + cell_count_n)/n";
         }

			public int getID() { return _id; }

			public String getName() { return "Development of the Arithmetic Mean Cell Counter"; }

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
		cellCounts.clear();
	   
   }

	public void restartSimulation() {
		cellCounts.clear();	   
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		try{
			double cellCounter = 0;
			for(AbstractCell actCell: allCells){
				if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){
					if(handler.conditionFulfilled(actCell)) cellCounter++;
				}
			}
			cellCounts.add(cellCounter);
			double sum = 0;
			for(Double d : cellCounts) sum += d.doubleValue();
			results.add1DValue((sum/((double)cellCounts.size())));			
		}
		catch(CellNotValidException ex){
			ExceptionDisplayer.getInstance().displayException(ex);
		}		   
   }
	
	
	
}