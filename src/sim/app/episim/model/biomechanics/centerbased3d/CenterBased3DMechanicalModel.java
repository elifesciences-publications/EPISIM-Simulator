package sim.app.episim.model.biomechanics.centerbased3d;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;

import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.EpisimCellShape;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanical3DModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.util.GenericBag;
import sim.util.Double3D;


public class CenterBased3DMechanicalModel extends AbstractMechanical3DModel{

	@Override
	public void setEpisimModelConnector(EpisimModelConnector modelConnector) {

		// TODO Auto-generated method stub
		
	}

	@Override
	public GenericBag<AbstractCell> getRealNeighbours() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EpisimCellShape<Shape3D> getPolygonCell() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EpisimCellShape<Shape3D> getPolygonCell(EpisimDrawInfo<TransformGroup> info) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EpisimCellShape<Shape3D> getPolygonNucleus() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EpisimCellShape<Shape3D> getPolygonNucleus(EpisimDrawInfo<TransformGroup> info) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CellBoundaries getCellBoundariesInMikron(double sizeDelta) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newSimStep(long simStepNumber) {

		// TODO Auto-generated method stub
		
	}

	@Override
	public double getX() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getY() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getZ() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void clearCellField() {

		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCellFromCellField() {

		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCellLocationInCellField(Double3D location) {

		// TODO Auto-generated method stub
		
	}

	@Override
	public Double3D getCellLocationInCellField() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Object getCellField() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EpisimModelConnector getEpisimModelConnector() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void newSimStepGloballyFinished(long simStepNumber) {

		// TODO Auto-generated method stub
		
	}

}
