package sim.app.episim;


import sim.app.episim.*;

public class EpisimModel{

      public static final int KTYPE_UNASSIGNED=0;
		public static final int KTYPE_STEM=1;
		public static final int KTYPE_BASAL=2;
		public static final int KTYPE_TA=3;
		public static final int KTYPE_SPINOSUM=4;
		public static final int KTYPE_LATESPINOSUM=5;        
		public static final int KTYPE_GRANULOSUM=7;
		public static final int KTYPE_RENAME=8;
		public static final int KTYPE_NONUCLEUS=9;
		public static final int KTYPE_NIRVANA=9;

		
		private int maxCellAge_t =2000;
	   private double randomness = 0.05;
		private double neighborhood_µm= 10.0;
		private double epidermalWaterflux=0.06;    // PERCENTAGE transport of water and particles upwards in one timestep
		private double epidermalDiffusion=0.00;    // PERCENTAGE transport of water and particles upwards in one timestep 
		private double minSigCalLateSpinosum=250; // ABSOLUTE minimal Calcium concentration for late Spinosum
		private double calBasalEntry_per_t=2.3; // ABSOLUTE secretion and evaporation of calcium
		private double calSaturation=500; // ABSOLUTE secretion and evaporation of calcium
		private double lamellaSecretion=10; // ABSOLUTE secretion and evaporation of lamella carrying lipids
		private double lamellaSaturation=150; // ABSOLUTE secretion and evaporation of calcium
		private double lipSaturation=150; // ABSOLUTE secretion and evaporation of calcium
		private double minSigLipidsBarrier=130; // ABSOLUTE Only when Lipids are present with this amount barrier is ok
		private double barrierLamellaUse_frac=0.5;  // PERCENTAGE of the available lipids are used in one timestep for the formation of a barrier
		private double basalLayerWidth=15;  // For Statistics of Basal Layer: Cell Definition (for GrowthFraction): distance to membrane not more than gBasalLayerWidth
		private double membraneCellsWidth=4;  // Cells sitting directly on membrane: must not differentiate but take up dermal molecules distance to membrane not more than gBasalLayerWidth  
		private double barrierLossReduction_frac=0.03;  // PERCENTAGE Loss of any particle on outer membrane               
		private int stemCycle_t=120; // every 50 iteration happens a stem cell cycle
		private int tACycle_t =120; // every 50 iteration happens a stem cell cycle              
		private double tAMaxBirthAge_frac=0.08;
		private int basalAmplitude_µm = 40;   // depth of an undulation
		private int basalOpening_µm=150;     // width of undulation at the middle
		private double seedMinDepth_frac=0.02; // beginning with which depth a stem cell is seeded
		private boolean seedReverse =false;
		private boolean uptakeCalcium=false;
		private int basalDensity_µm=8;     // width of undulation at the middle
		private double externalPush=1.1;    // y-offset
		private double cohesion = 0.01;
		private double gravitation=0.0;    // y-offset
		private double adhesionDist=2;   // distance farer away than cell size (outer circle) in which adhesion is actice
		private double ad_Stem_Other=0;       
		private double ad_TA_Other=0;
		private double ad_Spi_Spi=0.0;   // 0.1
		private double ad_Spi_Granu=0.0;    // 0.05
		private double ad_Granu_Granu=0.0;    //0.1
		private double width = 140;
		private int typeColor=1;


	 


