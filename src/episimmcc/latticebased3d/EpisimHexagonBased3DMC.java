package episimmcc.latticebased3d;


import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModel;
import sim.app.episim.model.biomechanics.latticebased3d.LatticeBased3DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;


public class EpisimHexagonBased3DMC extends EpisimModelConnector{

	private static final String ID = "2012-01-16";
	private static final String NAME = "Hexagon Grid Based Biomechanical Model 3D";
	
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
		return LatticeBased3DModel.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return HexagonBased3DMechModelInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return LatticeBased3DModelGP.class;
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
		
	public String getChemotacticField(){	   
   	return this.chemotacticField;
   }
	
   public void setChemotacticField(String chemotacticField){   
   	if(chemotacticField != null)this.chemotacticField=chemotacticField;
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
