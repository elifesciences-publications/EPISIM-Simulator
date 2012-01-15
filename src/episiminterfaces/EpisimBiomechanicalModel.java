package episiminterfaces;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.File;

import episimbiomechanics.EpisimModelConnector;
import sim.app.episim.AbstractCell;

import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public interface EpisimBiomechanicalModel {
	
	void setEpisimModelConnector(EpisimModelConnector modelConnector);
	GenericBag<AbstractCell> getRealNeighbours();
	
	Shape getPolygonCell();
	Shape getPolygonCell(DrawInfo2D info);
	Shape getPolygonNucleus();
	Shape getPolygonNucleus(DrawInfo2D info);
	CellBoundaries getCellBoundariesInMikron();
	void newSimStep(long simStepNumber);
	double getX();
	double getY();
	double getZ();
}
