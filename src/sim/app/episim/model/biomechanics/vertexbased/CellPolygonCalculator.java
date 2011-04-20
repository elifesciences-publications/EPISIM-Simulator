package sim.app.episim.model.biomechanics.vertexbased;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import ec.util.MersenneTwisterFast;

import sim.app.episim.model.biomechanics.vertexbased.GlobalBiomechanicalStatistics.GBSValue;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;


public class CellPolygonCalculator {
	
	
	public static final int SIDELENGTH = 30;//75;//30;
	public static final int SIDELENGTHHALF = SIDELENGTH/2;	
	private double min_edge_length;
	private double min_basallayer_distance;
	private double min_vertex_edge_distance;
	

	
	private MersenneTwisterFast rand = new MersenneTwisterFast(System.currentTimeMillis());
	
	private CellPolygon[] cellPolygons;	
	
	protected CellPolygonCalculator(){
		this.cellPolygons = new CellPolygon[0];
		VertexBasedMechanicalModelGlobalParameters globalParameters = (VertexBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		min_edge_length = SIDELENGTH * globalParameters.getMin_edge_length_percentage();
		min_basallayer_distance = SIDELENGTH * globalParameters.getMin_dist_percentage_basal_adhesion();
		min_vertex_edge_distance = min_edge_length * 0.8;
	}
	
	protected void setCellPolygons(CellPolygon[] cellPolygons){
		this.cellPolygons = cellPolygons;
	}
	
	public double getCellArea(CellPolygon cell){
		double areaTrapeze = 0;
		int n = cell.getUnsortedVertices().length;
		Vertex[] vertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayFirstVertexReferenceSigned(cell.getSortedVertices());
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
		while(true){
			
			int cellIndex =rand.nextInt(this.cellPolygons.length);
			
			if(!this.cellPolygons[cellIndex].isProliferating() && 
					((this.cellPolygons[cellIndex].hasContactToBasalLayer() || this.cellPolygons[cellIndex].hasContactToCellThatIsAttachedToBasalLayer())|| !TestVisualizationBiomechanics.LOAD_STANDARD_MEMBRANE)){
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
		Vertex[] vertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getUnsortedVertices());
		double cumulativeX = 0, cumulativeY = 0;
		for(Vertex v : vertices){
			cumulativeX += v.getDoubleX();
			cumulativeY += v.getDoubleY();
		}
		return new Vertex(cumulativeX/vertices.length, cumulativeY/vertices.length, false);
	}
	
	
	public CellPolygon divideCellPolygon(CellPolygon cell){
		//calculate the maximum distance of a cell's vertex to the cell's center vertex
		Vertex center = getCellCenter(cell);
		double maxDistance = 0;
		double actDist = 0;
		for(Vertex v:cell.getUnsortedVertices()){
			actDist= center.edist(v);
			if(actDist > maxDistance) maxDistance = actDist;
		}
		
		//calculate point with random angle on the circle with cell center as center and maxDistance as radius
		double randAngleInRadians = 0;
		
		//Look for new Cell Border for Cell division with minimal length 
		HashMap<Integer, Vertex[]> angleVertexMap = new HashMap<Integer, Vertex[]>();
		for(int n = 0; n < 180; n++){
			randAngleInRadians = Math.toRadians(n);
			Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(randAngleInRadians)), (center.getDoubleY()+maxDistance*Math.sin(randAngleInRadians)), false);
			int newVerticesCounter = 0;
			Vertex[] cellVertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getSortedVertices());
			Vertex[] newVertices = new Vertex[2];
			for(int i = 0; i < cellVertices.length; i++){
				Vertex v_s =(new Line(cellVertices[i], cellVertices[(i+1)%cellVertices.length])).getIntersectionOfLinesInLineSegment(new Line(center, vOnCircle));
				if(v_s != null){
					if(newVerticesCounter < 2){
						newVertices[newVerticesCounter] = v_s;
						newVerticesCounter++;
					}					
				}
			}
			angleVertexMap.put(n, newVertices);
		}
		
		double minDistance = Double.POSITIVE_INFINITY;
		int minAngle = 0;
		for(int actAngle : angleVertexMap.keySet()){
			if(isAngleConventientForCellDivision(cell, center, Math.toRadians(actAngle), maxDistance)){
				Vertex[] v = angleVertexMap.get(actAngle);
				double actDistance = angleVertexMap.get(actAngle)[0].edist(angleVertexMap.get(actAngle)[1]);
				if(actDistance < minDistance){
					minDistance = actDistance;
					minAngle = actAngle;
				}
			}
		}	
		
