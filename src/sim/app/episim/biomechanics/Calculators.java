package sim.app.episim.biomechanics;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.XYPoints;
import sim.app.episim.visualization.CellEllipse;

public abstract class Calculators {
	
	public static final int STARTX = 100;
	public static final int STARTY = 100;
	public static final int SIDELENGTH = 20;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;
	public static final double ALLOWED_DELTA = 1;
	
	
	private static Random rand = new Random(100);
	
	private static final double MAX_MERGE_VERTEX_DISTANCE = 2;
	private static final double MAX_CLEAN_VERTEX_DISTANCE = 4;
	private static final double MAX_CLEAN_ESTIMATED_VERTEX_FACTOR = 0.5;
	
	
	public static CellPolygon[] getSquareVertex(int xStart, int yStart, int sidelength, int size){
		Vertex[][] vertexNetwork = new Vertex[size][size];
		for(int i = 0; i < size; i++){
			Vertex[] vertices = new Vertex[size];
			for(int n = 0; n < size; n++){
				vertices[n] = new Vertex((xStart+n*sidelength),(yStart+i*sidelength));
			}
			vertexNetwork[i] = vertices;
		}
		
		CellPolygon[] polygons = new CellPolygon[(int)Math.pow(size-1, 2)];
		
		int polygonNumber = 0;
		for(int i = 0; i < (size-1); i++){
			for(int n = 0; n < (size-1); n++){
				CellPolygon p = new CellPolygon();
				p.addVertex(vertexNetwork[i][n]);
				p.addVertex(vertexNetwork[i][n+1]);
				p.addVertex(vertexNetwork[i+1][n]);
				p.addVertex(vertexNetwork[i+1][n+1]);
				polygons[polygonNumber++] = p;
				p.setPreferredArea(Math.pow(sidelength, 2));
			}
		}
		
		return polygons;
	}

	
	
	public static CellPolygon[] getStandardCellArray(int rows, int columns){
		int height = Math.round((float) Math.sqrt(Math.pow(SIDELENGTH, 2)-Math.pow(SIDELENGTH/2, 2)));
		
		CellPolygon[] cells = getCells(rows, columns, height);
		Vertex[][] vertices = getVertices(rows, columns, height);
		
		for(int rowNo = 0; rowNo < vertices.length; rowNo++){
			for(int columnNo = 0; columnNo < vertices[rowNo].length; columnNo++){
				for(int cellNo = 0; cellNo <  cells.length; cellNo++){
					if(vertices[rowNo][columnNo] != null && cells[cellNo] != null
						&&	distance(cells[cellNo].getX(), cells[cellNo].getY(), vertices[rowNo][columnNo].getIntX(), vertices[rowNo][columnNo].getIntY()) <= SIDELENGTH)
						cells[cellNo].addVertex(vertices[rowNo][columnNo]);
				}
			}
		}	
		
		return cells;		
	}
	
	
	
