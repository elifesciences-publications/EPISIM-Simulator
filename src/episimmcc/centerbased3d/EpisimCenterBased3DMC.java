package episimmcc.centerbased3d;

import sim.app.episim.model.biomechanics.centerbased3Dr.oldmodel.CenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3Dr.oldmodel.CenterBased3DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;


public class EpisimCenterBased3DMC extends EpisimModelConnector {
	
	private static final String ID = "2012-02-05";
	private static final String NAME = "Center Based 3D Biomechanical Model";
   
	private boolean hasCollision =false;
	private boolean isMembrane =false;
	private boolean isSurface = false;
	private double x;
	private double y;
	private double z;
		
	public EpisimCenterBased3DMC(){}
	
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
	
	@Hidden
	@NoExport
	public boolean isEpidermisDemoModel(){ return true; }
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModel> getEpisimBioMechanicalModelClass(){
		return CenterBased3DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return CenterBased3DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return CenterBased3DMechModelInit.class;
	}
	
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
	
	public double getZ() {	
		return z;
	}

	@Hidden
	public void setZ(double z) {	
		this.z = z;
	}
	
	public boolean getIsSurface(){
		return isSurface;
	}
	
	@Hidden
	public void setIsSurface(boolean isSurface){
		this.isSurface = isSurface;
	}
	
}
