package sim.app.episim.model.biomechanics.vertexbased;


import sim.util.Double2D;

/*
 * This class provides a continuous torodoidal vertex field. Some operations are available for the x
 * direction only since continuity is only needed for the width but not for the height.
 * 
 * 
 */
public class ContinuousVertexField{
	
	private static ContinuousVertexField instance = new ContinuousVertexField();
	
	
	private double height = Double.POSITIVE_INFINITY;
	private double width = Double.POSITIVE_INFINITY;
	
	
	
	private ContinuousVertexField(){}
	private ContinuousVertexField(double width, double height){
		this.width = width;
		this.height = height;
	}	
	
	public static synchronized ContinuousVertexField getInstance(){
		return instance;
	}
	
	public static void initializeCondinousVertexField(int width, int height){
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
				new Line(x1 + width, y1, x2, y2, false),
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
		return newLines[minIndex];
	}
}
