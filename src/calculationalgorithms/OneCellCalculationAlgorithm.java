package calculationalgorithms;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.calc.CalculationDataManager;
import sim.app.episim.util.ResultSet;
import episimexceptions.CellNotValidException;
import episiminterfaces.*;
import episiminterfaces.calc.CalculationAlgorithm;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationHandler;
import episiminterfaces.CellDeathListener;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import episiminterfaces.calc.marker.SingleCellObserver;
import episiminterfaces.calc.marker.SingleCellObserverAlgorithm;


public class OneCellCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements SingleCellObserverAlgorithm, CalculationAlgorithm{
		
	private final int MINCELLAGE = 2;
	private Map<String, CellType> trackedCells;
	
	private Map<String, SingleCellObserver> observers;
	private Map<Long, String> handlerIdStringIdMap;
		
	public OneCellCalculationAlgorithm(){		
		this.trackedCells = new HashMap<String, CellType>();
		observers = new HashMap<String, SingleCellObserver>();
		handlerIdStringIdMap = new HashMap<Long, String>();
	}
	
	public void restartSimulation(){
		this.trackedCells.clear();
	}
	
	public void reset(){
		restartSimulation();
		observers.clear();
		handlerIdStringIdMap.clear();
	}
		
	private void checkTrackedCells(CalculationHandler handler) {

		CellType actTrackedCell = null;
		CellType newTrackedCell = null;		

			actTrackedCell = this.trackedCells.get(handlerIdStringIdMap.get(handler.getID()));
			if(actTrackedCell == null || actTrackedCell.getEpisimCellBehavioralModelObject().getIsAlive() == false){			
				
				if(actTrackedCell != null){
					notifySingleCellObserver(handlerIdStringIdMap.get(handler.getID()));
					actTrackedCell.setTracked(false);
				}
				
				newTrackedCell = getNewCellForTracking(handler);
				if(newTrackedCell != null){
					
					newTrackedCell.setTracked(true);
				}
				if(newTrackedCell == null) this.trackedCells.remove(handlerIdStringIdMap.get(handler.getID()));
				else this.trackedCells.put(handlerIdStringIdMap.get(handler.getID()), newTrackedCell);
			}		
	}
		
	
	protected CellType getNewCellForTracking(CalculationHandler handler){
		Class<? extends CellType> requiredClass = handler.getRequiredCellType();
		if(requiredClass == null){
			for(CellType actCell : this.allCells){
				if(actCell.isTracked()) return actCell;
			}
			
			for(CellType actCell : this.allCells){
				if(actCell.getEpisimCellBehavioralModelObject().getAge() < MINCELLAGE && actCell.getEpisimCellBehavioralModelObject().getIsAlive() == true && 
						actCell.getEpisimCellBehavioralModelObject().getDifferentiation() != EpisimCellBehavioralModelGlobalParameters.STEMCELL) return actCell;
			}
		}
		else{
			CellType result = null;
			int counter = 0;
			
			for(CellType actCell : this.allCells){
				if(actCell.isTracked() && requiredClass.isAssignableFrom(actCell.getClass())) return actCell;
			}
			
			
			do{
				counter++;
				
			System.out.println("Suche zuf�llige Zelle f�r Tracking passend zur Klasse: "+  requiredClass.getCanonicalName());
				result = this.allCells.getRandomItemOfClass(requiredClass);
			}
			while(result != null && result.getEpisimCellBehavioralModelObject().getDifferentiation() == EpisimCellBehavioralModelGlobalParameters.STEMCELL && counter < this.allCells.size());
			return result;
		}
		return null;
	}
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {

		checkTrackedCells(handler);
		CellType trackedCell=null;
			
			try{
				
				trackedCell = this.trackedCells.get(handlerIdStringIdMap.get(handler.getID()));
				if(trackedCell != null){
					results.add1DValue(handler.calculate(trackedCell));
				}	
			}
			catch (CellNotValidException e){
				System.out.println("This exception should never occur: " + e.getClass().toString() + " - "+ e.getMessage());
			} 
   }

	public CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id) {

		final int _id = id;
	   
	   return new CalculationAlgorithmDescriptor(){

			public String getDescription() {				
	         return "This algorithm allows to observe a single cell. For oberservation the youngest cell in the tissue is automatically selected by the algorithm.";
         }

			public int getID() { return _id; }

			public String getName() { return "Single Cell Oberserver Unconditioned"; }

			public CalculationAlgorithmType getType(){ return CalculationAlgorithmType.ONEDIMRESULT; }

			public boolean hasCondition() { return false; }

			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
				
	         return params;
         }	   	
	   };
   }

	private void notifySingleCellObserver(String id){
		if(this.observers.containsKey(id)){
			this.observers.get(id).observedCellHasChanged();
		}
	}
	
	public void addSingleCellObserver(long[] calculationHandlerIds, SingleCellObserver observer) {
		if(calculationHandlerIds != null && calculationHandlerIds.length >0){
			String stringId = "";
			for(long id : calculationHandlerIds)stringId+=id;
			for(long id : calculationHandlerIds) this.handlerIdStringIdMap.put(id, stringId);
				
			
			this.observers.put(stringId, observer);
		}
   }
}
