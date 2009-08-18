package calculationalgorithms;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import sim.app.episim.CellType;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.ResultSet;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class HistogramCalculator implements CalculationAlgorithm{
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This algorithms calculates a histogram on the basis of the defined mathematical expression for all cells.";
         }

			public int getID() { return _id; }

			public String getName() { return "Unconditioned Histogram"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.HISTOGRAMRESULT; }

			public boolean hasCondition() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new LinkedHashMap<String, Class<?>>();
				
				
				params.put(CalculationAlgorithm.HISTOGRAMMINVALUEPARAMETER, Double.TYPE);
				params.put(CalculationAlgorithm.HISTOGRAMMAXVALUEPARAMETER, Double.TYPE);
				params.put(CalculationAlgorithm.HISTOGRAMNUMBEROFBINSPARAMETER, Integer.TYPE);
				
	         
	        
	         return params;
         }
	   };
	}

	public void registerCells(GenericBag<CellType> allCells) {

	   // TODO Auto-generated method stub
	   
   }

	public void reset() {

	   // TODO Auto-generated method stub
	   
   }

	public void restartSimulation() {

	   // TODO Auto-generated method stub
	   
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {

	   // TODO Auto-generated method stub
	   
   }
}
