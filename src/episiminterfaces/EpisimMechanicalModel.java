package episiminterfaces;

import sim.util.Bag;
import sim.util.Double2D;


public interface EpisimMechanicalModel {
	
	public static final String DUMMYVERSION = "2010-05-13";
	
	
	Double2D getNewPosition();
	Double2D getOldPosition();
	double orientation2D();
	int hitsOtherCell();
	boolean nextToOuterCell();
	Bag getNeighbouringCells();
	int getKeratinoHeight();
	int getKeratinoWidth();
	void newSimStep();
}
