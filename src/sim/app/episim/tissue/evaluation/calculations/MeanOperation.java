package sim.app.episim.tissue.evaluation.calculations;

import sim.app.episim.tissue.evaluation.tabledata.Cell.CellMember;
import sim.app.episim.tissue.evaluation.tabledata.CellGroup;

public class MeanOperation extends AbstractOperation {

	public MeanOperation(CellMember cellMember) {
		super(cellMember);
	}

	@Override
	public double getColumnValue(CellGroup cellgroup) {
		return mean(cellgroup);
	}

	@Override
	public String getColumnName() {
		return getMembername()+"_Mean";
	}

	
}
