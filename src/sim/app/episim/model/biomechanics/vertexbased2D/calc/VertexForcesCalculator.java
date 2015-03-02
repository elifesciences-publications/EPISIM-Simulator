package sim.app.episim.model.biomechanics.vertexbased2D.calc;

import sim.util.Double2D;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
/**
 * This class is not used and has to be fully updated (see Conjugate Gradient Optimizer) in case of recycling
 * @author Thomas
 *
 */

public class VertexForcesCalculator {
/*	
//	private static final double K = 0.01;
	//private static final double LAMBDA = 150;
//	private static final double GAMMA = 1;
	private static final double K = 650;
	private static final double LAMBDA = 15000;
	private static final double GAMMA = 100;

	
	private Vector calculateDynamicAreaForPolygon(double[][] polygon, int vertexNumber){
		
		double sign = testIfSignumChangeForAreaCalculation(polygon) ? -1 : 1;
		double noOfVertices = polygon.length;
		double [][] areaMatrix = new double[polygon[0].length][polygon[0].length];
		
		areaMatrix[0][0] = Math.pow(((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1])),2);
		areaMatrix[1][1] = Math.pow(((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0])),2);
		areaMatrix[0][1] = ((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]))* 
				((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]));
		areaMatrix[1][0] = ((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]))* 
				((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]));
			
		double[] result = new double[2];
		result[0] = (areaMatrix[0][0]*polygon[vertexNumber][0] + areaMatrix[0][1]*polygon[vertexNumber][1])*(K/2);
		result[1] = (areaMatrix[1][0]*polygon[vertexNumber][0] + areaMatrix[1][1]*polygon[vertexNumber][1])*(K/2);
		
		
		
		return new DenseVector(result);
	}
	
	private Vector calculateStaticAreaForPolygon(double[][] polygon, int vertexNumber, double A0){
	
			double sign = testIfSignumChangeForAreaCalculation(polygon) ? -1 : 1;
			double noOfVertices = polygon.length;
			double[] resultVector = new double[2];
			double provisionalResult = 0;
			
			for(int i = 0; i < noOfVertices; i++){
				if(i != vertexNumber && mod((i+1), noOfVertices) != vertexNumber){
					provisionalResult += ((sign*polygon[i][0]*polygon[mod((i+1), noOfVertices)][1] - sign*polygon[mod((i+1), noOfVertices)][0]*polygon[i][1]));
				}
			}
			provisionalResult -= (2*A0);
			
			resultVector[0] = ((sign*polygon[mod((vertexNumber+1), noOfVertices)][1])-(sign*polygon[mod((vertexNumber-1), noOfVertices)][1]))*provisionalResult*(K/2);
			resultVector[1] = ((sign*polygon[mod((vertexNumber-1), noOfVertices)][0])-(sign*polygon[mod((vertexNumber+1), noOfVertices)][0]))*provisionalResult*(K/2);
			return new DenseVector(resultVector);
		
	}
	
	private Vector calculatePerimeterForPolygon(double[][] polygon, int vertexNumber, double A0){
		double noOfVertices = polygon.length;
		
		double C0 = 2*Math.sqrt(Math.PI*A0)*VertexBasedMechanicalModelGlobalParameters.getInstance().getPref_perimeter_factor();
		
		double dynamicSquareRoots_1 = (Math.sqrt(
																Math.pow((polygon[vertexNumber][0]-polygon[mod((vertexNumber+1), noOfVertices)][0]),2)
															 + Math.pow((polygon[vertexNumber][1]-polygon[mod((vertexNumber+1), noOfVertices)][1]),2)
		
											  ));
		double dynamicSquareRoots_2 = (Math.sqrt(
																Math.pow((polygon[mod((vertexNumber-1), noOfVertices)][0]-polygon[vertexNumber][0]),2)
															 + Math.pow((polygon[mod((vertexNumber-1), noOfVertices)][1]-polygon[vertexNumber][1]),2)

												));
		
		double staticSquareRootsPerimeter =0;
		int startIndex = mod((vertexNumber+1), noOfVertices);
		int stopIndex = startIndex +(((int) noOfVertices)-2);
			
		for(int i = startIndex; i < stopIndex; i++){
			staticSquareRootsPerimeter += (Math.sqrt(
																	Math.pow((polygon[mod(i, noOfVertices)][0]-polygon[mod((i+1), noOfVertices)][0]),2)
																 + Math.pow((polygon[mod(i, noOfVertices)][1]-polygon[mod((i+1), noOfVertices)][1]),2)
														 ));
		}			
		
		
		double fraction_1_1 = ((polygon[vertexNumber][0]-polygon[mod((vertexNumber+1), noOfVertices)][0])/dynamicSquareRoots_1);
		double fraction_1_2 = ((polygon[mod((vertexNumber-1), noOfVertices)][0]-polygon[vertexNumber][0])/dynamicSquareRoots_2);
		
		double fraction_2_1 = ((polygon[vertexNumber][1]-polygon[mod((vertexNumber+1), noOfVertices)][1])/dynamicSquareRoots_1);
		double fraction_2_2 = ((polygon[mod((vertexNumber-1), noOfVertices)][1]-polygon[vertexNumber][1])/dynamicSquareRoots_2);
		
		double[] result = new double[2];
		
		result[0] =((staticSquareRootsPerimeter + dynamicSquareRoots_1 + dynamicSquareRoots_2 - C0)*(fraction_1_1 - fraction_1_2));
		result[1] =((staticSquareRootsPerimeter + dynamicSquareRoots_1 + dynamicSquareRoots_2 - C0)*(fraction_2_1 - fraction_2_2));
		result[0] *= GAMMA;
		result[1] *= GAMMA;
		
		return new DenseVector(result);
	}
	
	private Vector calculateLineTensionsForConnectedVertices(double[][] conVertices, double[] actVertex){
		double[] result = new double[2];
		for(int i = 0; i < conVertices.length; i++){
			double squareRoot = (Math.sqrt(
											Math.pow((actVertex[0]-conVertices[i][0]),2)
										 + Math.pow((actVertex[1]-conVertices[i][1]),2)
									 	));
			result[0] += ((actVertex[0]-conVertices[i][0])/squareRoot);
			result[1] += ((actVertex[1]-conVertices[i][1])/squareRoot);
		}
		result[0] *= LAMBDA;
		result[1] *= LAMBDA;
		return new DenseVector(result);
	}
	
	private int mod(double value, double base){
		return value%base < 0 ? (int)((value%base)+base) : (int)(value%base);
	}
	
	private boolean testIfSignumChangeForAreaCalculation(double polygon[][]){
		double area = 0;
		int noOfVertices = polygon.length;
		for(int n = 0; n < polygon.length; n++){
			area += (polygon[n%noOfVertices][0]*polygon[(n+1)%noOfVertices][1]- polygon[(n+1)%noOfVertices][0]*polygon[n%noOfVertices][1]);
		}
		area /= 2;
		return area < 0;
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
	
	private double[][] toDoubleArray(Vertex[] vertices){
		double[][] resultArray = new double[vertices.length][2];
		for(int i = 0; i < vertices.length; i++){
			resultArray[i] = new double[]{vertices[i].getDoubleX(), vertices[i].getDoubleY()};
		}
		return resultArray;
	}
	
	
	public double[] calculatedForcesActingOnVertex(Vertex vertex){
		double [][] calculationPolygon;
		Vertex[] polygonVertices;
		int vertexNumber = 0; 
		
		Vector totalResult = new DenseVector(2);
		
		for(CellPolygon pol : vertex.getCellsJoiningThisVertex()){
			polygonVertices = pol.getSortedVertices();
			boolean orderedCorrect = checkIfClockWise(polygonVertices);
			if(!orderedCorrect){
			
			polygonVertices = invertVertexOrdering(polygonVertices);
			
			}
			calculationPolygon = new double[polygonVertices.length][2];
			for(int i = 0; i < polygonVertices.length; i++){
				if(polygonVertices[i].equals(vertex)) vertexNumber = i;
				calculationPolygon[i] = new double[]{polygonVertices[i].getDoubleX(), polygonVertices[i].getDoubleY()};
			}			
			totalResult.add(calculateDynamicAreaForPolygon(calculationPolygon, vertexNumber));
			totalResult.add(calculateStaticAreaForPolygon(calculationPolygon, vertexNumber, pol.getPreferredArea()));
			totalResult.add(calculatePerimeterForPolygon(calculationPolygon, vertexNumber, pol.getPreferredArea()));		
		}
		
		Vertex[] connectedVertices = vertex.getAllOtherVerticesConnectedToThisVertex();
		totalResult.add(calculateLineTensionsForConnectedVertices(toDoubleArray(connectedVertices), new double[]{vertex.getDoubleX(), vertex.getDoubleY()}));
		
		
		return new double[]{totalResult.get(0), totalResult.get(1)};
	}
	
	/*public static void main(String[] args){
		VertexForcesCalculator calc  = new VertexForcesCalculator();
		CellPolygon pol = new CellPolygon();
		pol.setPreferredArea(1.0d);
		Vertex testVertex = new Vertex(1,1);
		pol.addVertex(testVertex);
		pol.addVertex(new Vertex(2,1));
		pol.addVertex(new Vertex(2,2));
		pol.addVertex(new Vertex(1,2));
			
		calc.calculatedForcesActingOnVertex(testVertex);
	}*/

}
