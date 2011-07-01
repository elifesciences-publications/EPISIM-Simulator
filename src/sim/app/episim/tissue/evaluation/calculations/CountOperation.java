package sim.app.episim.tissue.evaluation.calculations;

import sim.app.episim.tissue.evaluation.tabledata.CellGroup;
import sim.app.episim.tissue.evaluation.tabledata.GroupColumn;

public class CountOperation extends AbstractOperation {

	public CountOperation() {
		super(null);
	}

	@Override
	public double getColumnValue(CellGroup cellgroup) {
		return cellgroup.getGroupedCellCount();
	}

	@Override
	public String getColumnName() {
		return "nCells";
	}

}
