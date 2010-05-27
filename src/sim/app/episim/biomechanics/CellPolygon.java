package sim.app.episim.biomechanics;

import java.util.ArrayList;
import java.util.HashSet;

import sim.app.episim.biomechanics.VertexChangeEvent.VertexChangeEventType;

public class CellPolygon implements VertexChangeListener{
	
 private static int nextId = 1;
 private final int id;
 private double x = 0;
 private double y = 0;
 private boolean selected = false;
	
private HashSet<Vertex> vertices;

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

public Vertex[] getVertices(){ return vertices.toArray(new Vertex[vertices.size()]); }

public void sortVertices(){
	GrahamScan scan = new GrahamScan();
	Vertex[] v =  vertices.toArray(new Vertex[vertices.size()]);
	int h = scan.computeHull(v);
	vertices.clear();
	for(Vertex ver : v) vertices.add(ver);	
	
	//System.out.println("No of vertices: " + v.length + "    No of Hull Points: " + h);
}

public Vertex[] getSortedVertices(){
	GrahamScan scan = new GrahamScan();
	Vertex[] v =  vertices.toArray(new Vertex[vertices.size()]);
	int h = scan.computeHull(v);
	return v;
}

public void handleVertexChangeEvent(VertexChangeEvent event) {
	if(event.getType() == VertexChangeEventType.VERTEXDELETED){ 
		removeVertex(event.getSource());	
	}
	else if(event.getType() == VertexChangeEventType.VERTEXREPLACED){
		removeVertex(event.getSource());
		addVertex(event.getNewVertex());
	}
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

}
