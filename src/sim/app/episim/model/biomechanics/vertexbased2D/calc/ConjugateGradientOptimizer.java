package sim.app.episim.model.biomechanics.vertexbased2D.calc;

import java.util.ArrayList;

import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModelGP;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.ContinuousVertexField;
import sim.app.episim.model.biomechanics.vertexbased2D.geom.Vertex;
import sim.app.episim.model.controller.ModelController;
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
	private VertexBasedModelGP globalParameters;
	public ConjugateGradientOptimizer(){
		
	}
	
	
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
		m = m.scale((globalParameters.getKappa()/2));
		return m;
	}
	
	public Matrix calculatePerimeterMatrixForPolygon(double[][] polygon, int vertexNumber){
		
		double[] coeffizients = getPerimeterCoeffizients(polygon, vertexNumber);
		
		double[][] resultMatrix = new double[][]{{Math.pow(coeffizients[0], 2), coeffizients[0]*coeffizients[1]},{coeffizients[0]*coeffizients[1], Math.pow(coeffizients[1], 2)}};
		
		Matrix m = new DenseMatrix(resultMatrix);
		
		m = m.scale(globalParameters.getGamma());
		
		return m;
	}
	
	//Based on euclidean distance squared
	public Matrix calculateLineTensionMatrixForPolygon(int numberOfConnectedVertices, boolean[] higherLambdaArray){	
		
		double resultFactor = 0;
		for(int i=0; i < higherLambdaArray.length; i++){
			if(higherLambdaArray[i]) resultFactor += 2*globalParameters.getLambda_high_factor();
			else resultFactor += 2*globalParameters.getLambda_low_factor();
		}	
		
		double[][] resultMatrix = new double[][]{{resultFactor, 0},
				                                   {0, resultFactor}};
		
		Matrix m = new DenseMatrix(resultMatrix);
		
		m = m.scale(globalParameters.getLambda());
		
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
	
	public Vector calculatePerimeterResultVector(double[][] polygon, int vertexNumber, double A0){
		double noOfVertices = polygon.length;
		double C0 = 2*Math.sqrt(Math.PI*A0)*globalParameters.getPref_perimeter_factor();
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
		resultVector[0] -= C0;
		resultVector[1] = resultVector[0];
		resultVector[0] *= (-1*coeffizients[0]);
		resultVector[1] *= (-1*coeffizients[1]);
	
		
		Vector v = new DenseVector(resultVector);
		
		v = v.scale(globalParameters.getGamma());
		
		return v;
	}
	
	
	private boolean[] calculateHigherLambdaArray(Vertex[] otherVertices, Vertex vertex){
		boolean[] result =  new boolean[otherVertices.length];
		for(int i = 0; i < otherVertices.length; i++) result[i] = shouldHaveAHigherLambda(vertex, otherVertices[i]);
			
		return result;
	}
	
	private boolean shouldHaveAHigherLambda(Vertex v1, Vertex v2){
		if(v1 != v2){
			return Math.abs(v1.getDoubleX()-v2.getDoubleX())< 7;
		}
		
		return false;
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
		v = v.scale(globalParameters.getKappa()/2);
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
	
	public Vector calculateLineTensionResultVector(Vertex[] connectedVertices, boolean[] higherLambdaArray){
		double result_x =0, result_y = 0;
		
		for(int i = 0; i< connectedVertices.length; i++){
			if(higherLambdaArray[i]){
				result_x -= 2*globalParameters.getLambda_high_factor()*connectedVertices[i].getDoubleX();
				result_y -= 2*globalParameters.getLambda_high_factor()*connectedVertices[i].getDoubleY();
			}
			else{
				result_x -= 2*globalParameters.getLambda_low_factor()*connectedVertices[i].getDoubleX();
				result_y -= 2*globalParameters.getLambda_low_factor()*connectedVertices[i].getDoubleY();
			}
		}		
		double[] resultVector = new double[]{result_x*-1, result_y*-1};
		Vector v = new DenseVector(resultVector);
		 v = v.scale(globalParameters.getLambda());
		return v;
	}
	
	
	public int mod(double value, double base){
		return value%base < 0 ? (int)((value%base)+base) : (int)(value%base);
	}
		
	public void relaxVertex(Vertex vertex){
		this.globalParameters = (VertexBasedModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		Matrix totalResultMatrix = new DenseMatrix(2,2);
		Vector totalResultVector = new DenseVector(2);				
		double [][] calculationPolygon;
		
		int vertexNumber = 0;		
		int transformedVertexId = -1;
		ArrayList<Double> preferredAreaList = new ArrayList<Double>();
		Vertex[][] relatedVertexArrays = getAllRelatedCellPolygonVertexArrays(vertex, preferredAreaList);
		
		int[] vertexIndexArray = getVertexIndexArray(relatedVertexArrays, vertex.getId());
		Vertex[][] transformedRelatedVertexArrays = ContinuousVertexField.getInstance().getMinDistanceTransformedRelatedVertexArraysGivenVertexReferenceSigned(relatedVertexArrays, vertex);
				
		for(int vertexArrayNumber = 0; vertexArrayNumber < transformedRelatedVertexArrays.length; vertexArrayNumber++){
			
			transformedVertexId = transformedRelatedVertexArrays[vertexArrayNumber][vertexIndexArray[vertexArrayNumber]].getId();
			boolean orderedCorrect = checkIfClockWise(transformedRelatedVertexArrays[vertexArrayNumber]);
			
			if(!orderedCorrect){			
				transformedRelatedVertexArrays[vertexArrayNumber] = invertVertexOrdering(transformedRelatedVertexArrays[vertexArrayNumber]);			
			}
			
			calculationPolygon = new double[transformedRelatedVertexArrays[vertexArrayNumber].length][2];
			
			for(int i = 0; i < transformedRelatedVertexArrays[vertexArrayNumber].length; i++){
				if(transformedRelatedVertexArrays[vertexArrayNumber][i].getId() == transformedVertexId) vertexNumber = i;
				calculationPolygon[i] = new double[]{transformedRelatedVertexArrays[vertexArrayNumber][i].getDoubleX(), transformedRelatedVertexArrays[vertexArrayNumber][i].getDoubleY()};
			}			
			
			totalResultMatrix = totalResultMatrix.add(calculateAreaMatrixForPolygon(calculationPolygon, vertexNumber));
			totalResultMatrix = totalResultMatrix.add(calculatePerimeterMatrixForPolygon(calculationPolygon, vertexNumber));
			
			totalResultVector = totalResultVector.add(calculateAreaResultVector(calculationPolygon, vertexNumber, preferredAreaList.get(vertexArrayNumber)));
			totalResultVector = totalResultVector.add(calculatePerimeterResultVector(calculationPolygon, vertexNumber, preferredAreaList.get(vertexArrayNumber)));
		}
		
		Vertex[] connectedVertices = vertex.getAllOtherVerticesConnectedToThisVertex();
			
		Vertex[] transformedConnectedVertices = ContinuousVertexField.getInstance().getMinDistanceTransformedRelatedVertexArrayGivenVertexReferenceSigned(connectedVertices, vertex);
				
		boolean[] higherLambdaArray = calculateHigherLambdaArray(transformedConnectedVertices, vertex);
		
		totalResultMatrix = totalResultMatrix.add(calculateLineTensionMatrixForPolygon(transformedConnectedVertices.length, higherLambdaArray)); 
		totalResultVector = totalResultVector.add(calculateLineTensionResultVector(transformedConnectedVertices, higherLambdaArray));
		
		
		Vector v = calculateOptimalResultWithConjugateGradient(totalResultMatrix, totalResultVector, vertex);
				
		if(v != null){
			vertex.setNewX(v.get(0));
			vertex.setNewY(v.get(1));
		}
		else{
			vertex.setNewX(vertex.getDoubleX());
			vertex.setNewY(vertex.getDoubleY());
		}	
	}
	
	private Vertex[][] getAllRelatedCellPolygonVertexArrays(Vertex vertex, ArrayList<Double> preferredAreaList){
		CellPolygon[] cells = vertex.getCellsJoiningThisVertex();
		Vertex[][] relatedVertexArrays = new Vertex[cells.length][];
		for(int i = 0; i < cells.length; i++){
			relatedVertexArrays[i] = cells[i].getSortedVertices();
			preferredAreaList.add(cells[i].getPreferredArea());
		}
		return relatedVertexArrays;
	}
	
	private int[] getVertexIndexArray(Vertex[][] relatedVertexArrays, int vertexId){
		int[] indexArray = new int[relatedVertexArrays.length];
		for(int i = 0; i < relatedVertexArrays.length; i++){
			for(int n = 0; n < relatedVertexArrays[i].length; n++){
				if(relatedVertexArrays[i][n] != null && relatedVertexArrays[i][n].getId() == vertexId){
					indexArray[i]= n;
					break;
				}
			}
		}
		return indexArray;
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
		//solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());

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
