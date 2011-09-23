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

	public boolean getIsRetracting(){		
		return isRetracting;
	}	
	public void setIsRetracting(boolean isRetracting) {	
		this.isRetracting = isRetracting;
	}
	
}
