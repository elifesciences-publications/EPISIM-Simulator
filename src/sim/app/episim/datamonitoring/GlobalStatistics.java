package sim.app.episim.datamonitoring;


public class GlobalStatistics {
	private static GlobalStatistics instance;
	
	
	private int actualNumberStemCells=0;        // Stem cells
	private int actualNumberKCytes=0;      // num of kcytes that are not in nirvana
	private int actualNumberEarlySpiCells=0;         // Spinosum
	private int actualNumberTASells=0;          // TA Cells
	private int actualNumberLateSpi=0;     // Late Spinosum
	private int actualGranu=0;       // num of Granulosum KCytes
	private int actualNumberCorneum=0;       // num of Granulosum KCytes
	private int actualNumberOfNoNucleus=0;   // Cells after lifetime but not shed from the surface
	private int actualBasalStatisticsCells=0;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	
	private int actualNumberOfKCytes = 0;
	private int actualNumberOfEarlySpiCells = 0;
	private int actualNumberOfLateSpiCells = 0;
	private int actualNumberOfGranuCells = 0;
	
	private int actualNumberOfBasalStatisticsCells = 0;
	
	private GlobalStatistics(){
		
	}
	
	public static synchronized GlobalStatistics getInstance(){
		if(instance == null) instance = new GlobalStatistics();
		return instance;
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
		actualNumberTASells++;          // TA Cells
	}
	public void inkrementActualNumberLateSpi(){
		actualNumberLateSpi++;     // Late Spinosum
	}
	public void inkrementActualGranu(){
		actualGranu++;       // num of Granulosum KCytes
	}
	public void inkrementActualNumberCorneum(){
		actualNumberCorneum++;       // num of Granulosum KCytes
	}
	public void inkrementActualNumberOfNoNucleus(){
		actualNumberOfNoNucleus++;   // Cells after lifetime but not shed from the surface
	}
	public void inkrementActualBasalStatisticsCells(){
		actualBasalStatisticsCells++;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	}
	
	public void inkrementActualNumberOfKCytes(){
		actualNumberOfKCytes++;
	}
	public void inkrementActualNumberOfEarlySpiCells(){
		actualNumberOfEarlySpiCells++;
	}
	public void inkrementActualNumberOfLateSpiCells(){
		actualNumberOfLateSpiCells++;
	}
	public void inkrementActualNumberOfGranuCells(){
		actualNumberOfGranuCells++;
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
		actualNumberTASells--;          // TA Cells
	}
	public void dekrementActualNumberLateSpi(){
		actualNumberLateSpi--;     // Late Spinosum
	}
	public void dekrementActualGranu(){
		actualGranu--;       // num of Granulosum KCytes
	}
	public void dekrementActualNumberCorneum(){
		actualNumberCorneum--;       // num of Granulosum KCytes
	}
	public void dekrementActualNumberOfNoNucleus(){
		actualNumberOfNoNucleus--;   // Cells after lifetime but not shed from the surface
	}
	public void dekrementActualBasalStatisticsCells(){
		actualBasalStatisticsCells--;   // Cells which have the Flag isBasalStatisticsCell (ydist<10 from basal membrane)
	}
	
	public void dekrementActualNumberOfKCytes(){
		actualNumberOfKCytes--;
	}
	public void dekrementActualNumberOfEarlySpiCells(){
		actualNumberOfEarlySpiCells--;
	}
	public void dekrementActualNumberOfLateSpiCells(){
		actualNumberOfLateSpiCells--;
	}
	public void dekrementActualNumberOfGranuCells(){
		actualNumberOfGranuCells--;
	}
	
	public void dekrementActualNumberOfBasalStatisticsCells(){
		actualNumberOfBasalStatisticsCells--;
	}
	
	public void reset(){
		actualNumberStemCells=0;       
		actualNumberKCytes=0;      
		actualNumberEarlySpiCells=0;         
		actualNumberTASells=0;          
		actualNumberLateSpi=0;     
		actualGranu=0;      
		actualNumberCorneum=0;       
		actualNumberOfNoNucleus=0;   
		actualBasalStatisticsCells=0;   
		
		actualNumberOfKCytes = 0;
		actualNumberOfEarlySpiCells = 0;
		actualNumberOfLateSpiCells = 0;
		actualNumberOfGranuCells = 0;
		actualNumberOfBasalStatisticsCells = 0;
	
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

	
   public int getActualNumberTASells() {
   
   	return actualNumberTASells;
   }

	
   public int getActualNumberLateSpi() {
   
   	return actualNumberLateSpi;
   }

	
   public int getActualGranu() {
   
   	return actualGranu;
   }

	
   public int getActualNumberCorneum() {
   
   	return actualNumberCorneum;
   }

	
   public int getActualNumberOfNoNucleus() {
   
   	return actualNumberOfNoNucleus;
   }

	
   public int getActualBasalStatisticsCells() {
   
   	return actualBasalStatisticsCells;
   }

	
   public int getActualNumberOfKCytes() {
   
   	return actualNumberOfKCytes;
   }

	
   public int getActualNumberOfEarlySpiCells() {
   
   	return actualNumberOfEarlySpiCells;
   }

	
   public int getActualNumberOfLateSpiCells() {
   
   	return actualNumberOfLateSpiCells;
   }

	
   public int getActualNumberOfGranuCells() {
   
   	return actualNumberOfGranuCells;
   }

	
   public int getActualNumberOfBasalStatisticsCells() {
   
   	return actualNumberOfBasalStatisticsCells;
   }

	

}
