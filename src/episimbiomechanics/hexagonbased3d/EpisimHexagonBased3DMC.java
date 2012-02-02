package episimbiomechanics.hexagonbased3d;


import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased3d.HexagonBased3DMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.HexagonBased3DMechModelInit;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimHexagonBased3DMC extends EpisimModelConnector{

	private static final String ID = "2012-01-16";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model 3D";
	
	private boolean isSpreading = false;
	private boolean isProliferating = false;
	private boolean isRetracting = false;
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
		return HexagonBased3DMechanicalModel.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return HexagonBased3DMechModelInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return HexagonBased3DMechanicalModelGP.class;
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
	
	public boolean getIsRelaxing(){		
		return isRelaxing;
	}	
	public void setIsRelaxing(boolean isRelaxing) {	
		this.isRelaxing = isRelaxing;
	}
	
   public String getChemotacticField() {
   
   	return chemotacticField;
   }
	
   public void setChemotacticField(String chemotacticField) {
   
   	this.chemotacticField = chemotacticField;
   }		
}
