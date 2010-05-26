package sim.app.episim.biomechanics;

import java.util.ArrayList;

public class CellPolygon implements VertexChangeListener{
	
 private static int nextId = 1;
 private final int id;
 private double x = 0;
 private double y = 0;
 private boolean selected = false;
	
private ArrayList<Vertex> vertices;

public CellPolygon(double x, double y){
	id = nextId++;
	vertices = new ArrayList<Vertex>();
	this.x = x;
	this.y = y;
}

public CellPolygon(){
	this(0, 0);
}

public void addVertex(Vertex v){
	vertices.add(v);
	v.addVertexChangeListener(this);
}

public void removeVertex(Vertex v){
	vertices.remove(v);
	v.removeVertexChangeListener(this);
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

public void handleVertexChangeEvent(VertexChangeEvent event) {
	// TODO Auto-generated method stub	
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
