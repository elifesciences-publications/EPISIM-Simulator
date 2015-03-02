package sim.app.episim.tissueimport.evaluation.calculations;

import sim.app.episim.tissueimport.evaluation.tabledata.CellGroup;
import sim.app.episim.tissueimport.evaluation.tabledata.GroupColumn;

public class CountOperation extends AbstractOperation {

	public CountOperation() {
		super(null);
	}

	
	public double getColumnValue(CellGroup cellgroup) {
		return cellgroup.getGroupedCellCount();
	}

	
	public String getColumnName() {
		return "nCells";
	}

}
