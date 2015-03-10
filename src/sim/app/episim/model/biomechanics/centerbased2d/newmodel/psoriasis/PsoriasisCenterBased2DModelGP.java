package sim.app.episim.model.biomechanics.centerbased2d.newmodel.psoriasis;

import episiminterfaces.NoUserModification;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModelGP;


public class PsoriasisCenterBased2DModelGP extends CenterBased2DModelGP {
	
	private double reteRidgeGrowthInMikronPerSimStep = 0;
	
	private double maxBasalAmplitude_mikron = 145;
	private double initBasalAmplitude_mikron = 40;
	
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

}
