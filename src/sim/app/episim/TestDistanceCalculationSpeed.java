package sim.app.episim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class TestDistanceCalculationSpeed {

	
	private Point2d calculateDistanceToCellCenter(Point2d cellCenter, Vector2d directionVectorToOtherCell, double aAxis, double bAxis){
   	Vector2d xAxis = new Vector2d(1d,0d); 
   	double angle = directionVectorToOtherCell.angle(xAxis);   	
   	Point2d pointOnMembrane = new Point2d((cellCenter.x+aAxis*Math.cos(-1*angle)), (cellCenter.y+bAxis*Math.sin(-1*angle)));
   	
   	return pointOnMembrane;//cellCenter.distance(pointOnMembrane);
   }
	
	private Point2d calculateDistanceToCellCenter(Point2d cellCenter, Point2d otherCellCenter, double aAxis, double bAxis){
		 
		 Vector2d rayDirection = new Vector2d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y));
		 rayDirection.normalize();
		 //calculates the intersection of an ray with an ellipsoid
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2));
	  
	    if (a < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return null;//-1;
	    }
	   double sqrtA = Math.sqrt(a);	 
	   double hit = 1 / sqrtA;
	   double hitsecond = -1*(1 / sqrtA);
	    
	   double linefactor = hit;// < hitsecond ? hit : hitsecond;
	   Point2d intersectionPointEllipse = new Point2d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y));
	   
	   return intersectionPointEllipse;//cellCenter.distance(intersectionPointEllipse);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double dx = 4;
		double dy = 4;
		double x = 1;
		double y = 1;
		double x2 = 5;
		double y2 = 5;
		double height = 2;
		double width = 4;
		TestDistanceCalculationSpeed test = new TestDistanceCalculationSpeed();
		
		
		System.out.println("Variante A: " + test.calculateDistanceToCellCenter(new Point2d(x, y), new Vector2d(dx, dy), width, height));

		System.out.println("Variante B: " + test.calculateDistanceToCellCenter(new Point2d(x, y), new Point2d(x2, y2), width, height));
			
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		JPanel drawPanel = new JPanel(){
			public void paint(Graphics g){
				double height = 50;
				double width = 100;
				double x = 200;
				double y = 200;
				double x2 = 250;
				double y2 = 150;
				for(double i = 0; i <= 360; i+=0.5){
					Point2d pointOnMembrane = new Point2d((x+width*Math.cos(Math.toRadians(i))), (y+height*Math.sin(Math.toRadians(i))));
					g.drawLine((int)pointOnMembrane.x, (int)pointOnMembrane.y, (int)pointOnMembrane.x, (int)pointOnMembrane.y);
				}				
				TestDistanceCalculationSpeed test = new TestDistanceCalculationSpeed();
				Point2d pointA = test.calculateDistanceToCellCenter(new Point2d(x, y), new Vector2d(50, -50), width, height);
				Point2d pointB =test.calculateDistanceToCellCenter(new Point2d(x, y), new Point2d(x2, y2), width, height);
				g.fillOval((int)x-4, (int)y-4, 8, 8);
				g.setColor(Color.RED);
				g.fillOval((int)pointA.x-4, (int)pointA.y-4, 8, 8);
				g.setColor(Color.GREEN);
				g.fillOval((int)pointB.x-4, (int)pointB.y-4, 8, 8);
				g.setColor(Color.BLUE);
				g.fillOval((int)x2-4, (int)y2-4, 8, 8);
			}
			
		};
		drawPanel.setBackground(Color.WHITE);
		frame.getContentPane().add(drawPanel, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		// TODO Auto-generated method stub

	}

}
