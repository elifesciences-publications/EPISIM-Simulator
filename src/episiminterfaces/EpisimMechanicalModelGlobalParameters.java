package episiminterfaces;


public interface EpisimMechanicalModelGlobalParameters { 	
	
	int getBasalAmplitude_µm();
 	void setBasalAmplitude_µm(int val);

 	int getBasalOpening_µm();
 	void setBasalOpening_µm(int val);
 	
	double getBasalLayerWidth();
 	void setBasalLayerWidth(double val);

	
 	double getMembraneCellsWidth();
 	void setMembraneCellsWidth(double val);
 	
 	void setWidth(double val);
	double getWidth();
	
	double getRandomness();
 	void setRandomness(double val);
 	
 	double getSeedMinDepth_frac();
 	void setSeedMinDepth_frac(double val);

	
 	boolean getSeedReverse();
 	void setSeedReverse(boolean val);
	
 	int getBasalDensity_µm();
 	void setBasalDensity_µm(int val);
	
 	double getExternalPush();
 	void setExternalPush(double val);
	
 	double getCohesion();
 	void setCohesion(double val);
	
 	double getGravitation();
 	void setGravitation(double val); 	
   
   double getNeighborhood_µm();
 	void setNeighborhood_µm(double val); 		

}
