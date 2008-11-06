package sim.app.episim.visualization;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;


public class TestCanvas extends Canvas {
	
	private final int RADIUS = 20;
	
	private ArrayList<CellEllipse>  cellEllipses = new ArrayList<CellEllipse>();
	

	
	private CellEllipse draggedCellEllipse = null;
	
	
	
	public TestCanvas(){
		
		this.setBackground(Color.white);
		
		
	}
	
	
	public void drawCellEllipse(int x, int y){
		drawCellEllipse(new CellEllipse(x, y, RADIUS, RADIUS*2), true);		
	}
	
	public void drawCellEllipse(int x, int y, int r1, int r2){
		drawCellEllipse(new CellEllipse(x, y, r1, r2), true);
	}
	
	private void drawCellEllipse(CellEllipse cellEllipse, boolean newCellEllipse){
		Graphics2D g = (Graphics2D) this.getGraphics();
		g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(Color.BLUE);
		g.draw(cellEllipse.getEllipse());
		drawPoint(g, cellEllipse.getX(), cellEllipse.getY(), 2, Color.BLUE);
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
		
		
		g.drawLine(50, 10, 150, 10);
		g.drawLine(50, 10, (int)(100*Math.cos(0.5*Math.PI) +50), (int)(100*Math.sin(0.5*Math.PI) +10));
		
		for(CellEllipse ell : cellEllipses){
			drawCellEllipse(ell, false);
		}
		drawIntersectionPointsForCellEllipses((Graphics2D)g);
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
							System.out.println("IntersectionPoint " +i+" found:" + xOLD +", " + yOLD);
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
							System.out.println("IntersectionPoint " +i+" found:" + xOLD +", " + yOLD);
						}
						newIteration = false;
					}
				}
				it.next();

			}
			
		}
		System.out.println();
		
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
	
	
	
	

}