	private static double distance(double x1, double y1, double x2, double y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	private static Vertex[][] getVertices(int rows, int columns, int height){
		
		Vertex[][] vertices = new Vertex[2*(rows+1)][(columns+1)];
		
		int startX = STARTX;
		int startY = STARTY - SIDELENGTH;
		
		//first row Vertices
		for(int i = 0; i < columns; i++) vertices[0][i] = new Vertex(startX + i*2*height, startY);
		
		// in between two rows of vertices with the same startX are calculated
		for(int i = 1;  i <= rows; i++){
			if((i%2)!= 0) startX -=height;
			else startX += height;
			
			startY += SIDELENGTHHALF;
			for(int n = 0; n <= columns; n++) vertices[((2*i)-1)][n] = new Vertex(startX+ n*2*height, startY);
			startY += SIDELENGTH;
			for(int n = 0; n <= columns; n++) vertices[(2*i)][n] = new Vertex(startX+ n*2*height, startY);
		}
		
		
		//last row Vertices
		startY += SIDELENGTHHALF;
		startX += height;
		for(int i = 0; i < columns; i++) vertices[vertices.length-1][i] = new Vertex(startX + i*2*height, startY);
		
		return vertices;
	}	
	
	private static CellPolygon[] getCells(int rows, int columns, int height){
		CellPolygon[] cells = new CellPolygon[rows*columns];
		int cellIndex = 0;
		for(int i = 0; i < rows; i++){
			for(int n = 0; n < columns; n++){
				if(((i+1)%2) == 1)cells[cellIndex++] = new CellPolygon(STARTX + 2*n*height, STARTY + i*(SIDELENGTH+SIDELENGTHHALF));
				else cells[cellIndex++] = new CellPolygon(STARTX + (2*n + 1)*height, STARTY + i*(SIDELENGTH+SIDELENGTHHALF));
			}
		}
		return cells;
	}
	
	public static double getCellArea(CellPolygon cell){
		double areaTrapeze = 0;
		int n = cell.getVertices().length;
		Vertex[] vertices = cell.getSortedVerticesUsingTravellingSalesmanSimulatedAnnealing();
		for(int i = 0; i < n; i++){
			areaTrapeze += ((vertices[(i%n)].getDoubleX() - vertices[((i+1)%n)].getDoubleX())*(vertices[(i%n)].getDoubleY() + vertices[((i+1)%n)].getDoubleY()));
		}
		
		return (Math.abs(areaTrapeze) / 2);
	}
	
	public static double getCellPerimeter(CellPolygon cell){
		double cellPerimeter = 0;
		int n = cell.getVertices().length;
		Vertex[] vertices = cell.getVertices();
		for(int i = 0; i < n; i++){
			cellPerimeter += distance(vertices[(i%n)].getDoubleX(), vertices[(i%n)].getDoubleY(), vertices[((i+1)%n)].getDoubleX(), vertices[((i+1)%n)].getDoubleY());
		}
		return cellPerimeter;
	}

	public static void randomlySelectCell(CellPolygon[] cells){
		//for(Cell c :cells) c.setSelected(false);
		
		int cellIndex =rand.nextInt(cells.length);
		if(!cells[cellIndex].isSelected()){
			addNewVertices(cells[cellIndex]);
			cells[cellIndex].setSelected(true);
		}
		
	}
	
	public static Vertex getCellCenter(CellPolygon cell){
		Vertex[] vertices = cell.getVertices();
		double cumulativeX = 0, cumulativeY = 0;
		for(Vertex v : vertices){
			cumulativeX += v.getDoubleX();
			cumulativeY += v.getDoubleY();
		}
		return new Vertex(cumulativeX/vertices.length, cumulativeY/vertices.length);
	}
	
	
	public static void addNewVertices(CellPolygon cell){
		//calculate the maximum distance of a cell's vertex to the cell's center vertex
		Vertex center = getCellCenter(cell);
		double maxDistance = 0;
		double actDist = 0;
		for(Vertex v: cell.getVertices()){
			actDist= center.edist(v);
			if(actDist > maxDistance) maxDistance = actDist;
		}
		
		//calculate point with random angle on the circle with cell center as center and maxDistance as radius
		double randAngleInRadians = Math.toRadians(rand.nextInt(180));
		
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(randAngleInRadians)), (center.getDoubleY()+maxDistance*Math.sin(randAngleInRadians)));
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVertices = cell.getVertices();
		for(int i = 0; i < cellVertices.length; i++){
			Vertex v_s =getIntersectionOfLinesInLineSegment(cellVertices[i], cellVertices[(i+1)%cellVertices.length], center, vOnCircle);
			if(v_s != null){ 
				v_s.isNew = true;
				
				HashSet<Integer> foundCellIdsFirstVertex = new HashSet<Integer>();
				for(VertexChangeListener listener: cellVertices[i].getVertexChangeListener()){
					if(listener instanceof CellPolygon){
						foundCellIdsFirstVertex.add(((CellPolygon) listener).getId());
					}
				}
				for(VertexChangeListener listener: cellVertices[(i+1)%cellVertices.length].getVertexChangeListener()){
					if(listener instanceof CellPolygon){
						CellPolygon actCell = (CellPolygon) listener;
						if(foundCellIdsFirstVertex.contains(actCell.getId())){ 
							actCell.addVertex(v_s);
							actCell.sortVerticesWithGrahamScan();
						}
					}
				}
			}
		}
		
	}
	/**
	 * @param v1 first point line one (first cell vertex)
	 * @param v2 second point line one (second cellvertex)
	 * @param v3 first point line two (cell center)
	 * @param v4 second point line two( point with max distance on circle)
	 * @return intersection point, returns null if there is no intersection
	 */
	public static Vertex getIntersectionOfLinesInLineSegment(Vertex v1, Vertex v2, Vertex v3, Vertex v4){
		
		double denominator =  ((v4.getDoubleY() -v3.getDoubleY())*(v2.getDoubleX()-v1.getDoubleX())) - ((v4.getDoubleX()-v3.getDoubleX())*(v2.getDoubleY()-v1.getDoubleY()));
		
		if(denominator != 0){
			double u_a = (((v4.getDoubleX()-v3.getDoubleX())*(v1.getDoubleY()-v3.getDoubleY()))-((v4.getDoubleY()-v3.getDoubleY())*(v1.getDoubleX()-v3.getDoubleX()))) / denominator;			
			
			//only if u_a is between 0 and 1  the intersection point lies on the line segment described by the two cell vertices v1 and v2
			if(u_a >= 0 && u_a <= 1){
				double x_s = v1.getDoubleX() + u_a*(v2.getDoubleX()-v1.getDoubleX());
				double y_s = v1.getDoubleY() + u_a*(v2.getDoubleY()-v1.getDoubleY());
				return new Vertex(x_s, y_s);
			}
		}
		
		return null;
	}
	
	
	/**
	 * @param v1 first point line one (first cell vertex)
	 * @param v2 second point line one (second cell vertex)
	 * @param v3 first point line two (cell center)
	 * @param v4 second point line two(point with max distance on circle)
	 * @return intersection point, returns null if there is no intersection
	 */
	public static Vertex getIntersectionOfLines(Vertex v1, Vertex v2, Vertex v3, Vertex v4){
		
		double denominator =  ((v4.getDoubleY() -v3.getDoubleY())*(v2.getDoubleX()-v1.getDoubleX())) - ((v4.getDoubleX()-v3.getDoubleX())*(v2.getDoubleY()-v1.getDoubleY()));
		
		if(denominator != 0){
			double u_a = (((v4.getDoubleX()-v3.getDoubleX())*(v1.getDoubleY()-v3.getDoubleY()))-((v4.getDoubleY()-v3.getDoubleY())*(v1.getDoubleX()-v3.getDoubleX()))) / denominator;		
			double x_s = v1.getDoubleX() + u_a*(v2.getDoubleX()-v1.getDoubleX());
			double y_s = v1.getDoubleY() + u_a*(v2.getDoubleY()-v1.getDoubleY());
			return new Vertex(x_s, y_s);
			
		}
		
		return null;
	}
	
	public static void calculateCellPolygons(CellEllipse cellEll){
		CellPolygon cellPol_1 = null, cellPol_2 = null, cellPol_3 = null;
		if(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(cellEll.getId()) == null){
			cellPol_1 = new CellPolygon();
			CellEllipseIntersectionCalculationRegistry.getInstance().registerCellPolygonByCellEllipseId(cellEll.getId(), cellPol_1);
		}
		else cellPol_1 = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(cellEll.getId());
		Map<String, XYPoints> xyPoints = cellEll.getAllXYPointsOfEllipse();
		Set<String> alreadyCalculatedCouples = new HashSet<String>();
		
		Area clippedEll =cellEll.getClippedEllipse();
		if(xyPoints != null && !xyPoints.isEmpty()){
			for(String ellId1 : xyPoints.keySet()){
				for(String ellId2: xyPoints.keySet()){
					
				if(!ellId1.equals(ellId2) && !alreadyCalculatedCouples.contains(ellId1+ellId2)){
					
					alreadyCalculatedCouples.add(ellId1+ellId2);
					alreadyCalculatedCouples.add(ellId2+ellId1);
					Vertex[] isps1 = xyPoints.get(ellId1).intersectionPoints;
					Vertex[] isps2 = xyPoints.get(ellId2).intersectionPoints;
					
						long idOtherEll1 = Long.parseLong(ellId1.split(""+CellEllipse.SEPARATORCHAR)[1]);
						long idOtherEll2 = Long.parseLong(ellId2.split(""+CellEllipse.SEPARATORCHAR)[1]);
						
						if(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(idOtherEll1) == null){
							cellPol_2 = new CellPolygon();
							CellEllipseIntersectionCalculationRegistry.getInstance().registerCellPolygonByCellEllipseId(idOtherEll1, cellPol_2);
						}
						else cellPol_2 = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(idOtherEll1);
						
						if(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(idOtherEll2) == null){
							cellPol_3 = new CellPolygon();
							CellEllipseIntersectionCalculationRegistry.getInstance().registerCellPolygonByCellEllipseId(idOtherEll2, cellPol_3);
						}
						else cellPol_3 = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(idOtherEll2);					
					
						if(contains(cellEll, isps1[0].getDoubleX(), isps1[0].getDoubleY())){
							cellPol_1.addVertex(isps1[0]);
							cellPol_2.addVertex(isps1[0]);						
						}
						if(contains(cellEll, isps1[1].getDoubleX(), isps1[1].getDoubleY()) && !isZeroVertex(isps1[1])){
							cellPol_1.addVertex(isps1[1]);
							cellPol_2.addVertex(isps1[1]);
						}
						if(contains(cellEll, isps2[0].getDoubleX(), isps2[0].getDoubleY())){
							cellPol_1.addVertex(isps2[0]);
							cellPol_3.addVertex(isps2[0]);
						}
						if(contains(cellEll, isps2[1].getDoubleX(), isps2[1].getDoubleY())&& !isZeroVertex(isps2[1])){
							cellPol_1.addVertex(isps2[1]);
							cellPol_3.addVertex(isps2[1]);
						}
									
						Vertex polygonVertex = getIntersectionOfLines(isps1[0], isps1[1], isps2[0], isps2[1]);
							
						if(polygonVertex != null){
							if(checkMergeCondition(isps1, isps2, polygonVertex, MAX_MERGE_VERTEX_DISTANCE)){
								polygonVertex.setMergeVertex(true);
								cellPol_1.addVertex(polygonVertex);
								cellPol_2.addVertex(polygonVertex);
								cellPol_3.addVertex(polygonVertex);
							}						
						}					
					}					
				}
				
			}
			}
			if(xyPoints.keySet().size() == 1){
				for(String id : xyPoints.keySet()){
					
					Vertex[] isps = xyPoints.get(id).intersectionPoints;
					if(!isps[0].isWasDeleted() && !isps[1].isWasDeleted()){
						cellPol_1.addVertex(isps[0]);
						if(!isZeroVertex(isps[1]))cellPol_1.addVertex(isps[1]);
						
						long idOtherEll1 = Long.parseLong(id.split(""+CellEllipse.SEPARATORCHAR)[1]);
						if(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(idOtherEll1) == null){
							cellPol_2 = new CellPolygon();
							CellEllipseIntersectionCalculationRegistry.getInstance().registerCellPolygonByCellEllipseId(idOtherEll1, cellPol_2);
						}
						else cellPol_2 = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(idOtherEll1);
						
						cellPol_2.addVertex(isps[0]);
						if(!isZeroVertex(isps[1]))	cellPol_2.addVertex(isps[1]);
					}
				}
			}
			//cleanCellPolygonVertices(cellPol_1);
			
		
		
		
		
	}
	
	
	private static boolean isZeroVertex(Vertex v){
		return v.getDoubleX() == 0 && v.getDoubleY() == 0;
	}
	
	public static void calculateEstimatedVertices(CellEllipse ellipse){
		double stepsize = Math.PI / 4;
		double two_pi = 2 * Math.PI;
	
		CellPolygon cellPol = null;
		if(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(ellipse.getId()) == null){
			cellPol = new CellPolygon();
			CellEllipseIntersectionCalculationRegistry.getInstance().registerCellPolygonByCellEllipseId(ellipse.getId(), cellPol);
		}
		else cellPol = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(ellipse.getId());
		double[] newPoint = null;
		for(double i = 0; i < two_pi; i += stepsize){
			 newPoint = EllipseIntersectionCalculatorAndClipper.calculatePointOnEllipse(ellipse.getX(), ellipse.getY(), ellipse.getMajorAxis()/2, ellipse.getMinorAxis()/2, i, ellipse.getOrientationInRadians());
			 
			 if(contains(ellipse, newPoint[0], newPoint[1])){
				
				 Vertex v = new Vertex(newPoint[0], newPoint[1]);
				 v.setEstimatedVertex(true);
				 cellPol.addVertex(v);
			 }
		}
		cleanEstimatedVertices(ellipse);
	}
	
	private static boolean contains(CellEllipse ellipse, double x, double y){
		if(ellipse.getClippedEllipse().getBounds().contains(x-ALLOWED_DELTA, y-ALLOWED_DELTA)
				 || ellipse.getClippedEllipse().getBounds().contains(x-ALLOWED_DELTA, y+ALLOWED_DELTA)
				 || ellipse.getClippedEllipse().getBounds().contains(x+ALLOWED_DELTA, y-ALLOWED_DELTA)
				 || ellipse.getClippedEllipse().getBounds().contains(x+ALLOWED_DELTA, y+ALLOWED_DELTA)){
			return true;
		}
		return false;
	}
	
	
	
	public static void cleanCalculatedVertices(CellPolygon polygon){
		
		
	
		
		Vertex[] vertices = polygon.getVertices();
		
		for(int i = 0; i < vertices.length; i++){
			for(int n = 0; n < vertices.length; n++){
				if(n != i){
					if(vertices[i] != null && vertices[n] != null && !vertices[i].isWasDeleted() && !vertices[n].isWasDeleted()){
						if(vertices[i].edist(vertices[n])<= MAX_CLEAN_VERTEX_DISTANCE){
							if((vertices[i].isMergeVertex() && !vertices[n].isMergeVertex())
									|| (!vertices[i].isMergeVertex() == !vertices[n].isMergeVertex())){
								
							if(((vertices[i].isMergeVertex() == vertices[n].isMergeVertex())&&vertices[i].getId()< vertices[n].getId()) 
									||(vertices[i].isMergeVertex() && !vertices[n].isMergeVertex())){
								vertices[n].replaceVertex(vertices[i]);
								vertices[n]=null;
							}
							else{
									vertices[i].replaceVertex(vertices[n]);
									vertices[i]=null;
							}
							}
							else if(!vertices[i].isMergeVertex() && vertices[n].isMergeVertex()){
								vertices[i].replaceVertex(vertices[n]);
								vertices[i]=null;
							}
						}
						
					}
				}
			}
		}
	}
	
	
	private static void cleanEstimatedVertices(CellEllipse ell){
		
		CellPolygon pol = CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(ell.getId());
		
		Vertex[] vertices = pol.getVertices();
		double cleanDistance = ell.getMinorAxis()*MAX_CLEAN_ESTIMATED_VERTEX_FACTOR;
		Polygon[] neighbourPolygons = getNeighbourPolygons(pol);
		for(int i = 0; i < vertices.length; i++){
			if(vertices[i] != null && !vertices[i].isEstimatedVertex()){
				for(int n = 0; n < vertices.length; n++){
					if(n != i){
						if(vertices[i] != null && vertices[n] != null && !vertices[i].isWasDeleted() && !vertices[n].isWasDeleted() && vertices[n].isEstimatedVertex()){
							if(vertices[i].edist(vertices[n])<= cleanDistance){								
									vertices[n].delete();
									vertices[n]=null;								
							}			
						}
					}
				}
			}
			else if(neighbourPolygons != null &&vertices[i] != null && vertices[i].isEstimatedVertex()){
				for(int m = 0; m < neighbourPolygons.length; m++){
					if(neighbourPolygons[m] != null && neighbourPolygons[m].contains(vertices[i].getDoubleX(), vertices[i].getDoubleY())){
						vertices[i].delete();
						vertices[i]=null;
						
						break;
						
					}
				}
			}
		}
	}
	
	private static Polygon[] getNeighbourPolygons(CellPolygon cellPol){
		CellPolygon[] neighbourCellPols = cellPol.getNeighbourPolygons();	
		Polygon[] pols = new Polygon[neighbourCellPols.length];
		for(int i = 0; i< neighbourCellPols.length; i++){
			pols[i] = new Polygon();			
			Vertex[] sortedVertices = neighbourCellPols[i].getSortedVerticesUsingTravellingSalesmanSimulatedAnnealing();
		
			for(Vertex v : sortedVertices){	
				pols[i].addPoint(v.getIntX(), v.getIntY());
				
			}
		}
		return pols;
	}
	
	
	private static boolean checkMergeCondition(Vertex[] isps1, Vertex[] isps2, Vertex mergePoint, double maxDistance){
		double minDistance1 = Double.POSITIVE_INFINITY;
		double minDistance2 = Double.POSITIVE_INFINITY;
		Vertex minVertex1 = null;
		Vertex minVertex2 = null;
		
		Vertex[] allIsps = new Vertex[4];
		allIsps[0] = isps1[0];
		allIsps[1] = isps1[1];
		allIsps[2] = isps2[0];
		allIsps[3] = isps2[1];
		double actDist = 0;
		for(int i = 0; i<allIsps.length; i++){
			actDist = allIsps[i].edist(mergePoint);
			if(actDist < minDistance1){
				minDistance2 = minDistance1;
				minVertex2 = minVertex1;
				minDistance1 = actDist;
				minVertex1 = allIsps[i];
			}
			else if(actDist < minDistance2){ 
				minDistance2 = actDist;
				minVertex2 = allIsps[i];
			}
		}
		if(minDistance1 <= maxDistance && minDistance2 <= maxDistance){
			minVertex1.delete();
			minVertex2.delete();
			return true;
		}
		
		return false;
	}
	
	private static Vertex[] getVerticesWithMaxDistance(Vertex[] isps1, Vertex[] isps2, Vertex mergePoint){
		double maxDistance1 = Double.NEGATIVE_INFINITY;
		double maxDistance2 = Double.NEGATIVE_INFINITY;
		Vertex maxVertex1 = null;
		Vertex maxVertex2 = null;
		
		Vertex[] allIsps = new Vertex[4];
		allIsps[0] = isps1[0];
		allIsps[1] = isps1[1];
		allIsps[2] = isps2[0];
		allIsps[3] = isps2[1];
		
		double actDist = 0;
		for(int i = 0; i<allIsps.length; i++){
			actDist = allIsps[i].edist(mergePoint);
			if(actDist > maxDistance1){
				maxDistance2 = maxDistance1;
				maxVertex2 = maxVertex1;
				maxDistance1 = actDist;
				maxVertex1 = allIsps[i];
			}
			else if(actDist > maxDistance2){ 
				maxDistance2 = actDist;
				maxVertex2 = allIsps[i];
			}
		}
		return new Vertex[]{maxVertex1, maxVertex2};
	}
	
	
}
