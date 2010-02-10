package sim.app.episim.visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class TestCalculator {
	private boolean firstCall = true;
	public TestCalculator(){
		JFrame frame = new JFrame("Test Ellipse");
		
		JPanel canvas = new JPanel(){
			public void paint(Graphics g){
			  super.paint(g);
				Graphics2D g2D = (Graphics2D) g;
				
				final int rotationInDegrees = 73;
				
				final double majorAxisA2 = 1050;
				final double minorAxisB2 = 20;
				
				drawEllipse(g2D, 200, 200, 100, 50, 20);
				drawEllipse(g2D, 220, 175, majorAxisA2, minorAxisB2, rotationInDegrees);
				
			/*	
				Set<EllipsePoint> ell1 = calculateEllipse(200d,200d, 50d, 25d);
				Set<EllipsePoint> ell2 = calculateEllipse(220d,175d, majorAxisA2/2, minorAxisB2/2);
				 ell1.retainAll(ell2);
				 
				 */
				
				
				double[][] foci= calculateFoci(220d, 175d, majorAxisA2, minorAxisB2, Math.toRadians(rotationInDegrees));
				
				
				
				Double[] results = newtonIntersectionCalculation(200d, 200d, 50d, majorAxisA2/2, 25d, foci[0], foci[1], Math.toRadians(20));
				
				for(Double alpha : results){
					double[] point = calculatePointOnEllipse(200d, 200d, 50d, 25d, alpha, Math.toRadians(20));
					
					drawPoint(g2D, ((int)point[0]), ((int)point[1]), 4, Color.GREEN);
				}	
				/*for(EllipsePoint p: ell1){
					System.out.println("x: "+p.x+ "  y:"+p.y+"  alpha: " + p.alpha
							
					+ "  f_alpha: "+ f_alpha_NotOriented(200d, 200d, 50d, majorAxisA2/2, 25d, foci[0], foci[1], p.alpha));
					//drawPoint(g2D, p.x, p.y, 4, Color.GREEN);
				}*/
			
				
				//drawEllipseCustom(g2D, 200, 200, 50, 25, 20);
				//drawEllipseCustom(g2D, 325, 300, 50, 25, 67);
			}
		};
		
		
		int  alpha = 30;
		
		
		
		canvas.setBackground(Color.WHITE);
		
		frame.getContentPane().add(canvas);
		
		
		
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	
	private class EllipsePoint{
		
		public int x;
		public int y;
		public double alpha;
		
		@Override
      public int hashCode() {

	      final int prime = 31;
	      int result = 1;
	      result = prime * result + getOuterType().hashCode();
	      result = prime * result + x;
	      result = prime * result + y;
	      return result;
      }

		@Override
      public boolean equals(Object obj) {

	      if(this == obj)
		      return true;
	      if(obj == null)
		      return false;
	      if(getClass() != obj.getClass())
		      return false;
	      EllipsePoint other = (EllipsePoint) obj;
	      if(!getOuterType().equals(other.getOuterType()))
		      return false;
	      if(x != other.x)
		      return false;
	      if(y != other.y)
		      return false;
	      return true;
      }

		private TestCalculator getOuterType() {

	      return TestCalculator.this;
      }
		
	}
	
	private Set<EllipsePoint> calculateEllipse(double x1, double y1, double majorAxis, double minorAxis){
		double sin, cos;
		
		HashSet<EllipsePoint> points = new HashSet<EllipsePoint>();
		int x, y;
		EllipsePoint ellP;
		for (double i = 0; i < 2*Math.PI; i+=0.01){
			sin = Math.sin(i);
			cos = Math.cos(i);
			ellP = new EllipsePoint();
			
			ellP.x = (int)(x1 + majorAxis*cos);
			ellP.y = (int)(y1 + minorAxis*sin);
			ellP.alpha = i;
			points.add(ellP);
			
		}
		return points;
		
	}
	
	
	
	private double[][] calculateFoci(double x, double y, double majorAxis, double minorAxis, double angleInRadians){				
		double[][] result = new double[2][2];
		double distance = Math.sqrt(Math.pow(majorAxis/2, 2) - Math.pow(minorAxis/2, 2));
			result[0] = new double[]{x - distance, y};
			result[1] = new double[]{x + distance, y};
			result [0] = rotatePoint(result[0], new double[]{x, y}, angleInRadians);
			result [1] = rotatePoint(result[1], new double[]{x, y}, angleInRadians);
		return result;
	}
	
	private double[] rotatePoint(double[] point, double[] center, double angleInRadians){		
	   double sin = Math.sin(angleInRadians);
	   double cos = Math.cos(angleInRadians);
	   double x = point[0] - center[0];
	   double y = point[1] - center[1];
	   double[][] rm = new double[][]{{cos, -1*sin},{sin, cos}};
	  return new double[]{(x *rm[0][0] + y *rm[0][1])+center[0], (x *rm[1][0] + y *rm[1][1])+center[1]};	   
	}
	
	
	private double df_alpha_NotOriented(double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double alpha){
		double result1 = ((2*b1*Math.cos(alpha)*(b1*Math.sin(alpha)-f21[1]+y1)-2*a1*Math.sin(alpha)*(a1*Math.cos(alpha)-f21[0]+x1))/
		                   (2*Math.sqrt(Math.pow((a1*Math.cos(alpha)-f21[0]+x1),2) + Math.pow((b1*Math.sin(alpha)-f21[1]+y1), 2))));
		double result2 = ((2*b1*Math.cos(alpha)*(b1*Math.sin(alpha)-f22[1]+y1)-2*a1*Math.sin(alpha)*(a1*Math.cos(alpha)-f22[0]+x1))/
      (2*Math.sqrt(Math.pow((a1*Math.cos(alpha)-f22[0]+x1),2) + Math.pow((b1*Math.sin(alpha)-f22[1]+y1), 2))));
		
		return (result1+result2);
		
	}
	
	private double f_alpha_NotOriented(double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double alpha){
		double result1 = Math.sqrt(Math.pow((a1*Math.cos(alpha)-f21[0]+x1),2) + Math.pow((b1*Math.sin(alpha)-f21[1]+y1), 2));
		double result2 = Math.sqrt(Math.pow((a1*Math.cos(alpha)-f22[0]+x1),2) + Math.pow((b1*Math.sin(alpha)-f22[1]+y1), 2));
		
		return (result1+result2 -2*a2);
		
	}
	
	private Double[] newtonIntersectionCalculation(double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22){
		double u1_x = Math.pow(x1, 2) + Math.pow(f21[0], 2) - 2*f21[0]*x1;
		double v1_x = 2*x1*a1-2*f21[0]*a1;
		
		double u2_x = Math.pow(x1, 2) + Math.pow(f22[0], 2) - 2*f22[0]*x1;
		double v2_x = 2*x1*a1-2*f22[0]*a1;
		
		double u1_y = Math.pow(y1, 2) + Math.pow(f21[1], 2) - 2*f21[1]*y1;
		double v1_y = 2*y1*b1-2*f21[1]*b1;
		
		double u2_y = Math.pow(y1, 2) + Math.pow(f22[1], 2) - 2*f22[1]*y1;
		double v2_y = 2*y1*b1-2*f22[1]*b1;
		
		
		double a1_square = Math.pow(a1, 2);
		double b1_square = Math.pow(b1, 2);
		
		
		double sin = 0; 
		double cos = 0;
		double sin_2alpha = 0;
		double sin_square = 0;
		double cos_square = 0;
		
		double first_sqroot = 0;
		double second_sqroot = 0;
		
		double result1 = 0;
		double result2 = 0;
		
		double alpha =0;
		Set<Double> resultSet = new HashSet<Double>();
		int numberResults = 0;
		int counter = 0;
		double border = 2* Math.PI;
		double i = 0;
	   for(; i < border; i += Math.PI/8){
			alpha = i;
			do{
				sin = Math.sin(alpha);
				cos = Math.cos(alpha);
				
				sin_2alpha = Math.sin(2*alpha);
				
				sin_square = Math.pow(sin, 2);
				cos_square = Math.pow(cos, 2);
				
				first_sqroot = Math.sqrt(u1_x + v1_x*cos + a1_square*cos_square + u1_y + v1_y*sin + b1_square*sin_square);
				second_sqroot = Math.sqrt(u2_x + v2_x*cos+ a1_square*cos_square + u2_y + v2_y*sin + b1_square*sin_square);
				
				result1 = (first_sqroot + second_sqroot - 2*a2);	
				
				if( Math.abs(result1) >  0.0000001){
					result2 = 0.5*(1 / first_sqroot)*(-1*v1_x*sin-a1_square*sin_2alpha+v1_y*cos+b1_square*sin_2alpha)
					                 + 0.5*(1 / second_sqroot)*(-1*v2_x*sin-a1_square*sin_2alpha+v2_y*cos+b1_square*sin_2alpha);
					//System.out.println("My deriviation: " + result2 + "  Other deriviation: " + df_alpha_NotOriented(x1, y1, a1, a2, b1, f21, f22, alpha));
					//System.out.println("My f_alpha: " + result1 + "  Other f_alpha: " + f_alpha_NotOriented(x1, y1, a1, a2, b1, f21, f22, alpha));
					
					alpha = alpha - (result1 / result2);
					
				}
				else{
					double finalResult = (Math.round((alpha%(Math.PI*2))*10000000d)/10000000d);
					if(!resultSet.contains(finalResult)) System.out.println("Added Result " + finalResult +"  f_alpha: "+ result1);
					resultSet.add(finalResult);
					
				}
				counter++;
			}while(Math.abs(result1) > 0.0000001 && counter < 30 && result1>=0);
			counter = 0;
		}
		return resultSet.toArray(new Double[resultSet.size()]);
	}
	
	
	
	private Double[] newtonIntersectionCalculation(double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double phi){
			
		double x1_square = Math.pow(x1, 2);
		double y1_square = Math.pow(y1, 2);
		double a1_square = Math.pow(a1, 2);
		double b1_square = Math.pow(b1, 2);
		double quarter_a1_square = 0.25*a1_square;
		double quarter_b1_square = 0.25*b1_square;
		double cos_phi = Math.cos(phi);
		double sin_phi = Math.sin(phi);
		double cos_2phi = Math.cos(2*phi);
		double sin_2phi = Math.sin(2*phi);
	
		double a1_cos_phi = a1* cos_phi;
		double b1_cos_phi = b1* cos_phi;
		
		double a1_sin_phi = a1* sin_phi;
		double b1_sin_phi = b1* sin_phi;
		
		double quarter_b1_square_cos_2phi = quarter_b1_square*cos_2phi;
		double quarter_a1_square_cos_2phi = quarter_a1_square*cos_2phi;
		
		double u11 = x1_square - 2 * f21[0] * x1 + Math.pow(f21[0], 2) + quarter_a1_square + quarter_a1_square_cos_2phi + quarter_b1_square - quarter_b1_square_cos_2phi;
		double u12 = 2*x1*a1_cos_phi-2*f21[0]*a1_cos_phi;
		double u13 = -1*2*x1*b1_sin_phi + 2*f21[0]*b1_sin_phi;
		double u14 = -1*0.5*a1*b1*sin_2phi;
		double u15 = quarter_a1_square + quarter_a1_square_cos_2phi-quarter_b1_square+quarter_b1_square_cos_2phi;
		
		double u21 = x1_square - 2*f22[0]*x1 + Math.pow(f22[0], 2)+quarter_a1_square+quarter_a1_square_cos_2phi+quarter_b1_square-quarter_b1_square_cos_2phi;
		double u22 = 2*x1*a1_cos_phi-2*f22[0]*a1_cos_phi;
		double u23 = -1*2*x1*b1_sin_phi+2*f22[0]*b1_sin_phi;
		double u24 = u14;
		double u25 = u15;
		
		double v11 = y1_square - 2 * y1 * f21[1] + Math.pow(f21[1], 2) + quarter_b1_square + quarter_b1_square_cos_2phi + quarter_a1_square - quarter_a1_square_cos_2phi;
		double v12 = 2*y1*b1_cos_phi - 2*f21[1]*b1_cos_phi;
		double v13 = -1*2*f21[1]*a1_sin_phi+2*y1*a1_sin_phi;
		double v14 = -1*u14;
		double v15 = quarter_a1_square - quarter_a1_square_cos_2phi -quarter_b1_square-quarter_b1_square_cos_2phi;
		
		double v21 = y1_square - 2*y1*f22[1] + Math.pow(f22[1], 2) + quarter_b1_square +quarter_b1_square_cos_2phi + quarter_a1_square-quarter_a1_square_cos_2phi;
		double v22 = 2*y1*b1_cos_phi - 2*f22[1]*b1_cos_phi;
		double v23 = -1*2*f22[1]*a1_sin_phi+2*y1*a1_sin_phi;
		double v24 = v14;
		double v25 = v15;
		
		double u11_v11 = u11 + v11;
		double u12_v13 = u12 + v13;
		double u13_v12 = u13 + v12;
		double u14_v14 = u14 + v14;
		double u15_v15 = u15 + v15;
		
		double u21_v21 = u21 + v21;
		double u22_v23 = u22 + v23;
		double u23_v22 = u23 + v22;
		double u24_v24 = u24 + v24;
		double u25_v25 = u25 + v25;
		
		
		
		double sin_alpha = 0; 
		double cos_alpha = 0;
		double sin_2alpha = 0;
		double cos_2alpha = 0;
		
		
		double alpha =0;
		double f_alpha = 0;
		double f_alpha_partone = 0;
		double f_alpha_parttwo = 0;
		double df_dalpha = 0;
		double[] results = new double[4];
		Set<Double> resultSet = new HashSet<Double>();
		int numberResults = 0;
		double border = 2* Math.PI;
		double i = 0;
		
		
		int counter = 0;
		double stepsize = Math.PI/8;
		System.out.println("The stepsize: " +stepsize);
		
		while( i < border){
			alpha = i;
		 	do{
				counter++;
				sin_alpha = Math.sin(alpha);
				cos_alpha = Math.cos(alpha);
				
				sin_2alpha = Math.sin(2*alpha);
				cos_2alpha = Math.cos(2*alpha);
				
				f_alpha_partone = Math.sqrt(u11_v11 + u12_v13*cos_alpha + u13_v12*sin_alpha + u14_v14*sin_2alpha + u15_v15*cos_2alpha);
				f_alpha_parttwo = Math.sqrt(u21_v21 + u22_v23*cos_alpha + u23_v22*sin_alpha + u24_v24*sin_2alpha + u25_v25*cos_2alpha);
				
				f_alpha = f_alpha_partone + f_alpha_parttwo -2*a2;
				
	
				
				
				if(Math.abs(f_alpha) > 0.00000000001){
					df_dalpha = 0.5*(1/f_alpha_partone)*(-1*u12_v13*sin_alpha + u13_v12*cos_alpha + 2*u14_v14*cos_2alpha - 2*u15_v15*sin_2alpha)
			 		           + 0.5*(1/f_alpha_parttwo)*(-1*u22_v23*sin_alpha + u23_v22*cos_alpha + 2*u24_v24*cos_2alpha - 2*u25_v25*sin_2alpha); 
						
					
						
					alpha = alpha - (f_alpha / d_alpha(x1, y1, a1, a2, b1, f21, f22, phi, alpha));
					
				//	alpha = alpha*Math.pow(Math.E, (-1*(f_alpha / df_dalpha)));
					
				}
				else{
					if(!Double.isInfinite(alpha)){
						double finalResult = (Math.round((alpha%(Math.PI*2))*10000000d)/10000000d);
						resultSet.add(finalResult);
						System.out.println("Added Result " + finalResult + " Counter: " + counter + " alpha_start: " + i + " f_alpha: " + f_alpha);
					}
				}
				
		 }while(Math.abs(f_alpha) > 0.00000000001 && counter <=25 && alpha >= 0);
			
			i += stepsize;
			counter=0;
	   }
	
	   System.out.println("Anzahl gefundene Ergebnisse: " + resultSet.size());
		return resultSet.toArray(new Double[resultSet.size()]);
	}
	
	
	
	private double alpha(double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double phi, double alpha){
	
		double partOne = Math.pow((x1 - f21[0] + a1*Math.cos(phi)*Math.cos(alpha)-b1*Math.sin(phi)*Math.sin(alpha)), 2);
		double partTwo = Math.pow((y1 - f21[1] + b1*Math.cos(phi)*Math.sin(alpha)+a1*Math.sin(phi)*Math.cos(alpha)), 2);
		
		double partThree = Math.pow((x1 - f22[0] + a1*Math.cos(phi)*Math.cos(alpha)-b1*Math.sin(phi)*Math.sin(alpha)), 2);
		double partFour = Math.pow((y1 - f22[1] + b1*Math.cos(phi)*Math.sin(alpha)+a1*Math.sin(phi)*Math.cos(alpha)), 2);
		
		return (Math.sqrt(partOne+partTwo)+ Math.sqrt(partThree + partFour)-2*a2);
	}
	
	private double d_alpha(double x1, double y1, double a1, double a2, double b1, double[] f21, double[] f22, double phi, double alpha){
	
		double result1 = 
			Math.cos(alpha)*((b1*Math.cos(phi)*(a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f21[1]+y1))/
			(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f21[0]+ x1),2)+
			Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f21[1]+y1),2))))-
			(b1*Math.sin(phi)*(a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f21[0]+x1))/
			(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f21[0]+x1),2)+
					Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f21[1]+y1), 2)))))-
					Math.sin(alpha)*((a1*Math.cos(phi)*(a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f21[0]+x1))/
					(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f21[0]+x1),2)+
					Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f21[1]+y1),2))))+
					(a1*Math.sin(phi)*(a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f21[1]+y1))/
					(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f21[0]+x1),2)+
					Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f21[1]+y1),2)))));
		
		double result2 = 
			Math.cos(alpha)*((b1*Math.cos(phi)*(a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f22[1]+y1))/
			(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f22[0]+ x1),2)+
			Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f22[1]+y1),2))))-
			(b1*Math.sin(phi)*(a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f22[0]+x1))/
			(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f22[0]+x1),2)+
					Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f22[1]+y1), 2)))))-
					Math.sin(alpha)*((a1*Math.cos(phi)*(a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f22[0]+x1))/
					(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f22[0]+x1),2)+
					Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f22[1]+y1),2))))+
					(a1*Math.sin(phi)*(a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f22[1]+y1))/
					(Math.sqrt((Math.pow((a1*Math.cos(alpha)*Math.cos(phi)-b1*Math.sin(alpha)*Math.sin(phi)-f22[0]+x1),2)+
					Math.pow((a1*Math.cos(alpha)*Math.sin(phi)+b1*Math.sin(alpha)*Math.cos(phi)-f22[1]+y1),2)))));
		
		return result1 + result2;
	}
	
	
	
	private void drawPoint(Graphics2D g, double x, double y, double size, Color c){
		if(x> 0 || y > 0){
			if(size % 2 != 0) size -= 1;
			Color oldColor = g.getColor();
			g.setColor(c);
			g.fillRect((int)(x-(size/2)), (int)(y-(size/2)), (int)(size+1), (int)(size+1));
			g.setColor(oldColor);
		}
	}
	
	private void drawEllipse(Graphics2D g2D, double x, double y, double majorAxis , double minorAxis, double angleInDegrees){
		double[] s1 = rotatePoint(new double[]{(x + majorAxis/2),y}, new double[]{x, y}, Math.toRadians(angleInDegrees));
		double[] s2 = rotatePoint(new double[]{(x - majorAxis/2),y}, new double[]{x, y}, Math.toRadians(angleInDegrees));
		double[] s3 = rotatePoint(new double[]{x, (y - minorAxis/2)}, new double[]{x, y}, Math.toRadians(angleInDegrees));
		double[] s4 = rotatePoint(new double[]{x, (y + minorAxis/2)}, new double[]{x, y}, Math.toRadians(angleInDegrees));
	      
	  	
		Ellipse2D ell = new Ellipse2D.Double(x-majorAxis/2, y-minorAxis/2, majorAxis, minorAxis);
		AffineTransform trans = new AffineTransform();
		trans.rotate(Math.toRadians(angleInDegrees), x, y);
		g2D.setPaint(Color.blue);
		g2D.draw(trans.createTransformedShape(ell));
		
		g2D.drawLine((int)s2[0], (int)s2[1], (int)s1[0], (int)s1[1]);
		g2D.drawLine((int)s3[0], (int)s3[1], (int)s4[0], (int)s4[1]);
		double[][] result =calculateFoci(x, y, majorAxis, minorAxis, Math.toRadians(angleInDegrees));
		
		
		drawPoint(g2D, result[0][0], result[0][1], 5, Color.RED);
		drawPoint(g2D, result[1][0], result[1][1], 5, Color.RED);
		
	}
	
	private double[] calculatePointOnEllipse(double x, double y, double majorAxis, double minorAxis, double alpha, double phi){
		double cos_alpha = Math.cos(alpha);
		double sin_alpha = Math.sin(alpha);
		double cos_phi = Math.cos(phi);
		double sin_phi = Math.sin(phi);
		
		double point_x = x + majorAxis * cos_phi*cos_alpha -  minorAxis*sin_phi*sin_alpha;
		double point_y = y + minorAxis*cos_phi*sin_alpha + majorAxis*sin_phi*cos_alpha;
		
		return new double[]{point_x, point_y};
	}
	/*
	private void drawEllipseCustom(Graphics2D g, int x1, int y1, int a , int b, int angleInDegrees){
		double sin, cos;
		
		double sinAngle = Math.sin(Math.toRadians(angleInDegrees));
		double cosAngle =  Math.cos(Math.toRadians(angleInDegrees));
		int x, y;
		
		for (double i = 0; i < 2*Math.PI; i+=0.01){
			sin = Math.sin(i);
			cos = Math.cos(i);
			x = (int)(x1 + a*cosAngle*cos - b*sinAngle*sin);
			y = (int)(y1 + b*cosAngle*sin + a*sinAngle*cos);
			g.drawLine(x, y, x, y);
		}
		
		
	}
	*/
	
	
	
	public static void main(String[] args){
		TestCalculator test = new TestCalculator();
	}
}
