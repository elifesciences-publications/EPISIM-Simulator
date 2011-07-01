package sim.app.episim.tissue.evaluation.tabledata;

public interface GroupColumn extends Column{
	double getColumnValue(CellGroup cellgroup);
	CellColumn getOriginal();
}
