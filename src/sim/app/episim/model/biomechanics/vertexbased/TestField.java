package sim.app.episim.model.biomechanics.vertexbased;




public class TestField {

	
	
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ContinuousVertexField.initializeCondinousVertexField(100, 100);
		Vertex v1 = new Vertex(10, 0);
		Vertex v2 = new Vertex(10, 10);
		
		Line testLine1 = new Line(v1, v2);
		
		Vertex v3 = new Vertex(0, 5);
		
		testLine1.setNewValuesOfVertexToDistance(v3, 20);
		
		System.out.println("New Coordinates: ("+ v3.getNewX()+", "+ v3.getNewY()+")");		
		
		

	}

}
