package sim.app.episim.model.biomechanics;


import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.NoExport;
import sim.app.episim.AbstractCell;
import sim.engine.SimState;

public abstract class AbstractMechanicalModel<T> implements java.io.Serializable{
	
	private AbstractCell cell;
	
	public AbstractMechanicalModel(){
		this(null);
	}
	
	public AbstractMechanicalModel(AbstractCell cell){
		this.cell = cell;
	}
	
	protected AbstractCell getCell(){ return this.cell; }	
	
	
	protected abstract void clearCellField();
	protected abstract void resetCellField();
	public abstract void removeCellFromCellField();
	public abstract void setCellLocationInCellField(T location);
	public abstract T getCellLocationInCellField();
	protected abstract Object getCellField();
	
	
	
	
	public abstract EpisimModelConnector getEpisimModelConnector();
	
	
	/**
	 * This pseudo-static Method is called ONLY ONE TIME after all cell's mechanical model's newSimStep Method has been called
	 * The method is called at ONLY ONE arbitrary model instance and should implement global mechanical operations after a sim step has been calculated
	 * 
	 */
	protected abstract void newSimStepGloballyFinished(long simStepNumber, SimState state);

}
