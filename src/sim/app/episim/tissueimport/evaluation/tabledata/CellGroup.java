package sim.app.episim.tissueimport.evaluation.tabledata;

import java.util.ArrayList;

import sim.app.episim.tissueimport.evaluation.filter.Condition;
import sim.app.episim.tissueimport.evaluation.filter.Filter;
import sim.app.episim.visualization.legacy.CellEllipse_;


public class CellGroup {
	
	private CellEllipse_[] groupedCells;
	private CellColumn groupedBy;
	private Condition groupCondition;
	
	public CellGroup(CellColumn groupedBy, CellTable table, Condition groupCondition) {
		this.groupedCells = table.filter(new Filter(groupCondition));
		this.groupCondition = groupCondition;
		this.groupedBy = groupedBy;
	}
	
	public double getGroupedCellCount(){
		return groupedCells.length;
	}

	public CellEllipse_[] getGroupedCells() {
		return groupedCells;
	}
	
	public Condition getGroupCondition(){
		return groupCondition;
	}

	public CellColumn getGroupedBy() {
		return groupedBy;
	}	
	
	
}


