package sim.app.episim.tissueimport.evaluation.calculations;

import sim.app.episim.tissueimport.evaluation.tabledata.CellGroup;
import sim.app.episim.visualization.legacy.CellEllipse_.CellMember;

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
