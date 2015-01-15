package sim.app.episim.model.biomechanics.hexagonbased.demo;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;




public class HexagonBasedMechanicalModelDemoGP extends HexagonBasedMechanicalModelGP {
	private boolean addSecretingCellColony=true;
	
	private double number_of_initially_occupied_columns =8;
	
	public double getNumber_of_initially_occupied_columns() {

	   return number_of_initially_occupied_columns;
   }   
   
   public void setNumber_of_initially_occupied_columns(double number_of_initially_occupied_columns) {

	   this.number_of_initially_occupied_columns = number_of_initially_occupied_columns;
   }
	
	
	public boolean getAddSecretingCellColony(){ return addSecretingCellColony; }	
   public void setAddSecretingCellColony(boolean addSecretingCellColony) {
	   this.addSecretingCellColony = addSecretingCellColony;
   }

}
