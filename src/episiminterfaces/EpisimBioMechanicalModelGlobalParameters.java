package episiminterfaces;


public interface EpisimBioMechanicalModelGlobalParameters { 	
	
	double getNeighborhood_�m();
 	void setNeighborhood_�m(double val);
 	
 	int getBasalOpening_�m();
 	void setBasalOpening_�m(int val);
 	
 	double getSeedMinDepth_frac();
 	void setSeedMinDepth_frac(double val);
 	
 	boolean getSeedReverse();
 	void setSeedReverse(boolean val);
 	
	void setWidth(double val);
	double getWidth();
	
	int getBasalAmplitude_�m();
 	void setBasalAmplitude_�m(int val);
 	
 	int getBasalDensity_�m();
 	void setBasalDensity_�m(int val);
}
