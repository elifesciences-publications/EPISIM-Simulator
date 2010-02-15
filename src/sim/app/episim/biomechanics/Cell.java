package sim.app.episim.biomechanics;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import sim.app.episim.biomechanics.VertexChangeEvent.VertexChangeEventType;


public class Cell implements VertexChangeListener{
	
 private static int nextId = 1;
 private final int id;
	
private ArrayList<Vertex> vertices;

public Cell(){
	id = nextId++;
	vertices = new ArrayList<Vertex>();
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


	

}
