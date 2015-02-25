package episiminterfaces.calc;

import java.util.Map;

import sim.app.episim.model.AbstractCell;
import episimexceptions.CellNotValidException;
import episimexceptions.MissingObjectsException;


public interface CalculationHandler {
	
	long getID();
	long getCorrespondingBaselineCalculationHandlerID();
	int getCalculationAlgorithmID();
	Map<String, Object> getParameters();
	
	Class<? extends AbstractCell> getRequiredCellType();
	
	boolean isBaselineValue();
	boolean conditionFulfilled(AbstractCell cell) throws CellNotValidException;
	double calculate(AbstractCell cell) throws CellNotValidException;

}
