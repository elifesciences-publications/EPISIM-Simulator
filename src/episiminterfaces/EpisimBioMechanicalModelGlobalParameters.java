package episiminterfaces;


public interface EpisimBioMechanicalModelGlobalParameters { 	
	
	double getNeighborhood_µm();
 	void setNeighborhood_µm(double val);
 	
 	int getBasalOpening_µm();
 	void setBasalOpening_µm(int val);
 	
 	double getSeedMinDepth_frac();
 	void setSeedMinDepth_frac(double val);
 	
 	boolean getSeedReverse();
 	void setSeedReverse(boolean val);
 	
	void setWidth(double val);
	double getWidth();
	
	int getBasalAmplitude_µm();
 	void setBasalAmplitude_µm(int val);
 	
 	int getBasalDensity_µm();
 	void setBasalDensity_µm(int val);
}
