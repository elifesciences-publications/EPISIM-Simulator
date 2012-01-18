package sim.app.episim.model.biomechanics;

import java.awt.Shape;

import episiminterfaces.EpisimCellShape;


public class Episim2DCellShape<T extends Shape> implements EpisimCellShape<Shape>{

	private T cellShape;
	
	public Episim2DCellShape(T shape){
		this.cellShape = cellShape;
	}
	
	public T getCellShape(){ return cellShape; }
	
	
	
}
