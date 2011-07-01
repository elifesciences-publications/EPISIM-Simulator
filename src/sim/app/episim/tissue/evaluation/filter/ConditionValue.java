package sim.app.episim.tissue.evaluation.filter;

import sim.app.episim.tissue.evaluation.tabledata.CellColumn;

public class ConditionValue extends ConditionInterval {
	
	public ConditionValue(CellColumn cellMember, double value) {
		super(value, cellMember, value);
	}

	public ConditionValue(CellColumn cellMember, double value, double error) {
		super(value-error, cellMember, value+error);
	}

}
