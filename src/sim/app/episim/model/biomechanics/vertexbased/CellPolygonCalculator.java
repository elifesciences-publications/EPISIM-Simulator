package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ec.util.MersenneTwisterFast;

import sim.app.episim.model.biomechanics.vertexbased.GlobalBiomechanicalStatistics.GBSValue;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.XYPoints;
import sim.app.episim.visualization.CellEllipse;

public class CellPolygonCalculator {
	
	
	public static final int SIDELENGTH = 75;//30;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;
	
	
	
	public static final double MIN_EDGE_LENGTH =SIDELENGTH * VertexBasedMechanicalModelGlobalParameters.getInstance().getMin_edge_length_percentage();
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
	public void randomlySelectCellForApoptosis(){
		//for(Cell c :cells) c.setSelected(false);
		
		while(true){
			int cellIndex =rand.nextInt(this.cellPolygons.length);
	
			if(!this.cellPolygons[cellIndex].isDying()){
				this.cellPolygons[cellIndex].initializeApoptosis();
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
				GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T1_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T1_TRANSITION_NUMBER)+1));
			}
		}
		
	}
	
	public void checkForT2Transition(CellPolygon cell){
		Vertex[] cellVertices = cell.getSortedVertices();
		if(cellVertices.length<=3 || cell.isDying()){
			for(int i = 0; i < cellVertices.length; i++){
				if(cellVertices[i].edist(cellVertices[(i+1)%cellVertices.length]) < CellPolygonCalculator.MIN_EDGE_LENGTH || cell.getPreferredArea() <= 0){
					doT2Transition(cell);
					GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T2_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T2_TRANSITION_NUMBER)+1));
					cell.apoptosis();
					break;
				}
			}
			
		}
	}
	
	public void checkForT3Transitions(CellPolygon cell){
		if(cell != null){
			Vertex[] vertices = cell.getSortedVertices();
			for(Vertex v : vertices){
				if(v.getCellsJoiningThisVertex().length <=2){
					Line line = null;
					if((line=isCloseEnoughToOtherBoundaryForAdhesion(v, false)) != null){
						Vertex isp = line.getIntersectionPointOfLineThroughVertex(v, false, true);
						if(isp != null){
							v.setDoubleX(isp.getDoubleX());
							v.setDoubleY(isp.getDoubleY());
						}
						doT3Transition(v, line);
						GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T3_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T3_TRANSITION_NUMBER)+1));
						System.out.println("T3 Transition performed");
					}
				}
			}
		}
	}
	
	
	private void doT3Transition(Vertex adhVertex, Line line){
		Line[] linesConnectedToVertex = selectRelevantLinesConnectedToVertex(adhVertex);
		
		
		if(linesConnectedToVertex != null && linesConnectedToVertex.length >0){
			if(linesConnectedToVertex.length >2){ 
				System.err.print("ERROR: Method do T3 Transition: More than two relevant lines connected to Vertex found!");
			}
			else{
				Vertex newVertex1 = calculateNewVertex(adhVertex, linesConnectedToVertex[0], line);
				Vertex newVertex2 = calculateNewVertex(adhVertex, linesConnectedToVertex[1], line);
				
				CellPolygon adhCell = line.getCellPolygonOfLine();
				adhCell.addVertex(newVertex1);
				adhCell.addVertex(newVertex2);
				adhCell.addVertex(adhVertex);
				linesConnectedToVertex[0].getCellPolygonOfLine().addVertex(newVertex1);
				linesConnectedToVertex[1].getCellPolygonOfLine().addVertex(newVertex2);
			}
		}		
	}	
	
	private  Line[] selectRelevantLinesConnectedToVertex(Vertex adhVertex){
		ArrayList<Line> linesConnectedToVertex = new ArrayList<Line>();
		Vertex[] connectedVertices = adhVertex.getAllOtherVerticesConnectedToThisVertex();
		HashSet<CellPolygon> cellsConnectedToVertex = new HashSet<CellPolygon>();
		cellsConnectedToVertex.addAll(Arrays.asList(adhVertex.getCellsJoiningThisVertex()));
		
		//here the relevant to line connected to the Vertex v are selected, this should result in two relevant lines
		if(cellsConnectedToVertex.size() >= 2){
			for(Vertex actV: connectedVertices){
				int foundCells = 0;
				for(CellPolygon cellPol: actV.getCellsJoiningThisVertex()){
					if(cellsConnectedToVertex.contains(cellPol)) foundCells++;
				}
				if(foundCells < 2){
					linesConnectedToVertex.add(new Line(adhVertex, actV));
				}
			}
		}
		else{
			for(Vertex actV : connectedVertices){
				linesConnectedToVertex.add(new Line(adhVertex, actV));
			}
		}
		return linesConnectedToVertex.toArray(new Line[linesConnectedToVertex.size()]);
	}
	
	private Vertex calculateNewVertex(Vertex adhVertex, Line alreadyConnectedLine, Line adhLine){
		
		double intersectionAngleInDegrees = adhLine.getIntersectionAngleInDegreesWithOtherLine(alreadyConnectedLine);
		
		if(intersectionAngleInDegrees > 90){
			intersectionAngleInDegrees = 180 - intersectionAngleInDegrees;
		}
		
		Vertex vertexToRotate = adhVertex.equals(alreadyConnectedLine.getV1())? alreadyConnectedLine.getV2(): alreadyConnectedLine.getV1();
		
		double distanceVertexToRotate = adhLine.getDistanceOfVertex(vertexToRotate, false, false);
		
		Vertex newVertex = getNewRotatedVertex(adhVertex, vertexToRotate, intersectionAngleInDegrees/2);
		
		if(adhLine.getDistanceOfVertex(newVertex, false, false) > distanceVertexToRotate){
			double new_intersectionAngleInDegrees = 360 - (intersectionAngleInDegrees/2);
			newVertex = getNewRotatedVertex(adhVertex, vertexToRotate, new_intersectionAngleInDegrees);
		}
		
		double[] directionVector = new double[]{newVertex.getDoubleX()-adhVertex.getDoubleX(), newVertex.getDoubleY()-adhVertex.getDoubleY()};
		double normFact = Math.sqrt(Math.pow(directionVector[0], 2)+Math.pow(directionVector[1], 2));
		directionVector[0] /= normFact;
		directionVector[1] /= normFact;
		double lengthFactor = MIN_VERTEX_EDGE_DISTANCE / (2*Math.tan(Math.toRadians(intersectionAngleInDegrees/2)));
		newVertex= new Vertex(adhVertex.getDoubleX() + lengthFactor*directionVector[0], adhVertex.getDoubleY() + lengthFactor*directionVector[1]);
		return newVertex;
	}
	
	public static void main(String[] args){
		Line line1 = new Line(0,0,10,0);
		Line line2 = new Line(5,0,-20,10);
		System.out.println("Der Winkel: "+ line1.getIntersectionAngleInDegreesWithOtherLine(line2));
	}
	
	public void rotateVertex(Vertex centerVertex, Vertex vertexToRotate, double angleInDegrees){
		Vertex v = getNewRotatedVertex(centerVertex, vertexToRotate, angleInDegrees);
      vertexToRotate.setDoubleX(v.getDoubleX());
      vertexToRotate.setDoubleY(v.getDoubleY());        
	}
	
	
	private Vertex getNewRotatedVertex(Vertex centerVertex, Vertex vertexToRotate, double angleInDegrees){
		double angle = Math.toRadians(angleInDegrees);
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
           	      
      // rotation
      double a = vertexToRotate.getDoubleX() - centerVertex.getDoubleX();
      double b = vertexToRotate.getDoubleY() - centerVertex.getDoubleY();
      double newX = (+a * cos - b * sin + centerVertex.getDoubleX());
      double newY = (+a * sin + b * cos + centerVertex.getDoubleY());
      return new Vertex(newX, newY);
	}
		
	private void doT2Transition(CellPolygon cell){
		
		double new_X = 0;
		double new_Y = 0;
		Vertex[] vertices = cell.getUnsortedVertices();
		for(Vertex v : vertices){
			new_X += v.getDoubleX();
			new_Y += v.getDoubleY();
			v.removeVertexChangeListener(cell);
		}
		
		new_X /= vertices.length;
		new_Y /= vertices.length;
		Vertex newVertex = new Vertex(new_X, new_Y);		
		for(Vertex v : vertices){
			v.replaceVertex(newVertex);
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
	
	private Line isVertexTooCloseToAnotherCellBoundary(Vertex vertex, boolean takeNewValues){
		Line[] linesToTest =getLineArrayToTestVertexDistance(vertex);
		for(Line actLine : linesToTest){
			if(actLine.getDistanceOfVertex(vertex, takeNewValues, true) <= (MIN_VERTEX_EDGE_DISTANCE*0.8)) return actLine;
		}
		return null;
	}
	
	private Line isCloseEnoughToOtherBoundaryForAdhesion(Vertex vertex, boolean takeNewValues){
		Line[] linesToTest =getLineArrayToTestVertexDistance(vertex);
		for(Line actLine : linesToTest){
			if(actLine.getDistanceOfVertex(vertex, takeNewValues, true) <= MIN_VERTEX_EDGE_DISTANCE) return actLine;
		}
		return null;
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
	
	private void checkCloseToOtherEdge(Vertex v){
		Line line = null;
		if((line=isVertexTooCloseToAnotherCellBoundary(v, true)) !=null){
			v.setVertexColor(Color.YELLOW);
			
			line.setNewValuesOfVertexToDistance(v, (MIN_VERTEX_EDGE_DISTANCE*1.05));
			checkNewVertexValuesForComplianceWithStandardBorders(v, true);
			GlobalBiomechanicalStatistics.getInstance().set(GBSValue.VERTEX_TOO_CLOSE_TO_EDGE, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.VERTEX_TOO_CLOSE_TO_EDGE) +1));
		}
		else v.setVertexColor(Color.BLUE);
	}
	
	
	
	public void applyVertexPositionCheckPipeline(Vertex v){
		checkNewVertexValuesForComplianceWithStandardBorders(v, false);
		checkCloseToOtherEdge(v);
	}
	
	public void applyCellPolygonCheckPipeline(CellPolygon cell){
		checkForT1Transitions(cell);
		checkForT2Transition(cell);
	}
	
	
	
	private void checkNewVertexValuesForComplianceWithStandardBorders(Vertex vertex, boolean estimateNewValue){
		TissueBorder tissueBorder = TissueController.getInstance().getTissueBorder();
		
		double minYDelta = Double.POSITIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
				
		if(tissueBorder.lowerBound(vertex.getNewX()) < vertex.getNewY()){
			if(estimateNewValue){
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
				}
			}
			else resetToOldValue(vertex);			
		}
	}
	
	
	private void resetToOldValueWithRadomizedDelta(Vertex v){
		double delta =  0;//v.eDistOldAndNewValue();
		
		double[] directionVector = new double[]{v.getDoubleX()-v.getNewX(), v.getDoubleY()-v.getNewY()};
		
		double normFactor = Math.sqrt(Math.pow(directionVector[0],2)+Math.pow(directionVector[1],2));
		directionVector[0]/=normFactor;
		directionVector[1]/=normFactor;
		
		v.setNewX(v.getDoubleX() + directionVector[0]*delta);
		v.setNewY(v.getDoubleY() + directionVector[1]*delta);	
	}
	
	
	private void resetToOldValue(Vertex v){		
		v.setNewX(v.getDoubleX());
		v.setNewY(v.getDoubleY());	
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
