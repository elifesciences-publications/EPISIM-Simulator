package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
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

public class CellPolygonCalculator {
	
	
	public static final int SIDELENGTH = 30;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;
	
	
	
	public static final double MIN_EDGE_LENGTH =SIDELENGTH/4;
	public static final double MIN_VERTEX_EDGE_DISTANCE = MIN_EDGE_LENGTH;
	
	private MersenneTwisterFast rand = new MersenneTwisterFast(System.currentTimeMillis());
	
	private CellPolygon[] cellPolygons;
	
	public CellPolygonCalculator(CellPolygon[] cellPolygons){
		if(cellPolygons == null) throw new IllegalArgumentException("Cell Polygon Array must not be null");
		this.cellPolygons = cellPolygons;
	}
	
	public void setCellPolygons(CellPolygon[] cellPolygons){
		if(cellPolygons != null) this.cellPolygons = cellPolygons;
	}
	
	public double getCellArea(CellPolygon cell){
		double areaTrapeze = 0;
		int n = cell.getUnsortedVertices().length;
		Vertex[] vertices = cell.getSortedVertices();
		for(int i = 0; i < n; i++){
			areaTrapeze += ((vertices[(i%n)].getDoubleX() - vertices[((i+1)%n)].getDoubleX())*(vertices[(i%n)].getDoubleY() + vertices[((i+1)%n)].getDoubleY()));
		}
		return (Math.abs(areaTrapeze) / 2);
	}
	
	public double getCellPerimeter(CellPolygon cell){
		double cellPerimeter = 0;
		int n = cell.getUnsortedVertices().length;
		Vertex[] vertices = cell.getSortedVertices();
		for(int i = 0; i < n; i++){
			cellPerimeter += vertices[(i%n)].edist(vertices[((i+1)%n)]);
		}
		return cellPerimeter;
	}

	public void randomlySelectCellForProliferation(){
		//for(Cell c :cells) c.setSelected(false);
		
		while(true){
			int cellIndex =rand.nextInt(this.cellPolygons.length);
	
			if(!this.cellPolygons[cellIndex].isProliferating()){
				this.cellPolygons[cellIndex].proliferate();
				return;
			}
		}
		
	}
	
