package sim.app.episim.model.biomechanics.vertexbased.calc.simanneal;

import net.sourceforge.jannealer.AnnealingScheme;
import net.sourceforge.jannealer.ObjectiveFunction;
import net.sourceforge.jannealer.test.Util;


public class TestAnnealing {
	
	public static void main(String[] args)
	{
		AnnealingScheme scheme = new AnnealingScheme();
		
		scheme.setFunction(new ObjectiveFunction()
		{
			public int getNdim()
			{
				return 1;
			}
			/* this function must return a number which is a measure of 
			 * the goodness of the proposed solution
			 */  
			public double distance(double[] vertex)
			{
				return Math.pow(vertex[0]-3, 2);
			}
		});
		
		/* this is where we search for a solution */
		
		scheme.setCoolingRate(94.8662551440329);
		scheme.setTemperature(5438270.421124832);
		scheme.setIterations(15);
		scheme.setSolution(new double[]{100000d});
		scheme.anneal();
		Util.printSolution(scheme);
	}

}
