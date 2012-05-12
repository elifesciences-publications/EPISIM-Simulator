package sim.app.episim.datamonitoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.calc.CalculationAlgorithm;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.GlobalClassLoader;
import sim.engine.SimState;
public class GlobalStatistics implements java.io.Serializable, CellDeathListener, ClassLoaderChangeListener{
	private static GlobalStatistics instance;
	
	
	private static final int  NUMBEROFBUCKETS = 10;
	public static final double LASTBUCKETAMOUNT = 2;
	public static final double FIRSTBUCKETAMOUNT = 1;
	
	private int actualNumberStemCells=0;        // Stem cells
	private int actualNumberKCytes=0;      // num of kcytes that are not in nirvana
	private int actualNumberEarlySpiCells=0;         // Spinosum
	private int actualNumberTACells=0;          // TA Cells
	private int actualNumberLateSpi=0;     // Late Spinosum
	private int actualNumberGranuCells=0;       // num of Granulosum KCytes
	private int actualBasalStatisticsCells=0;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	
	private double apoptosis_Basal_Statistics=0;    // apoptosis events during 100 ticks, is calculated from  ..Counter   
	private double apoptosis_EarlySpi_Statistics=0;
	private double apoptosis_LateSpi_Statistics=0;
	private double apoptosis_Granu_Statistics=0;
	
	private double apoptosis_BasalCounter=0;        // Counter is reset every 100 ticks
	private double apoptosis_EarlySpiCounter=0;    // Counter is reset every 100 ticks
	private double apoptosis_LateSpiCounter=0;    // Counter is reset every 100 ticks
	private double apoptosis_GranuCounter=0;     // Counter is reset every 100 ticks
	
/*	private double barrier_ExtCalcium_Statistics=0;
	private double barrier_Lamella_Statistics=0;
	private double barrier_Lipids_Statistics=0;
	
	private double barrier_ExtCalcium_Statistics_temp=0;
	private double barrier_Lamella_Statistics_temp=0;
	private double barrier_Lipids_Statistics_temp=0;*/
	
	private int oldNumOuterCells=0;
	
	private int actualNumberOfBasalStatisticsCells = 0;
	
	private GenericBag<AbstractCell> allCells;
	
	private double sumOfAllAges = 0;
	
	private double [] dnaContents = new double[NUMBEROFBUCKETS]; 
	
	private double [] dnaContentsCumulative = new double [NUMBEROFBUCKETS];
	private double [] dnaContentsAveraged = new double [NUMBEROFBUCKETS];
	private int histogrammCounter = 0;
	
	
	private Set<CellDeathListener> globalCellDeathListener;
	
	private GlobalStatistics(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		globalCellDeathListener = new HashSet<CellDeathListener>();
	}
	
	
	/*
	 * Calculation Algorithms can register here
	 */
	public void addCellDeathListenerCalculationAlgorithm(CellDeathListener listener){
		if(listener instanceof CalculationAlgorithm)this.globalCellDeathListener.add(listener);
	}
	
	
	public GenericBag<AbstractCell> getCells(){
		return this.allCells;		
	}
	public double getGradientMinX(){
		return 30;
	}
	public double getGradientMaxX(){
		return 40;
	}
	public double getGradientMinY(){
		return 0;
	}
	public double getGradientMaxY(){
		return TissueController.getInstance().getTissueBorder().getHeightInMikron();
	}
	
	public static synchronized GlobalStatistics getInstance(){
		if(instance == null) instance = new GlobalStatistics();
		return instance;
	}
	
	
	public EnhancedSteppable getUpdateSteppable(GenericBag<AbstractCell> cells){
		if(cells == null) throw new IllegalArgumentException("Global Statistic Bag containing all cells must not be null!");
		this.allCells = cells;
		return new EnhancedSteppable(){
			private int counter = 0;

			public double getInterval() {
				return 10;
         }
			public void step(SimState state){ 
					updateDataFields(++counter);
					if(counter == 10) counter = 0;
         }
			
		};
		
	}
	
	public double getBucketIntervalSize(){
		return ((LASTBUCKETAMOUNT-FIRSTBUCKETAMOUNT)/ ((double)(NUMBEROFBUCKETS-2)));
	}
	
