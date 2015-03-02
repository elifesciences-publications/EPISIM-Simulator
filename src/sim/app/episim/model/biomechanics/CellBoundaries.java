package sim.app.episim.model.biomechanics;

import java.awt.Color;
import java.awt.Shape;

import javax.media.j3d.BoundingPolytope;
import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import sim.app.episim.tissueimport.TissueController;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;



public class CellBoundaries {
	private Shape shape;
	private Ellipsoid ellipsoid;
	
	private Vector3d minVector;
	private Vector3d maxVector;
	
	public CellBoundaries(Shape shape){
		this.shape = shape;
	}
	
	public CellBoundaries(Ellipsoid ellipsoid, Vector3d minVector, Vector3d maxVector){
		this.ellipsoid = ellipsoid;
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
		if(ellipsoid != null){
			return ellipsoid.contains(x, y, z);
		}
		return false;
	}
	
	public void getXYCrosssection(double z, IntGrid2D resultingColorPixelMap, Color pixelColor){
		if(ellipsoid != null){
			ellipsoid.getXYCrosssection(z, getMinXInMikron()*0.9, getMinYInMikron()*0.9, getMaxXInMikron()*1.1, getMaxYInMikron()*1.1, resultingColorPixelMap, pixelColor);
		}
	}
	public void getXZCrosssection(double y, IntGrid2D resultingColorPixelMap, Color pixelColor){
		if(ellipsoid != null){
			ellipsoid.getXZCrosssection(y, getMinXInMikron()*0.9, getMinZInMikron()*0.9, getMaxXInMikron()*1.1, getMaxZInMikron()*1.1, resultingColorPixelMap, pixelColor);
		}
	}
	public void getYZCrosssection(double x, IntGrid2D resultingColorPixelMap, Color pixelColor){
		if(ellipsoid != null){
			ellipsoid.getYZCrosssection(x, getMinYInMikron()*0.9, getMinZInMikron()*0.9, getMaxYInMikron()*1.1, getMaxZInMikron()*1.1, resultingColorPixelMap, pixelColor);
		}
	}
	
}
