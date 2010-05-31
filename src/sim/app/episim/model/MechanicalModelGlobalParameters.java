package sim.app.episim.model;

import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModelGlobalParameters;

public class MechanicalModelGlobalParameters implements EpisimMechanicalModelGlobalParameters, java.io.Serializable {

	public static final int KTYPE_UNASSIGNED = EpisimCellDiffModelGlobalParameters.KTYPE_UNASSIGNED;

	public static final int KTYPE_STEM = EpisimCellDiffModelGlobalParameters.STEMCELL;
	
	public static final int KTYPE_TA = EpisimCellDiffModelGlobalParameters.TACELL;

	public static final int KTYPE_SPINOSUM = EpisimCellDiffModelGlobalParameters.EARLYSPICELL;

	public static final int KTYPE_LATESPINOSUM = EpisimCellDiffModelGlobalParameters.LATESPICELL;

	public static final int KTYPE_GRANULOSUM = EpisimCellDiffModelGlobalParameters.GRANUCELL;

	public static final int KTYPE_NONUCLEUS = EpisimCellDiffModelGlobalParameters.KTYPE_NONUCLEUS;

	public static final int KTYPE_NIRVANA = EpisimCellDiffModelGlobalParameters.KTYPE_NIRVANA;
	
	

	
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

	private double adhesionDist = 2; // distance farer away than cell size (outer circle) in which adhesion is actice

	private double ad_Stem_Other = 0;

	private double ad_TA_Other = 0;

	private double ad_Spi_Spi = 0.0; // 0.1

	private double ad_Spi_Granu = 0.0; // 0.05

	private double ad_Granu_Granu = 0.0; //0.1

	private double[][] adh_array = new double[8][8];

	
	
	private double neighborhood_µm= 10.0;
	
	private double basalLayerWidth=15;  // For Statistics of Basal Layer: Cell Definition (for GrowthFraction): distance to membrane not more than gBasalLayerWidth
	private double membraneCellsWidth=4;  // Cells sitting directly on membrane: must not differentiate but take up dermal molecules distance to membrane not more than gBasalLayerWidth  

	public MechanicalModelGlobalParameters() {

		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++)
				adh_array[i][j] = 0; // default
		//TODO: Review Adhesion Array --> Used Constants
		adh_array[KTYPE_STEM][KTYPE_STEM] = ad_Stem_Other;
		adh_array[KTYPE_STEM][KTYPE_TA] = ad_Stem_Other;
		
		adh_array[KTYPE_STEM][KTYPE_SPINOSUM] = ad_Stem_Other;
		adh_array[KTYPE_STEM][KTYPE_LATESPINOSUM] = ad_Stem_Other;
		adh_array[KTYPE_STEM][KTYPE_GRANULOSUM] = ad_Stem_Other;
		
		adh_array[KTYPE_STEM][KTYPE_STEM] = ad_Stem_Other;
		adh_array[KTYPE_TA][KTYPE_STEM] = ad_Stem_Other;
	
		adh_array[KTYPE_SPINOSUM][KTYPE_STEM] = ad_Stem_Other;
		adh_array[KTYPE_LATESPINOSUM][KTYPE_STEM] = ad_Stem_Other;
		adh_array[KTYPE_GRANULOSUM][KTYPE_STEM] = ad_Stem_Other;
	

		adh_array[KTYPE_TA][KTYPE_STEM] = ad_TA_Other;
		adh_array[KTYPE_TA][KTYPE_TA] = ad_TA_Other;
		
		adh_array[KTYPE_TA][KTYPE_SPINOSUM] = ad_TA_Other;
		adh_array[KTYPE_TA][KTYPE_LATESPINOSUM] = ad_TA_Other;
		adh_array[KTYPE_TA][KTYPE_GRANULOSUM] = ad_TA_Other;
		
		adh_array[KTYPE_TA][KTYPE_STEM] = ad_TA_Other;
		adh_array[KTYPE_TA][KTYPE_TA] = ad_TA_Other;
		
		adh_array[KTYPE_SPINOSUM][KTYPE_TA] = ad_TA_Other;
		adh_array[KTYPE_LATESPINOSUM][KTYPE_TA] = ad_TA_Other;
		adh_array[KTYPE_GRANULOSUM][KTYPE_TA] = ad_TA_Other;
		

		adh_array[KTYPE_SPINOSUM][KTYPE_SPINOSUM] = ad_Spi_Spi;
		adh_array[KTYPE_SPINOSUM][KTYPE_LATESPINOSUM] = ad_Spi_Spi;
		adh_array[KTYPE_LATESPINOSUM][KTYPE_SPINOSUM] = ad_Spi_Spi;
		adh_array[KTYPE_LATESPINOSUM][KTYPE_LATESPINOSUM] = ad_Spi_Spi;

