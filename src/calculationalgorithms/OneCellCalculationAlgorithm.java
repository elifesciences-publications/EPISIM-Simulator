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
import episiminterfaces.calc.SingleCellObserver;
import episiminterfaces.CellDeathListener;
import episiminterfaces.calc.SingleCellObserverAlgorithm;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;


public class OneCellCalculationAlgorithm extends AbstractCommonCalculationAlgorithm implements SingleCellObserverAlgorithm, CalculationAlgorithm{
		
	private final int MINCELLAGE = 2;
	private Map<Long, CellType> trackedCells;
	
	private Map<Long, SingleCellObserver> observers;
	
		
	public OneCellCalculationAlgorithm(){		
		this.trackedCells = new HashMap<Long, CellType>();
		observers = new HashMap<Long, SingleCellObserver>();
	}
	
	public void restartSimulation(){
		this.trackedCells.clear();
	}
	
	public void reset(){
		restartSimulation();
		observers.clear();
	}
		
	private void checkTrackedCells(CalculationHandler handler) {

		CellType actTrackedCell = null;
		CellType newTrackedCell = null;		

			actTrackedCell = this.trackedCells.get(handler.getID());
			if(actTrackedCell == null || actTrackedCell.getEpisimCellDiffModelObject().getIsAlive() == false){			
				
				if(actTrackedCell != null){
					notifySingleCellObserver(handler.getID());
					actTrackedCell.setTracked(false);
				}
				
				newTrackedCell = getNewCellForTracking(handler);
				if(newTrackedCell != null){
					
					newTrackedCell.setTracked(true);
				}
				this.trackedCells.put(handler.getID(), newTrackedCell);
			}		
	}
		
	
	protected CellType getNewCellForTracking(CalculationHandler handler){
		Class<? extends CellType> requiredClass = handler.getRequiredCellType();
		if(requiredClass == null){
			for(CellType actCell : this.allCells){
				if(actCell.isTracked()) return actCell;
			}
			
			for(CellType actCell : this.allCells){
				if(actCell.getEpisimCellDiffModelObject().getAge() < MINCELLAGE && actCell.getEpisimCellDiffModelObject().getIsAlive() == true && 
						actCell.getEpisimCellDiffModelObject().getDifferentiation() != EpisimCellDiffModelGlobalParameters.STEMCELL) return actCell;
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
				
			System.out.println("Suche zufällige Zelle für Tracking passend zur Klasse: "+  requiredClass.getCanonicalName());
				result = this.allCells.getRandomItemOfClass(requiredClass);
			}
			while(result != null && result.getEpisimCellDiffModelObject().getDifferentiation() == EpisimCellDiffModelGlobalParameters.STEMCELL && counter < this.allCells.size());
			return result;
		}
		return null;
	}
	
	public void calculate(CalculationHandler handler, ResultSet<Double> results) {

		checkTrackedCells(handler);
		CellType trackedCell=null;
			
			try{
				
				trackedCell = this.trackedCells.get(handler.getID());
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

	private void notifySingleCellObserver(long id){
		if(this.observers.containsKey(id)){
			this.observers.get(id).observedCellHasChanged();
		}
	}
	
	public void addSingleCellObserver(long[] calculationHandlerIds, SingleCellObserver observer) {
		if(calculationHandlerIds != null && calculationHandlerIds.length >0){
			for(long id : calculationHandlerIds){
				this.observers.put(id, observer);
			}
		}
   }
}
