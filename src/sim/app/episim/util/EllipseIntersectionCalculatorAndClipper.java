package sim.app.episim.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


import sim.app.episim.model.visualization.AbstractCellEllipse;
import sim.app.episim.model.visualization.CellEllipse;
;




public class EllipseIntersectionCalculatorAndClipper {
	
	private static EllipseIntersectionCalculatorAndClipper instance;
	
	private static long noOfCalls =0;
	private static double cumulativeTimeInMillis;
	private static long noOfSwaps = 0;
	
	private static int noOfRuns = 0;
	private static int maxNoOfRuns = 0;
	
	public static class IntersectionPoints implements java.io.Serializable{
		
		public double[] intersectionPointsX;
		public double[] intersectionPointsY;
				
		
		
		public String toString(){
			StringBuffer str = new StringBuffer();
			str.append("Intersection Points:\n");
			for(int i = 0; i < intersectionPointsX.length && i < intersectionPointsY.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+intersectionPointsX[i]+","+intersectionPointsY[i]+")  ");
			
			return str.toString();
		}
		
	}
	
	
	
	
	
	public static IntersectionPoints getClippedEllipsesAndIntersectionPoints(CellEllipse actEllipse, CellEllipse otherEllipse){
		if(instance == null) instance = new EllipseIntersectionCalculatorAndClipper();
		noOfCalls++;
		long timeStart = (new Date()).getTime();
		
		IntersectionPoints p = getClippedEllipsesAndXYPoints(null, actEllipse, otherEllipse);
		long timeEnd = (new Date()).getTime();
		cumulativeTimeInMillis += (timeEnd-timeStart);
		if((noOfCalls % 100000) == 0)System.out.println("Durchschnittliche Dauer ("+ noOfCalls+ ", noOfSwaps: "+noOfSwaps+"): "+ (cumulativeTimeInMillis/noOfCalls));
		return p;
	}
	
	public static IntersectionPoints getClippedEllipsesAndXYPoints(Graphics2D g, CellEllipse actEllipse, CellEllipse otherEllipse){
		if(instance == null) instance = new EllipseIntersectionCalculatorAndClipper();
		
		IntersectionPoints p = instance.calculateClippedEllipses(g, actEllipse, otherEllipse);
		
		if(p != null){
			actEllipse.addIntersectionPoints(p, otherEllipse.getId());
			otherEllipse.addIntersectionPoints(p, actEllipse.getId());
		}
		
		
		return p;
	}	
	
	private IntersectionPoints calculateClippedEllipses(Graphics2D g, CellEllipse actEllipse, CellEllipse otherEllipse){
		double distanceEllipses =distance(actEllipse.getX(), actEllipse.getY(), otherEllipse.getX(), otherEllipse.getY());
		//System.out.println("Ellipsen Distanz: "+ distanceEllipses);
		if(distanceEllipses > 0 && distanceEllipses < ((actEllipse.getMajorAxis()/2)+(otherEllipse.getMajorAxis()/2))){
		double [][] intersectionPoints = newtonIntersectionCalculation(actEllipse, otherEllipse);
			
			if(intersectionPoints != null && getNumberOfIntersectionPoints(intersectionPoints)>=2){
				
				CellEllipseIntersectionCalculationRegistry.getInstance().addIntersectionCellEllipses(actEllipse.getId(), otherEllipse.getId());
				if(g!= null){
					for(int i = 0; i < intersectionPoints.length; i++){
						drawPoint(g, intersectionPoints[i][0],intersectionPoints[i][1], 5, Color.GREEN);
					}
				}
				intersectionPoints = select2InterSectionPointsWithMinDistance(intersectionPoints, new double[]{actEllipse.getX(), actEllipse.getY()},new double[]{otherEllipse.getX(), otherEllipse.getY()});
				IntersectionPoints isPoints = getIntersectionPoints(intersectionPoints[0], intersectionPoints[1]);
				clipEllipses(actEllipse, otherEllipse, isPoints);
				return isPoints;
			}
		}
		
		return null;
	}
	
