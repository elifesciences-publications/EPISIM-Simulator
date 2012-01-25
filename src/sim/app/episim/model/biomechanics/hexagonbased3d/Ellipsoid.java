package sim.app.episim.model.biomechanics.hexagonbased3d;


import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

import javax.vecmath.Vector3d;



public class Ellipsoid {
	
	private static final Matrix3d identityMatrix = new Matrix3d(1,0,0,0,1,0,0,0,1);
	
	
	
	private Matrix3d ellipsoidMatrix;
	
	private Vector3d ellipsoidCenter;
	
	public Ellipsoid(Transform3D transform, double radius){
		ellipsoidMatrix = calculateEllipsoidMatrix(transform, radius);
		Matrix4d m = new Matrix4d();
		transform.get(m);
		ellipsoidCenter = new Vector3d();
		m.get(ellipsoidCenter);
	}
	
	
	private Matrix3d calculateEllipsoidMatrix(Transform3D transform, double radius){
		Matrix3d rotM = new Matrix3d();
		transform.get(rotM);
		rotM.mul(rotM, identityMatrix);
		
		Vector3d[] vect = new Vector3d[]{ new Vector3d(), new Vector3d(),new Vector3d()};
		
		for(int i = 0; i < vect.length; i++) rotM.getColumn(i, vect[i]);
		
		Matrix3d[] axisMatrices = new Matrix3d[3];
		for(int i = 0; i < axisMatrices.length; i++){
			axisMatrices[i] = new Matrix3d(vect[i].x*vect[i].x, vect[i].x*vect[i].y, vect[i].x*vect[i].z, 
												  vect[i].y*vect[i].x, vect[i].y*vect[i].y, vect[i].y*vect[i].z, 
												  vect[i].z*vect[i].x, vect[i].z*vect[i].y, vect[i].z*vect[i].z);
		}
		
		double[] axisLengthFactors = new double[3];
		Vector3d scalesVec = new Vector3d();
		transform.getScale(scalesVec);
		
		double[] scales = new double[3];
		scalesVec.get(scales);
		for(int i = 0; i < axisLengthFactors.length; i++){
			axisLengthFactors[i] = (1d/Math.pow((scales[i]*radius), 2));
		}
		Matrix3d resultMatrix = new Matrix3d();
		for(int i = 0; i < axisMatrices.length; i++){
			axisMatrices[i].mul(axisLengthFactors[i]);
			resultMatrix.add(axisMatrices[i]);
		}		
		return resultMatrix;
	}
	
	public boolean contains(double x, double y, double z){
		Vector3d point = new Vector3d((x-ellipsoidCenter.x),(y-ellipsoidCenter.y),(z-ellipsoidCenter.z));
		Matrix3d point_T = new Matrix3d();
		point_T.setRow(0, point);
		point_T.mul(point_T, ellipsoidMatrix);
		Vector3d resVect = new Vector3d();
		point_T.getRow(0, resVect);
		return resVect.dot(point) <=1;
	}
}
