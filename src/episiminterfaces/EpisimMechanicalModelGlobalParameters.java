package episiminterfaces;

/*
 * TODO: To be reviewed
 */
public interface EpisimMechanicalModelGlobalParameters {
	
	
	int getTypeColor();
 	void setTypeColor(int val);
 	
 	String getTypeColorName();
	
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

	
 	boolean getUptakeCalcium();
 	void setUptakeCalcium(boolean val);

	
 	int getBasalDensity_�m();
 	void setBasalDensity_�m(int val);

	
 	double getExternalPush();
 	void setExternalPush(double val);

	
 	double getCohesion();
 	void setCohesion(double val);

	
 	double getGravitation();
 	void setGravitation(double val);


 	double getAdhesionDist();
 	void  setAdhesionDist(double val);

	
 	void setAd_Stem_Other(double val); 
	 
     
   double getAd_Stem_Other();

   void setAd_TA_Other(double val); 
 
   double getAd_TA_Other();

   void setAd_Spi_Spi(double val);
   double getAd_Spi_Spi();

   void setAd_Spi_Granu(double val);
   double getAd_Spi_Granu();

   void setAd_Granu_Granu(double val);
   double getAd_Granu_Granu();
   
   double getNeighborhood_�m();
 	void setNeighborhood_�m(double val);
 

	        
   double gibAdh_array(int pos1, int pos2);

  	void setzeAdh_array(int pos1, int pos2, double val);
  	
  	double [][] returnAdhesionArray();
 	

}
