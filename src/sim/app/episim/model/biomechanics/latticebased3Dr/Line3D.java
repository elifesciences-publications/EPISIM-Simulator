package sim.app.episim.model.biomechanics.latticebased3Dr;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Line3D {
	
	private Vector3d p1;
	private Vector3d p2;
	
	private static final double EPSILON = 0.001;	
	
	public Line3D(Vector3d point1,Vector3d point2){
		this.p1 = point1;
		this.p2 = point2;
	}	
	
	/*
   	Calculate the line segment PaPb that is the shortest route between
	   two line segment P1P2 and P3P4. Calculate also the values of mua and mub where
	      Pa = P1 + mua (P2 - P1)
	      Pb = P3 + mub (P4 - P3)
	   Return FALSE if no solution exists.
	 */
	public boolean lineLineIntersect(Line3D otherLine, double maxLineDistance)
	{
		Vector3d p3 = otherLine.getPoint1();
		Vector3d p4 = otherLine.getPoint2();
		
		Vector3d p13 = new Vector3d(),p43 = new Vector3d(),p21 = new Vector3d();
	   double d1343, d4321, d1321, d4343, d2121;
	   double numer, denom, mua, mub;
	
	   Point3d pa = new Point3d();
	   Point3d pb = new Point3d();   
	   
	   p13.x = p1.x - p3.x;
	   p13.y = p1.y - p3.y;
	   p13.z = p1.z - p3.z;
	   p43.x = p4.x - p3.x;
	   p43.y = p4.y - p3.y;
	   p43.z = p4.z - p3.z;
	   if (Math.abs(p43.x) < EPSILON && Math.abs(p43.y) < EPSILON && Math.abs(p43.z) < EPSILON) return false;
	   p21.x = p2.x - p1.x;
	   p21.y = p2.y - p1.y;
	   p21.z = p2.z - p1.z;
	   if (Math.abs(p21.x) < EPSILON && Math.abs(p21.y) < EPSILON && Math.abs(p21.z) < EPSILON) return false;
	
	   d1343 = p13.x * p43.x + p13.y * p43.y + p13.z * p43.z;
	   d4321 = p43.x * p21.x + p43.y * p21.y + p43.z * p21.z;
	   d1321 = p13.x * p21.x + p13.y * p21.y + p13.z * p21.z;
	   d4343 = p43.x * p43.x + p43.y * p43.y + p43.z * p43.z;
	   d2121 = p21.x * p21.x + p21.y * p21.y + p21.z * p21.z;
	
	   denom = d2121 * d4343 - d4321 * d4321;
	   if (Math.abs(denom) < EPSILON) return false;
	   
	   numer = (d1343 * d4321) - (d1321 * d4343);
	
	   mua = numer / denom;
	   mub = (d1343 + (d4321 * mua)) / d4343;
	
	   pa.x = p1.x + mua * p21.x;
	   pa.y = p1.y + mua * p21.y;
	   pa.z = p1.z + mua * p21.z;
	   pb.x = p3.x + mub * p43.x;
	   pb.y = p3.y + mub * p43.y;
	   pb.z = p3.z + mub * p43.z;
	/*   System.out.println("mua : " + mua);
	   System.out.println("mub : " + mub);
	   System.out.println("distance pa - pb : " + pa.distance(pb));*/
	   return (mua>=0 && mua<=1 && mub>=0 && mub<=1) && pa.distance(pb) <= maxLineDistance;
	}	
	
   public Vector3d getPoint1() {
	   return p1;
   }   
   public void setPoint1(Vector3d point1) {
	   this.p1 = point1;
   } 
   public Vector3d getPoint2() {
	   return p2;
   }   
   public void setPoint2(Vector3d point2) {
	   this.p2 = point2;
   }
   
   public static void main(String[] args){
   	Line3D line1 = new Line3D(new Vector3d(75,125,125), new Vector3d(25,75,175));
   	Line3D line2 = new Line3D(new Vector3d(75,125,175), new Vector3d(25,75,125));
   	System.out.println("Intersection: " +line1.lineLineIntersect(line2, 12.5));
   }
   
}
