package sim.app.episim.datamonitoring;

import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;
import sim.app.episim.CellType;
import sim.app.episim.KCyte;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.util.EnhancedSteppable;
import sim.app.episim.util.GenericBag;
import sim.engine.SimState;
public class GlobalStatistics implements java.io.Serializable{
	private static GlobalStatistics instance;
	
	
	private int actualNumberStemCells=0;        // Stem cells
	private int actualNumberKCytes=0;      // num of kcytes that are not in nirvana
	private int actualNumberEarlySpiCells=0;         // Spinosum
	private int actualNumberTACells=0;          // TA Cells
	private int actualNumberLateSpi=0;     // Late Spinosum
	private int actualGranuCells=0;       // num of Granulosum KCytes
	private int actualNumberOfNoNucleus=0;   // Cells after lifetime but not shed from the surface
	private int actualBasalStatisticsCells=0;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)

	
	private int actualNumberOfBasalStatisticsCells = 0;
	
	private GenericBag<CellType> allCells;
	
	private double sumOfAllAges = 0;
	
	private GlobalStatistics(){
		
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
		return TissueBorder.getInstance().getHeight();
	}
	
	public static synchronized GlobalStatistics getInstance(){
		if(instance == null) instance = new GlobalStatistics();
		return instance;
	}
	
	
	public EnhancedSteppable getUpdateSteppable(GenericBag<CellType> cells){
		if(cells == null) throw new IllegalArgumentException("Global Statistic Bag containing all cells must not be null!");
		this.allCells = cells;
		return new EnhancedSteppable(){

			public double getInterval() {
				
	         return 50;
         }

			public void step(SimState state) {
 
					updateDataFields();
         }
			
		};
		
	}
	
	
	
	private void updateDataFields(){
		
		reset();
		this.actualNumberKCytes = allCells.size();
		
		for(CellType actCell: allCells){
			int diffLevel =  actCell.getEpisimCellDiffModelObject().getDifferentiation();
			  switch(diffLevel){
				  case EpisimCellDiffModelGlobalParameters.EARLYSPICELL:{
					  this.actualNumberEarlySpiCells++;
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.GRANUCELL:{
					  this.actualGranuCells++;
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.KTYPE_NIRVANA:{
					  
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.KTYPE_NONUCLEUS:{
					  this.actualNumberOfNoNucleus++;
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.KTYPE_UNASSIGNED:{
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.LATESPICELL:{
					  this.actualNumberLateSpi++;
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.STEMCELL:{
					  this.actualNumberStemCells++;
				  }
				  break;
				  case EpisimCellDiffModelGlobalParameters.TACELL:{
					  this.actualNumberTACells++;
				  }
				  break;
			  }
			  
			  if(actCell instanceof KCyte && ((KCyte) actCell).isBasalStatisticsCell()) this.actualBasalStatisticsCells++;
			  sumOfAllAges += actCell.getEpisimCellDiffModelObject().getAge();
		}
	}
	
	
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// Inkrement
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public void inkrementActualNumberStemCells(){ 
		actualNumberStemCells++;        // Stem cells
	}
	public void inkrementActualNumberKCytes(){
		actualNumberKCytes++;      // num of kcytes that are not in nirvana
	}
	public void inkrementActualNumberEarlySpiCells(){
		actualNumberEarlySpiCells++;         // Spinosum
	}
	public void inkrementActualNumberTASells(){
		actualNumberTACells++;          // TA Cells
	}
	public void inkrementActualNumberLateSpi(){
		actualNumberLateSpi++;     // Late Spinosum
	}
	public void inkrementActualGranuCells(){
		actualGranuCells++;       // num of Granulosum KCytes
	}
	public void inkrementActualNumberOfNoNucleus(){
		actualNumberOfNoNucleus++;   // Cells after lifetime but not shed from the surface
	}
	public void inkrementActualBasalStatisticsCells(){
		actualBasalStatisticsCells++;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	}
	
	public void inkrementActualNumberOfBasalStatisticsCells(){
		actualNumberOfBasalStatisticsCells++;
	}
	
	
	
	
	
	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	// Dekrement
	//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public void dekrementActualNumberStemCells(){ 
		actualNumberStemCells--;        // Stem cells
	}
	public void dekrementActualNumberKCytes(){
		actualNumberKCytes--;      // num of kcytes that are not in nirvana
	}
	public void dekrementActualNumberEarlySpiCells(){
		actualNumberEarlySpiCells--;         // Spinosum
	}
	public void dekrementActualNumberTASells(){
		actualNumberTACells--;          // TA Cells
	}
	public void dekrementActualNumberLateSpi(){
		actualNumberLateSpi--;     // Late Spinosum
	}
	public void dekrementActualGranuCells(){
		actualGranuCells--;       // num of Granulosum KCytes
	}
	public void dekrementActualNumberOfNoNucleus(){
		actualNumberOfNoNucleus--;   // Cells after lifetime but not shed from the surface
	}
	public void dekrementActualBasalStatisticsCells(){
		actualBasalStatisticsCells--;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	}
		
	public void dekrementActualNumberOfBasalStatisticsCells(){
		actualNumberOfBasalStatisticsCells--;
	}
	
	public void reset(){
		actualNumberStemCells=0;       
		actualNumberKCytes=0;      
		actualNumberEarlySpiCells=0;         
		actualNumberTACells=0;          
		actualNumberLateSpi=0;     
		actualGranuCells=0;      
		actualNumberOfNoNucleus=0;   
		actualBasalStatisticsCells=0;   
		
		actualNumberOfBasalStatisticsCells = 0;
		sumOfAllAges = 0;
	
	}

	
   public int getActualNumberStemCells() {
   
   	return actualNumberStemCells;
   }

	
   public int getActualNumberKCytes() {
   
   	return actualNumberKCytes;
   }

	
   public int getActualNumberEarlySpiCells() {
   
   	return actualNumberEarlySpiCells;
   }

	
   public int getActualNumberTACells() {
   
   	return actualNumberTACells;
   }

	
   public int getActualNumberLateSpi() {
   
   	return actualNumberLateSpi;
   }

	
   public int getActualGranuCells() {
   
   	return actualGranuCells;
   }

   public int getActualNumberOfNoNucleus() {
   
   	return actualNumberOfNoNucleus;
   }

	
   public int getActualBasalStatisticsCells() {
   
   	return actualBasalStatisticsCells;
   }
   

	
   public int getActualNumberOfBasalStatisticsCells() {
   
   	return actualNumberOfBasalStatisticsCells;
   }
   
   public double getMeanAgeOfAllCells(){
   	
   	return (this.sumOfAllAges / this.actualNumberKCytes);
   }

	

}
