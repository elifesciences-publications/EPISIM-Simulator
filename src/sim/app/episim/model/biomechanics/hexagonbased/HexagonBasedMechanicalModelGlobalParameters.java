package sim.app.episim.model.biomechanics.hexagonbased;

import sim.app.episim.model.visualization.BorderlinePortrayal.BorderlineConfig;
import sim.app.episim.util.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class HexagonBasedMechanicalModelGlobalParameters implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	public static final double number_of_columns =40;
	public static final double number_of_rows =50;
	public static final double number_of_initially_occupied_columns =10;
	private static final double celldiameter_mikron = 50;
	public static final double inner_hexagonal_radius = ((celldiameter_mikron/2d)/2d)*Math.sqrt(3d);
	public static final double outer_hexagonal_radius = (celldiameter_mikron/2d);
	
	 
	
	private double height_mikron = number_of_rows*2d*inner_hexagonal_radius;
	private double width_mikron = celldiameter_mikron + (number_of_columns-1d)*1.5*outer_hexagonal_radius;
	
	private double numberOfPixelsPerMicrometer = 0.1;
	
	public static final double initialPositionWoundEdge_Mikron = celldiameter_mikron + (number_of_columns-1d)*1.5*outer_hexagonal_radius;
		//celldiameter_mikron + (number_of_initially_occupied_columns-1d)*1.5*outer_hexagonal_radius;
	
	
	private double neighborhood_mikron = 2d*inner_hexagonal_radius;
	
	private double positionXWoundEdge_Mikron = initialPositionWoundEdge_Mikron;
	
	private BorderlineConfig initialWoundEdgeBorderlineConfig;
	private BorderlineConfig actualWoundEdgeBorderlineConfig;
	
	private boolean useContinuousSpace = false;
	private boolean useCellCellInteractionEnergy = true;
	
	private double lambdaChem = 1;
	private boolean chemotaxisEnabled = true;

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
		this.width_mikron = val;
   }
	
	@NoUserModification
	public double getWidthInMikron() {
		return this.width_mikron;
   }

	public void setHeightInMikron(double val) {
		this.height_mikron = val;
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
	public double getHeightInMikron(){
		return this.height_mikron;
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
   
   	return positionXWoundEdge_Mikron;
   }	
   public void setPositionXWoundEdge_Mikron(double positionXWoundEdge_Mikron) {
   
   	this.positionXWoundEdge_Mikron = positionXWoundEdge_Mikron;
   }
   
   @NoUserModification
   public boolean areDiffusionFieldsContinousInXDirection() {
		return getUseContinuousSpace();
   }

	@NoUserModification
   public boolean areDiffusionFieldsContinousInYDirection() {
	   return getUseContinuousSpace();
   }

	
   public double getLambdaChem() {
   
   	return lambdaChem;
   }

	
   public void setLambdaChem(double lambdaChem) {
   
   	this.lambdaChem = lambdaChem;
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

}
