package sim.app.episim.tissue.evaluation.filter;

import sim.app.episim.tissue.evaluation.tabledata.Cell;
import sim.app.episim.tissue.evaluation.tabledata.Cell.CellMember;
import sim.app.episim.tissue.evaluation.tabledata.CellColumn;

public class ConditionInterval implements Condition {

	private CellColumn cellMember;
	private Interval condition;
	private boolean invert = false;

	public ConditionInterval(double min, CellColumn cellMember, double max) {
		this(min, cellMember, max, false);
	}

	public ConditionInterval(double min, CellColumn cellMember, double max,
			boolean invert) {
		condition = new Interval(min, max);
		this.cellMember = cellMember;
		this.invert = invert;
	}

	public ConditionInterval(double min, CellColumn cellMember) {
		this(min, cellMember, Double.POSITIVE_INFINITY);
	}

	public ConditionInterval(CellMember cellMember, double max) {
		this(Double.NEGATIVE_INFINITY, cellMember, max);
	}

	@Override
	public boolean match(Cell cell) {
		if (cellMember == null)
			return true;
		boolean matches;
		matches = condition.includes(cellMember.getColumnValue(cell));
		if (invert)
			return !matches;
		else
			return matches;
	}

	@Override
	public double getMiddleValue() {
		return condition.getMean();
	}

	@Override
	public double getMin() {
		return condition.getMin();
	}

	@Override
	public double getMax() {
		return condition.getMax();
	}
}
