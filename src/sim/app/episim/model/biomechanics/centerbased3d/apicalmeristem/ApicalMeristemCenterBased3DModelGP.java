package sim.app.episim.model.biomechanics.centerbased3d.apicalmeristem;


/*
Contains all global biomechanical model parameters.
Implements methods to get and set these parameters.
*/

import javax.vecmath.Point3d;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class ApicalMeristemCenterBased3DModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private double width 										    = 1100; //850;  // 
	private double height 											 = 2050; //1350; // 
	private double length 											 = 2050; //1350; // 
	
	private double initialInnerEyeRadius						 = 100; //
	private double innerEyeRadius									 = 100; //
	private double randomness										 = 0;   //0.00000000125;
		
	private double neighborhood_mikron							 = 20.0; 
	private double numberOfPixelsPerMicrometer				 = 1;
	private int numberOfSecondsPerSimStep						 = 3600; //7200;
	
	private double mechanicalNeighbourhoodOptDistFact		 = 1.5;
	private double directNeighbourhoodOptDistFact			 = 1.3;	
	private double optDistanceAdhesionFact						 = 1.3;
	private double optDistanceScalingFactor					 = 0.95;
	private double optDistanceToBMScalingFactor				 = 0.95;
	private double dummyCellOptDistanceScalingFactor		 = 0.5;
	private double linearToExpMaxOverlap_Perc					 = 0.85; //0.85;
	private double repulSpringStiffness_N_per_micro_m 		 = 0.0000000022;
	
	//Pathmanathan et al. 2009: 2.2x10^-3 [N m^-1]; multiply with 1*10^-6 to convert to Newtons per micrometer
	private double adhSpringStiffness_N_per_square_micro_m = 0.000000000022; //0.000000000149;
	
	private double minAverageMigrationMikron 					 = 0.2;
	private double prolifCompWidthMikron						 = 25;
	private int neighbourLostThres								 = 10;
	
	
	public ApicalMeristemCenterBased3DModelGP() {}	
	
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
  	return this.length;
  }	
  public void setLengthInMikron(double val) {   
  	if(val > 0) this.length = val;
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
	   return ModelDimensionality.THREE_DIMENSIONAL;
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
	
  public double getInitialInnerEyeRadius(){   
  	return initialInnerEyeRadius;
  }
	
  public void setInitialInnerEyeRadius(double initialInnerEyeRadius) {   
  	this.initialInnerEyeRadius = initialInnerEyeRadius;
  }

	@NoUserModification
  public double getInnerEyeRadius() {   
  	return innerEyeRadius;
  }

	@NoUserModification
  public void setInnerEyeRadius(double innerEyeRadius) {   
  	this.innerEyeRadius = innerEyeRadius;
  }
	@NoUserModification
	public Point3d getInnerEyeCenter(){
		return new Point3d(50d, getHeightInMikron()/2d, getLengthInMikron()/2d );
	}

	
  public double getMinAverageMigrationMikron() {
  
  	return minAverageMigrationMikron;
  }

	
  public void setMinAverageMigrationMikron(double minAverageMigrationMikron) {
  
  	this.minAverageMigrationMikron = minAverageMigrationMikron;
  }

	
  public double getProlifCompWidthMikron() {
  
  	return prolifCompWidthMikron;
  }

	
  public void setProlifCompWidthMikron(double prolifCompWidthMikron) {
  
  	this.prolifCompWidthMikron = prolifCompWidthMikron;
  }

	
  public double getDummyCellOptDistanceScalingFactor() {
  
  	return dummyCellOptDistanceScalingFactor;
  }

	
  public void setDummyCellOptDistanceScalingFactor(double dummyCellOptDistanceScalingFactor) {
  
  	this.dummyCellOptDistanceScalingFactor = dummyCellOptDistanceScalingFactor;
  }

}
