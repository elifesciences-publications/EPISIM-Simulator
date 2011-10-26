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
	
	private boolean isSpreading = false;
	private boolean isProliferating = false;
	private boolean isRetracting = false;
	private boolean isSpreadingPossible = true;
	private boolean isOnTestSurface = false;
	private boolean isAtLeftSideSurfaceBorder = false;
	private boolean isAtRightSideSurfaceBorder = false;

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
	
	public void setIsSpreading(boolean isSpreading){
		this.isSpreading = isSpreading;
	}	
	public boolean getIsSpreading(){
		return this.isSpreading;
	}
	
	public boolean getIsProliferating(){		
		return isProliferating;
	}	
	public void setIsProliferating(boolean isProliferating) {	
		this.isProliferating = isProliferating;
	}
	
	
	public boolean getIsSpreadingPossible(){
		return isSpreadingPossible;
	}
	
	@Hidden
	public void setIsSpreadingPossible(boolean isSpreadingPossible){
		this.isSpreadingPossible = isSpreadingPossible;
	}	

	public boolean getIsRetracting(){		
		return isRetracting;
	}	
	public void setIsRetracting(boolean isRetracting) {	
		this.isRetracting = isRetracting;
	}
	
   public boolean getIsOnTestSurface() {
   
   	return isOnTestSurface;
   }
	
	@Hidden
   public void setIsOnTestSurface(boolean isOnTestSurface) {
   
   	this.isOnTestSurface = isOnTestSurface;
   }
	
   public boolean getIsAtLeftSideSurfaceBorder() {
   
   	return isAtLeftSideSurfaceBorder;
   }
	
   @Hidden
   public void setIsAtLeftSideSurfaceBorder(boolean isAtLeftSideSurfaceBorder) {
   
   	this.isAtLeftSideSurfaceBorder = isAtLeftSideSurfaceBorder;
   }
	
   public boolean getIsAtRightSideSurfaceBorder() {
   
   	return isAtRightSideSurfaceBorder;
   }
   @Hidden
   public void setIsAtRightSideSurfaceBorder(boolean isTheRightSideSurfaceBorder) {
   
   	this.isAtRightSideSurfaceBorder = isTheRightSideSurfaceBorder;
   }
	
}
