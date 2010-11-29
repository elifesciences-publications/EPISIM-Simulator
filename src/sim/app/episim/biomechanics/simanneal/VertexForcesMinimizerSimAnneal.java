package sim.app.episim.biomechanics.simanneal;

import sim.app.episim.biomechanics.Vertex;
import net.sourceforge.jannealer.AnnealingScheme;


public class VertexForcesMinimizerSimAnneal {
	
	private AnnealingScheme annealingScheme;
	
	public VertexForcesMinimizerSimAnneal(){
		annealingScheme = new AnnealingScheme();
		annealingScheme.setTolerance(10);
	}	
	public VertexForcesMinimizerSimAnneal(double temperature, double coolingRate, int noOfIterations, double tolerance){
		annealingScheme = new AnnealingScheme();
		annealingScheme.setCoolingRate(coolingRate);
		annealingScheme.setTemperature(temperature);
		annealingScheme.setIterations(noOfIterations);
		annealingScheme.setTolerance(tolerance);
	}
	
	
	public void relaxForcesActingOnVertex(Vertex vertex){
		double old_X = vertex.getDoubleX();
		double old_Y = vertex.getDoubleY();
		
		
		annealingScheme.setFunction(new VertexForcesObjectiveFunction(vertex));
		annealingScheme.setSolution(new double[]{vertex.getDoubleX(), vertex.getDoubleY()});
		
		annealingScheme.anneal();
		double[] solution = annealingScheme.getSolution();
		vertex.setNewX(solution[0]);
		vertex.setNewY(solution[1]);
		vertex.setDoubleX(old_X);
		vertex.setDoubleY(old_Y);
		//vertex.commitNewValues();
	}

}
