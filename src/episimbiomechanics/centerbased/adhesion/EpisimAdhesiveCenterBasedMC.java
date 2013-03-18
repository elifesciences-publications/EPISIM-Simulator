package episimbiomechanics.centerbased.adhesion;


import sim.app.episim.model.biomechanics.centerbased.adhesion.AdhesiveCenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.adhesion.AdhesiveCenterBasedMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;


public class EpisimAdhesiveCenterBasedMC extends EpisimModelConnector {
	
	private static final String ID = "2013-03-13";
	private static final String NAME = "Adhesive Center Based Biomechanical Model";
   
	private boolean hasCollision =false;
	private boolean isBasal =false;
	private boolean isMigratory=false;
	private boolean isSurface = false;
	private double x=0;
	private double y=0;
	private double width=0;
	private double height=0;
	
	private double adhesionBasalMembrane=1;
	private double adhesionBasalCell=1;
	private double adhesionSuprabasalCell=1;
	private double adhesionEarlySuprabasalCell=1;
	private double adhesionStemCell=1;
	
	private String nameDiffLevelBasalCell="";
	private String nameDiffLevelSuprabasalCell="";
	private String nameDiffLevelEarlySuprabasalCell="";
	private String nameDiffLevelStemCell="";
		
	public EpisimAdhesiveCenterBasedMC(){}
	
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
		return AdhesiveCenterBasedMechanicalModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return AdhesiveCenterBasedMechanicalModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return AdhesiveCenterBasedMechModelInit.class;
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
   public double getAdhesionStemCell() {
      
   	return adhesionStemCell;
   }	
   public void setAdhesionStemCell(double adhesionStemCell) {
   
   	this.adhesionStemCell = adhesionStemCell;
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

	
   public String getNameDiffLevelStemCell() {
   
   	return nameDiffLevelStemCell;
   }

	
   public void setNameDiffLevelStemCell(String nameDiffLevelStemCell) {
   
   	this.nameDiffLevelStemCell = nameDiffLevelStemCell;
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