		adh_array[KTYPE_SPINOSUM][KTYPE_GRANULOSUM] = ad_Spi_Granu;
		adh_array[KTYPE_LATESPINOSUM][KTYPE_GRANULOSUM] = ad_Spi_Granu;
		adh_array[KTYPE_GRANULOSUM][KTYPE_SPINOSUM] = ad_Spi_Granu;
		adh_array[KTYPE_GRANULOSUM][KTYPE_LATESPINOSUM] = ad_Spi_Granu;

	}
	
	
	
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

	public double getAdhesionDist() {

		return adhesionDist;
	}

	public void setAdhesionDist(double val) {

		adhesionDist = val;
	}

	public void setAd_Stem_Other(double val) {

		if(val >= 0.0){
			ad_Stem_Other = val;
			adh_array[KTYPE_STEM][KTYPE_STEM] = val;
			adh_array[KTYPE_STEM][KTYPE_TA] = val;
			
			adh_array[KTYPE_STEM][KTYPE_SPINOSUM] = val;
			adh_array[KTYPE_STEM][KTYPE_LATESPINOSUM] = val;
			adh_array[KTYPE_STEM][KTYPE_GRANULOSUM] = val;
			
			adh_array[KTYPE_STEM][KTYPE_STEM] = val;
			adh_array[KTYPE_TA][KTYPE_STEM] = val;
			
			adh_array[KTYPE_SPINOSUM][KTYPE_STEM] = val;
			adh_array[KTYPE_LATESPINOSUM][KTYPE_STEM] = val;
			adh_array[KTYPE_GRANULOSUM][KTYPE_STEM] = val;
			
		}
	}

	public double getAd_Stem_Other() {

		return ad_Stem_Other;
	}

	public void setAd_TA_Other(double val) {

		if(val >= 0.0){
			ad_TA_Other = val;
			adh_array[KTYPE_TA][KTYPE_STEM] = val;
			adh_array[KTYPE_TA][KTYPE_TA] = val;
			
			adh_array[KTYPE_TA][KTYPE_SPINOSUM] = val;
			adh_array[KTYPE_TA][KTYPE_LATESPINOSUM] = val;
			adh_array[KTYPE_TA][KTYPE_GRANULOSUM] = val;
			
			adh_array[KTYPE_TA][KTYPE_STEM] = val;
			adh_array[KTYPE_TA][KTYPE_TA] = val;
			
			adh_array[KTYPE_SPINOSUM][KTYPE_TA] = val;
			adh_array[KTYPE_LATESPINOSUM][KTYPE_TA] = val;
			adh_array[KTYPE_GRANULOSUM][KTYPE_TA] = val;
			
		}
	}

	public double getAd_TA_Other() {

		return ad_TA_Other;
	}

	public void setAd_Spi_Spi(double val) {

		if(val >= 0.0){
			ad_Spi_Spi = val;
			adh_array[KTYPE_SPINOSUM][KTYPE_SPINOSUM] = val;
			adh_array[KTYPE_SPINOSUM][KTYPE_LATESPINOSUM] = val;
			adh_array[KTYPE_LATESPINOSUM][KTYPE_SPINOSUM] = val;
			adh_array[KTYPE_LATESPINOSUM][KTYPE_LATESPINOSUM] = val;
		}
	}

	public double getAd_Spi_Spi() {

		return ad_Spi_Spi;
	}

	public void setAd_Spi_Granu(double val) {

		if(val >= 0.0){
			ad_Spi_Granu = val;
			adh_array[KTYPE_SPINOSUM][KTYPE_GRANULOSUM] = val;
			adh_array[KTYPE_LATESPINOSUM][KTYPE_GRANULOSUM] = val;
			adh_array[KTYPE_GRANULOSUM][KTYPE_SPINOSUM] = val;
			adh_array[KTYPE_GRANULOSUM][KTYPE_LATESPINOSUM] = val;
		}
	}

	public double getAd_Spi_Granu() {

		return ad_Spi_Granu;
	}

	public void setAd_Granu_Granu(double val) {

		if(val >= 0.0){
			ad_Granu_Granu = val;
			adh_array[KTYPE_GRANULOSUM][KTYPE_GRANULOSUM] = val;
		}
	}

	public double getAd_Granu_Granu() {

		return ad_Granu_Granu;
	}

	public double gibAdh_array(int pos1, int pos2) {

		return adh_array[pos1][pos2];
	}

	public void setzeAdh_array(int pos1, int pos2, double val) {

		if(val >= 0.0)
			adh_array[pos1][pos2] = val;
	}
	public double [][] returnAdhesionArray(){
		return adh_array;
	}
	public void setWidth(double val) {

		if(val > 0)
			width = val;
	}

	public double getWidth() {

		return width;
	}
	

}
