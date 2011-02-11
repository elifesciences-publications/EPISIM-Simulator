package sim.app.episim.model.biomechanics.vertexbased;

import sim.app.episim.model.biomechanics.vertexbased.VertexChangeEvent.VertexChangeEventType;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;


public class ContinuousVertexField implements VertexChangeListener{
	
	private static ContinuousVertexField instance = new ContinuousVertexField();
	
	
	private Continuous2D vertexField;
	private ContinuousVertexField(){}
	private ContinuousVertexField(int width, int height){
		vertexField = new Continuous2D(CellPolygonCalculator.MIN_EDGE_LENGTH, width, height);
	}
	
	public void addVertexToField(Vertex vertex){
		if(vertexField != null && vertex != null) vertexField.setObjectLocation(vertex, new Double2D(vertex.getDoubleX(), vertex.getDoubleY()));
	}
	
	public static synchronized ContinuousVertexField getInstance(){
		return instance;
	}
	
	public static void initializeCondinousVertexField(int width, int height){
		instance = new ContinuousVertexField(width, height);
	}
	public void handleVertexChangeEvent(VertexChangeEvent event) {
		Vertex source = event.getSource();
		VertexChangeEventType eventType = event.getType();
	   if(eventType == VertexChangeEventType.VERTEXDELETED){
	   	if(vertexField != null) vertexField.remove(source);
	   }
	   else if(eventType == VertexChangeEventType.VERTEXREPLACED){
	   	if(vertexField != null && source != null && event.getNewVertex() != null){ 
	   		vertexField.remove(source);
	   		vertexField.setObjectLocation(event.getNewVertex(), new Double2D(event.getNewVertex().getDoubleX(), event.getNewVertex().getDoubleY()));
	   	}
	   }
	   else if(eventType == VertexChangeEventType.VERTEXMOVED){
	   	if(vertexField != null){
	   		vertexField.setObjectLocation(source, new Double2D(source.getDoubleX(), source.getDoubleY()));
	   	}
	   }	   
   }
	
	public boolean isRegisteredInField(Vertex vertex){
		return vertexField == null || !vertexField.exists(vertex) ? false : true;
	}

}
