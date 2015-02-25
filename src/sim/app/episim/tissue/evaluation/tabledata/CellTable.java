package sim.app.episim.tissue.evaluation.tabledata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import sim.app.episim.tissue.evaluation.filter.ConditionInterval;
import sim.app.episim.tissue.evaluation.filter.ConditionValue;
import sim.app.episim.tissue.evaluation.filter.Filter;
import sim.app.episim.tissue.evaluation.filter.Interval;
import sim.app.episim.visualization.legacy.CellEllipse_;

public class CellTable extends AbstractTable{

	private CellEllipse_[] cellData;
	private Filter[] filter = new Filter[0];
	private CellColumn[] column = new CellColumn[0];
	
	public CellTable(ArrayList<CellEllipse_>... initialCellData) {
		ArrayList<CellEllipse_> cellData = new ArrayList<CellEllipse_>();
		for (ArrayList<CellEllipse_> cells : initialCellData) {
			cellData.addAll(cells);
		}
		this.cellData = cellData.toArray(new CellEllipse_[0]);
	}
	
	private CellGroup[] sortCellGroups(HashMap<Double, CellGroup> cellgroups){
		CellGroup[] sorted = new CellGroup[cellgroups.size()];
		
		ArrayList<Double> list = new ArrayList<Double>();
		list.addAll(cellgroups.keySet());
		Collections.sort(list);
		int i = 0;
		for(double d : list){
			sorted[i++] = cellgroups.get(d);
		}
		
		return sorted;
	}
	
	public void addIntervalColumn(final CellColumn column,double intervalSize){
		
		CellColumn valueCol = new CellColumn() {
			
			
			public String getColumnName() {
				return column.getColumnName()+"_IntBegin";
			}

			
			public double getColumnValue(CellEllipse_ cell) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		
		HashMap<Double, CellGroup> cellGroups = new HashMap<Double, CellGroup>(); 
		Interval start = new Interval(true, 0, false, intervalSize);
		for(CellEllipse_ c : cellData){
			double value = column.getColumnValue(c);
			CellGroup cells;
			cells = cellGroups.get(value);
			if(cells == null){
				Interval matching = start.nextMatching(value);
				cells = new CellGroup(column, this, new ConditionInterval(matching.getMin(), column, matching.getMax()));
				cellGroups.put(matching.getMean(), cells);
			}
		}
	}
	
	public GroupedTable groupBy(final CellColumn column,double intervalSize, Collection<GroupColumn> groupColumns){
		return groupBy(column, intervalSize, groupColumns.toArray(new GroupColumn[0]));
	}
	
	public GroupedTable groupBy(final CellColumn column,double intervalSize, GroupColumn... groupColumns){
		GroupedTable groupedTable = null;
		HashMap<Double, CellGroup> cellGroups = new HashMap<Double, CellGroup>(); 
		Interval start = new Interval(true, 0, false, intervalSize);
		for(CellEllipse_ c : cellData){
			double value = column.getColumnValue(c);
			Interval matching = start.nextMatching(value);
			CellGroup cells;
			cells = cellGroups.get(matching.getMin());
			if(cells == null){
				
				cells = new CellGroup(column, this, new ConditionInterval(matching.getMin(), column, matching.getMax()));
				cellGroups.put(matching.getMin(), cells);
			}
		}
		groupedTable = new GroupedTable(sortCellGroups(cellGroups));
		GroupColumn valueCol = new GroupColumn() {
			
			
			public String getColumnName() {
				return column.getColumnName()+"_IntBegin";
			}
			
			
			public double getColumnValue(CellGroup cellgroup) {
				return cellgroup.getGroupCondition().getMin();
			}

			
			public CellColumn getOriginal() {
				// TODO Auto-generated method stub
				return column;
			}
		};
		
		groupedTable.addColumn(valueCol);
		groupedTable.addColumn(groupColumns);
		return groupedTable;
	}
	
	public GroupedTable groupBy(final CellColumn column, GroupColumn... groupColumns){
		return groupBy(column, 0,groupColumns);
	}

	public CellColumn[] getColumn() {
		return column;
	}

	public void setColumn(CellColumn... column) {
		this.column = column;
	}

	public void addColumn(CellColumn... column) {
		if(column.length != 0)
		this.column = mergeArrays(this.column, column, CellColumn.class);
	}
	
	
	private static CellEllipse_[] filter(CellEllipse_[] cellList, Filter filter) {
		ArrayList<CellEllipse_> filteredCells = new ArrayList<CellEllipse_>();
		for (CellEllipse_ c : cellList) {
			if (filter.match(c))
				filteredCells.add(c);
		}
		return filteredCells.toArray(new CellEllipse_[0]);
	}

	public CellEllipse_[] filter(Filter... filter) {
		CellEllipse_[] filteredCells = cellData;
		for (Filter f : filter) {
			filteredCells = filter(filteredCells, f);
		}
		return filteredCells;
	}

	private CellEllipse_[] filter() {
		return filter(getFilter());
	}
	
	public Filter[] getFilter() {
		return filter;
	}

	public void setFilter(Filter... filter) {
		this.filter = filter;
	}

	public void addFilter(Filter... filter) {
		if(filter.length != 0)
		this.filter = mergeArrays(this.filter, filter, Filter.class);
	}
	
	public ArrayList<Double[]> getData() {
		ArrayList<Double[]> tableData = new ArrayList<Double[]>();
		CellEllipse_[] filteredCells = filter();
		for (CellEllipse_ cell : filteredCells) {
			CellColumn[] column = getColumn();
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
