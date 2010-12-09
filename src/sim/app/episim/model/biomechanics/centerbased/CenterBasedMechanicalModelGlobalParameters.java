package sim.app.episim.model.biomechanics.centerbased;

import episiminterfaces.EpisimMechanicalModelGlobalParameters;

public class CenterBasedMechanicalModelGlobalParameters implements EpisimMechanicalModelGlobalParameters, java.io.Serializable {
	
	private int basalAmplitude_µm = 40; // depth of an undulation

	private int basalOpening_µm = 250; // width of undulation at the middle

	private double width = 140;
	
	private double randomness = 0.05;

	private double seedMinDepth_frac = 0.02; // beginning with which depth a stem cell is seeded

	private boolean seedReverse = false;	

	private int basalDensity_µm = 8; // width of undulation at the middle

	private double externalPush = 1.1; // y-offset

	private double cohesion = 0.01;

	private double gravitation = 0.0; // y-offset
	
	private double neighborhood_µm= 10.0;
	
	private double basalLayerWidth=15;  // For Statistics of Basal Layer: Cell Definition (for GrowthFraction): distance to membrane not more than gBasalLayerWidth
	private double membraneCellsWidth=4;  // Cells sitting directly on membrane: must not differentiate but take up dermal molecules distance to membrane not more than gBasalLayerWidth  

	public CenterBasedMechanicalModelGlobalParameters() {}	
	
	public int getBasalAmplitude_µm() {

		return basalAmplitude_µm;
	}

	public void setBasalAmplitude_µm(int val) {

		if(val >= 0.0)
			basalAmplitude_µm = val;
	}

	public int getBasalOpening_µm() {

		return basalOpening_µm;
	}

	public void setBasalOpening_µm(int val) {

		if(val >= 0.0)
			basalOpening_µm = val;
	}
	public double getBasalLayerWidth() {

		return basalLayerWidth;
	}

	public void setBasalLayerWidth(double val) {

		if(val >= 0.0)
			basalLayerWidth = val;
	}

	public double getMembraneCellsWidth() {

		return membraneCellsWidth;
	}

	public void setMembraneCellsWidth(double val) {

		if(val >= 0.0)
			membraneCellsWidth = val;
	}

	
	
	public double getRandomness() {

		return randomness;
	}

	public void setRandomness(double val) {

		if(val >= 0.0)
			randomness = val;
	}
	
	
	public double getNeighborhood_µm() { return neighborhood_µm; }
 	public void setNeighborhood_µm(double val) { if (val > 0) neighborhood_µm= val; }

	

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

	public int getBasalDensity_µm() {

		return basalDensity_µm;
	}

	public void setBasalDensity_µm(int val) {

		if(val >= 0)
			basalDensity_µm = val;
	}

	public double getExternalPush() {

		return externalPush;
	}

	public void setExternalPush(double val) {

		if(val > 0)
			externalPush = val;
	}

	public double getCohesion() {

		return cohesion;
	}

	public void setCohesion(double val) {

		if(val >= 0.0)
			cohesion = val;
	}

	public double getGravitation() {

		return gravitation;
	}

	public void setGravitation(double val) {

		if(val >= 0.0)
			gravitation = val;
	}	
	
	public void setWidth(double val) {

		if(val > 0)
			width = val;
	}

	public double getWidth() {

		return width;
	}
	

}
