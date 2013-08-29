package episimbiomechanics.hexagonbased2d.singlesurface;

import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.demo.HexagonBasedMechanicalModelDemoGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimHexagonBased2DSingleSurfaceMC extends EpisimModelConnector {
	
	private static final String ID = "2012-07-09";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model Single Surface";
	
	private boolean isSpreading = false;
	private boolean isProliferating = false;
	private boolean isRetracting = false;
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
		return HexagonBasedMechanicalModel.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return HexagonBased2DMechModelSingleSurfaceInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return HexagonBasedMechanicalModelDemoGP.class;
	}
	
	public String getChemotacticField(){	   
   	return this.chemotacticField;
   }
	
   public void setChemotacticField(String chemotacticField){   
   	if(chemotacticField != null)this.chemotacticField=chemotacticField;
   }
   
   public void setIsSpreading(boolean isSpreading){
		this.isSpreading = isSpreading;
	}
   
	public boolean getIsSpreading(){
		return this.isSpreading;
	}
	
	public void setIsRetracting(boolean isRetracting){
		this.isRetracting = isRetracting;
	}
   
	public boolean getIsRetracting(){
		return this.isRetracting;
	}
	
	public boolean getIsProliferating(){		
		return this.isProliferating;
	}	
	public void setIsProliferating(boolean isProliferating) {	
		this.isProliferating = isProliferating;
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
