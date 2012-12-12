package sim.app.episim;

import javax.vecmath.Point3d;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Point3d cellCenter = new Point3d(1,2,3);
		Point3d rayDirection = new Point3d(1,1,1);
		double linefactor = 1;
		 Point3d intersectionPointEllipsoid = new Point3d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y),(cellCenter.z+ linefactor*rayDirection.z));
		   
		 System.out.println(cellCenter.distance(intersectionPointEllipsoid));
		 System.out.println(Math.sqrt(Math.pow(linefactor*rayDirection.x,2)+Math.pow(linefactor*rayDirection.y,2)+Math.pow(linefactor*rayDirection.z,2)));
	}

}
