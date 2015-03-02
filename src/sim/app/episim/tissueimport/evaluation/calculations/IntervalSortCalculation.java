package sim.app.episim.tissueimport.evaluation.calculations;

import java.util.HashMap;

import sim.app.episim.tissueimport.evaluation.filter.ConditionInterval;
import sim.app.episim.tissueimport.evaluation.filter.Interval;
import sim.app.episim.tissueimport.evaluation.tabledata.CellColumn;
import sim.app.episim.tissueimport.evaluation.tabledata.CellGroup;
import sim.app.episim.visualization.legacy.CellEllipse_;
import sim.app.episim.visualization.legacy.CellEllipse_.CellMember;

public class IntervalSortCalculation implements CellColumn{

	private Interval startInterval;
	private CellColumn column;
	String header = null;
	public IntervalSortCalculation(CellColumn column, double intervalSize) {

		this.column = column;
		header = getColumnName()+"_IntBegin";
		startInterval = new Interval(true, 0, false, intervalSize);
	}
	
	public String getColumnName() {
		return header;
	}

	public double getColumnValue(CellEllipse_ cell) {
		return startInterval.nextMatching(column.getColumnValue(cell)).getMin();
	}
}
