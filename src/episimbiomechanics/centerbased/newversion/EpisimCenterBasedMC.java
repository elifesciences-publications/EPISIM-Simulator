package episimbiomechanics.centerbased.newversion;

import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP;

public class EpisimCenterBasedMC extends EpisimModelConnector {
	
	private static final String ID = "2013-08-22";
	private static final String NAME = "New Center Based Biomechanical Model";
   
	private boolean hasCollision =false;
	private boolean isBasal =false;
	private boolean isSurface = false;
	private double x=0;
	private double y=0;
	private double width=0;
	private double height=0;
	
	private double adhesionStemCell=0;
	private double adhesionTACell=0;
	private double adhesionEarlySpinosumCell=0;
	private double adhesionLateSpinosumCell=0;
	private double adhesionGranulosumCell=0;
	private double adhesionCorneocyte=0;
	
	private String nameDiffLevelStemCell="";
	private String nameDiffLevelTACell="";
	private String nameDiffLevelEarlySpinosumCell="";
	private String nameDiffLevelLateSpinosumCell="";
	private String nameDiffLevelGranulosumCell="";
	private String nameDiffLevelCorneocyte="";
		
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
		return CenterBasedMechanicalModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return CenterBasedMechanicalModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return CenterBasedMechModelInit.class;
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

	
   public double getAdhesionStemCell() {
   
   	return adhesionStemCell;
   }

	
   public void setAdhesionStemCell(double adhesionStemCell) {
   
   	this.adhesionStemCell = adhesionStemCell;
   }

	
   public double getAdhesionTACell() {
   
   	return adhesionTACell;
   }

	
   public void setAdhesionTACell(double adhesionTACell) {
   
   	this.adhesionTACell = adhesionTACell;
   }

	
   public double getAdhesionEarlySpinosumCell() {
   
   	return adhesionEarlySpinosumCell;
   }

	
   public void setAdhesionEarlySpinosumCell(double adhesionEarlySpinosumCell) {
   
   	this.adhesionEarlySpinosumCell = adhesionEarlySpinosumCell;
   }

	
   public double getAdhesionLateSpinosumCell() {
   
   	return adhesionLateSpinosumCell;
   }

	
   public void setAdhesionLateSpinosumCell(double adhesionLateSpinosumCell) {
   
   	this.adhesionLateSpinosumCell = adhesionLateSpinosumCell;
   }

	
   public double getAdhesionGranulosumCell() {
   
   	return adhesionGranulosumCell;
   }

	
   public void setAdhesionGranulosumCell(double adhesionGranulosumCell) {
   
   	this.adhesionGranulosumCell = adhesionGranulosumCell;
   }

	
   public double getAdhesionCorneocyte() {
   
   	return adhesionCorneocyte;
   }

	
   public void setAdhesionCorneocyte(double adhesionCorneocyte) {
   
   	this.adhesionCorneocyte = adhesionCorneocyte;
   }

	
   public String getNameDiffLevelStemCell() {
   
   	return nameDiffLevelStemCell;
   }

	
   public void setNameDiffLevelStemCell(String nameDiffLevelStemCell) {
   
   	this.nameDiffLevelStemCell = nameDiffLevelStemCell;
   }

	
   public String getNameDiffLevelTACell() {
   
   	return nameDiffLevelTACell;
   }

	
   public void setNameDiffLevelTACell(String nameDiffLevelTACell) {
   
   	this.nameDiffLevelTACell = nameDiffLevelTACell;
   }

	
   public String getNameDiffLevelEarlySpinosumCell() {
   
   	return nameDiffLevelEarlySpinosumCell;
   }

	
   public void setNameDiffLevelEarlySpinosumCell(String nameDiffLevelEarlySpinosumCell) {
   
   	this.nameDiffLevelEarlySpinosumCell = nameDiffLevelEarlySpinosumCell;
   }

	
   public String getNameDiffLevelLateSpinosumCell() {
   
   	return nameDiffLevelLateSpinosumCell;
   }

	
   public void setNameDiffLevelLateSpinosumCell(String nameDiffLevelLateSpinosumCell) {
   
   	this.nameDiffLevelLateSpinosumCell = nameDiffLevelLateSpinosumCell;
   }

	
   public String getNameDiffLevelGranulosumCell() {
   
   	return nameDiffLevelGranulosumCell;
   }

	
   public void setNameDiffLevelGranulosumCell(String nameDiffLevelGranulosumCell) {
   
   	this.nameDiffLevelGranulosumCell = nameDiffLevelGranulosumCell;
   }

	
   public String getNameDiffLevelCorneocyte() {
   
   	return nameDiffLevelCorneocyte;
   }

	
   public void setNameDiffLevelCorneocyte(String nameDiffLevelCorneocyte) {
   
   	this.nameDiffLevelCorneocyte = nameDiffLevelCorneocyte;
   }
   
	@Hidden
	@NoExport
   public double getAdhesionFactorForDiffLevel(EpisimDifferentiationLevel diffLevel){
   	if(diffLevel.name().equals(getNameDiffLevelStemCell())) return getAdhesionStemCell();
   	else if(diffLevel.name().equals(getNameDiffLevelTACell())) return getAdhesionTACell();
   	else if(diffLevel.name().equals(getNameDiffLevelEarlySpinosumCell())) return getAdhesionEarlySpinosumCell();
   	else if(diffLevel.name().equals(getNameDiffLevelLateSpinosumCell())) return getAdhesionLateSpinosumCell();
   	else if(diffLevel.name().equals(getNameDiffLevelGranulosumCell())) return getAdhesionGranulosumCell();
   	else if(diffLevel.name().equals(getNameDiffLevelCorneocyte())) return getAdhesionCorneocyte();   	
   	return 0;
   }
		
}
