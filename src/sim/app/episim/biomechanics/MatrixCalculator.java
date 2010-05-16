package sim.app.episim.biomechanics;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.ICC;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;


public class MatrixCalculator {
	
	
	private boolean testIfSignumChangeForAreaCalculation(double polygon[][], int vertexNumber){
		double area = 0;
		int noOfVertices = polygon.length;
		for(int i = vertexNumber, n = 0; n < polygon.length; n++, i++ ){
			area += (polygon[i%noOfVertices][0]*polygon[(i+1)%noOfVertices][1]- polygon[(i+1)%noOfVertices][0]*polygon[i%noOfVertices][1]);
		}
		return area < 0;
	}
	
	private int signManhattan(double value1, double value2){
		
		return (value1-value2) < 0 ? -1:1;
	}
	
	public Matrix calculateAreaMatrixForPolygon(double[][] polygon, int vertexNumber){
		
		double sign = testIfSignumChangeForAreaCalculation(polygon, vertexNumber) ? -1 : 1;
		double noOfVertices = polygon.length;
		double [][] areaMatrix = new double[polygon[0].length][polygon[0].length];
		
		areaMatrix[0][0] = Math.pow(((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1])),2);
		areaMatrix[1][1] = Math.pow(((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0])),2);
		areaMatrix[0][1] = ((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]))* 
				((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]));
		areaMatrix[1][0] = ((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]))* 
				((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]));
			
		Matrix m = new DenseMatrix(areaMatrix);
		System.out.println(m.toString());
		return m;
	}
	
	public Matrix calculatePerimeterMatrixForPolygon(double[][] polygon, int vertexNumber){
		
		double[] coeffizients = getPerimeterCoeffizients(polygon, vertexNumber);
		
		double[][] resultMatrix = new double[][]{{Math.pow(coeffizients[0], 2), coeffizients[0]*coeffizients[1]},{coeffizients[0]*coeffizients[1], Math.pow(coeffizients[1], 2)}};
		
		Matrix m = new DenseMatrix(resultMatrix);
		
		printMatrix(resultMatrix);
		
		return m;
	}
	
	private double[] getPerimeterCoeffizients(double[][] polygon, int vertexNumber){
		double noOfVertices = polygon.length;
		int coeffizient_1 = 0, coeffizient_2 = 0;
		int sign = signManhattan(polygon[vertexNumber][0], polygon[mod((vertexNumber+1), noOfVertices)][0]);
		coeffizient_1 += sign;
		sign = signManhattan(polygon[mod((vertexNumber-1), noOfVertices)][0], polygon[vertexNumber][0]);
		coeffizient_1 -= sign;		
		sign = signManhattan(polygon[vertexNumber][1], polygon[mod((vertexNumber+1), noOfVertices)][1]);
		coeffizient_2 += sign;
		sign = signManhattan(polygon[mod((vertexNumber-1), noOfVertices)][1], polygon[vertexNumber][1]);
		coeffizient_2 -= sign;
		
		return new double[]{coeffizient_1, coeffizient_2};
	}
	
	public Vector calculatePerimeterResultVector(double[][] polygon, int vertexNumber){
		double noOfVertices = polygon.length;
		double[] coeffizients = getPerimeterCoeffizients(polygon, vertexNumber);
		double[] resultVector = new double[2];
		int sign = 1;
		for(int i = 0; i < noOfVertices; i++){
			if(i== vertexNumber){
				sign = signManhattan(polygon[vertexNumber][0], polygon[mod((vertexNumber+1), noOfVertices)][0]);
				resultVector[0] += sign*-1*polygon[mod((vertexNumber+1), noOfVertices)][0];
				sign = signManhattan(polygon[vertexNumber][1], polygon[mod((vertexNumber+1), noOfVertices)][1]);
				resultVector[0] += sign*-1*polygon[mod((vertexNumber+1), noOfVertices)][1];
							
			}
			else if(mod((i+1), noOfVertices) == vertexNumber){
				sign = signManhattan(polygon[i][0], polygon[vertexNumber][0]);
				resultVector[0] += sign*polygon[i][0];
				sign = signManhattan(polygon[i][1], polygon[vertexNumber][1]);
				resultVector[0] += sign*polygon[i][1];
			}
			else{
				resultVector[0] += Math.abs(polygon[i][0]-polygon[mod((i+1), noOfVertices)][0]);
				resultVector[0] += Math.abs(polygon[i][1]-polygon[mod((i+1), noOfVertices)][1]);
			}
			
		}
		resultVector[1] = resultVector[0];
		resultVector[0] *= (-1*coeffizients[0]);
		resultVector[1] *= (-1*coeffizients[1]);
	
		printVector(resultVector);
		Vector v = new DenseVector(resultVector);
		
		return v;
	}
	
	private void printVector(double[] v){
		for(int i = 0; i < v.length; i++){
			System.out.println(v[i]);
		}
	}
	
	private void printMatrix(double[][] m){
		for(int i = 0; i < m.length; i++){
			for(int n = 0; n < m[i].length; n++){
				System.out.print(m[i][n]+" ");
			}
			System.out.println();
		}
	}
	
	
	public Vector calculateAreaResultVector(double[][] polygon, int vertexNumber, double A0){
		double sign = testIfSignumChangeForAreaCalculation(polygon, vertexNumber) ? -1 : 1;
		double noOfVertices = polygon.length;
		double[] resultVector = new double[2];
		double provisionalResult = 0;
		
		for(int i = 0; i < noOfVertices; i++){
			if(i != vertexNumber && mod((i+1), noOfVertices) != vertexNumber){
				provisionalResult += ((sign*polygon[i][0]*polygon[mod((i+1), noOfVertices)][1] - sign*polygon[mod((i+1), noOfVertices)][0]*polygon[i][1]));
			}
		}
		provisionalResult -= A0;
		
		resultVector[0] = ((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]))*provisionalResult*-1;
		resultVector[1] = ((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]))*provisionalResult*-1;
		
		Vector v = new DenseVector(resultVector); 
		System.out.println(v.toString());
		return v;
	}
	
	
	public Vector calculateLineTensionResultVector(double[][] polygon, int vertexNumber){
		double noOfVertices = polygon.length;
		int coeffizient_1 = 0, coeffizient_2 = 0;
		int sign = signManhattan(polygon[vertexNumber][0], polygon[mod((vertexNumber+1), noOfVertices)][0]);
		coeffizient_1 += sign;
		sign = signManhattan(polygon[vertexNumber][0], polygon[mod((vertexNumber-1), noOfVertices)][0]);
		coeffizient_1 += sign;		
		sign = signManhattan(polygon[vertexNumber][1], polygon[mod((vertexNumber+1), noOfVertices)][1]);
		coeffizient_2 += sign;
		sign = signManhattan(polygon[vertexNumber][1], polygon[mod((vertexNumber-1), noOfVertices)][1]);
		coeffizient_2 += sign;
		
		double[] resultVector = new double[]{coeffizient_1, coeffizient_2};
		printVector(resultVector);
		return new DenseVector(resultVector);
	}
	
	
	
	
	public int mod(double value, double base){
		return value%base < 0 ? (int)((value%base)+base) : (int)(value%base);
	}
	
	
	
	
	
	public static void main(String[] args){
		double [][] testPolygon = new double[][]{{1,1},{1,2},{2,2},{2,1}};
		MatrixCalculator calculator = new MatrixCalculator();
		calculator.calculatePerimeterMatrixForPolygon(testPolygon, 0);
		calculator.calculatePerimeterResultVector(testPolygon, 0);
		calculator.calculateLineTensionResultVector(testPolygon, 0);
		
	/*	CompRowMatrix A = new CompRowMatrix(new DenseMatrix(new double[][]{{1,1},{1,5}}));
		DenseVector x, b;

		
		
		b = new DenseVector(new double[]{3,15});
		x = new DenseVector(new double[]{0.8,0.8});
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
		
		System.out.println(x.toString());*/
		
		
		
	}
}
