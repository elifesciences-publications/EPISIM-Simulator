package sim.app.episim.datamonitoring.calc;

import sim.app.episim.CellType;
import sim.app.episim.util.GenericBag;



public abstract class AbstractCommonCalculator {
	protected GenericBag<CellType> allCells;
	public void registerCells(GenericBag<CellType> allCells){
		this.allCells = allCells;
		
	}
	
	
}
