package sim.app.episim.model.biomechanics;

import javax.media.j3d.Shape3D;

import episiminterfaces.EpisimCellShape;


public class Episim3DCellShape<T extends Shape3D> implements EpisimCellShape<Shape3D>{

	private T cellShape;
	
	public Episim3DCellShape(T shape){
		this.cellShape = shape;
	}
	
	public T getCellShape(){ return this.cellShape; }
}
