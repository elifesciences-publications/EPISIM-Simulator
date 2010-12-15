package sim.app.episim.model.biomechanics.vertexbased;


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
	}
	
	
	
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
	}
	
	public boolean belongsVertexToLine(Vertex vertexToTest){
		if(v1 != null && v2 != null){
			return vertexToTest.equals(v1) || vertexToTest.equals(v2);
		}
		
		return false;
	}
	
	
	/**
	 * @param v1 first point line one (first cell vertex)
	 * @param v2 second point line one (second cellvertex)
	 * @param v3 first point line two (cell center)
	 * @param v4 second point line two( point with max distance on circle)
	 * @return intersection point, returns null if there is no intersection
	 */
	public Vertex getIntersectionOfLinesInLineSegment(Line otherLine){
		
		double denominator =  ((otherLine.getDoubleY2() -otherLine.getDoubleY1())*(x2-x1)) - ((otherLine.getDoubleX2()-otherLine.getDoubleX1())*(y2-y1));
		
		if(denominator != 0){
			double u_a = (((otherLine.getDoubleX2()-otherLine.getDoubleX1())*(y1-otherLine.getDoubleY1()))-((otherLine.getDoubleY2()-otherLine.getDoubleY1())*(x1-otherLine.getDoubleX1()))) / denominator;			
			
			//only if u_a is between 0 and 1  the intersection point lies on the line segment described by the two cell vertices v1 and v2
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