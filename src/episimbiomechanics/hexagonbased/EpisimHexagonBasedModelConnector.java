package episimbiomechanics.hexagonbased;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBasedMechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;


public class EpisimHexagonBasedModelConnector extends EpisimModelConnector {
	
	private static final String ID = "2011-09-23";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model";
	
	private boolean isSpreadingFN = false;
	private boolean isSpreadingRGD = false;
	private boolean isProliferating = false;
	private boolean isRetractingFN = false;
	private boolean isRetractingRGD = false;
	private boolean isOnTestSurface = false;
	private boolean isAtSurfaceBorder = false;

	protected String getIdForInternalUse() {
		return ID;
	}
	public String getBiomechanicalModelName() {
		return NAME;
	}

	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return HexagonBasedMechanicalModel.class;
	}

	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return HexagonBasedMechanicalModelInitializer.class;
	}

	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return HexagonBasedMechanicalModelGlobalParameters.class;
	}
	
	public void setIsSpreadingFN(boolean isSpreadingFN){
		this.isSpreadingFN = isSpreadingFN;
	}	
	public boolean getIsSpreadingFN(){
		return this.isSpreadingFN;
	}
	
	public void setIsSpreadingRGD(boolean isSpreadingRGD){
		this.isSpreadingRGD = isSpreadingRGD;
	}	
	public boolean getIsSpreadingRGD(){
		return this.isSpreadingRGD;
	}
	
	public boolean getIsProliferating(){		
		return isProliferating;
	}	
	public void setIsProliferating(boolean isProliferating) {	
		this.isProliferating = isProliferating;
	}	
	
	public boolean getIsRetractingFN(){		
		return isRetractingFN;
	}	
	public void setIsRetractingFN(boolean isRetractingFN) {	
		this.isRetractingFN = isRetractingFN;
	}
	
	public boolean getIsRetractingRGD(){		
		return isRetractingRGD;
	}	
	public void setIsRetractingRGD(boolean isRetractingRGD) {	
		this.isRetractingRGD = isRetractingRGD;
	}
	
   public boolean getIsOnTestSurface() {   
   	return isOnTestSurface;
   }
	
	@Hidden
   public void setIsOnTestSurface(boolean isOnTestSurface) {   
   	this.isOnTestSurface = isOnTestSurface;
   }
	
   public boolean getIsAtSurfaceBorder() {   
   	return isAtSurfaceBorder;
   }
	
   @Hidden
   public void setIsAtSurfaceBorder(boolean isAtSurfaceBorder){   
   	this.isAtSurfaceBorder = isAtSurfaceBorder;
   }	
}
