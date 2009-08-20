package calculationalgorithms.common;

import sim.app.episim.CellType;
import sim.app.episim.util.GenericBag;



public abstract class AbstractCommonCalculationAlgorithm {
	protected GenericBag<CellType> allCells;
	public void registerCells(GenericBag<CellType> allCells){
		this.allCells = allCells;		
	}
	
	
}
