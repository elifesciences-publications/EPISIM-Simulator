package sim.app.episim.model.biomechanics.vertexbased;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import ec.util.MersenneTwisterFast;


public class Line {
	
	private double x1;
	private double y1;
	private double x2;
	private double y2;
	private Vertex v1;
	private Vertex v2;
	
	public Line(double x1, double y1, double x2, double y2){
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
	}
	
	public Line(Vertex v1, Vertex v2){
		if(v1 == null || v2 == null) throw new IllegalArgumentException("Neither v1 nor v2 must be null");
		this.x1 = v1.getDoubleX();
		this.y1 = v1.getDoubleY();
		this.x2 = v2.getDoubleX();
		this.y2 = v2.getDoubleY();
		this.v1 = v1;
		this.v2 = v2;
		
		if(!ContinuousVertexField.getInstance().isRegisteredInField(v1)) ContinuousVertexField.getInstance().addVertexToField(v1);
		if(!ContinuousVertexField.getInstance().isRegisteredInField(v2)) ContinuousVertexField.getInstance().addVertexToField(v2);
	}
	
	public double getDistanceOfVertex(Vertex v, boolean takeNewValues, boolean withinLineSegment){

		double x_Vertex = takeNewValues ? v.getNewX() : v.getDoubleX();
		double y_Vertex = takeNewValues ? v.getNewY() : v.getDoubleY();
		Vertex isp = getIntersectionPointOfLineThroughVertex(v, takeNewValues, withinLineSegment);
		if(isp!=null)return isp.edist(new Vertex(x_Vertex, y_Vertex));
		else return Double.POSITIVE_INFINITY;
	}
	
	public double getLength(){
		return (v1 != null && v2!= null)? v1.edist(v2) : Math.sqrt(Math.pow((x1-x2), 2)+Math.pow((y1-y2), 2));
	}
	
	public Vertex getIntersectionPointOfLineThroughVertex(Vertex v, boolean takeNewValues, boolean withinLineSegment){
		double[] directionVectorOfLine = new double[]{x2-x1, y2-y1};
		double[] directionVectorOfOrthogonalLine = new double[]{directionVectorOfLine[1],-1*directionVectorOfLine[0]};
		
		double x_Vertex = takeNewValues ? v.getNewX() : v.getDoubleX();
		double y_Vertex = takeNewValues ? v.getNewY() : v.getDoubleY();
				
		Line intersectionLine = new Line(x_Vertex, y_Vertex, x_Vertex+directionVectorOfOrthogonalLine[0], y_Vertex+directionVectorOfOrthogonalLine[1]);
		return withinLineSegment ? this.getIntersectionOfLinesInLineSegment(intersectionLine): this.getIntersectionOfLines(intersectionLine);
	}
	
	
	
	public void setNewValuesOfVertexToDistance(Vertex v, double distance){
		Vertex isp = getIntersectionPointOfLineThroughVertex(v, true, true);
		if(isp != null){
			double[] directionVector = new double[]{v.getDoubleX() - isp.getDoubleX(), v.getDoubleY() - isp.getDoubleY()};
			double normFactor = Math.sqrt(Math.pow(directionVector[0],2)+Math.pow(directionVector[1],2));
			directionVector[0]/=normFactor;
			directionVector[1]/=normFactor;
			v.setNewX((v.getNewX() + (directionVector[0]*distance)));
			v.setNewY((v.getNewY() + (directionVector[1]*distance)));
		}
	}	
	
	/*
	 * 
	 * This method could be easily extended to three dimensions but is slower than the one we use for two dimensions
	 * 
	 */
/*	
	public double getDistanceOfVertex(Vertex v){
		
		double[] directionVectorOfLine = new double[]{x2-x1, y2-y1};
		double lambda = 0;		
		//calculate plane that is perpendicular to the line using Vertex v and the Intersection point of the line with this plane
		double scalarOfPlane = directionVectorOfLine[0]*v.getDoubleX() + directionVectorOfLine[1]*v.getDoubleY();
		
		double scalarOfPlaneLineIntersection =  directionVectorOfLine[0]* x1 + directionVectorOfLine[1]*y1;
		double denominator = (Math.pow(directionVectorOfLine[0], 2) + Math.pow(directionVectorOfLine[0], 2));
		if(denominator != 0){
			lambda = (scalarOfPlane - scalarOfPlaneLineIntersection) / denominator;
		}
		
		double[] intersectionPoint ={(x1 + lambda*directionVectorOfLine[0]), (y1 + lambda*directionVectorOfLine[1])};
		
		double distance = Math.sqrt(Math.pow((intersectionPoint[0] - v.getDoubleX()),2)+ Math.pow((intersectionPoint[1] - v.getDoubleY()),2));
		
		if(distance == 0 || isIntersectionOfLinesInLineSegment(new Line(v.getDoubleX(), v.getDoubleY(), intersectionPoint[0], intersectionPoint[1]))){
			
			 return distance;
		}
		
		return Double.POSITIVE_INFINITY;
	}*/
	