	private void updateDataFields(int counter){
		
		reset(false);
		this.actualNumberKCytes = allCells.size();
		calculateDnaAmountHistogramm(allCells);
		
		
		for(AbstractCell actCell: allCells){
			int diffLevel =  actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();
			  switch(diffLevel){
				  case EpisimDifferentiationLevel.EARLYSPICELL:{
					  this.actualNumberEarlySpiCells++;
				  }
				  break;
				  case EpisimDifferentiationLevel.GRANUCELL:{
					  this.actualNumberGranuCells++;
				  }
				  break;
				  case EpisimDifferentiationLevel.LATESPICELL:{
					  this.actualNumberLateSpi++;
				  }
				  break;
				  case EpisimDifferentiationLevel.STEMCELL:{
					  this.actualNumberStemCells++;
				  }
				  break;
				  case EpisimDifferentiationLevel.TACELL:{
					  this.actualNumberTACells++;
				  }
				  break;
			  }
			  
			  if (actCell.getIsOuterCell()) // statistics from last time evaluation (so we are always lacking behind one calling period !)
           {
             /*barrier_ExtCalcium_Statistics_temp += actCell.getEpisimCellBehavioralModelObject().getCa();
               barrier_Lamella_Statistics_temp +=actCell.getEpisimCellBehavioralModelObject().getLam();
               barrier_Lipids_Statistics_temp +=actCell.getEpisimCellBehavioralModelObject().getLip();*/                            
               oldNumOuterCells++;
           }
			  
			  if(actCell instanceof UniversalCell && ((UniversalCell) actCell).getIsBasalCell()) this.actualBasalStatisticsCells++;
			  sumOfAllAges += actCell.getEpisimCellBehavioralModelObject().getAge();
		}
			if(counter == 10){
				
				  if(this.actualBasalStatisticsCells>0) this.apoptosis_Basal_Statistics=((this.apoptosis_BasalCounter/100)/actualBasalStatisticsCells)*100;    // /100: per 100 timeticks, then:percentage of Apopotosis
				  if(this.actualNumberEarlySpiCells>0) this.apoptosis_EarlySpi_Statistics=((this.apoptosis_EarlySpiCounter/100)/this.actualNumberEarlySpiCells)*100;    // /100: per 100 timeticks, then:percentage of Apopotosis
				  if(this.actualNumberLateSpi>0)this.apoptosis_LateSpi_Statistics=((this.apoptosis_LateSpiCounter/100)/this.actualNumberLateSpi)*100;    // /100: per 100 timeticks, then:percentage of Apopotosis
				  if(this.actualNumberGranuCells>0)this.apoptosis_Granu_Statistics=((this.apoptosis_GranuCounter/100)/this.actualNumberGranuCells)*100;    // /100: per 100 timeticks, then:percentage of Apopotosis
	
		/*		  barrier_ExtCalcium_Statistics=barrier_ExtCalcium_Statistics_temp/oldNumOuterCells;
				  barrier_Lipids_Statistics= barrier_Lipids_Statistics_temp/oldNumOuterCells;
				  barrier_Lamella_Statistics=barrier_Lamella_Statistics_temp/oldNumOuterCells;*/
				  
				// System.out.println(this.apoptosis_Basal_Statistics + ", "+this.apoptosis_EarlySpi_Statistics + ", "+ this.apoptosis_LateSpi_Statistics + ", "+ this.apoptosis_Granu_Statistics);
				  
				  
				  oldNumOuterCells=0;
              
         /*     barrier_ExtCalcium_Statistics_temp=0;
              barrier_Lipids_Statistics_temp=0;
              barrier_Lamella_Statistics_temp=0;*/
				  
	           this.apoptosis_BasalCounter=0;    // Cells removed from simulation during last time tick    
	           this.apoptosis_EarlySpiCounter=0;
	           this.apoptosis_LateSpiCounter=0;
	           this.apoptosis_GranuCounter=0;
			}
		
	}
	public double [] getDNAContents(){ return this.dnaContents; }
	
	private void calculateDnaAmountHistogramm(GenericBag<AbstractCell> allCells)
	{
		double intervalSize = getBucketIntervalSize();
		//System.out.println(intervalSize);
		
		for(AbstractCell actCell : allCells){
			if(actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.TACELL ||
					actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL){
				double dnaContent = actCell.getEpisimCellBehavioralModelObject().getDnaContent();
				for(int i = 0; i < dnaContents.length; i++){
					if(dnaContent <= (FIRSTBUCKETAMOUNT + i * intervalSize)){ 
						dnaContents[i] += 1;
						dnaContentsCumulative [i] += 1;
						break;
					}
					//	System.out.println("Act Value: " + (FIRSTBUCKETAMOUNT + i * intervalSize));
				}
			}
		}
		histogrammCounter++;
		//Calculate averaged histogramm
		for(int i = 0; i < dnaContentsAveraged.length; i++){
			dnaContentsAveraged[i] = dnaContentsCumulative[i] / histogrammCounter;
		}
		
	}	
	public double[] getDNAContentsAveraged(){ return this.dnaContentsAveraged; }	
	
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// Inkrement
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void inkrementActualNumberStemCells(){ 
		actualNumberStemCells++;        // Stem cells
	}
	public void inkrementActualNumberKCytes(){
		actualNumberKCytes++;      // num of kcytes that are not in nirvana
	}
		
	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// Dekrement
	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void dekrementActualNumberKCytes(){
		actualNumberKCytes--;      // num of kcytes that are not in nirvana
	}
	
