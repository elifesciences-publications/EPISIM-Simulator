package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sim.app.episim.biomechanics.Vertex;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.XYPoints;
import sim.portrayal.DrawInfo2D;


public abstract class AbstractCellEllipse implements Serializable{
	
	private transient Area clippedEllipse;
	private transient Area ellipseAsArea;
	
	private long id;
	private int x;
	private int y;
	private int majorAxis;
	private int minorAxis;
	private int heightExp;
	private int widthExp;
	private double area_IV;
	private double perimeter_IV;
	private double distanceToBL;
	private double orientationInRadians;
	private double scaleFactorWidth = 1;
	private double scaleFactorHeight = 1;
	
	private Color color;
	
	
	private transient DrawInfo2D lastDrawInfo2D = null;
	
	private HashMap<String, XYPoints> xyPointsOfEllipse;
  

	
	
	public static final char  SEPARATORCHAR = ';';
	
	
	public AbstractCellEllipse(long id, int x, int y, int majorAxis, int minorAxis, int height, int width, int orientationInDegrees, double area, double perimeter, double distanceToBL,  Color c){
		this.id = id;			
		clippedEllipse = new Area(new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis));
		ellipseAsArea = getClone(clippedEllipse);
		this.x = x;
		this.y = y;
		this.majorAxis = majorAxis;
		this.minorAxis = minorAxis;
		this.color = c;
		this.heightExp = height;
		this.widthExp = width;
		this.area_IV = area;
		this.perimeter_IV = perimeter;
		this.distanceToBL = distanceToBL;
		this.rotateCellEllipseInDegrees(orientationInDegrees);
		this.xyPointsOfEllipse = new HashMap<String, XYPoints>();
		CellEllipseIntersectionCalculationRegistry.getInstance().registerCellEllipse(this);
	}
	
	public void resetClippedEllipse(){ 
		if(ellipseAsArea== null) ellipseAsArea = new Area(new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis));
		clippedEllipse = getClone(this.ellipseAsArea);
		this.xyPointsOfEllipse.clear();
	}
	
	public void addXYPoints(XYPoints points, long idOtherEllipse){
		this.xyPointsOfEllipse.put(getIDString(idOtherEllipse), points);
	}
	
	public Vertex[] getIntersectionPoints(long idOtherEllipse){
		String idString = getIDString(idOtherEllipse);
		if(this.xyPointsOfEllipse.containsKey(idString)){
			return xyPointsOfEllipse.get(idString).intersectionPoints;
		}
		return null;
	}
	
	private String getIDString(long idOtherEllipse){
		return ""+this.id + SEPARATORCHAR + idOtherEllipse;
	}
	
   public int getX() { 
   	
   	if(lastDrawInfo2D != null){
   		return (int) lastDrawInfo2D.draw.x;
   	}      	
   	else return x; 
   	
   }
	
  public Map<String, XYPoints> getAllXYPointsOfEllipse(){
	  return this.xyPointsOfEllipse;
  }
	
   public int getY() { 
   	if(lastDrawInfo2D != null){
   		return (int) lastDrawInfo2D.draw.y;
   	}      	
   	else return y;      	
   }
	
   
  
	
   public double getMajorAxis() { 
   	
   	 if(lastDrawInfo2D != null){
	      	return (majorAxis * scaleFactorWidth);
	      }
	      else return majorAxis;  	
   }
	
   public void setMajorAxis(int majorAxis) {     
   	this.majorAxis = majorAxis;
   	resetEllipseAsArea();
   	testMajorMinorAxisSwap();
   }
	
   public void setXY(int x, int y){
   	this.x = x;
   	this.y = y;
   	resetEllipseAsArea();
   }
   
   public void setMajorAxisAndMinorAxis(int majorAxis, int minorAxis){
   	
   	this.majorAxis = majorAxis;
   	this.minorAxis = minorAxis;
   	resetEllipseAsArea();
   	testMajorMinorAxisSwap();
   }
   
   public double getMinorAxis() { 
      if(lastDrawInfo2D != null){
      	return (minorAxis * scaleFactorHeight);
      }
      else return minorAxis; 
   	
   
   }
	
   public void setMinorAxis(int minorAxis) {
   	
   	
   	this.minorAxis = minorAxis;
   	resetEllipseAsArea();
   	testMajorMinorAxisSwap();
   }
   
   private void resetEllipseAsArea(){
   	if(lastDrawInfo2D != null){
   		
   		
   		ellipseAsArea = new Area(new Ellipse2D.Double(lastDrawInfo2D.draw.x- (getMajorAxis()/2),lastDrawInfo2D.draw.y-(getMinorAxis()/2),getMajorAxis(),getMinorAxis()));
   	}
      else{
      	      	
      	ellipseAsArea = new Area(new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis));
      }	
      	
     this.rotateCellEllipseInRadians(this.orientationInRadians);
      	resetClippedEllipse();
      
   }
	
   public Area getEllipse() {
   	if(ellipseAsArea== null) ellipseAsArea = new Area(new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis));
		
   	return ellipseAsArea;
   }
   
   public Area getEllipseClone() { return getClone(ellipseAsArea);}

   public void clipAreaFromEllipse(Area area){
   	this.clippedEllipse.subtract(area); 
   }
   
   public Area getClippedEllipse(){ 
   	if(clippedEllipse== null) clippedEllipse = new Area(new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis));
   	return this.clippedEllipse; 
   }
  		
	public long getId() { return id; }
	
	public void rotateCellEllipseInDegrees(double degrees){			
	
		rotateCellEllipseInRadians(Math.toRadians(degrees));
	}
	
	public void rotateCellEllipseInRadians(double radians){
		this.orientationInRadians = radians%(2*Math.PI);
		AffineTransform trans = new AffineTransform();
		trans.rotate(radians, x, y);
		ellipseAsArea = new Area(trans.createTransformedShape(ellipseAsArea));
		if(this.clippedEllipse != null) this.clippedEllipse = new Area(trans.createTransformedShape(clippedEllipse));
	}
	
   public double getOrientationInRadians() { return orientationInRadians; }
   
   public double getOrientationInDegrees(){ return Math.toDegrees(orientationInRadians); }

   public int getHeightExp() {return heightExp;}
	
   public void setHeightExp(int heightExp) { this.heightExp = heightExp; }
	
   public int getWidthExp() {	return widthExp; }
	
   public void setWidthExp(int widthExp) { this.widthExp = widthExp; }
	
   public Color getColor() { return color; }
	
   public void setColor(Color color) { this.color = color; }   
   
   public double getArea_IV() {	return area_IV; }
	
   public void setArea_IV(double area) { this.area_IV = area; }
   
   public double getPerimeter_IV() {	return perimeter_IV; }
	
   public void setPerimeter_IV(double perimeter) { this.perimeter_IV = perimeter; }
   
   public double getDistanceToBL() { return distanceToBL; }
	
   public void setDistanceToBL(double distanceToBL) { this.distanceToBL = distanceToBL; }
   
   public Area getClone(Area shape){ return (Area) shape.clone(); }
   
   public DrawInfo2D getLastDrawInfo2D() { return lastDrawInfo2D; }
	
   public void setLastDrawInfo2D(DrawInfo2D lastDrawInfo2D, boolean resetRequired){
   	if(lastDrawInfo2D != null){
   		this.lastDrawInfo2D = lastDrawInfo2D;
   		this.scaleFactorHeight = lastDrawInfo2D.draw.height;
   		this.scaleFactorWidth = lastDrawInfo2D.draw.width;
   		testMajorMinorAxisSwap();
   		if(resetRequired){ 
   			resetEllipseAsArea();
   			
   		}
   	}
   }
   
   public void translateCell(DrawInfo2D newLastDrawInfo2D){
   	if(newLastDrawInfo2D != null && this.lastDrawInfo2D != null){
   		
   		AffineTransform trans = new AffineTransform();
   		trans.translate(newLastDrawInfo2D.draw.x - this.lastDrawInfo2D.draw.x, newLastDrawInfo2D.draw.y-this.lastDrawInfo2D.draw.y);
   		ellipseAsArea = new Area(trans.createTransformedShape(ellipseAsArea));
			if(this.clippedEllipse != null) this.clippedEllipse = new Area(trans.createTransformedShape(clippedEllipse));
   		
   		this.lastDrawInfo2D = newLastDrawInfo2D;
   		this.scaleFactorHeight = lastDrawInfo2D.draw.height;
   		this.scaleFactorWidth = lastDrawInfo2D.draw.width;
   		testMajorMinorAxisSwap();
   		
   	}
   }
   
   private void testMajorMinorAxisSwap(){
   	if(getMinorAxis() > getMajorAxis()) {
   		int tmp = minorAxis;
   		minorAxis = majorAxis;
   		majorAxis = tmp;
   		
   		double t = 	scaleFactorHeight;
   		this.scaleFactorHeight = scaleFactorWidth;
   		scaleFactorWidth = t;
   		
   	}
   }
   
   

}
