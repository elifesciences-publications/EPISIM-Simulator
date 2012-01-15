package sim.app.episim.model.biomechanics;

import java.awt.Shape;

import javax.media.j3d.Bounds;


public class CellBoundaries {
	
	
	
	public CellBoundaries(Shape shape){
		
	}
	
	public CellBoundaries(Bounds bounds){
		
	}
	
	public double getMinXInMikron(){
		return 0;
	}
	public double getMinYInMikron(){
		return 0;
	}
	public double getMinZInMikron(){
		return 0;
	}
	
	public double getMaxXInMikron(){
		return 0;
	}
	public double getMaxYInMikron(){
		return 0;
	}
	public double getMaxZInMikron(){
		return 0;
	}
	
	public boolean contains(double x, double y){
		return false;
	}
	
	public boolean contains(double x, double y, double z){
		return false;
	}
	
	

}
