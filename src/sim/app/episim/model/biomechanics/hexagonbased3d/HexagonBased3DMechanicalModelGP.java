package sim.app.episim.model.biomechanics.hexagonbased3d;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episiminterfaces.NoUserModification;

public class HexagonBased3DMechanicalModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {

	
	private static final double celldiameter_mikron = 50;
	public static final double hexagonal_radius = (celldiameter_mikron/2d);
	
	 
	
	

	private double numberOfPixelsPerMicrometer = 0.1;
	
	
	private double number_of_columns =40;
	private double number_of_rows =40;
	private double number_of_initially_occupied_layers =8;
	
	private double neighborhood_mikron = 2d*hexagonal_radius;
		
	private boolean useContinuousSpace = false;
	private boolean useCellCellInteractionEnergy = true;
	private boolean stickToCellColony = true;
	
	private boolean chemotaxisEnabled = true;	
	private boolean addSecretingCellColony=true;
	
	
	public boolean getAddSecretingCellColony(){ return addSecretingCellColony; }	
   public void setAddSecretingCellColony(boolean addSecretingCellColony) {
	   this.addSecretingCellColony = addSecretingCellColony;
   }
	
	public double getNeighborhood_mikron() {
	   return this.neighborhood_mikron;
   }

	public void setNeighborhood_mikron(double val) {
	   this.neighborhood_mikron = val;	   
   }
	
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
   
   public int getNumber_of_initially_occupied_layers() {

	   return (int)number_of_initially_occupied_layers;
   }
   
   public void setNumber_of_initially_occupied_layers(int number_of_initially_occupied_layers) {

	   this.number_of_initially_occupied_layers = number_of_initially_occupied_layers;
   }

	public int getBasalOpening_mikron() {
		//not needed in first version
	   return 0;
   }
	@NoUserModification
	public void setBasalOpening_mikron(int val) {
		//not needed in first version
   }
	@NoUserModification
	public int getBasalAmplitude_mikron() {
		//not needed in first version
	   return 0;
   }
	@NoUserModification
	public void setBasalAmplitude_mikron(int val) {
		//not needed in first version
   }
	@NoUserModification
	@NoExport
	public void setWidthInMikron(double val) {
		if(val > 0){
			this.number_of_columns = (int) (val/(2d*hexagonal_radius));
		}
   }
	
	@NoUserModification
	@NoExport
	public double getWidthInMikron() {
		return (this.number_of_columns*2d*hexagonal_radius);
   }
	
	@NoExport
	public void setHeightInMikron(double val) {
		if(val > 0){
			this.number_of_rows = (int)(val/(2d*hexagonal_radius));
		}
   }
			
	@NoUserModification
	@NoExport
	public double getHeightInMikron(){
		return (this.number_of_rows*2d*hexagonal_radius);
   }
	
	@NoUserModification
	public double getLengthInMikron() {
		return (this.number_of_columns*2d*hexagonal_radius);
	}
	
	@NoUserModification
	public void setLengthInMikron(double val) {
		if(val > 0){
	   	this.number_of_columns = (int)(val/(2d*hexagonal_radius));
		}
	}	
	
	@NoUserModification
   public double getCellDiameter_mikron() {   
   	return celldiameter_mikron;
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

	
   
   @NoUserModification
   public boolean areDiffusionFieldsContinousInXDirection() {
		return getUseContinuousSpace();
   }

	@NoUserModification
   public boolean areDiffusionFieldsContinousInYDirection() {
	   return getUseContinuousSpace();
   }
	
	@NoUserModification
   public boolean areDiffusionFieldsContinousInZDirection() {
	   return getUseContinuousSpace();
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

}
