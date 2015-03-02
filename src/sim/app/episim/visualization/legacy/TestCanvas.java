package sim.app.episim.visualization.legacy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModelController;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.CellPolygonNetworkBuilder;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.ContinuousVertexField;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.Vertex;
import sim.app.episim.model.biomechanics.vertexbased2D.util.CellCanvas;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.IntersectionPoints;
import sim.app.episim.visualization.CellEllipse;


public class TestCanvas extends JPanel {
	
	private final int RADIUS = 30;
	
	private ArrayList<CellEllipse>  cellEllipses = new ArrayList<CellEllipse>();
	private ArrayList<CellPolygon>  cellPolygons = new ArrayList<CellPolygon>();
	private ArrayList<Vertex>  vertices = new ArrayList<Vertex>();
	
	private GeneralPath surface = null;
	private GeneralPath basalLayer = null;
	private GeneralPath fullContour = null;
	
	private CellEllipse draggedCellEllipse = null;
	private CellPolygon draggedCellPolygon = null;
	private Vertex draggedVertex = null;
	
	private Set<String> ellipseKeySet;
	
	private long nextId = 0;
	
	private int visualizationStep = 0;
	
	private boolean importedTissueVisualizationMode = true;
	
	
	
	private CellCanvas cellCanvas;
	
	private final int CANVAS_ANCHOR_X = 100;
	private final int CANVAS_ANCHOR_Y = 100;
	
	
	private Color backgroundColor;
	private Color cellColor;
	private Color outerSurfaceColor;
	private Color basementMembraneColor;
	private Color cellMembraneColor;
	private Color cellCenterColor;
	
	
	
	public TestCanvas(){
		ellipseKeySet = new HashSet<String>();
		resetColors();
		this.setBackground(Color.white);
		
		 
	/*CellEllipse cellEll = new CellEllipse(getNextCellEllipseId(), 100, 290, 3*RADIUS, RADIUS, Color.BLUE);
	  cellEll.rotateCellEllipseInDegrees(0);
	  this.drawCellEllipse(null,cellEll, true);
		
	  cellEll = new CellEllipse(getNextCellEllipseId(), 100, 290,  3*RADIUS, RADIUS, Color.BLUE);
	  cellEll.rotateCellEllipseInDegrees(90);
	  this.drawCellEllipse(null,cellEll, true);*/
			
	/*  cellCanvas = new CellCanvas(CANVAS_ANCHOR_X,CANVAS_ANCHOR_Y,400,400);
	  ContinuousVertexField.initializeContinousVertexField(400, 400);			
			
	 
		
	  cellPolygons.addAll(Arrays.asList(CellPolygonNetworkBuilder.getStandardCellArray(1, 1)));
	  VertexBasedModelController.getInstance().setCellPolygonArrayInCalculator(cellPolygons.toArray(new CellPolygon[cellPolygons.size()]));
	  rotateCellPolygon(cellPolygons.get(0), 90);*/
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
		basalLayer = TissueController.getInstance().getTissueBorder().getBasalLayerDrawPolygon();
		surface = TissueController.getInstance().getTissueBorder().getSurfaceDrawPolygon();
		fullContour = TissueController.getInstance().getTissueBorder().getFullContourDrawPolygon();
		this.repaint();		
	}
	
	public void clearPanel(){
		if(this.cellEllipses != null) this.cellEllipses.clear();
		if(this.cellPolygons != null) this.cellPolygons.clear();
		if(this.vertices != null) this.vertices.clear();
		if(this.ellipseKeySet != null) this.ellipseKeySet.clear();
		CellEllipseIntersectionCalculationRegistry.getInstance().reset();
		surface = null;
		basalLayer = null;
		fullContour = null;
		
		draggedCellEllipse = null;
		draggedCellPolygon = null;
		draggedVertex = null;		
		
		nextId = 0;		
		visualizationStep = 0;		
		importedTissueVisualizationMode = false;		
		cellCanvas = null;		
	}
	
	public void drawCellEllipse(int x, int y, int r1, int r2, Color c){
		drawCellEllipse(null,new CellEllipse(getNextCellEllipseId(), x, y, r1, r2, c), true);
	}
	
