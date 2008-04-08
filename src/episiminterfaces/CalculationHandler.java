package episiminterfaces;

import sim.app.episim.CellType;
import episimexceptions.CellNotValidException;
import episimexceptions.MissingObjectsException;


public interface CalculationHandler {
	
	Class<? extends CellType> getRequiredCellType();
	double calculate(CellType cell) throws CellNotValidException;

}
