package calculationalgorithms.common;

import sim.app.episim.AbstractCell;
import sim.app.episim.util.GenericBag;



public abstract class AbstractCommonCalculationAlgorithm {
	protected GenericBag<AbstractCell> allCells;
	public void registerCells(GenericBag<AbstractCell> allCells){
		this.allCells = allCells;		
	}
	
	
}
