package episimbiomechanics.hexagonbased2d.singlesurface.bactmacro;

import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelSingleSurface;
import sim.app.episim.model.biomechanics.hexagonbased.singlesurface.HexagonBasedMechanicalModelSingleSurfaceGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episimbiomechanics.hexagonbased2d.singlesurface.EpisimHexagonBased2DSingleSurfaceMC;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimBacteriaMacrophageModelMC extends EpisimHexagonBased2DSingleSurfaceMC {
	
	private static final String ID = "2012-07-05";
	private static final String NAME = "Simple Hexagon Grid Based Biomechanical Bacteria Macrophage Model";
	
		
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
		return HexagonBasedMechanicalModelSingleSurface.class;
	}
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return EpisimBacteriaMacrophageModelInit.class;
	}
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return HexagonBasedMechanicalModelSingleSurfaceGP.class;
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