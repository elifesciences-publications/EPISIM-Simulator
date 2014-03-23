package episimbiomechanics.centerbased3d.newversion.epidermis;

import java.util.HashMap;

import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episimbiomechanics.EpisimModelConnector.Pairwise;
import episimbiomechanics.centerbased.newversion.epidermis.CenterBasedMechModelInit;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;

public class EpisimEpidermisCenterBased3DMC extends episimbiomechanics.centerbased3d.newversion.EpisimCenterBased3DMC {
	
	private static final String ID = "2014-03-12";
	private static final String NAME = "New Epidermis Center Based Biomechanical 3D Model";

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
	private HashMap<Long, Double> contactArea = new HashMap<Long, Double>();
	
	private double cellVolume = 0;
	private double extCellSpaceVolume = 0;
	private double extCellSpaceMikron = 0.2d;
	private double cellSurfaceArea=0;
	
	public EpisimEpidermisCenterBased3DMC(){}
	
	@NoExport
	public void resetPairwiseParameters(){
		this.contactArea.clear();
	}
	
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
   @Pairwise
   @NoExport
   public void setContactArea(long cellId, double contactArea){
   	this.contactArea.put(cellId, contactArea);
   }
   @Pairwise
   @NoExport
   public double getContactArea(long cellId){
   	return this.contactArea.containsKey(cellId)? this.contactArea.get(cellId):0;
   }
   
   @Hidden
   public void setContactArea(HashMap<Long, Double> contactArea){
   	if(contactArea != null) this.contactArea = contactArea;
   }
   
   @Hidden
   public HashMap<Long, Double> getContactArea(){
   	return this.contactArea;
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

	
   public double getCellVolume() {   
   	return cellVolume;
   }

   @Hidden
   public void setCellVolume(double cellVolume) {
   
   	this.cellVolume = cellVolume;
   }

	
   public double getExtCellSpaceVolume() {
   
   	return extCellSpaceVolume;
   }

   @Hidden
   public void setExtCellSpaceVolume(double extCellSpaceVolume) {
   
   	this.extCellSpaceVolume = extCellSpaceVolume;
   }

	
   public double getExtCellSpaceMikron() {
   
   	return extCellSpaceMikron;
   }

	
   public void setExtCellSpaceMikron(double extCellSpaceMikron) {
   
   	this.extCellSpaceMikron = extCellSpaceMikron;
   }

	
   public double getCellSurfaceArea() {
   
   	return cellSurfaceArea;
   }

   @Hidden
   public void setCellSurfaceArea(double cellSurfaceArea) {
   
   	this.cellSurfaceArea = cellSurfaceArea;
   }
	
  	
}
