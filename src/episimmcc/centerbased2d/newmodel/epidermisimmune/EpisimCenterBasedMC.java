package episimmcc.centerbased2d.newmodel.epidermisimmune;

import java.util.HashMap;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.immunecells.ImmuneCellsCenterBased2DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.centerbased2d.newmodel.epidermisimmune.CenterBasedMechModelInit;


public class EpisimCenterBasedMC extends episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC {
	
	private static final String ID = "2017-03-12";
	private static final String NAME = "New Center Based Biomechanical Model - Immune Cells";
	
	private boolean isImmuneCell=false;
	
	private double scaleBMInfNeighImmCell=0.0;
	private double scaleRWImmCell=0.0;
	private double scaleRWDownBiasImmCell=0.0;	
	
	public EpisimCenterBasedMC(){}
	
	@NoExport
	public void resetPairwiseParameters(){
		//this.contactArea.clear();
	}
	
	@Hidden
	@NoExport
	protected String getIdForInternalUse(){
		return ID;
	}
	
	@Hidden
	@NoExport
	public String getBiomechanicalModelName(){
		return NAME;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return CenterBased2DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return ImmuneCellsCenterBased2DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return CenterBasedMechModelInit.class;
	}	
   
	public boolean getIsImmuneCell() {
		   return this.isImmuneCell;
	}
	
	public void setIsImmuneCell(boolean isImmuneCell) {
		  this.isImmuneCell = isImmuneCell;   
	}

	
   public double getScaleBMInfNeighImmCell() {
   
   	return scaleBMInfNeighImmCell;
   }

	
   public void setScaleBMInfNeighImmCell(double scaleBMInfNeighImmCell) {
   
   	this.scaleBMInfNeighImmCell = scaleBMInfNeighImmCell;
   }

	
   public double getScaleRWImmCell() {
   
   	return scaleRWImmCell;
   }

	
   public void setScaleRWImmCell(double scaleRWImmCell) {
   
   	this.scaleRWImmCell = scaleRWImmCell;
   }

	
   public double getScaleRWDownBiasImmCell() {
   
   	return scaleRWDownBiasImmCell;
   }

	
   public void setScaleRWDownBiasImmCell(double scaleRWDownBiasImmCell) {
   
   	this.scaleRWDownBiasImmCell = scaleRWDownBiasImmCell;
   }

	
   
}