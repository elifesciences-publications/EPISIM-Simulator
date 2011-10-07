package episiminterfaces;

import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.io.File;

import episimbiomechanics.EpisimModelConnector;
import sim.app.episim.AbstractCell;

import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.util.GenericBag;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public interface EpisimBiomechanicalModel {
	
	void setEpisimModelConnector(EpisimModelConnector modelConnector);
	GenericBag<AbstractCell> getRealNeighbours();
	
	Polygon getPolygonCell();
	Polygon getPolygonCell(DrawInfo2D info);
	Polygon getPolygonNucleus();
	Polygon getPolygonNucleus(DrawInfo2D info);
	void newSimStep(long simStepNumber);
	double getX();
	double getY();
	double getZ();
}
