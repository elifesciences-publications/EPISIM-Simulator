package sim.app.episim.model.biomechanics;

import java.awt.Shape;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


public class CellBoundaries {
	private Shape shape;
	private Bounds bounds;
	
	private Vector3d minVector;
	private Vector3d maxVector;
	
	public CellBoundaries(Shape shape){
		this.shape = shape;
	}
	
	public CellBoundaries(Bounds bounds, Vector3d minVector, Vector3d maxVector){
		this.bounds = bounds;
		this.minVector = minVector;
		this.maxVector = maxVector;
	}
	
	public double getMinXInMikron(){
		if(this.shape != null){
			return shape.getBounds2D().getMinX();
		}
		if(minVector != null){
			return minVector.x;
		}
		return 0;
	}
	public double getMinYInMikron(){
		if(this.shape != null){
			return shape.getBounds2D().getMinY();
		}
		if(minVector != null){
			return minVector.y;
		}
		return 0;
	}
	public double getMinZInMikron(){
		if(minVector != null){
			return minVector.z;
		}
		return 0;
	}
	
	public double getMaxXInMikron(){
		if(this.shape != null){
			return shape.getBounds2D().getMaxX();
		}
		if(maxVector != null){
			return maxVector.x;
		}
		return 0;
	}
	public double getMaxYInMikron(){
		if(this.shape != null){
			return shape.getBounds2D().getMaxY();
		}
		if(maxVector != null){
			return maxVector.y;
		}
		return 0;
	}
	public double getMaxZInMikron(){
		if(maxVector != null){
			return maxVector.z;
		}
		return 0;
	}
	
	public boolean contains(double x, double y){
		if(this.shape != null){
			return shape.contains(x, y);
		}
		return false;
	}
	
	public boolean contains(double x, double y, double z){
		if(bounds != null){
			return bounds.intersect(new Point3d(x,y,z));
		}
		return false;
	}
	
	

}
