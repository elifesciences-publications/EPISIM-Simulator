package sim.app.episim.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import sim.app.episim.visualization.CellEllipse;




public class EllipseIntersectionCalculatorAndClipper {
	
	private static EllipseIntersectionCalculatorAndClipper instance;
	
	private static long noOfCalls =0;
	private static double cumulativeTimeInMillis;
	private static long noOfSwaps = 0;
	
	private static int noOfRuns = 0;
	private static int maxNoOfRuns = 0;
	
	public static class XYPoints{
		
		public int[] xPointsQuaderEllipse1;
		public int[] yPointsQuaderEllipse1;
		public int[] xPointsQuaderEllipse2;
		public int[] yPointsQuaderEllipse2;
		public double[][] intersectionPoints;
		
		protected void swapEllipse1And2(){
			int[] tmpX = xPointsQuaderEllipse1;
			int[] tmpY = yPointsQuaderEllipse1;
			
			xPointsQuaderEllipse1 = xPointsQuaderEllipse2;
			yPointsQuaderEllipse1 = yPointsQuaderEllipse2;
			
			
			xPointsQuaderEllipse2 =	tmpX;
			yPointsQuaderEllipse2 = tmpY;
		}
		
		public String toString(){
			StringBuffer str = new StringBuffer();
			str.append("Ellipse 1:\n");
			for(int i = 0; i < xPointsQuaderEllipse1.length && i < yPointsQuaderEllipse1.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+xPointsQuaderEllipse1[i]+","+yPointsQuaderEllipse1[i]+")  ");
			str.append("\nEllipse 2:\n");
			for(int i = 0; i < xPointsQuaderEllipse2.length && i < yPointsQuaderEllipse2.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+xPointsQuaderEllipse2[i]+","+yPointsQuaderEllipse2[i]+")  ");
			str.append("\n");
			return str.toString();
		}
		
	}
	
	
	
	
	
	public static XYPoints getClippedEllipsesAndXYPoints(CellEllipse actEllipse, CellEllipse otherEllipse){
		noOfCalls++;
		long timeStart = (new Date()).getTime();
		
		XYPoints p = getClippedEllipsesAndXYPoints(null, actEllipse, otherEllipse);
		long timeEnd = (new Date()).getTime();
		cumulativeTimeInMillis += (timeEnd-timeStart);
		if((noOfCalls % 100000) == 0)System.out.println("Durchschnittliche Dauer ("+ noOfCalls+ ", noOfSwaps: "+noOfSwaps+"): "+ (cumulativeTimeInMillis/noOfCalls));
		return p;
	}
	
	public static XYPoints getClippedEllipsesAndXYPoints(Graphics2D g ,CellEllipse actEllipse, CellEllipse otherEllipse){
		if(instance == null) instance = new EllipseIntersectionCalculatorAndClipper();
		
		return instance.calculateClippedEllipses(g, actEllipse, otherEllipse);
	}
	
	private XYPoints calculateClippedEllipses(Graphics2D g, CellEllipse actEllipse, CellEllipse otherEllipse){
		double distanceEllipses =distance(actEllipse.getX(), actEllipse.getY(), otherEllipse.getX(), otherEllipse.getY());
		//System.out.println("Ellipsen Distanz: "+ distanceEllipses);
		if(distanceEllipses > 0 && distanceEllipses < ((actEllipse.getMajorAxis()/2)+(otherEllipse.getMajorAxis()/2))){
		double [][] intersectionPoints = newtonIntersectionCalculation(actEllipse, otherEllipse);
			
			if(intersectionPoints != null){
				
				if(g!= null){
					for(int i = 0; i < intersectionPoints.length; i++){
						drawPoint(g, intersectionPoints[i][0],intersectionPoints[i][1], 5, Color.GREEN);
					}
				}
				intersectionPoints = select2InterSectionPointsWithMinDistance(intersectionPoints, new double[]{actEllipse.getX(), actEllipse.getY()},new double[]{otherEllipse.getX(), otherEllipse.getY()});
				XYPoints xyPoints = calculateXYPoints(intersectionPoints[0], intersectionPoints[1], actEllipse, otherEllipse);
				clipEllipses(actEllipse, otherEllipse, xyPoints);
				return xyPoints;
			}
		}
		
		return null;
	}
	
