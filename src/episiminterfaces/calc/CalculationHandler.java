package episiminterfaces.calc;

import java.util.Map;

import sim.app.episim.CellType;
import episimexceptions.CellNotValidException;
import episimexceptions.MissingObjectsException;


public interface CalculationHandler {
	
	long getID();
	long getCorrespondingBaselineCalculationHandlerID();
	int getCalculationAlgorithmID();
	Map<String, Object> getParameters();
	
	Class<? extends CellType> getRequiredCellType();
	
	boolean isBaselineValue();
	boolean conditionFulfilled(CellType cell) throws CellNotValidException;
	double calculate(CellType cell) throws CellNotValidException;

}
