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
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JPanel;

import sim.app.episim.biomechanics.Calculators;
import sim.app.episim.biomechanics.CellPolygon;
import sim.app.episim.biomechanics.Vertex;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.XYPoints;


public class TestCanvas extends JPanel {
	
	private final int RADIUS = 30;
	
	private ArrayList<CellEllipse>  cellEllipses = new ArrayList<CellEllipse>();
	

	
	private CellEllipse draggedCellEllipse = null;
	
	private Set<String> ellipseKeySet;
	
	private long nextId = 0;
	
	private int visualizationStep = 0;
	
	public TestCanvas(){
		ellipseKeySet = new HashSet<String>();
		this.setBackground(Color.white);
		
		 
		CellEllipse cellEll = new CellEllipse(getNextCellEllipseId(), 100, 290, 3*RADIUS, RADIUS, Color.BLUE);
		cellEll.rotateCellEllipseInDegrees(0);
		this.drawCellEllipse(null,cellEll, true);
		
			cellEll = new CellEllipse(getNextCellEllipseId(), 100, 290,  3*RADIUS, RADIUS, Color.BLUE);
		 cellEll.rotateCellEllipseInDegrees(90);
			this.drawCellEllipse(null,cellEll, true);
		
		
	}
	
	
	
	/*
	public void drawCellEllipse(int x, int y, Color c){
		int radius1 = 0;
		int radius2 = 0;
		
		//while(radius1 < RADIUS) radius1 = rand.nextInt(2*RADIUS);
		//while(radius2 < RADIUS || radius2 > radius1) radius2 = rand.nextInt(2*RADIUS);
		
		CellEllipse cellEll = new CellEllipse(getNextCellEllipseId(), x, y, 2*RADIUS, RADIUS, c);
		//cellEll.rotateCellEllipseInDegrees(rand.nextInt(180));
		drawCellEllipse(null,cellEll, true);
		
	}*/
	
	public void addImportedCells(List<CellEllipse> importedCells){
		this.cellEllipses.clear();
		this.cellEllipses.addAll(importedCells);
		this.repaint();
		
	}
	
	public void drawCellEllipse(int x, int y, int r1, int r2, Color c){
		drawCellEllipse(null,new CellEllipse(getNextCellEllipseId(), x, y, r1, r2, c), true);
	}
	
	private void drawCellEllipse(Graphics2D g,CellEllipse cellEllipse, boolean newCellEllipse){
		if(g==null)g = (Graphics2D) this.getGraphics();
		if(g != null){
			g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			Color oldColor = g.getColor();
			g.setColor(cellEllipse.getFillColor());
			g.fill(cellEllipse.getClippedEllipse());
			g.setColor(cellEllipse.getColor());
			g.draw(cellEllipse.getClippedEllipse());
			g.setColor(oldColor);
			drawPoint(g, cellEllipse.getX(), cellEllipse.getY(), 2, cellEllipse.getColor());
			CellPolygon cellPol = Calculators.getCellPolygon(cellEllipse);
			Vertex[] vertices = null;
			if(cellPol != null && (vertices = cellPol.getVertices()) != null){
				for(Vertex v : vertices){
					if(v != null)drawPoint(g, v.getIntX(), v.getIntY(), 5, Color.RED);
				}
			}
		}
		if(newCellEllipse) cellEllipses.add(cellEllipse);
	}
	
	
	public CellEllipse pickCellEllipse(int x, int y){
		CellEllipse cellEllipse = findCellEllipse(x, y);
		if(cellEllipse != null){
			draggedCellEllipse	=cellEllipse;
		}
		return cellEllipse;
	}
	
	public void dragCellEllipse(int x, int y){
		if(draggedCellEllipse != null){
			draggedCellEllipse.setXY(x, y);
			
			repaint();
		}
	}
	
