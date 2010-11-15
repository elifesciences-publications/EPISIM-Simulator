package calculationalgorithms;

import java.util.HashMap;
import java.util.Map;

import sim.app.episim.AbstractCellType;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.ResultSet;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;


public class Dummy1{	

	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {
				
	         return "This is our first Dummy Algorithm. It's designed solely for testing purpose. \n Dummy Dummy Dummy, jummy, jummy jummy";
         }

			public int getID() { return _id; }

			public String getName() { return "Example Algorithm 1"; }

			public CalculationAlgorithmType getType(){ return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return true; }

			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
	         params.put("Beispiel 1", Boolean.TYPE);
	         params.put("Beispiel 2", Integer.TYPE);
	         params.put("Beispiel 3", Double.TYPE);
	         
	         return params;
         }
			
			
	   	
	   };
	
   }

	public void registerCells(GenericBag<AbstractCellType> allCells) {

	  
	   
   }

	
	public void reset() {

	  
	   
   }

	public void restartSimulation() {

	   
	   
   }

	public void calculate(CalculationHandler handler, ResultSet<Double> results) {

	  
	   
   }

	
}
