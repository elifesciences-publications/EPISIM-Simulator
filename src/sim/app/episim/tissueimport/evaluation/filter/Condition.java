package sim.app.episim.tissueimport.evaluation.filter;

import sim.app.episim.visualization.legacy.CellEllipse_;

public interface Condition {
	boolean match(CellEllipse_ cell);
	double getMiddleValue();
	double getMin();
	double getMax();
}