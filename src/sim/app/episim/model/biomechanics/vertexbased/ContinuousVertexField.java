package sim.app.episim.model.biomechanics.vertexbased;


import java.util.ArrayList;

import ec.util.MersenneTwisterFast;

import sim.util.Double2D;

/*
 * This class provides a continuous torodoidal vertex field. Some operations are available for the x
 * direction only since continuity is only needed for the width but not for the height.
 * 
 * 
 */
public class ContinuousVertexField{
	
	private class Quadrant{
		
		private double x1;
		private double x2;
		private double y1;
		private double y2;
		
		protected Quadrant(double x1, double y1, double x2, double y2){
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
		}
		protected boolean isVertexInQuadrant(Vertex v, boolean takeNewValues){
			double x = takeNewValues ? v.getNewX() : v.getDoubleX();
			double y = takeNewValues ? v.getNewY() : v.getDoubleY();
			
			return x >= x1 && x < x2 && y >= y1 && y < y2;
		}
	}
	
	private static ContinuousVertexField instance = new ContinuousVertexField();	
	
	private double height = Double.POSITIVE_INFINITY;
	private double width = Double.POSITIVE_INFINITY;	
	
	private ArrayList<Quadrant> quadrants = new ArrayList<Quadrant>();
	
	private ContinuousVertexField(){
		this(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	private ContinuousVertexField(double width, double height){
		this.width = width;
		this.height = height;
		if(width > Double.NEGATIVE_INFINITY && width < Double.POSITIVE_INFINITY 
				&&  height > Double.NEGATIVE_INFINITY && height < Double.POSITIVE_INFINITY){
			quadrants.add(new Quadrant(0,0,(width/2),(height/2)));
			quadrants.add(new Quadrant((width/2), 0, width, (height/2)));
			quadrants.add(new Quadrant(0,(height/2),(width/2),height));
			quadrants.add(new Quadrant((width/2),(height/2), width, height));
		}
		else if(width > Double.NEGATIVE_INFINITY && width < Double.POSITIVE_INFINITY){
			quadrants.add(new Quadrant(0,Double.NEGATIVE_INFINITY,(width/2),Double.POSITIVE_INFINITY));
			quadrants.add(new Quadrant((width/2), Double.NEGATIVE_INFINITY, width, Double.POSITIVE_INFINITY));
		}
		else if(height > Double.NEGATIVE_INFINITY && height < Double.POSITIVE_INFINITY){
			quadrants.add(new Quadrant(Double.NEGATIVE_INFINITY,0,Double.POSITIVE_INFINITY,(height/2)));
			quadrants.add(new Quadrant(Double.NEGATIVE_INFINITY, (height/2), Double.POSITIVE_INFINITY, height));
		}
	}	
	
	public static synchronized ContinuousVertexField getInstance(){
		return instance;
	}
	
	public static void initializeContinousVertexField(int width, int height){
		instance = new ContinuousVertexField(width, height);
	}	
	
	public double getXLocationInField(double x_value){
		return x_value < 0 ? ((x_value % width)+width) : x_value % width;
	}	
	public double getXLocationInField(Vertex v){
		return getXLocationInField(v, false);
	}
	public double getXLocationInField(Vertex v, boolean takeNewValues){
		return takeNewValues ? getXLocationInField(v.getNewX()) : getXLocationInField(v.getDoubleX());
	}
	
	public double getYLocationInField(double y_value){
		return y_value < 0 ? ((y_value % height)+height) : y_value % height;
	}
	public double getYLocationInField(Vertex v){
		return getYLocationInField(v, false);
	}
	public double getYLocationInField(Vertex v, boolean takeNewValues){
		return takeNewValues ? getYLocationInField(v.getNewY()) : getYLocationInField(v.getDoubleY());
	}
	
	public Double2D getLocationInField(Vertex v){
		return getLocationInField( v, false);
	}
	public Double2D getLocationInField(Vertex v, boolean takeNewValues){
		double x = takeNewValues ? v.getNewX() : v.getDoubleX();
		double y = takeNewValues ? v.getNewY() : v.getDoubleY();
		return new Double2D(getXLocationInField(x), getYLocationInField(y));
	}
	
	public double getMinEuclideanDistance(Vertex v1, Vertex v2){
		return getMinEuclideanDistance(v1, v2, false);
	}
	public double getMinEuclideanDistance(Vertex v1, Vertex v2, boolean takeNewValues){
		
		double xDifference = dxMinAbs(v1, v2, takeNewValues);
		double yDifference = dyMinAbs(v1, v2, takeNewValues);
		
		return Math.sqrt(Math.pow(xDifference, 2)+ Math.pow(yDifference, 2));
	}
	
	
	//return positive infinity if no borders are defined
	public double getCrossXBorderEuclideanDistance(Vertex v1, Vertex v2){
		return getCrossXBorderEuclideanDistance(v1, v2, false);
	}
	public double getCrossXBorderEuclideanDistance(Vertex v1, Vertex v2, boolean takeNewValues){
			
		double xDifference = dxCrossBorderAbs(v1, v2, takeNewValues);
		double yDifference = dyMinAbs(v1, v2, takeNewValues);
		
		return Math.sqrt(Math.pow(xDifference, 2)+ Math.pow(yDifference, 2));
	}
	
	
	public double dxMinAbs(double x1, double x2){
		if(x1 > x2){
			double tmp = x1;
			x1=x2;
			x2=tmp;
		}				
		return (x2-x1) > ((x1+width)-x2) ? ((x1+width)-x2) : (x2-x1);
	}	
	public double dxMinAbs(Vertex v1, Vertex v2){
		return dxMinAbs(v1, v2, false);
	}
	public double dxMinAbs(Vertex v1, Vertex v2, boolean takeNewValues){
		double x1 = takeNewValues ? v1.getNewX(): v1.getDoubleX();
		double x2 = takeNewValues ? v2.getNewX(): v2.getDoubleX();				
		return dxMinAbs(x1, x2);	
	}
	
	public double dyMinAbs(double y1, double y2){
		if(y1 > y2){
			double tmp = y1;
			y1=y2;
			y2=tmp;
		}		
		return (y2-y1) > ((y1+height)-y2) ? ((y1+height)-y2) : (y2-y1);
	}
	public double dyMinAbs(Vertex v1, Vertex v2){
		return dyMinAbs(v1, v2, false);
	}
	public double dyMinAbs(Vertex v1, Vertex v2, boolean takeNewValues){
		double y1 = takeNewValues ? v1.getNewY(): v1.getDoubleY();
		double y2 = takeNewValues ? v2.getNewY(): v2.getDoubleY();
		
		return dyMinAbs(y1, y2);
	}
	
	
	public double dxCrossBorderAbs(double x1, double x2){
		if(x1 > x2){
			double tmp = x1;
			x1=x2;
			x2=tmp;
		}				
		return ((x1+width)-x2);	
	}	
	public double dxCrossBorderAbs(Vertex v1, Vertex v2){
		return dxMinAbs(v1, v2, false);
	}
	public double dxCrossBorderAbs(Vertex v1, Vertex v2, boolean takeNewValues){
		double x1 = takeNewValues ? v1.getNewX(): v1.getDoubleX();
		double x2 = takeNewValues ? v2.getNewX(): v2.getDoubleX();				
		return dxCrossBorderAbs(x1, x2);
	}
	
	
	/**
	 * 
	 * Calculates the directionVector (v1.x - v2.x, v1.y - v2.y)
	 * 
	 */
	public double[] getDirectionVector(Vertex v1, Vertex v2){
		return getDirectionVector(v1, v2, false);
	}
	
	/**
	 * 
	 * Calculates the directionVector (v1.x - v2.x, v1.y - v2.y)
	 * 
	 */
	public double[] getDirectionVector(Vertex v1, Vertex v2, boolean takeNewValues){
		return new double[]{dxMinSign(v1, v2, takeNewValues), dyMinSign(v1, v2, takeNewValues)};
	}
	
	/**
	 * 
	 * Calculates the directionVector (x1 - x2, y1 - y2)
	 * 
	 */
	public double[] getDirectionVector(double x1, double y1, double x2, double y2){
		return new double[]{dxMinSign(x1, x2), dyMinSign(y1, y2)};
	}
	
	
	/**
	 * 
	 * Calculates the directionVector (v1.x - v2.x, v1.y - v2.y)
	 * 
	 */
	public double[] getNormDirectionVector(Vertex v1, Vertex v2){
		return getNormDirectionVector(v1, v2, false);
	}
	
	/**
	 * 
	 * Calculates the directionVector (v1.x - v2.x, v1.y - v2.y)
	 * 
	 */
	public double[] getNormDirectionVector(Vertex v1, Vertex v2, boolean takeNewValues){
		double[] directionVector = getDirectionVector(v1, v2, takeNewValues);
		double length = Math.sqrt(Math.pow(directionVector[0], 2)+Math.pow(directionVector[1], 2));
		directionVector[0] /= length;
		directionVector[1] /= length;
		return directionVector;
	}
	
	/**
	 * 
	 * returns min x1 - x2 in field
	 */
	public double dxMinSign(double x1, double x2){
		if(x1 > x2){
			x2 = (x1-x2) > ((x2+width)-x1)?(x2+width):x2;
		}
		else if(x1 < x2){
			x1 = (x2-x1) > ((x1+width)-x2)?(x1+width):x1;
		}
		return x1 - x2;
	}
	
	public double dxMinSign(Vertex v1, Vertex v2){
		return dxMinSign(v1, v2, false);
	}
	
	public double dxMinSign(Vertex v1, Vertex v2, boolean takeNewValues){
		double x1 = takeNewValues ? v1.getNewX(): v1.getDoubleX();
		double x2 = takeNewValues ? v2.getNewX(): v2.getDoubleX();
		return dxMinSign(x1, x2);
	}


	/**
	 * 
	 * returns min y1 - y2 in field
	 */
	public double dyMinSign(double y1, double y2){
			if(y1 > y2){
				y2 = (y1-y2) > ((y2+height)-y1)?(y2+height):y2;
			}
			else if(y1 < y2){
				y1 = (y2-y1) > ((y1+height)-y2)?(y1+height):y1;
			}
			return y1 - y2;
	}
	
	public double dyMinSign(Vertex v1, Vertex v2){
		return dyMinSign(v1, v2, false);
	}
	
	public double dyMinSign(Vertex v1, Vertex v2, boolean takeNewValues){
		double y1 = takeNewValues ? v1.getNewY(): v1.getDoubleY();
		double y2 = takeNewValues ? v2.getNewY(): v2.getDoubleY();
		return dxMinSign(y1, y2);
	}
	
	public Line getNewLineWithMinLength(Line line){
		double x1 = line.getDoubleX1();
		double x2 = line.getDoubleX2();
		
		double y1 = line.getDoubleY1();
		double y2 = line.getDoubleY2();
				
		Line[] newLines = new Line[]{
				new Line(x1, y1, x2, y2, false),
				new Line(x1+width, y1, x2, y2, false),
				new Line(x1, y1+height, x2, y2, false),
				new Line(x1+width, y1+height, x2, y2, false),
				new Line(x1, y1, x2+width, y2, false),
				new Line(x1, y1, x2, y2+height, false),
				new Line(x1, y1, x2+width, y2+height, false)				
		};
		double minLength = Double.POSITIVE_INFINITY;
		int minIndex = -1;
 		for(int i = 0; i < newLines.length; i++){
			if(newLines[i].getLengthDependingOnSetXYValues(false) < minLength){
				minLength=newLines[i].getLengthDependingOnSetXYValues(false);
				minIndex = i;
			}
		}
 		if(minIndex == -1){
 			System.out.println("Found no line with min length");
 			return newLines[0];
 		}
		return newLines[minIndex];
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayFirstVertexReferenceUnsigned(Vertex[] vertices){
		return getMinDistanceTransformedVertexArray(vertices[0], vertices, false, false);
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayFirstVertexReferenceUnsigned(Vertex[] vertices, boolean takeNewValues){
		return getMinDistanceTransformedVertexArray(vertices[0], vertices, takeNewValues, false);
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayFirstVertexReferenceSigned(Vertex[] vertices){
		return getMinDistanceTransformedVertexArray(vertices[0], vertices, false, true);
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayFirstVertexReferenceSigned(Vertex[] vertices, boolean takeNewValues){
		return getMinDistanceTransformedVertexArray(vertices[0], vertices, takeNewValues, true);
	}	
	
	public Vertex[] getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceUnsigned(Vertex[] vertices){
		return getMinDistanceTransformedVertexArray(getMajorityQuadrantReferenceVertex(vertices, false), vertices, false, false);
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceUnsigned(Vertex[] vertices, boolean takeNewValues){
		return getMinDistanceTransformedVertexArray(getMajorityQuadrantReferenceVertex(vertices, takeNewValues), vertices, takeNewValues, false);
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(Vertex[] vertices){
		return getMinDistanceTransformedVertexArray(getMajorityQuadrantReferenceVertex(vertices, false), vertices, false, true);
	}
	
	public Vertex[] getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(Vertex[] vertices, boolean takeNewValues){
		return getMinDistanceTransformedVertexArray(getMajorityQuadrantReferenceVertex(vertices, takeNewValues), vertices, takeNewValues, true);
	}
	
	private Vertex[] getMinDistanceTransformedVertexArray(Vertex referenceVertex, Vertex[] vertices, boolean takeNewValues, boolean signed){
		
		double refX = takeNewValues ? referenceVertex.getNewX() : referenceVertex.getDoubleX();
		double refY = takeNewValues ? referenceVertex.getNewY() : referenceVertex.getDoubleY();
		Vertex[] transformedVertices = new Vertex[vertices.length];
		
		for(int i = 0; i < vertices.length; i++){
			
			double actX = takeNewValues ? vertices[i].getNewX() : vertices[i].getDoubleX();
			double actY = takeNewValues ? vertices[i].getNewY() : vertices[i].getDoubleY();
			
			double transX = refX;
			double transY = refY;
			transX += signed ? dxMinSign(vertices[i], referenceVertex, takeNewValues) : dxMinAbs(vertices[i], referenceVertex, takeNewValues);
			transY += signed ? dyMinSign(vertices[i], referenceVertex, takeNewValues) : dyMinAbs(vertices[i], referenceVertex, takeNewValues);
						
			double newX = (Math.abs(refX - transX) < Math.abs(refX - actX) && Math.abs(Math.abs(refX - transX)-Math.abs(refX - actX)) >= 0.0001) ? transX : actX;
			double newY = (Math.abs(refY - transY) < Math.abs(refY - actY) && Math.abs(Math.abs(refY - transY)-Math.abs(refY - actY)) >= 0.0001) ? transY : actY;
			
			transformedVertices[i] = new Vertex(newX, newY, false);		
		}
		return transformedVertices;
	}
	
	private Vertex getMajorityQuadrantReferenceVertex(Vertex[] vertices, boolean takeNewValues){
		MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());
		if(quadrants.isEmpty()){
			return vertices[random.nextInt(vertices.length)];
		}
		else{
			int[] noOfVerticesInQuadrant = new int[quadrants.size()];
			ArrayList[] verticesInQuadrantsLists = new ArrayList[quadrants.size()];
			
			for(int i = 0; i < verticesInQuadrantsLists.length; i++) verticesInQuadrantsLists[i] = new ArrayList();
			
			for(int i = 0; i < vertices.length; i++){
				for(int n = 0; n < quadrants.size(); n++){
					if(quadrants.get(n).isVertexInQuadrant(vertices[i], takeNewValues)){ 
						noOfVerticesInQuadrant[n]++;
						verticesInQuadrantsLists[n].add(vertices[i]);
						break;
					}
				}
			}
			
			int maxNoOfVerticesInOneQuadrant =0;
			int maxIndex = -1;
			for(int i = 0; i < noOfVerticesInQuadrant.length; i++){
				if(noOfVerticesInQuadrant[i] > maxNoOfVerticesInOneQuadrant){
					maxNoOfVerticesInOneQuadrant = noOfVerticesInQuadrant[i];
					maxIndex = i;
				}
			}
			Object obj = verticesInQuadrantsLists[maxIndex].get(random.nextInt(maxNoOfVerticesInOneQuadrant));
			if(obj instanceof Vertex) return (Vertex) obj;
		}
		return null;
	}
	
	
}
