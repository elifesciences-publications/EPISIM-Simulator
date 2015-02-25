package sim.app.episim.model.biomechanics;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import sim.app.episim.model.AbstractCell;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import episiminterfaces.EpisimBiomechanicalModel;


public abstract class AbstractMechanical2DModel extends AbstractMechanicalModel<Double2D> implements EpisimBiomechanicalModel<Shape, DrawInfo2D>{

	public AbstractMechanical2DModel(){
		this(null);
	}
	
	public AbstractMechanical2DModel(AbstractCell cell){
		super(cell);
	}
	
	public abstract void setLastDrawInfo2D(DrawInfo2D info);
	public abstract DrawInfo2D getLastDrawInfo2D();
	protected abstract void removeCellsInWoundArea(GeneralPath woundArea);
	
}
