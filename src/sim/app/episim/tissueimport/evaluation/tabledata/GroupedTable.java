package sim.app.episim.tissueimport.evaluation.tabledata;

import java.util.ArrayList;

public class GroupedTable extends AbstractTable{
	
	private CellGroup[] cellGroupData;
	private GroupColumn[] columns = new GroupColumn[0];
	
	public GroupedTable(CellGroup... initialCellGroupData) {
		this.cellGroupData = initialCellGroupData;
	}
	
	public GroupColumn[] getColumn() {
		return columns;
	}

	public void setColumn(GroupColumn... columns) {
		this.columns = columns;
	}

	public void addColumn(GroupColumn... columns) {
		if(columns.length != 0)
		this.columns = mergeArrays(this.columns, columns, GroupColumn.class);
	}
	
	public CellGroup[] getCellGroupData(){
		return cellGroupData;
	}

	
	public ArrayList<Double[]> getData() { 
		ArrayList<Double[]> tableData = new ArrayList<Double[]>();
		for (CellGroup cell : cellGroupData) {
			GroupColumn[] column = getColumn();
			Double[] row = new Double[column.length];
			for (int i = 0; i < column.length; i++) {
				if (column[i] != null) {
					row[i] = column[i].getColumnValue(cell);
				}
			} tableData.add(row);
		}

		return tableData;
	}

}
