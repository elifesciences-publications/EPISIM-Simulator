package sim.app.episim.model.biomechanics.centerbased3d;

import episimmcc.EpisimModelConnector;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical3DModel;


public abstract class AbstractCenterBased3DModel extends AbstractBiomechanical3DModel{
	
	public AbstractCenterBased3DModel(){
		this(null, null);
	}	
	public AbstractCenterBased3DModel(AbstractCell cell, EpisimModelConnector modelConnector){
		super(cell, modelConnector);
	}
	
	public abstract double getStandardCellHeight();
	public abstract void setStandardCellHeight(double val);
	public abstract double getStandardCellWidth();
	public abstract void setStandardCellWidth(double val);
	public abstract double getStandardCellLength();
	public abstract void setStandardCellLength(double val);
	public abstract double getCellHeight();
	public abstract double getCellWidth();
	public abstract double getCellLength();

}