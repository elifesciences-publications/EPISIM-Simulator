package sim.app.episim.model;


import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import sim.app.episim.*;

public class EpisimModel implements EpisimCellDiffModel, EpisimCellDiffModelGlobalParameters{

     

		
		private int maxCellAge_t =2000;
	   
		
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
	
		private double barrierLossReduction_frac=0.03;  // PERCENTAGE Loss of any particle on outer membrane               
		
		
		private int stemCycle_t=120; // every 50 iteration happens a stem cell cycle
		private int tACycle_t =120; // every 50 iteration happens a stem cell cycle              
		
		private double tAMaxBirthAge_frac=0.08;
		
		
	 
		
	 	public int getMaxAge() { return maxCellAge_t; }
		public void setMaxAge(int val) { if (val >= 0.0) maxCellAge_t= val; }
	 	
		
	    
	 	

		
	   public double getEpidermalWaterflux() { return epidermalWaterflux; }
	 	public void   setEpidermalWaterflux(double val) { if (val >= 0.0) epidermalWaterflux= val; }

		
	 	public double getEpidermalDiffusion() { return epidermalDiffusion; }
	 	public void   setEpidermalDiffusion(double val) { if (val >= 0.0) epidermalDiffusion= val; }

		
		public double getMinSigCalLateSpinosum() { return minSigCalLateSpinosum; }
	 	public void   setMinSigCalLateSpinosum(double val) { if (val >= 0.0) minSigCalLateSpinosum= val; }

		
	 	public double getCalBasalEntry_per_t() { return calBasalEntry_per_t; }
		public void   setCalBasalEntry_per_t(double val) { if (val >= 0.0) calBasalEntry_per_t= val; }
		
	 	public double getMaxCa() { return calSaturation; }
	 	public void   setMaxCa(double val) { if (val >= 0.0) calSaturation= val; }

		
	 	public double getLamellaSecretion() { return lamellaSecretion; }
	 	public void   setLamellaSecretion(double val) { if (val >= 0.0) lamellaSecretion= val; }

		
		
	 	public double getMaxLam() { return lamellaSaturation; }
	 	public void   setMaxLam(double val) { if (val >= 0.0) lamellaSaturation= val; }

		
	 	public double getLipSaturation() { return lipSaturation; }
		public void   setLipSaturation(double val) { if (val >= 0.0) lipSaturation= val; }

		
	 	public double getMinSigLipidsBarrier() { return minSigLipidsBarrier; }
	 	public void   setMinSigLipidsBarrier(double val) { if (val >= 0.0) minSigLipidsBarrier= val; }

		
	 	public double getBarrierLamellaUse_frac() { return barrierLamellaUse_frac; }
		public void   setBarrierLamellaUse_frac(double val) { if (val >= 0.0) barrierLamellaUse_frac= val; }

	 	public double getBarrierLossReduction_frac() { return barrierLossReduction_frac; }
	 	public void   setBarrierLossReduction_frac(double val) { if (val >= 0.0) barrierLossReduction_frac= val; }

		
	 	public int getCellCycleStem() { return stemCycle_t; }
	 	public void setCellCycleStem(int val) { if (val >= 0.0) stemCycle_t= val; }

		
		public int getCellCycleTA() { return tACycle_t ; }
	 	public void setCellCycleTA(int val) { if (val >= 0.0) tACycle_t = val; }

	 	
		public double getTAMaxBirthAge_frac() { return tAMaxBirthAge_frac; }
	 	public void setTAMaxBirthAge_frac(double val) { if (val >= 0.0) tAMaxBirthAge_frac= val; }
		
	 	
	               

	public void differentiate(KCyte  kCyte, Epidermis theEpidermis, boolean pBarrierMember){
		
	/*	int keratinoType =kCyte.getKeratinoType();
		
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
			if((theEpidermis.isDevelopGranulosum()) && (kCyte.getLipids() >= minSigLipidsBarrier)
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
         
     }*/
	}
	public void resetInitialGlobalValues() {

		// TODO Auto-generated method stub
		
	}
	public double getCa() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public double getLam() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public double getLip() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public void setCa(double val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setLam(double val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setLip(double val) {

	   // TODO Auto-generated method stub
	   
   }
	public int getDifferentiation() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public void setDifferentiation(int val) {

	   // TODO Auto-generated method stub
	   
   }
	public int getAge() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public void setAge(int val) {

	   // TODO Auto-generated method stub
	   
   }
	public boolean getBooleanProperty(int propertycode) {

	   // TODO Auto-generated method stub
	   return false;
   }
	public double getNumberProperty(int propertycode) {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public String getStringProperty(int propertycode) {

	   // TODO Auto-generated method stub
	   return null;
   }
	public void setBooleanProperty(int propertycode, boolean val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setNumberProperty(int propertycode, double val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setStringProperty(int propertycode, String val) {

	   // TODO Auto-generated method stub
	   
   }
	public boolean getIsAlive() {

	   // TODO Auto-generated method stub
	   return false;
   }
	public double getX() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public double getY() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public EpisimCellDiffModel[] oneStep(EpisimCellDiffModel[] neighbours) {

	   // TODO Auto-generated method stub
	   return null;
   }
	public void setIsAlive(boolean val) {

	   // TODO Auto-generated method stub
	  
   }
	public void setX(double val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setY(double val) {

	   // TODO Auto-generated method stub
	   
   }
	public int getSpecies() {

	   // TODO Auto-generated method stub
	   return 0;
   }
	public void setSpecies(int val) {

	   // TODO Auto-generated method stub
	   
   }
	public boolean getHasCollision() {

	   // TODO Auto-generated method stub
	   return false;
   }
	public boolean getIsMembrane() {

	   // TODO Auto-generated method stub
	   return false;
   }
	public boolean getIsSurface() {

	   // TODO Auto-generated method stub
	   return false;
   }
	public void setHasCollision(boolean val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setIsMembrane(boolean val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setIsSurface(boolean val) {

	   // TODO Auto-generated method stub
	   
   }
	public int getColorB() {

		// TODO Auto-generated method stub
		return 0;
	}
	public int getColorG() {

		// TODO Auto-generated method stub
		return 0;
	}
	public int getColorR() {

		// TODO Auto-generated method stub
		return 0;
	}
	public void setColorB(int val) {

		// TODO Auto-generated method stub
		
	}
	public void setColorG(int val) {

		// TODO Auto-generated method stub
		
	}
	public void setColorR(int val) {

		// TODO Auto-generated method stub
		
	}
	public void setNumberOfNeighbours(int val) {

	   // TODO Auto-generated method stub
	   
   }
	public void setSnapshotValues(EpisimCellDiffModelGlobalParameters parametersObj) {

	   // TODO Auto-generated method stub
	   
   }

	


}