package calculationalgorithms;

import java.util.HashMap;
import java.util.Map;

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;


public class Dummy1 implements CalculationAlgorithm{
	
	

	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {
				
	         return "This is our first Dummy Algorithm. It's designed solely for testing purpose. \n Dummy Dummy Dummy, jummy, jummy jummy";
         }

			public int getID() { return _id; }

			public String getName() { return "Dummy 1"; }

			public int getType(){ return CalculationAlgorithm.ONEDIMRESULT; }

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

	
}
