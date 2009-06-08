package sim.app.episim.datamonitoring.calc;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;
import sim.app.episim.CellType;
import episimexceptions.CellNotValidException;
import episiminterfaces.*;
import episiminterfaces.calc.CalculationHandler;



public class OneCellCalculator extends AbstractCommonCalculator{
	
	private Map<Long, CalculationHandler> calculationHandlers;
   private Map<Long, OneCellTrackingDataManager<Double,Double>> dataManagers;
	
	private final int MINCELLAGE = 2;
	private Map<Long, CellType> trackedCells;
		
	protected OneCellCalculator(){
		this.calculationHandlers = new HashMap<Long, CalculationHandler>();
		this.dataManagers = new HashMap<Long, OneCellTrackingDataManager<Double,Double>>();
		this.trackedCells = new HashMap<Long, CellType>();
	}
	
	public void restartSimulation(){
		this.trackedCells.clear();
		for(OneCellTrackingDataManager<Double,Double> manager : dataManagers.values()){ 
			
			if(manager != null) manager.restartSimulation();
			
		}
	}
	
	public void registerForOneCellCalculation(CalculationHandler handler, OneCellTrackingDataManager<Double,Double> datamanager){
		if(handler == null) throw new IllegalArgumentException("OneCellCalculator: CalculationHandler must not be null!");
		this.calculationHandlers.put(handler.getID(),handler);
		this.dataManagers.put(handler.getID(),datamanager);
	}
	
	
	private void checkTrackedCells() {

		CellType actTrackedCell = null;
		CellType newTrackedCell = null;

		for(CalculationHandler handler : this.calculationHandlers.values()){

			actTrackedCell = this.trackedCells.get(handler.getID());
			if(actTrackedCell == null || actTrackedCell.getEpisimCellDiffModelObject().getIsAlive() == false){
				
				
				
				if(actTrackedCell != null){
					if(dataManagers.get(handler.getID())!= null)dataManagers.get(handler.getID()).cellHasChanged();
					actTrackedCell.setTracked(false);
				}

				
				newTrackedCell = getNewCellForTracking(handler);
				if(newTrackedCell != null &&(handler.getRequiredCellType() == null
				      || handler.getRequiredCellType().isAssignableFrom(newTrackedCell.getClass()))){
					
					newTrackedCell.setTracked(true);
				}
				this.trackedCells.put(handler.getID(), newTrackedCell);
			}
		}
	}
		
	
	private CellType getNewCellForTracking(CalculationHandler handler){
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
			//	System.out.println("Suche zufällige Zelle für Tracking passend zur Klasse: "+  requiredClass.getCanonicalName());
				result = this.allCells.getRandomItemOfClass(requiredClass);
			}
			while(result != null && result.getEpisimCellDiffModelObject().getDifferentiation() == EpisimCellDiffModelGlobalParameters.STEMCELL && counter < this.allCells.size());
			return result;
		}
		return null;
	}
	
	
	
	public double calculateOneCellBaseline(long valueHandlerID){
		checkTrackedCells();
		CellType trackedCellBaseLine=null;
		
			trackedCellBaseLine = this.trackedCells.get(valueHandlerID);
			if(trackedCellBaseLine != null){
				try{
	            return calculationHandlers.get(valueHandlerID).calculate(trackedCellBaseLine);
            }
            catch (CellNotValidException e){
            	System.out.println("This exception should never occur: " + e.getClass().toString() + " - "+ e.getMessage());
            }			
			}
		
		return Double.NEGATIVE_INFINITY;
	}
	
	
	public void calculateOneCell(double baselineResult, long valueHandlerID){
		if(baselineResult == Double.NEGATIVE_INFINITY) checkTrackedCells();
	
		double result = 0;
		
		CellType trackedCell=null;
		
		
			try{
				
				trackedCell = this.trackedCells.get(valueHandlerID);
				if(trackedCell != null){
					result = calculationHandlers.get(valueHandlerID).calculate(trackedCell);
					//if(baseLineResult != Double.NEGATIVE_INFINITY)
					//TODO: Wechsel der Zelle beim Rausschreiben in CSV kenntlich machen
					this.dataManagers.get(valueHandlerID).addNewValue(baselineResult, result);
				}
					
					
			}
			catch (CellNotValidException e){
				System.out.println("This exception should never occur: " + e.getClass().toString() + " - "+ e.getMessage());
			}
			
		
	}
	
	public void calculateOneCell(long secondValueHandlerID){
		calculateOneCell(Double.NEGATIVE_INFINITY,secondValueHandlerID);
	}
	

}
