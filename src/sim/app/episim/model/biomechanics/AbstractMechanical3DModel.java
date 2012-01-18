package sim.app.episim.model.biomechanics;

import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;

import sim.app.episim.AbstractCell;
import sim.util.Double3D;

import episiminterfaces.EpisimBiomechanicalModel;


public abstract class AbstractMechanical3DModel extends AbstractMechanicalModel<Double3D> implements EpisimBiomechanicalModel<Shape3D, TransformGroup>{
	
	public AbstractMechanical3DModel(){
		this(null);
	}
	
	public AbstractMechanical3DModel(AbstractCell cell){
		super(cell);
	}
}
