package sim.app.episim.model.biomechanics.centerbased2d.newmodel.psoriasis;

import episiminterfaces.NoUserModification;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModelGP;


public class PsoriasisCenterBased2DModelGP extends CenterBased2DModelGP {
	
	private double reteRidgeGrowthInMikronPerSimStep = 0;
	
	private double maxBasalAmplitude_mikron = 145;
	private double initBasalAmplitude_mikron = 40;
	private double immuneCellYDelta_mikron = 50;
	private double immuneCellYDeltaConstProportion = 0.8;
	private double immuneCellDensity = 0.5;
	private boolean addImmuneCells = true;
	private double width = 600;
	private long immuneCellSeed = 15;
	
	
	public void newSimStep(){
		if(reteRidgeGrowthInMikronPerSimStep > 0){
			if(getBasalAmplitude_mikron() < maxBasalAmplitude_mikron){ 
				this.setBasalAmplitude_mikron(getBasalAmplitude_mikron()+reteRidgeGrowthInMikronPerSimStep);
				this.setBasalYDelta_mikron(getBasalYDelta_mikron()-reteRidgeGrowthInMikronPerSimStep);
			}
		}
	}
	
   public double getReteRidgeGrowthInMikronPerSimStep() {   
   	return reteRidgeGrowthInMikronPerSimStep;
   }	
   public void setReteRidgeGrowthInMikronPerSimStep(double reteRidgeGrowthInMikronPerSimStep) {   
   	this.reteRidgeGrowthInMikronPerSimStep = reteRidgeGrowthInMikronPerSimStep > 0 ? reteRidgeGrowthInMikronPerSimStep : 0;
   }
	
   public double getMaxBasalAmplitude_mikron() {   
   	return maxBasalAmplitude_mikron;
   }

	
   public void setMaxBasalAmplitude_mikron(double maxBasalAmplitude_mikron) {   
   	this.maxBasalAmplitude_mikron = maxBasalAmplitude_mikron > getBasalAmplitude_mikron() ? maxBasalAmplitude_mikron : getBasalAmplitude_mikron();
   }

	
   public double getInitBasalAmplitude_mikron() {
   
   	return initBasalAmplitude_mikron;
   }

	
   public void setInitBasalAmplitude_mikron(double initBasalAmplitude_mikron) {
   
   	this.initBasalAmplitude_mikron = initBasalAmplitude_mikron;
   }

	
   public double getImmuneCellYDelta_mikron() {
   
   	return immuneCellYDelta_mikron;
   }

	
   public void setImmuneCellYDelta_mikron(double immuneCellYDelta_mikron) {
   
   	this.immuneCellYDelta_mikron = immuneCellYDelta_mikron;
   }

	
   public double getImmuneCellYDeltaConstProportion() {
   
   	return immuneCellYDeltaConstProportion;
   }

	
   public void setImmuneCellYDeltaConstProportion(double immuneCellYDeltaConstProportion) {
   
   	this.immuneCellYDeltaConstProportion = immuneCellYDeltaConstProportion>=0 && immuneCellYDeltaConstProportion <=1 ? immuneCellYDeltaConstProportion : this.immuneCellYDeltaConstProportion;
   }

	
   public boolean isAddImmuneCells() {
   
   	return addImmuneCells;
   }

	
   public void setAddImmuneCells(boolean addImmuneCells) {
   
   	this.addImmuneCells = addImmuneCells;
   }

	
   public double getImmuneCellDensity() {
   
   	return immuneCellDensity;
   }

	
   public void setImmuneCellDensity(double immuneCellDensity) {
   
   	this.immuneCellDensity = immuneCellDensity>0 && immuneCellDensity <=1 ? immuneCellDensity : this.immuneCellDensity;
   }
   
   public void setWidthInMikron(double val) {
		if(val > 0)	width = val;
	}	
	public double getWidthInMikron() {
		return width;
	}

	
   public long getImmuneCellSeed() {
   
   	return immuneCellSeed;
   }

	
   public void setImmuneCellSeed(long immuneCellSeed) {
   
   	this.immuneCellSeed = immuneCellSeed;
   }

}
