package calculationalgorithms;

import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;


public class Dummy3 implements CalculationAlgorithm{
	
	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {
		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {	         
	         return "This is our second Dummy Algorithm. It's designed solely for testing purpose. \n Dummy Dummy Dummy, jummy, jummy jummy";
         }

			public long getID() { return _id; }

			public String getName() { return "Dummy 2"; }

			public int getType() { return CalculationAlgorithm.TWODIMRESULT; }

			public boolean hasCondition() { return false; }	   	
	   };
	}
}
