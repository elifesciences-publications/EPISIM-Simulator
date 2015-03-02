package sim.app.episim.model.biomechanics.centerbased2D.newmodel;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class CenterBased2DModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private int basalAmplitude_mikron = 40; // depth of an undulation
	private int basalPeriod_mikron = 100;
	private int basalYDelta_mikron = 5;
	private int basalOpening_mikron = 500; // width of undulation at the middle
	private double width = 400;
	private double height = 200;
	private double randomness = 0;//0.00000000125;
	private double seedMinDepth_frac = 0.25; // beginning with which depth a stem cell is seeded
	
	private int basalDensity_mikron = 30;//OriginalValue: 8; 

	
	
	private double neighborhood_mikron= 20.0; 
	private boolean drawCellsAsEllipses = false;
	private double numberOfPixelsPerMicrometer = 1;
	private int numberOfSecondsPerSimStep=1800;
	
	private double mechanicalNeighbourhoodOptDistFact = 1.5;
	private double directNeighbourhoodOptDistFact = 1.3;	
	private double optDistanceAdhesionFact = 1.3;
	private double optDistanceScalingFactor = 0.85;
	private double optDistanceToBMScalingFactor = 0.95;
	private double linearToExpMaxOverlap_Perc = 0.5;
	private double repulSpringStiffness_N_per_micro_m = 0.0000000022;
	private double adhSpringStiffness_N_per_square_micro_m = 0.000000000022;//0.000000000149;
	
	private int neighbourLostThres = 10;
	
	public CenterBased2DModelGP() {}
	
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
   public ModelDimensionality getModelDimensionality() {	   
	   return ModelDimensionality.TWO_DIMENSIONAL;
   }

	@NoUserModification
   public double getMechanicalNeighbourhoodOptDistFact() {
   
   	return mechanicalNeighbourhoodOptDistFact;
   }

	@NoUserModification
   public void setMechanicalNeighbourhoodOptDistFact(double neighbourhoodOptDistFact) {
   
   	this.mechanicalNeighbourhoodOptDistFact = neighbourhoodOptDistFact;
   }
	
	
   public double getDirectNeighbourhoodOptDistFact() {
   
   	return directNeighbourhoodOptDistFact;
   }

	@NoUserModification
   public void setDirectNeighbourhoodOptDistFact(double directNeighbourhoodOptDistFact) {
   
   	if(directNeighbourhoodOptDistFact>=1)this.directNeighbourhoodOptDistFact = directNeighbourhoodOptDistFact;
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

	
   
	
   public double getLinearToExpMaxOverlap_perc() {
   
   	return linearToExpMaxOverlap_Perc;
   }

	
   public void setLinearToExpMaxOverlap_perc(double linearToExpMaxOverlap_Perc) {
   
   	this.linearToExpMaxOverlap_Perc = linearToExpMaxOverlap_Perc;
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
   public int getNumberOfSecondsPerSimStep() {   
   	return numberOfSecondsPerSimStep;
   }

	
   public void setNumberOfSecondsPerSimStep(int numberOfSecondsPerSimStep) {   
   	if(numberOfSecondsPerSimStep>0)this.numberOfSecondsPerSimStep = numberOfSecondsPerSimStep;
   }

	
   public int getNeighbourLostThres() {
   
   	return neighbourLostThres;
   }

	
   public void setNeighbourLostThres(int neighbourLostThres) {
   
   	this.neighbourLostThres = neighbourLostThres;
   }
  
}