	private void drawCellEllipse(Graphics2D g,CellEllipse cellEllipse, boolean newCellEllipse){
		if(g==null)g = (Graphics2D) this.getGraphics();
		if(g != null){
			if(importedTissueVisualizationMode){
				g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Color oldColor = g.getColor();
				g.setColor(cellColor);
				g.fill(cellEllipse.getClippedEllipse());
			/*	if(cellEllipse.getNucleus() != null){
					g.setColor(new Color(110, 110, 228));
					g.fill(cellEllipse.getNucleus().getClippedEllipse());
					g.setColor(new Color(0,0, 156));
					g.draw(cellEllipse.getNucleus().getClippedEllipse());
				}*/
				g.setColor(cellMembraneColor);
				g.draw(cellEllipse.getClippedEllipse());
			//	g.setColor(Color.YELLOW);
			//	g.draw(cellEllipse.getEllipseBoundingBox());
				g.setColor(oldColor);
				drawPoint(g, (int)cellEllipse.getX(), (int)cellEllipse.getY(), 2, cellCenterColor);
				
			}
			else{
				g.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Color oldColor = g.getColor();
				g.setColor(cellEllipse.getFillColor());
				g.fill(cellEllipse.getClippedEllipse());
				g.setColor(cellEllipse.getColor());
				g.draw(cellEllipse.getClippedEllipse());
				g.setColor(oldColor);
				drawPoint(g, (int)cellEllipse.getX(), (int)cellEllipse.getY(), 2, cellEllipse.getColor());
			}
		}
		if(newCellEllipse) cellEllipses.add(cellEllipse);
	}
	
	
	private void translateCellPolygon(CellPolygon polygon, double new_X, double new_Y){
		Vertex[] vertices = polygon.getSortedVertices();
		Vertex cellCenter = VertexBasedModelController.getInstance().getCellPolygonCalculator().getCellCenter(polygon);
		double deltaX =new_X - cellCenter.getDoubleX();
		double deltaY =new_Y - cellCenter.getDoubleY();
		for(Vertex v : vertices){
			v.setDoubleX(v.getDoubleX()+deltaX);
			v.setDoubleY(v.getDoubleY()+deltaY);
			v.setNewX(v.getDoubleX()+deltaX);
			v.setNewY(v.getDoubleY()+deltaY);
		}
	}
	public void drawCellPolygon(double x, double y){
		CellPolygon pol = CellPolygonNetworkBuilder.getStandardCellArray(1, 1)[0];
		cellPolygons.add(pol);
		VertexBasedModelController.getInstance().setCellPolygonArrayInCalculator(cellPolygons.toArray(new CellPolygon[cellPolygons.size()]));
		translateCellPolygon(pol, x - CANVAS_ANCHOR_X, y - CANVAS_ANCHOR_Y);
		repaint();
	}
	
	public void drawBigVertex(double x, double y){
		vertices.add(new Vertex(x-CANVAS_ANCHOR_X, y-CANVAS_ANCHOR_Y));
		repaint();
	}	
	
	public CellEllipse pickCellEllipse(int x, int y){
		CellEllipse cellEllipse = findCellEllipse(x, y);
		if(cellEllipse != null){
			draggedCellEllipse = cellEllipse;
		}
		return cellEllipse;
	}
	
	public CellPolygon pickCellPolygon(int x, int y){
		CellPolygon cellPolygon = findCellPolygon(x-CANVAS_ANCHOR_X, y-CANVAS_ANCHOR_Y);
		if(cellPolygon != null){
			draggedCellPolygon = cellPolygon;
		}
		return cellPolygon;
	}
	
	public Vertex pickBigVertex(int x, int y){
		Vertex vertex = findBigVertex(x-CANVAS_ANCHOR_X, y-CANVAS_ANCHOR_Y);
		if(vertex != null){
			draggedVertex = vertex;
		}
		return vertex;
	}
	
	
	public void dragCellEllipse(int x, int y){
		if(draggedCellEllipse != null){
			draggedCellEllipse.setXY(x, y);
			
			repaint();
		}
	}
	public void dragCellPolygon(int x, int y){
		if(draggedCellPolygon != null){
			translateCellPolygon(draggedCellPolygon, x-CANVAS_ANCHOR_X, y-CANVAS_ANCHOR_Y);
			
			repaint();
		}
	}
	
