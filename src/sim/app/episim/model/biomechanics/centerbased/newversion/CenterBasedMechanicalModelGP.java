package sim.app.episim.model.biomechanics.centerbased.newversion;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class CenterBasedMechanicalModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private int basalAmplitude_mikron = 40; // depth of an undulation
	private int basalPeriod_mikron = 100;
	private int basalYDelta_mikron = 5;
	private int basalOpening_mikron = 500; // width of undulation at the middle
	private double width = 400;
	private double height = 200;
	private double randomness = 0.05;
	private double seedMinDepth_frac = 0.25; // beginning with which depth a stem cell is seeded
	private boolean seedReverse = false;	
	private int basalDensity_mikron = 30;//OriginalValue: 8; 

	
	
	private double neighborhood_mikron= 20.0; 
	private boolean drawCellsAsEllipses = false;
	private double numberOfPixelsPerMicrometer = 1;
	
	private double neighbourhoodOptDistFact = 1.6;	
	private double optDistanceAdhesionFact = 1.25;
	private double optDistanceScalingFactor = 0.85;
	private double optDistanceToBMScalingFactor = 0.8;
	private double linearToExpMaxOverlap_mikron = 2.5;
	private double repulSpringStiffness_N_per_micro_m =     0.0000000022;
	private double adhSpringStiffness_N_per_square_micro_m =0.000000000022;//0.000000000149;
	private double randomGravity =0.01;// 0.2;

	
	
	public CenterBasedMechanicalModelGP() {}
	
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

	public double getSeedMinDepth_frac() {
		return seedMinDepth_frac;
	}

	public void setSeedMinDepth_frac(double val) {
		if(val >= 0.0)
			seedMinDepth_frac = val;
	}

	public boolean getSeedReverse() {
		return seedReverse;
	}

	public void setSeedReverse(boolean val) {
		seedReverse = val;
	}	

	public int getBasalDensity_mikron() {
		return basalDensity_mikron;
	}

	public void setBasalDensity_mikron(int val) {
		if(val >= 0)
			basalDensity_mikron = val;
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

	@NoUserModification
   public double getNeighbourhoodOptDistFact() {
   
   	return neighbourhoodOptDistFact;
   }

	@NoUserModification
   public void setNeighbourhoodOptDistFact(double neighbourhoodOptDistFact) {
   
   	this.neighbourhoodOptDistFact = neighbourhoodOptDistFact;
   }
	
	
   public double getOptDistanceAdhesionFact() {
   
   	return optDistanceAdhesionFact;
   }

	
   public void setOptDistanceAdhesionFact(double optDistanceAdhesionFact) {
   
   	this.optDistanceAdhesionFact = optDistanceAdhesionFact>= 1?optDistanceAdhesionFact:1;
   }

	
   public double getOptDistanceScalingFactor() {
   
   	return optDistanceScalingFactor;
   }

	
   public void setOptDistanceScalingFactor(double optDistanceScalingFactor) {
   
   	this.optDistanceScalingFactor = optDistanceScalingFactor > 0 ? optDistanceScalingFactor: 0.001;
   }

	
   public double getRandomGravity() {
   
   	return randomGravity;
   }

	
   public void setRandomGravity(double randomGravity) {
   
   	this.randomGravity = randomGravity;
   }

	
   public int getBasalPeriod_mikron() {
   
   	return basalPeriod_mikron;
   }

	
   public void setBasalPeriod_mikron(int basalPeriod_mikron) {
   
   	this.basalPeriod_mikron = basalPeriod_mikron;
   }

	
   public int getBasalYDelta_mikron() {
   
   	return basalYDelta_mikron;
   }

	
   public void setBasalYDelta_mikron(int basalYDelta_mikron) {
   
   	this.basalYDelta_mikron = basalYDelta_mikron;
   }

	
   
	
   public double getLinearToExpMaxOverlap_mikron() {
   
   	return linearToExpMaxOverlap_mikron;
   }

	
   public void setLinearToExpMaxOverlap_mikron(double linearToExpMaxOverlap_mikron) {
   
   	this.linearToExpMaxOverlap_mikron = linearToExpMaxOverlap_mikron;
   }

	
   public double getRepulSpringStiffness_N_per_micro_m() {
   
   	return repulSpringStiffness_N_per_micro_m;
   }

	
   public void setRepulSpringStiffness_N_per_micro_m(double repulSpringStiffness_N_per_micro_m) {
   
   	this.repulSpringStiffness_N_per_micro_m = repulSpringStiffness_N_per_micro_m;
   }

	
   public double getAdhSpringStiffness_N_per_square_micro_m() {
   
   	return adhSpringStiffness_N_per_square_micro_m;
   }

	
   public void setAdhSpringStiffness_N_per_square_micro_m(double adhSpringStiffness_N_per_square_micro_m) {
   
   	this.adhSpringStiffness_N_per_square_micro_m = adhSpringStiffness_N_per_square_micro_m;
   }

	
   public double getOptDistanceToBMScalingFactor() {
   
   	return optDistanceToBMScalingFactor;
   }

	
   public void setOptDistanceToBMScalingFactor(double optDistanceToBMScalingFactor) {
   
   	this.optDistanceToBMScalingFactor = optDistanceToBMScalingFactor;
   }   
}
