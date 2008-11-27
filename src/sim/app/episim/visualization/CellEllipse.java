package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;


public class CellEllipse {
	
		private Shape ellipse;
		
		private Area clippedEllipse;
		
		
		private int id;
		private int x;
		private int y;
		private int majorAxis;
		private int minorAxis;
		private int heightExp;
		private int widthExp;
		private double orientationInRadians;
		
		
		private Color color;
		
		
		
		public CellEllipse(int id, int x, int y, int majorAxis, int minorAxis, int height, int width, Color c){
			this.id = id;
			ellipse = new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis);
			clippedEllipse = new Area(ellipse);			
			this.x = x;
			this.y = y;
			this.majorAxis = majorAxis;
			this.minorAxis = minorAxis;
			this.color = c;
			this.heightExp = height;
			this.widthExp = width;
		}

		
		
		
		
		
		public void resetClippedEllipse(){ clippedEllipse = new Area(ellipse);}
		
      public int getX() { return x; }

		
      public void setX(int x) {
      	AffineTransform trans = new AffineTransform();
      	trans.translate((x-this.x), 0);
      	ellipse = trans.createTransformedShape(ellipse);
      	this.x = x;
      }

		
      public int getY() { return y; }

		
      public void setY(int y) {
      	AffineTransform trans = new AffineTransform();
      	trans.translate(0, (y-this.y));
      	ellipse = trans.createTransformedShape(ellipse);
      	this.y = y;
      }

		
      public int getMajorAxis() { return majorAxis; }

		
      public void setMajorAxis(int majorAxis) {
      	ellipse = new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis);
      	this.majorAxis = majorAxis;
      	this.rotateCellEllipseInRadians(this.orientationInRadians);
      }

		
      public int getMinorAxis() { return minorAxis; }

		
      public void setMinorAxis(int minorAxis) {
      	ellipse = new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis);
      	this.minorAxis = minorAxis;
      	this.rotateCellEllipseInRadians(this.orientationInRadians);
      }
		
      public Shape getEllipse() { return ellipse;}

      public void clipAreaFromEllipse(Area area){
      	this.clippedEllipse.subtract(area);
      }
      
      public Shape getClippedEllipse(){
      	return this.clippedEllipse;
      }
     		
		public int getId() {		
			return id;
		}
		
		public void rotateCellEllipseInDegrees(double degrees){
			this.orientationInRadians = degrees*(Math.PI/180);
			rotateCellEllipseInRadians(orientationInRadians);
		}
		
		public void rotateCellEllipseInRadians(double radians){
			this.orientationInRadians = radians;
			AffineTransform trans = new AffineTransform();
			trans.rotate(radians, x, y);
			this.ellipse = trans.createTransformedShape(this.ellipse);
			if(this.clippedEllipse != null) this.clippedEllipse = new Area(trans.createTransformedShape(clippedEllipse));
		}
				
      public double getOrientationInRadians() {
      
      	return orientationInRadians;
      }
      
      public double getOrientationInDegrees(){
      	return this.orientationInRadians*(180/Math.PI);
      }

		public class Nucleus{
			
			private int idN;
			private int xN;
			private int yN;
			private int majorAxisN;
			private int minorAxisN;
			private int heightExpN;
			private int widthExpN;
			private double orientationInRadiansN;
			private Shape ellipseN;
			
			private Area clippedEllipseN;
			
			private Color colorN;
			
			public Nucleus(int id, int x, int y, int majorAxis, int minorAxis, int height, int width, Color c){
				this.idN = id;
				ellipseN = new Ellipse2D.Double(x - (majorAxis/2),y-(minorAxis/2),majorAxis,minorAxis);
				clippedEllipseN = new Area(ellipseN);			
				this.xN = x;
				this.yN = y;
				this.majorAxisN = majorAxis;
				this.minorAxisN = minorAxis;
				this.heightExpN = height;
				this.widthExpN = width;
				this.colorN = c;
			}

			public void resetClippedEllipse(){ clippedEllipseN = new Area(ellipseN);}
			
	      public int getX() { return xN; }

			
	      public void setX(int x) {
	      	AffineTransform trans = new AffineTransform();
	      	trans.translate((x-this.xN), 0);
	      	ellipseN = trans.createTransformedShape(ellipseN);
	      	this.xN = x;
	      }

			
	      public int getY() { return yN; }

			
	      public void setY(int y) {
	      	AffineTransform trans = new AffineTransform();
	      	trans.translate(0, (y-this.yN));
	      	ellipseN = trans.createTransformedShape(ellipseN);
	      	this.yN = y;
	      }

			
	      public int getMajorAxis() { return majorAxisN; }

			
	      public void setMajorAxis(int majorAxis) {
	      	ellipseN = new Ellipse2D.Double(xN - (majorAxis/2),yN-(minorAxisN/2),majorAxis,minorAxisN);
	      	this.majorAxisN = majorAxis;
	      	this.rotateCellEllipseInRadians(this.orientationInRadiansN);
	      }

			
	      public int getMinorAxis() { return minorAxisN; }

			
	      public void setMinorAxis(int minorAxis) {
	      	ellipseN = new Ellipse2D.Double(xN - (majorAxisN/2),yN-(minorAxis/2),majorAxisN,minorAxis);
	      	this.minorAxisN = minorAxis;
	      	this.rotateCellEllipseInRadians(this.orientationInRadiansN);
	      }
			
	      public Shape getEllipse() { return ellipseN;}

	      public void clipAreaFromEllipse(Area area){
	      	this.clippedEllipseN.subtract(area);
	      }
	      
	      public Shape getClippedEllipse(){
	      	return this.clippedEllipseN;
	      }
	     		
			public int getId() {		
				return idN;
			}
			
			public void rotateCellEllipseInDegrees(double degrees){
				this.orientationInRadiansN = degrees*(Math.PI/180);
				rotateCellEllipseInRadians(orientationInRadiansN);
			}
			
			public void rotateCellEllipseInRadians(double radians){
				this.orientationInRadiansN = radians;
				AffineTransform trans = new AffineTransform();
				trans.rotate(radians, xN, yN);
				this.ellipseN = trans.createTransformedShape(this.ellipseN);
				if(this.clippedEllipseN != null) this.clippedEllipseN = new Area(trans.createTransformedShape(clippedEllipseN));
			}
			
			

			
	      public double getOrientationInRadians() {
	      
	      	return orientationInRadiansN;
	      }
	      
	      public double getOrientationInDegrees(){
	      	return this.orientationInRadiansN*(180/Math.PI);
	      }

			
         public int getHeightExp() {
         
         	return heightExpN;
         }

			
         public void setHeightExp(int heightExp) {
         
         	this.heightExpN = heightExp;
         }

			
         public int getWidthExp() {
         
         	return widthExp;
         }

			
         public void setWidthExp(int widthExp) {
         
         	this.widthExpN = widthExp;
         }
			
		}

		
      public int getHeightExp() {
      
      	return heightExp;
      }

		
      public void setHeightExp(int heightExp) {
      
      	this.heightExp = heightExp;
      }

		
      public int getWidthExp() {
      
      	return widthExp;
      }

		
      public void setWidthExp(int widthExp) {
      
      	this.widthExp = widthExp;
      }






		
      public Color getColor() {
      
      	return color;
      }






		
      public void setColor(Color color) {
      
      	this.color = color;
      }
     
}


