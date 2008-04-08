package sim.app.episim.datamonitoring.calc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jfree.data.xy.XYSeries;

import sim.app.episim.CellType;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.util.Sorting;
import episimexceptions.CellNotValidException;
import episiminterfaces.*;



public class OneCellCalculator extends AbstractCommonCalculator{
	
	private ArrayList<CalculationHandler> calculationHandlers;
	private ArrayList<XYSeries> xySeries;
	
	
	private Map<Integer, CellType> trackedCells;
	private Map<Long, CellType> trackedCellsBaseLine;
	private Map<Integer, Integer> cellCounters;
	int seriesCounter = 0;
	private int counterInit = 0;
	protected OneCellCalculator(){
		this.calculationHandlers = new ArrayList<CalculationHandler>();
		this.xySeries = new ArrayList<XYSeries>();
		this.trackedCells = new HashMap<Integer, CellType>();
		this.trackedCellsBaseLine = new HashMap<Long, CellType>();
		this.cellCounters = new HashMap<Integer, Integer>();
	}
	
	public void restartSimulation(){
		this.trackedCells.clear();
		this.trackedCellsBaseLine.clear();
		this.cellCounters.clear();
		for(int i = 0; i < xySeries.size(); i++) this.cellCounters.put(i, counterInit);
	}
	
	public void registerForOneCellCalculation(CalculationHandler handler, XYSeries series){
		if(handler == null || series == null) throw new IllegalArgumentException("OneCellCalculator: CalculationHandler or XYSeries must not be null!");
		this.calculationHandlers.add(handler);
		this.xySeries.add(series);
		this.cellCounters.put(seriesCounter, counterInit);
		seriesCounter++;
	}
	
	
	private void checkTrackedCell(){
			boolean invalidCellFound = false;
			
			do{
				invalidCellFound = false;
				CellType actTrackedCell = null;
				CellType newTrackedCell = null;
				int counter = 0;
				for(XYSeries actSeries: this.xySeries){
					
					if(newTrackedCell == null) newTrackedCell =getNewCellForTacking(this.calculationHandlers.get(counter));
					if(newTrackedCell != null){
						actTrackedCell = this.trackedCells.get(counter);
						if(actTrackedCell == null || actTrackedCell.getEpisimCellDiffModelObject().getIsAlive() == false){
							if(this.calculationHandlers.get(counter).getRequiredCellType() == null
									|| this.calculationHandlers.get(counter).getRequiredCellType().isAssignableFrom(newTrackedCell.getClass())){
								if(actTrackedCell != null) actTrackedCell.setTracked(false);
								actSeries.clear();
								actSeries.setKey(((String)actSeries.getKey()).substring(0, (" (Cell " + cellCounters.get(counter) +")").length()) + (" (Cell " + (cellCounters.get(counter) +1)+ ")"));
								cellCounters.put(counter, (cellCounters.get(counter)+1));
								this.trackedCells.put(counter, newTrackedCell);
								newTrackedCell.setTracked(true);
							}
						}
					}
						
					counter++;
				}
			}
			while(invalidCellFound);
			
	}
	
	
	private CellType getNewCellForTacking(CalculationHandler handler){
		Class<? extends CellType> requiredClass = handler.getRequiredCellType();
		if(requiredClass == null){
			for(CellType actCell : this.allCells){
				if(actCell.isTracked()) return actCell;
			}
			
			for(CellType actCell : this.allCells){
				if(actCell.getEpisimCellDiffModelObject().getAge() < 10 && actCell.getEpisimCellDiffModelObject().getIsAlive() == true && 
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
			
				result = this.allCells.getRandomItemOfClass(requiredClass);
			}
			while(result != null && result.getEpisimCellDiffModelObject().getDifferentiation() == EpisimCellDiffModelGlobalParameters.STEMCELL && counter < this.allCells.size());
			return result;
		}
		return null;
	}
	
	
	
	private CellType findValidSeriesTrackedCellForBaseLine(CalculationHandler handler){
		if(handler != null){
			for(CellType actCell: this.trackedCells.values()){
			
		         if(handler.getRequiredCellType() == null || handler.getRequiredCellType().isAssignableFrom(actCell.getClass())){ 
		         	if(actCell.getEpisimCellDiffModelObject().getIsAlive() == true)return actCell;
		         }
		         
	       }
	         
			
		}
		return null;
	}
	
	/**
	 * 
	 * @param handler
	 * @return returns negative infinity if cell not valid
	 */
	public double calculateOneCellBaseLine(long chartId, CalculationHandler handler){
		CellType foundCell = this.trackedCellsBaseLine.get(chartId);
		if(foundCell!= null && foundCell.getEpisimCellDiffModelObject().getIsAlive() == false){
			foundCell.setTracked(false);
			foundCell = null;
		}
		if(foundCell == null) foundCell = findValidSeriesTrackedCellForBaseLine(handler);
		if(foundCell == null) foundCell = getNewCellForTacking(handler);
		try{
			
			if(foundCell != null){
				this.trackedCellsBaseLine.put(chartId, foundCell);
				return handler.calculate(foundCell);
			}
			else{
				return Double.NEGATIVE_INFINITY;
			}
      }
      catch (CellNotValidException e){
	     
	      return Double.NEGATIVE_INFINITY;
      }
     
	}
	
	public void calculateOneCell(double baseLineResult){
		checkTrackedCell();
	
		double result = 0;
		
		for(int i = 0; i < calculationHandlers.size(); i++){
			try{
				CellType trackedCell = this.trackedCells.get(i);
				if(trackedCell != null){
					result = calculationHandlers.get(i).calculate(trackedCell);
					this.xySeries.get(i).add(baseLineResult, result);
				}
			}
			catch (CellNotValidException e){
				//Exception is Ignores
			}
			
		}
	}
		
		
	

}
