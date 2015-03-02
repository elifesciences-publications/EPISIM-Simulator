package sim.app.episim.model.biomechanics.vertexbased2Dr.calc.simanneal;

import sim.app.episim.model.biomechanics.vertexbased2Dr.calc.VertexForcesCalculator;
import sim.app.episim.model.biomechanics.vertexbased2Dr.geom.Vertex;
import net.sourceforge.jannealer.ObjectiveFunction;


public class VertexForcesObjectiveFunction implements ObjectiveFunction{
	
	private Vertex vertex;
	private VertexForcesCalculator calculator;
	
	
	
	public VertexForcesObjectiveFunction(Vertex vertex){
		this.vertex = vertex;
		calculator = new VertexForcesCalculator();		
	}

	public int getNdim() {return 2;}

	public double distance(double[] params) {
	/*	vertex.setNewX(params[0]);
		vertex.setNewY(params[1]);
		vertex.commitNewValues();
		double[] result = calculator.calculatedForcesActingOnVertex(vertex);
		return (Math.abs(result[0])+ Math.abs(result[1]));*/
		return 0;
	}

}
