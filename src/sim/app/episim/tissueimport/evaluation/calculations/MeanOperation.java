package sim.app.episim.tissueimport.evaluation.calculations;

import sim.app.episim.tissueimport.evaluation.tabledata.CellGroup;
import sim.app.episim.visualization.legacy.CellEllipse_.CellMember;

public class MeanOperation extends AbstractOperation {

	public MeanOperation(CellMember cellMember) {
		super(cellMember);
	}

	
	public double getColumnValue(CellGroup cellgroup) {
		return mean(cellgroup);
	}

	
	public String getColumnName() {
		return getMembername()+"_Mean";
	}

	
}