	public boolean belongsVertexToLine(Vertex vertexToTest){
		if(v1 != null && v2 != null){
			return vertexToTest.equals(v1) || vertexToTest.equals(v2);
		}
		
		return false;
	}
	
	
	/**
	 * @param v1 first point line one (first cell vertex)
	 * @param v2 second point line one (second cellvertex)
	 * 
	 * @param v3 first point line two (cell center)
	 * @param v4 second point line two( point with max distance on circle)
	 * @return intersection point, returns null if there is no intersection or if the intersection point is not on the line segment of described by the coordinates of this line
	 */
	public Vertex getIntersectionOfLinesInLineSegment(Line otherLine){
		
		double denominator =  ((otherLine.getDoubleY2() -otherLine.getDoubleY1())*(x2-x1)) - ((otherLine.getDoubleX2()-otherLine.getDoubleX1())*(y2-y1));
		
		if(denominator != 0){
			double u_a = (((otherLine.getDoubleX2()-otherLine.getDoubleX1())*(y1-otherLine.getDoubleY1()))-((otherLine.getDoubleY2()-otherLine.getDoubleY1())*(x1-otherLine.getDoubleX1()))) / denominator;			
			
			//only if u_a is between 0 and 1  the intersection point lies on the line segment described by the two line vertices v1 and v2
			if(u_a >= 0 && u_a <= 1){
				double x_s = x1 + u_a*(x2-x1);
				double y_s = y1 + u_a*(y2-y1);
				return new Vertex(x_s, y_s);
			}
		}
		
		return null;
	}
	
	public boolean isIntersectionOfLinesInLineSegment(Line otherLine){
		return getIntersectionOfLinesInLineSegment(otherLine) != null;
	}
	
	public boolean isIntersectionOfLinesInLineSegment(Line otherLine, double deltaOfISP){
		Vertex intersectionPoint = getIntersectionOfLinesInLineSegment(otherLine);
		if(intersectionPoint != null){
			return intersectionPoint.edist(v1) > deltaOfISP && intersectionPoint.edist(v2) > deltaOfISP;
		}
			
		return false;
	}
	
	public double getIntersectionAngleInDegreesWithOtherLine(Line otherLine){
		double[] directionVector1 = new double[]{x2-x1, y2-y1};
		double[] directionVector2 = new double[]{otherLine.getDoubleX2()-otherLine.getDoubleX1(), otherLine.getDoubleY2()-otherLine.getDoubleY1()};
		
		double denominator = Math.sqrt(Math.pow(directionVector1[0], 2)+Math.pow(directionVector1[1], 2))*Math.sqrt(Math.pow(directionVector2[0], 2)+Math.pow(directionVector2[1], 2));
		if(denominator != 0){
			double enumerator = directionVector1[0]*directionVector2[0] + directionVector1[1]*directionVector2[1];
			return Math.toDegrees(Math.acos(enumerator/denominator));
		}
		return Double.NEGATIVE_INFINITY;
	}
	
	
	/**
	 * @param v1 first point line one (first cell vertex)
	 * @param v2 second point line one (second cell vertex)
	 * @param v3 first point line two (cell center)
	 * @param v4 second point line two(point with max distance on circle)
	 * @return intersection point, returns null if there is no intersection
	 */
	public Vertex getIntersectionOfLines(Line otherLine){
		
		double denominator =  ((otherLine.getDoubleY2() - otherLine.getDoubleY1())*(x2-x1)) - ((otherLine.getDoubleX2()-otherLine.getDoubleX1())*(y2-y1));
		
		if(denominator != 0){
			double u_a = (((otherLine.getDoubleX2()-otherLine.getDoubleX1())*(y1-otherLine.getDoubleY1()))-((otherLine.getDoubleY2()-otherLine.getDoubleY1())*(x1-otherLine.getDoubleX1()))) / denominator;		
			double x_s = x1 + u_a*(x2-x1);
			double y_s = y1 + u_a*(y2-y1);
			return new Vertex(x_s, y_s);
			
		}
		
		return null;
	}
	
