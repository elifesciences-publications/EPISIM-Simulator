package sim.app.episim.model.biomechanics.latticebased2Dr.tumor.simple;

import sim.app.episim.model.biomechanics.latticebased2Dr.LatticeBased2DModelGP;


public class LatticeBased2DModelSimpleTumorGP extends LatticeBased2DModelGP {
	
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
