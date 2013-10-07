package episimbiomechanics.centerbased.newversion.epidermis;

import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;


public class EpisimCenterBasedMC extends episimbiomechanics.centerbased.newversion.EpisimCenterBasedMC {
	
	private static final String ID = "2013-08-22";
	private static final String NAME = "New Center Based Biomechanical Model";

	private double adhesionStemCell=0;
	private double adhesionTACell=0;
	private double adhesionBasalCell=0;
	private double adhesionSpinosumCell=0;
	private double adhesionGranulosumCell=0;
	private double adhesionCorneocyte=0;
		
	private String nameDiffLevelStemCell="";
	private String nameDiffLevelTACell="";
	private String nameDiffLevelBasalCell="";
	private String nameDiffLevelSpinosumCell="";
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

	
   public double getAdhesionBasalCell() {
   
   	return adhesionBasalCell;
   }

	
   public void setAdhesionBasalCell(double adhesionBasalCell) {
   
   	this.adhesionBasalCell = adhesionBasalCell;
   }

	
   public double getAdhesionSpinosumCell() {
   
   	return adhesionSpinosumCell;
   }

	
   public void setAdhesionSpinosumCell(double adhesionSpinosumCell) {
   
   	this.adhesionSpinosumCell = adhesionSpinosumCell;
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

	
   public String getNameDiffLevelBasalCell() {
   
   	return nameDiffLevelBasalCell;
   }

	
   public void setNameDiffLevelBasalCell(String nameDiffLevelBasalCell) {
   
   	this.nameDiffLevelBasalCell = nameDiffLevelBasalCell;
   }

	
   public String getNameDiffLevelSpinosumCell() {   
   	return nameDiffLevelSpinosumCell;
   }

	
   public void setNameDiffLevelSpinosumCell(String nameDiffLevelSpinosumCell) {   
   	this.nameDiffLevelSpinosumCell = nameDiffLevelSpinosumCell;
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
   public double getAdhesionFactorForCell(AbstractCell cell){
		EpisimDifferentiationLevel diffLevel = cell.getEpisimCellBehavioralModelObject().getDiffLevel();
   	if(diffLevel.name().equals(getNameDiffLevelStemCell())) return getAdhesionStemCell();
   	else if(diffLevel.name().equals(getNameDiffLevelTACell())) return getAdhesionTACell();
   	else if(diffLevel.name().equals(getNameDiffLevelBasalCell())) return getAdhesionBasalCell();
   	else if(diffLevel.name().equals(getNameDiffLevelSpinosumCell())) return getAdhesionSpinosumCell();
   	else if(diffLevel.name().equals(getNameDiffLevelGranulosumCell())) return getAdhesionGranulosumCell();
   	else if(diffLevel.name().equals(getNameDiffLevelCorneocyte())) return getAdhesionCorneocyte();   	
   	return 0;
   }
	
  	
}

