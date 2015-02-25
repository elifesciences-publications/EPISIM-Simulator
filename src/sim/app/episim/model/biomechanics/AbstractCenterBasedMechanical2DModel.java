package sim.app.episim.model.biomechanics;

import sim.app.episim.model.AbstractCell;


public abstract class AbstractCenterBasedMechanical2DModel extends AbstractMechanical2DModel{
	
	public AbstractCenterBasedMechanical2DModel(){
		this(null);
	}	
	public AbstractCenterBasedMechanical2DModel(AbstractCell cell){
		super(cell);
	}
	
	public abstract double getStandardCellHeight();
	public abstract void setStandardCellHeight(double val);
	public abstract double getStandardCellWidth();
	public abstract void setStandardCellWidth(double val);
	public abstract double getCellHeight();
	public abstract double getCellWidth();

}
