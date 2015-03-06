package sim.app.episim.model.biomechanics.vertexbased2d.test;

import sim.app.episim.model.biomechanics.vertexbased2d.VertexBasedModelController;
import sim.app.episim.model.biomechanics.vertexbased2d.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2d.geom.ContinuousVertexField;
import sim.app.episim.model.biomechanics.vertexbased2d.geom.Vertex;




public class TestField {
	
	
	
	public static void main(String[] args) {
		
		ContinuousVertexField.initializeContinousVertexField(100, 100);
				
		Vertex[] vertices = new Vertex[]{new Vertex(90, 30), new Vertex(90, 70), new Vertex(10, 30), new Vertex(10, 70)};
		
		CellPolygon[] cellPolArray = new CellPolygon[1];		
		VertexBasedModelController.getInstance().setCellPolygonArrayInCalculator(cellPolArray);
				
		cellPolArray[0] = new CellPolygon();
		
		for(Vertex v : vertices)cellPolArray[0].addVertex(v);
		
		System.out.println("----------------------");
		
		Vertex v1 = new Vertex(10, 30);
		Vertex v2 = new Vertex(90, 30);
	}

}