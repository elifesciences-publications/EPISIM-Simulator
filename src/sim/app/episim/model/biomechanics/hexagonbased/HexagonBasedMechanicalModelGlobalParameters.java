package sim.app.episim.model.biomechanics.hexagonbased;

import sim.app.episim.model.visualization.BorderlinePortrayal.BorderlineConfig;
import sim.app.episim.util.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class HexagonBasedMechanicalModelGlobalParameters implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private double height_mikron = 5000;
	private double width_mikron = 5000;
	private double cellDiameter_mikron = 50;
	private double neighborhood_mikron = 50;
	private double numberOfPixelsPerMicrometer = 0.1;
	
	public static final double initialPositionWoundEdge_Mikron = 500;
	
	private double positionXWoundEdge_Mikron = 500;
	
	private BorderlineConfig initialWoundEdgeBorderlineConfig;
	private BorderlineConfig actualWoundEdgeBorderlineConfig;
	
	private boolean useContinuousSpace = false;

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
   	return cellDiameter_mikron;
   }	
   public void setCellDiameter_mikron(double cellDiameter_mikron) {   
   	this.cellDiameter_mikron = cellDiameter_mikron;
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

}
