package sim.app.episim.tissue.evaluation.filter;

import sim.app.episim.visualization.CellEllipse_;

public interface Condition {
	boolean match(CellEllipse_ cell);
	double getMiddleValue();
	double getMin();
	double getMax();
}