package episimbiomechanics.hexagonbased;

import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.HexagonBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBasedMechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimHexagonBasedModelConnector extends EpisimModelConnector {
	
	private static final String ID = "2011-09-23";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model";
	
	private boolean isSpreadingFN = false;
	private boolean isSpreadingRGD = false;
	private boolean isProliferating = false;
	private boolean isRetractingToFN = false;
	private boolean isRetractingToRGD = false;
	private boolean isOnTestSurface = false;
	private boolean isAtSurfaceBorder = false;
	private boolean isRelaxing = false;
	private String chemotacticField="";
	
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
		return HexagonBasedMechanicalModelInitializer.class;
	}
	@NoExport
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
	
	public boolean getIsRetractingToFN(){		
		return isRetractingToFN;
	}	
	public void setIsRetractingToFN(boolean isRetractingToFN) {	
		this.isRetractingToFN = isRetractingToFN;
	}
	
	public boolean getIsRetractingToRGD(){		
		return isRetractingToRGD;
	}	
	public void setIsRetractingToRGD(boolean isRetractingToRGD) {	
		this.isRetractingToRGD = isRetractingToRGD;
	}
	
	public boolean getIsRelaxing(){		
		return isRelaxing;
	}	
	public void setIsRelaxing(boolean isRelaxing) {	
		this.isRelaxing = isRelaxing;
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
	
   public String getChemotacticField() {
   
   	return chemotacticField;
   }
	
   public void setChemotacticField(String chemotacticField) {
   
   	this.chemotacticField = chemotacticField;
   }	
}
