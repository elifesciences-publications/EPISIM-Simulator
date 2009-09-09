package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class TestCalculator {
	
	public TestCalculator(){
		JFrame frame = new JFrame("Test Ellipse");
		
		JPanel canvas = new JPanel(){
			public void paint(Graphics g){
			  super.paint(g);
				Graphics2D g2D = (Graphics2D) g;
				
				
				drawEllipse(g2D, 200, 200, 100, 50, 0);
				drawEllipse(g2D, 275, 200, 100, 50, 0);
				
				
			int[][] foci= calculateFoci(275, 200, 100, 50);
			bruteForceIntersectionCalculation(200, 200, 100, 100, 50, foci[0], foci[1]);
				
				//drawEllipseCustom(g2D, 300, 300, 50, 25, 45);
				//drawEllipseCustom(g2D, 325, 300, 50, 25, 67);
			}
		};
		
		canvas.setBackground(Color.WHITE);
		
		frame.getContentPane().add(canvas);
		
		
		System.out.println("Test-Result: "+ ((-2*Math.sin(1)*Math.cos(1))));
		System.out.println("Test-Result: "+ (-1*Math.sin(2)));
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private int[][] calculateFoci(int x, int y, int majorAxis, int minorAxis){
				
		int[][] result = new int[2][2];
		int distance = (int)Math.sqrt(Math.pow(majorAxis/2, 2) - Math.pow(minorAxis/2, 2));
			result[0] = new int[]{x - distance, y};
			result[1] = new int[]{x + distance, y};
		return result;
	}
	
	
	private void bruteForceIntersectionCalculation(double x1, double y1, double a1, double a2, double b1, int[] f21, int[] f22){
		double u1_x = Math.pow(x1, 2) + Math.pow(f21[0], 2) - 2*f21[0]*x1;
		double v1_x = 2*x1*a1-2*f21[0]*a1;
		
		double u2_x = Math.pow(x1, 2) + Math.pow(f22[0], 2) - 2*f22[0]*x1;
		double v2_x = 2*x1*a1-2*f22[0]*a1;
		
		double u1_y = Math.pow(y1, 2) + Math.pow(f21[1], 2) - 2*f21[1]*y1;
		double v1_y = 2*y1*b1-2*f21[1]*b1;
		
		double u2_y = Math.pow(y1, 2) + Math.pow(f22[1], 2) - 2*f22[1]*y1;
		double v2_y = 2*y1*b1-2*f22[1]*b1;
		
		double sin = 0; 
		double cos = 0;
		
		for(double i = 0; i <= 2*Math.PI; i+=0.0001){
			sin = Math.sin(i);
			cos = Math.cos(i);
			double result = (Math.sqrt(u1_x+ v1_x*cos+Math.pow(a1,2 )*Math.pow(cos, 2)+u1_y+v1_y*sin+Math.pow(b1, 2)*Math.pow(sin, 2)) + Math.sqrt(u2_x+ v2_x*cos+Math.pow(a1, 2)*Math.pow(cos, 2)+u2_y+v2_y*sin+Math.pow(b1, 2)*Math.pow(sin, 2)) -2*a2);	
				
			if(result >=0 && result < 0.00001) System.out.println(result);
			
		}
		
		
		
	}
	
	private int[] rotatePoint(int[] point, int[] center, double angleInDegrees){
		double angle = Math.toRadians(angleInDegrees);
	   double sin = Math.sin(angle);
	   double cos = Math.cos(angle);
	   int x = point[0] - center[0];
	   int y = point[1] - center[1];
	   double[][] rm = new double[][]{{cos, -1*sin},{sin, cos}};
	  return new int[]{(int)(x *rm[0][0] + y *rm[0][1])+center[0], (int)(x *rm[1][0] + y *rm[1][1])+center[1]};
	   
	}
	
	private void drawPoint(Graphics2D g, int x, int y, int size, Color c){
		if(x> 0 || y > 0){
			if(size % 2 != 0) size -= 1;
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect(x-(size/2), y-(size/2), size+1, size+1);
			g.setColor(oldColor);
		}
	}
	
	private void drawEllipse(Graphics2D g2D, int x, int y, int majorAxis , int minorAxis, int angleInDegrees){
	   int[] s1 = rotatePoint(new int[]{(x + majorAxis/2),y}, new int[]{x, y}, angleInDegrees);
	   int[] s2 = rotatePoint(new int[]{(x - majorAxis/2),y}, new int[]{x, y}, angleInDegrees);
	   int[] s3 = rotatePoint(new int[]{x, (y - minorAxis/2)}, new int[]{x, y}, angleInDegrees);
	   int[] s4 = rotatePoint(new int[]{x, (y + minorAxis/2)}, new int[]{x, y}, angleInDegrees);
	      
	  	
		Ellipse2D ell = new Ellipse2D.Double(x-majorAxis/2, y-minorAxis/2, majorAxis, minorAxis);
		AffineTransform trans = new AffineTransform();
		trans.rotate(Math.toRadians(angleInDegrees), x, y);
		g2D.setPaint(Color.blue);
		g2D.draw(trans.createTransformedShape(ell));
		
		g2D.drawLine(s2[0], s2[1], s1[0], s1[1]);
		g2D.drawLine(s3[0], s3[1], s4[0], s4[1]);
		int[][] result =calculateFoci(x, y, majorAxis, minorAxis);
		int[][] rotatedResult = new int[][]{rotatePoint(result[0], new int[]{x,y}, angleInDegrees), rotatePoint(result[1], new int[]{x,y}, angleInDegrees)};
		
		drawPoint(g2D, rotatedResult[0][0], rotatedResult[0][1], 5, Color.RED);
		drawPoint(g2D, rotatedResult[1][0], rotatedResult[1][1], 5, Color.RED);
		
	}
	
	
	
	private void drawEllipseCustom(Graphics2D g, int x1, int y1, int a , int b, int angleInDegrees){
		double sin, cos;
		
		double sinAngle = Math.sin(Math.toRadians(angleInDegrees));
		double cosAngle =  Math.cos(Math.toRadians(angleInDegrees));
		int x, y;
		
		for (double i = 0; i < 2*Math.PI; i+=0.01){
			sin = Math.sin(i);
			cos = Math.cos(i);
			x = (int)(x1 + a*cosAngle*cos + b*sinAngle*sin);
			y = (int)(y1 + b*cosAngle*sin - a*sinAngle*cos);
			g.drawLine(x, y, x, y);
		}
		
		
	}
	
	
	public static void main(String[] args){
		TestCalculator test = new TestCalculator();
	}
}
