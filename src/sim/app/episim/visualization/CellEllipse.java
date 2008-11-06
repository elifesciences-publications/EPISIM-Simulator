package sim.app.episim.visualization;

import java.awt.geom.Ellipse2D;


public class CellEllipse {
	
		private Ellipse2D.Double ellipse;
		
		private int x;
		private int y;
		private int r1;
		private int r2;
		
		public CellEllipse(int x, int y, int r1, int r2){
			
			ellipse = new Ellipse2D.Double(x - r1,y-r2,r1*2,r2*2);
			this.x = x;
			this.y = y;
			this.r1 = r1;
			this.r2 = r2;
		
		}

		
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
}


