package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.IntersectionPoints;
import sim.portrayal.DrawInfo2D;


public abstract class AbstractCellEllipse implements Serializable{
	
	private transient Area clippedEllipse;
	private transient Area ellipseAsArea;
	
	private long id;
	private double x;
	private double y;
	private double majorAxis;
	private double minorAxis;
	private double heightExp;
	private double widthExp;
	private double area_IV;
	private double perimeter_IV;
	private double distanceToBL;
	private double orientationInRadians;

	
	private Path2D.Double ellipseBoundingBox;
	private Color color;
	
	
	private transient SimulationDisplayProperties lastDisplayProps = null;
	
	private HashMap<String, IntersectionPoints> intersectionPointsOfEllipse;
  

	
	
	public static final char  SEPARATORCHAR = ';';
	
	
	
	
	public AbstractCellEllipse(long id, double x, double y, double majorAxis, double minorAxis, double height, double width, double orientationInDegrees, double area, double perimeter, double distanceToBL,  Color c){
		this.id = id;			
		
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
		clippedEllipse = new Area(new Ellipse2D.Double(getX() - (getMajorAxis()/2),getY()-(getMinorAxis()/2),getMajorAxis(),getMinorAxis()));
		ellipseAsArea = getClone(clippedEllipse);
		ellipseBoundingBox = convertRectangleToPath(ellipseAsArea.getBounds2D());
		this.rotateCellEllipseInDegrees(orientationInDegrees);
		this.intersectionPointsOfEllipse = new HashMap<String, IntersectionPoints>();
		CellEllipseIntersectionCalculationRegistry.getInstance().registerCellEllipse(this);
	}
	
	public void resetClippedEllipse(){ 
		if(ellipseAsArea== null) ellipseAsArea = new Area(new Ellipse2D.Double(getX() - (getMajorAxis()/2),getY()-(getMinorAxis()/2),getMajorAxis(),getMinorAxis()));
		clippedEllipse = getClone(this.ellipseAsArea);
		this.intersectionPointsOfEllipse.clear();
	}
	
	public void addIntersectionPoints(IntersectionPoints points, long idOtherEllipse){
		this.intersectionPointsOfEllipse.put(getIDString(idOtherEllipse), points);
	}
		
	private String getIDString(long idOtherEllipse){
		return ""+this.id + SEPARATORCHAR + idOtherEllipse;
	}
	
   public double getX() { 
   	
   	if(lastDisplayProps != null){
   		double x = this.x*lastDisplayProps.displayScaleX;
   		x+=lastDisplayProps.offsetX;
   		return x;
   	}      	
   	else return x;    	
   }
	
  public Map<String, IntersectionPoints> getAllIntersectionPointsOfEllipse(){
	  return this.intersectionPointsOfEllipse;
  }
	
   public double getY() { 
   	if(lastDisplayProps != null){
   		double y = this.y;
   		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
   		y = heightInMikron - y;
   		y*= lastDisplayProps.displayScaleY;
   		y+= lastDisplayProps.offsetY;
   		return y;
   	}      	
   	else return y;      	
   }
	
   
  
	
   public double getMajorAxis() { 
   	
   	 if(lastDisplayProps != null){
	      	return (majorAxis * lastDisplayProps.displayScaleX);
	      }
	      else return majorAxis;  	
   }
	
   public void setMajorAxis(int majorAxis) {     
   	this.majorAxis = majorAxis;
   	resetEllipseAsArea();
   	testMajorMinorAxisSwap();
   }
	
   public void setXY(double x, double y){
   	this.x = x;
   	this.y = y;
   	resetEllipseAsArea();   	
   }
   
   public void setMajorAxisAndMinorAxis(double majorAxis, double minorAxis){
   	
   	this.majorAxis = majorAxis;
   	this.minorAxis = minorAxis;
   	resetEllipseAsArea();
   	
   	testMajorMinorAxisSwap();
   }
   
   public double getMinorAxis() { 
      if(lastDisplayProps != null){
      	return (minorAxis * lastDisplayProps.displayScaleY);
      }
      else return minorAxis; 
   	
   
   }
	
   public void setMinorAxis(int minorAxis) {
   	
   	
   	this.minorAxis = minorAxis;
   	resetEllipseAsArea();
   	testMajorMinorAxisSwap();
   }
   
   private void resetEllipseAsArea(){
   	
      	      	
      ellipseAsArea = new Area(new Ellipse2D.Double(getX() - (getMajorAxis()/2),getY()-(getMinorAxis()/2),getMajorAxis(),getMinorAxis()));
      
      ellipseBoundingBox = convertRectangleToPath(ellipseAsArea.getBounds2D());	
     this.rotateCellEllipseInRadians(this.orientationInRadians);
     resetClippedEllipse(); 
      
   }
	
   public Area getEllipse() {
   	if(ellipseAsArea== null) ellipseAsArea = new Area(new Ellipse2D.Double(getX() - (getMajorAxis()/2),getY()-(getMinorAxis()/2),getMajorAxis(),getMinorAxis()));
		
   	return ellipseAsArea;
   }
   
   public Area getEllipseClone() { return getClone(ellipseAsArea);}

   public void clipAreaFromEllipse(Area area){
   	this.clippedEllipse.subtract(area); 
   }
   
   public Area getClippedEllipse(){ 
   	if(clippedEllipse== null){ 
   		clippedEllipse = new Area(new Ellipse2D.Double(getX() - (getMajorAxis()/2),getY()-(getMinorAxis()/2),getMajorAxis(),getMinorAxis()));
   	}
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
		ellipseBoundingBox = (Path2D.Double)trans.createTransformedShape(ellipseBoundingBox);
		if(this.clippedEllipse != null) this.clippedEllipse = new Area(trans.createTransformedShape(clippedEllipse));
	}
	
   public double getOrientationInRadians() { return orientationInRadians; }
   
   public double getOrientationInDegrees(){ return Math.toDegrees(orientationInRadians); }

   public double getHeightExp() {return heightExp;}
	
   public void setHeightExp(int heightExp) { this.heightExp = heightExp; }
	
   public double getWidthExp() {	return widthExp; }
	
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
   
   
   
   public Path2D.Double getEllipseBoundingBox(){ return this.ellipseBoundingBox;}
   
   public SimulationDisplayProperties getLastSimulationDisplayProps(){ return this.lastDisplayProps; }
	
   public void setLastSimulationDisplayProps(SimulationDisplayProperties lastDisplayProps, boolean resetRequired){
   	if(lastDisplayProps != null){
   		this.lastDisplayProps = lastDisplayProps;
   		
   		testMajorMinorAxisSwap();
   		if(resetRequired){
   			resetEllipseAsArea();   			
   		}
   	}
   }
   
   
   
   private void testMajorMinorAxisSwap(){
   	if(getMinorAxis() > getMajorAxis()) {
   		double tmp = minorAxis;
   		minorAxis = majorAxis;
   		majorAxis = tmp;
   	}
   }
   
   private Path2D.Double convertRectangleToPath(Rectangle2D rect){
   	final double delta = 10;
   	Path2D.Double path = new Path2D.Double();   	
   	path.moveTo(rect.getMinX()-delta, rect.getMinY()-delta);
   	path.lineTo(rect.getMaxX()+delta, rect.getMinY()-delta);
   	path.lineTo(rect.getMaxX()+delta, rect.getMaxY()+delta);
   	path.lineTo(rect.getMinX()-delta, rect.getMaxY()+delta);
   	path.closePath();
		return path;
	}
  

}