	private int getNumberOfIntersectionPoints(double [][] intersectionPoints){
		int counter = 0;
		for(int i = 0; i < intersectionPoints.length; i++){
			if(intersectionPoints[i][0]!=0 && intersectionPoints[i][1]!=0){
				counter++;
			}
		}
		return counter;
	}
	
	
	private double[][] newtonIntersectionCalculation(AbstractCellEllipse actEllipse, AbstractCellEllipse otherEllipse){//double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double phi){
		
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
	
	public static double[] calculatePointOnEllipse(double x, double y, double major_axis, double minor_axis, double alpha, double orientation){
		
		double cos_alpha = Math.cos(alpha);
		double sin_alpha = Math.sin(alpha);
		double cos_phi = Math.cos(orientation);
		double sin_phi = Math.sin(orientation);
		
		double point_x = x + major_axis * cos_phi*cos_alpha -  minor_axis*sin_phi*sin_alpha;
		double point_y = y + minor_axis*cos_phi*sin_alpha + major_axis*sin_phi*cos_alpha;
		
		return new double[]{point_x, point_y};
	}
	
	
	
	private IntersectionPoints getIntersectionPoints(double[] sp1, double[] sp2){		
		IntersectionPoints isps = new IntersectionPoints();
		isps.intersectionPointsX = new double[]{sp1[0], sp2[0]};
		isps.intersectionPointsY = new double[]{sp1[1], sp2[1]};
		
		return isps;
	}
	
	private Area getClippingAreaForEllipse(IntersectionPoints isPoints, AbstractCellEllipse cellEllipse){
		Rectangle2D boundingBox = cellEllipse.getEllipse().getBounds2D();
		Point2D.Double[] boundingPoints;/* = new Point2D.Double[]{new Point2D.Double(boundingBox.getMinX(), boundingBox.getMinY()),
				new Point2D.Double(boundingBox.getMaxX(), boundingBox.getMinY()),
				new Point2D.Double(boundingBox.getMaxX(), boundingBox.getMaxY()),
				new Point2D.Double(boundingBox.getMinX(), boundingBox.getMaxY())};*/
		boundingPoints = getBoundingPoints(cellEllipse);
		
		double minDistance = Double.POSITIVE_INFINITY;
		int[] minIndices = new int[]{-1, -1};
		for(int p = 0; p < minIndices.length; p++){
			for(int i = 0; i < boundingPoints.length;i++){
				double actDistance = boundingPoints[i].distance(isPoints.intersectionPointsX[p], isPoints.intersectionPointsY[p]);
				if(actDistance < minDistance){
					minIndices[p]=i;
					minDistance = actDistance;
				}
			}
			minDistance = Double.POSITIVE_INFINITY;
		}
		
		Path2D.Double path = new Path2D.Double();
		int minMinIndex = minIndices[0] < minIndices[1] ? minIndices[0] : minIndices[1];
		int maxMinIndex = minIndices[0] > minIndices[1] ? minIndices[0] : minIndices[1];
		if(minIndices[0]== minIndices[1]){
			path.moveTo(isPoints.intersectionPointsX[0], isPoints.intersectionPointsY[0]);
			path.lineTo(isPoints.intersectionPointsX[1], isPoints.intersectionPointsY[1]);
			path.lineTo(boundingPoints[minIndices[0]].x, boundingPoints[minIndices[0]].y);
			path.closePath();
		}
		else if((maxMinIndex-minMinIndex)==1 || ((maxMinIndex+1)%boundingPoints.length)==minMinIndex){
			
			path.moveTo(isPoints.intersectionPointsX[0], isPoints.intersectionPointsY[0]);
			path.lineTo(isPoints.intersectionPointsX[1], isPoints.intersectionPointsY[1]);
			path.lineTo(boundingPoints[minIndices[1]].x, boundingPoints[minIndices[1]].y);
			path.lineTo(boundingPoints[minIndices[0]].x, boundingPoints[minIndices[0]].y);
			path.closePath();
		}
		else{
			int testIndex1 = minIndices[0] < minIndices[1] ? (minIndices[0] + 1) : (minIndices[1]+1);
			int testIndex2 = minIndices[0] > minIndices[1] ? ((minIndices[0] + 1)%boundingPoints.length) : ((minIndices[1] + 1)%boundingPoints.length);
			Point2D.Double[] polygon1 = new Point2D.Double[]{
					new Point2D.Double(isPoints.intersectionPointsX[0], isPoints.intersectionPointsY[0]),
					new Point2D.Double(isPoints.intersectionPointsX[1], isPoints.intersectionPointsY[1]),
					
					boundingPoints[minIndices[1]],
					boundingPoints[testIndex1],
					boundingPoints[minIndices[0]]
			};
			Point2D.Double[] polygon2 = new Point2D.Double[]{
					new Point2D.Double(isPoints.intersectionPointsX[0], isPoints.intersectionPointsY[0]),
					new Point2D.Double(isPoints.intersectionPointsX[1], isPoints.intersectionPointsY[1]),
					
					boundingPoints[minIndices[1]],
					boundingPoints[testIndex2],
					boundingPoints[minIndices[0]]
			};
			double areaPolygon1 = getPolygonArea(polygon1);
			double areaPolygon2 = getPolygonArea(polygon2);
			Point2D.Double[] choosenPolygon = areaPolygon1 < areaPolygon2 ? polygon1 : polygon2;
			
			for(int i = 0; i < choosenPolygon.length; i++){
				if(i==0) path.moveTo(choosenPolygon[i].x, choosenPolygon[i].y);
				else path.lineTo(choosenPolygon[i].x, choosenPolygon[i].y);
			}
			path.closePath();
		}		
		return new Area(path);
	}
	
	private Point2D.Double[] getBoundingPoints(AbstractCellEllipse cellEllipse){
		Path2D.Double boundingPath = cellEllipse.getEllipseBoundingBox();
		PathIterator pathIter = boundingPath.getPathIterator(null);
		Point2D.Double[] boundingPoints = new Point2D.Double[4];
		int pointCounter = 0;
		while(!pathIter.isDone()){
			double[] actPoint = new double[2];
			int type = pathIter.currentSegment(actPoint);
			if(type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO){
				if(pointCounter < boundingPoints.length) boundingPoints[pointCounter++]=new Point2D.Double(actPoint[0], actPoint[1]);
			}		
			pathIter.next();
		}
		return boundingPoints;
	}
	
	private double getPolygonArea(Point2D.Double[] polygon){
		double area = 0;
		for(int i = 0; i < polygon.length; i++){
			double x_i = polygon[i].x;
			double y_i = polygon[i].y;
			double x_i_1 = polygon[(i+1)%polygon.length].x;
			double y_i_1 = polygon[(i+1)%polygon.length].y;
			area+=((x_i+x_i_1)*(y_i_1-y_i));
		}
		return Math.abs(area)/2;
	}
	
	private void clipEllipses(CellEllipse actEllipse, CellEllipse otherEllipse, IntersectionPoints isPoints){		
		actEllipse.clipAreaFromEllipse(getClippingAreaForEllipse(isPoints, actEllipse));
		otherEllipse.clipAreaFromEllipse(getClippingAreaForEllipse(isPoints, otherEllipse));
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
			for(int i = 0; i < intersectionPoints.length && i<4; i++){
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
