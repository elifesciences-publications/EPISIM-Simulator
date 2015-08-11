package sim.app.episim.model.biomechanics;


import java.awt.Color;

import javax.media.j3d.Transform3D;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;



public class Ellipsoid {
	
	private static final Matrix3d identityMatrix = new Matrix3d(1,0,0,0,1,0,0,0,1);
	
	
	
	private Matrix3d ellipsoidMatrix;
	
	private Vector3d ellipsoidCenter;
	private  boolean optimizedGraphicsActivated =false;
	public Ellipsoid(Transform3D transform, double radius){
		ellipsoidMatrix = calculateEllipsoidMatrix(transform, radius);
		Matrix4d m = new Matrix4d();
		transform.get(m);
		ellipsoidCenter = new Vector3d();
		m.get(ellipsoidCenter);
		MiscalleneousGlobalParameters param = MiscalleneousGlobalParameters.getInstance();     
      if(param instanceof MiscalleneousGlobalParameters3D && ((MiscalleneousGlobalParameters3D)param).getOptimizedGraphics()){	
			optimizedGraphicsActivated = true;
		}
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
		return getPointEllipsoidDistance(x, y, z) <=1;
	}
	
	private double getPointEllipsoidDistance(double x, double y, double z){
		Vector3d point = new Vector3d((x-ellipsoidCenter.x),(y-ellipsoidCenter.y),(z-ellipsoidCenter.z));
		Matrix3d point_T = new Matrix3d();
		point_T.setRow(0, point);
		point_T.mul(point_T, ellipsoidMatrix);
		Vector3d resVect = new Vector3d();
		point_T.getRow(0, resVect);
		return resVect.dot(point);
	}
	
	private static final Color contourColor = Color.GRAY.brighter();
	
