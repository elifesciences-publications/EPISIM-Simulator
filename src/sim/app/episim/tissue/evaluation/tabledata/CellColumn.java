package sim.app.episim.tissue.evaluation.tabledata;

import sim.app.episim.visualization.legacy.CellEllipse_;

public interface CellColumn extends Column{
double getColumnValue(CellEllipse_ cell);
}
