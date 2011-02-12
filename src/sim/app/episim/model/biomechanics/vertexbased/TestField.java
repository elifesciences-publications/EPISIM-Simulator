package sim.app.episim.model.biomechanics.vertexbased;




public class TestField {

	
	
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ContinuousVertexField.initializeCondinousVertexField(10, 10);
		Vertex v1 = new Vertex(8,10);
		Vertex v2 = new Vertex(15, 10);
		
		System.out.println("Position v1: " + ContinuousVertexField.getInstance().getLocationInField(v1, false));
		System.out.println("Position v2: " + ContinuousVertexField.getInstance().getLocationInField(v2, false));
		System.out.println("Distance: " + ContinuousVertexField.getInstance().getEuclideanDistance(v1, v2));

	}

}
