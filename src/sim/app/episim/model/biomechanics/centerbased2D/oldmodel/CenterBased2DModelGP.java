package sim.app.episim.model.biomechanics.centerbased2D.oldmodel;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoUserModification;

public class CenterBased2DModelGP implements EpisimBiomechanicalModelGlobalParameters, java.io.Serializable {
	
	private int basalAmplitude_mikron = 40; // depth of an undulation
	private int basalPeriod_mikron = 70;
	private int basalYDelta_mikron = 2;
	private int basalOpening_mikron = 250; // width of undulation at the middle
	private double width = 140;
	private double height = 100;
	private double randomness = 0.05;
	private double seedMinDepth_frac = 0.02; // beginning with which depth a stem cell is seeded
	private boolean seedReverse = false;	
	private int basalDensity_mikron = 9;//OriginalValue: 8; 
	private double externalPush = 1.2; // y-offset
	
	
	private double neighborhood_mikron= 10.0;
	private double membraneCellsWidth=4;  // Cells sitting directly on membrane: must not differentiate but take up dermal molecules distance to membrane not more than gBasalLayerWidth  
	private boolean drawCellsAsEllipses = false;
	private double numberOfPixelsPerMicrometer = 1;
	
	private double neighbourhoodOptDistFact = 1.6;
	
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
	
	public double getMembraneCellsWidthInMikron() {
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

   

	
  
	

}
