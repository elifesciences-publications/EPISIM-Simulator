package sim.app.episim.model.biomechanics.vertexbased;

import episiminterfaces.EpisimMechanicalModelGlobalParameters;


public class VertexBasedMechanicalModelGlobalParameters implements EpisimMechanicalModelGlobalParameters, java.io.Serializable {
	
	/**
    * 
    */
   private static final long serialVersionUID = -2526221430357457145L;
   
	private double kappa = 650;
	private double lambda = 15000;
	private double lambda_high_factor = 1;//2;
	private double lambda_low_factor = 1;//0.4;
	private double gamma = 100;	
	
	//----------------------------------------------------------------------------------------------------------------------
	// Other required Parameters
	//----------------------------------------------------------------------------------------------------------------------
	
	private int basalAmplitude_µm = 40; // depth of an undulation
	private int basalOpening_µm = 250; // width of undulation at the middle
	private double width = 140;	
	private double seedMinDepth_frac = 0.02; // beginning with which depth a stem cell is seeded
	private boolean seedReverse = false;	
	private int basalDensity_µm = 8; // width of undulation at the middle	
	private double neighborhood_µm= 10.0;
	
	
	//----------------------------------------------------------------------------------------------------------------------
	
	
	private static VertexBasedMechanicalModelGlobalParameters instance = new VertexBasedMechanicalModelGlobalParameters();
	
	private VertexBasedMechanicalModelGlobalParameters() {}


	public static VertexBasedMechanicalModelGlobalParameters getInstance(){ return instance; }
	
   public double getKappa() {
   
   	return kappa;
   }



	
   public void setKappa(double kappa) {
   
   	this.kappa = kappa;
   }



	
   public double getLambda() {
   
   	return lambda;
   }



	
   public void setLambda(double lambda) {
   
   	this.lambda = lambda;
   }



	
   public double getLambda_high_factor() {
   
   	return lambda_high_factor;
   }



	
   public void setLambda_high_factor(double lambda_high_factor) {
   
   	this.lambda_high_factor = lambda_high_factor;
   }



	
   public double getLambda_low_factor() {
   
   	return lambda_low_factor;
   }



	
   public void setLambda_low_factor(double lambda_low_factor) {
   
   	this.lambda_low_factor = lambda_low_factor;
   }



	
   public double getGamma() {
   
   	return gamma;
   }



	
   public void setGamma(double gamma) {
   
   	this.gamma = gamma;
   }

	
   public int getBasalAmplitude_µm() { return basalAmplitude_µm; }	
   public void setBasalAmplitude_µm(int basalAmplitude_µm) { this.basalAmplitude_µm = basalAmplitude_µm; }
	
   public int getBasalOpening_µm() { return basalOpening_µm; }	
   public void setBasalOpening_µm(int basalOpening_µm) { this.basalOpening_µm = basalOpening_µm; }
	
   public double getWidth() {	return width; }	
   public void setWidth(double width) { this.width = width; }
	
   public double getSeedMinDepth_frac() { return seedMinDepth_frac; }   
   public void setSeedMinDepth_frac(double seedMinDepth_frac) { this.seedMinDepth_frac = seedMinDepth_frac; }   
   
   public boolean getSeedReverse() { return seedReverse; }	
   public void setSeedReverse(boolean seedReverse) { this.seedReverse = seedReverse; }
   
   public int getBasalDensity_µm() { return basalDensity_µm; }	
   public void setBasalDensity_µm(int basalDensity_µm) { this.basalDensity_µm = basalDensity_µm; }
	
   public double getNeighborhood_µm() { return neighborhood_µm; }
   public void setNeighborhood_µm(double neighborhood_µm) { this.neighborhood_µm = neighborhood_µm; }
}
