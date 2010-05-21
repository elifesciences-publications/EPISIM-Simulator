package episimbiomechanics;

import episiminterfaces.EpisimMechanicalModel;


public class EpisimModelIntegrator implements EpisimMechanicalModel{
	
	private static final String VERSION = "2010-05-13";
	
	private double contractility =0;
	private double adhesion =0;
	private double elasticity =0;
	
	
	public String getBiomechanicalModelId(){
		return VERSION;
	}
	
	public EpisimModelIntegrator(){}
	
	public double getContractility() {
	
		return contractility;
	}
	
	public void setContractility(double contractility) {
	
		this.contractility = contractility;
	}
	
	public double getAdhesion() {
	
		return adhesion;
	}
	
	public void setAdhesion(double adhesion) {
	
		this.adhesion = adhesion;
	}
	
	public double getElasicity() {
	
		return elasticity;
	}
	
	public void setElasticity(double elasticity) {
	
		this.elasticity = elasticity;
	}
	
	
}
