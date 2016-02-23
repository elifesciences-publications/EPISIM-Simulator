package sim.app.episim.model.biomechanics.latticebased2d;

import episimmcc.EpisimModelConnector;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical2DModel;
import sim.util.Double2D;


public abstract class AbstractLatticeBased2DModel extends AbstractBiomechanical2DModel{
	
	public AbstractLatticeBased2DModel(AbstractCell cell, EpisimModelConnector modelConnector){
		super(cell, modelConnector);
	}
	
	public abstract boolean isSpreading();
	public abstract Double2D getLocationInMikron();
	public abstract Double2D getSpreadingLocationInMikron();
	public abstract Double2D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing();
	
}
