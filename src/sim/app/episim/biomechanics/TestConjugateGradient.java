package sim.app.episim.biomechanics;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.ICC;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;


public class TestConjugateGradient {

	
	
	public static void main(String[] args){
		CompRowMatrix A = new CompRowMatrix(new DenseMatrix(new double[][]{{4,1},{1,3}}));
		DenseVector x, b;

		
		
		b = new DenseVector(new double[]{1,1});
		x = new DenseVector(new double[]{2,1});
		// Allocate storage for Conjugate Gradients
		IterativeSolver solver = new CG(x);

		// Create a Cholesky preconditioner
		Preconditioner M = new ICC(A.copy());
		
		// Set up the preconditioner, and attach it
		M.setMatrix(A);
		
		solver.setPreconditioner(M);

		// Add a convergence monitor
		solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());

		// Start the solver, and check for problems
		try {
		  solver.solve(A, b, x);
		  
		} catch (IterativeSolverNotConvergedException e) {
		  System.err.println("Iterative solver failed to converge");
		}
		
		System.out.println(x.toString());
	}
	
}
