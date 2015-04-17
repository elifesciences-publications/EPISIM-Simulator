package sim.app.episim.model.biomechanics;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import sim.app.episim.model.AbstractCell;
import sim.portrayal.DrawInfo2D;
import sim.util.Double2D;
import episiminterfaces.EpisimBiomechanicalModel;


public abstract class AbstractBiomechanical2DModel extends AbstractBiomechanicalModel<Double2D> implements EpisimBiomechanicalModel<Shape, DrawInfo2D>{

	public AbstractBiomechanical2DModel(){
		this(null);
	}
	
	public AbstractBiomechanical2DModel(AbstractCell cell){
		super(cell);
	}
	
	public abstract void setLastDrawInfo2D(DrawInfo2D info);
	public abstract DrawInfo2D getLastDrawInfo2D();
	
	
}
