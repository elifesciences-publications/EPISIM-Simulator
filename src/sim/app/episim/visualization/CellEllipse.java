package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;


public class CellEllipse {
	
		
		
		private Shape clippedEllipse;
		private Shape ellipseAsArea;
		
		private int id;
		private int x;
		private int y;
		private int majorAxis;
		private int minorAxis;
		private int heightExp;
		private int widthExp;
		private double area;
		private double solidity;
		private double distanceToBL;
		private double orientationInRadians;
			
		private Color color;
		
		
		
		private Nucleus nucleus = null;
		
		public CellEllipse(int id, int x, int y, int majorAxis, int minorAxis, Color c){
			this(id, x, y, majorAxis, minorAxis, 0, 0, 0, 0, 0, 0, c);
		}
		
		public CellEllipse(int id, int x, int y, int majorAxis, int minorAxis, int height, int width, int orientationInDegrees, double area, double solidity, double distanceToBL, Color c){
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
			this.area = area;
			this.solidity = solidity;
			this.distanceToBL = distanceToBL;
			this.rotateCellEllipseInDegrees(orientationInDegrees);
		}
	
		
		
		public void resetClippedEllipse(){ clippedEllipse = getClone(this.ellipseAsArea);}
		
      public int getX() { return x; }
		
      public void setX(int x) {
      	
      	AffineTransform trans = new AffineTransform();
      	trans.translate((x-this.x), 0);
      	ellipseAsArea = new Area(trans.createTransformedShape(ellipseAsArea));
      	this.x = x;
      }
		
      public int getY() { return y; }
		
      public void setY(int y) {
      	
      	AffineTransform trans = new AffineTransform();
      	trans.translate(0, (y-this.y));
      	ellipseAsArea = new Area(trans.createTransformedShape(ellipseAsArea));
      	this.y = y;
      }
		
      public int getMajorAxis() { return majorAxis; }
		
      public void setMajorAxis(int majorAxis) {
      	
      	ellipseAsArea = new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis);
      	this.majorAxis = majorAxis;
      	this.rotateCellEllipseInRadians(this.orientationInRadians);
      }
		
      public int getMinorAxis() { return minorAxis; }
		
      public void setMinorAxis(int minorAxis) {
      	
      	ellipseAsArea = new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis);
      	this.minorAxis = minorAxis;
      	this.rotateCellEllipseInRadians(this.orientationInRadians);
      }
		
      public Shape getEllipse() { return ellipseAsArea;}

      public void clipAreaFromEllipse(Area area){
      	((Area)this.clippedEllipse).subtract(area); 
      }
      
      public Shape getClippedEllipse(){ return this.clippedEllipse; }
     		
		public int getId() { return id; }
		
		public void rotateCellEllipseInDegrees(double degrees){
			
		
			rotateCellEllipseInRadians(Math.toRadians(degrees));
		}
		
		public void rotateCellEllipseInRadians(double radians){
			this.orientationInRadians = (orientationInRadians+radians)%(2*Math.PI);
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
      
      public Nucleus getNucleus() { return nucleus; }

      public void setNucleus(Nucleus nucleus) { this.nucleus = nucleus; }
      
      public double getArea() { return area; }
		
      public void setArea(double area) { this.area = area; }
		
      public double getSolidity() { return solidity; }

		public void setSolidity(double solidity) { this.solidity = solidity; }
		
      public double getDistanceToBL() { return distanceToBL; }
		
      public void setDistanceToBL(double distanceToBL) { this.distanceToBL = distanceToBL; }
      
      public Shape getClone(Shape shape){
      	if(shape instanceof Path2D) return (Shape)((Path2D)shape).clone();
      	else if(shape instanceof Area) return (Shape)((Area)shape).clone();
      	else throw new ClassCastException("Cannot Clone: "+ shape.getClass().getName());
      }
      
      //-------------------------------------------------------------------------------------------------------
      // Nested Class Nucleus
      //-------------------------------------------------------------------------------------------------------
		public class Nucleus extends CellEllipse{
						
			public Nucleus(int id, int x, int y, int majorAxis, int minorAxis, int height, int width, int orientationInDegrees, double area, double solidity, double distanceToBL, Color c){
				super(id,  x,  y,  majorAxis,  minorAxis,  height,  width,  orientationInDegrees,  area,  solidity,  distanceToBL,  c);
				
			}
	
		}
		
}

