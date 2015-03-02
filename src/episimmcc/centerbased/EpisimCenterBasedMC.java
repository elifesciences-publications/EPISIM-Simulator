package episimmcc.centerbased;

import sim.app.episim.model.biomechanics.centerbased2Dr.oldmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2Dr.oldmodel.CenterBased2DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;



public class EpisimCenterBasedMC extends EpisimModelConnector {
	
	private static final String ID = "2010-05-13";
	private static final String NAME = "Center Based Biomechanical Model";
   
	private boolean hasCollision =false;
	private boolean isMembrane =false;
	private boolean isSurface = false;
	private double x;
	private double y;
		
	public EpisimCenterBasedMC(){}
	
	@Hidden
	@NoExport
	protected String getIdForInternalUse(){
		return ID;
	}
	
	@Hidden
	@NoExport
	public String getBiomechanicalModelName(){
		return NAME;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return CenterBased2DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return CenterBased2DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return CenterBasedMechModelInit.class;
	}
	
	@Hidden
	@NoExport
	public boolean isEpidermisDemoModel(){ return true; }
	
	public boolean getHasCollision() {
	
		return hasCollision;
	}
	
	@Hidden
	public void setHasCollision(boolean hasCollision){
		this.hasCollision = hasCollision;
	}
	
	public boolean getIsMembrane() {
		
		return isMembrane;
	}
	
	@Hidden
	public void setIsMembrane(boolean isMembrane){
		this.isMembrane = isMembrane;
	}

	
	public double getX() {	
		return x;
	}

	@Hidden
	public void setX(double x) {	
		this.x = x;
	}

	
	public double getY() {	
		return y;
	}

	@Hidden
	public void setY(double y) {	
		this.y = y;
	}
	
	public boolean getIsSurface(){
		return isSurface;
	}
	
	@Hidden
	public void setIsSurface(boolean isSurface){
		this.isSurface = isSurface;
	}
	
}
