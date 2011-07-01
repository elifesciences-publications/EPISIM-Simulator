package sim.app.episim.tissue.evaluation.filter;

import sim.app.episim.tissue.evaluation.tabledata.Cell;

public interface Condition {
	boolean match(Cell cell);
	double getMiddleValue();
	double getMin();
	double getMax();
}
