package sim.app.episim.model.biomechanics.hexagonbased;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.util.Double2D;


public abstract class AbstractHexagonBasedMechanicalModel extends AbstractMechanical2DModel{
	
	public AbstractHexagonBasedMechanicalModel(AbstractCell cell){
		super(cell);
	}
	
	public abstract boolean isSpreading();
	public abstract Double2D getLocationInMikron();
	public abstract Double2D getSpreadingLocationInMikron();
	public abstract Double2D correctToroidalSpreadingCoordinatesInMikronForEllipseDrawing();
	
}
