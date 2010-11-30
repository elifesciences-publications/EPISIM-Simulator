package sim.app.episim.biomechanics;

import java.awt.Color;
import java.util.HashSet;

import sim.app.episim.biomechanics.VertexChangeEvent.VertexChangeEventType;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;

public class CellPolygon implements VertexChangeListener{
	
 private static int nextId = 1;
 private final int id;
 private double x = 0;
 private double y = 0;
 private boolean selected = false;
 
 private double preferredArea;
 
 private double originalPreferredArea = Double.NEGATIVE_INFINITY;
	
 private HashSet<Vertex> vertices;
 private Vertex[] sortedVertices;
 private boolean isAlreadyCalculated;
 
 private boolean isVertexSortingDirty = true;
public CellPolygon(double x, double y){
	id = nextId++;
	vertices = new HashSet<Vertex>();
	this.x = x;
	this.y = y;
}

public CellPolygon(){
	this(0, 0);
}

public void addVertex(Vertex v){
	if(v != null &&  !vertices.contains(v) && !v.isWasDeleted()){
		vertices.add(v);
		v.addVertexChangeListener(this);
		isVertexSortingDirty = true;
	}
}

public void removeVertex(Vertex v){
	if(vertices.contains(v)){
		vertices.remove(v);
		v.removeVertexChangeListener(this);
	}
}

public int getId(){ return id;}


public int hashCode() {

	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	return result;
}

public Vertex[] getUnsortedVertices(){ return vertices.toArray(new Vertex[vertices.size()]); }

/*public void sortVerticesWithGrahamScan(){
	GrahamScan scan = new GrahamScan();
	Vertex[] v =  vertices.toArray(new Vertex[vertices.size()]);
	int h = scan.computeHull(v);
	vertices.clear();
	for(Vertex ver : v) vertices.add(ver);	
	
	//System.out.println("No of vertices: " + v.length + "    No of Hull Points: " + h);
}

public Vertex[] getSortedVerticesUsingGrahamScan(){
	GrahamScan scan = new GrahamScan();
	Vertex[] v =  vertices.toArray(new Vertex[vertices.size()]);
	int h = scan.computeHull(v);
	return v;
}*/

public void grow(double areaToGrow){
	if(this.originalPreferredArea == Double.NEGATIVE_INFINITY) this.originalPreferredArea = this.preferredArea;
	this.preferredArea += areaToGrow;
}

public boolean canDivide(){
	return getCurrentArea() > (2*originalPreferredArea) && originalPreferredArea > 0;
}

public CellPolygon cellDivision(){
	this.preferredArea = this.originalPreferredArea;
	this.originalPreferredArea = Double.NEGATIVE_INFINITY;
	return Calculators.divideCellPolygon(this);
}


public Vertex[] getSortedVertices(){
	if(isVertexSortingDirty || sortedVertices == null) sortVerticesUsingTravellingSalesmanSimulatedAnnealing();
	return sortedVertices;
}

private void sortVerticesUsingTravellingSalesmanSimulatedAnnealing(){
	SimulatedAnnealingForOrderingVertices sim = new SimulatedAnnealingForOrderingVertices(getUnsortedVertices());
	sortedVertices = sim.sortVertices();	
	isVertexSortingDirty = false;
}

public void handleVertexChangeEvent(VertexChangeEvent event) {
	if(event.getType() == VertexChangeEventType.VERTEXDELETED){ 
		removeVertex(event.getSource());
		isVertexSortingDirty = true;
	}
	else if(event.getType() == VertexChangeEventType.VERTEXREPLACED){
		removeVertex(event.getSource());
		addVertex(event.getNewVertex());
		isVertexSortingDirty = true;
	}
	else if (event.getType() == VertexChangeEventType.VERTEXMOVED){
		isVertexSortingDirty = true;
	}
}


public int getNumberOfNeighbourPolygons(){
	return getNeighbourPolygons().length;
}


public CellPolygon[] getNeighbourPolygons(){
	HashSet<Integer> alreadyCheckedIds = new HashSet<Integer>();
	HashSet<CellPolygon> neighbourPolygonsSet = new HashSet<CellPolygon>();
	for(Vertex v: this.getUnsortedVertices()){
		if(v.getNumberOfCellsJoiningThisVertex() > 0){
			for(CellPolygon pol :v.getCellsJoiningThisVertex()){
				if(!alreadyCheckedIds.contains(pol.getId()) && pol.getId() != this.getId()){ 
					alreadyCheckedIds.add(pol.getId());
					neighbourPolygonsSet.add(pol);
				}
			}
		}
	}
	
	CellPolygon[] neighbourPolygonsArray = new CellPolygon[neighbourPolygonsSet.size()];
	
	return neighbourPolygonsSet.toArray(neighbourPolygonsArray);	
}


public double getCurrentArea(){
	return Calculators.getCellArea(this);
}


public Color getFillColor() { 
	int neighbourNo = getNumberOfNeighbourPolygons();
	
		if(neighbourNo <= 3) return Color.WHITE;
		else if(neighbourNo == 4) return Color.GREEN;
		else if(neighbourNo == 5) return Color.YELLOW;
		else if(neighbourNo == 6) return Color.GRAY;
		else if(neighbourNo == 7) return Color.BLUE;
		else if(neighbourNo == 8) return Color.RED;
		else if(neighbourNo >= 9) return Color.PINK;
	
	 
	
	return Color.WHITE;
}



public boolean equals(Object obj) {

	if(this == obj)
		return true;
	
	if(obj == null)
		return false;
	
	if(getClass() != obj.getClass())
		return false;
	
	CellPolygon other = (CellPolygon) obj;
	
	if(id != other.id)
		return false;	
	
	return true;
}

public double getX() { return x; }
public void setX(double x) { this.x = x; }
public double getY() { return y; }
public void setY(double y){ this.y = y; }


public boolean isSelected() {
	return selected;
}


public void setSelected(boolean selected) {
	this.selected = selected;
}


public void resetCalculationStatusOfAllVertices(){
	for(Vertex v : this.getUnsortedVertices()){ 
		v.resetCalculationStatus();
	}
}
public void commitNewVertexValues(){
	for(Vertex v : this.getUnsortedVertices()){ 
		v.commitNewValues();
	}
}

public double getPreferredArea() {
	return preferredArea;
}

public void setPreferredArea(double preferredArea) {
	this.preferredArea = preferredArea;
}

public void setIsAlreadyCalculated(boolean val){this.isAlreadyCalculated = val;}
public boolean isAlreadyCalculated(){ return this.isAlreadyCalculated;}


}
