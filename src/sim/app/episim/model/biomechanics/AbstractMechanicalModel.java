package sim.app.episim.model.biomechanics;

import java.awt.geom.GeneralPath;
import java.io.File;

import episiminterfaces.EpisimBiomechanicalModel;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;


public abstract class AbstractMechanicalModel implements EpisimBiomechanicalModel{
	
	private AbstractCell cell;
	
	public AbstractMechanicalModel(){
		this(null);
	}
	
	public AbstractMechanicalModel(AbstractCell cell){
		this.cell = cell;
	}
	
	protected AbstractCell getCell(){ return this.cell; }	
	
	public abstract void setLastDrawInfo2D(DrawInfo2D info);
	protected abstract void removeCellsInWoundArea(GeneralPath woundArea);
	protected abstract void clearCellField();
	public abstract void removeCellFromCellField();
	public abstract void setCellLocationInCellField(Double2D location);
	public abstract Double2D getCellLocationInCellField();
	protected abstract Object getCellField();
	protected abstract void setReloadedCellField(Object cellField);
	
	/**
	 * This pseudo-static Method is called ONLY ONE TIME after all cell's mechanical model's newSimStep Method has been called
	 * The method is called at ONLY ONE arbitrary model instance and should implement global mechanical operations after a sim step has been calculated
	 * 
	 */
	protected abstract void newSimStepGloballyFinished(long simStepNumber);

}
