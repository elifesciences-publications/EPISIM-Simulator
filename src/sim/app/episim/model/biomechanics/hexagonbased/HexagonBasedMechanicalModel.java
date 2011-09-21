package sim.app.episim.model.biomechanics.hexagonbased;

import java.awt.Polygon;
import java.io.File;

import episimbiomechanics.EpisimModelConnector;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBasedMechanicalModelInitializer;
import sim.app.episim.util.GenericBag;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public class HexagonBasedMechanicalModel extends AbstractMechanicalModel {

	public HexagonBasedMechanicalModel(AbstractCell cell) {

	   super(cell);
	   
	   ObjectGrid2D grid2D = new ObjectGrid2D(100, 100);
	   grid2D.
	   
   }

	public void setEpisimModelConnector(EpisimModelConnector modelConnector) {

		// TODO Auto-generated method stub

	}

	public GenericBag<AbstractCell> getRealNeighbours() {

		// TODO Auto-generated method stub
		return null;
	}

	public Double2D getNewPosition() {

		// TODO Auto-generated method stub
		return null;
	}

	public Double2D getOldPosition() {

		// TODO Auto-generated method stub
		return null;
	}

	public Polygon getPolygonCell() {

		// TODO Auto-generated method stub
		return null;
	}

	public Polygon getPolygonCell(DrawInfo2D info) {

		// TODO Auto-generated method stub
		return null;
	}

	public Polygon getPolygonNucleus() {

		// TODO Auto-generated method stub
		return null;
	}

	public Polygon getPolygonNucleus(DrawInfo2D info) {

		// TODO Auto-generated method stub
		return null;
	}

	public boolean nextToOuterCell() {

		// TODO Auto-generated method stub
		return false;
	}

	public boolean isMembraneCell() {

		// TODO Auto-generated method stub
		return false;
	}

	public GenericBag<AbstractCell> getNeighbouringCells() {

		// TODO Auto-generated method stub
		return null;
	}

	public void newSimStep(long simStepNumber) {

		// TODO Auto-generated method stub

	}

	public double getX() {

		// TODO Auto-generated method stub
		return 0;
	}

	public double getY() {

		// TODO Auto-generated method stub
		return 0;
	}

	public double getZ() {

		// TODO Auto-generated method stub
		return 0;
	}

	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer() {
		return new HexagonBasedMechanicalModelInitializer();
	}
	
	public BiomechanicalModelInitializer getBiomechanicalModelInitializer(File modelInitializationFile) {
		return new HexagonBasedMechanicalModelInitializer(modelInitializationFile);
	}

}
