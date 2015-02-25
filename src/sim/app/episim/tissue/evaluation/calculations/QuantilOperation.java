package sim.app.episim.tissue.evaluation.calculations;

import sim.app.episim.tissue.evaluation.tabledata.CellGroup;
import sim.app.episim.visualization.legacy.CellEllipse_.CellMember;

public class QuantilOperation extends AbstractOperation{
	
	private double quantile;
	
	public QuantilOperation(CellMember cellMember, double quantile) {
		super(cellMember);
		if(quantile <= 1 && quantile >= 0)
			this.quantile = quantile;
	}

	
	public String getColumnName() {
		return getMembername()+"_"+quantile+"_Quantile";
	}

	
	public double getColumnValue(CellGroup cellgroup) {
		return quantile(cellgroup, quantile);
	}

}
