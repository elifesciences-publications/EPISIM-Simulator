package sim.app.episim.model.biomechanics.hexagonbased.singlesurface.tumor.simple;

import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelGP;


public class HexagonBasedMechanicalModelSimpleTumorGP extends HexagonBasedMechanicalModelGP {
	
	private int initialSecretionCellDensityInPercent = 100;
	private double number_of_initially_occupied_columns =8;
	
	public double getNumber_of_initially_occupied_columns() {

	   return number_of_initially_occupied_columns;
   }   
   
   public void setNumber_of_initially_occupied_columns(double number_of_initially_occupied_columns) {

	   this.number_of_initially_occupied_columns = number_of_initially_occupied_columns;
   }
   
   public int getInitialSecretionCellDensityInPercent() {
      
   	return initialSecretionCellDensityInPercent;
   }
	
   public void setInitialSecretionCellDensityInPercent(int initialSecretionCellDensityInPercent) {
   
   	this.initialSecretionCellDensityInPercent = initialSecretionCellDensityInPercent;
   }
}