	public CellPolygon[] getCellPolygonsOfLine(){
		HashSet<CellPolygon> cellPolygonsAssociatedWithLine = new HashSet<CellPolygon>();
		if(v1 != null && v2 != null){
			HashSet<CellPolygon> cellsV1 = new HashSet<CellPolygon>();
			HashSet<CellPolygon> cellsV2 = new HashSet<CellPolygon>();
			cellsV1.addAll(Arrays.asList(v1.getCellsJoiningThisVertex()));
			cellsV2.addAll(Arrays.asList(v2.getCellsJoiningThisVertex()));
			for(CellPolygon pol : cellsV1){
				if(cellsV2.contains(pol)) cellPolygonsAssociatedWithLine.add(pol);
			}
		}		
		return cellPolygonsAssociatedWithLine.toArray(new CellPolygon[cellPolygonsAssociatedWithLine.size()]);
	}
	
	public void setIntX1(int x1){ setDoubleX1((double)x1); }
	public int getIntX1(){ return (int)Math.round(getDoubleX1()); }
	public void setIntY1(int y1){ setDoubleY1((double)y1); }
	public int getIntY1(){ return (int)Math.round(getDoubleY1()); }
	public void setIntX2(int x2){ setDoubleX2((double)x2); }
	public int getIntX2(){ return (int)Math.round(getDoubleX2()); }
	public void setIntY2(int y2){ setDoubleY2((double)y2); }
	public int getIntY2(){ return (int)Math.round(getDoubleY2()); }
	
   public double getDoubleX1() { return x1; }	
   public void setDoubleX1(double x1) { this.x1 = x1; }	
   public double getDoubleY1() { return y1; }
	
   public void setDoubleY1(double y1) { this.y1 = y1; }	
   public double getDoubleX2() {	return x2; }
	
   public void setDoubleX2(double x2) { this.x2 = x2; }	
   public double getDoubleY2() { return y2; }	
   public void setDoubleY2(double y2){ this.y2 = y2; }
   
   public Vertex getV1() {	return v1; }	
   public void setV1(Vertex v1) { 
   	this.v1 = v1; 
   	this.x1 = v1.getDoubleX();
   	this.y1 = v1.getDoubleY();
   }	
   public Vertex getV2(){ return v2; }	
   public void setV2(Vertex v2) { 
   	this.v2 = v2;
   	this.x2 = v2.getDoubleX();
   	this.y2 = v2.getDoubleY();
   }
	
   public int hashCode() {

	   final int prime = 31;
	   int result = 1;
	   long temp;
	   temp = Double.doubleToLongBits(x1);
	   result = prime * result + (int) (temp ^ (temp >>> 32));
	   temp = Double.doubleToLongBits(x2);
	   result = prime * result + (int) (temp ^ (temp >>> 32));
	   temp = Double.doubleToLongBits(y1);
	   result = prime * result + (int) (temp ^ (temp >>> 32));
	   temp = Double.doubleToLongBits(y2);
	   result = prime * result + (int) (temp ^ (temp >>> 32));
	   return result;
   }
	
   public boolean equals(Object obj) {

	   if(this == obj)
		   return true;
	   if(obj == null)
		   return false;
	   if(getClass() != obj.getClass())
		   return false;
	   Line other = (Line) obj;
	   if(this.v1!=null &&this.v2!=null && other.getV1() != null && other.getV2()!=null){
	   	if(this.v1.equals(other.getV1()) && this.v2.equals(other.getV2())) return true;
	   	else if(this.v2.equals(other.getV1()) && this.v1.equals(other.getV2())) return true;
	   }
	   if(Double.doubleToLongBits(x1) != Double.doubleToLongBits(other.x1))
		   return false;
	   if(Double.doubleToLongBits(x2) != Double.doubleToLongBits(other.x2))
		   return false;
	   if(Double.doubleToLongBits(y1) != Double.doubleToLongBits(other.y1))
		   return false;
	   if(Double.doubleToLongBits(y2) != Double.doubleToLongBits(other.y2))
		   return false;
	   return true;
   }
   
   
	
   

}
