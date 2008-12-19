package sim.app.episim.util;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;

import sim.app.episim.visualization.CellEllipse;




public class EllipseIntersectionCalculatorAndClipper {
	
	private static EllipseIntersectionCalculatorAndClipper instance;
	
	public static class XYPoints{
		
		public int[] xPointsEllipse1;
		public int[] yPointsEllipse1;
		public int[] xPointsEllipse2;
		public int[] yPointsEllipse2;
		
		protected void swapEllipse1And2(){
			int[] tmpX = xPointsEllipse1;
			int[] tmpY = yPointsEllipse1;
			
			xPointsEllipse1 = xPointsEllipse2;
			yPointsEllipse1 = yPointsEllipse2;
			
			
			xPointsEllipse2 =	tmpX;
			yPointsEllipse2 = tmpY;
		}
		
		public String toString(){
			StringBuffer str = new StringBuffer();
			str.append("Ellipse 1:\n");
			for(int i = 0; i < xPointsEllipse1.length && i < yPointsEllipse1.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+xPointsEllipse1[i]+","+yPointsEllipse1[i]+")  ");
			str.append("\nEllipse 2:\n");
			for(int i = 0; i < xPointsEllipse2.length && i < yPointsEllipse2.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+xPointsEllipse2[i]+","+yPointsEllipse2[i]+")  ");
			str.append("\n");
			return str.toString();
		}
		
	}
	
	private XYPoints calculateClippedEllipses(CellEllipse actEllipse, CellEllipse otherEllipse){		
			int [][] intersectionPoints = getIntersectionPoints(actEllipse.getEllipse(), otherEllipse.getEllipse());
			if(intersectionPoints != null){
				intersectionPoints = select2InterSectionPointsWithMinDistance(intersectionPoints, new int[]{actEllipse.getX(), actEllipse.getY()},new int[]{otherEllipse.getX(), otherEllipse.getY()});
				XYPoints xyPoints = calculateXYPoints(intersectionPoints[0], intersectionPoints[1], actEllipse, otherEllipse);
				clipEllipses(actEllipse, otherEllipse, xyPoints);
				return xyPoints;
			}
		return null;
	}
	
