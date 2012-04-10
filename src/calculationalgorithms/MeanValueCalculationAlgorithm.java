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


public class MeanValueCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	public MeanValueCalculationAlgorithm(){}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates the mean result of the defined mathematical expression for all Cells.";
         }

			public int getID() { return _id; }

			public String getName() { return "Mean Value Calculator"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return false; }
			public boolean hasMathematicalExpression() { return true; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();	
	         
	        
	         return params;
         }
	   };
	}

	public void reset() {}

	public void restartSimulation() {}

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		double sum = 0;
		int counter = 0;
		double result = 0;
		for(AbstractCell actCell : allCells){

			try{
				result = handler.calculate(actCell);
				sum += result;
				counter++;
			}
			catch (CellNotValidException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		
		if(sum != 0 && counter != 0) results.add1DValue((sum / counter));
		else results.add1DValue(0d);
	   result = 0;
   }	
	
}