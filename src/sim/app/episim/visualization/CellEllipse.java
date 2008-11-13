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
		private int r1;
		private int r2;
		private double orientationInRadians;
		
		
		public Color c;
		
		public CellEllipse(int id, int x, int y, int r1, int r2, Color c){
			this.id = id;
			ellipse = new Ellipse2D.Double(x - r1,y-r2,r1*2,r2*2);
			clippedEllipse = new Area(ellipse);			
			this.x = x;
			this.y = y;
			this.r1 = r1;
			this.r2 = r2;
			this.c = c;
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

		
      public int getR1() { return r1; }

		
      public void setR1(int r1) {
      	ellipse = new Ellipse2D.Double(x - r1,y-r2,r1*2,r2*2);
      	this.r1 = r1;
      	this.rotateCellEllipseInRadians(this.orientationInRadians);
      }

		
      public int getR2() { return r2; }

		
      public void setR2(int r2) {
      	ellipse = new Ellipse2D.Double(x - r1,y-r2,r1*2,r2*2);
      	this.r2 = r2;
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
		
		public int getBiggerAxis(){
			if(r1 > r2) return 2*r1;
			else return 2*r2;
		}

		
      public double getOrientationInRadians() {
      
      	return orientationInRadians;
      }
      
      public double getOrientationInDegrees(){
      	return this.orientationInRadians*(180/Math.PI);
      }

		
     
}


