package sim.app.episim.model.biomechanics.vertexbased2D.geom;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2D.oldmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModelGP;
import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModelController;
import sim.app.episim.model.biomechanics.vertexbased2D.calc.CellPolygonCalculator;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper.IntersectionPoints;
import sim.app.episim.visualization.CellEllipse;
import sim.util.Double2D;


public abstract class CellPolygonNetworkBuilder {
	
	public static final int STARTX= 100;
	public static final int STARTY= 100;
	
	public static final double ALLOWED_DELTA = 1;
	
	private static final double MAX_MERGE_VERTEX_DISTANCE_FACTOR = 0.3;
	
	private static final double MAX_CLEAN_VERTEX_DISTANCE = 4;
	private static final double MAX_CLEAN_ESTIMATED_VERTEX_FACTOR = 0.5;
	
	public static CellPolygon[] getSquareVertex(int xStart, int yStart, int sidelength, int size)
	{
		CellPolygonCalculator calculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
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
	
	
	
	
	public static void calculateEstimatedVertices(CellEllipse ellipse){
		CellPolygonCalculator calculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
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
	
	private static boolean contains(CellEllipse ellipse, double x, double y){
		if(ellipse.getClippedEllipse().getBounds().contains(x-ALLOWED_DELTA, y-ALLOWED_DELTA)
				 || ellipse.getClippedEllipse().getBounds().contains(x-ALLOWED_DELTA, y+ALLOWED_DELTA)
				 || ellipse.getClippedEllipse().getBounds().contains(x+ALLOWED_DELTA, y-ALLOWED_DELTA)
				 || ellipse.getClippedEllipse().getBounds().contains(x+ALLOWED_DELTA, y+ALLOWED_DELTA)){
			return true;
		}
		return false;
	}
	
	private static boolean isZeroVertex(Vertex v){
		return v.getDoubleX() == 0 && v.getDoubleY() == 0;
	}

	private static Vertex[][] getVertices(int startX, int startY, int rows, int columns, int height){
		
		Vertex[][] vertices = new Vertex[2*(rows+1)][(columns+1)];
		
		
		startY = startY - CellPolygonCalculator.SIDELENGTH;
		
		//first row Vertices
		for(int i = 0; i < columns; i++) vertices[0][i] = new Vertex(startX + i*2*height, startY, false);
		
		// in between two rows of vertices with the same startX are calculated
		for(int i = 1;  i <= rows; i++){
			if((i%2)!= 0) startX -=height;
			else startX += height;
			
			startY += CellPolygonCalculator.SIDELENGTHHALF;
			for(int n = 0; n <= columns; n++) vertices[((2*i)-1)][n] = new Vertex(startX+ n*2*height, startY, false);
			startY += CellPolygonCalculator.SIDELENGTH;
			for(int n = 0; n <= columns; n++) vertices[(2*i)][n] = new Vertex(startX+ n*2*height, startY, false);
		}
		
		
		//last row Vertices
		startY += CellPolygonCalculator.SIDELENGTHHALF;
		startX += height;
		for(int i = 0; i < columns; i++) vertices[vertices.length-1][i] = new Vertex(startX + i*2*height, startY, false);
		
		return vertices;
	}
	
	public static CellPolygon[] getStandardCellArray(int rows, int columns){
		CellPolygonCalculator calculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
		return getStandardCellArray(STARTX, STARTY, rows, columns);
	}
	
	public static CellPolygon[] getStandardThreeCellArray(){
		CellPolygonCalculator calculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
		int startX1= 175;
		int startX2= 375;
		int startY= 250;
		return new CellPolygon[]{
				getStandardCellArray(startX1, startY, 1, 1)[0],
				getStandardCellArray(startX2, startY, 1, 1)[0],
				getStandardCellArray(STARTX, STARTY, 1, 1)[0]
		};
	}
	
	public static CellPolygon[] getStandardMembraneCellArray(){
		
		ArrayList<CellPolygon> standardCellEnsemble = new ArrayList<CellPolygon>();		
		Double2D lastloc = new Double2D(0, (int)TissueController.getInstance().getTissueBorder().lowerBoundInMikron(CellPolygonCalculator.SIDELENGTH, 0)+CellPolygonCalculator.SIDELENGTH);
		Double2D newloc= null;
		for(double x = CellPolygonCalculator.SIDELENGTH; x <= TissueController.getInstance().getTissueBorder().getWidthInPixels(); x += 1){		
			//	if(newloc.distance(lastloc) > 3* CellPolygonCalculator.SIDELENGTH || x == CellPolygonCalculator.SIDELENGTH){
				
			newloc = new Double2D(x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, 0)+CellPolygonCalculator.SIDELENGTH);			
			
			if(newloc.distance(lastloc) > 4 * CellPolygonCalculator.SIDELENGTH || x == CellPolygonCalculator.SIDELENGTH){
				CellPolygon cell = getStandardCellArray((int)x, (int)(TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x,0)+CellPolygonCalculator.SIDELENGTH), 1, 1)[0];	
				standardCellEnsemble.add(cell);
				lastloc = newloc;		//	}	
			}
		}		
	setCellEnsembleToMinBasalLayerDistance(standardCellEnsemble);
		
		return standardCellEnsemble.toArray(new CellPolygon[standardCellEnsemble.size()]);
	}
	
