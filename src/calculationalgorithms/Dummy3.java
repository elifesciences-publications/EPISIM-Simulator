package calculationalgorithms;

import java.util.HashMap;
import java.util.Map;

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;


public class Dummy3 implements CalculationAlgorithm{
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This is our second Dummy Algorithm. It's designed solely for testing purpose. \n Dummy Dummy Dummy, jummy, jummy jummy";
         }

			public int getID() { return _id; }

			public String getName() { return "Dummy 2"; }

			public CalculationAlgorithmType getType() { return CalculationAlgorithmType.TWODIMRESULT; }

			public boolean hasCondition() { return false; }
			
			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
				params.put("Beispiel 1", Integer.TYPE);
	         params.put("Beispiel 2", Boolean.TYPE);
	        
	         return params;
         }
	   };
	}
}
