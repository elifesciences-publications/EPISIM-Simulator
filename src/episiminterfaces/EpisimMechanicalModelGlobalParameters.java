package episiminterfaces;


public interface EpisimMechanicalModelGlobalParameters { 	
	
	int getBasalAmplitude_�m();
 	void setBasalAmplitude_�m(int val);

 	int getBasalOpening_�m();
 	void setBasalOpening_�m(int val);
 	
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
	
 	int getBasalDensity_�m();
 	void setBasalDensity_�m(int val);
	
 	double getExternalPush();
 	void setExternalPush(double val);
	
 	double getCohesion();
 	void setCohesion(double val);
	
 	double getGravitation();
 	void setGravitation(double val); 	
   
   double getNeighborhood_�m();
 	void setNeighborhood_�m(double val); 		

}
