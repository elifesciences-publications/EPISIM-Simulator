package sim.app.episim.model.biomechanics.centerbased2d;

import episimmcc.EpisimModelConnector;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical2DModel;


public abstract class AbstractCenterBased2DModel extends AbstractBiomechanical2DModel{
	
	public AbstractCenterBased2DModel(){
		this(null, null);
	}	
	public AbstractCenterBased2DModel(AbstractCell cell, EpisimModelConnector modelConnector){
		super(cell, modelConnector);
	}
	
	public abstract double getStandardCellHeight();
	public abstract void setStandardCellHeight(double val);
	public abstract double getStandardCellWidth();
	public abstract void setStandardCellWidth(double val);
	public abstract double getCellHeight();
	public abstract double getCellWidth();

}