		private double[][] adh_array=new double [10][10];

	 	
	   
	                          
		public EpisimModel(){

			for (int i=0; i<10; i++)
	         for (int j=0; j<10; j++)
	             adh_array[i][j]=0;  // default
	     
	         adh_array[KTYPE_STEM][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_TA] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_BASAL] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_SPINOSUM] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_LATESPINOSUM] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_GRANULOSUM] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_RENAME] = ad_Stem_Other;
	         adh_array[KTYPE_STEM][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_TA][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_BASAL][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_SPINOSUM][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_STEM] = ad_Stem_Other;
	         adh_array[KTYPE_RENAME][KTYPE_STEM] = ad_Stem_Other;
	                  
	         adh_array[KTYPE_TA][KTYPE_STEM] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_TA] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_BASAL] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_SPINOSUM] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_LATESPINOSUM] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_GRANULOSUM] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_RENAME] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_STEM] = ad_TA_Other;
	         adh_array[KTYPE_TA][KTYPE_TA] = ad_TA_Other;
	         adh_array[KTYPE_BASAL][KTYPE_TA] = ad_TA_Other;
	         adh_array[KTYPE_SPINOSUM][KTYPE_TA] = ad_TA_Other;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_TA] = ad_TA_Other;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_TA] = ad_TA_Other;
	         adh_array[KTYPE_RENAME][KTYPE_TA] = ad_TA_Other;
	   
	         adh_array[KTYPE_SPINOSUM][KTYPE_SPINOSUM] = ad_Spi_Spi;
	         adh_array[KTYPE_SPINOSUM][KTYPE_LATESPINOSUM] = ad_Spi_Spi;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_SPINOSUM] = ad_Spi_Spi;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_LATESPINOSUM] = ad_Spi_Spi;             
	                 
	         adh_array[KTYPE_SPINOSUM][KTYPE_GRANULOSUM] = ad_Spi_Granu;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_GRANULOSUM] = ad_Spi_Granu;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_SPINOSUM] = ad_Spi_Granu;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_LATESPINOSUM] = ad_Spi_Granu;
		
		}

		
	 	public int getTypeColor() { return typeColor; }
	 	public void setTypeColor(int val) { if (val >= 0.0) typeColor= val; }
	 	public String[] typeString={"Unused", "Color by cell type","Cell type and outer cells","Color by age", "Color by calcium", "Color by lamella", "Enough lipids for barrier", "Color by ion transports", "Voronoi", "Calcium Voronoi"};
	 	public String getTypeColorName() { if ((typeColor<1) || (typeColor>9)) typeColor=1; return typeString[typeColor]; }
		
	 	public int getMaxCellAge_t() { return maxCellAge_t; }
		public void setMaxCellAge_t(int val) { if (val >= 0.0) maxCellAge_t= val; }
	 	
		public double getRandomness() { return randomness; }
	 	public void setRandomness(double val) { if (val >= 0.0) randomness = val; }
	    
	 	public double getNeighborhood_µm() { return neighborhood_µm; }
	 	public void setNeighborhood_µm(double val) { if (val > 0) neighborhood_µm= val; }

		
	      public double getEpidermalWaterflux() { return epidermalWaterflux; }
	 	public void   setEpidermalWaterflux(double val) { if (val >= 0.0) epidermalWaterflux= val; }

		
	 	public double getEpidermalDiffusion() { return epidermalDiffusion; }
	 	public void   setEpidermalDiffusion(double val) { if (val >= 0.0) epidermalDiffusion= val; }

		
		public double getMinSigCalLateSpinosum() { return minSigCalLateSpinosum; }
	 	public void   setMinSigCalLateSpinosum(double val) { if (val >= 0.0) minSigCalLateSpinosum= val; }

		
	 	public double getCalBasalEntry_per_t() { return calBasalEntry_per_t; }
		public void   setCalBasalEntry_per_t(double val) { if (val >= 0.0) calBasalEntry_per_t= val; }
		
	 	public double getCalSaturation() { return calSaturation; }
	 	public void   setCalSaturation(double val) { if (val >= 0.0) calSaturation= val; }

		
	 	public double getLamellaSecretion() { return lamellaSecretion; }
	 	public void   setLamellaSecretion(double val) { if (val >= 0.0) lamellaSecretion= val; }

		
		
	 	public double getLamellaSaturation() { return lamellaSaturation; }
	 	public void   setLamellaSaturation(double val) { if (val >= 0.0) lamellaSaturation= val; }

		
	 	public double getLipSaturation() { return lipSaturation; }
		public void   setLipSaturation(double val) { if (val >= 0.0) lipSaturation= val; }

		
	 	public double getMinSigLipidsBarrier() { return minSigLipidsBarrier; }
	 	public void   setMinSigLipidsBarrier(double val) { if (val >= 0.0) minSigLipidsBarrier= val; }

		
	 	public double getBarrierLamellaUse_frac() { return barrierLamellaUse_frac; }
		public void   setBarrierLamellaUse_frac(double val) { if (val >= 0.0) barrierLamellaUse_frac= val; }

		
	 	public double getBasalLayerWidth() { return basalLayerWidth; }
	 	public void   setBasalLayerWidth(double val) { if (val >= 0.0) basalLayerWidth= val; }

		
	 	public double getMembraneCellsWidth() { return membraneCellsWidth; }
	 	public void   setMembraneCellsWidth(double val) { if (val >= 0.0) membraneCellsWidth= val; }

		
	 	public double getBarrierLossReduction_frac() { return barrierLossReduction_frac; }
	 	public void   setBarrierLossReduction_frac(double val) { if (val >= 0.0) barrierLossReduction_frac= val; }

		
	 	public int getStemCycle_t() { return stemCycle_t; }
	 	public void setStemCycle_t(int val) { if (val >= 0.0) stemCycle_t= val; }

		
		public int getTACycle_t() { return tACycle_t ; }
	 	public void setTACycle_t(int val) { if (val >= 0.0) tACycle_t = val; }

	 	
		public double getTAMaxBirthAge_frac() { return tAMaxBirthAge_frac; }
	 	public void setTAMaxBirthAge_frac(double val) { if (val >= 0.0) tAMaxBirthAge_frac= val; }

		
	 	public int getBasalAmplitude_µm() { return basalAmplitude_µm; }
	 	public void setBasalAmplitude_µm(int val) { if (val >= 0.0) basalAmplitude_µm= val; }

	 	public int getBasalOpening_µm() { return basalOpening_µm; }
	 	public void setBasalOpening_µm(int val) { if (val >= 0.0) basalOpening_µm= val; }

		
	 	public double getSeedMinDepth_frac() { return seedMinDepth_frac; }
	 	public void setSeedMinDepth_frac(double val) { if (val >= 0.0) seedMinDepth_frac= val; }

		
	 	public boolean getSeedReverse() { return seedReverse ; }
	 	public void setSeedReverse(boolean val) { seedReverse =val; }

		
	 	public boolean getUptakeCalcium() { return uptakeCalcium; }
	 	public void setUptakeCalcium(boolean val) { uptakeCalcium=val; }

		
	 	public int getBasalDensity_µm() { return basalDensity_µm; }
	 	public void setBasalDensity_µm(int val) { if (val >= 0) basalDensity_µm = val; }

		
	 	public double getExternalPush() { return externalPush; }
	 	public void setExternalPush(double val) { if (val > 0) externalPush= val; }

		
	 	public double getCohesion() { return cohesion; }
	 	public void setCohesion(double val) { if (val >= 0.0) cohesion = val; }

		
	 	public double getGravitation() { return gravitation; }
	 	public void setGravitation(double val) { if (val >= 0.0) gravitation = val; }


	 	public double getAdhesionDist() { return adhesionDist; }
	 	public void  setAdhesionDist(double val) { adhesionDist= val; }

		
	 	public void setAd_Stem_Other(double val) 
		 { if (val >= 0.0) 
		     {
	      	   ad_Stem_Other=val;
		         adh_array[KTYPE_STEM][KTYPE_STEM] = val;
		         adh_array[KTYPE_STEM][KTYPE_TA] = val;
	      	   adh_array[KTYPE_STEM][KTYPE_BASAL] = val;
		         adh_array[KTYPE_STEM][KTYPE_SPINOSUM] = val;
		         adh_array[KTYPE_STEM][KTYPE_LATESPINOSUM] = val;
		         adh_array[KTYPE_STEM][KTYPE_GRANULOSUM] = val;
		         adh_array[KTYPE_STEM][KTYPE_RENAME] = val;
		         adh_array[KTYPE_STEM][KTYPE_STEM] = val;
		         adh_array[KTYPE_TA][KTYPE_STEM] = val;
		         adh_array[KTYPE_BASAL][KTYPE_STEM] = val;
		         adh_array[KTYPE_SPINOSUM][KTYPE_STEM] = val;
		         adh_array[KTYPE_LATESPINOSUM][KTYPE_STEM] = val;
		         adh_array[KTYPE_GRANULOSUM][KTYPE_STEM] = val;
		         adh_array[KTYPE_RENAME][KTYPE_STEM] = val;
	     }
	 }    
	 public double getAd_Stem_Other() { return ad_Stem_Other; }

	 public void setAd_TA_Other(double val) 
	 { if (val >= 0.0)  
	   {
	         ad_TA_Other= val; 
	         adh_array[KTYPE_TA][KTYPE_STEM] = val;
	         adh_array[KTYPE_TA][KTYPE_TA] = val;
	         adh_array[KTYPE_TA][KTYPE_BASAL] = val;
	         adh_array[KTYPE_TA][KTYPE_SPINOSUM] = val;
	         adh_array[KTYPE_TA][KTYPE_LATESPINOSUM] = val;
	         adh_array[KTYPE_TA][KTYPE_GRANULOSUM] = val;
	         adh_array[KTYPE_TA][KTYPE_RENAME] = val;
	         adh_array[KTYPE_TA][KTYPE_STEM] = val;
	         adh_array[KTYPE_TA][KTYPE_TA] = val;
	         adh_array[KTYPE_BASAL][KTYPE_TA] = val;
	         adh_array[KTYPE_SPINOSUM][KTYPE_TA] = val;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_TA] = val;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_TA] = val;
	         adh_array[KTYPE_RENAME][KTYPE_TA] = val;        
	   }
	 }    
	 public double getAd_TA_Other() { return ad_TA_Other; }

	 public void setAd_Spi_Spi(double val) 
	 {   if (val >= 0.0) 
	     {
	         ad_Spi_Spi= val; 
	         adh_array[KTYPE_SPINOSUM][KTYPE_SPINOSUM] = val;
	         adh_array[KTYPE_SPINOSUM][KTYPE_LATESPINOSUM] = val;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_SPINOSUM] = val;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_LATESPINOSUM] = val;                    
	     }
	 }    
	 public double getAd_Spi_Spi() { return ad_Spi_Spi; }

	 public void setAd_Spi_Granu(double val) 
	 { 
	     if (val >= 0.0) {
	         ad_Spi_Granu= val;
	         adh_array[KTYPE_SPINOSUM][KTYPE_GRANULOSUM] = val;
	         adh_array[KTYPE_LATESPINOSUM][KTYPE_GRANULOSUM] = val;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_SPINOSUM] = val;
	         adh_array[KTYPE_GRANULOSUM][KTYPE_LATESPINOSUM] = val;
	     }
	 }
	 public double getAd_Spi_Granu() { return ad_Spi_Granu; }

	 public void setAd_Granu_Granu(double val) 
	 { if (val >= 0.0) {
	       ad_Granu_Granu= val; 
	         adh_array[KTYPE_GRANULOSUM][KTYPE_GRANULOSUM] = val;            
	   }
	 }    
	 public double getAd_Granu_Granu() { return ad_Granu_Granu; }   

	 public void setWidth(double val) { if (val > 0) width = val; }
	 public double getWidth() { return width; }   

		        
	 public double gibAdh_array(int pos1, int pos2){
		return adh_array[pos1][pos2];
	 }

	public void setzeAdh_array(int pos1, int pos2, double val){
		if (val >= 0.0) adh_array[pos1][pos2] = val;
	 }	               	               
	               

	public void differentiate(KCyte  kCyte, EpidermisClass theEpidermis, boolean pBarrierMember){
		
		int keratinoType =kCyte.getKeratinoType();
		
		double ageFrac=(double)kCyte.getKeratinoAge() / maxCellAge_t;
      
      if (keratinoType!=KTYPE_NONUCLEUS)
      {
         kCyte.incrementKeratinoAge(); 
      	
          ageFrac=(double)kCyte.getKeratinoAge() / (double)maxCellAge_t;
      }
		switch(keratinoType) {
		case  KTYPE_TA: {
			if(ageFrac >= tAMaxBirthAge_frac) // ta cells become
																			// after their birth
																			// period spinosum
																			// cells
			{
				kCyte.setKeratinoType(KTYPE_SPINOSUM);
				// update statistics
				theEpidermis.dekrementActualTA();
				theEpidermis.inkrementActualSpi();
			}
			break;
		}

		case KTYPE_SPINOSUM: {
			if(((kCyte.getExternalCalcium() + kCyte.getInternalCalcium()) >= minSigCalLateSpinosum)
					&& (!kCyte.isMembraneCell())) // ((ageFrac>=theEpidermis.LateSpinosumAge)
													// &&
			{
				kCyte.setKeratinoType(KTYPE_LATESPINOSUM);
				// statistics
				theEpidermis.dekrementActualSpi();
				theEpidermis.inkrementActualLateSpi();
			}
			break;
		}

		case KTYPE_LATESPINOSUM: {
			if((theEpidermis.DevelopGranulosum) && (kCyte.getLipids() >= minSigLipidsBarrier)
					&& (pBarrierMember)){
				kCyte.incrementSpinosumCounter();
				if(kCyte.getSpinosumCounter() > 100){
					kCyte.setKeratinoType(KTYPE_GRANULOSUM); // Spinosum, nur über Signale
																// erreichbar
					// statistics
					theEpidermis.dekrementActualLateSpi();
					theEpidermis.inkrementActualGranu();
				}

			}
			else if((kCyte.getExternalCalcium() + kCyte.getInternalCalcium()) < minSigCalLateSpinosum) // de-differentiate
																																			// if
																																			// not
																																			// enough
																																			// calcium
			{
				kCyte.decrementSpinosumCounter();
				
			}
			break;
		}

		case KTYPE_GRANULOSUM: {
			
			kCyte.setKeratinoWidth(kCyte.getGKeratinoWidthGranu());
			kCyte.setKeratinoHeight(kCyte.getGKeratinoHeightGranu());
		}
		}
//	 Death as to old, cell goes to nirvana and can be resurrected later
		// as a new one
     // if ((KeratinoType!=ktype_stem) && (KeratinoType!=ktype_nonucleus)
		// &&(KeratinoAge>=theEpidermis.maxAge))
     if ((kCyte.getKeratinoType()!=KTYPE_STEM) && 
   		  (kCyte.getKeratinoType()!= KTYPE_NONUCLEUS) && (kCyte.getKeratinoAge()>=kCyte.getLocal_maxAge()))
     {
         if (kCyte.isBasalStatisticsCell())
             theEpidermis.setGStatistics_Apoptosis_BasalCounter(
            		 theEpidermis.getGStatistics_Apoptosis_BasalCounter() + 1); // Counter
																						// counts
																						// and
																						// _BasalApoptosis
																						// is the
																						// value
																						// per 10
																						// timeticks
         if (kCyte.getKeratinoType()==KTYPE_SPINOSUM)
         	 theEpidermis.setGStatistics_Apoptosis_EarlySpiCounter(
            		 theEpidermis.getGStatistics_Apoptosis_EarlySpiCounter() + 1);
             
																							// counts
																							// and
																							// _BasalApoptosis
																							// is
																							// the
																							// value
																							// per
																							// 10
																							// timeticks
         if (kCyte.getKeratinoType()== KTYPE_LATESPINOSUM)
         	 theEpidermis.setGStatistics_Apoptosis_LateSpiCounter(
            		 theEpidermis.getGStatistics_Apoptosis_LateSpiCounter() + 1);
            
																						// counts
																						// and
																						// _BasalApoptosis
																						// is the
																						// value
																						// per 10
																						// timeticks
         if (kCyte.getKeratinoType()== KTYPE_GRANULOSUM)
         	 theEpidermis.setGStatistics_Apoptosis_GranuCounter(
            		 theEpidermis.getGStatistics_Apoptosis_GranuCounter() + 1);
            																		// counts
																						// and
																						// _BasalApoptosis
																						// is the
																						// value
																						// per 10
																						// timeticks

         if (kCyte.getKeratinoType()==KTYPE_SPINOSUM) 
         	theEpidermis.dekrementActualSpi();
         if (kCyte.getKeratinoType()==KTYPE_LATESPINOSUM) 
         	theEpidermis.dekrementActualLateSpi();
         if (kCyte.getKeratinoType()==KTYPE_TA) 
         	theEpidermis.dekrementActualTA();
         if (kCyte.getKeratinoType()==KTYPE_GRANULOSUM) 
         	theEpidermis.dekrementActualGranu();
         kCyte.setKeratinoType(KTYPE_NONUCLEUS);
         theEpidermis.inkrementActualNoNucleus();
         
     }
	}

	public double [][] returnAdhesionArray(){
		return adh_array;
	}


}