	private double[][] newtonIntersectionCalculation(CellEllipse actEllipse, CellEllipse otherEllipse){//double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double phi){
		
		double x1 = actEllipse.getX();
		double y1 = actEllipse.getY();
		double a1 = ((double) actEllipse.getMajorAxis())/2;
		double b1 = ((double) actEllipse.getMinorAxis())/2;
		double a2 = ((double) otherEllipse.getMajorAxis())/2;
		double[][] foci = calculateFoci(otherEllipse.getX(), otherEllipse.getY(), otherEllipse.getMajorAxis(), otherEllipse.getMinorAxis(), otherEllipse.getOrientationInRadians());
		double[] f21 = foci[0];
		double[] f22 = foci[1];
		double phi = actEllipse.getOrientationInRadians();
		
		double x1_square = Math.pow(x1, 2);
		double y1_square = Math.pow(y1, 2);
		double a1_square = Math.pow(a1, 2);
		double b1_square = Math.pow(b1, 2);
		double quarter_a1_square = 0.25*a1_square;
		double quarter_b1_square = 0.25*b1_square;
		double cos_phi = Math.cos(phi);
		double sin_phi = Math.sin(phi);
		double cos_2phi = Math.cos(2*phi);
		double sin_2phi = Math.sin(2*phi);
	
		double a1_cos_phi = a1* cos_phi;
		double b1_cos_phi = b1* cos_phi;
		
		double a1_sin_phi = a1* sin_phi;
		double b1_sin_phi = b1* sin_phi;
		
		double quarter_b1_square_cos_2phi = quarter_b1_square*cos_2phi;
		double quarter_a1_square_cos_2phi = quarter_a1_square*cos_2phi;
		
		double u11 = x1_square - 2 * f21[0] * x1 + Math.pow(f21[0], 2) + quarter_a1_square + quarter_a1_square_cos_2phi + quarter_b1_square - quarter_b1_square_cos_2phi;
		double u12 = 2*x1*a1_cos_phi-2*f21[0]*a1_cos_phi;
		double u13 = -1*2*x1*b1_sin_phi + 2*f21[0]*b1_sin_phi;
		double u14 = -1*0.5*a1*b1*sin_2phi;
		double u15 = quarter_a1_square + quarter_a1_square_cos_2phi-quarter_b1_square+quarter_b1_square_cos_2phi;
		
		double u21 = x1_square - 2*f22[0]*x1 + Math.pow(f22[0], 2)+quarter_a1_square+quarter_a1_square_cos_2phi+quarter_b1_square-quarter_b1_square_cos_2phi;
		double u22 = 2*x1*a1_cos_phi-2*f22[0]*a1_cos_phi;
		double u23 = -1*2*x1*b1_sin_phi+2*f22[0]*b1_sin_phi;
		double u24 = u14;
		double u25 = u15;
		
		double v11 = y1_square - 2 * y1 * f21[1] + Math.pow(f21[1], 2) + quarter_b1_square + quarter_b1_square_cos_2phi + quarter_a1_square - quarter_a1_square_cos_2phi;
		double v12 = 2*y1*b1_cos_phi - 2*f21[1]*b1_cos_phi;
		double v13 = -1*2*f21[1]*a1_sin_phi+2*y1*a1_sin_phi;
		double v14 = -1*u14;
		double v15 = quarter_a1_square - quarter_a1_square_cos_2phi -quarter_b1_square-quarter_b1_square_cos_2phi;
		
		double v21 = y1_square - 2*y1*f22[1] + Math.pow(f22[1], 2) + quarter_b1_square +quarter_b1_square_cos_2phi + quarter_a1_square-quarter_a1_square_cos_2phi;
		double v22 = 2*y1*b1_cos_phi - 2*f22[1]*b1_cos_phi;
		double v23 = -1*2*f22[1]*a1_sin_phi+2*y1*a1_sin_phi;
		double v24 = v14;
		double v25 = v15;
		
		double u11_v11 = u11 + v11;
		double u12_v13 = u12 + v13;
		double u13_v12 = u13 + v12;
		double u14_v14 = u14 + v14;
		double u15_v15 = u15 + v15;
		
		double u21_v21 = u21 + v21;
		double u22_v23 = u22 + v23;
		double u23_v22 = u23 + v22;
		double u24_v24 = u24 + v24;
		double u25_v25 = u25 + v25;
		
		
		
		double sin_alpha = 0; 
		double cos_alpha = 0;
		double sin_2alpha = 0;
		double cos_2alpha = 0;
		
		
		double alpha =0;
		double f_alpha = 0;
		double f_alpha_partone = 0;
		double f_alpha_parttwo = 0;
		double df_dalpha = 0;
		
		Set<Double> resultSet = new HashSet<Double>();
		
		double border = 2* Math.PI;
		double i = 0;
		
		
		int counter = 0;
		final double stepsize = Math.PI/8;
				
		while( i < border){
			alpha = i;
		 	do{
				counter++;
				sin_alpha = Math.sin(alpha);
				cos_alpha = Math.cos(alpha);
				
				sin_2alpha = Math.sin(2*alpha);
				cos_2alpha = Math.cos(2*alpha);
				
				f_alpha_partone = Math.sqrt(u11_v11 + u12_v13*cos_alpha + u13_v12*sin_alpha + u14_v14*sin_2alpha + u15_v15*cos_2alpha);
				f_alpha_parttwo = Math.sqrt(u21_v21 + u22_v23*cos_alpha + u23_v22*sin_alpha + u24_v24*sin_2alpha + u25_v25*cos_2alpha);
				
				f_alpha = f_alpha_partone + f_alpha_parttwo -2*a2;			
				
				if(Math.abs(f_alpha) > 0.00000000001){
					df_dalpha = 0.5*(1/f_alpha_partone)*(-1*u12_v13*sin_alpha + u13_v12*cos_alpha + 2*u14_v14*cos_2alpha - 2*u15_v15*sin_2alpha)
			 		           + 0.5*(1/f_alpha_parttwo)*(-1*u22_v23*sin_alpha + u23_v22*cos_alpha + 2*u24_v24*cos_2alpha - 2*u25_v25*sin_2alpha); 
						
					alpha = alpha - (f_alpha / df_dalpha);		
				}
				else{
					if(!Double.isInfinite(alpha)){
						//noOfCalls++;
					//	noOfRuns += counter;
					//	if(counter > maxNoOfRuns) maxNoOfRuns = counter;
				//		if((noOfCalls % 100000) ==0) System.out.println("Durchschn. Berechnungsschritte(No. of Calls "+noOfCalls+"): " + (noOfRuns/noOfCalls)+ "   Max No. Schritte: "+ maxNoOfRuns);
						double finalResult = (Math.round((alpha%(Math.PI*2))*100d)/100d);
						resultSet.add(finalResult);
			//	if(counter > 6)System.out.println("Added Result " + finalResult + " Counter: " + counter + " alpha_start: " + i + " f_alpha: " + f_alpha);
					}
				}
				
		 }while(Math.abs(f_alpha) > 0.00000000001 && counter <=6 && alpha >= 0);
			
			i += stepsize;
			counter=0;
	   }
	
		if(!resultSet.isEmpty()){
		   double [][] intersectionPoints = new double[4][2]; 
		   if(resultSet.size() > 4) intersectionPoints= new double[resultSet.size()][2];
		   int index = 0;
		   for(double angle : resultSet){
		   	intersectionPoints[index++] = calculatePointOnEllipse(x1, y1, a1, b1,angle, phi);
		   }
			return intersectionPoints;
		}
		return null;
	}
	
