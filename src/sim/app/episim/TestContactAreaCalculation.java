package sim.app.episim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import sim.util.gui.NumberTextField;
import ec.util.MersenneTwisterFast;

public class TestContactAreaCalculation {
	private JFrame frame;
	
	private List<Ellipsoid> ellipses;
	private List<Sphere> circles;
	private ContactAreaBar contactAreaBar=null;
	
	private Point2d firstCoordinate = null;
	private double measuredDistance = 0;
	private double optimalDistance =0;
	private double deltaZ =0;
	private Point3d intersectionPointEll1 =null;
	private Point3d intersectionPointEll2 =null;
	
	private final Point2d ORIGIN = new Point2d(500, 750);
	
	private Ellipsoid selectedEllipse = null;
	private Point2d mouseDraggingCoordinate = null;
	
	private class Ellipsoid{
		protected double x;
		protected double y;
		protected double z;
		protected double a;
		protected double b;
		protected double c;
		protected Ellipsoid(double x, double y, double z, double a, double b, double c){
			this.x = x;
			this.y = y;
			this.z = z;
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}
	private class Sphere{
		protected double x;
		protected double y;
		protected double z;
		protected double r;
		protected Sphere(double x, double y, double z, double r){
			this.x = x;
			this.y = y;
			this.z = z;
			this.r = r;
		}
	}
	
	private class ContactAreaBar{
		protected double x;
		protected double y;
		protected double length;
		protected boolean vertical;
		
		protected ContactAreaBar(double x, double y, double length, boolean vertical){
			this.x = x;
			this.y = y;
			this.length = length;
			this.vertical = vertical;
		}
	}
	
	public TestContactAreaCalculation(){
		frame = new JFrame();
		Dimension frameSize = new Dimension(1000, 750);
		frame.setSize(frameSize);
		frame.setPreferredSize(frameSize);
		final JPanel drawPanel = new JPanel(){
			public void paint(Graphics g){
				if(ellipses != null){
					for(Ellipsoid ell : ellipses){
						drawEllipse((Graphics2D)g, ell);
					}
				}
				if(circles != null){
					for(Sphere circ : circles){
						//drawCircle((Graphics2D)g, circ);
					}
				}
				if(contactAreaBar != null)drawInfos((Graphics2D)g, contactAreaBar);
			}
			
		};
		drawPanel.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1){
					Point2d mousePos = new Point2d(e.getX(), e.getY());
					Ellipsoid minDistEll= null;
					double minDist = Double.POSITIVE_INFINITY;
					for(Ellipsoid ell : ellipses){
						Ellipse2D.Double ell2D = new Ellipse2D.Double(ORIGIN.x + ell.x-ell.a, ell.y-ell.b,2*ell.a, 2*ell.b);
						if(ell2D.contains(mousePos.x, mousePos.y)){
							double dist = mousePos.distance(new Point2d(ell.x, ell.y));
							if(dist < minDist){
								minDistEll = ell;
								minDist = dist;
							}							
						}
					}
					selectedEllipse = minDistEll;
					mouseDraggingCoordinate = mousePos;
				}
			}
			public void mouseReleased(MouseEvent e) {
				selectedEllipse = null;
				mouseDraggingCoordinate = null;
			}	
			
