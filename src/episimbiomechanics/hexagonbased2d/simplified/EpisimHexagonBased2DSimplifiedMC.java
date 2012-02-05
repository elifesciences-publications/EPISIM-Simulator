package episimbiomechanics.hexagonbased2d.simplified;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBased2DMechModelSimpleInit;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episimbiomechanics.hexagonbased2d.EpisimHexagonBased2DMC;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimHexagonBased2DSimplifiedMC extends EpisimHexagonBased2DMC {
	
	private static final String ID = "2012-02-02";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model Simplified";
	
		
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
		return HexagonBasedMechanicalModel.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return HexagonBased2DMechModelSimpleInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return HexagonBasedMechanicalModelGP.class;
	}
	
	public String getChemotacticField(){	   
   	return super.getChemotacticField();
   }
	
   public void setChemotacticField(String chemotacticField){   
   	super.setChemotacticField(chemotacticField);
   }
   
   public void setIsSpreading(boolean isSpreading){
		super.setIsSpreadingFN(isSpreading);
	}
   
	public boolean getIsSpreading(){
		return super.getIsSpreadingFN();
	}
	
	public void setIsRetracting(boolean isRetracting){
		super.setIsRetractingToFN(isRetracting);
	}
   
	public boolean getIsRetracting(){
		return super.getIsRetractingToFN();
	}
	
	public boolean getIsProliferating(){		
		return super.getIsProliferating();
	}	
	public void setIsProliferating(boolean isProliferating) {	
		super.setIsProliferating(isProliferating);
	}	

}