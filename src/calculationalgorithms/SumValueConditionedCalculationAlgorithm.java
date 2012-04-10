package calculationalgorithms;

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


public class SumValueConditionedCalculationAlgorithm  extends AbstractCommonCalculationAlgorithm implements CalculationAlgorithm{
	
	public SumValueConditionedCalculationAlgorithm(){}
	
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates the sum of the defined mathematical expression over all Cells. Only cells which hold the specified condition are considered.";
         }

			public int getID() { return _id; }

			public String getName() { return "Sum Calculator Conditioned"; }

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
		double result = 0;
		for(AbstractCell actCell : allCells){

			try{
				if(handler.conditionFulfilled(actCell)){
					result = handler.calculate(actCell);
					sum += result;					
					if(sum != 0) results.add1DValue(sum);
					else results.add1DValue(0d);
				}
			}
			catch (CellNotValidException e){
				ExceptionDisplayer.getInstance().displayException(e);
			}
		}
		
		
	   result = 0;
   }	
	
}