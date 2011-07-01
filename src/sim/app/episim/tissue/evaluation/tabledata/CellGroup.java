package sim.app.episim.tissue.evaluation.tabledata;

import java.util.ArrayList;

import sim.app.episim.tissue.evaluation.filter.Condition;
import sim.app.episim.tissue.evaluation.filter.Filter;


public class CellGroup {
	
	private Cell[] groupedCells;
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

	public Cell[] getGroupedCells() {
		return groupedCells;
	}
	
	public Condition getGroupCondition(){
		return groupCondition;
	}

	public CellColumn getGroupedBy() {
		return groupedBy;
	}	
	
	
}


