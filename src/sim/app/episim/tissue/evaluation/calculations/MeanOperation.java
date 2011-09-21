package sim.app.episim.tissue.evaluation.calculations;

import sim.app.episim.tissue.evaluation.tabledata.CellGroup;
import sim.app.episim.visualization.CellEllipse_.CellMember;

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