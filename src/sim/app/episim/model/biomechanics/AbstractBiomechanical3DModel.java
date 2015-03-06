package sim.app.episim.model.biomechanics;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;

import sim.app.episim.model.AbstractCell;
import sim.util.Double3D;
import episiminterfaces.EpisimBiomechanicalModel;


public abstract class AbstractBiomechanical3DModel extends AbstractBiomechanicalModel<Double3D> implements EpisimBiomechanicalModel<Shape3D, TransformGroup>{
	
	public AbstractBiomechanical3DModel(){
		this(null);
	}
	
	public AbstractBiomechanical3DModel(AbstractCell cell){
		super(cell);
	}
}