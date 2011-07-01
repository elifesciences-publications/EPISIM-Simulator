package sim.app.episim.tissue.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import sim.app.episim.tissue.evaluation.tabledata.AbstractTable;
import sim.app.episim.tissue.evaluation.tabledata.Column;
import sim.app.episim.visualization.CellEllipse_;
import sim.app.episim.visualization.CellEllipse_.CellMember;


public class CellCSVWriter {
	
	public static void writeTable(String path, AbstractTable table) {
		Writer fw = null;
		try {
			fw = new FileWriter(path, false);
			for (Column col : table.getColumn()) {
				String cellData = col.getColumnName();
				fw.write(cellData+";");
			}
			fw.write("\r\n");
			for (Double[] dataRow : table.getData()) {
				for (double dataField : dataRow) {
					fw.write(dataField+";");
				}
				fw.write("\r\n");
			}
			// fw.write(string+"\r\n");
		} catch (IOException e) {
			System.err.println("Could't write to file");
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	public static void writeCells(String path, CellEllipse_[] cells, CellMember... cellmembers) {
		Writer fw = null;
		try {
			fw = new FileWriter(path, false);
			for (CellMember member : cellmembers) {
				String cellData = member.getColumnName();
				fw.write(cellData+";");
			}
			fw.write("\r\n");
			for (CellEllipse_ cell : cells) {
				for (CellMember member : cellmembers) {
					double cellData = member.getColumnValue(cell);
					fw.write(cellData+";");
				}
				fw.write("\r\n");
			}
			// fw.write(string+"\r\n");
		} catch (IOException e) {
			System.err.println("Could't write to file");
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
}
