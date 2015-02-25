package sim.app.episim.tissue.evaluation;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

import sim.app.episim.tissue.ImportedTissue_;
import sim.app.episim.tissue.evaluation.calculations.*;
import sim.app.episim.tissue.evaluation.gui.BoxPlot;
import sim.app.episim.tissue.evaluation.gui.Chart;
import sim.app.episim.tissue.evaluation.tabledata.*;
import sim.app.episim.tissue.xmlread.TissueImporter_;
import sim.app.episim.visualization.legacy.CellEllipse_;
import sim.app.episim.visualization.legacy.CellEllipse_.CellMember;

public class TissueEvaluator {
	CellTable table = null;

	@SuppressWarnings("unchecked")
	private TissueEvaluator(String[] files) {

		table = new CellTable(getAllCells(files));
		table.addColumn(CellMember.values());
		table.addColumn(new IntervalSortCalculation(CellMember.DIST_TO_BL_ABS,
				5));
		// CellCSVWriter.writeTable("test_celltable.csv", table);
	}

	private ImportedTissue_ loadTissue(String file) {
		return TissueImporter_.getInstance().loadTissue(new File(file));
	}

	private ArrayList<CellEllipse_> getAllCells(String... file) {
		ArrayList<CellEllipse_> cells = new ArrayList<CellEllipse_>();
		for (String s : file) {
			System.out.println(s);
			cells.addAll(loadTissue(s).getCells());
		}
		return cells;
	}

	private GroupedTable genTable1() {
		GroupColumn col1 = new MeanOperation(CellMember.AREA);
		GroupColumn col2 = new ErrorOperation(CellMember.AREA, 0.10d, false);
		GroupColumn col3 = new ErrorOperation(CellMember.AREA, 0.10d, true);
		GroupColumn col4 = new MeanOperation(CellMember.MAJOR_AXIS);
		GroupColumn col5 = new ErrorOperation(CellMember.MAJOR_AXIS, 0.10d,
				false);
		GroupColumn col6 = new ErrorOperation(CellMember.MAJOR_AXIS, 0.10d,
				true);
		GroupColumn col7 = new MeanOperation(CellMember.MINOR_AXIS);
		GroupColumn col8 = new ErrorOperation(CellMember.MINOR_AXIS, 0.10d,
				false);
		GroupColumn col9 = new ErrorOperation(CellMember.MINOR_AXIS, 0.10d,
				true);
		GroupColumn col0 = new CountOperation();

		GroupedTable grTable = table.groupBy(CellMember.DIST_TO_BL_ABS, 2,
				col1, col2, col3, col4, col5, col6, col7, col8, col9, col0);
		return grTable;
	}

	private GroupedTable genTable2() {
		GroupColumn col1 = new MeanOperation(CellMember.AREA);
		GroupColumn col2 = new QuantilOperation(CellMember.AREA, 0.05d);
		GroupColumn col3 = new QuantilOperation(CellMember.AREA, 0.5d);
		GroupColumn col4 = new QuantilOperation(CellMember.AREA, 0.95d);
		GroupColumn col5 = new MeanOperation(CellMember.MAJOR_AXIS);
		GroupColumn col6 = new QuantilOperation(CellMember.MAJOR_AXIS, 0.05d);
		GroupColumn col7 = new QuantilOperation(CellMember.MAJOR_AXIS, 0.5d);
		GroupColumn col8 = new QuantilOperation(CellMember.MAJOR_AXIS, 0.95d);
		GroupColumn col9 = new MeanOperation(CellMember.MINOR_AXIS);
		GroupColumn cola = new QuantilOperation(CellMember.MINOR_AXIS, 0.05d);
		GroupColumn colb = new QuantilOperation(CellMember.MINOR_AXIS, 0.5d);
		GroupColumn colc = new QuantilOperation(CellMember.MINOR_AXIS, 0.95d);
		GroupColumn cold = new CountOperation();

		GroupedTable grTable = table.groupBy(CellMember.DIST_TO_BL_ABS, 2,
				col1, col2, col3, col4, col5, col6, col7, col8, col9, cola,
				colb, colc, cold);
		return grTable;
	}

	private GroupedTable genTable3() {
		GroupColumn col1 = new MeanOperation(CellMember.ORIENTATION_BL);
		GroupColumn cold = new CountOperation();

		GroupedTable grTable = table.groupBy(CellMember.DIST_TO_BL_ABS, 2,
				col1, cold);
		return grTable;
	}

	private GroupedTable genTable8() {
		GroupColumn col1 = new MeanOperation(CellMember.AREA);
		GroupColumn col2 = new QuantilOperation(CellMember.AREA, 0.05d);
		GroupColumn col3 = new QuantilOperation(CellMember.AREA, 0.5d);
		GroupColumn col4 = new QuantilOperation(CellMember.AREA, 0.95d);
		GroupColumn cold = new CountOperation();

		GroupedTable grTable = table.groupBy(CellMember.DIST_TO_BL_ABS, 5,
				col1, col2, col3, col4, cold);
		return grTable;
	}

	private GroupedTable genTable4() {
		GroupColumn col1 = new MeanOperation(CellMember.ORIENTATION_BL);

		GroupedTable grTable = table
				.groupBy(CellMember.DIST_TO_BL_ABS, 5, col1);
		return grTable;
	}

	public static void main(String[] args) {

		ArrayList<String> files = new ArrayList<String>();

		TissueEvaluator te = new TissueEvaluator(files.toArray(new String[0]));

		GroupedTable grTable = te.genTable8();
		CellCSVWriter.writeTable("test_groupTable.csv", grTable);

		new Chart(grTable.getColumn()[0], grTable);
		new BoxPlot("Test", grTable.getColumn()[0], grTable,
				CellMember.ROUNDNESS);


	}
}
