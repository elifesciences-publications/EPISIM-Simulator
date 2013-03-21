package sim.app.episim.model.biomechanics.centerbased.adhesion;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;

public class AdhesiveCenterBasedMechanicalModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private int basalAmplitude_mikron = 0; // depth of an undulation
	private int basalOpening_mikron = 250; // width of undulation at the middle
	private double width = 200;
	private double height = 50;
	private double randomness = 0.07;
	
	
	private double externalPush = 1.2; // y-offset
	
	
	private double neighborhood_mikron= 10.0;
	private boolean drawCellsAsEllipses = false;
	private double numberOfPixelsPerMicrometer = 1;
	
	private double neighbourhoodOptDistFact = 1.3;
	
	
	private double initCellCoveredDistInMikron = 30;
	private int cellSizeDeltaSimSteps =100;
	private double optDistanceAdhesionFact = 1.25;
	private int basalMembraneDiscrSteps= 40;
	private int basalMembraneContactTimeThreshold= 2000;
	private double basalMembraneHighAdhesionFactor=3;
	private double optDistanceScalingFactor = 0.95;
	private double randomGravity = 0.3;
	
	public AdhesiveCenterBasedMechanicalModelGP() {}
	
	public boolean isDrawCellsAsEllipses() {
		return drawCellsAsEllipses;
	}
	
	public void setDrawCellsAsEllipses(boolean drawCellsAsEllipses) {
	  	this.drawCellsAsEllipses = drawCellsAsEllipses;
	}
	
	public int getBasalAmplitude_mikron() {
		return basalAmplitude_mikron;
	}

	public void setBasalAmplitude_mikron(int val) {
		if(val >= 0.0)
			basalAmplitude_mikron = val;
	}
	
	public int getBasalOpening_mikron() {
		return basalOpening_mikron;
	}

	public void setBasalOpening_mikron(int val) {
		if(val >= 0.0)
			basalOpening_mikron = val;
	}	
	
	public double getRandomness() {
		return randomness;
	}

	public void setRandomness(double val) {
		if(val >= 0.0)
			randomness = val;
	}
	
	@NoUserModification
	public double getNeighborhood_mikron() { return neighborhood_mikron; }
	
	@NoUserModification
 	public void setNeighborhood_mikron(double val) { if (val > 0) neighborhood_mikron= val; }	
	
	
	public double getExternalPush() {
		return externalPush;
	}
	public void setExternalPush(double val) {
		if(val > 0)
			externalPush = val;
	}
	
	public void setWidthInMikron(double val) {
		if(val > 0)	width = val;
	}	
	public double getWidthInMikron() {
		return width;
	}
   public double getHeightInMikron() {   
   	return height;
   }	
   public void setHeightInMikron(double val) {   
   	if(val > 0) this.height = val;
   }
   public double getLengthInMikron() {
   	//not needed in 2D model
   	return 0;
   }	
   public void setLengthInMikron(double val) {   
   	//not needed in 2D model
   }

	public void setNumberOfPixelsPerMicrometer(double val) {
		this.numberOfPixelsPerMicrometer = val;
   }
	
	@NoUserModification
	public double getNumberOfPixelsPerMicrometer() {
		return this.numberOfPixelsPerMicrometer;
   }

	@NoUserModification
   public boolean areDiffusionFieldsContinousInXDirection() {
		return true;
   }

	@NoUserModification
   public boolean areDiffusionFieldsContinousInYDirection() {
	   return false;
   }
	
	@NoUserModification
	public boolean areDiffusionFieldsContinousInZDirection() {	   
	   return false;
   }

	@NoUserModification
   public ModelDimensionality getModelDimensionality() {	   
	   return ModelDimensionality.TWO_DIMENSIONAL;
   }

	
   public double getNeighbourhoodOptDistFact() {
   
   	return neighbourhoodOptDistFact;
   }

	
   public void setNeighbourhoodOptDistFact(double neighbourhoodOptDistFact) {
		
   	this.neighbourhoodOptDistFact = neighbourhoodOptDistFact;
   }

	
   public double getInitCellCoveredDistInMikron() {
   
   	return initCellCoveredDistInMikron;
   }

	
   public void setInitCellCoveredDistInMikron(double initCellCoveredDistInMikron) {   
   	this.initCellCoveredDistInMikron = initCellCoveredDistInMikron <0 ?0 : initCellCoveredDistInMikron > this.width? this.width:initCellCoveredDistInMikron;
   }

	
   public int getCellSizeDeltaSimSteps() {
   
   	return cellSizeDeltaSimSteps;
   }

	
   public void setCellSizeDeltaSimSteps(int cellSizeDeltaSimSteps) {
   
   	this.cellSizeDeltaSimSteps = cellSizeDeltaSimSteps < 1 ?1:cellSizeDeltaSimSteps;
   }

	
   public double getOptDistanceAdhesionFact() {
   
   	return optDistanceAdhesionFact;
   }

	
   public void setOptDistanceAdhesionFact(double optDistanceAdhesionFact) {
   
   	this.optDistanceAdhesionFact = optDistanceAdhesionFact>= 1?optDistanceAdhesionFact:1;
   }

	
   public int getBasalMembraneDiscrSteps() {
   
   	return basalMembraneDiscrSteps;
   }

	
   public void setBasalMembraneDiscrSteps(int basalMembraneDiscrSteps) {
   
   	this.basalMembraneDiscrSteps = basalMembraneDiscrSteps >= 10 ? basalMembraneDiscrSteps : 10;;
   }

	
   public int getBasalMembraneContactTimeThreshold() {
   
   	return basalMembraneContactTimeThreshold;
   }

	
   public void setBasalMembraneContactTimeThreshold(int basalMembraneContactTimeThreshold) {
   
   	this.basalMembraneContactTimeThreshold = basalMembraneContactTimeThreshold >0 ? basalMembraneContactTimeThreshold : 0;
   }

	
   public double getOptDistanceScalingFactor() {
   
   	return optDistanceScalingFactor;
   }

	
   public void setOptDistanceScalingFactor(double optDistanceScalingFactor) {
   
   	this.optDistanceScalingFactor = optDistanceScalingFactor > 0 ? optDistanceScalingFactor: 0.001;
   }

	
   public double getBasalMembraneHighAdhesionFactor() {
   
   	return basalMembraneHighAdhesionFactor;
   }

	
   public void setBasalMembraneHighAdhesionFactor(double basalMembraneHighAdhesionFactor) {
   
   	this.basalMembraneHighAdhesionFactor = basalMembraneHighAdhesionFactor;
   }

	
   public double getRandomGravity() {
   
   	return randomGravity;
   }

	
   public void setRandomGravity(double randomGravity) {
   
   	this.randomGravity = randomGravity;
   }

  

  
}
