package sim.app.episim.tissue.evaluation.filter;

import sim.app.episim.tissue.evaluation.tabledata.CellColumn;
import sim.app.episim.visualization.CellEllipse_;
import sim.app.episim.visualization.CellEllipse_.CellMember;

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

	
	public boolean match(CellEllipse_ cell) {
		if (cellMember == null)
			return true;
		boolean matches;
		matches = condition.includes(cellMember.getColumnValue(cell));
		if (invert)
			return !matches;
		else
			return matches;
	}

	
	public double getMiddleValue() {
		return condition.getMean();
	}

	
	public double getMin() {
		return condition.getMin();
	}

	
	public double getMax() {
		return condition.getMax();
	}
}