	public Vertex getCellCenter(CellPolygon cell){
		Vertex[] vertices = cell.getUnsortedVertices();
		double cumulativeX = 0, cumulativeY = 0;
		for(Vertex v : vertices){
			cumulativeX += v.getDoubleX();
			cumulativeY += v.getDoubleY();
		}
		return new Vertex(cumulativeX/vertices.length, cumulativeY/vertices.length);
	}
	
	
	public CellPolygon divideCellPolygon(CellPolygon cell){
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
			Vertex v_s =(new Line(cellVertices[i], cellVertices[(i+1)%cellVertices.length])).getIntersectionOfLinesInLineSegment(new Line(center, vOnCircle));
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
			newCell = new CellPolygon(this);
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
	
	public void checkForT1Transitions(CellPolygon cell){
		Vertex[] cellVertices = cell.getSortedVertices();
		for(int i = 0; i < cellVertices.length; i++){
			if(cellVertices[i].edist(cellVertices[(i+1)%cellVertices.length]) < CellPolygonCalculator.MIN_EDGE_LENGTH){
				doT1Transition(cellVertices[i], cellVertices[(i+1)%cellVertices.length]);
				System.out.println("Performed T1 Transition");
			}
		}
		
	}
	
	private void doT1Transition(Vertex v1, Vertex v2){
		Vertex center = new Vertex((v1.getDoubleX()+ v2.getDoubleX())/2,(v1.getDoubleY()+ v2.getDoubleY())/2);
		
		
		//calculate direction Vector, then the Vector orthogonal to the direction Vector, then normalize this vector then, add SHORTEST_EDGE_LENGTH/2 * vector to cell center
		
		double[] vectorOthogonalToDirectionVector = new double[]{(-1*(center.getDoubleY() - v1.getDoubleY())),(center.getDoubleX() - v1.getDoubleX())};
		
		
		//normalize the vector
		double lengthOfVector = Math.sqrt((Math.pow(vectorOthogonalToDirectionVector[0],2)+Math.pow(vectorOthogonalToDirectionVector[1],2)));
		vectorOthogonalToDirectionVector[0] /=lengthOfVector;
		vectorOthogonalToDirectionVector[1] /=lengthOfVector;
				
		v1.setDoubleX((center.getDoubleX()+(CellPolygonCalculator.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[0]));
		v1.setDoubleY((center.getDoubleY()+(CellPolygonCalculator.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[1]));
		
		v2.setDoubleX((center.getDoubleX()-(CellPolygonCalculator.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[0]));
		v2.setDoubleY((center.getDoubleY()-(CellPolygonCalculator.MIN_EDGE_LENGTH/2)*vectorOthogonalToDirectionVector[1]));
		
		
		
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
			Vertex cellCenter = getCellCenter(pol);
			if(cellCenter.edist(v1) < cellCenter.edist(v2)) pol.removeVertex(v2);
			else pol.removeVertex(v1);
		}
		
	}	
	
	/**
	 * This methods checks whether or not the new vertices introduced in cell division are too close to an already existing vertex
	 * @return
	 */
	private boolean isRandomAngleConventientForCellDivision(CellPolygon cell, Vertex center, double angle, double maxDistance){
				
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(angle)), (center.getDoubleY()+maxDistance*Math.sin(angle)));
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVertices = cell.getSortedVertices();
		for(int i = 0; i < cellVertices.length; i++){
			Vertex v_s =(new Line(cellVertices[i], cellVertices[(i+1)%cellVertices.length])).getIntersectionOfLinesInLineSegment(new Line(center, vOnCircle));
			if(v_s != null){
				if(v_s.edist(cellVertices[i]) < CellPolygonCalculator.MIN_EDGE_LENGTH ||v_s.edist(cellVertices[(i+1)%cellVertices.length]) < CellPolygonCalculator.MIN_EDGE_LENGTH){ 
					return false;
				}
			}
		}
		return true;
	}


	private void resetIsNewStatusOfAllVertices(Vertex[] vertices){ for(Vertex v : vertices) v.setIsNew(false);}
	
	
	private int getIndexOfFirstNewVertex(Vertex[] vertices){
		
		for(int i = 0; i < vertices.length; i++){
			if(vertices[i].isNew()) return i;
		}
		
		return -1;
	}
	
	public boolean isVertexTooCloseToAnotherCellBoundary(Vertex vertex){
		Line[] linesToTest =getLineArrayToTestVertexDistance(vertex);
		for(Line actLine : linesToTest){
			if(actLine.getDistanceOfVertex(vertex) <= MIN_VERTEX_EDGE_DISTANCE) return true;
		}
		return false;
	}
	
	private Line[] getLineArrayToTestVertexDistance(Vertex vertex){
		ArrayList<Line> lines = new ArrayList<Line>();
		for(CellPolygon cell : cellPolygons){
			Vertex[] cellVertices = cell.getSortedVertices();
			Line actLine = null;
			for(int i = 0; i < cellVertices.length;i++){
				actLine = new Line(cellVertices[i], cellVertices[((i+1)%cellVertices.length)]);
				if(!actLine.belongsVertexToLine(vertex)) lines.add(actLine);
			}
		}
		return lines.toArray(new Line[lines.size()]);
	}
	
	
	public void checkNewVertexValuesForComplianceWithStandardBorders(Vertex vertex){
		TissueBorder tissueBorder = TissueController.getInstance().getTissueBorder();
		
		double minYDelta = Double.POSITIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
				
		if(tissueBorder.lowerBound(vertex.getNewX()) < vertex.getNewY()){
			/*
			 //the method implemented below induces in some cases an upward movement out of the undulation of the basement membrane
			 //for this reason this method is not used; the new values are simply set to the old values
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
			}*/
			vertex.setNewX(vertex.getDoubleX());
			vertex.setNewY(vertex.getDoubleY());
		}
	}	
	
	public void relaxVertexEstimated(Vertex v){
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
