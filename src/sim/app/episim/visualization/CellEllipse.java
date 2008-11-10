package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;


public class CellEllipse {
	
		private Ellipse2D.Double ellipse;
		
		private Area clippedEllipse;
		
		
		private int id;
		private int x;
		private int y;
		private int r1;
		private int r2;
		
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
			System.out.println("Id:"+id);
		}

		public void resetClippedEllipse(){ clippedEllipse = new Area(ellipse);}
		
      public int getX() { return x; }

		
      public void setX(int x) {
      	ellipse.x = (x-r1);
      	this.x = x;
      }

		
      public int getY() { return y; }

		
      public void setY(int y) {
      	ellipse.y = (y-r2);
      	this.y = y;
      }

		
      public int getR1() { return r1; }

		
      public void setR1(int r1) {
      	ellipse.width = 2*r1;
      	this.r1 = r1;
      }

		
      public int getR2() { return r2; }

		
      public void setR2(int r2) {
      	ellipse.height = 2*r2;
      	this.r2 = r2;
      }

		
      public Ellipse2D.Double getEllipse() { return ellipse;}

      public void clipAreaFromEllipse(Area area){
      	this.clippedEllipse.subtract(area);
      }
      
      public Shape getClippedEllipse(){
      	return this.clippedEllipse;
      }
      
      
		
		public int getId() {		
			return id;
		}
		
		public int getBiggerAxis(){
			if(ellipse.width > ellipse.height) return (int)ellipse.width;
			else return (int) ellipse.height;
		}
}


