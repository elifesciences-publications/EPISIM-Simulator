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
	
	private int basalAmplitude_�m = 40; // depth of an undulation
	private int basalOpening_�m = 250; // width of undulation at the middle
	private double width = 140;	
	private double seedMinDepth_frac = 0.02; // beginning with which depth a stem cell is seeded
	private boolean seedReverse = false;	
	private int basalDensity_�m = 8; // width of undulation at the middle	
	private double neighborhood_�m= 10.0;
	
	
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

	
   public int getBasalAmplitude_�m() { return basalAmplitude_�m; }	
   public void setBasalAmplitude_�m(int basalAmplitude_�m) { this.basalAmplitude_�m = basalAmplitude_�m; }
	
   public int getBasalOpening_�m() { return basalOpening_�m; }	
   public void setBasalOpening_�m(int basalOpening_�m) { this.basalOpening_�m = basalOpening_�m; }
	
   public double getWidth() {	return width; }	
   public void setWidth(double width) { this.width = width; }
	
   public double getSeedMinDepth_frac() { return seedMinDepth_frac; }   
   public void setSeedMinDepth_frac(double seedMinDepth_frac) { this.seedMinDepth_frac = seedMinDepth_frac; }   
   
   public boolean getSeedReverse() { return seedReverse; }	
   public void setSeedReverse(boolean seedReverse) { this.seedReverse = seedReverse; }
   
   public int getBasalDensity_�m() { return basalDensity_�m; }	
   public void setBasalDensity_�m(int basalDensity_�m) { this.basalDensity_�m = basalDensity_�m; }
	
   public double getNeighborhood_�m() { return neighborhood_�m; }
   public void setNeighborhood_�m(double neighborhood_�m) { this.neighborhood_�m = neighborhood_�m; }
}
