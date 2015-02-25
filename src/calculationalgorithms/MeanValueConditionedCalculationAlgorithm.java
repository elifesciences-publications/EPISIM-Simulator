package calculationalgorithms;

import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.util.ResultSet;
import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import episimexceptions.CellNotValidException;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class MeanValueConditionedCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	public MeanValueConditionedCalculationAlgorithm(){}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates the mean result of the defined mathematical expression for all Cells. Only cells which fulfil the specified condition are included in the calculation.";
         }

			public int getID() { return _id; }

			public String getName() { return "Mean Value Calculator Conditioned"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }
			public boolean hasMathematicalExpression() { return true; }
			
			public Map<String, Class<?>> getParameters(){				
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
				if(handler.conditionFulfilled(actCell)){
					result = handler.calculate(actCell);
					sum += result;
					counter++;
					
				}
			}
			catch (CellNotValidException e){
				EpisimExceptionHandler.getInstance().displayException(e);
			}
		}
		if(sum != 0 && counter != 0) results.add1DValue((sum / counter));
		else results.add1DValue(0d);
		
	   result = 0;
   }	
	
}