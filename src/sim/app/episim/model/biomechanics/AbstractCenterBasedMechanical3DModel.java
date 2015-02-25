package sim.app.episim.model.biomechanics;

import sim.app.episim.model.AbstractCell;


public abstract class AbstractCenterBasedMechanical3DModel extends AbstractMechanical3DModel{
	
	public AbstractCenterBasedMechanical3DModel(){
		this(null);
	}	
	public AbstractCenterBasedMechanical3DModel(AbstractCell cell){
		super(cell);
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