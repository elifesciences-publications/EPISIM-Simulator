package sim.app.episim.model.biomechanics.centerbased3D.newmodel.chemotaxis;

import episiminterfaces.NoUserModification;
import sim.app.episim.model.biomechanics.centerbased3D.newmodel.CenterBased3DMechanicalModelGP;


public class ChemotaxisCenterBased3DModelGP extends CenterBased3DMechanicalModelGP {
	private boolean continousDiffusionInXDirection = false;
	private boolean continousDiffusionInYDirection = false;
	private boolean continousDiffusionInZDirection = false;
	private boolean chemotaxisEnabled = false;
	private double tCellDensity=0.1;
	private double chemotaxisCellSizeDeltaFact = 1.25;
	
	public ChemotaxisCenterBased3DModelGP() {super();}
	
	@NoUserModification
   public boolean areDiffusionFieldsContinousInXDirection() {
		return isContinousDiffusionInXDirection();
   }

	@NoUserModification
   public boolean areDiffusionFieldsContinousInYDirection() {
	   return isContinousDiffusionInYDirection();
   }
	
	@NoUserModification
	public boolean areDiffusionFieldsContinousInZDirection() {	   
	   return isContinousDiffusionInZDirection();
   }
	
	public boolean isContinousDiffusionInXDirection() {
	   
   	return continousDiffusionInXDirection;
   }

	
   public void setContinousDiffusionInXDirection(boolean continousDiffusionInXDirection) {
   
   	this.continousDiffusionInXDirection = continousDiffusionInXDirection;
   }

	
   public boolean isContinousDiffusionInYDirection() {
   
   	return continousDiffusionInYDirection;
   }

	
   public void setContinousDiffusionInYDirection(boolean continousDiffusionInYDirection) {
   
   	this.continousDiffusionInYDirection = continousDiffusionInYDirection;
   }
   
   public boolean isContinousDiffusionInZDirection() {      
   	return continousDiffusionInZDirection;
   }

	
   public void setContinousDiffusionInZDirection(boolean continousDiffusionInZDirection) {   
   	this.continousDiffusionInZDirection = continousDiffusionInZDirection;
   }

	
   public double getTCellDensity() {
   
   	return tCellDensity;
   }

	
   public void setTCellDensity(double tCellDensity) {
   
   	this.tCellDensity = tCellDensity;
   }

	
   public boolean isChemotaxisEnabled() {
   
   	return chemotaxisEnabled;
   }

	
   public void setChemotaxisEnabled(boolean chemotaxisEnabled) {
   
   	this.chemotaxisEnabled = chemotaxisEnabled;
   }

	
   public double getChemotaxisCellSizeDeltaFact() {
   
   	return chemotaxisCellSizeDeltaFact;
   }

	
   public void setChemotaxisCellSizeDeltaFact(double chemotaxisCellSizeDeltaFact) {
   
   	this.chemotaxisCellSizeDeltaFact = chemotaxisCellSizeDeltaFact;
   }

}
