package sim.app.episim.model.diffusion;


public class ExtracellularDiffusionFieldBCConfig2D {
	public enum BoundaryCondition{
		DIRICHLET("Dirichlet"),
		NEUMANN("Neumann"),
		PERIODIC("Periodic");
		private String name ="";
		private BoundaryCondition(String name){
			this.name = name;
		}		
		public String toString(){ return this.name;}
	}
	
	private BoundaryCondition boundaryConditionX = BoundaryCondition.DIRICHLET;
	private BoundaryCondition boundaryConditionY = BoundaryCondition.DIRICHLET;
	
	private double constantValueX = 0;
	private double constantValueY = 0;
	
	private double constantFlowX = 0;
	private double constantFlowY = 0;
	
	public ExtracellularDiffusionFieldBCConfig2D(){}
	
	public ExtracellularDiffusionFieldBCConfig2D(BoundaryCondition boundaryConditionX, BoundaryCondition boundaryConditionY,
																double constantValueX, double constantValueY,
																double constantFlowX, double constantFlowY){
		this.boundaryConditionX = boundaryConditionX;
		this.boundaryConditionY = boundaryConditionY;
		this.constantValueX = constantValueX;
		this.constantValueY = constantValueY;
		this.constantFlowX = constantFlowX;
		this.constantFlowY = constantFlowY;
	}
	
   public BoundaryCondition getBoundaryConditionX() {   
   	return boundaryConditionX;
   }
	
   public void setBoundaryConditionX(BoundaryCondition boundaryConditionX) {   
   	this.boundaryConditionX = boundaryConditionX;
   }
	
   public BoundaryCondition getBoundaryConditionY() {   
   	return boundaryConditionY;
   }
	
   public void setBoundaryConditionY(BoundaryCondition boundaryConditionY) {   
   	this.boundaryConditionY = boundaryConditionY;
   }
	
   public double getConstantValueX() {   
   	return constantValueX;
   }
	
   public void setConstantValueX(double constantValueX) {   
   	this.constantValueX = constantValueX;
   }
	
   public double getConstantValueY() {   
   	return constantValueY;
   }
	
   public void setConstantValueY(double constantValueY) {   
   	this.constantValueY = constantValueY;
   }
	
   public double getConstantFlowX() {   
   	return constantFlowX;
   }
	
   public void setConstantFlowX(double constantFlowX) {
   
   	this.constantFlowX = constantFlowX;
   }
	
   public double getConstantFlowY() {   
   	return constantFlowY;
   }
	
   public void setConstantFlowY(double constantFlowY) {   
   	this.constantFlowY = constantFlowY;
   }
}