	public static XYPoints getClippedEllipsesAndXYPoints(CellEllipse actEllipse, CellEllipse otherEllipse){
		if(instance == null) instance = new EllipseIntersectionCalculatorAndClipper();
		
		return instance.calculateClippedEllipses(actEllipse, otherEllipse);
	}
	
	
	private double distance(int x1, int y1, int x2, int y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	
	
	private int[][] getIntersectionPoints(Shape shape1, Shape shape2){
		
		ArrayList<Double> protocollXPoints = new ArrayList<Double>();
		ArrayList<Double> protocollYPoints = new ArrayList<Double>();
		
		//maximum of 4 intersection points for two ellipses
		int [][] intersectionPoints = new int[4][2];
					
			 	Area a1 = getIntersection(shape1, shape2);
	        
	        //return if the ellipses don't overlap
	        if(a1.isEmpty()) return null;
	        
	        
	        
	        PathIterator it = a1.getPathIterator(null);
	        double[] d = new double[6];
	        double xOLD = 0;
	        double yOLD = 0;
	        int i = 0;       
	        
	        int previousSpX=0;
	        int previousSpY=0;
	        
	        boolean newIteration = true;
	        boolean intersectionPointConfirmed = false;
	        
	        while (newIteration){
	      	  
				int type = it.currentSegment(d);

				switch(type) {

					case PathIterator.SEG_LINETO:{
						intersectionPointConfirmed = true;
					}
	
					case PathIterator.SEG_CUBICTO: {
						if(equalPointsForItersection(xOLD, d[0])  && equalPointsForItersection(yOLD, d[1]) && intersectionPointConfirmed){
							int equalPointsForIntersectionX = getEqualPointsForItersection(xOLD, d[0]);
							int equalPointsForIntersectionY = getEqualPointsForItersection(yOLD, d[1]);
							if(previousSpX != equalPointsForIntersectionX || previousSpY != equalPointsForIntersectionY){
								intersectionPoints[i][0] = equalPointsForIntersectionX;
								intersectionPoints[i][1] = equalPointsForIntersectionY;
								previousSpX = intersectionPoints[i][0];
						      previousSpY = intersectionPoints[i][1];
						      intersectionPointConfirmed = false;
								i++;
							}
						}
						else intersectionPointConfirmed = false;
						xOLD = d[4];
						yOLD = d[5];
						protocollXPoints.add(d[4]);
						protocollYPoints.add(d[5]);
	
					}
						break;
					case PathIterator.SEG_CLOSE:{
						if(i< 4 && (i % 2)!=0){
							int roundedNewX = (int) Math.round(d[4]);
							int roundedNewY = (int) Math.round(d[5]);
							if(previousSpX != roundedNewX || previousSpY != roundedNewY){
								intersectionPoints[i][0] = roundedNewX;
								intersectionPoints[i][1] = roundedNewY;
							}
						}
						//exceptional case if no intersection point is found by the algorithm
						//then the starting and the end point of the intersection area are the intersection points
						else if(i==0 && protocollXPoints.size()==2 &&protocollYPoints.size()==2){
							intersectionPoints[0][0] = (int) Math.round(protocollXPoints.get(0));
							intersectionPoints[0][1] = (int) Math.round(protocollYPoints.get(0));
							intersectionPoints[1][0] = (int) Math.round(protocollXPoints.get(1));
							intersectionPoints[1][1] = (int) Math.round(protocollYPoints.get(1));
						}
						newIteration = false;
					}
				}
				it.next();

			}
				
		return intersectionPoints;
	}
	
	private XYPoints calculateXYPoints(int[] sp1, int[] sp2, CellEllipse actEllipse, CellEllipse otherEllipse){
		
		
	//	System.out.println("SP 1:"+ sp1[0] + ","+ sp1[1]);
		//System.out.println("SP 2:"+ sp2[0] + ","+ sp2[1]);
		
		double [] directionVector = new double[]{sp1[0]-sp2[0], sp1[1]-sp2[1]};
		
		double[] newVector; 
		
		if(directionVector[0]==0){ 
			if(sp1[0]> actEllipse.getX()) newVector = new double[]{1, 0};
			else newVector = new double[]{-1, 0};
		}
		else if(directionVector[1]==0){
			if(sp1[1]> actEllipse.getY()) newVector = new double[]{0, 1};
			else newVector = new double[]{0, -1};
		}
		else{ 
			newVector = new double[]{-1*(directionVector[1]/directionVector[0]), 1};
			double newVectorNormfact = 1/Math.sqrt(Math.pow(newVector[0], 2)+Math.pow(newVector[1], 2));
			newVector[0] *= newVectorNormfact;
			newVector[1] *= newVectorNormfact;
			if(actEllipse.getY() >= otherEllipse.getY()){
				newVector[0] *= -1;
				newVector[1] *= -1;
			}			
		}
		
		//System.out.println("actEllipse("+actEllipse.getX()+","+ actEllipse.getY()+") otherEllipse("+otherEllipse.getX()+","+otherEllipse.getY()+")");
		
			
		newVector[0] *= actEllipse.getMajorAxis();
		newVector[1] *= actEllipse.getMajorAxis();
				
		XYPoints xyPoints = new XYPoints();
		
		xyPoints.xPointsEllipse1 = new int[]{sp1[0], sp2[0], sp2[0] + (int)newVector[0], sp1[0] + (int)newVector[0]};/*x-Points*/
		xyPoints.yPointsEllipse1 = new int[]{sp1[1], sp2[1], sp2[1] + (int)newVector[1], sp1[1] + (int)newVector[1]};/*y-Points*/
	/*	
		System.out.print("X-Points: ");
		for(int x:xyPoints.xPointsEllipse1)System.out.print(x+", ");
		System.out.println();
	
		System.out.print("Y-Points: ");
		for(int y:xyPoints.yPointsEllipse1)System.out.print(y+", ");
		System.out.println();
		
		System.out.println("Direction Vector: ("+directionVector[0]+", "+directionVector[1]+")");
		System.out.println("New Vector: ("+newVector[0]+", "+newVector[1]+")");
		
		*/
		
		newVector[0] *= -1;
		newVector[1] *= -1;
		
		
		xyPoints.xPointsEllipse2 = new int[]{sp1[0], sp2[0], sp2[0] + (int)newVector[0], sp1[0] + (int)newVector[0]};/*x-Points*/
		xyPoints.yPointsEllipse2 = new int[]{sp1[1], sp2[1], sp2[1] + (int)newVector[1], sp1[1] + (int)newVector[1]};/*y-Points*/
		
	
		pointCorrectionXYPoints(xyPoints.xPointsEllipse1, xyPoints.yPointsEllipse1, directionVector);
		pointCorrectionXYPoints(xyPoints.xPointsEllipse2, xyPoints.yPointsEllipse2, directionVector);	
		
		//Look if swap is necessary using the distance between centroid of actEllipse and the 
		//one of the resulting points of the trapeze (different from the intersection points sp)
		//we're looking for maximal distance
		
		if(distance(actEllipse.getX(), actEllipse.getY(), xyPoints.xPointsEllipse1[2], xyPoints.yPointsEllipse1[2]) < 
				distance(actEllipse.getX(), actEllipse.getY(), xyPoints.xPointsEllipse2[2], xyPoints.yPointsEllipse2[2])) xyPoints.swapEllipse1And2();
		
		return xyPoints;
	}
	
	
	//Point-correction to make trapezoids out in order to cut everything without remaining spaces
	private void pointCorrectionXYPoints(int[] xPointsEllipse, int[] yPointsEllipse, double[] directionVector){
		final double FACTORDIRVECT = 1.5;	
		
		//Point-correction in order to cut everything without remaining spaces
		if(xPointsEllipse[2] < xPointsEllipse[3]){
			if(directionVector[0] <= 0){
				xPointsEllipse[2] += directionVector[0]*FACTORDIRVECT;
				xPointsEllipse[3] -= directionVector[0]*FACTORDIRVECT;
			}
			else{
				xPointsEllipse[2] -= directionVector[0]*FACTORDIRVECT;
				xPointsEllipse[3] += directionVector[0]*FACTORDIRVECT;
			}
		}
		else if(xPointsEllipse[2] > xPointsEllipse[3]){			
			if(directionVector[0] <= 0){
				xPointsEllipse[3] += directionVector[0]*FACTORDIRVECT;
				xPointsEllipse[2] -= directionVector[0]*FACTORDIRVECT;
			}
			else{
				xPointsEllipse[3] -= directionVector[0]*FACTORDIRVECT;
				xPointsEllipse[2] += directionVector[0]*FACTORDIRVECT;
			}
		}
		if(yPointsEllipse[2] < yPointsEllipse[3]){
			if(directionVector[1] <= 0){
				yPointsEllipse[2] += directionVector[1]*FACTORDIRVECT;
				yPointsEllipse[3] -= directionVector[1]*FACTORDIRVECT;
			}
			else{
				yPointsEllipse[2] -= directionVector[1]*FACTORDIRVECT;
				yPointsEllipse[3] += directionVector[1]*FACTORDIRVECT;
			}
		}
		else if(yPointsEllipse[2] > yPointsEllipse[3]){
			if(directionVector[1] <= 0){
				yPointsEllipse[3] += directionVector[1]*FACTORDIRVECT;
				yPointsEllipse[2] -= directionVector[1]*FACTORDIRVECT;
			}
			else{
				yPointsEllipse[3] -= directionVector[1]*FACTORDIRVECT;
				yPointsEllipse[2] += directionVector[1]*FACTORDIRVECT;
			}
		}
	}
	
	private boolean equalPointsForItersection(double oldPoint, double newPoint){
		if(Math.abs(oldPoint-newPoint) <= 1.5) return true;
		
		return false;
	}
	private int getEqualPointsForItersection(double oldPoint, double newPoint){
		long valueL=0;
		double valueD=0;
		if(Math.round(oldPoint) == (valueL=Math.round(newPoint))) return (int) valueL;
		else if(Math.floor(oldPoint) == (valueL=Math.round(newPoint))) return (int) valueL;
		else if(Math.round(oldPoint) == (valueD=Math.floor(newPoint))) return (int) valueD;
		else if(Math.floor(oldPoint) == (valueD=Math.floor(newPoint))) return (int) valueD;
		
		return (int) Math.round(newPoint);
	}
	
	private void clipEllipses(CellEllipse actEllipse, CellEllipse otherEllipse, XYPoints xyPoints){
		
		actEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsEllipse1, xyPoints.yPointsEllipse1, 4)));
		otherEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsEllipse2, xyPoints.yPointsEllipse2, 4)));
		
	}
	
	private Area getIntersection(Shape shape1, Shape shape2){
		 Area a1 = new Area(shape1);
       Area a2 = new Area(shape2);
       a1.intersect(a2);
       return a1;
	}
	
	private int[][] select2InterSectionPointsWithMinDistance(int[][] intersectionPoints, int[] actEll, int[] otherEll){
		//if there are only two intersection points no changes are necessary
		if(intersectionPoints[2][0] == 0 && intersectionPoints[2][1] == 0 && intersectionPoints[3][0] == 0 && intersectionPoints[3][1] == 0) return intersectionPoints;
		else{
			double [] distances = new double[4];
			for(int i = 0; i < intersectionPoints.length; i++){
				distances[i]+= distance(actEll[0], actEll[1], intersectionPoints[i][0], intersectionPoints[i][1]);
				distances[i]+= distance(otherEll[0], otherEll[1], intersectionPoints[i][0], intersectionPoints[i][1]);
			}
			
			double min1 = Double.POSITIVE_INFINITY;
			double min2 = Double.POSITIVE_INFINITY;
			int min1Index = -1;
			int min2Index = -1;
			for(int i = 0; i < distances.length; i++){
				if(distances[i] < min1){
					if(min1 < min2){
						min2 = min1;
						min2Index = min1Index;
					}
					min1 = distances[i];
					min1Index = i;
				}
				else if(distances[i] < min2){
					min2 = distances[i];
					min2Index = i;
				}
			}
			return new int[][]{{intersectionPoints[min1Index][0], intersectionPoints[min1Index][1]},{intersectionPoints[min2Index][0], intersectionPoints[min2Index][1]},{0,0},{0,0}};
		}
	}
}