	private double distance(double x1, double y1, double x2, double y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	private void drawPoint(Graphics2D g, double x, double y, double size, Color c){
		if(x> 0 || y > 0){
			if(size % 2 != 0) size -= 1;
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect((int)(x-(size/2)), (int)(y-(size/2)), (int)(size+1), (int)(size+1));
			g.setColor(oldColor);
		}
	}	
	
	private double[] calculatePointOnEllipse(double x, double y, double a, double b, double alpha, double phi){
		double cos_alpha = Math.cos(alpha);
		double sin_alpha = Math.sin(alpha);
		double cos_phi = Math.cos(phi);
		double sin_phi = Math.sin(phi);
		
		double point_x = x + a * cos_phi*cos_alpha -  b*sin_phi*sin_alpha;
		double point_y = y + b*cos_phi*sin_alpha + a*sin_phi*cos_alpha;
		
		return new double[]{point_x, point_y};
	}
	
	
	
	private XYPoints calculateXYPoints(double[] sp1, double[] sp2, CellEllipse actEllipse, CellEllipse otherEllipse){
		double [] directionVector = new double[]{sp1[0]-sp2[0], sp1[1]-sp2[1]};
		double[] normalVector; 
		
		if(directionVector[0]==0){ 
			if(sp1[0]> actEllipse.getX()) normalVector = new double[]{1, 0};
			else normalVector = new double[]{-1, 0};
		}
		else if(directionVector[1]==0){
			if(sp1[1]> actEllipse.getY()) normalVector = new double[]{0, 1};
			else normalVector = new double[]{0, -1};
		}
		else{ 
			normalVector = new double[]{-1*(directionVector[1]/directionVector[0]), 1};
			double normalizationFactor = 1/Math.sqrt(Math.pow(normalVector[0], 2)+Math.pow(normalVector[1], 2));
			normalVector[0]*= normalizationFactor;
			normalVector[1]*= normalizationFactor;
		}
					
		XYPoints xyPoints = new XYPoints();
		
		double[][] theFinalPoints = new double[4][2];
		
		theFinalPoints[0] = pointCorrection(sp1, sp2, directionVector, actEllipse.getMajorAxis()/2);
		theFinalPoints[1] = pointCorrection(sp2, sp1, directionVector, actEllipse.getMajorAxis()/2);
		theFinalPoints[2] = new double[]{theFinalPoints[1][0]  + normalVector[0] * actEllipse.getMajorAxis(), theFinalPoints[1][1]  + normalVector[1] * actEllipse.getMajorAxis()};
		theFinalPoints[3] = new double[]{theFinalPoints[0][0] + normalVector[0] * actEllipse.getMajorAxis(), theFinalPoints[0][1]  + normalVector[1] * actEllipse.getMajorAxis()};
		
		xyPoints.xPointsQuaderEllipse1 = new int[]{(int)theFinalPoints[0][0], (int)theFinalPoints[1][0],(int)theFinalPoints[2][0],(int)theFinalPoints[3][0]};/*x-Points*/
		xyPoints.yPointsQuaderEllipse1 = new int[]{(int)theFinalPoints[0][1], (int)theFinalPoints[1][1],(int)theFinalPoints[2][1],(int)theFinalPoints[3][1]};/*y-Points*/
			
		theFinalPoints[2] = new double[]{theFinalPoints[1][0]  + normalVector[0] * actEllipse.getMajorAxis()*-1, theFinalPoints[1][1]  + normalVector[1] * actEllipse.getMajorAxis()*-1};
		theFinalPoints[3] = new double[]{theFinalPoints[0][0] + normalVector[0] * actEllipse.getMajorAxis()*-1, theFinalPoints[0][1]  + normalVector[1] * actEllipse.getMajorAxis()*-1};
		
		xyPoints.xPointsQuaderEllipse2 = new int[]{(int)theFinalPoints[0][0], (int)theFinalPoints[1][0],(int)theFinalPoints[2][0],(int)theFinalPoints[3][0]};/*x-Points*/
		xyPoints.yPointsQuaderEllipse2 = new int[]{(int)theFinalPoints[0][1], (int)theFinalPoints[1][1],(int)theFinalPoints[2][1],(int)theFinalPoints[3][1]};/*y-Points*/
		
	
	
	
		
		//Look if swap is necessary using the distance between centroid of actEllipse and the 
		//one of the resulting points of the trapeze (different from the intersection points sp)
		//we're looking for maximal distance
		
	if(distance(actEllipse.getX(), actEllipse.getY(), xyPoints.xPointsQuaderEllipse1[2], xyPoints.yPointsQuaderEllipse1[2]) < 
			distance(actEllipse.getX(), actEllipse.getY(), xyPoints.xPointsQuaderEllipse2[2], xyPoints.yPointsQuaderEllipse2[2])) xyPoints.swapEllipse1And2();
		
		return xyPoints;
	}
	
	
	//Point-correction to make square out in order to cut everything without remaining spaces
	private double[] pointCorrection(double[] pointToDisplace, double[] referencePoint, double[] directionVector, double displacementFactor){
		double normFactor = 1/Math.sqrt(Math.pow(directionVector[0], 2) + Math.pow(directionVector[1], 2));
		double[] directionVectorNormalized = new double[2];
		directionVectorNormalized[0] = directionVector[0]*normFactor;
		directionVectorNormalized[1] = directionVector[1]*normFactor;
				
		if(!conditionsStillFulfilled(new double[]{pointToDisplace[0]+directionVectorNormalized[0]*displacementFactor,pointToDisplace[1]+directionVectorNormalized[1]*displacementFactor}
		                             , pointToDisplace, referencePoint)) displacementFactor*=-1;
		
		return new double[]{pointToDisplace[0]+directionVectorNormalized[0]*displacementFactor,pointToDisplace[1]+directionVectorNormalized[1]*displacementFactor};
		
	}
	
	
	private boolean conditionsStillFulfilled(double[] newPoint,double[] pointToDisplace, double[] referencePoint){
		boolean firstCondition = false;
		boolean secondCondition = false;
		if(pointToDisplace[0] <= referencePoint[0]) firstCondition = newPoint[0]<= pointToDisplace[0];
		else firstCondition = newPoint[0] > pointToDisplace[0];
		if(pointToDisplace[1] <= referencePoint[1]) secondCondition = newPoint[1]<= pointToDisplace[1];
		else secondCondition = newPoint[1] > pointToDisplace[1];
		
		return firstCondition && secondCondition;
	}
	
	
	
	private void clipEllipses(CellEllipse actEllipse, CellEllipse otherEllipse, XYPoints xyPoints){
		
		actEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsQuaderEllipse1, xyPoints.yPointsQuaderEllipse1, 4)));
		
		otherEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsQuaderEllipse2, xyPoints.yPointsQuaderEllipse2, 4)));
	
	   double areaOther = areaEllipse(otherEllipse.getClippedEllipse());
		double areaAct = areaEllipse(actEllipse.getClippedEllipse());
		//System.out.println("actEllipse / otherEllipse: "+ areaAct / areaOther);
		//System.out.println("otherEllipse / actEllipse: "+ areaOther / areaAct);
		if((areaAct / areaOther) < 0.03 || (areaOther / areaAct) < 0.03){
			//System.out.println("Swap");
			noOfSwaps++;
			actEllipse.resetClippedEllipse();
			otherEllipse.resetClippedEllipse();
			xyPoints.swapEllipse1And2();
			actEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsQuaderEllipse1, xyPoints.yPointsQuaderEllipse1, 4)));			
			otherEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsQuaderEllipse2, xyPoints.yPointsQuaderEllipse2, 4)));
			
		}
		
	}
	
	private double areaEllipse(Area ell){
		return ell.getBounds().getHeight()*ell.getBounds().getWidth();
	}
	
	private double[][] calculateFoci(double x, double y, double majorAxis, double minorAxis, double angleInRadians){				
		double[][] result = new double[2][2];
		double distance = Math.sqrt(Math.pow(majorAxis/2, 2) - Math.pow(minorAxis/2, 2));
			result[0] = new double[]{x - distance, y};
			result[1] = new double[]{x + distance, y};
			result [0] = rotatePoint(result[0], new double[]{x, y}, angleInRadians);
			result [1] = rotatePoint(result[1], new double[]{x, y}, angleInRadians);
		return result;
	}
	
	private double[] rotatePoint(double[] point, double[] center, double angleInRadians){		
	   double sin = Math.sin(angleInRadians);
	   double cos = Math.cos(angleInRadians);
	   double x = point[0] - center[0];
	   double y = point[1] - center[1];
	   double[][] rm = new double[][]{{cos, -1*sin},{sin, cos}};
	  return new double[]{(x *rm[0][0] + y *rm[0][1])+center[0], (x *rm[1][0] + y *rm[1][1])+center[1]};	   
	}
	
	private double[][] select2InterSectionPointsWithMinDistance(double[][] intersectionPoints, double[] actEll, double[] otherEll){
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
			
			
			
			
			//Only the two intersection point pair with the minimal distance should be taken
		/*	min1 = Double.POSITIVE_INFINITY;
			min2 = Double.POSITIVE_INFINITY;
			
			int[] minIndexPair1 = null;
			int[] minIndexPair2 = null;
			
			for(int i=0; i < intersectionPoints.length; i++){
				for(int n=0; n < intersectionPoints.length; n++){
					if(intersectionPoints[i][0] > 0 && intersectionPoints[i][1] > 0 && intersectionPoints[n][0]>0&& intersectionPoints[n][0]>0){
						double actDist = distance(intersectionPoints[i][0], intersectionPoints[i][1], intersectionPoints[n][0], intersectionPoints[n][0]);
						if(actDist > 0){
							if(actDist < min1){ 
								min2 = min1;
								min1 = actDist;
								minIndexPair2 = minIndexPair1;
								minIndexPair1 = new int[]{i, n};
							}
							else if(actDist < min2){ 
								min2 =actDist;
								minIndexPair2 = new int[]{i, n};
							}
						}
					}
				}
			}
			if(((min1Index == minIndexPair1[0]||min1Index == minIndexPair1[1])&&(min2Index == minIndexPair1[0] || min2Index == minIndexPair1[1]))||
				((min1Index == minIndexPair2[0]||min1Index == minIndexPair2[1])&&(min2Index == minIndexPair2[0] || min2Index == minIndexPair2[1]))){
				System.out.println("Successssssssssss");
			}
			else System.out.println("Errorrrrrrrrrrrrrrrrrrrrrr");
			*/
			
			return new double[][]{{intersectionPoints[min1Index][0], intersectionPoints[min1Index][1]},{intersectionPoints[min2Index][0], intersectionPoints[min2Index][1]},{0,0},{0,0}};
		}
	}
	
	
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//             OLD INTERSECTION POINT CALCULATION METHODS
	//----------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
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
	private double[][] getIntersectionPoints(Area a1, Area a2){
		
		ArrayList<Double> protocollXPoints = new ArrayList<Double>();
		ArrayList<Double> protocollYPoints = new ArrayList<Double>();
		
		//maximum of 4 intersection points for two ellipses
		double [][] intersectionPoints = new double[4][2];
					
			 	a1.intersect(a2);
	        
	        //return if the ellipses don't overlap
	        if(a1.isEmpty()) return null;        
	        
	        PathIterator it = a1.getPathIterator(null);
	        double[] d = new double[6];
	        double xOLD = 0;
	        double yOLD = 0;
	        int i = 0;       
	        
	        double previousSpX=0;
	        double previousSpY=0;
	        
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
							double equalPointsForIntersectionX = getEqualPointsForItersection(xOLD, d[0]);
							double equalPointsForIntersectionY = getEqualPointsForItersection(yOLD, d[1]);
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
	
	
	
	
	
}
