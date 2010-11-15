package calculationalgorithms.common;

import sim.app.episim.AbstractCellType;
import sim.app.episim.util.GenericBag;



public abstract class AbstractCommonCalculationAlgorithm {
	protected GenericBag<AbstractCellType> allCells;
	public void registerCells(GenericBag<AbstractCellType> allCells){
		this.allCells = allCells;		
	}
	
	
}
