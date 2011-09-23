package sim.app.episim.model.biomechanics.hexagonbased;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class HexagonBasedMechanicalModelGlobalParameters implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private double height = 5000;
	private double width = 5000;
	private double cellDiameter_mikron = 50;
	private double neighborhood_mikron = 50;
	private double numberOfPixelsPerMicrometer = 0.1;

	public double getNeighborhood_mikron() {
	   return this.neighborhood_mikron;
   }

	public void setNeighborhood_mikron(double val) {
	   this.neighborhood_mikron = val;	   
   }

	public int getBasalOpening_mikron() {
		//not needed in first version
	   return 0;
   }
	public void setBasalOpening_mikron(int val) {
		//not needed in first version
   }

	public int getBasalAmplitude_mikron() {
		//not needed in first version
	   return 0;
   }
	public void setBasalAmplitude_mikron(int val) {
		//not needed in first version
   }

	public void setWidthInMikron(double val) {
		this.width = val;
   }

	public double getWidthInMikron() {
		return this.width;
   }

	public void setHeightInMikron(double val) {
		this.height = val;
   }

	public double getHeightInMikron(){
		return this.height;
   }	
   public double getCellDiameter_mikron() {   
   	return cellDiameter_mikron;
   }	
   public void setCellDiameter_mikron(double cellDiameter_mikron) {   
   	this.cellDiameter_mikron = cellDiameter_mikron;
   }

	public void setNumberOfPixelsPerMicrometer(double val) {
	   this.numberOfPixelsPerMicrometer	= val;   
   }

	public double getNumberOfPixelsPerMicrometer() {
	   return this.numberOfPixelsPerMicrometer;
   }

}