	private static void setCellEnsembleToMinBasalLayerDistance(ArrayList<CellPolygon> standardCellEnsemble){
		CellPolygonCalculator calculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
		for(CellPolygon cell: standardCellEnsemble){
			double minBasalLayerDistance = calculator.getMinDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(),cell);
			int sign = 1;
			Vertex center = calculator.getCellCenter(cell);
			if(minBasalLayerDistance < 0){
				cell.moveTo(center.getDoubleX()+1, center.getDoubleY());
				double newMinDistance = calculator.getMinDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(),cell);
				if(newMinDistance < minBasalLayerDistance) sign = -1;
			}
			int delta = 0;
			while(minBasalLayerDistance < 0){
				cell.moveTo(center.getDoubleX()+(sign*delta), center.getDoubleY()+(delta));
				delta++;
				minBasalLayerDistance = calculator.getMinDistanceToBasalLayer(TissueController.getInstance().getTissueBorder(),cell);
			}			
		}
	}
	
	public static void setCellPolygonSizeAccordingToAge(double age, CellPolygon cellPolygon){
		
		double additionalArea =((VertexBasedModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).getGrowth_rate_per_sim_step()*age;
		double totalArea = additionalArea + cellPolygon.getCurrentArea();
		
		Vertex cellCenter = cellPolygon.getCellCenter();		
		double newSideLength = Math.sqrt(((2d/3d)*totalArea)/Math.sqrt(3d));
		for(Vertex v : cellPolygon.getUnsortedVertices()){
			double[] directionVector = ContinuousVertexField.getInstance().getNormDirectionVector(v, cellCenter);
			v.setDoubleX(cellCenter.getDoubleX() + newSideLength*directionVector[0]);
			v.setDoubleY(cellCenter.getDoubleY() + newSideLength*directionVector[1]);
		}
		
		ArrayList<CellPolygon> cell = new ArrayList<CellPolygon>();
		cell.add(cellPolygon);		
		if(age>0) cellPolygon.activateInitialRandomStemCellProliferation();		
	}	
	
	private static CellPolygon[] getStandardCellArray(int startX, int startY, int rows, int columns){
		
		CellPolygonCalculator calculator = VertexBasedModelController.getInstance().getCellPolygonCalculator();
		
		int height = Math.round((float) Math.sqrt(Math.pow(CellPolygonCalculator.SIDELENGTH, 2) - Math.pow(CellPolygonCalculator.SIDELENGTH/2, 2)));
		
		CellPolygon[] cells = getCells(startX, startY, rows, columns, height);
		Vertex[][] vertices = getVertices(startX, startY, rows, columns, height);
		
		for(int rowNo = 0; rowNo < vertices.length; rowNo++){
			for(int columnNo = 0; columnNo < vertices[rowNo].length; columnNo++){
				for(int cellNo = 0; cellNo <  cells.length; cellNo++){
					if(vertices[rowNo][columnNo] != null && cells[cellNo] != null
						&&	((int)distance(cells[cellNo].getX(), cells[cellNo].getY(), vertices[rowNo][columnNo].getIntX(), vertices[rowNo][columnNo].getIntY())) <= CellPolygonCalculator.SIDELENGTH)
						cells[cellNo].addVertex(vertices[rowNo][columnNo]);					
				}
			}
		}		
		calculator.setCellPolygons(cells);
		for(CellPolygon pol : cells){
			pol.setPreferredArea(pol.getCurrentArea());			
		}		
		return cells;		
	}
	
	private static double distance(double x1, double y1, double x2, double y2){	
		return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));		
	}
	
	private static CellPolygon[] getCells(int startX, int startY,int rows, int columns, int height){
		CellPolygon[] cells = new CellPolygon[rows*columns];
		int cellIndex = 0;
		for(int i = 0; i < rows; i++){
			for(int n = 0; n < columns; n++){
				if(((i+1)%2) == 1)cells[cellIndex++] = new CellPolygon(startX + 2*n*height, startY + i*(CellPolygonCalculator.SIDELENGTH+CellPolygonCalculator.SIDELENGTHHALF));
				else cells[cellIndex++] = new CellPolygon(startX + (2*n + 1)*height, startY + i*(CellPolygonCalculator.SIDELENGTH+CellPolygonCalculator.SIDELENGTHHALF));
			}
		}
		return cells;
	}
	
}
