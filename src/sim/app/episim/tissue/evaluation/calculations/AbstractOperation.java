package sim.app.episim.tissue.evaluation.calculations;

import java.util.ArrayList;
import java.util.Collections;

import sim.app.episim.tissue.evaluation.tabledata.CellColumn;
import sim.app.episim.tissue.evaluation.tabledata.CellGroup;
import sim.app.episim.tissue.evaluation.tabledata.GroupColumn;
import sim.app.episim.visualization.CellEllipse_;
import sim.app.episim.visualization.CellEllipse_.CellMember;

public abstract class AbstractOperation implements GroupColumn {

	private CellMember cellMember;

	public AbstractOperation(CellMember cellMember) {
		this.cellMember = cellMember;
	}

	protected ArrayList<Double> getSortedList(CellGroup cellgroup) {
		ArrayList<Double> values = new ArrayList<Double>();
		if (cellMember != null && cellgroup != null) {
			for (CellEllipse_ c : cellgroup.getGroupedCells()) {
				values.add(cellMember.getColumnValue(c));
			}
			Collections.sort(values);
			return values;
		} else
			return null;
	}

	protected double quantile(CellGroup cellgroup, double alpha) {
		if(alpha <= 0  || alpha >= 1)
			return Double.NaN;
		ArrayList<Double> list = getSortedList(cellgroup);
		int size = list.size();
		double q = 0;
		
		double rank = size * alpha;
		if(Math.floor(rank) == Math.ceil(rank)){
			int iRank = (int)rank;
			q = (list.get(iRank)+list.get(iRank-1))/2.0d;
		}else{
			q = list.get((int)Math.floor(rank));
		}
		return q;
	}

	private int getIndexOfQuantile(CellGroup cellgroup, double quantile) {
		int size = getSortedList(cellgroup).size();
		double rank = size * quantile;
		int irank = (int) Math.ceil(rank);
		if (irank != (int) Math.floor(rank))
			irank--;
		return irank;

	}

	protected double mean(CellGroup cellgroup) {
		return mean(getSortedList(cellgroup));
	}

	protected double min(CellGroup cellgroup) {
		return getSortedList(cellgroup).get(0);
	}

	protected double max(CellGroup cellgroup) {
		ArrayList<Double> list = getSortedList(cellgroup);
		return list.get(list.size());
	}

	protected String getMembername() {
		return cellMember.getColumnName();
	}

	public static double mean(ArrayList<Double> list) {
		double sum = 0;
		for (double d : list)
			sum += d;
		return sum / list.size();
	}

	public static ArrayList<Double> cutList(int lowIndex, int highIndex,
			ArrayList<Double> sortedList) {
		ArrayList<Double> cutList = new ArrayList<Double>();
		for (int i = lowIndex; i <= highIndex; i++) {
			cutList.add(sortedList.get(i));
		}
		return cutList;
	}
	
	@Override
	public CellColumn getOriginal() {
		return cellMember;
	}

}
