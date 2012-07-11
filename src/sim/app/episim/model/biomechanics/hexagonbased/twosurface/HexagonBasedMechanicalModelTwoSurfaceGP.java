package sim.app.episim.model.biomechanics.hexagonbased.twosurface;

import sim.app.episim.model.biomechanics.hexagonbased.AbstractHexagonBasedMechanicalModelGP;
import sim.app.episim.model.visualization.BorderlinePortrayal.BorderlineConfig;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.NoExport;


public class HexagonBasedMechanicalModelTwoSurfaceGP extends AbstractHexagonBasedMechanicalModelGP {
	
	
	private double celldiameter_mikron = 50;
	private double inner_hexagonal_radius = ((celldiameter_mikron/2d)/2d)*Math.sqrt(3d);
	private double outer_hexagonal_radius = (celldiameter_mikron/2d);
	
	 
	
	
	
	private double numberOfPixelsPerMicrometer = 1;
	
	
	private double number_of_columns =50;
	private double number_of_rows =50;
	private double number_of_initially_occupied_columns =10;
	private int initialCellDensityInPercent = 100;
	private int initialSecretionCellDensityInPercent = 100;
	
	private double initialPositionWoundEdge_Mikron = celldiameter_mikron + (number_of_initially_occupied_columns-1d)*1.5*outer_hexagonal_radius;
	
	
	private double neighborhood_mikron = 2d*inner_hexagonal_radius;
	
	private double positionXWoundEdge_Mikron = initialPositionWoundEdge_Mikron;
	
	private BorderlineConfig initialWoundEdgeBorderlineConfig;
	private BorderlineConfig actualWoundEdgeBorderlineConfig;
	
	private boolean useContinuousSpace = false;
	private boolean useCellCellInteractionEnergy = true;
	
	private boolean chemotaxisEnabled = true;	
	private boolean addSecretingCellColony=true;	
	
   public void setInitialPositionWoundEdge_Mikron(double initialPositionWoundEdge_Mikron) {

	   this.initialPositionWoundEdge_Mikron = initialPositionWoundEdge_Mikron;
   }
   
   public double getInitialPositionWoundEdge_Mikron() {

	   return initialPositionWoundEdge_Mikron;
   }
	
   public double getNumber_of_columns() {
   	
	   return number_of_columns;
   }
   
   public void setNumber_of_columns(double number_of_columns) {

	   this.number_of_columns = (int)number_of_columns;
	   if((this.number_of_columns %2) > 0) this.number_of_columns++;
   }
   
   public double getNumber_of_rows() {
	   return number_of_rows;
   }
   
   public void setNumber_of_rows(double number_of_rows) {

	   this.number_of_rows = (int)number_of_rows;
	   if((this.number_of_rows %2) > 0) this.number_of_rows++;
   }
   
   
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
	
	@NoExport
	public void setWidthInMikron(double val) {
		if(val > 0){
			setNumber_of_columns(1+ ((val-celldiameter_mikron)/(1.5*outer_hexagonal_radius)));
		}
	}
	
	@NoUserModification
	@NoExport
	public double getWidthInMikron() {
		return celldiameter_mikron + (number_of_columns-1d)*1.5*outer_hexagonal_radius;
   }	
	

	public void setCellDiameterMikron(double val) {
		if(val > 0){
			double widthInMikron = getWidthInMikron();
			double heightInMikron = getHeightInMikron();
					
			this.celldiameter_mikron = val;
			inner_hexagonal_radius = ((celldiameter_mikron/2d)/2d)*Math.sqrt(3d);
			outer_hexagonal_radius = (celldiameter_mikron/2d);
			setNumber_of_columns(1 + ((widthInMikron-celldiameter_mikron)/(1.5*outer_hexagonal_radius)));
			setNumber_of_rows(heightInMikron/(2d*inner_hexagonal_radius));
		}
	}
	
	@NoUserModification
	public double getCellDiameterInMikron() {
		return celldiameter_mikron ;
   }
	
	@NoExport
	public void setHeightInMikron(double val) {
		if(val > 0){
			setNumber_of_rows(val/(2d*inner_hexagonal_radius));
		}		
   }
	
	public void setInitialWoundEdgeBorderlineConfig(BorderlineConfig config){
		this.initialWoundEdgeBorderlineConfig = config;
	}
	
	@NoUserModification
	public BorderlineConfig getInitialWoundEdgeBorderlineConfig(){
		return this.initialWoundEdgeBorderlineConfig;
	}
	
	public void setActualWoundEdgeBorderlineConfig(BorderlineConfig config){
		this.actualWoundEdgeBorderlineConfig = config;
	}
	
	@NoUserModification
	public BorderlineConfig getActualWoundEdgeBorderlineConfig(){
		return this.actualWoundEdgeBorderlineConfig;
	}
	
	
	@NoUserModification
	@NoExport
	public double getHeightInMikron(){
		return number_of_rows*2d*inner_hexagonal_radius;
   }
	
	@NoUserModification
	public double getLengthInMikron() {
	   	//not needed in 2D model
	   	return 0;
	}
	@NoUserModification
	public void setLengthInMikron(double val) {   
	   	//not needed in 2D model
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
   public double getPositionXWoundEdge_Mikron() {
		if(initialPositionWoundEdge_Mikron > positionXWoundEdge_Mikron) positionXWoundEdge_Mikron = initialPositionWoundEdge_Mikron;
   	return positionXWoundEdge_Mikron;
   }	
   public void setPositionXWoundEdge_Mikron(double positionXWoundEdge_Mikron) {   	
   	this.positionXWoundEdge_Mikron = positionXWoundEdge_Mikron;
   	if(initialPositionWoundEdge_Mikron > positionXWoundEdge_Mikron) positionXWoundEdge_Mikron = initialPositionWoundEdge_Mikron;
   }
   
   @NoUserModification
   public boolean areDiffusionFieldsContinousInXDirection() {
		return getUseContinuousSpace();
   }

	@NoUserModification
   public boolean areDiffusionFieldsContinousInYDirection() {
	   return getUseContinuousSpace();
   }

	
   

	
   public boolean isChemotaxisEnabled() {
   
   	return chemotaxisEnabled;
   }

	
   public void setChemotaxisEnabled(boolean chemotaxisEnabled) {
   
   	this.chemotaxisEnabled = chemotaxisEnabled;
   }
   
   @NoUserModification
	public boolean areDiffusionFieldsContinousInZDirection() {	   
	   return false;
   }

	@NoUserModification
   public ModelDimensionality getModelDimensionality() {	   
	   return ModelDimensionality.TWO_DIMENSIONAL;
   }

	
   public int getInitialCellDensityInPercent() {
   
   	return initialCellDensityInPercent;
   }

	
   public void setInitialCellDensityInPercent(int initialCellDensityInPercent) {
   
   	this.initialCellDensityInPercent = initialCellDensityInPercent;
   }
   
   public int getInitialSecretionCellDensityInPercent() {
      
   	return initialSecretionCellDensityInPercent;
   }

	
   public void setInitialSecretionCellDensityInPercent(int initialSecretionCellDensityInPercent) {
   
   	this.initialSecretionCellDensityInPercent = initialSecretionCellDensityInPercent;
   }

	
   public double getInner_hexagonal_radius() {
   
   	return inner_hexagonal_radius;
   }

	
   public double getOuter_hexagonal_radius() {
   
   	return outer_hexagonal_radius;
   }

}
