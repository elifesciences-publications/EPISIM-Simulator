package sim.app.episim.model.biomechanics.centerbased;

import episiminterfaces.EpisimMechanicalModelGlobalParameters;

public class CenterBasedMechanicalModelGlobalParameters implements EpisimMechanicalModelGlobalParameters, java.io.Serializable {
	
	private int basalAmplitude_�m = 40; // depth of an undulation

	private int basalOpening_�m = 250; // width of undulation at the middle

	private double width = 140;
	
	private double randomness = 0.05;

	private double seedMinDepth_frac = 0.02; // beginning with which depth a stem cell is seeded

	private boolean seedReverse = false;	

	private int basalDensity_�m = 8; // width of undulation at the middle

	private double externalPush = 1.1; // y-offset

	private double cohesion = 0.01;

	private double gravitation = 0.0; // y-offset
	
	private double neighborhood_�m= 10.0;
	
	private double basalLayerWidth=15;  // For Statistics of Basal Layer: Cell Definition (for GrowthFraction): distance to membrane not more than gBasalLayerWidth
	private double membraneCellsWidth=4;  // Cells sitting directly on membrane: must not differentiate but take up dermal molecules distance to membrane not more than gBasalLayerWidth  

	public CenterBasedMechanicalModelGlobalParameters() {}	
	
	public int getBasalAmplitude_�m() {

		return basalAmplitude_�m;
	}

	public void setBasalAmplitude_�m(int val) {

		if(val >= 0.0)
			basalAmplitude_�m = val;
	}

	public int getBasalOpening_�m() {

		return basalOpening_�m;
	}

	public void setBasalOpening_�m(int val) {

		if(val >= 0.0)
			basalOpening_�m = val;
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
	
	
	public double getNeighborhood_�m() { return neighborhood_�m; }
 	public void setNeighborhood_�m(double val) { if (val > 0) neighborhood_�m= val; }

	

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

	public int getBasalDensity_�m() {

		return basalDensity_�m;
	}

	public void setBasalDensity_�m(int val) {

		if(val >= 0)
			basalDensity_�m = val;
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