	public void dragBigVertex(int x, int y){
		if(draggedVertex != null){			
			draggedVertex.setDoubleX(x-CANVAS_ANCHOR_X); 
			draggedVertex.setDoubleY(y-CANVAS_ANCHOR_Y);			
			repaint();
		}
	}
	
	public void releaseCellEllipse(){
		draggedCellEllipse = null;
	}
	
	public void releaseCellPolygon(){
		VertexBasedModelController.getInstance().getCellPolygonCalculator().checkForT3Transitions(draggedCellPolygon);
		draggedCellPolygon = null;
		repaint();
	}
	
	public void releaseBigVertex(){		
		draggedVertex = null;
		repaint();
	}
	
	public void paint(Graphics g){
		super.paint(g);
		this.ellipseKeySet.clear();
		for(CellEllipse ell : cellEllipses){
			ell.resetClippedEllipse();
		}
		calculateIntersectionPointsForCellEllipses((Graphics2D)g);
		if(importedTissueVisualizationMode){ 
			this.setBackground(backgroundColor);
			for(CellEllipse ell : cellEllipses){				
				drawCellEllipse((Graphics2D) g,ell, false);
			/*	if(ell.getAllIntersectionPointsOfEllipse() != null){
					for(IntersectionPoints isps: ell.getAllIntersectionPointsOfEllipse().values()){
						drawPoint((Graphics2D)g, (int)isps.intersectionPointsX[0], (int)isps.intersectionPointsY[0], 2, Color.BLUE);
						drawPoint((Graphics2D)g, (int)isps.intersectionPointsX[1], (int)isps.intersectionPointsY[1], 2, Color.BLUE);
					}
				}*/
			}
		/*	if(fullContour != null){
				Graphics2D graphics = (Graphics2D) g;
				graphics.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Color oldColor = graphics.getColor();
				
				graphics.setColor(new Color(1, 255, 0));
				graphics.draw(fullContour);
				System.out.println("Full Contour: Breite: "+fullContour.getBounds().width +"  Höhe: "+fullContour.getBounds().height);
				graphics.setColor(oldColor);
			}*/
			
			
			if(basalLayer != null){
				Graphics2D graphics = (Graphics2D) g;
				graphics.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Color oldColor = graphics.getColor();
				
				graphics.setColor(basementMembraneColor);
				graphics.draw(basalLayer);				
				graphics.setColor(oldColor);
			}
			if(surface != null){
				Graphics2D graphics = (Graphics2D) g;
				graphics.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Color oldColor = graphics.getColor();
				
				graphics.setColor(outerSurfaceColor);
				graphics.draw(surface);
				
				graphics.setColor(oldColor);
			}
			/*if(TissueController.getInstance().getActImportedTissue() != null){
				Graphics2D graphics = (Graphics2D) g;
				graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				Color oldColor = graphics.getColor();				
				graphics.setColor(Color.RED);
				for(Point2D p : TissueController.getInstance().getActImportedTissue().getBasalLayerPoints()){				
					graphics.drawRect((int)p.getX(), (int)p.getY(), 1, 1);					
				}
				graphics.setColor(oldColor);
			}*/
		}
		else{
			
			CellEllipseIntersectionCalculationRegistry.getInstance().simulationWasStopped();
			if(cellCanvas != null)cellCanvas.drawCanvasBorder((Graphics2D) g);
			
			for(CellEllipse ell : cellEllipses){				
			//	drawCellEllipse((Graphics2D) g,ell, false);
			//	CellPolygonNetworkBuilder.calculateCellPolygons(ell);
			}
		 	
			CellEllipseIntersectionCalculationRegistry.getInstance().getAllCellEllipseVertices();
			
			
			for(CellEllipse ell : cellEllipses){
				CellPolygonNetworkBuilder.cleanCalculatedVertices(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(ell.getId()));
				CellPolygonNetworkBuilder.calculateEstimatedVertices(ell);		
			}
			
			CellPolygon cellPol = null;
			Vertex[] vertices = null;
			int i = 0;
			for(CellEllipse ell : cellEllipses){
				
				cellPol = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(ell.getId());
				if(cellPol != null && (vertices = cellPol.getUnsortedVertices()) != null){
					drawCellPolygon((Graphics2D)g, cellPol, false);
					
					for(Vertex v : vertices){
						if(v != null){
							if(v.isWasDeleted())drawPoint((Graphics2D)g, v.getIntX(), v.getIntY(), 5, Color.BLACK);
							else if(v.isEstimatedVertex()) drawPoint((Graphics2D)g, v.getIntX(), v.getIntY(), 5, Color.MAGENTA);
							else if(v.isMergeVertex()) drawPoint((Graphics2D)g, v.getIntX(), v.getIntY(), 5, Color.YELLOW);
							else drawPoint((Graphics2D)g, v.getIntX(), v.getIntY(), 5, Color.RED);
						}
					}
					
				}
			
				
			}
			
			for(CellPolygon pol : cellPolygons){ 
				cellCanvas.drawCellPolygon((Graphics2D)g, pol, null, null);
			}
			for(Vertex v : this.vertices){
				cellCanvas.drawBigVertex((Graphics2D)g, v);
			}
		}
	}
	
