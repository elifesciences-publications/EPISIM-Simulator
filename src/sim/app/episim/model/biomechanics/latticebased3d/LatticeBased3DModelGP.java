package sim.app.episim.model.biomechanics.latticebased3d;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episiminterfaces.NoUserModification;

public class LatticeBased3DModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {

	
	private double cellDiameterInMikron = 50;
	
	private double numberOfPixelsPerMicrometer = 0.1;
		
	private double number_of_columns =40;
	private double number_of_rows =40;
	private double number_of_layers =40;
	private double number_of_initially_occupied_layers =8;
	
	
	private boolean useContinuousSpace = false;
	private boolean useCellCellInteractionEnergy = true;
	private boolean stickToCellColony = true;
	
	private boolean chemotaxisEnabled = true;	
	private boolean addSecretingCellColony=true;
	
	
	public boolean getAddSecretingCellColony(){ return addSecretingCellColony; }	
   public void setAddSecretingCellColony(boolean addSecretingCellColony) {
	   this.addSecretingCellColony = addSecretingCellColony;
   }
	@NoUserModification
	public double getNeighborhood_mikron() {
	   return getCellDiameterInMikron();
   }
	@NoUserModification
	public void setNeighborhood_mikron(double val) {
	   /*DOES NOTHING*/
   }
	
	public double getCellRadius(){ return (cellDiameterInMikron/2d);}
	
	public int getNumber_of_rows() {

	   return (int) number_of_rows;
   }
   
   public void setNumber_of_rows(int number_of_rows) {
	   this.number_of_rows = number_of_rows;
   }
   
   
   public int getNumber_of_columns() {

	   return (int)number_of_columns;
   }
   
   
   public void setNumber_of_columns(int number_of_columns) {

	   this.number_of_columns = number_of_columns;
   }
   
   public int getNumber_of_layers() {

	   return (int) number_of_layers;
   }
   
   public void setNumber_of_layers(int number_of_layers) {
	   this.number_of_layers = number_of_layers;
   }
   
   public int getNumber_of_initially_occupied_layers() {

	   return (int)number_of_initially_occupied_layers;
   }
   
   public void setNumber_of_initially_occupied_layers(int number_of_initially_occupied_layers) {

	   this.number_of_initially_occupied_layers = number_of_initially_occupied_layers;
   }
   
	@NoUserModification
	@NoExport
	public void setWidthInMikron(double val) {
		if(val > 0){
			this.number_of_columns = (int) (val/(getCellDiameterInMikron()));
		}
   }
	
	@NoUserModification
	@NoExport
	public double getWidthInMikron() {
		return (this.number_of_columns*getCellDiameterInMikron());
   }
	
	@NoExport
	public void setHeightInMikron(double val) {
		if(val > 0){
			this.number_of_rows = (int)(val/(getCellDiameterInMikron()));
		}
   }
			
	@NoUserModification
	@NoExport
	public double getHeightInMikron(){
		return (this.number_of_rows*getCellDiameterInMikron());
   }
	
	@NoUserModification
	public double getLengthInMikron() {
		return (this.number_of_layers*getCellDiameterInMikron());
	}
	
	@NoUserModification
	public void setLengthInMikron(double val) {
		if(val > 0){
	   	this.number_of_layers = (int)(val/(getCellDiameterInMikron()));
		}
	}	
	
	public void setNumberOfPixelsPerMicrometer(double val) {
	   this.numberOfPixelsPerMicrometer	= val;   
   }
	
	public boolean getUseContinuousSpace(){
		return this.useContinuousSpace;
	}
	
	public void setUseContinuousSpace(boolean useContinuousSpace){
		this.useContinuousSpace=useContinuousSpace;
	}
	
	public boolean getStickToCellColony(){ return this.stickToCellColony;}
	
	public void setStickToCellColony(boolean val){ this.stickToCellColony=val;}
	
	public boolean getUseCellCellInteractionEnergy(){
		return this.useCellCellInteractionEnergy;
	}
	
	public void setUseCellCellInteractionEnergy(boolean useCellCellInteractionEnergy){
		this.useCellCellInteractionEnergy = useCellCellInteractionEnergy;
	}
	
	@NoUserModification
	public double getNumberOfPixelsPerMicrometer() {
	   return this.numberOfPixelsPerMicrometer;
   }
   
   public boolean isChemotaxisEnabled() {
   
   	return chemotaxisEnabled;
   }

	
   public void setChemotaxisEnabled(boolean chemotaxisEnabled) {
   
   	this.chemotaxisEnabled = chemotaxisEnabled;
   }
   
	@NoUserModification
   public ModelDimensionality getModelDimensionality() {	   
	   return ModelDimensionality.THREE_DIMENSIONAL;
   }
	@NoUserModification
   public double getCellDiameterInMikron() {
   
   	return cellDiameterInMikron;
   }
	@NoUserModification
   public void setCellDiameterInMikron(double cellDiameterInMikron) {
   
   	this.cellDiameterInMikron = cellDiameterInMikron;
   }
	
	

}
