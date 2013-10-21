package sim.app.episim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

public class TestContactAreaCalculation {
	private JFrame frame;
	
	private List<Ellipse> ellipses;
	private List<Circle> circles;
	private ContactAreaBar contactAreaBar=null;
	
	private Point2d firstCoordinate = null;
	private double measuredDistance = 0;
	
	private Ellipse selectedEllipse = null;
	private Point2d mouseDraggingCoordinate = null;
	
	private class Ellipse{
		protected double x;
		protected double y;
		protected double a;
		protected double b;
		protected Ellipse(double x, double y, double a, double b){
			this.x = x;
			this.y = y;
			this.a = a;
			this.b = b;
		}
	}
	private class Circle{
		protected double x;
		protected double y;
		protected double r;
		protected Circle(double x, double y, double r){
			this.x = x;
			this.y = y;
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
					for(Ellipse ell : ellipses){
						drawEllipse((Graphics2D)g, ell);
					}
				}
				if(circles != null){
					for(Circle circ : circles){
						drawCircle((Graphics2D)g, circ);
					}
				}
				if(contactAreaBar != null)drawContactAreaBar((Graphics2D)g, contactAreaBar);
			}
			
		};
		drawPanel.addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1){
					Point2d mousePos = new Point2d(e.getX(), e.getY());
					Ellipse minDistEll= null;
					double minDist = Double.POSITIVE_INFINITY;
					for(Ellipse ell : ellipses){
						Ellipse2D.Double ell2D = new Ellipse2D.Double(ell.x-ell.a, ell.y-ell.b,2*ell.a, 2*ell.b);
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
		frame.getContentPane().add(drawPanel, BorderLayout.CENTER);
		frame.setVisible(true);
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
	
	private void drawEllipse(Graphics2D graphics, Ellipse ell){
		final double centerRadius = 8;
		Ellipse2D.Double e = new Ellipse2D.Double(ell.x-ell.a, ell.y-ell.b,2*ell.a, 2*ell.b);
		Ellipse2D.Double e2 = new Ellipse2D.Double(ell.x-(centerRadius/2), ell.y-(centerRadius/2),centerRadius, centerRadius);
		graphics.draw(e);
		graphics.fill(e2);
	}
	private void drawCircle(Graphics2D graphics, Circle circ){
		final double centerRadius = 8;
		
		Ellipse2D.Double e = new Ellipse2D.Double(circ.x-circ.r, circ.y-circ.r,2*circ.r, 2*circ.r);
		Ellipse2D.Double e2 = new Ellipse2D.Double(circ.x-(centerRadius/2), circ.y-(centerRadius/2),centerRadius, centerRadius);
		Color c = graphics.getColor();
		graphics.setColor(Color.BLUE);
		graphics.draw(e);
		graphics.fill(e2);
		graphics.setColor(c);
	}
	
	private void drawContactAreaBar(Graphics2D graphics, ContactAreaBar contactAreaBar){
		final double barThickness = 8;
		Rectangle2D.Double bar =null;
		if(contactAreaBar.vertical)bar= new Rectangle2D.Double(contactAreaBar.x-(barThickness/2), contactAreaBar.y-(contactAreaBar.length/2), barThickness, contactAreaBar.length);
		else bar= new Rectangle2D.Double(contactAreaBar.x-(contactAreaBar.length/2), contactAreaBar.y-(barThickness/2), contactAreaBar.length, barThickness);
		graphics.drawString("Contact Diameter: "+ contactAreaBar.length, 10, 20);
		graphics.drawString("Measured Diameter: "+ measuredDistance, 10, 50);
		graphics.setColor(Color.BLUE);
		graphics.fill(bar);
	}
	
	
	
	  private double calculateDistanceToCellCenter(Point2d cellCenter, Point2d otherCellCenter, double aAxis, double bAxis){
			 
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
		   	 return -1;
		    }
		   double sqrtA = Math.sqrt(a);	 
		   double hit = 1 / sqrtA;
		   double hitsecond = -1*(1 / sqrtA);
		    
		   double linefactor = hit;// < hitsecond ? hit : hitsecond;
		   Point2d intersectionPointEllipse = new Point2d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y));
		   
		   return cellCenter.distance(intersectionPointEllipse);
		}
	  
	private void calculateContactArea(){
		if(this.circles == null) this.circles = new ArrayList<Circle>();
		if(this.ellipses != null && this.ellipses.size() >=2){
			Ellipse ell1 = ellipses.get(0);
			Ellipse ell2 = ellipses.get(1);
			Point2d centerEllipse1 = new Point2d(ell1.x, ell1.y);
			Point2d centerEllipse2 = new Point2d(ell2.x, ell2.y);
			double contactRadius=0;
			if((ell1.a/ell1.b) > 10 && (ell2.a/ell2.b) > 10){
				Rectangle2D.Double rect1 = new Rectangle2D.Double(ell1.x-ell1.a, ell1.y-ell1.b, 2*ell1.a,2*ell1.b);
				Rectangle2D.Double rect2 = new Rectangle2D.Double(ell2.x-ell2.a, ell2.y-ell2.b, 2*ell2.a,2*ell2.b);
				Rectangle2D.Double intersectionRect = new Rectangle2D.Double();
				Rectangle2D.Double.intersect(rect1, rect2, intersectionRect);
				contactRadius = (intersectionRect.contains(new Point2D.Double(ell1.x, ell1.y)) || intersectionRect.contains(new Point2D.Double(ell2.x, ell2.y))) 
												? Math.min(intersectionRect.width, intersectionRect.height) : Math.max(intersectionRect.width, intersectionRect.height);
				contactRadius/=2;
				this.contactAreaBar = new ContactAreaBar(10+contactRadius, 30, 2*contactRadius, false);
			}
			else if((ell1.a/ell1.b) > 10 || (ell2.a/ell2.b) > 10){
				this.circles.clear();
				Ellipse flatEllipse=null, otherEllipse=null;
				if((ell1.a/ell1.b) > 10){
					flatEllipse = ell1;
					otherEllipse = ell2;
				}
				else if((ell2.a/ell2.b) > 10){ 
					flatEllipse = ell2;
					otherEllipse = ell1;
				}
				double dx = otherEllipse.x - flatEllipse.x;
				double dy = otherEllipse.y - flatEllipse.y;
				if(Math.abs(dy) <= flatEllipse.b){
					contactRadius = flatEllipse.b;
				}
				else{
					double d=(Math.abs(dy)-flatEllipse.b)*(otherEllipse.a/otherEllipse.b);
					contactRadius = Math.sqrt(Math.pow(otherEllipse.a, 2)-Math.pow(d, 2));
					
					this.circles.add(new Circle((otherEllipse.x), (flatEllipse.y)+(Math.signum(dy)*(d+flatEllipse.b)), otherEllipse.a));
				}
				this.contactAreaBar = new ContactAreaBar(10+contactRadius, 30, 2*contactRadius, false);
			}
			else{
				double r1_old = calculateDistanceToCellCenter(centerEllipse1, centerEllipse2, ell1.a, ell1.b);
				double r2_old = calculateDistanceToCellCenter(centerEllipse2, centerEllipse1, ell2.a, ell2.b);
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
					this.circles.add(new Circle((ell1.x+dirVect1.x), (ell1.y+dirVect1.y), r1));
					this.circles.add(new Circle((ell2.x+dirVect2.x), (ell2.y+dirVect2.y), r2));
				}
			}			
		}
	}
	
	public void start(){
		double a1 = 200;
		double b1 = 10;
		double a2 = 200;
		double b2 = 10;
		Point2d centerEllipse1 = new Point2d(300, 200);
		Point2d centerEllipse2 = new Point2d(450, 200);
		
		this.ellipses = new ArrayList<Ellipse>();
		this.ellipses.add(new Ellipse(centerEllipse1.x,centerEllipse1.y,a1,b1));
		this.ellipses.add(new Ellipse(centerEllipse2.x,centerEllipse2.y,a2,b2));
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
