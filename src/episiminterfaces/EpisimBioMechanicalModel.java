package episiminterfaces;

import episimbiomechanics.EpisimModelConnector;
import sim.app.episim.AbstractCell;

import sim.app.episim.util.GenericBag;
import sim.util.Double2D;


public interface EpisimBioMechanicalModel {
	
	public static final String DUMMYVERSION = "2010-05-13";
	
	void setEpisimModelConnector(EpisimModelConnector modelConnector);
	AbstractCell[] getRealNeighbours();
	Double2D getNewPosition();
	Double2D getOldPosition();
	double orientation2D();
	int hitsOtherCell();
	boolean nextToOuterCell();
	boolean isMembraneCell();
	GenericBag<AbstractCell> getNeighbouringCells();
	int getKeratinoHeight();
	int getKeratinoWidth();
	void newSimStep();
	double getX();
	double getY();
	double getZ();
}