	public void getXYCrosssection(double z, double minX, double minY, double maxX, double maxY, double xOffset, double yOffset, IntGrid2D resultingColorPixelMap, Color pixelColor){
		boolean enableContour = true;
		boolean enableHiRes = false;
		if(MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D){
			enableContour = (((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getDrawCellContourInCrosssection());
			enableHiRes = (((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getHiResCrosssection());
		}
		double factorXY = TissueController.getInstance().getTissueBorder().get3DTissueCrosssectionXYResolutionFactor();
		if(optimizedGraphicsActivated || enableHiRes)factorXY*=2;
		double incrementX = 1 / factorXY;
		double incrementY = 1 / factorXY;
		int[][] colorMap = new int[(int)((maxX-minX)*factorXY+1)][(int)((maxY-minY)*factorXY+1)];
		initializeColorMap(colorMap);
		for(double y = minY; y <= maxY; y+= incrementY){
			for(double x = minX; x <= maxX; x+= incrementX){
				double distance = getPointEllipsoidDistance(x, y, z);
				if(distance <= (1)){
					resultingColorPixelMap.field[resultingColorPixelMap.stx((int)((x+xOffset)*factorXY))][resultingColorPixelMap.sty((int)((y+yOffset)*factorXY))]= pixelColor.getRGB();
					colorMap[(int)((x-minX)*factorXY)][(int)((y-minY)*factorXY)] = 1;
				}				
			}
		}
		if(enableContour){
			for(double y = minY; y <= maxY; y+= incrementY){
				for(double x = minX; x <= maxX; x+= incrementX){
					if(isContourPixel((int)((x-minX)*factorXY), (int)((y-minY)*factorXY), colorMap)){
						resultingColorPixelMap.field[resultingColorPixelMap.stx((int)((x+xOffset)*factorXY))][resultingColorPixelMap.sty((int)((y+yOffset)*factorXY))]= contourColor.getRGB();
					}				
				}
			}
		}
	}
	public void getXZCrosssection(double y, double minX, double minZ, double maxX, double maxZ, double xOffset, double zOffset, IntGrid2D resultingColorPixelMap, Color pixelColor){
		boolean enableContour = true;
		boolean enableHiRes = false;
		if(MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D){
			enableContour = (((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getDrawCellContourInCrosssection());
			enableHiRes = (((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getHiResCrosssection());
		}
		double factorXZ = TissueController.getInstance().getTissueBorder().get3DTissueCrosssectionXZResolutionFactor();
		if(optimizedGraphicsActivated || enableHiRes)factorXZ*=2;
		double incrementX = 1 / factorXZ;
		double incrementZ = 1 / factorXZ;
		int[][] colorMap = new int[(int)((maxX-minX)*factorXZ)+1][(int)((maxZ-minZ)*factorXZ+1)];
		initializeColorMap(colorMap);
		for(double z = minZ; z <= maxZ; z+= incrementZ){
			for(double x = minX; x <= maxX; x+= incrementX){
				double distance = getPointEllipsoidDistance(x, y, z);
				if(distance <=1){
					resultingColorPixelMap.field[resultingColorPixelMap.stx((int)((x+xOffset)*factorXZ))][resultingColorPixelMap.sty((int)((z+zOffset)*factorXZ))]= pixelColor.getRGB();
					colorMap[(int)((x-minX)*factorXZ)][(int)((z-minZ)*factorXZ)] = 1;
				}
			}
		}
		if(enableContour){
			for(double z = minZ; z <= maxZ; z+= incrementZ){
				for(double x = minX; x <= maxX; x+= incrementX){
					if(isContourPixel((int)((x-minX)*factorXZ), (int)((z-minZ)*factorXZ), colorMap)){
						resultingColorPixelMap.field[resultingColorPixelMap.stx((int)((x+xOffset)*factorXZ))][resultingColorPixelMap.sty((int)((z+zOffset)*factorXZ))]= contourColor.getRGB();
					}				
				}
			}
		}
	}
	public void getYZCrosssection(double x, double minY, double minZ, double maxY, double maxZ, double yOffset, double zOffset, IntGrid2D resultingColorPixelMap, Color pixelColor){
		boolean enableContour = true;
		boolean enableHiRes = false;
		if(MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D){
			enableContour = (((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getDrawCellContourInCrosssection());
			enableHiRes = (((MiscalleneousGlobalParameters3D) MiscalleneousGlobalParameters.getInstance()).getHiResCrosssection());
		}
		double factorYZ = TissueController.getInstance().getTissueBorder().get3DTissueCrosssectionYZResolutionFactor();
		if(optimizedGraphicsActivated || enableHiRes)factorYZ*=2;
		double incrementZ = 1 / factorYZ;
		double incrementY = 1 / factorYZ;
		int[][] colorMap = new int[(int)((maxZ-minZ)*factorYZ+1)][(int)((maxY-minY)*factorYZ+1)];
		initializeColorMap(colorMap);
		for(double y = minY; y <= maxY; y+= incrementY){
			for(double z = minZ; z <= maxZ; z+= incrementZ){
				double distance = getPointEllipsoidDistance(x, y, z);
				if(distance <= 1){
					resultingColorPixelMap.field[resultingColorPixelMap.stx((int)((z+zOffset)*factorYZ))][resultingColorPixelMap.sty((int)((y+yOffset)*factorYZ))]= pixelColor.getRGB();
					colorMap[(int)((z-minZ)*factorYZ)][(int)((y-minY)*factorYZ)] = 1;
				}
			}
		}
		if(enableContour){
			for(double y = minY; y <= maxY; y+= incrementY){
				for(double z = minZ; z <= maxZ; z+= incrementZ){
					if(isContourPixel((int)((z-minZ)*factorYZ), (int)((y-minY)*factorYZ), colorMap)){
						resultingColorPixelMap.field[resultingColorPixelMap.stx((int)((z+zOffset)*factorYZ))][resultingColorPixelMap.sty((int)((y+yOffset)*factorYZ))]= contourColor.getRGB();
					}				
				}
			}
		}
	}
	private boolean isContourPixel(int x, int y, int[][] colorMap){
		if(colorMap[x][y] == -1){
			if((x-1) >=0){
				if(colorMap[x-1][y]==1) return true;
			}
			if((y-1) >=0){
				if(colorMap[x][y-1]==1) return true;
			}			
			if((x+1) < colorMap.length){
				if(colorMap[x+1][y]==1) return true;
			}
			if((y+1) < colorMap[x].length){
				if(colorMap[x][y+1]==1) return true;
			}
			
		/*	if((x-1) >=0 &&(y-1) >=0){
				if(colorMap[x-1][y-1]==1) return true;
			}
			if((x+1) < colorMap.length &&(y+1) < colorMap[x+1].length){
				if(colorMap[x+1][y+1]==1) return true;
			}
			if((x-1) >= 0 &&(y+1) < colorMap[x-1].length){
				if(colorMap[x-1][y+1]==1) return true;
			}
			if((x+1) < colorMap.length &&(y-1) >=0){
				if(colorMap[x+1][y-1]==1) return true;
			}*/
		}
		return false;
	}
	
	private void initializeColorMap(int[][] colorMap){
		for(int x = 0; x < colorMap.length; x++){
			for(int y = 0; y < colorMap[x].length; y++){
				colorMap[x][y] = -1;
			}
		}
	}
}
