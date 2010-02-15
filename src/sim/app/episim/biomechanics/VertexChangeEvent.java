package sim.app.episim.biomechanics;


public class VertexChangeEvent {
	
	public enum VertexChangeEventType {VERTEXMOVED, VERTEXDELETED;}
	
	private Vertex source;
	private VertexChangeEventType type;
	
	VertexChangeEvent(Vertex source, VertexChangeEventType type){
		this.source = source;
		this.type = type;
	}
	
	 public Vertex getSource() { return source; }

		
	 public VertexChangeEventType getType() {	return type; }

}
