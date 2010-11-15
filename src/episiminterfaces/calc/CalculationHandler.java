package episiminterfaces.calc;

import java.util.Map;

import sim.app.episim.AbstractCellType;
import episimexceptions.CellNotValidException;
import episimexceptions.MissingObjectsException;


public interface CalculationHandler {
	
	long getID();
	long getCorrespondingBaselineCalculationHandlerID();
	int getCalculationAlgorithmID();
	Map<String, Object> getParameters();
	
	Class<? extends AbstractCellType> getRequiredCellType();
	
	boolean isBaselineValue();
	boolean conditionFulfilled(AbstractCellType cell) throws CellNotValidException;
	double calculate(AbstractCellType cell) throws CellNotValidException;

}