			public void mouseClicked(MouseEvent e) {
			
				if(e.getButton()==MouseEvent.BUTTON1){
					firstCoordinate = new Point2d(e.getX(), e.getY());
				}
				if(e.getButton()==MouseEvent.BUTTON3){
					if(firstCoordinate!= null){ 
						measuredDistance= firstCoordinate.distance(new Point2d(e.getX(), e.getY()));
						frame.repaint();
					}
				}
				
			}
		});
		drawPanel.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e){
				if(mouseDraggingCoordinate != null && selectedEllipse != null){
					double dx = e.getX()-mouseDraggingCoordinate.x;
					double dy = e.getY()-mouseDraggingCoordinate.y;
					selectedEllipse.x+=dx;
					selectedEllipse.y+=dy;
					mouseDraggingCoordinate= new Point2d(e.getX(), e.getY());
					calculateContactArea();
					frame.repaint();
				}
			}
		});
		drawPanel.setBackground(Color.WHITE);
		drawPanel.setDoubleBuffered(true);
		JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		textFieldPanel.add(new JLabel("Delta-Z:  "));
		NumberTextField deltaZField = new NumberTextField(0d){
			  public void setValue(double val)
	        {
		       if(val >= 0){
		      	 super.setValue(val);		      	 
		      	 deltaZ = val;
		      	 if(ellipses != null && ellipses.size()>=2 && ellipses.get(1) != null){
		      		 ellipses.get(1).z=deltaZ;
		      	 }
		      	 frame.repaint();
		       }
	        }
		};
		deltaZField.setPreferredSize(new Dimension(250, deltaZField.getPreferredSize().height));
		textFieldPanel.add(deltaZField);
		frame.getContentPane().add(textFieldPanel, BorderLayout.NORTH);
		frame.getContentPane().add(drawPanel, BorderLayout.CENTER);
		frame.setVisible(true);
		frame.setTitle("Test Contact Area Calculation 3D");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		centerMe(frame);
	}
	private void centerMe(JFrame frame){
		if(frame != null){
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();			
			frame.setLocation(((int)((dim.getWidth()/2)-(frame.getPreferredSize().getWidth()/2))), 
			((int)((dim.getHeight()/2)-(frame.getPreferredSize().getHeight()/2))));
		}
	}
	
	private void drawEllipse(Graphics2D graphics, Ellipsoid ell){
		final double centerRadius = 8;
		Ellipse2D.Double e = new Ellipse2D.Double(ORIGIN.x + ell.x-ell.a, (ell.y-ell.b),2*ell.a, 2*ell.b);
		Ellipse2D.Double e2 = new Ellipse2D.Double(ORIGIN.x + ell.x-(centerRadius/2), (ell.y-(centerRadius/2)),centerRadius, centerRadius);
		graphics.draw(e);
		graphics.fill(e2);
	}
	private void drawCircle(Graphics2D graphics, Sphere circ){
		final double centerRadius = 8;
		
		Ellipse2D.Double e = new Ellipse2D.Double(circ.x-circ.r, circ.y-circ.r,2*circ.r, 2*circ.r);
		Ellipse2D.Double e2 = new Ellipse2D.Double(circ.x-(centerRadius/2), circ.y-(centerRadius/2),centerRadius, centerRadius);
		Color c = graphics.getColor();
		graphics.setColor(Color.BLUE);
		graphics.draw(e);
		graphics.fill(e2);
		graphics.setColor(c);
	}
	
	private void drawInfos(Graphics2D graphics, ContactAreaBar contactAreaBar){
		final double barThickness = 8;
		graphics.drawString("Ellipsoid 1: ("+ this.ellipses.get(0).x+", "+this.ellipses.get(0).y+", "+0.0d+")", 10, 20);
		graphics.drawString("Ellipsoid 2: ("+ this.ellipses.get(1).x+", "+this.ellipses.get(1).y+", "+deltaZ+")", 10, 40);
		graphics.drawString("Optimal Distance: "+optimalDistance, 10, 60);
		graphics.drawString("Contact Diameter: "+ contactAreaBar.length, 10, 90);
		graphics.drawString("Measured Diameter: "+ measuredDistance, 10, 130);
		
		
		
		Rectangle2D coordinateSystem = new Rectangle2D.Double(ORIGIN.x, 50, 1000, ORIGIN.y);
		
		graphics.draw(coordinateSystem);
		
		graphics.setColor(Color.BLUE);
		Rectangle2D.Double bar =null;
		if(this.intersectionPointEll1 != null){
			Line2D.Double line1 = new Line2D.Double(ORIGIN.x+ this.ellipses.get(0).x, this.ellipses.get(0).y, ORIGIN.x+ intersectionPointEll1.x, intersectionPointEll1.y);
			graphics.draw(line1);
		}
		if(this.intersectionPointEll2 != null){
			Line2D.Double line2 = new Line2D.Double(ORIGIN.x+this.ellipses.get(1).x, this.ellipses.get(1).y,ORIGIN.x+ intersectionPointEll2.x, intersectionPointEll2.y);
			graphics.draw(line2);
		}
		
		bar= new Rectangle2D.Double(contactAreaBar.x-(contactAreaBar.length/2), contactAreaBar.y-(barThickness/2)+75, contactAreaBar.length, barThickness);
		graphics.fill(bar);
	}
	
	
	
	private double calculateDistanceToCellCenter(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
		 
		 Vector3d rayDirection = new Vector3d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y), (otherCellCenter.z-cellCenter.z));
		 rayDirection.normalize();
		 //calculates the intersection of an ray with an ellipsoid
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;
		 double cAxis_2=cAxis * cAxis;
		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2))
	            + ((rayDirection.z * rayDirection.z) / (cAxis_2));
	  
	    if (a < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return -1;
	    }
	   double sqrtA = Math.sqrt(a);	 
	   double hit = 1 / sqrtA;
	   double hitsecond = -1*(1 / sqrtA);
	    
	   double linefactor = hit;// < hitsecond ? hit : hitsecond;
	   Point3d intersectionPointEllipsoid = new Point3d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y),(cellCenter.z+ linefactor*rayDirection.z));
	   
	   return cellCenter.distance(intersectionPointEllipsoid);
	}
	private Point3d calculateIntersectionPoint(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
		 
		 Vector3d rayDirection = new Vector3d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y), (otherCellCenter.z-cellCenter.z));
		 rayDirection.normalize();
		 //calculates the intersection of an ray with an ellipsoid
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;
		 double cAxis_2=cAxis * cAxis;
		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2))
	            + ((rayDirection.z * rayDirection.z) / (cAxis_2));
	  
	    if (a < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return null;
	    }
	   double sqrtA = Math.sqrt(a);	 
	   double hit = 1 / sqrtA;
	   double hitsecond = -1*(1 / sqrtA);
	    
	   double linefactor = hit;// < hitsecond ? hit : hitsecond;
	   return new Point3d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y),(cellCenter.z+ linefactor*rayDirection.z));
	}
	   
	 
	  
	private void calculateContactArea(){
		if(this.circles == null) this.circles = new ArrayList<Sphere>();
		if(this.ellipses != null && this.ellipses.size() >=2){
			Ellipsoid ell1 = ellipses.get(0);
			Ellipsoid ell2 = ellipses.get(1);
			Point3d centerEllipse1 = new Point3d(ell1.x, ell1.y, ell1.z);
			Point3d centerEllipse2 = new Point3d(ell2.x, ell2.y, ell2.z);
			double contactRadius=0;
			final double AXIS_RATIO_THRES = 5;
			if((ell1.a/ell1.b) > AXIS_RATIO_THRES && (ell2.a/ell2.b) > AXIS_RATIO_THRES){
				Rectangle2D.Double rect1 = new Rectangle2D.Double(ell1.x-ell1.a, ell1.y-ell1.b, 2*ell1.a,2*ell1.b);
				Rectangle2D.Double rect2 = new Rectangle2D.Double(ell2.x-ell2.a, ell2.y-ell2.b, 2*ell2.a,2*ell2.b);
				Rectangle2D.Double intersectionRectXY = new Rectangle2D.Double();
				Rectangle2D.Double.intersect(rect1, rect2, intersectionRectXY);
				double contactRadiusXY =  intersectionRectXY.height < ell1.b ? intersectionRectXY.width : intersectionRectXY.height;
 						
				System.out.println("XY-Rect width: "+ intersectionRectXY.width + "  height: "+ intersectionRectXY.height);
				System.out.println("Contact-Diameter-XY: "+ contactRadiusXY);
				contactRadiusXY/=2;
				
				rect1 = new Rectangle2D.Double(ell1.z-ell1.c, ell1.y-ell1.b, 2*ell1.c,2*ell1.b);
				rect2 = new Rectangle2D.Double(ell2.z-ell2.c, ell2.y-ell2.b, 2*ell2.c,2*ell2.b);
				Rectangle2D.Double intersectionRectZY = new Rectangle2D.Double();
				Rectangle2D.Double.intersect(rect1, rect2, intersectionRectZY);
				double contactRadiusZY = intersectionRectZY.width;
				System.out.println("Contact-Diameter-ZY: "+ contactRadiusZY );
				contactRadiusZY/=2;
				
				double contactArea = Math.PI*contactRadiusXY*contactRadiusZY;
				System.out.println("Contact Area: "+ contactArea);
				contactRadius = Math.sqrt(contactArea /Math.PI);
				this.contactAreaBar = new ContactAreaBar(10+contactRadius, 30, 2*contactRadius, false);
				System.out.println();
			}
			else if((ell1.a/ell1.b) > AXIS_RATIO_THRES|| (ell2.a/ell2.b) > AXIS_RATIO_THRES){
				this.circles.clear();
				Ellipsoid flatEllipse=null, otherEllipse=null;
				if((ell1.a/ell1.b) > AXIS_RATIO_THRES){
					flatEllipse = ell1;
					otherEllipse = ell2;
				}
				else if((ell2.a/ell2.b) > AXIS_RATIO_THRES){ 
					flatEllipse = ell2;
					otherEllipse = ell1;
				}
				double dx = otherEllipse.x - flatEllipse.x;
				double dy = otherEllipse.y - flatEllipse.y;
				double dz = otherEllipse.z - flatEllipse.z;
				if(Math.abs(dy) <= flatEllipse.b){
					contactRadius = flatEllipse.b;
				}
				else{
					double d=(Math.abs(dy)-flatEllipse.b)*(otherEllipse.a/otherEllipse.b);
					contactRadius = Math.sqrt(Math.pow(otherEllipse.a, 2)-Math.pow(d, 2));
					
					this.circles.add(new Sphere((otherEllipse.x), (flatEllipse.y)+(Math.signum(dy)*(d+flatEllipse.b)), 0,otherEllipse.a));
				}
				this.contactAreaBar = new ContactAreaBar(10+contactRadius, 30, 2*contactRadius, false);
			}
			else{
				double r1_old = calculateDistanceToCellCenter(centerEllipse1, centerEllipse2, ell1.a, ell1.b, ell1.c);
				double r2_old = calculateDistanceToCellCenter(centerEllipse2, centerEllipse1, ell2.a, ell2.b, ell2.c);
				optimalDistance = r1_old+r2_old;
				
				intersectionPointEll1 = calculateIntersectionPoint(centerEllipse1, centerEllipse2, ell1.a, ell1.b, ell1.c);
				intersectionPointEll2 = calculateIntersectionPoint(centerEllipse2, centerEllipse1, ell2.a, ell2.b, ell2.c);
				
				
				double h_old = centerEllipse1.distance(centerEllipse2);
				contactRadius = 0;
				double r1=0, r2=0;
				double h_diff = 0;
				if(h_old < (r1_old+r2_old)){
				 
					r1=(ell1.b/r1_old)*ell1.a;
					r2=(ell2.b/r2_old)*ell2.a;
					
					
					double h_scale =((r1/r1_old)*(r1_old/(r1_old+r2_old))+(r2/r2_old)*(r2_old/(r1_old+r2_old)));
					double h=h_old*h_scale;
					h_diff=h-h_old;
					double contactArea = (Math.PI/(4*Math.pow(h, 2)))*(2*Math.pow(h, 2)*(Math.pow(r1, 2)+Math.pow(r2, 2))
							                                           + 2*Math.pow(r1, 2)*Math.pow(r2, 2)
							                                           - Math.pow(r1, 4)-Math.pow(r2, 4)-Math.pow(h, 4));
					 contactRadius = Math.sqrt(contactArea/Math.PI);
				}
				this.contactAreaBar = new ContactAreaBar(10+contactRadius, 30, 2*contactRadius, false);
				
				
				this.circles.clear();
				if(contactRadius > 0){
					Vector2d dirVect1 = new Vector2d(ell1.x-ell2.x, ell1.y-ell2.y);
					Vector2d dirVect2 = new Vector2d(ell2.x-ell1.x, ell2.y-ell1.y);
					dirVect1.normalize();
					dirVect2.normalize();
					dirVect1.scale(h_diff*(r1_old/(r1_old+r2_old)));
					dirVect2.scale(h_diff*(r2_old/(r1_old+r2_old)));
					this.circles.add(new Sphere((ell1.x+dirVect1.x), (ell1.y+dirVect1.y), 0, r1));
					this.circles.add(new Sphere((ell2.x+dirVect2.x), (ell2.y+dirVect2.y), 0, r2));
				}
			}			
		}
	}
	
	public void start(){
		double a1 = 200;
		double b1 = 20;
		double a2 = 200;
		double b2 = 20;
		Point3d centerEllipse1 = new Point3d(0, 200,0);
		Point3d centerEllipse2 = new Point3d(100, 200, deltaZ);
		
		this.ellipses = new ArrayList<Ellipsoid>();
		this.ellipses.add(new Ellipsoid(centerEllipse1.x,centerEllipse1.y,centerEllipse1.z,a1,b1,a1));
		this.ellipses.add(new Ellipsoid(centerEllipse2.x,centerEllipse2.y,centerEllipse2.z,a2,b2,a2));
		calculateContactArea();
		frame.repaint();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TestContactAreaCalculation test = new TestContactAreaCalculation();
		test.start();		
	}

}
