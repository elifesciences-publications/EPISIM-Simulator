package sim.app.episim.model.biomechanics.vertexbased;


import sim.util.Double2D;


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
	
	
	public double getXLocationInField(Vertex v){
		return getXLocationInField(v, false);
	}
	public double getXLocationInField(Vertex v, boolean takeNewValues){
		return takeNewValues ? v.getNewX() % width: v.getDoubleX() % width;
	}
	
	public double getYLocationInField(Vertex v){
		return getYLocationInField(v, false);
	}
	public double getYLocationInField(Vertex v, boolean takeNewValues){
		return takeNewValues ? v.getNewY() % height: v.getDoubleY() % height;
	}
	
	public Double2D getLocationInField(Vertex v){
		return getLocationInField( v, false);
	}
	public Double2D getLocationInField(Vertex v, boolean takeNewValues){
		double x = takeNewValues ? v.getNewX() : v.getDoubleX();
		double y = takeNewValues ? v.getNewY() : v.getDoubleY();
		return new Double2D(x%width, y%height);
	}
	
	public double getMinEuclideanDistance(Vertex v1, Vertex v2){
		return getMinEuclideanDistance(v1, v2, false);
	}
	public double getMinEuclideanDistance(Vertex v1, Vertex v2, boolean takeNewValues){
		double x1 = takeNewValues ? v1.getNewX(): v1.getDoubleX();
		double x2 = takeNewValues ? v2.getNewX(): v2.getDoubleX();
		
		double y1 = takeNewValues ? v1.getNewY(): v1.getDoubleY();
		double y2 = takeNewValues ? v2.getNewY(): v2.getDoubleY();
		
		if(x1 > x2){
			double tmp = x1;
			x1=x2;
			x2=tmp;
		}
		if(y1 > y2){
			double tmp = y1;
			y1=y2;
			y2=tmp;
		}
		
		double xDifference = (x2-x1) > ((x1+width)-x2) ? ((x1+width)-x2) : (x2-x1);
		double yDifference = (y2-y1) > ((y1+height)-y2) ? ((x1+height)-y2) : (y2-y1);
		
		return Math.sqrt(Math.pow(xDifference, 2)+ Math.pow(yDifference, 2));
	}
	
	
	

}
