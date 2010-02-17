package sim.app.episim.biomechanics;

import java.util.ArrayList;

public class Cell implements VertexChangeListener{
	
 private static int nextId = 1;
 private final int id;
 private int x = 0;
 private int y = 0;
	
private ArrayList<Vertex> vertices;

public Cell(int x, int y){
	id = nextId++;
	vertices = new ArrayList<Vertex>();
	this.x = x;
	this.y = y;
}

public Cell(){
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
	Cell other = (Cell) obj;
	if(id != other.id)
		return false;
	return true;
}

public int getX() { return x; }
public void setX(int x) { this.x = x; }
public int getY() { return y; }
public void setY(int y){ this.y = y; }

}
