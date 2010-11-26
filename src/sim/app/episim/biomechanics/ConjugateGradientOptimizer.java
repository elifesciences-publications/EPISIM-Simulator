package sim.app.episim.biomechanics;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CGS;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.ICC;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;


public class ConjugateGradientOptimizer {
	
	
	private static final double K = 40;
	private static final double LAMBDA = 15000;
	private static final double GAMMA = 100;
	
	private boolean testIfSignumChangeForAreaCalculation(double polygon[][], int vertexNumber){
		double area = 0;
		int noOfVertices = polygon.length;
		for(int n = 0; n < polygon.length; n++){
			area += (polygon[n%noOfVertices][0]*polygon[(n+1)%noOfVertices][1]- polygon[(n+1)%noOfVertices][0]*polygon[n%noOfVertices][1]);
		}
		area /= 2;
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
		m = m.scale((K/2));
		return m;
	}
	
	public Matrix calculatePerimeterMatrixForPolygon(double[][] polygon, int vertexNumber){
		
		double[] coeffizients = getPerimeterCoeffizients(polygon, vertexNumber);
		
		double[][] resultMatrix = new double[][]{{Math.pow(coeffizients[0], 2), coeffizients[0]*coeffizients[1]},{coeffizients[0]*coeffizients[1], Math.pow(coeffizients[1], 2)}};
		
		Matrix m = new DenseMatrix(resultMatrix);
		
		m = m.scale(GAMMA);
		
		return m;
	}
	
	//Based on euclidean distance squared
	public Matrix calculateLineTensionMatrixForPolygon(int numberOfConnectedVertices){	
		
		double[][] resultMatrix = new double[][]{{2*numberOfConnectedVertices, 0},{0, 2*numberOfConnectedVertices}};
		
		Matrix m = new DenseMatrix(resultMatrix);
		
		m = m.scale(LAMBDA);
		
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
	
		
		Vector v = new DenseVector(resultVector);
		
		v= v.scale(GAMMA);
		
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
		provisionalResult -= (2*A0);
		
		resultVector[0] = ((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]))*provisionalResult*-1;
		resultVector[1] = ((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]))*provisionalResult*-1;
		
		Vector v = new DenseVector(resultVector);
		v = v.scale(K/2);
		return v;
	}
	
	
	/*public Vector calculateLineTensionResultVector(double[][] polygon, int vertexNumber){
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
		
		double[] resultVector = new double[]{coeffizient_1*-1, coeffizient_2*-1};
		Vector v = new DenseVector(resultVector);
		 v = v.scale(LAMBDA);
		return v;
	}*/
	
	public Vector calculateLineTensionResultVector(Vertex[] connectedVertices){
		double result_x =0, result_y = 0;
		
		for(Vertex v : connectedVertices){
			result_x -= 2*v.getDoubleX();
			result_y -= 2*v.getDoubleY();
		}
		
		double[] resultVector = new double[]{result_x*-1, result_y*-1};
		Vector v = new DenseVector(resultVector);
		 v = v.scale(LAMBDA);
		return v;
	}
	
	
	public int mod(double value, double base){
		return value%base < 0 ? (int)((value%base)+base) : (int)(value%base);
	}
	
	
	
	
	
	
	
	
public void relaxVertex(Vertex vertex){
		
		Matrix totalResultMatrix = new DenseMatrix(2,2);
		Vector totalResultVector = new DenseVector(2);
		
		
		double [][] calculationPolygon;
		Vertex[] polygonVertices;
		int vertexNumber = 0; 
		for(CellPolygon pol : vertex.getCellsJoiningThisVertex()){
			polygonVertices = pol.getSortedVerticesUsingTravellingSalesmanSimulatedAnnealing();
			boolean orderedCorrect = checkIfClockWise(polygonVertices);
			if(!orderedCorrect){
			
			polygonVertices = invertVertexOrdering(polygonVertices);
			
			}
			calculationPolygon = new double[polygonVertices.length][2];
			for(int i = 0; i < polygonVertices.length; i++){
				if(polygonVertices[i].equals(vertex)) vertexNumber = i;
				calculationPolygon[i] = new double[]{polygonVertices[i].getDoubleX(), polygonVertices[i].getDoubleY()};
			}			
			totalResultMatrix = totalResultMatrix.add(calculateAreaMatrixForPolygon(calculationPolygon, vertexNumber));
			totalResultMatrix = totalResultMatrix.add(calculatePerimeterMatrixForPolygon(calculationPolygon, vertexNumber));
			
			totalResultVector = totalResultVector.add(calculateAreaResultVector(calculationPolygon, vertexNumber, pol.getPreferredArea()));
			totalResultVector = totalResultVector.add(calculatePerimeterResultVector(calculationPolygon, vertexNumber));
		//
		}
		
		Vertex[] connectedVertices = vertex.getAllOtherVerticesConnectedToThisVertex();
		
		totalResultMatrix=totalResultMatrix.add(calculateLineTensionMatrixForPolygon(connectedVertices.length)); 
		totalResultVector = totalResultVector.add(calculateLineTensionResultVector(connectedVertices));
		
		
		Vector v = calculateOptimalResultWithConjugateGradient(totalResultMatrix, totalResultVector, vertex);
		vertex.setNewX(Math.round(v.get(0)));
		vertex.setNewY(Math.round(v.get(1)));
		
	//	System.out.println(x.toString());
		
		
		
	}
	
	private boolean checkIfClockWise(Vertex[] vertices){
		double areaTrapeze = 0;
		int n = vertices.length;
		
		for(int i = 0; i < n; i++){
			areaTrapeze += ((vertices[((i+1)%n)].getDoubleX() - vertices[(i%n)].getDoubleX())*(vertices[((i+1)%n)].getDoubleY() + vertices[(i%n)].getDoubleY()));
		}
		
		return areaTrapeze < 0;
	}
	
	private Vertex[] invertVertexOrdering(Vertex[] vertices){
		Vertex[] verticesNew = new Vertex[vertices.length];
		for(int i = 0, n= (vertices.length-1); i< verticesNew.length && n >=0; i++, n--){
			verticesNew[i] = vertices[n];
		}
		return verticesNew;
	}
	
	private Vector calculateOptimalResultWithConjugateGradient(Matrix matrix, Vector resultVector, Vertex vertex){
		CompRowMatrix A = new CompRowMatrix(matrix);
		Vector x, b;
	
		b = resultVector;
		x = new DenseVector(new double[]{vertex.getDoubleX(), vertex.getDoubleY()});
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
			
			 Vector result= solver.solve(A, b, x);
			// System.out.println("---------And the result is------------------");
			// System.out.println("("+Math.round(result.get(0))+" , "+ Math.round(result.get(1))+")");
			 return result;
		  
		} catch (IterativeSolverNotConvergedException e) {
		  System.err.println("Iterative solver failed to converge");
		}
		return null;
	}
	
	
}
