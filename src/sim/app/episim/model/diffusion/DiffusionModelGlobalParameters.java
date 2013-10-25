package sim.app.episim.model.diffusion;

import episiminterfaces.NoUserModification;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.ObjectManipulations;


public class DiffusionModelGlobalParameters implements java.io.Serializable{
	
	private static DiffusionModelGlobalParameters instance=null;
	private static DiffusionModelGlobalParameters resetinstance = null;
	
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
	
	
	
	private DiffusionModelGlobalParameters(){
		
	}
	
	/**
	 * This method is intended to be used exclusively by the ExtraCellularDiffusionController
	 */
	@NoUserModification
	public synchronized static DiffusionModelGlobalParameters getInstance(){
		if(instance==null){
			if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
				instance = new DiffusionModelGlobalParameters();
				resetinstance = new DiffusionModelGlobalParameters();
			}
			else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
				instance = new DiffusionModelGlobalParameters3D();
				resetinstance = new DiffusionModelGlobalParameters3D();
			}
		}
		return instance;
	}
	
	public void reloadDiffusionModelGlobalParametersObject(DiffusionModelGlobalParameters parametersObject){
		if(parametersObject != null){
			resetinstance = parametersObject;
			ObjectManipulations.resetInitialGlobalValues(instance, resetinstance);
		}
	}
	
	public void resetInitialGlobalValues(){
		ObjectManipulations.resetInitialGlobalValues(instance, resetinstance);
	}
	public void classLoaderHasChanged() {
	   instance = null;
   }
	
	public static class DiffusionModelGlobalParameters3D extends DiffusionModelGlobalParameters{
		private BoundaryCondition boundaryConditionZ = BoundaryCondition.DIRICHLET;
		private double constantValueZ = 0;
		private double constantFlowZ = 0;
		protected DiffusionModelGlobalParameters3D(){}
		
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