	private CellEllipse findCellEllipse(int x, int y){
		CellEllipse cellEllipseWithMinimalDistance = null;
		double minimalDistance = Double.POSITIVE_INFINITY;
		for(CellEllipse ell: cellEllipses){
			if(   x >= (ell.getX() -ell.getMajorAxis()) && y >= (ell.getY() - ell.getMinorAxis())
				&& x <= (ell.getX() + ell.getMajorAxis()) && y <= (ell.getY() + ell.getMinorAxis())){
				
				double distance = distance(x, y, (int)ell.getX(), (int)ell.getY());
				
				if(distance < minimalDistance){
					minimalDistance = distance;
					cellEllipseWithMinimalDistance = ell;
				}
				
			}
		}
		return cellEllipseWithMinimalDistance;
	}
	
	private CellPolygon findCellPolygon(double x, double y){
		CellPolygon cellPolygonWithMinimalDistance = null;
		double minimalDistance = Double.POSITIVE_INFINITY;
		Vertex position = new Vertex(x, y);
		CellPolygon polygon;
		Vertex[] vertices = null;
		for(CellPolygon cellPol: cellPolygons){
			vertices =  ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cellPol.getSortedVertices());
			Polygon pol = new Polygon();
			for(Vertex v : vertices){
				pol.addPoint(v.getIntX(), v.getIntY());
			}			
			if(pol.contains(x, y)){
				Vertex cellCenter = VertexBasedModelController.getInstance().getCellPolygonCalculator().getCellCenter(cellPol);
				if(cellCenter.edist(position)< minimalDistance){
					minimalDistance =cellCenter.edist(position);
					cellPolygonWithMinimalDistance = cellPol;
				}
			}				
		}
		return cellPolygonWithMinimalDistance;
	}
	
	private Vertex findBigVertex(double x, double y){
		Vertex vertexWithMinimalDistance = null;
		double minimalDistance = Double.POSITIVE_INFINITY;
		Vertex position = new Vertex(x, y);
		
		for(Vertex v: vertices){			
			if(v.edist(position)< minimalDistance){
				minimalDistance =v.edist(position);
				vertexWithMinimalDistance = v;
			}
		}		
		
		return vertexWithMinimalDistance;
	}
	
	private double distance(int x1, int y1, int x2, int y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	private void rotateCellPolygon(CellPolygon cellPol, double angleInDegrees){
		 	double angle = Math.toRadians(angleInDegrees);
	      double sin = Math.sin(angle);
	      double cos = Math.cos(angle);
	      Vertex cellCenter = VertexBasedModelController.getInstance().getCellPolygonCalculator().getCellCenter(cellPol);
	     	      
	      // rotation
	     Vertex[] vertices = cellPol.getSortedVertices();
	     for(Vertex v: vertices){
	              double a = v.getDoubleX() - cellCenter.getDoubleX();
	              double b = v.getDoubleY() - cellCenter.getDoubleY();
	              int newX = (int) (+a * cos - b * sin + cellCenter.getDoubleX());
	              int newY = (int) (+a * sin + b * cos + cellCenter.getDoubleY());
	             v.setDoubleX(newX);
	             v.setDoubleY(newY);        
	     }
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
								
								
								IntersectionPoints isPoints = EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(null ,actEll, otherEll);
								//EllipseIntersectionCalculatorAndClipper.getClippedNucleus(actEll);
								if(isPoints != null){ 
								//	drawIntersectionLine(g, xyPoints);
									
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
	
	private void drawIntersectionLine(Graphics2D g, IntersectionPoints intersectionPoints){
		g.drawLine((int)intersectionPoints.intersectionPointsX[0],(int) intersectionPoints.intersectionPointsY[0], (int)intersectionPoints.intersectionPointsX[1],(int) intersectionPoints.intersectionPointsY[1]);
	}
	
	
	
	
	
	private long getNextCellEllipseId(){
		return this.nextId++;
	}

	
	private void drawCellPolygon(Graphics2D g, CellPolygon cell, boolean showCellAreaAndPerimeter){
		if(cell != null){
			//drawPoint(g, cell.getX(), cell.getY(), 2, Color.BLUE);
			Polygon p = new Polygon();
			
			//Vertex[] sortedVertices = cell.getSortedVerticesUsingGrahamScan();
			Vertex[] sortedVertices = cell.getSortedVertices();
		
			for(Vertex v : sortedVertices){	
				p.addPoint(v.getIntX(), v.getIntY());
				
			}
		//	g.drawString(""+ Math.round(Calculators.getCellArea(cell))*0.2 + ", " + Math.round(Calculators.getCellPerimeter(cell))*0.2, cell.getX()-10, cell.getY());
						
			Color oldColor = g.getColor();
			g.setColor(cell.getFillColor());
			g.fillPolygon(p);
			g.setColor(oldColor);
			g.drawPolygon(p);			
			
		//	for(Vertex v : cell.getVertices()){	
			//	drawVertex(g, v, false);				
		//	}
			
			//drawVertex(g,Calculators.getCellCenter(cell),false);
		}
	}
	
   public void setImportedTissueVisualizationMode(boolean importedTissueVisualizationMode) {   
   	this.importedTissueVisualizationMode = importedTissueVisualizationMode;
   }

	
   public Color getBackgroundColor() {
   
   	return backgroundColor;
   }

	
   public void setBackgroundColor(Color backgroundColor) {
   	this.setBackground(backgroundColor);
   	this.backgroundColor = backgroundColor;
   }

	
   public Color getCellColor() {
   
   	return cellColor;
   }

	
   public void setCellColor(Color cellColor) {
   
   	this.cellColor = cellColor;
   }

	
   public Color getOuterSurfaceColor() {
   
   	return outerSurfaceColor;
   }

	
   public void setOuterSurfaceColor(Color outerSurfaceColor) {
   
   	this.outerSurfaceColor = outerSurfaceColor;
   }

	
   public Color getBasementMembraneColor() {
   
   	return basementMembraneColor;
   }

	
   public void setBasementMembraneColor(Color basementMembraneColor) {
   
   	this.basementMembraneColor = basementMembraneColor;
   }

	
   public Color getCellMembraneColor() {
   
   	return cellMembraneColor;
   }

	
   public void setCellMembraneColor(Color cellMembraneColor) {
   
   	this.cellMembraneColor = cellMembraneColor;
   }

	
   public Color getCellCenterColor() {
   
   	return cellCenterColor;
   }

	
   public void setCellCenterColor(Color cellCenterColor) {
   
   	this.cellCenterColor = cellCenterColor;
   }
	
	public void resetColors(){
		backgroundColor = Color.BLACK;
		cellColor=new Color(246, 123, 123);
		outerSurfaceColor = new Color(1, 255, 0);
		basementMembraneColor = new Color(1, 255, 0);
		cellMembraneColor = new Color(218,7,0);
		cellCenterColor = Color.WHITE;
	}

	

}
