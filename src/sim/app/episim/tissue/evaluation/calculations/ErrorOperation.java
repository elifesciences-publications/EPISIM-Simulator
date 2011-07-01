package sim.app.episim.tissue.evaluation.calculations;

import sim.app.episim.tissue.evaluation.tabledata.Cell.CellMember;
import sim.app.episim.tissue.evaluation.tabledata.CellGroup;

public class ErrorOperation extends QuantilOperation {
	
	boolean positive;

	public ErrorOperation(CellMember cellMember, double error, boolean positive) {
		super(cellMember, positive ? 1-error : error);
		this.positive = positive;
	}

	@Override
	public String getColumnName() {
		return getMembername()+ (positive ? "Pos" : "Neg") + "Err";
	}

	@Override
	public double getColumnValue(CellGroup cellgroup) {
		double mean = super.mean(cellgroup);
		return Math.abs(super.getColumnValue(cellgroup)-mean);
	}

	
}
