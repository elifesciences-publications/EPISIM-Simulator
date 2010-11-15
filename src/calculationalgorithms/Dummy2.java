package calculationalgorithms;

import java.util.HashMap;
import java.util.Map;

import sim.app.episim.AbstractCellType;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.ResultSet;

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;


public class Dummy2{
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This is our second Dummy Algorithm. It's designed solely for testing purpose. \n Dummy Dummy Dummy, jummy, jummy jummy";
         }

			public int getID() { return _id; }

			public String getName() { return "Dummy 25"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.ONEDIMDATASERIESRESULT; }

			public boolean hasCondition() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
					        
	         return params;
         }
	   };
	}

	public void registerCells(GenericBag<AbstractCellType> allCells) {

	   
	   
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {

	   
	   
   }

	public void reset() {

	   
	   
   }

	public void restartSimulation() {

	   
	   
   }

}