	public void releaseCellEllipse(){
		draggedCellEllipse = null;
	}
	
	
	public void paint(Graphics g){
		super.paint(g);
		this.ellipseKeySet.clear();
		CellEllipseIntersectionCalculationRegistry.getInstance().reset();
		for(CellEllipse ell : cellEllipses){
			ell.resetClippedEllipse();
		}
		calculateIntersectionPointsForCellEllipses((Graphics2D)g);
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
	
	
	
	
	private void calculateIntersectionPointsForCellEllipses(Graphics2D g){
		int numberOfCells = cellEllipses.size();
		visualizationStep++;
		for(int n = 0; n < numberOfCells; n++){
			CellEllipse actEll = cellEllipses.get(n);
			
				for(int m = 0; m < numberOfCells; m++){
					if(n == m) continue;
					else{
						CellEllipse otherEll = cellEllipses.get(m);
						
							
						if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(actEll.getId(), otherEll.getId(), visualizationStep)){
					   	CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(actEll.getId(), otherEll.getId());
					   	
					   	
							
								//maximum of 4 intersection points for two ellipses
							/*	for(int i = 0; i < 4; i++){
									drawPoint(g, intersectionPoints[i][0], intersectionPoints[i][1], 4, Color.RED);
									
								}*/
								this.ellipseKeySet.add(actEll.getId()+","+otherEll.getId());
								this.ellipseKeySet.add(otherEll.getId()+","+actEll.getId());
								
								
								XYPoints xyPoints = EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(g ,actEll, otherEll);
								if(xyPoints != null){ 
									drawIntersectionLine(g, xyPoints);
									//drawSquares(g, xyPoints);
								}
							}
							//maxiumum of two intersection points for cells in later simulation
							
							
					
					}
				}
				
		}
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
	
	private void drawIntersectionLine(Graphics2D g, XYPoints xyPoints){
		g.drawLine(xyPoints.xPointsQuaderEllipse1[0], xyPoints.yPointsQuaderEllipse1[0], xyPoints.xPointsQuaderEllipse1[1], xyPoints.yPointsQuaderEllipse1[1]);
	}
	
	private void drawSquares(Graphics2D g, XYPoints xyPoints){
				
		//System.out.println(newVector[0]+","+newVector[1]);
		g.drawLine(xyPoints.xPointsQuaderEllipse1[0], xyPoints.yPointsQuaderEllipse1[0], xyPoints.xPointsQuaderEllipse1[1], xyPoints.yPointsQuaderEllipse1[1]);
		g.drawLine(xyPoints.xPointsQuaderEllipse1[1], xyPoints.yPointsQuaderEllipse1[1], xyPoints.xPointsQuaderEllipse1[2], xyPoints.yPointsQuaderEllipse1[2]);
		g.drawLine(xyPoints.xPointsQuaderEllipse1[2], xyPoints.yPointsQuaderEllipse1[2], xyPoints.xPointsQuaderEllipse1[3], xyPoints.yPointsQuaderEllipse1[3]);
		g.drawLine(xyPoints.xPointsQuaderEllipse1[3], xyPoints.yPointsQuaderEllipse1[3], xyPoints.xPointsQuaderEllipse1[0], xyPoints.yPointsQuaderEllipse1[0]);
	/*	
		g.drawLine(xyPoints.xPointsQuaderEllipse2[0], xyPoints.yPointsQuaderEllipse2[0], xyPoints.xPointsQuaderEllipse2[1], xyPoints.yPointsQuaderEllipse2[1]);
		g.drawLine(xyPoints.xPointsQuaderEllipse2[1], xyPoints.yPointsQuaderEllipse2[1], xyPoints.xPointsQuaderEllipse2[2], xyPoints.yPointsQuaderEllipse2[2]);
		g.drawLine(xyPoints.xPointsQuaderEllipse2[2], xyPoints.yPointsQuaderEllipse2[2], xyPoints.xPointsQuaderEllipse2[3], xyPoints.yPointsQuaderEllipse2[3]);
		g.drawLine(xyPoints.xPointsQuaderEllipse2[3], xyPoints.yPointsQuaderEllipse2[3], xyPoints.xPointsQuaderEllipse2[0], xyPoints.yPointsQuaderEllipse2[0]);
		*/
	}
	
	
	
	private long getNextCellEllipseId(){
		return this.nextId++;
	}

	
	

	

}
