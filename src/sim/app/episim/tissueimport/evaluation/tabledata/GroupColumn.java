package sim.app.episim.tissueimport.evaluation.tabledata;

public interface GroupColumn extends Column{
	double getColumnValue(CellGroup cellgroup);
	CellColumn getOriginal();
}
