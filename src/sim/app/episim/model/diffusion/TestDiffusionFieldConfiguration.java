package sim.app.episim.model.diffusion;

import episiminterfaces.EpisimDiffusionFieldConfiguration;


public class TestDiffusionFieldConfiguration implements EpisimDiffusionFieldConfiguration{
	
	public String getDiffusionFieldName() {	     
      return "TestDiffusionField";
   }
	
   public double getDiffusionCoefficient() {

      return 0.00000000000002;
   }
	
   public double getLatticeSiteSizeInMikron() {
      return 1;
   }
	
   public double getDegradationRate() {
      return 0.0;
   }
   
   public int getNumberOfIterationsPerCBMSimStep() {
      return 1;
   }
	
   public double getDeltaTimeInSecondsPerIteration() {	      
      return 1;
   }
	
   public double getMaximumConcentration() {	     
      return 255;
   }
	
   public double getMinimumConcentration() {	      
      return 0;
   }

	public double getDefaultConcentration() {

		return 0;
	}

}
