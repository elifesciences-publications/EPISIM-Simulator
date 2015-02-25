package sim.app.episim.other;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
public class TestDistanceCalculation {	
	 private double calculateDistanceToCellCenter(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){		 
		 Vector3d rayDirection = new Vector3d((cellCenter.x-otherCellCenter.x), (cellCenter.y-otherCellCenter.y), (cellCenter.z-otherCellCenter.z));
		 rayDirection.normalize();
		 Point3d rayPosition = new Point3d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y), (otherCellCenter.z-cellCenter.z));
		 
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;
		 double cAxis_2=cAxis * cAxis;		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2))
	            + ((rayDirection.z * rayDirection.z) / (cAxis_2));	 
	    double b = ((2 * rayPosition.x * rayDirection.x) / (aAxis_2))
	            + ((2 * rayPosition.y * rayDirection.y) / (bAxis_2))
	            + ((2 * rayPosition.z * rayDirection.z) / (cAxis_2));
	 
	    double c = ((rayPosition.x * rayPosition.x) / (aAxis_2))
	            + ((rayPosition.y * rayPosition.y) / (bAxis_2))
	            + ((rayPosition.z * rayPosition.z) / (cAxis_2))
	            - 1;	 
	    double d = ((b * b) - (4.0d * a * c));	 
	    if (d < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return -1;
	    }
	    else
	    {
	        d = Math.sqrt(d);
	    }	 
	    double hit = (-b + d) / (2.0f * a);
	    double hitsecond = (-b - d) / (2.0f * a);	    
	    double linefactor = hit < hitsecond ? hit : hitsecond;
	    Point3d intersectionPointEllipsoid = new Point3d((cellCenter.x+ rayPosition.x + linefactor*rayDirection.x),(cellCenter.y+ rayPosition.y + linefactor*rayDirection.y),(cellCenter.z+ rayPosition.z + linefactor*rayDirection.z));	   
	    return cellCenter.distance(intersectionPointEllipsoid);
	}	
	public void start(){		
		double distance1 = calculateDistanceToCellCenter(new Point3d(1, 1, 1), new Point3d(2,1,1), 6, 2, 3);
		double distance2 = calculateDistanceToCellCenter(new Point3d(2, 1, 1), new Point3d(1,1,1), 4, 5, 1);
		System.out.println("Optimal Distance: " +(distance1 + distance2));
	}	
	public static void main(String[] args){
		(new TestDistanceCalculation()).start();
	}	
}