		/*while(!isRandomAngleConventientForCellDivision(cell, center, randAngleInRadians, maxDistance)){
			randAngleInRadians = Math.toRadians(rand.nextInt(180));
		}*/
		randAngleInRadians = Math.toRadians(minAngle);
		
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(randAngleInRadians)), (center.getDoubleY()+maxDistance*Math.sin(randAngleInRadians)), false);
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVerticesTransformed = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getSortedVertices());
		Vertex[] cellVerticesNotTransformed = cell.getSortedVertices();
		int newVerticesCounter = 0;
		for(int i = 0; i < cellVerticesTransformed.length; i++){
			Vertex v_s =(new Line(cellVerticesTransformed[i], cellVerticesTransformed[(i+1)%cellVerticesTransformed.length])).getIntersectionOfLinesInLineSegment(new Line(center, vOnCircle));
			if(v_s != null){
				newVerticesCounter++;
				v_s.setIsNew(true);
				if(newVerticesCounter > 2)System.out.println("Error: Found more than two new Vertices during Cell Division!");
				HashSet<Integer> foundCellIdsFirstVertex = new HashSet<Integer>();
				for(CellPolygon cellPol: cellVerticesNotTransformed[i].getCellsJoiningThisVertex()){				
						foundCellIdsFirstVertex.add(((CellPolygon) cellPol).getId());					
				}
				for(CellPolygon cellPol: cellVerticesNotTransformed[(i+1)%cellVerticesTransformed.length].getCellsJoiningThisVertex()){
					if(foundCellIdsFirstVertex.contains(cellPol.getId())){ 
						cellPol.addVertex(v_s);							
					}				
				}
			}
		}	
		
		Vertex[] cellVertices = cell.getSortedVertices();
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
	
	public void checkForT1Transitions(CellPolygon cell){
		Vertex[] cellVertices = cell.getSortedVertices();
		for(int i = 0; i < cellVertices.length; i++){
			if(!cellVertices[i].isAttachedToBasalLayer() && !cellVertices[(i+1)%cellVertices.length].isAttachedToBasalLayer()&& cellVertices[i].edist(cellVertices[(i+1)%cellVertices.length]) < min_edge_length){
				Vertex v1 = cellVertices[i];
				Vertex v2 = cellVertices[(i+1)%cellVertices.length];
				
				HashSet<CellPolygon> cellsConnectedWithV1 = new HashSet<CellPolygon>();
				HashSet<CellPolygon> cellsConnectedWithV2 = new HashSet<CellPolygon>();
				cellsConnectedWithV1.addAll(Arrays.asList(v1.getCellsJoiningThisVertex()));
				cellsConnectedWithV2.addAll(Arrays.asList(v2.getCellsJoiningThisVertex()));
				
				
				HashSet<CellPolygon> cellsConnectedToBothVertices = new HashSet<CellPolygon>();
				
				for(CellPolygon cellPol : cellsConnectedWithV1.toArray(new CellPolygon[cellsConnectedWithV1.size()])){
					if(cellsConnectedWithV2.contains(cellPol)){
						//Afterwards we want to have in these sets only those cells that are connected with only one of the two vertices
						cellsConnectedWithV1.remove(cellPol);
						cellsConnectedWithV2.remove(cellPol);
						cellsConnectedToBothVertices.add(cellPol);
					}
				}
				
				if(cellsConnectedToBothVertices.size() == 2){// && cellsConnectedWithV1.size() == 1 && cellsConnectedWithV2.size() == 1){
					doT1Transition(v1, v2, cellsConnectedWithV1, cellsConnectedWithV2, cellsConnectedToBothVertices.toArray(new CellPolygon[cellsConnectedToBothVertices.size()]));
					GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T1_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T1_TRANSITION_NUMBER)+1));
					System.out.println("T1 Transition performed ------------");
				}
			}
		}
		
	}
	
	public void checkForT2Transition(CellPolygon cell){
		Vertex[] cellVertices = cell.getSortedVertices();
		if(cellVertices.length<=3 || cell.isDying()){
			for(int i = 0; i < cellVertices.length; i++){
				if(cellVertices[i].edist(cellVertices[(i+1)%cellVertices.length]) < min_edge_length || cell.getPreferredArea() <= 0){
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
				
				CellPolygon[] joiningCells = v.getCellsJoiningThisVertex();
				if(joiningCells.length <=2 && joiningCells.length >=1){
					Line line = null;
					if((line=isCloseEnoughToOtherBoundaryForAdhesion(v, false)) != null){
						Vertex isp = line.getIntersectionPointOfLineThroughVertex(v, false, true);
						if(isp != null){
							v.setDoubleX(isp.getDoubleX());
							v.setDoubleY(isp.getDoubleY());
							v.setNewX(isp.getDoubleX());
							v.setNewY(isp.getDoubleY());
						}
						doT3Transition(v, line);
						
					}
				}
			}
		}
	}	
	
	private void doT3Transition(Vertex adhVertex, Line line){
		if(isNotSelfAdhesion(adhVertex, line)){
		Line[] linesConnectedToVertex = selectRelevantLinesConnectedToVertex(adhVertex);		
			if(linesConnectedToVertex != null && linesConnectedToVertex.length >0){
				if(linesConnectedToVertex.length >2){ 
					System.err.print("ERROR: Method do T3 Transition: More than two relevant lines connected to Vertex found!");
				}
				else if(linesConnectedToVertex.length == 2 && isOuterLine(linesConnectedToVertex[0]) && isOuterLine(linesConnectedToVertex[1])) {
					CellPolygon[] adhCells = line.getCellPolygonsOfLine();
					CellPolygon[] line1Cells = linesConnectedToVertex[0].getCellPolygonsOfLine();
					CellPolygon[] line2Cells = linesConnectedToVertex[1].getCellPolygonsOfLine();
					if(adhCells.length == 1 && line1Cells.length == 1 && line2Cells.length == 1){
						Vertex newVertex1 = calculateNewVertex(adhVertex, linesConnectedToVertex[0], line);
						Vertex newVertex2 = calculateNewVertex(adhVertex, linesConnectedToVertex[1], line);	
				
						CellPolygon adhCell = adhCells[0];
						double distanceToAdhesionLineNewVertex1 = (!Double.isNaN(newVertex1.getDoubleX()) && !Double.isNaN(newVertex1.getDoubleY())) 
						                                           ? line.getDistanceOfVertex(newVertex1, false, true) : Double.POSITIVE_INFINITY;
						double distanceToAdhesionLineNewVertex2 = (!Double.isNaN(newVertex2.getDoubleX()) && !Double.isNaN(newVertex2.getDoubleY())) 
                  														? line.getDistanceOfVertex(newVertex2, false, true) : Double.POSITIVE_INFINITY;
						
						
						if(distanceToAdhesionLineNewVertex1 < Double.POSITIVE_INFINITY && distanceToAdhesionLineNewVertex2 < Double.POSITIVE_INFINITY){							
							adhCell.addVertex(newVertex1);
							adhCell.addVertex(newVertex2);
							doT3TransitionLineVertexReplacementCheck(line, newVertex1);
							doT3TransitionLineVertexReplacementCheck(line, newVertex2);							
							line1Cells[0].addVertex(newVertex1);
							line2Cells[0].addVertex(newVertex2);
							doT3TransitionLineVertexReplacementCheck(linesConnectedToVertex[0], newVertex1);
							doT3TransitionLineVertexReplacementCheck(linesConnectedToVertex[1], newVertex2);							
							GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T3_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T3_TRANSITION_NUMBER)+1));
							System.out.println("T3 Transition performed");
						}
						else if(distanceToAdhesionLineNewVertex1 < Double.POSITIVE_INFINITY){
							adhCell.addVertex(newVertex1);
							doT3TransitionLineVertexReplacementCheck(line, newVertex1);
							
							line1Cells[0].addVertex(newVertex1);
							doT3TransitionLineVertexReplacementCheck(linesConnectedToVertex[0], newVertex1);
							GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T3A_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T3A_TRANSITION_NUMBER)+1));
							System.out.println("T3A Transition performed");
						}
						else if(distanceToAdhesionLineNewVertex2 < Double.POSITIVE_INFINITY){
							adhCell.addVertex(newVertex2);
							doT3TransitionLineVertexReplacementCheck(line, newVertex2);
							
							line2Cells[0].addVertex(newVertex2);
							doT3TransitionLineVertexReplacementCheck(linesConnectedToVertex[1], newVertex2);
							GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T3B_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T3B_TRANSITION_NUMBER)+1));
							System.out.println("T3B Transition performed");
						}
						else{
							GlobalBiomechanicalStatistics.getInstance().set(GBSValue.T3C_TRANSITION_NUMBER, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.T3C_TRANSITION_NUMBER)+1));
							System.out.println("T3C Transition performed");
						}				
						
						adhCell.addVertex(adhVertex);
						doT3TransitionLineVertexReplacementCheck(line, adhVertex);
					}
				}
			}
		}
	}
	
	private boolean isNotSelfAdhesion(Vertex adhVertex, Line adhLine){		
		CellPolygon[] adhLineCellPolygons  = adhLine.getCellPolygonsOfLine();
		HashSet<CellPolygon> adhVertexCellPolygons = new HashSet<CellPolygon>();
		adhVertexCellPolygons.addAll(Arrays.asList(adhVertex.getCellsJoiningThisVertex()));		
		for(CellPolygon adhLineCellPol : adhLineCellPolygons){
			if(adhVertexCellPolygons.contains(adhLineCellPol)){ 
				return false;
			}
		}		
		return true;
	}
	
	private boolean doT3TransitionLineVertexReplacementCheck(Line line, Vertex newVertex){
		if(line.getV1().edist(newVertex) < min_edge_length/2){
			line.getV1().replaceVertex(newVertex);
			System.out.println("T3 Transition Vertex replacement performed");
			return true;
		}
		else if(line.getV2().edist(newVertex) < min_edge_length/2){
			line.getV2().replaceVertex(newVertex);
			System.out.println("T3 Transition Vertex replacement performed");
			return true;
		}
		return false;
	}	
	
	private  Line[] selectRelevantLinesConnectedToVertex(Vertex adhVertex){
		ArrayList<Line> linesConnectedToVertex = new ArrayList<Line>();
		Vertex[] connectedVertices = adhVertex.getAllOtherVerticesConnectedToThisVertex();
		HashSet<CellPolygon> cellsConnectedToVertex = new HashSet<CellPolygon>();
		cellsConnectedToVertex.addAll(Arrays.asList(adhVertex.getCellsJoiningThisVertex()));
		
		//here the relevant to line connected to the Vertex v are selected, this should result in two relevant lines
		if(cellsConnectedToVertex.size() > 1){
			if(connectedVertices.length == 3){
				for(Vertex actV: connectedVertices){
					int foundCells = 0;
					for(CellPolygon cellPol: actV.getCellsJoiningThisVertex()){
						if(cellsConnectedToVertex.contains(cellPol)) foundCells++;
					}
					if(foundCells == 1){
						linesConnectedToVertex.add(new Line(adhVertex, actV));
					}
				}
			}
			else{ 
				System.out.println("Found not two connected Vertices");			
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
		
		double[] directionVector = ContinuousVertexField.getInstance().getNormDirectionVector(newVertex, adhVertex);
		double lengthFactor = min_vertex_edge_distance / (2*Math.tan(Math.toRadians(intersectionAngleInDegrees/2)));
		newVertex= new Vertex(adhVertex.getDoubleX() + lengthFactor*directionVector[0], adhVertex.getDoubleY() + lengthFactor*directionVector[1]);
		return newVertex;
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
		Vertex[] verticesNotTransformed = cell.getUnsortedVertices();
		Vertex[] verticesTransformed = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getUnsortedVertices());
		
		for(int i = 0; i < verticesNotTransformed.length; i++){
			new_X += verticesTransformed[i].getDoubleX();
			new_Y += verticesTransformed[i].getDoubleY();
			verticesNotTransformed[i].removeVertexChangeListener(cell);
		}
				
		new_X /= verticesNotTransformed.length;
		new_Y /= verticesNotTransformed.length;
		Vertex newVertex = new Vertex(new_X, new_Y);		
		for(Vertex v : verticesNotTransformed){
			v.replaceVertex(newVertex);
		}
	}
	
	
	private void doT1Transition(Vertex v1, Vertex v2, HashSet<CellPolygon> cellsConnectedOnlyWithV1, HashSet<CellPolygon> cellsConnectedOnlyWithV2, CellPolygon[] cellsConnectedToBothVertices){
		
		Vertex[] verticesV1V2 = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(new Vertex[]{v1, v2});
		
		
		
		Vertex center = new Vertex((verticesV1V2[0].getDoubleX()+ verticesV1V2[1].getDoubleX())/2,(verticesV1V2[0].getDoubleY()+ verticesV1V2[1].getDoubleY())/2);		
		
		//calculate direction Vector, then the Vector orthogonal to the direction Vector, then normalize this vector then, add SHORTEST_EDGE_LENGTH/2 * vector to cell center
		
		double[] directionVectorV1 = ContinuousVertexField.getInstance().getNormDirectionVector(v1, center);
		double[] directionVectorV2 = ContinuousVertexField.getInstance().getNormDirectionVector(v2, center);
		
				
		verticesV1V2[0].setDoubleX((center.getDoubleX()+((1.05*min_edge_length)/2)*directionVectorV1[0]));
		verticesV1V2[0].setDoubleY((center.getDoubleY()+((1.05*min_edge_length)/2)*directionVectorV1[1]));
		
		verticesV1V2[1].setDoubleX((center.getDoubleX()+((1.05*min_edge_length)/2)*directionVectorV2[0]));
		verticesV1V2[1].setDoubleY((center.getDoubleY()+((1.05*min_edge_length)/2)*directionVectorV2[1]));
		
		
		rotateVertex(center, verticesV1V2[0], -90);
		rotateVertex(center, verticesV1V2[1], -90);
		
		v1.setDoubleX(verticesV1V2[0].getDoubleX());
		v1.setDoubleY(verticesV1V2[0].getDoubleY());
		v2.setDoubleX(verticesV1V2[1].getDoubleX());
		v2.setDoubleY(verticesV1V2[1].getDoubleY());
		
		
		
		//add vertex v1 and v2 respectively to those cells that were formerly connected with only one of the vertices
		for(CellPolygon pol :cellsConnectedOnlyWithV1) pol.addVertex(v2);
		for(CellPolygon pol :cellsConnectedOnlyWithV2) pol.addVertex(v1);		
		
		
		Vertex[] sortedVerticesCell1  = cellsConnectedToBothVertices[0].getSortedVertices();
		int indexV1 = findVertexIndexInArray(sortedVerticesCell1, v1);
		
		Vertex neighbour1 = sortedVerticesCell1[modIndex(indexV1-1, sortedVerticesCell1.length)].equals(v2) 
									? sortedVerticesCell1[modIndex(indexV1-2, sortedVerticesCell1.length)]
									: sortedVerticesCell1[modIndex(indexV1-1, sortedVerticesCell1.length)];
									
		Vertex neighbour2 = sortedVerticesCell1[modIndex(indexV1+1, sortedVerticesCell1.length)].equals(v2) 
									? sortedVerticesCell1[modIndex(indexV1+2, sortedVerticesCell1.length)]
									: sortedVerticesCell1[modIndex(indexV1+1, sortedVerticesCell1.length)];
									
		double distanceV1 = neighbour1.edist(v1) + neighbour2.edist(v1);
		double distanceV2 = neighbour1.edist(v2) + neighbour2.edist(v2);
		
		if(distanceV1 < distanceV2){
			cellsConnectedToBothVertices[0].removeVertex(v2);
			cellsConnectedToBothVertices[1].removeVertex(v1);
		}
		else{
			cellsConnectedToBothVertices[0].removeVertex(v1);
			cellsConnectedToBothVertices[1].removeVertex(v2);
		}
	}	
	
	/**
	 * This methods checks whether or not the new vertices introduced in cell division are too close to an already existing vertex
	 * @return
	 */
	private boolean isAngleConventientForCellDivision(CellPolygon cell, Vertex center, double angle, double maxDistance){
				
		Vertex vOnCircle = new Vertex((center.getDoubleX() +maxDistance*Math.cos(angle)), (center.getDoubleY()+maxDistance*Math.sin(angle)), false);
		
		//Calculate Intersection between the line cellcenter-vOnCircle and all sides of cell
		Vertex[] cellVertices = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(cell.getSortedVertices());
		for(int i = 0; i < cellVertices.length; i++){
			Vertex v_s =(new Line(cellVertices[i], cellVertices[(i+1)%cellVertices.length])).getIntersectionOfLinesInLineSegment(new Line(center, vOnCircle));
			if(v_s != null){
				if(v_s.edist(cellVertices[i]) < min_edge_length ||v_s.edist(cellVertices[(i+1)%cellVertices.length]) < min_edge_length){ 
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
	
	private Line[] isVertexTooCloseToAnotherCellBoundary(Vertex vertex, boolean takeNewValues){
		Line[] linesToTest=getLineArrayToTestVertexDistance(vertex);
		HashSet<Line> foundLines = new HashSet<Line>();
		for(Line actLine : linesToTest){
			if(actLine.getDistanceOfVertex(vertex, takeNewValues, true) <= (min_vertex_edge_distance)
					&&!isNotSelfAdhesion(vertex, actLine)) foundLines.add(actLine);		
			}
		return foundLines.toArray(new Line[foundLines.size()]);
	}
	
	private Line isCloseEnoughToOtherBoundaryForAdhesion(Vertex vertex, boolean takeNewValues){
		Line[] linesToTest=getLineArrayToTestVertexDistance(vertex);
		for(Line actLine : linesToTest){
			if(actLine.getDistanceOfVertex(vertex, takeNewValues, true) <= min_vertex_edge_distance && isOuterLine(actLine)) return actLine;
		}
		return null;
	}
	
	private Line[] getLineArrayToTestVertexDistance(Vertex vertex){
		HashSet<Line> lines = new HashSet<Line>();
		for(CellPolygon cell : cellPolygons){
			Line[] cellLines = cell.getLinesOfCellPolygon();
			for(Line actLine : cellLines){
				if(!actLine.belongsVertexToLine(vertex)) lines.add(actLine);
			}
		}
		return lines.toArray(new Line[lines.size()]);
	}
	
	public Line[] getAllLinesOfVertexNetwork(){
		HashSet<Line> lines = new HashSet<Line>();
		for(CellPolygon cell : cellPolygons){
			lines.addAll(Arrays.asList(cell.getLinesOfCellPolygon()));
		}
		return lines.toArray(new Line[lines.size()]);
	}
	
	public Line[] getAllOuterLinesOfVertexNetwork(){
		HashSet<Line> outerLines = new HashSet<Line>();
		for(Line actLine : getAllLinesOfVertexNetwork()){
			if(isOuterLine(actLine)) outerLines.add(actLine);
		}
		return outerLines.toArray(new Line[outerLines.size()]);
	}
	
	public Line[] getAllIntersectingLines(){
		HashSet<Line> intersectingLines = new HashSet<Line>();
		for(Line actLine : getAllLinesOfVertexNetwork()){
			if(checkIfIntersectionLineAndCurate(actLine)) intersectingLines.add(actLine);
		}
		return intersectingLines.toArray(new Line[intersectingLines.size()]);
	}
		
	public Line[] getAllLinesBelongingToOnlyTwoCellsOfVertexNetwork(){
		HashSet<Line> lines = new HashSet<Line>();
		for(Line actLine : getAllLinesOfVertexNetwork()){
			if(isLineOfTwoCellsOnly(actLine)) lines.add(actLine);
		}
		return lines.toArray(new Line[lines.size()]);
	}
	
	public Line[] getAllCorruptLinesOfVertexNetwork(){
		HashSet<Line> corruptLines = new HashSet<Line>();
		for(Line actLine : getAllLinesOfVertexNetwork()){
			if(isCorruptLine(actLine)) corruptLines.add(actLine);
		}
		return corruptLines.toArray(new Line[corruptLines.size()]);
	}
	
	public void checkForVerticesToMerge(CellPolygon cellPolygon){
		Line[] cellLines = cellPolygon.getLinesOfCellPolygon();
		for(Line actLine : cellLines){
			if((isOuterLine(actLine) || isLineOfTwoCellsOnly(actLine)) && actLine.getLength() < (min_edge_length*0.5)){
				mergeVerticesOfLine(actLine);
				System.out.println("Vertices merged");
				GlobalBiomechanicalStatistics.getInstance().set(GBSValue.VERTICES_MERGED, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.VERTICES_MERGED)+1));
			}
		}
	}
	
	private void mergeVerticesOfLine(Line line){
		int noConnectedCellsV1 = line.getV1().getNumberOfCellsJoiningThisVertex();
		int noConnectedCellsV2 = line.getV2().getNumberOfCellsJoiningThisVertex();
		Vertex[] verticesV1V2 = ContinuousVertexField.getInstance().getMinDistanceTransformedVertexArrayMajorityQuadrantReferenceSigned(new Vertex[]{line.getV1(), line.getV2()});
		double newX = (verticesV1V2[0].getDoubleX() + verticesV1V2[1].getDoubleX())/2;
		double newY = (verticesV1V2[0].getDoubleY() + verticesV1V2[1].getDoubleY())/2;
		Vertex remainingVertex, vertexToReplace; 
		if(noConnectedCellsV1 < noConnectedCellsV2){
			remainingVertex = line.getV2();
			vertexToReplace = line.getV1();	
		}
		else{
			remainingVertex = line.getV1();
			vertexToReplace = line.getV2();
		}
		
		remainingVertex.setDoubleX(newX);
		remainingVertex.setDoubleY(newY);
		remainingVertex.setNewX(newX);
		remainingVertex.setNewY(newY);
		vertexToReplace.replaceVertex(remainingVertex);
	}
	
	private boolean isOuterLine(Line line){
		int noOfCommonCells = 0;
		HashSet<CellPolygon> cellsVertex1 = new HashSet<CellPolygon>();
		cellsVertex1.addAll(Arrays.asList(line.getV1().getCellsJoiningThisVertex()));
		for(CellPolygon actCell : line.getV2().getCellsJoiningThisVertex()){
			if(cellsVertex1.contains(actCell)) noOfCommonCells++;
		}
		//if(noOfCommonCells > 2) System.out.println("Komische Sache: Bei einer Kante haben die beiden Vertices mehr als zwei gemeinsame Zellen...");
		return noOfCommonCells < 2;
	}
	
	private boolean isCorruptLine(Line line){
		int noOfCommonCells = 0;
		HashSet<CellPolygon> cellsVertex1 = new HashSet<CellPolygon>();
		cellsVertex1.addAll(Arrays.asList(line.getV1().getCellsJoiningThisVertex()));
		for(CellPolygon actCell : line.getV2().getCellsJoiningThisVertex()){
			if(cellsVertex1.contains(actCell)) noOfCommonCells++;
		}
		
		return noOfCommonCells > 2;
	}
	
	private void checkIfIntruderVertexAndCurate(Vertex vertex){
		if(vertex.getNumberOfCellsJoiningThisVertex() ==1){
			Vertex[] joiningVertices = vertex.getAllOtherVerticesConnectedToThisVertex();
			HashSet<CellPolygon> cellPolygons = new HashSet<CellPolygon>();
			for(Vertex v : joiningVertices){
				cellPolygons.addAll(Arrays.asList(v.getCellsJoiningThisVertex()));
			}
			for(CellPolygon cell : cellPolygons){
				Vertex[] sortedVertices = cell.getSortedVertices();
				int index =  findVertexIndexInArray(sortedVertices, vertex);
				if(index >= 0){
					Vertex lowerBorderVertex = sortedVertices[modIndex(index-1, sortedVertices.length)];
					Vertex upperBorderVertex = sortedVertices[modIndex(index+1, sortedVertices.length)];
					
					for(CellPolygon cell2: cellPolygons){
						if(!cell.equals(cell2)){
							sortedVertices = cell2.getSortedVertices();
							int indexLowerBorder = findVertexIndexInArray(sortedVertices, lowerBorderVertex);
							int indexUpperBorder = findVertexIndexInArray(sortedVertices, upperBorderVertex);
							if(indexLowerBorder >= 0 && indexUpperBorder >= 0 && indexLowerBorder != indexUpperBorder){
								if(indexLowerBorder > indexUpperBorder){
									int tmp = indexLowerBorder;
									indexLowerBorder = indexUpperBorder;
									indexUpperBorder = tmp;
								}
								if((indexUpperBorder - indexLowerBorder) == 1 ||
										((indexUpperBorder - indexLowerBorder) == indexUpperBorder &&(indexUpperBorder==1 || indexUpperBorder== (sortedVertices.length-1)))){
									if(findVertexIndexInArray(sortedVertices, vertex)<0){
										GlobalBiomechanicalStatistics.getInstance().set(GBSValue.INTRUDER_VERTEX_NO, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.INTRUDER_VERTEX_NO)+1));
										System.out.println("Intruder Vertex Curated");
										vertex.setIntruderVertex(true);
										cell2.addVertex(vertex);
									}
								}
							}
						}
					}					
				}
			}
		}		
	}
	
	private int modIndex(int value, int base){
		return value%base < 0 ? (int)((value%base)+base) : (int)(value%base);
	}
	
	private int findVertexIndexInArray(Vertex[] vertexArray, Vertex v){
		for(int i = 0; i < vertexArray.length; i++){
			if(vertexArray[i]!= null && vertexArray[i].equals(v)) return i;
		}
		return -1;
	}
	
	
	
	
	private boolean checkIfIntersectionLineAndCurate(Line line){
		ArrayList<Line[]> intersectionLinePairs = new ArrayList<Line[]>();
		for(CellPolygon cell : this.cellPolygons){
			for(Line actLine :cell.getLinesOfCellPolygon()){
				if(!line.equals(actLine)){
					boolean intersectionFound = actLine.isIntersectionOfLinesInLineSegment(line, min_edge_length) && line.isIntersectionOfLinesInLineSegment(actLine, min_edge_length);
					if(intersectionFound){
						intersectionLinePairs.add(new Line[]{line, actLine});
						Vertex isp = line.getIntersectionOfLinesInLineSegment(actLine);
						if(line.getV1().edist(isp) < line.getV2().edist(isp)){
							line.getV1().replaceVertex(isp);
							line.setV1(isp);
						}
						else{
							line.getV2().replaceVertex(isp);
							line.setV2(isp);
						}
						if(actLine.getV1().edist(isp) < actLine.getV2().edist(isp)){
							actLine.getV1().replaceVertex(isp);
							actLine.setV1(isp);
						}
						else{
							actLine.getV2().replaceVertex(isp);
							actLine.setV2(isp);
						}
						isp.setIntruderVertex(true);
						System.out.println("Intersection Curated!");
					}
				}
			}
		}
		return !intersectionLinePairs.isEmpty();
	}
	
	
	
	private boolean isLineOfTwoCellsOnly(Line line){
		if(line.getV1().getNumberOfCellsJoiningThisVertex() == 2 && line.getV2().getNumberOfCellsJoiningThisVertex()==2){
			HashSet<CellPolygon> cellsV1 = new HashSet<CellPolygon>();
			cellsV1.addAll(Arrays.asList(line.getV1().getCellsJoiningThisVertex()));
			CellPolygon[] cellsV2 = line.getV2().getCellsJoiningThisVertex();
			if(cellsV1.contains(cellsV2[0]) && cellsV1.contains(cellsV2[1])) return true;
		}		
		return false;
	}
	
	
	private void checkCloseToOtherEdge(Vertex v){
		Line[] lines = null;
		if((lines=isVertexTooCloseToAnotherCellBoundary(v, true)) !=null && lines.length > 0){
			v.setVertexColor(Color.YELLOW);
			
			for(Line actLine: lines)actLine.setNewValuesOfVertexToDistance(v, (min_vertex_edge_distance*1.1));
			checkNewVertexValuesForBasalLayerAdhesion(v);
			GlobalBiomechanicalStatistics.getInstance().set(GBSValue.VERTEX_TOO_CLOSE_TO_EDGE, (GlobalBiomechanicalStatistics.getInstance().get(GBSValue.VERTEX_TOO_CLOSE_TO_EDGE) +1));
		}
		else v.setVertexColor(Color.BLUE);
	}
	
	
	
	public void applyVertexPositionCheckPipeline(Vertex v){
		checkNewVertexValuesForBasalLayerAdhesion(v);
		checkIfIntruderVertexAndCurate(v);
	//	checkCloseToOtherEdge(v);
	}
	
	public void applyCellPolygonCheckPipeline(CellPolygon cell){
		checkForT1Transitions(cell);
		checkForT2Transition(cell);
		checkForT3Transitions(cell);
		checkForVerticesToMerge(cell);
	}
	
	
	
	private void checkNewVertexValuesForBasalLayerAdhesion(Vertex vertex){
		TissueBorder tissueBorder = TissueController.getInstance().getTissueBorder();
		
		
				
		if((tissueBorder.lowerBound(vertex.getNewX()) < vertex.getNewY() || getDistanceToBasalLayer(tissueBorder, vertex, true) <= min_basallayer_distance)
				&& !vertex.isAttachedToBasalLayer()){
			
			//if(estimateNewValue){
				 //the method implemented below induces in some cases an upward movement out of the undulation of the basement membrane
				 //for this reason this method is not used; the new values are simply set to the old values
		//	if(!vertex.isAttachedToBasalLayer() || estimateNewValue) 
				setToNewEstimatedValueOnBasalLayer(tissueBorder, vertex);
			//}
		//	else resetToOldValue(vertex);	
			vertex.setIsAttachedToBasalLayer(true);
		}
		else if(vertex.isAttachedToBasalLayer()){
			double distanceToOldValue = vertex.edist(new Vertex(vertex.getNewX(), vertex.getNewY()));
			if(distanceToOldValue < (3*min_basallayer_distance)){
				//setToNewEstimatedValueOnBasalLayer(tissueBorder, vertex);
				if(tissueBorder.lowerBound(vertex.getDoubleX()) != vertex.getDoubleY())
					vertex.setDoubleY(tissueBorder.lowerBound(vertex.getDoubleX()));
				resetToOldValue(vertex);
			}
			else if( (tissueBorder.lowerBound(vertex.getNewX()) > vertex.getNewY())){
					vertex.setIsAttachedToBasalLayer(false);
			}
			
		}
	}
	
	public double getDistanceToBasalLayer(TissueBorder tissueBorder, Vertex vertex, boolean takeNewValues){
		if(tissueBorder.isStandardMembraneLoaded() || tissueBorder.lowerBound(0) != Double.POSITIVE_INFINITY){
			double startX = takeNewValues ? (vertex.getNewX() - min_edge_length):(vertex.getDoubleX() - min_edge_length);
			 
			double minDistance = Double.POSITIVE_INFINITY;
			int maxNoSteps = (int) (2*min_edge_length);
			for(double newX = startX, stepNo = 0; stepNo < maxNoSteps; newX++, stepNo++){
				newX = ContinuousVertexField.getInstance().getXLocationInField(newX);		
				double distance = Math.sqrt(Math.pow(ContinuousVertexField.getInstance().dxMinAbs(newX, (takeNewValues ?vertex.getNewX():vertex.getDoubleX())),2)
						                     + Math.pow((tissueBorder.lowerBound(newX)-(takeNewValues ?vertex.getNewY():vertex.getDoubleY())), 2));
				if(distance < minDistance) minDistance = distance;
			}
			return minDistance;
		}
		else return Double.POSITIVE_INFINITY;
	}
	
	public double getMinDistanceToBasalLayer(TissueBorder tissueBorder, CellPolygon cell){
		double minDistance = Double.POSITIVE_INFINITY;
		for(Vertex v : cell.getUnsortedVertices()){
			double distance = getDistanceToBasalLayer(tissueBorder, v, false);
			if(tissueBorder.lowerBound(v.getDoubleX())< v.getDoubleY()) distance*=-1;
			if(distance < minDistance) minDistance = distance;
			
		}
		return minDistance;
	}
	
	private void setToNewEstimatedValueOnBasalLayer(TissueBorder tissueBorder, Vertex vertex){
		double minY = Double.POSITIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY;
		double minDistance = Double.POSITIVE_INFINITY;
		final double intervalX =  ContinuousVertexField.getInstance().dxMinAbs(vertex.getNewX(), vertex.getDoubleX());
		final double intervalY = ContinuousVertexField.getInstance().dyMinAbs(vertex.getNewY(), vertex.getDoubleY());
		double stepSize = intervalX /10d;
	/*	if(vertex.getNewX() > vertex.getDoubleX() && stepSize>0.1){
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
		else minX = vertex.getNewX();*/
		if((intervalX+intervalY) >=1){
			
			if(ContinuousVertexField.getInstance().dxMinSign(vertex.getNewX(),vertex.getDoubleX())<0){
			
			for(double newX = vertex.getDoubleX(); newX >=vertex.getNewX(); newX -= stepSize){
				   newX = ContinuousVertexField.getInstance().getXLocationInField(newX);		
					double distance = Math.sqrt(Math.pow(ContinuousVertexField.getInstance().dxMinAbs(newX, vertex.getNewX()),2)+ Math.pow((tissueBorder.lowerBound(newX)-vertex.getNewY()), 2));
					distance += Math.sqrt(Math.pow(ContinuousVertexField.getInstance().dxMinAbs(newX,vertex.getDoubleX()),2)+ Math.pow((tissueBorder.lowerBound(newX)-vertex.getDoubleY()), 2));
					if(distance < minDistance){
						minY = tissueBorder.lowerBound(newX);
						minX = newX;
					}
				}
			} else if(ContinuousVertexField.getInstance().dxMinSign(vertex.getNewX(),vertex.getDoubleX())>0){
				for(double newX = vertex.getDoubleX(); newX <=vertex.getNewX(); newX += stepSize){
					newX = ContinuousVertexField.getInstance().getXLocationInField(newX);
					double distance = Math.sqrt(Math.pow(ContinuousVertexField.getInstance().dxMinAbs(newX,vertex.getNewX()),2)+ Math.pow((tissueBorder.lowerBound(newX)-vertex.getNewY()), 2));
					distance += Math.sqrt(Math.pow(ContinuousVertexField.getInstance().dxMinAbs(newX,vertex.getDoubleX()),2)+ Math.pow((tissueBorder.lowerBound(newX)-vertex.getDoubleY()), 2));
					if(distance < minDistance){
						minY = tissueBorder.lowerBound(newX);
						minX = newX;
					}
				}
			}
			else{
				minX = vertex.getNewX();
				minY = tissueBorder.lowerBound(minX);
			}
		}
		if(minX < Double.POSITIVE_INFINITY && minY < Double.POSITIVE_INFINITY){
			vertex.setNewX(minX);
			vertex.setNewY(minY);
		}
		else resetToOldValue(vertex);
	}
		
	private void resetToOldValue(Vertex v){		
		v.setNewX(v.getDoubleX());
		v.setNewY(v.getDoubleY());	
	}

	
   public double getMin_edge_length() {   
   	return min_edge_length;
   }

	
   public double getMin_basallayer_distance_before_adhesion(){   
   	return min_basallayer_distance;
   }
	
   public double getMin_vertex_edge_distance() {   
   	return min_vertex_edge_distance;
   }	
	
}