	public void reset(boolean isRestartReset){
		
		actualNumberStemCells=0;       
		actualNumberKCytes=0;      
		actualNumberEarlySpiCells=0;         
		actualNumberTACells=0;          
		actualNumberLateSpi=0;     
		actualNumberGranuCells=0;      
		actualBasalStatisticsCells=0;		
		actualNumberOfBasalStatisticsCells = 0;
		sumOfAllAges = 0;		
		
		dnaContents = new double[NUMBEROFBUCKETS];
		dnaContentsAveraged = new double [NUMBEROFBUCKETS];
		
	/*	barrier_ExtCalcium_Statistics_temp=0;
		barrier_Lamella_Statistics_temp=0;
		barrier_Lipids_Statistics_temp=0;*/
		oldNumOuterCells=0;
		
		if(isRestartReset){		
			
			apoptosis_BasalCounter=0;
			apoptosis_EarlySpiCounter=0; 
			apoptosis_LateSpiCounter=0;
			apoptosis_GranuCounter=0;
			
			apoptosis_Basal_Statistics=0;       
			apoptosis_EarlySpi_Statistics=0;
			apoptosis_LateSpi_Statistics=0;
			apoptosis_Granu_Statistics=0;
			
		/*	barrier_ExtCalcium_Statistics=0;
			barrier_Lamella_Statistics=0;
			barrier_Lipids_Statistics=0;*/
			
			dnaContentsCumulative = new double [NUMBEROFBUCKETS];
			histogrammCounter = 0;			
			
		}	
	}


	public void cellIsDead(AbstractCell cell) {
		notifyAllGlobalCellDeathListener(cell);
		   if(cell instanceof UniversalCell){
		   	UniversalCell kcyte = (UniversalCell) cell;
		   	dekrementActualNumberKCytes();
		   	
		   	if(kcyte.getIsBasalCell()){ 
		   		this.apoptosis_BasalCounter++;
		   		
		   	}
		   	int diffLevel =  kcyte.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal();
		   	switch(diffLevel){
	 			   case EpisimDifferentiationLevel.EARLYSPICELL:{
	 				  this.apoptosis_EarlySpiCounter++; 					 
	 			 }
	 			 break;
	 			 case EpisimDifferentiationLevel.GRANUCELL:{
	 				  	this.apoptosis_GranuCounter++; 					  
	 			 }
	 			 break;
	 			 case EpisimDifferentiationLevel.LATESPICELL:{
	 				  	this.apoptosis_LateSpiCounter++; 					  
	 			 }
	 			 break; 				  
	 		}	   	
	   }
	   
   }
	
	private void notifyAllGlobalCellDeathListener(AbstractCell cell){
		for(CellDeathListener listener: globalCellDeathListener){
			listener.cellIsDead(cell);
		}
	}
	
   public int getActualNumberStemCells() {return actualNumberStemCells; }
   public int getActualNumberKCytes(){ return actualNumberKCytes; }
   public int getActualNumberEarlySpiCells() { return actualNumberEarlySpiCells; }
   public int getActualNumberTACells(){ return actualNumberTACells; }
   public int getActualNumberLateSpi(){ return actualNumberLateSpi; }
   public int getActualGranuCells(){ return actualNumberGranuCells; }
   public int getActualBasalStatisticsCells(){ return actualBasalStatisticsCells; }
   public int getActualNumberOfBasalStatisticsCells() { return actualNumberOfBasalStatisticsCells; }
   public double getMeanAgeOfAllCells(){ return (this.sumOfAllAges / this.actualNumberKCytes); }
   public double getApoptosis_Basal_Statistics(){ return apoptosis_Basal_Statistics; }
	public double getApoptosis_EarlySpi_Statistics(){ return apoptosis_EarlySpi_Statistics; }	
   public double getApoptosis_LateSpi_Statistics(){ return apoptosis_LateSpi_Statistics; }
	public double getApoptosis_Granu_Statistics(){ return apoptosis_Granu_Statistics; }
   /*public double getBarrier_ExtCalcium_Statistics() {	return barrier_ExtCalcium_Statistics; }
   public double getBarrier_Lamella_Statistics() { return barrier_Lamella_Statistics; }	
   public double getBarrier_Lipids_Statistics() {return barrier_Lipids_Statistics; }*/


	
   public void classLoaderHasChanged() {
	   instance = null;
   }

	
   
}
