package sim.app.episim.tissue.evaluation.calculations;

import sim.app.episim.tissue.evaluation.tabledata.CellGroup;
import sim.app.episim.tissue.evaluation.tabledata.Cell.CellMember;

public class MeanRemoveOutlierOperation extends AbstractOperation{
	
	private double quantile1 = 0;
	private double quantile2 = 1;

	public MeanRemoveOutlierOperation(CellMember cellMember, double quantile1, double quantile2) {
		super(cellMember);
		this.quantile1 = quantile1;
		this.quantile2 = quantile2;
	}

	@Override
	public String getColumnName() {
		return getMembername() + "_Mean";
	}

	@Override
	public double getColumnValue(CellGroup cellgroup) {
		return 0; //TODO noch nicht implementiert
		//return mean(cutList(getIndexOfQuantile(cellgroup, quantile1), getIndexOfQuantile(cellgroup, quantile2), getSortedList(cellgroup)));
	}


}
