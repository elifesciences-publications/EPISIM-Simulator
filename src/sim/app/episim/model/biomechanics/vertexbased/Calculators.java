package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ec.util.MersenneTwisterFast;

import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.XYPoints;
import sim.app.episim.visualization.CellEllipse;

public abstract class Calculators {
	
	public static final int STARTX= 275;
	public static final int STARTY= 420;
	public static final int SIDELENGTH = 30;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;
	public static final double ALLOWED_DELTA = 1;
	
	
	public static final double MIN_EDGE_LENGTH =SIDELENGTH/4;
	
	private static MersenneTwisterFast rand = new MersenneTwisterFast(System.currentTimeMillis());
	
	private static final double MAX_MERGE_VERTEX_DISTANCE_FACTOR = 0.3;
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
						&&	((int)distance(cells[cellNo].getX(), cells[cellNo].getY(), vertices[rowNo][columnNo].getIntX(), vertices[rowNo][columnNo].getIntY())) <= SIDELENGTH)
						cells[cellNo].addVertex(vertices[rowNo][columnNo]);
					
				}
			}
		}	
		for(CellPolygon pol : cells) pol.setPreferredArea(Calculators.getCellArea(pol));
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
		int n = cell.getUnsortedVertices().length;
		Vertex[] vertices = cell.getSortedVertices();
		for(int i = 0; i < n; i++){
			areaTrapeze += ((vertices[(i%n)].getDoubleX() - vertices[((i+1)%n)].getDoubleX())*(vertices[(i%n)].getDoubleY() + vertices[((i+1)%n)].getDoubleY()));
		}
		return (Math.abs(areaTrapeze) / 2);
	}
	
	public static double getCellPerimeter(CellPolygon cell){
		double cellPerimeter = 0;
		int n = cell.getUnsortedVertices().length;
		Vertex[] vertices = cell.getSortedVertices();
		for(int i = 0; i < n; i++){
			cellPerimeter += distance(vertices[(i%n)].getDoubleX(), vertices[(i%n)].getDoubleY(), vertices[((i+1)%n)].getDoubleX(), vertices[((i+1)%n)].getDoubleY());
		}
		return cellPerimeter;
	}

	public static void randomlySelectCellForProliferation(CellPolygon[] cells){
		//for(Cell c :cells) c.setSelected(false);
		
		while(true){
			int cellIndex =rand.nextInt(cells.length);
	
			if(!cells[cellIndex].isProliferating()){
				cells[cellIndex].proliferate();
				return;
			}
		}
		
	}
	
	public static Vertex getCellCenter(CellPolygon cell){
		Vertex[] vertices = cell.getUnsortedVertices();
		double cumulativeX = 0, cumulativeY = 0;
		for(Vertex v : vertices){
			cumulativeX += v.getDoubleX();
			cumulativeY += v.getDoubleY();
		}
		return new Vertex(cumulativeX/vertices.length, cumulativeY/vertices.length);
	}
	
	
	public static CellPolygon divideCellPolygon(CellPolygon cell){
		//calculate the maximum distance of a cell's vertex to the cell's center vertex
		Vertex center = getCellCenter(cell);
		double maxDistance = 0;
		double actDist = 0;
		for(Vertex v: cell.getUnsortedVertices()){
			actDist= center.edist(v);
			if(actDist > maxDistance) maxDistance = actDist;
		}
		
		//calculate point with random angle on the circle with cell center as center and maxDistance as radius
		double randAngleInRadians = Math.toRadians(rand.nextInt(180));
		
		while(!isRandomAngleConventientForCellDivision(cell, center, randAngleInRadians, maxDistance)){
			randAngleInRadians = Math.toRadians(rand.nextInt(180));
		}
		
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(randAngleInRadians)), (center.getDoubleY()+maxDistance*Math.sin(randAngleInRadians)));
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVertices = cell.getSortedVertices();
		int newVerticesCounter = 0;
		for(int i = 0; i < cellVertices.length; i++){
			Vertex v_s =getIntersectionOfLinesInLineSegment(cellVertices[i], cellVertices[(i+1)%cellVertices.length], center, vOnCircle);
			if(v_s != null){
				newVerticesCounter++;
				v_s.setIsNew(true);
				if(newVerticesCounter > 2)System.out.println("Error: Found more than two new Vertices during Cell Division!");
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
						}
					}
				}
			}
		}	
		
		cellVertices = cell.getSortedVertices();
		int startIndex = getIndexOfFirstNewVertex(cellVertices);
		boolean stop = false;
		CellPolygon newCell = null;
		if(startIndex >= 0){
			newCell = new CellPolygon();
			for(int i = startIndex; !stop; i++){
				newCell.addVertex(cellVertices[i]);
				if(!cellVertices[i].isNew())cell.removeVertex(cellVertices[i]);
				if(cellVertices[i].isNew() && i != startIndex){ 
					stop = true;
					break;
				}
			}
		}
		resetIsNewStatusOfAllVertices(cellVertices);
		return newCell;
	}
	
	public static void checkForT1Transitions(CellPolygon cell){
		Vertex[] cellVertices = cell.getSortedVertices();
		for(int i = 0; i < cellVertices.length; i++){
			if(cellVertices[i].edist(cellVertices[(i+1)%cellVertices.length]) < Calculators.MIN_EDGE_LENGTH){
				doT1Transition(cellVertices[i], cellVertices[(i+1)%cellVertices.length]);
				System.out.println("Performed T1 Transition");
			}
		}
		
	}
	
	private static void doT1Transition(Vertex v1, Vertex v2){
		Vertex center = new Vertex((v1.getDoubleX()+ v2.getDoubleX())/2,(v1.getDoubleY()+ v2.getDoubleY())/2);
		
		
		//calculate direction Vector, then the Vector orthogonal to the direction Vector, then normalize this vector then, add SHORTEST_EDGE_LENGTH/2 * vector to cell center
		
		double[] vectorOthogonalToDirectionVector = new double[]{(-1*(center.getDoubleY() - v1.getDoubleY())),(center.getDoubleX() - v1.getDoubleX())};
		
		
		//normalize the vector
		double lengthOfVector = Math.sqrt((Math.pow(vectorOthogonalToDirectionVector[0],2)+Math.pow(vectorOthogonalToDirectionVector[1],2)));
		vectorOthogonalToDirectionVector[0] /=lengthOfVector;
		vectorOthogonalToDirectionVector[1] /=lengthOfVector;
				
		v1.setDoubleX((center.getDoubleX()+(Calculators.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[0]));
		v1.setDoubleY((center.getDoubleY()+(Calculators.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[1]));
		
		v2.setDoubleX((center.getDoubleX()-(Calculators.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[0]));
		v2.setDoubleY((center.getDoubleY()-(Calculators.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[1]));
		
		
		
		HashSet<CellPolygon> cellsAssociatedWithV1 = new HashSet<CellPolygon>();
		HashSet<CellPolygon> cellsAssociatedWithV2 = new HashSet<CellPolygon>();
			
		for(VertexChangeListener listener : v1.getVertexChangeListener()){ if(listener instanceof CellPolygon) cellsAssociatedWithV1.add((CellPolygon)listener); }
		for(VertexChangeListener listener : v2.getVertexChangeListener()){ if(listener instanceof CellPolygon) cellsAssociatedWithV2.add((CellPolygon)listener); }
		
		HashSet<CellPolygon> cellsConnectedToBothVertices = new HashSet<CellPolygon>();
		
		for(CellPolygon cellPol : cellsAssociatedWithV1.toArray(new CellPolygon[cellsAssociatedWithV1.size()])){
			if(cellsAssociatedWithV2.contains(cellPol)){
				//Afterwards we want to have in these sets only those cells that are connected with only one of the two vertices
				cellsAssociatedWithV1.remove(cellPol);
				cellsAssociatedWithV2.remove(cellPol);
				cellsConnectedToBothVertices.add(cellPol);
			}
		}
		
		//add vertex v1 and v2 respectively to those cells that were formerly connected with only one of the vertices
		for(CellPolygon pol :cellsAssociatedWithV1) pol.addVertex(v2);
		for(CellPolygon pol :cellsAssociatedWithV2) pol.addVertex(v1);
		
		//remove the vertex that is more far from the center of the polygon; 
		//afterwards the cells that were formerly connected to both vertices are only connected to one of the two vertices
		for(CellPolygon pol : cellsConnectedToBothVertices){
			Vertex cellCenter = Calculators.getCellCenter(pol);
			if(cellCenter.edist(v1) < cellCenter.edist(v2)) pol.removeVertex(v2);
			else pol.removeVertex(v1);
		}
		
	}
	
	
	
	
	
	
	/**
	 * This methods checks whether or not the new vertices introduced in cell division are too close to an already existing vertex
	 * @return
	 */
	private static boolean isRandomAngleConventientForCellDivision(CellPolygon cell, Vertex center, double angle, double maxDistance){
				
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(angle)), (center.getDoubleY()+maxDistance*Math.sin(angle)));
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVertices = cell.getSortedVertices();
		for(int i = 0; i < cellVertices.length; i++){
			Vertex v_s =getIntersectionOfLinesInLineSegment(cellVertices[i], cellVertices[(i+1)%cellVertices.length], center, vOnCircle);
			if(v_s != null){
				if(v_s.edist(cellVertices[i]) < Calculators.MIN_EDGE_LENGTH ||v_s.edist(cellVertices[(i+1)%cellVertices.length]) < Calculators.MIN_EDGE_LENGTH){ 
					System.out.println("Angle is not convenient for CellDivision");        
					return false;
				}
			}
		}
		return true;
	}


	private static void resetIsNewStatusOfAllVertices(Vertex[] vertices){ for(Vertex v : vertices) v.setIsNew(false);}
	
	
	private static int getIndexOfFirstNewVertex(Vertex[] vertices){
		
		for(int i = 0; i < vertices.length; i++){
			if(vertices[i].isNew()) return i;
		}
		
		return -1;
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
							if(checkMergeCondition(isps1, isps2, polygonVertex, MAX_MERGE_VERTEX_DISTANCE_FACTOR*cellEll.getMinorAxis())){
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
	
	
	public static void checkNewVertexValuesForComplianceWithStandardBorders(Vertex vertex){
		TissueBorder tissueBorder = TissueController.getInstance().getTissueBorder();
		
		double minYDelta = Double.POSITIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
				
		if(tissueBorder.lowerBound(vertex.getNewX()) < vertex.getNewY()){
			double deltaX = Math.abs(vertex.getNewX()-vertex.getDoubleX());
			double stepSize = deltaX/10d;
			if(vertex.getNewX() > vertex.getDoubleX() && stepSize>0.1){
				for(double newX = vertex.getNewX(); newX >=vertex.getDoubleX(); newX -= stepSize){
					double yDelta = Math.abs(tissueBorder.lowerBound(newX) - vertex.getDoubleY());
					if(yDelta < minYDelta){
						minYDelta = yDelta;
						minX = newX;
					}
				}
			}
			else if(vertex.getNewX() < vertex.getDoubleX() && stepSize>0.1){
				for(double newX = vertex.getNewX(); newX <=vertex.getDoubleX(); newX += stepSize){
					double yDelta = Math.abs(tissueBorder.lowerBound(newX) - vertex.getDoubleY());
					if(yDelta < minYDelta){
						minYDelta = yDelta;
						minX = newX;
					}
				}
			}
			else minX = vertex.getNewX();
			if(minX < Double.POSITIVE_INFINITY){
				vertex.setNewX(minX);
				vertex.setNewY(tissueBorder.lowerBound(minX));
			}
		}
	}
	
	
	public static void cleanCalculatedVertices(CellPolygon polygon){	
		
		Vertex[] vertices = polygon.getUnsortedVertices();
		
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
		
		Vertex[] vertices = pol.getUnsortedVertices();
		double cleanDistance = ell.getMinorAxis()*MAX_CLEAN_ESTIMATED_VERTEX_FACTOR;
		
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
		}
	}
	
	private static Polygon[] getAllPolygonObjects(CellPolygon[] cellPols){
			
		Polygon[] pols = new Polygon[cellPols.length];
		for(int i = 0; i< cellPols.length; i++){
			pols[i] = new Polygon();			
			Vertex[] sortedVertices = cellPols[i].getSortedVertices();
		
			for(Vertex v : sortedVertices){	
				pols[i].addPoint(v.getIntX(), v.getIntY());
				
			}
		}
		return pols;
	}
	
	public static void globallyCleanAllPolygonsEstimatedVertices(CellPolygon[] cellPolygons){
		Polygon[] allPolygons = getAllPolygonObjects(cellPolygons);
		
		for(int  i = 0; i < cellPolygons.length; i++){
			Vertex[] actPolygonsVertices = cellPolygons[i].getUnsortedVertices();
			for(int n = 0; n < actPolygonsVertices.length; n++){
				Vertex actVertex = actPolygonsVertices[n];
				if(actVertex != null){
					for(int m = 0; m < allPolygons.length; m++){
						if(m !=i && allPolygons[m].contains(actVertex.getDoubleX(), actVertex.getDoubleY())){
							if(!actVertex.isVertexOfCellPolygon(cellPolygons[m])){
								actVertex.delete();
								
								break;
							}
							
						}
					}
				}
			}
		}		
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
	
	
	public static void relaxVertexEstimated(Vertex v){
		if(v.getNumberOfCellsJoiningThisVertex() <= 2){
			double newX = v.getDoubleX();		
			double newY = v.getDoubleY();
			
			for(int i = 0; i < v.getNumberOfCellsJoiningThisVertex(); i++){
				CellPolygon cell = v.getCellsJoiningThisVertex()[i];
				double currentArea = cell.getCurrentArea();
				if(currentArea-cell.getPreferredArea() != 0){
					Vertex center = getCellCenter(cell);
					Vertex directionVectorNorm =  center.relToNormalized(v);
					
					boolean resultShouldBeBigger = currentArea-cell.getPreferredArea()<0;
					double percentage = Math.abs((currentArea-cell.getPreferredArea())/cell.getPreferredArea());
					double oldDistance = center.mdist(v);
					directionVectorNorm.scalarMult(percentage*oldDistance/(cell.getUnsortedVertices().length-1));
					double newDistance = center.mdist(new Vertex(v.getDoubleX()+directionVectorNorm.getDoubleX(), v.getDoubleY()+directionVectorNorm.getDoubleY()));
					if((resultShouldBeBigger && newDistance < oldDistance) || (!resultShouldBeBigger && newDistance > oldDistance)) directionVectorNorm.scalarMult(-1);					
					newX += directionVectorNorm.getDoubleX();
					newY += directionVectorNorm.getDoubleY();
				}
			}
			v.setNewX(newX);
			v.setNewY(newY);			
		}
	}
	
}
