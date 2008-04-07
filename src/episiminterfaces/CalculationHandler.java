package episiminterfaces;

import sim.app.episim.CellType;
import episimexceptions.CellNotValidException;
import episimexceptions.MissingObjectsException;


public interface CalculationHandler {
	
	
	double calculate(CellType cell) throws CellNotValidException;

}
