package sim.app.episim.visualization;

import java.awt.BasicStroke;

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JPanel;


public class TestCanvas extends JPanel {
	
	private final int RADIUS = 20;
	
	private ArrayList<CellEllipse>  cellEllipses = new ArrayList<CellEllipse>();
	

	
	private CellEllipse draggedCellEllipse = null;
	
	private Set<String> ellipseKeySet;
	
	private int nextId = 0;
	private Random rand;
	public TestCanvas(){
		ellipseKeySet = new HashSet<String>();
		this.setBackground(Color.white);
		
		
		CellEllipse cellEll = new CellEllipse(getNextCellEllipseId(), 99, 290, RADIUS*3, RADIUS, 0, 0, Color.BLUE);
		cellEll.rotateCellEllipseInDegrees(10);
		this.drawCellEllipse(null,cellEll, true);
		
	/*	cellEll = new CellEllipse(getNextCellEllipseId(), 139, 285, RADIUS*3, RADIUS, Color.BLUE);
	   cellEll.rotateCellEllipseInDegrees(100);
		this.drawCellEllipse(null,cellEll, true);*/
		
		rand = new Random();
	}
	
	
	private class XYPoints{
		
		public int[] xPointsEllipse1;
		public int[] yPointsEllipse1;
		public int[] xPointsEllipse2;
		public int[] yPointsEllipse2;
		
		protected void swapEllipse1And2(){
			int[] tmpX = xPointsEllipse1;
			int[] tmpY = yPointsEllipse1;
			
			xPointsEllipse1 = xPointsEllipse2;
			yPointsEllipse1 = yPointsEllipse2;
			
			xPointsEllipse2 =	tmpX;
			yPointsEllipse2 = tmpY;
		}
		
		public String toString(){
			StringBuffer str = new StringBuffer();
			str.append("Ellipse 1:\n");
			for(int i = 0; i < xPointsEllipse1.length && i < yPointsEllipse1.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+xPointsEllipse1[i]+","+yPointsEllipse1[i]+")  ");
			str.append("\nEllipse 2:\n");
			for(int i = 0; i < xPointsEllipse2.length && i < yPointsEllipse2.length;i++) 
				str.append("X"+ (i+1)+",Y"+(i+1)+"("+xPointsEllipse2[i]+","+yPointsEllipse2[i]+")  ");
			str.append("\n");
			return str.toString();
		}
		
	}
	
	
	
	
	public void drawCellEllipse(int x, int y, Color c){
		CellEllipse cellEll = new CellEllipse(getNextCellEllipseId(), x, y, RADIUS, RADIUS*2, 0, 0, c);
		cellEll.rotateCellEllipseInDegrees(rand.nextInt(180));
		drawCellEllipse(null,cellEll, true);
		
	}
	
	public void drawCellEllipse(int x, int y, int r1, int r2, Color c){
		drawCellEllipse(null,new CellEllipse(getNextCellEllipseId(), x, y, r1, r2, 0, 0, c), true);
	}
	
	private void drawCellEllipse(Graphics2D g,CellEllipse cellEllipse, boolean newCellEllipse){
		if(g==null)g = (Graphics2D) this.getGraphics();
		if(g != null){
			g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			Color oldColor = g.getColor();
			g.setColor(cellEllipse.getColor());
			g.draw(cellEllipse.getClippedEllipse());
			g.setColor(oldColor);
			drawPoint(g, cellEllipse.getX(), cellEllipse.getY(), 2, cellEllipse.getColor());
		}
		if(newCellEllipse) cellEllipses.add(cellEllipse);
	}
	
	
	public void pickCellEllipse(int x, int y){
		CellEllipse cellEllipse = findCellEllipse(x, y);
		if(cellEllipse != null){
			draggedCellEllipse	=cellEllipse;
		}
	}
	
	public void dragCellEllipse(int x, int y){
		if(draggedCellEllipse != null){
			draggedCellEllipse.setX(x);
			draggedCellEllipse.setY(y);
			repaint();
		}
	}
	
