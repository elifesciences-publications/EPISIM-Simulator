package sim.app.episim.model.diffusion;

import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D.BoundaryCondition;


public class ExtracellularDiffusionFieldBCConfig3D extends ExtracellularDiffusionFieldBCConfig2D {
	private BoundaryCondition boundaryConditionZ = BoundaryCondition.DIRICHLET;
	private double constantValueZ = 0;
	private double constantFlowZ = 0;
	
	public ExtracellularDiffusionFieldBCConfig3D(){ super(); }
	public ExtracellularDiffusionFieldBCConfig3D(BoundaryCondition boundaryConditionX, BoundaryCondition boundaryConditionY, BoundaryCondition boundaryConditionZ,
																double constantValueX, double constantValueY, double constantValueZ,
																double constantFlowX, double constantFlowY, double constantFlowZ){
		super(boundaryConditionX, boundaryConditionY, constantValueX, constantValueY,constantFlowX, constantFlowY);
		this.boundaryConditionZ = boundaryConditionZ;
		this.constantValueZ = constantValueZ;
		this.constantFlowZ = constantFlowZ;
	}
	
	
   public BoundaryCondition getBoundaryConditionZ() {
   
   	return boundaryConditionZ;
   }
	
   public void setBoundaryConditionZ(BoundaryCondition boundaryConditionZ) {
   
   	this.boundaryConditionZ = boundaryConditionZ;
   }
	
   public double getConstantValueZ() {
   
   	return constantValueZ;
   }
	
   public void setConstantValueZ(double constantValueZ) {
   
   	this.constantValueZ = constantValueZ;
   }
	
   public double getConstantFlowZ() {
   
   	return constantFlowZ;
   }
	
   public void setConstantFlowZ(double constantFlowZ) {
   
   	this.constantFlowZ = constantFlowZ;
   }
}
