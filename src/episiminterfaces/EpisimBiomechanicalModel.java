package episiminterfaces;

import episimbiomechanics.EpisimModelConnector;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.util.GenericBag;



public interface EpisimBiomechanicalModel<T, I> {
	
	void setEpisimModelConnector(EpisimModelConnector modelConnector);
	GenericBag<AbstractCell> getRealNeighbours();
	
	EpisimCellShape<T> getPolygonCell();
	EpisimCellShape<T> getPolygonCell(EpisimDrawInfo<I> info);
	EpisimCellShape<T> getPolygonNucleus();
	EpisimCellShape<T> getPolygonNucleus(EpisimDrawInfo<I> info);
	@NoExport
	CellBoundaries getCellBoundariesInMikron(double sizeDelta);
	void newSimStep(long simStepNumber);
	double getX();
	double getY();
	double getZ();
}
