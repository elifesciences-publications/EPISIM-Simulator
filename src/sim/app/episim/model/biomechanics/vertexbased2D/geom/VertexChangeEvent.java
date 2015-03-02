package sim.app.episim.model.biomechanics.vertexbased2D.geom;



public class VertexChangeEvent {
	
	public enum VertexChangeEventType {VERTEXMOVED, VERTEXDELETED, VERTEXREPLACED;}
	
	private Vertex source;
	private Vertex newVertex;
	private VertexChangeEventType type;
	
	VertexChangeEvent(Vertex source, VertexChangeEventType type){
		this.source = source;
		this.type = type;
	}
	
	VertexChangeEvent(Vertex source, Vertex newVertex, VertexChangeEventType type){
		this.source = source;
		this.type = type;
		this.newVertex = newVertex;
	}
	
	 public Vertex getSource() { return source; }

	 public Vertex getNewVertex(){ return newVertex;}
		
	 public VertexChangeEventType getType() {	return type; }

}
