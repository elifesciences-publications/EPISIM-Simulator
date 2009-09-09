package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import sim.portrayal.DrawInfo2D;


public class CellEllipse {
	
		
		
		private Shape clippedEllipse;
		private Shape ellipseAsArea;
		
		private long id;
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
		
		private DrawInfo2D lastDrawInfo2D = null;
		
		
     

		private Nucleus nucleus = null;
		
		public CellEllipse(long id, int x, int y, int majorAxis, int minorAxis, Color c){
			this(id, x, y, majorAxis, minorAxis, 0, 0, 0, 0, 0, 0, c);
		}
		
		public CellEllipse(long id, int x, int y, int majorAxis, int minorAxis, int height, int width, int orientationInDegrees, double area, double solidity, double distanceToBL, Color c){
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
		
     
		
      public int getY() { return y; }
		
      
     
		
      public int getMajorAxis() { return majorAxis; }
		
      public void setMajorAxis(int majorAxis) {     
      	this.majorAxis = majorAxis;
      	resetEllipseAsArea();
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
      }
      
      public int getMinorAxis() { return minorAxis; }
		
      public void setMinorAxis(int minorAxis) {
      	
      	
      	this.minorAxis = minorAxis;
      	resetEllipseAsArea();
      }
      
      private void resetEllipseAsArea(){
      	if(lastDrawInfo2D != null){
      		double majorAxisScaled = majorAxis * lastDrawInfo2D.draw.width;
      		double minorAxisScaled = minorAxis * lastDrawInfo2D.draw.height;
      		ellipseAsArea = new Area(new Ellipse2D.Double(lastDrawInfo2D.draw.x- (majorAxisScaled/2),lastDrawInfo2D.draw.y-(minorAxisScaled/2),majorAxisScaled,minorAxisScaled));
      	}
	      else{
	      	      	
	      	ellipseAsArea = new Area(new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis));
	      }	
	      	
	      	this.rotateCellEllipseInRadians(this.orientationInRadians);
	      	resetClippedEllipse();
	      
      }
		
      public Shape getEllipse() { return ellipseAsArea;}

      public void clipAreaFromEllipse(Area area){
      	((Area)this.clippedEllipse).subtract(area); 
      }
      
      public Shape getClippedEllipse(){ return getClone(this.clippedEllipse); }
     		
		public long getId() { return id; }
		
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
      
      public DrawInfo2D getLastDrawInfo2D() { return lastDrawInfo2D; }
		
      public void setLastDrawInfo2D(DrawInfo2D lastDrawInfo2D){
      	if(lastDrawInfo2D != null){
      		this.lastDrawInfo2D = lastDrawInfo2D;
      		resetEllipseAsArea();
      	}
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

