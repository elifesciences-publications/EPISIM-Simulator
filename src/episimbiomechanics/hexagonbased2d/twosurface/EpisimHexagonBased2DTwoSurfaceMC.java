package episimbiomechanics.hexagonbased2d.twosurface;

import sim.app.episim.model.biomechanics.hexagonbased.twosurface.HexagonBasedMechanicalModelTwoSurface;
import sim.app.episim.model.biomechanics.hexagonbased.twosurface.HexagonBasedMechanicalModelTwoSurfaceGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimHexagonBased2DTwoSurfaceMC extends EpisimModelConnector {
	
	private static final String ID = "2011-09-23";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model Two Surface";
	
	private boolean isSpreadingFN = false;
	private boolean isSpreadingRGD = false;
	private boolean isProliferating = false;
	private boolean isRetractingToFN = false;
	private boolean isRetractingToRGD = false;
	private boolean isOnTestSurface = false;
	private boolean isAtSurfaceBorder = false;
	private boolean isRelaxing = false;
	private String chemotacticField="";
	private double cellCellInteractionEnergy = 0.7;
	private double lambdaChem = 1;
	
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
		return HexagonBasedMechanicalModelTwoSurface.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return HexagonBased2DMechModelTwoSurfaceInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return HexagonBasedMechanicalModelTwoSurfaceGP.class;
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
   
   public double getCellCellInteractionEnergy() {
	   return cellCellInteractionEnergy;
	}
	   
	   
	public void setCellCellInteractionEnergy(double cellCellInteractionEnergy) {
	   if(cellCellInteractionEnergy >= 0)this.cellCellInteractionEnergy = cellCellInteractionEnergy;
	}
	
	 public double getLambdaChem() {
		   
		 return lambdaChem;
	 }

		
	 public void setLambdaChem(double lambdaChem) {	   
	   	this.lambdaChem = lambdaChem;
	 }
}
