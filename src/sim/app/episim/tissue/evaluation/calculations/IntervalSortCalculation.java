package sim.app.episim.tissue.evaluation.calculations;

import java.util.HashMap;

import sim.app.episim.tissue.evaluation.filter.ConditionInterval;
import sim.app.episim.tissue.evaluation.filter.Interval;
import sim.app.episim.tissue.evaluation.tabledata.Cell;
import sim.app.episim.tissue.evaluation.tabledata.CellGroup;
import sim.app.episim.tissue.evaluation.tabledata.Cell.CellMember;
import sim.app.episim.tissue.evaluation.tabledata.CellColumn;

public class IntervalSortCalculation implements CellColumn{

	private Interval startInterval;
	private CellColumn column;
	String header = null;
	public IntervalSortCalculation(CellColumn column, double intervalSize) {

		this.column = column;
		header = getColumnName()+"_IntBegin";
		startInterval = new Interval(true, 0, false, intervalSize);
	}
	@Override
	public String getColumnName() {
		return header;
	}
	@Override
	public double getColumnValue(Cell cell) {
		return startInterval.nextMatching(column.getColumnValue(cell)).getMin();
	}
}
