package sim.app.episim.model.biomechanics.hexagonbased3d;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;



public class HexagonBased3DMechanicalModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {

	@Override
   public double getNeighborhood_mikron() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public void setNeighborhood_mikron(double val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public int getBasalOpening_mikron() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public void setBasalOpening_mikron(int val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public int getBasalAmplitude_mikron() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public void setBasalAmplitude_mikron(int val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public void setWidthInMikron(double val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public double getWidthInMikron() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public void setHeightInMikron(double val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public double getHeightInMikron() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public void setLengthInMikron(double val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public double getLengthInMikron() {

	   // TODO Auto-generated method stub
	   return 0;
   }

	@Override
   public ModelDimensionality getModelDimensionality(){

	 
	   return  ModelDimensionality.THREE_DIMENSIONAL;
   }

	@Override
   public boolean areDiffusionFieldsContinousInXDirection() {

	   // TODO Auto-generated method stub
	   return false;
   }

	@Override
   public boolean areDiffusionFieldsContinousInYDirection() {

	   // TODO Auto-generated method stub
	   return false;
   }

	@Override
   public boolean areDiffusionFieldsContinousInZDirection() {

	   // TODO Auto-generated method stub
	   return false;
   }

	@Override
   public void setNumberOfPixelsPerMicrometer(double val) {

	   // TODO Auto-generated method stub
	   
   }

	@Override
   public double getNumberOfPixelsPerMicrometer() {

	   // TODO Auto-generated method stub
	   return 0;
   }

}
