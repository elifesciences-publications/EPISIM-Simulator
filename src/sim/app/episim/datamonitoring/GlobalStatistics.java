package sim.app.episim.datamonitoring;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.calc.CalculationAlgorithm;
import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
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
	
	private static Semaphore sem = new Semaphore(1);
	
	private GenericBag<AbstractCell> allCells;
	
	private double sumOfAllAges = 0;
	
	private double [] dnaContents = new double[NUMBEROFBUCKETS]; 
	
	private double [] dnaContentsCumulative = new double [NUMBEROFBUCKETS];
	private double [] dnaContentsAveraged = new double [NUMBEROFBUCKETS];
	private int histogrammCounter = 0;
	
	
	private Set<CellDeathListener> globalCellDeathListener;
	
	private int allLivingCells = 0;
	
	private HashMap<EpisimCellType, Integer> cellTypeLivingCounterMap;
	private HashMap<EpisimCellType, Integer> cellTypeApoptosisCounterMap;
	private HashMap<EpisimCellType, Double> cellTypeApoptosisStatisticsMap;
	
	
	private HashMap<EpisimDifferentiationLevel, Integer> diffLevelLivingCounterMap;
	private HashMap<EpisimDifferentiationLevel, Integer> diffLevelApoptosisCounterMap;
	private HashMap<EpisimDifferentiationLevel, Double> diffLevelApoptosisStatisticsMap;
	
	private GlobalStatistics(){
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
		globalCellDeathListener = new HashSet<CellDeathListener>();
		buildCounterMaps();
	}
	
	private void buildCounterMaps(){
		EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		cellTypeLivingCounterMap = new HashMap<EpisimCellType, Integer>();
		cellTypeApoptosisCounterMap = new HashMap<EpisimCellType, Integer>();
		cellTypeApoptosisStatisticsMap = new HashMap<EpisimCellType, Double>();
		diffLevelLivingCounterMap = new HashMap<EpisimDifferentiationLevel, Integer>();
		diffLevelApoptosisCounterMap = new HashMap<EpisimDifferentiationLevel, Integer>();
		diffLevelApoptosisStatisticsMap = new HashMap<EpisimDifferentiationLevel, Double>();
		for(EpisimCellType cellType : cellTypes){
			cellTypeLivingCounterMap.put(cellType, 0);
			cellTypeApoptosisCounterMap.put(cellType, 0);
			cellTypeApoptosisStatisticsMap.put(cellType, 0d);
		}
		for(EpisimDifferentiationLevel diffLevel : diffLevels){
			diffLevelLivingCounterMap.put(diffLevel, 0);
			diffLevelApoptosisCounterMap.put(diffLevel, 0);
			diffLevelApoptosisStatisticsMap.put(diffLevel, 0d);
		}
	}
	private void resetCounterMaps(HashMap<EpisimCellType, Integer> cellTypeMap, HashMap<EpisimDifferentiationLevel, Integer> diffLevelMap){
		EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		for(EpisimCellType cellType : cellTypes){
			cellTypeMap.put(cellType, 0);
		}
		for(EpisimDifferentiationLevel diffLevel : diffLevels){
			diffLevelMap.put(diffLevel, 0);
		}
	}
	private void resetStatisticsMaps(HashMap<EpisimCellType, Double> cellTypeMap, HashMap<EpisimDifferentiationLevel, Double> diffLevelMap){
		EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
		EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
		for(EpisimCellType cellType : cellTypes){
			cellTypeMap.put(cellType, 0d);
		}
		for(EpisimDifferentiationLevel diffLevel : diffLevels){
			diffLevelMap.put(diffLevel, 0d);
		}
	}
	
	
	
	public int getNumberOfAllLivingCells(){
		return this.allLivingCells;
	}
	
	/*
	 * Calculation Algorithms can register here
	 */
	public void addCellDeathListenerCalculationAlgorithm(CellDeathListener listener){
		if(listener instanceof CalculationAlgorithm)this.globalCellDeathListener.add(listener);
	}
	
	
	

	
	public static GlobalStatistics getInstance(){
		if(instance==null){
			try{
	         sem.acquire();
	         instance = new GlobalStatistics();				
				sem.release();
         }
         catch (InterruptedException e){
	        ExceptionDisplayer.getInstance().displayException(e);
         }
				
		}
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
		return ((LASTBUCKETAMOUNT-FIRSTBUCKETAMOUNT)/ ((double)(NUMBEROFBUCKETS-2))); //TODO: Correct This
	}
	
	private void updateDataFields(int counter){
		
		reset(false);
		 
		calculateDnaAmountHistogramm(allCells);
		
		
		for(AbstractCell actCell: allCells){
			if(actCell.getEpisimCellBehavioralModelObject().getIsAlive()){
				this.allLivingCells++;
				EpisimDifferentiationLevel diffLevel = actCell.getEpisimCellBehavioralModelObject().getDiffLevel();
				EpisimCellType cellType = actCell.getEpisimCellBehavioralModelObject().getCellType();
				this.diffLevelLivingCounterMap.put(diffLevel, this.diffLevelLivingCounterMap.get(diffLevel)+1);
				this.cellTypeLivingCounterMap.put(cellType, this.cellTypeLivingCounterMap.get(cellType)+1);
			}
			
			
			  
			 
			  
			  
			  sumOfAllAges += actCell.getEpisimCellBehavioralModelObject().getAge();
		}
			if(counter == 10){
				  this.resetStatisticsMaps(cellTypeApoptosisStatisticsMap, diffLevelApoptosisStatisticsMap);
				  for(EpisimCellType cellType: this.cellTypeApoptosisCounterMap.keySet()){
					  if(this.cellTypeLivingCounterMap.get(cellType) > 0){
						  double result = ((this.cellTypeApoptosisCounterMap.get(cellType)/100d)/this.cellTypeLivingCounterMap.get(cellType))*100d;
						  cellTypeApoptosisStatisticsMap.put(cellType, result);
					  }
				  }
				  for(EpisimDifferentiationLevel diffLevel: this.diffLevelApoptosisCounterMap.keySet()){
					  if(this.diffLevelLivingCounterMap.get(diffLevel) > 0){
						  double result = ((this.diffLevelApoptosisCounterMap.get(diffLevel)/100d)/this.diffLevelLivingCounterMap.get(diffLevel))*100d;
						  diffLevelApoptosisStatisticsMap.put(diffLevel, result);
					  }
				  }		
				  this.resetCounterMaps(cellTypeApoptosisCounterMap, diffLevelApoptosisCounterMap);
			}
		
	}
	public double [] getDNAContents(){ return this.dnaContents; }
	
	private void calculateDnaAmountHistogramm(GenericBag<AbstractCell> allCells)
	{
		double intervalSize = getBucketIntervalSize();
		//System.out.println(intervalSize);
		
		for(AbstractCell actCell : allCells){
			//if(actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.TACELL ||
			//		actCell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL){
				double dnaContent = actCell.getEpisimCellBehavioralModelObject().getDnaContent();
				for(int i = 0; i < dnaContents.length; i++){
					if(dnaContent <= (FIRSTBUCKETAMOUNT + i * intervalSize)){ 
						dnaContents[i] += 1;
						dnaContentsCumulative [i] += 1;
						break;
					}
					//	System.out.println("Act Value: " + (FIRSTBUCKETAMOUNT + i * intervalSize));
				}
			//}
		}
		histogrammCounter++;
		//Calculate averaged histogramm
		for(int i = 0; i < dnaContentsAveraged.length; i++){
			dnaContentsAveraged[i] = dnaContentsCumulative[i] / histogrammCounter;
		}
		
	}	
	public double[] getDNAContentsAveraged(){ return this.dnaContentsAveraged; }	
	
	

	
	public void reset(boolean isRestartReset){
		
		
		sumOfAllAges = 0;		
		this.allLivingCells =0;
		dnaContents = new double[NUMBEROFBUCKETS];
		dnaContentsAveraged = new double [NUMBEROFBUCKETS];
		
	
		
		if(isRestartReset){		
			buildCounterMaps();
					
			dnaContentsCumulative = new double [NUMBEROFBUCKETS];
			histogrammCounter = 0;	
		}
		else{
			resetCounterMaps(this.cellTypeLivingCounterMap, this.diffLevelLivingCounterMap);
		}
	}


	public void cellIsDead(AbstractCell cell) {
		notifyAllGlobalCellDeathListener(cell);
		if(cell instanceof UniversalCell){
		  	UniversalCell kcyte = (UniversalCell) cell;
		  	
		  	EpisimCellType cellType =  kcyte.getEpisimCellBehavioralModelObject().getCellType();
		  	EpisimDifferentiationLevel diffLevel =  kcyte.getEpisimCellBehavioralModelObject().getDiffLevel();
		  	this.cellTypeApoptosisCounterMap.put(cellType, this.cellTypeApoptosisCounterMap.get(cellType)+1);
		  	this.diffLevelApoptosisCounterMap.put(diffLevel, this.diffLevelApoptosisCounterMap.get(diffLevel)+1);
	 	}   
   }
	
	private void notifyAllGlobalCellDeathListener(AbstractCell cell){
		for(CellDeathListener listener: globalCellDeathListener){
			listener.cellIsDead(cell);
		}
	}
	
 
  
   public double getMeanAgeOfAllCells(){ return (this.sumOfAllAges / this.allLivingCells); }
  
	
   public void classLoaderHasChanged() {
	   instance = null;
   }


	
	public HashMap<EpisimCellType, Integer> getCellTypeLivingCounterMap() {
	
		return cellTypeLivingCounterMap;
	}


	
	public HashMap<EpisimCellType, Integer> getCellTypeApoptosisCounterMap() {
	
		return cellTypeApoptosisCounterMap;
	}
	
	public HashMap<EpisimCellType, Double> getCellTypeApoptosisStatisticsMap() {
		
		return cellTypeApoptosisStatisticsMap;
	}


	
	public HashMap<EpisimDifferentiationLevel, Integer> getDiffLevelLivingCounterMap() {
	
		return diffLevelLivingCounterMap;
	}


	
	public HashMap<EpisimDifferentiationLevel, Integer> getDiffLevelApoptosisCounterMap() {
	
		return diffLevelApoptosisCounterMap;
	}
	public HashMap<EpisimDifferentiationLevel, Double> getDiffLevelApoptosisStatisticsMap() {
		
		return diffLevelApoptosisStatisticsMap;
	}  
}
