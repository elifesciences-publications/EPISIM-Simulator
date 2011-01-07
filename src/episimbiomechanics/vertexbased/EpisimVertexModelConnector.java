package episimbiomechanics.vertexbased;

import episimbiomechanics.EpisimModelConnector;


public class EpisimVertexModelConnector extends EpisimModelConnector {

	private static final String VERSION = "2011-01-07";
	private static final String NAME = "Vertex Based Biomechanical Model";
   
	private double contractility =1;
	private double adhesion =2;
	private double elasticity =3;
	
	
	public String getBiomechanicalModelId(){
		return VERSION;
	}
	
	public String getBiomechanicalModelName(){
		return NAME;
	}
	
	public EpisimVertexModelConnector(){}
	
	public double getContractility() {
	
		return contractility;
	}
	
	public void setContractility(double contractility) {
	
		//System.out.println("Contracility was set to "+ contractility);
		this.contractility = contractility;
	}
	
	public double getAdhesion() {
	
		return adhesion;
	}
	
	public void setAdhesion(double adhesion) {
	
		this.adhesion = adhesion;
	}
	
	public double getElasticity() {
	
		return elasticity;
	}
	
	public void setElasticity(double elasticity) {
	
		this.elasticity = elasticity;
	}
	


}
