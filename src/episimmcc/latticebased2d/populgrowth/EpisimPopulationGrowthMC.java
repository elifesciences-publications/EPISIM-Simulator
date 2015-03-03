package episimmcc.latticebased2d.populgrowth;

import sim.app.episim.model.biomechanics.latticebased2d.LatticeBased2DModel;
import sim.app.episim.model.biomechanics.latticebased2d.demo.LatticeBased2DModelDemoGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector.Hidden;
import episimmcc.latticebased2d.EpisimLatticeBased2DMC;
import episimmcc.latticebased2d.bactmacro.EpisimBacteriaMacrophageModelInit;


public class EpisimPopulationGrowthMC extends EpisimLatticeBased2DMC {
	
	private static final String ID = "2012-11-11";
	private static final String NAME = "Episim Population Growth Test Model";
	
		
	@Hidden
	@NoExport
	protected String getIdForInternalUse() {
		return ID;
	}
	@Hidden
	@NoExport
	public String getBiomechanicalModelName() {
		return NAME;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return LatticeBased2DModel.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return EpisimPopulationGrowthInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return LatticeBased2DModelDemoGP.class;
	}
	
	public String getChemotacticField(){	   
   	return super.getChemotacticField();
   }
	
   public void setChemotacticField(String chemotacticField){   
   	super.setChemotacticField(chemotacticField);
   }
   
   public void setIsSpreading(boolean isSpreading){
		super.setIsSpreading(isSpreading);
	}
   
	public boolean getIsSpreading(){
		return super.getIsSpreading();
	}
	
	public void setIsRetracting(boolean isRetracting){
		super.setIsRetracting(isRetracting);
	}   
	public boolean getIsRetracting(){
		return super.getIsRetracting();
	}
	public void setLambdaChem(double lambdaChem){
		super.setLambdaChem(lambdaChem);
	}
	
	public double getLambdaChem(){
		return super.getLambdaChem();
	}
	
	public void setCellCellInteractionEnergy(double energy){
		super.setCellCellInteractionEnergy(energy);
	}
	
	public double getCellCellInteractionEnergy(){
		return super.getCellCellInteractionEnergy();
	}
	public boolean getIsProliferating(){		
		return super.getIsProliferating();
	}	
	public void setIsProliferating(boolean isProliferating) {	
		super.setIsProliferating(isProliferating);
	}	

}
