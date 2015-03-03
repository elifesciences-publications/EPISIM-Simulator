package episimmcc.centerbased3d.oldmodel.wound;


import sim.app.episim.model.biomechanics.centerbased3d.oldmodel.wound.AdhesiveCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.oldmodel.wound.AdhesiveCenterBased3DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;


public class EpisimAdhesiveCenterBased3DMC extends EpisimModelConnector {
	
	private static final String ID = "2013-03-27";
	private static final String NAME = "Adhesive Center Based 3D Biomechanical Model";
   
	private boolean hasCollision =false;
	private boolean isBasal =false;
	private boolean isMigratory=false;
	private boolean isSurface = false;
	private double x=0;
	private double y=0;
	private double z=0;
	private double width=0;
	private double height=0;
	private double length=0;
	
	private double adhesionBasalMembrane=1;
	private double adhesionBasalCell=1;
	private double adhesionSuprabasalCell=1;
	private double adhesionEarlySuprabasalCell=1;
	private double adhesionFastDividingCell=1;
	
	private String nameDiffLevelBasalCell="";
	private String nameDiffLevelSuprabasalCell="";
	private String nameDiffLevelEarlySuprabasalCell="";
	private String nameDiffLevelFastDividingCell="";
		
	public EpisimAdhesiveCenterBased3DMC(){}
	
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
		return AdhesiveCenterBased3DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return AdhesiveCenterBased3DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return AdhesiveCenterBased3DMechModelInit.class;
	}
	
	public boolean getHasCollision() {
	
		return hasCollision;
	}
	
	@Hidden
	public void setHasCollision(boolean hasCollision){
		this.hasCollision = hasCollision;
	}
	
	public boolean getIsBasal() {
		
		return isBasal;
	}
	
	@Hidden
	public void setIsBasal(boolean isBasal){
		this.isBasal = isBasal;
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
	
	public double getWidth() {	
		return width;
	}
	public void setWidth(double width) {	
		this.width = width;
	}
	
	public double getHeight() {	
		return height;
	}
	public void setHeight(double height) {	
		this.height = height;
	}
	public double getLength() {	
		return length;
	}
	public void setLength(double length) {	
		this.length = length;
	}	
	public boolean getIsMigratory() {	
		return isMigratory;
	}
	public void setIsMigratory(boolean isMigratory) {	
		this.isMigratory = isMigratory;
	}	
   public double getAdhesionBasalMembrane() {
   
   	return adhesionBasalMembrane;
   }	
   public void setAdhesionBasalMembrane(double adhesionBasalMembrane) {
   
   	this.adhesionBasalMembrane = adhesionBasalMembrane;
   }	
   public double getAdhesionBasalCell() {
   
   	return adhesionBasalCell;
   }	
   public void setAdhesionBasalCell(double adhesionBasalCell) {
   
   	this.adhesionBasalCell = adhesionBasalCell;
   }	
   public double getAdhesionSuprabasalCell() {
   
   	return adhesionSuprabasalCell;
   }	
   public void setAdhesionSuprabasalCell(double adhesionSuprabasalCell) {
   
   	this.adhesionSuprabasalCell = adhesionSuprabasalCell;
   }
   public double getAdhesionFastDividingCell() {
      
   	return adhesionFastDividingCell;
   }	
   public void setAdhesionFastDividingCell(double adhesionFastDividingCell) {
   
   	this.adhesionFastDividingCell = adhesionFastDividingCell;
   }

	
   public String getNameDiffLevelBasalCell() {
   
   	return nameDiffLevelBasalCell;
   }

	
   public void setNameDiffLevelBasalCell(String nameDiffLevelBasalCell) {
   
   	this.nameDiffLevelBasalCell = nameDiffLevelBasalCell;
   }

	
   public String getNameDiffLevelSuprabasalCell() {
   
   	return nameDiffLevelSuprabasalCell;
   }

	
   public void setNameDiffLevelSuprabasalCell(String nameDiffLevelSuprabasalCell) {
   
   	this.nameDiffLevelSuprabasalCell = nameDiffLevelSuprabasalCell;
   }

	
   public String getNameDiffLevelFastDividingCell() {
   
   	return nameDiffLevelFastDividingCell;
   }

	
   public void setNameDiffLevelFastDividingCell(String nameDiffLevelFastDividingCell) {
   
   	this.nameDiffLevelFastDividingCell = nameDiffLevelFastDividingCell;
   }

	
   public String getNameDiffLevelEarlySuprabasalCell() {
   
   	return nameDiffLevelEarlySuprabasalCell;
   }

	
   public void setNameDiffLevelEarlySuprabasalCell(String nameDiffLevelEarlySuprabasalCell) {
   
   	this.nameDiffLevelEarlySuprabasalCell = nameDiffLevelEarlySuprabasalCell;
   }

	
   public double getAdhesionEarlySuprabasalCell() {
   
   	return adhesionEarlySuprabasalCell;
   }

	
   public void setAdhesionEarlySuprabasalCell(double adhesionEarlySuprabasalCell) {
   
   	this.adhesionEarlySuprabasalCell = adhesionEarlySuprabasalCell;
   }
	
}