	public void releaseCellEllipse(){
		draggedCellEllipse = null;
	}
	
	
	public void paint(Graphics g){
		super.paint(g);
		this.ellipseKeySet.clear();
		
		for(CellEllipse ell : cellEllipses){
			ell.resetClippedEllipse();
		}
		drawIntersectionPointsForCellEllipses((Graphics2D)g);
		for(CellEllipse ell : cellEllipses){
			
			drawCellEllipse((Graphics2D) g,ell, false);
		}
	}
	
	private CellEllipse findCellEllipse(int x, int y){
		CellEllipse cellEllipseWithMinimalDistance = null;
		double minimalDistance = Double.POSITIVE_INFINITY;
		for(CellEllipse ell: cellEllipses){
			if(   x >= (ell.getX() -ell.getMajorAxis()) && y >= (ell.getY() - ell.getMinorAxis())
				&& x <= (ell.getX() + ell.getMajorAxis()) && y <= (ell.getY() + ell.getMinorAxis())){
				
				double distance = distance(x, y, ell.getX(), ell.getY());
				
				if(distance < minimalDistance){
					minimalDistance = distance;
					cellEllipseWithMinimalDistance = ell;
				}
				
			}
		}
		return cellEllipseWithMinimalDistance;
	}
	
	private double distance(int x1, int y1, int x2, int y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	
	private boolean doOverlap(Shape shape1, Shape shape2){
		Area a1 = new Area(shape1);
		Area a2 = new Area(shape2);
		
		a1.intersect(a2);
		
		return !a1.isEmpty();
	}
	
	private void drawIntersectionPointsForCellEllipses(Graphics2D g){
		int numberOfCells = cellEllipses.size();
		int [][] intersectionPoints = new int[4][2];
		for(int n = 0; n < numberOfCells; n++){
			CellEllipse actEll = cellEllipses.get(n);
			for(int m = 0; m < numberOfCells; m++){
				if(n == m) continue;
				else{
					CellEllipse otherEll = cellEllipses.get(m);
					if(doOverlap(actEll.getEllipse(), otherEll.getEllipse())){
						
						if(!this.ellipseKeySet.contains(actEll.getId()+","+otherEll.getId())){
								
							intersectionPoints = getIntersectionPoints(actEll.getEllipse(), otherEll.getEllipse());
						
							//maximum of 4 intersection points for two ellipses
						/*	for(int i = 0; i < 4; i++){
								drawPoint(g, intersectionPoints[i][0], intersectionPoints[i][1], 4, Color.RED);
								
							}*/
							
							XYPoints xyPoints = calculateXYPoints(intersectionPoints[0], intersectionPoints[1], actEll, otherEll);
							
							clipEllipses(g, actEll, otherEll, xyPoints);
							//drawSquares(g, xyPoints);
						}
						//maxiumum of two intersection points for cells in later simulation
						
						
					}
				}
			}
		}
	}
	
	private int[][] getIntersectionPoints(Shape shape1, Shape shape2){
		
		//maximum of 4 intersection points for two ellipses
		int [][] intersectionPoints = new int[4][2];
					
			Area a1 = new Area(shape1);
	        Area a2 = new Area(shape2);
	        a1.intersect(a2);
	        PathIterator it = a1.getPathIterator(null);
	        double[] d = new double[6];
	        double xOLD = 0;
	        double yOLD = 0;
	        int i = 0;       
	        
	        int previousSpX=0;
	        int previousSpY=0;
	        
	        boolean newIteration = true;
	        boolean intersectionPointConfirmed = false;
	        
	        while (newIteration){
	      	  
				int type = it.currentSegment(d);

				switch(type) {

					case PathIterator.SEG_LINETO:{
						intersectionPointConfirmed = true;
					}
	
					case PathIterator.SEG_CUBICTO: {
						if(equalPointsForItersection(xOLD, d[0])  && equalPointsForItersection(yOLD, d[1]) && intersectionPointConfirmed){
							if(previousSpX != getEqualPointsForItersection(xOLD, d[0]) || previousSpY != getEqualPointsForItersection(yOLD, d[1])){
								intersectionPoints[i][0] = getEqualPointsForItersection(xOLD, d[0]);
								intersectionPoints[i][1] = getEqualPointsForItersection(yOLD, d[1]);
								previousSpX = intersectionPoints[i][0];
						      previousSpY = intersectionPoints[i][1];
						      intersectionPointConfirmed = false;
								i++;
							}
						}
						else intersectionPointConfirmed = false;
						xOLD = d[4];
						yOLD = d[5];
	
					}
						break;
					case PathIterator.SEG_CLOSE:{
						if(i< 4 && (i % 2)!=0){
							if(previousSpX != ((int) Math.round(d[4])) || previousSpY != ((int) Math.round(d[5]))){
								intersectionPoints[i][0] = (int) Math.round(d[4]);
								intersectionPoints[i][1] = (int) Math.round(d[5]);
							}
						}
						newIteration = false;
					}
				}
				it.next();

			}
				
		return intersectionPoints;
	}
	
	private boolean equalPointsForItersection(double oldPoint, double newPoint){
		if(Math.abs(oldPoint-newPoint) <= 1.5) return true;
		
		return false;
	}
	private int getEqualPointsForItersection(double oldPoint, double newPoint){
		if(Math.round(oldPoint) == Math.round(newPoint)) return (int) Math.round(newPoint);
		else if(Math.floor(oldPoint) == Math.round(newPoint)) return (int) Math.round(newPoint);
		else if(Math.round(oldPoint) == Math.floor(newPoint)) return (int) Math.floor(newPoint);
		else if(Math.floor(oldPoint) == Math.floor(newPoint)) return (int) Math.floor(newPoint);
		
		return (int) Math.round(newPoint);
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
	
	private void drawSquares(Graphics2D g, XYPoints xyPoints){
				
		//System.out.println(newVector[0]+","+newVector[1]);
		g.drawLine(xyPoints.xPointsEllipse1[0], xyPoints.yPointsEllipse1[0], xyPoints.xPointsEllipse1[1], xyPoints.yPointsEllipse1[1]);
		g.drawLine(xyPoints.xPointsEllipse1[1], xyPoints.yPointsEllipse1[1], xyPoints.xPointsEllipse1[2], xyPoints.yPointsEllipse1[2]);
		g.drawLine(xyPoints.xPointsEllipse1[2], xyPoints.yPointsEllipse1[2], xyPoints.xPointsEllipse1[3], xyPoints.yPointsEllipse1[3]);
		g.drawLine(xyPoints.xPointsEllipse1[3], xyPoints.yPointsEllipse1[3], xyPoints.xPointsEllipse1[0], xyPoints.yPointsEllipse1[0]);
		
		g.drawLine(xyPoints.xPointsEllipse2[0], xyPoints.yPointsEllipse2[0], xyPoints.xPointsEllipse2[1], xyPoints.yPointsEllipse2[1]);
		g.drawLine(xyPoints.xPointsEllipse2[1], xyPoints.yPointsEllipse2[1], xyPoints.xPointsEllipse2[2], xyPoints.yPointsEllipse2[2]);
		g.drawLine(xyPoints.xPointsEllipse2[2], xyPoints.yPointsEllipse2[2], xyPoints.xPointsEllipse2[3], xyPoints.yPointsEllipse2[3]);
		g.drawLine(xyPoints.xPointsEllipse2[3], xyPoints.yPointsEllipse2[3], xyPoints.xPointsEllipse2[0], xyPoints.yPointsEllipse2[0]);
		
	}
	
	private void clipEllipses(Graphics2D g, CellEllipse actEllipse, CellEllipse otherEllipse, XYPoints xyPoints){
			
		actEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsEllipse1, xyPoints.yPointsEllipse1, 4)));
		otherEllipse.clipAreaFromEllipse(new Area(new Polygon(xyPoints.xPointsEllipse2, xyPoints.yPointsEllipse2, 4)));
		
	}
	
	private int getNextCellEllipseId(){
		return this.nextId++;
	}

	
	private XYPoints calculateXYPoints(int[] sp1, int[] sp2, CellEllipse actEllipse, CellEllipse otherEllipse){
		this.ellipseKeySet.add(actEllipse.getId()+","+otherEllipse.getId());
		this.ellipseKeySet.add(otherEllipse.getId()+","+actEllipse.getId());
		
	//	System.out.println("SP 1:"+ sp1[0] + ","+ sp1[1]);
		//System.out.println("SP 2:"+ sp2[0] + ","+ sp2[1]);
		
		double [] directionVector = new double[]{sp1[0]-sp2[0], sp1[1]-sp2[1]};
		
		double[] newVector; 
		
		if(directionVector[0]==0){ 
			if(sp1[0]> actEllipse.getX()) newVector = new double[]{1, 0};
			else newVector = new double[]{-1, 0};
		}
		else if(directionVector[1]==0){
			if(sp1[1]> actEllipse.getY()) newVector = new double[]{0, 1};
			else newVector = new double[]{0, -1};
		}
		else{ 
			newVector = new double[]{-1*(directionVector[1]/directionVector[0]), 1};
			double newVectorNormfact = 1/Math.sqrt(Math.pow(newVector[0], 2)+Math.pow(newVector[1], 2));
			newVector[0] *= newVectorNormfact;
			newVector[1] *= newVectorNormfact;
			if(actEllipse.getY() >= otherEllipse.getY()){
				newVector[0] *= -1;
				newVector[1] *= -1;
			}
			
			//if(sp1[0] < acr)
			
		}
		
	//	System.out.println("actEllipse("+actEllipse.getX()+","+ actEllipse.getY()+") otherEllipse("+otherEllipse.getX()+","+otherEllipse.getY()+")");
		
			
		newVector[0] *= actEllipse.getMajorAxis();
		newVector[1] *= actEllipse.getMinorAxis();
				
		XYPoints xyPoints = new XYPoints();
		
		xyPoints.xPointsEllipse1 = new int[]{sp1[0], sp2[0], sp2[0] + (int)newVector[0], sp1[0] + (int)newVector[0]};/*x-Points*/
		xyPoints.yPointsEllipse1 = new int[]{sp1[1], sp2[1], sp2[1] + (int)newVector[1], sp1[1] + (int)newVector[1]};/*y-Points*/
	/*	
		System.out.print("X-Points: ");
		for(int x:xyPoints.xPointsEllipse1)System.out.print(x+", ");
		System.out.println();
	
		System.out.print("Y-Points: ");
		for(int y:xyPoints.yPointsEllipse1)System.out.print(y+", ");
		System.out.println();
		
		System.out.println("Direction Vector: ("+directionVector[0]+", "+directionVector[1]+")");
		System.out.println("New Vector: ("+newVector[0]+", "+newVector[1]+")");
		
		*/
		
		newVector[0] *= -1;
		newVector[1] *= -1;
		
		
		xyPoints.xPointsEllipse2 = new int[]{sp1[0], sp2[0], sp2[0] + (int)newVector[0], sp1[0] + (int)newVector[0]};/*x-Points*/
		xyPoints.yPointsEllipse2 = new int[]{sp1[1], sp2[1], sp2[1] + (int)newVector[1], sp1[1] + (int)newVector[1]};/*y-Points*/
		
	final double FACTORDIRVECT = 1.5;	
		
	//Point-correction in order to cut everything without remaining spaces
		if(xyPoints.xPointsEllipse1[2] < xyPoints.xPointsEllipse1[3]){
			if(directionVector[0] <= 0){
				xyPoints.xPointsEllipse1[2] += directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse1[3] -= directionVector[0]*FACTORDIRVECT;
			}
			else{
				xyPoints.xPointsEllipse1[2] -= directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse1[3] += directionVector[0]*FACTORDIRVECT;
			}
		}
		else if(xyPoints.xPointsEllipse1[2] > xyPoints.xPointsEllipse1[3]){			
			if(directionVector[0] <= 0){
				xyPoints.xPointsEllipse1[3] += directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse1[2] -= directionVector[0]*FACTORDIRVECT;
			}
			else{
				xyPoints.xPointsEllipse1[3] -= directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse1[2] += directionVector[0]*FACTORDIRVECT;
			}
		}
		if(xyPoints.yPointsEllipse1[2] < xyPoints.yPointsEllipse1[3]){
			if(directionVector[1] <= 0){
				xyPoints.yPointsEllipse1[2] += directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse1[3] -= directionVector[1]*FACTORDIRVECT;
			}
			else{
				xyPoints.yPointsEllipse1[2] -= directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse1[3] += directionVector[1]*FACTORDIRVECT;
			}
		}
		else if(xyPoints.yPointsEllipse1[2] > xyPoints.yPointsEllipse1[3]){
			if(directionVector[1] <= 0){
				xyPoints.yPointsEllipse1[3] += directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse1[2] -= directionVector[1]*FACTORDIRVECT;
			}
			else{
				xyPoints.yPointsEllipse1[3] -= directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse1[2] += directionVector[1]*FACTORDIRVECT;
			}
		}
				
				
		
		//Point-correction in order to cut everything without remaining spaces
		if(xyPoints.xPointsEllipse2[2] < xyPoints.xPointsEllipse2[3]){
			if(directionVector[0] <= 0){
				xyPoints.xPointsEllipse2[2] += directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse2[3] -= directionVector[0]*FACTORDIRVECT;
			}
			else{
				xyPoints.xPointsEllipse2[2] -= directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse2[3] += directionVector[0]*FACTORDIRVECT;
			}
		}
		else if(xyPoints.xPointsEllipse2[2] > xyPoints.xPointsEllipse2[3]){
			if(directionVector[0] <= 0){
				xyPoints.xPointsEllipse2[3] += directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse2[2] -= directionVector[0]*FACTORDIRVECT;
			}
			else{
				xyPoints.xPointsEllipse2[3] -= directionVector[0]*FACTORDIRVECT;
				xyPoints.xPointsEllipse2[2] += directionVector[0]*FACTORDIRVECT;
			}
		}
		if(xyPoints.yPointsEllipse2[2] < xyPoints.yPointsEllipse2[3]){
			if(directionVector[1] <= 0){
				xyPoints.yPointsEllipse2[2] += directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse2[3] -= directionVector[1]*FACTORDIRVECT;
			}
			else{
				xyPoints.yPointsEllipse2[2] -= directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse2[3] += directionVector[1]*FACTORDIRVECT;
			}
		}
		else if(xyPoints.yPointsEllipse2[2] > xyPoints.yPointsEllipse2[3]){
			if(directionVector[1] <= 0){
				xyPoints.yPointsEllipse2[3] += directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse2[2] -= directionVector[1]*FACTORDIRVECT;
			}
			else{
				xyPoints.yPointsEllipse2[3] -= directionVector[1]*FACTORDIRVECT;
				xyPoints.yPointsEllipse2[2] += directionVector[1]*FACTORDIRVECT;
			}
		}
		
		//Look if swap is necessary using the distance between centroid of actEllipse and the 
		//one of the resulting points of the trapeze (different from the intersection points sp)
		//we're looking for maximal distance
		
		if(distance(actEllipse.getX(), actEllipse.getY(), xyPoints.xPointsEllipse1[2], xyPoints.yPointsEllipse1[2]) < 
				distance(actEllipse.getX(), actEllipse.getY(), xyPoints.xPointsEllipse2[2], xyPoints.yPointsEllipse2[2])) xyPoints.swapEllipse1And2();
		
		return xyPoints;
	}

	

}
