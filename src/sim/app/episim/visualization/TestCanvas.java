package sim.app.episim.visualization;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TestCanvas extends Canvas {
	
	private final int RADIUS = 20;
	
	private ArrayList<CellEllipse>  cellEllipses = new ArrayList<CellEllipse>();
	

	
	private CellEllipse draggedCellEllipse = null;
	
	private Set<String> ellipseKeySet;
	
	private int nextId = 0;
	
	public TestCanvas(){
		ellipseKeySet = new HashSet<String>();
		this.setBackground(Color.white);
		
		
	}
	
	
	public void drawCellEllipse(int x, int y, Color c){
		drawCellEllipse(new CellEllipse(getNextCellEllipseId(), x, y, RADIUS, RADIUS*2, c), true);		
	}
	
	public void drawCellEllipse(int x, int y, int r1, int r2, Color c){
		drawCellEllipse(new CellEllipse(getNextCellEllipseId(),x, y, r1, r2, c), true);
	}
	
	private void drawCellEllipse(CellEllipse cellEllipse, boolean newCellEllipse){
		Graphics2D g = (Graphics2D) this.getGraphics();
		if(g != null){
			g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			Color oldColor = g.getColor();
			g.setColor(cellEllipse.c);
			g.draw(cellEllipse.getClippedEllipse());
			g.setColor(oldColor);
			drawPoint(g, cellEllipse.getX(), cellEllipse.getY(), 2, cellEllipse.c);
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
			
			drawCellEllipse(ell, false);
		}
	}
	
	private CellEllipse findCellEllipse(int x, int y){
		CellEllipse cellEllipseWithMinimalDistance = null;
		double minimalDistance = Double.POSITIVE_INFINITY;
		for(CellEllipse ell: cellEllipses){
			if(   x >= (ell.getX() -ell.getR1()) && y >= (ell.getY() - ell.getR2())
				&& x <= (ell.getX() + ell.getR1()) && y <= (ell.getY() + ell.getR2())){
				
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
		for(int n = 0; n < numberOfCells; n++){
			CellEllipse actEll = cellEllipses.get(n);
			for(int m = 0; m < numberOfCells; m++){
				if(n == m) continue;
				else{
					CellEllipse otherEll = cellEllipses.get(m);
					if(doOverlap(actEll.getEllipse(), otherEll.getEllipse())){
						int [][] intersectionPoints = getIntersectionPoints(actEll.getEllipse(), otherEll.getEllipse());
						//maximum of 4 intersection points for two ellipses
						for(int i = 0; i < 4; i++){
							drawPoint(g, intersectionPoints[i][0], intersectionPoints[i][1], 4, Color.RED);
							
						}
						//maxiumum of two intersection points for cells in later simulation
						//drawSquare(g, intersectionPoints[0], intersectionPoints[1], (int) actEll.getEllipse().getWidth(), actEll.getY() > otherEll.getY());
					
						if(!this.ellipseKeySet.contains(actEll.getId()+","+otherEll.getId()))
							clipEllipse(g, intersectionPoints[0], intersectionPoints[1], actEll, otherEll);
						
					}
				}
			}
		}
	}
	
	private int[][] getIntersectionPoints(Shape shape1, Shape shape2){
		
		//maximum of 4 intersection points for two ellipses
		int [][] intersectionPoints = new int[4][2];
		
		
		if(shape1 instanceof Ellipse2D && shape2 instanceof Ellipse2D){
			
			Area a1 = new Area(shape1);
	        Area a2 = new Area(shape2);
	        a1.intersect(a2);
	        PathIterator it = a1.getPathIterator(null);
	        double[] d = new double[6];
	        double xOLD = 0;
	        double yOLD = 0;
	        int i = 0;       
	        
	        boolean newIteration = true;
	        while (newIteration){
				int type = it.currentSegment(d);

				switch(type) {

					case PathIterator.SEG_CUBICTO:
	
					case PathIterator.SEG_LINETO: {
						if(Math.round(d[0]) == xOLD && Math.round(d[1]) == yOLD){
							intersectionPoints[i][0] = (int)xOLD;
							intersectionPoints[i][1] = (int)yOLD;
							
							i++;
						}
						xOLD = Math.round(d[4]);
						yOLD = Math.round(d[5]);
	
					}
						break;
					case PathIterator.SEG_CLOSE:{
						if(i< 4 && (i % 2)!=0){
							intersectionPoints[i][0] = (int) Math.round(d[4]);
							intersectionPoints[i][1] = (int) Math.round(d[5]);
						}
						newIteration = false;
					}
				}
				it.next();

			}
			
		}
		
		
		return intersectionPoints;
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
	
	private void drawSquare(Graphics2D g, int[] sp1, int[] sp2, CellEllipse actEllipse, CellEllipse otherEllipse){
		
		double [] directionVector = {sp1[0]-sp2[0], sp1[1]-sp2[1]};

		double[] newVector = {-1*(directionVector[1]/directionVector[0]), 1};
		double newVectorNormfact = 1/Math.sqrt(Math.pow(newVector[0], 2)+Math.pow(newVector[1], 2));
		

		newVector[0] *= newVectorNormfact;
		newVector[1] *= newVectorNormfact;
		if(actEllipse.getY() < otherEllipse.getY()){				
			newVector[0] *= actEllipse.getBiggerAxis();
			newVector[1] *= actEllipse.getBiggerAxis();
		}
		else{
			newVector[0] *= (-1* actEllipse.getBiggerAxis());
			newVector[1] *= (-1* actEllipse.getBiggerAxis());
		}
		
		//System.out.println(newVector[0]+","+newVector[1]);
		g.drawLine(sp1[0], sp1[1], sp2[0], sp2[1]);
		g.drawLine(sp2[0], sp2[1], sp2[0] + (int)newVector[0],sp2[1] + (int)newVector[1]);
		g.drawLine(sp1[0], sp1[1], sp1[0] + (int)newVector[0],sp1[1] +(int)newVector[1]);
		g.drawLine(sp1[0]+(int)newVector[0], sp1[1]+(int)newVector[1], sp2[0]+(int)newVector[0], sp2[1]+(int)newVector[1]);
	}
	
	private void clipEllipse(Graphics2D g, int[] sp1, int[] sp2, CellEllipse actEllipse, CellEllipse otherEllipse){
		
		this.ellipseKeySet.add(actEllipse.getId()+","+otherEllipse.getId());
		this.ellipseKeySet.add(otherEllipse.getId()+","+actEllipse.getId());
		
		double [] directionVector = {sp1[0]-sp2[0], sp1[1]-sp2[1]};

		double[] newVector = {-1*(directionVector[1]/directionVector[0]), 1};
		double newVectorNormfact = 1/Math.sqrt(Math.pow(newVector[0], 2)+Math.pow(newVector[1], 2));
		

		newVector[0] *= newVectorNormfact;
		newVector[1] *= newVectorNormfact;
		if(actEllipse.getY() < otherEllipse.getY()){				
			newVector[0] *= actEllipse.getBiggerAxis();
			newVector[1] *= actEllipse.getBiggerAxis();
		}
		else{
			newVector[0] *= (-1* actEllipse.getBiggerAxis());
			newVector[1] *= (-1* actEllipse.getBiggerAxis());
		}		
		//System.out.println(newVector[0]+","+newVector[1]);
		
		actEllipse.clipAreaFromEllipse(new Area(new Polygon(
		new int[]{sp1[0], sp2[0], sp2[0] + (int)newVector[0], sp1[0] + (int)newVector[0]},/*x-Points*/
		new int[]{sp1[1], sp2[1], sp2[1] + (int)newVector[1], sp1[1] + (int)newVector[1]},/*y-Points*/
		4
		)));
		
		newVector[0] *= -1;
		newVector[1] *= -1;
		
		otherEllipse.clipAreaFromEllipse(new Area(new Polygon(
				new int[]{sp1[0], sp2[0], sp2[0] + (int)newVector[0], sp1[0] + (int)newVector[0]},/*x-Points*/
				new int[]{sp1[1], sp2[1], sp2[1] + (int)newVector[1], sp1[1] + (int)newVector[1]},/*y-Points*/
				4
				)));
		
	}
	
	private int getNextCellEllipseId(){
		return this.nextId++;
	}
	
	
	

}
