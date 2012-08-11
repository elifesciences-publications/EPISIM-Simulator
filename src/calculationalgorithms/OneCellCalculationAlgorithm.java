package calculationalgorithms;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import calculationalgorithms.common.AbstractCommonCalculationAlgorithm;
import sim.app.episim.AbstractCell;
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
		
	public static final String CELLSEARCHINGSIMSTEPINTERVAL = "cell search simstep interval";
	protected Map<String, AbstractCell> trackedCells;
	
	protected Map<String, SingleCellObserver> observers;
	protected Map<Long, String> handlerIdStringIdMap;
	private int counter = 0;
	private long lastTimeStep = 0;	
	public OneCellCalculationAlgorithm(){		
		this.trackedCells = new HashMap<String, AbstractCell>();
		observers = new HashMap<String, SingleCellObserver>();
		handlerIdStringIdMap = new HashMap<Long, String>();
	}
	
	public void restartSimulation(){
		this.trackedCells.clear();
		counter=0;
		lastTimeStep = 0;
	}
	
	public void reset(){
		restartSimulation();
		observers.clear();
		handlerIdStringIdMap.clear();
		counter=0;
		lastTimeStep = 0;
	}
		
	protected void checkTrackedCells(CalculationHandler handler) throws CellNotValidException {

		AbstractCell actTrackedCell = null;
		AbstractCell newTrackedCell = null;
		int searchInterval = (Integer) handler.getParameters().get(OneCellCalculationAlgorithm.CELLSEARCHINGSIMSTEPINTERVAL);
      if(searchInterval < 1) searchInterval = 1;
		actTrackedCell = this.trackedCells.get(handlerIdStringIdMap.get(handler.getID()));
		if(actTrackedCell == null || actTrackedCell.getEpisimCellBehavioralModelObject().getIsAlive() == false || !handler.conditionFulfilled(actTrackedCell)){			
			
			if(actTrackedCell != null){
				this.trackedCells.remove(handlerIdStringIdMap.get(handler.getID()));
				notifySingleCellObserver(handlerIdStringIdMap.get(handler.getID()));
				actTrackedCell.setTracked(false);				
			}
			
			newTrackedCell = getAlreadyTrackedCell(handler);
			if(newTrackedCell != null && !newTrackedCell.getEpisimCellBehavioralModelObject().getIsAlive()) newTrackedCell = null;
			
			if(newTrackedCell == null && counter %searchInterval == 0) newTrackedCell = getNewCellForTracking(handler);
			
			if(newTrackedCell != null && trackedCells.get(handlerIdStringIdMap.get(handler.getID()))==null){					
				newTrackedCell.setTracked(true);
				this.trackedCells.put(handlerIdStringIdMap.get(handler.getID()), newTrackedCell);
			}			
		}		
	}
	
	protected AbstractCell getAlreadyTrackedCell(CalculationHandler handler){
		Class<? extends AbstractCell> requiredClass = handler.getRequiredCellType();
		if(requiredClass == null){
			for(AbstractCell cell: this.trackedCells.values()){
				return cell;
			}
		}
		else{
			for(AbstractCell cell: this.trackedCells.values()){
				if(requiredClass.isAssignableFrom(cell.getClass())) return cell;
			}
		}
		return null;
	}
	
	protected AbstractCell getNewCellForTracking(CalculationHandler handler){
		Class<? extends AbstractCell> requiredClass = handler.getRequiredCellType();
		if(requiredClass == null){
			for(AbstractCell actCell : this.allCells){
				if(actCell.getEpisimCellBehavioralModelObject().getIsAlive() == true) 
					return actCell;
			}
		}
		else{
			AbstractCell result = null;
			int counter = 0;
			
			do{
				counter++;
				System.out.println("Suche zufällige Zelle für Tracking passend zur Klasse: "+  requiredClass.getCanonicalName());
				result = this.allCells.getRandomItemOfClass(requiredClass);
			}
			while(result != null && counter < this.allCells.size());
			return result;
		}
		return null;
	}
	
	
	protected long getLastTimeStep(){ return this.lastTimeStep; }
	
	protected void setLastTimeStep(long lastTimeStep){ this.lastTimeStep = lastTimeStep;}
	protected void incrementCounter(){ counter++;}
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {
		if(results.getTimeStep() != lastTimeStep){
			lastTimeStep =results.getTimeStep();
			counter++;
		}
		try{
				checkTrackedCells(handler);
				AbstractCell trackedCell=null;
			
			
				
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
			public boolean hasMathematicalExpression() { return true; }

			public Map<String, Class<?>> getParameters() {
				Map<String, Class<?>> params = new HashMap<String, Class<?>>();
				params.put(OneCellCalculationAlgorithm.CELLSEARCHINGSIMSTEPINTERVAL, Integer.TYPE);
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
