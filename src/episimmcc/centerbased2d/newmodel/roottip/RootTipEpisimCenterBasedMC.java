package episimmcc.centerbased2d.newmodel.roottip;

import java.util.HashMap;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased2d.roottip.RootTipCenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.roottip.RootTipCenterBased2DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;


public class RootTipEpisimCenterBasedMC extends episimmcc.centerbased2d.newmodel.EpisimCenterBasedMC {
	
	private static final String ID = "2018-02-21";
	private static final String NAME = "Root tip model";

	private HashMap<Long, Double> contactArea = new HashMap<Long, Double>();
	private HashMap<Long, Double> cellcellAdhesion = new HashMap<Long, Double>();
	private double bmContactArea = 0;
	private boolean isNucleated = true;
	
	private double cellVolume = 0;
	private double extCellSpaceVolume = 0;
	private double extCellSpaceMikron = 0.2d;
	private double cellSurfaceArea = 0;
	private double totalContactArea = 0;
	private boolean basalCellContact = false;
	private double biasX = 0;
	private double biasY = 0;
	
	public RootTipEpisimCenterBasedMC(){}
	
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
		return RootTipCenterBased2DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return RootTipCenterBased2DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return RootTipCenterBasedMechModelInit.class;
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
   
   
   @Pairwise
   @NoExport
   public void setCellCellAdhesion(long cellId, double adhesion){
   	this.cellcellAdhesion.put(cellId, adhesion);
   }
   @Pairwise
   @NoExport
   public double getCellCellAdhesion(long cellId){
   	return this.cellcellAdhesion.containsKey(cellId)? this.cellcellAdhesion.get(cellId):0;
   }
   
   @Hidden
   public void setCellCellAdhesion(HashMap<Long, Double> cellcellAdhesion){
   	if(cellcellAdhesion != null) this.cellcellAdhesion = cellcellAdhesion;
   }
   
   @Hidden
   public HashMap<Long, Double> getCellCellAdhesion(){
   	return this.cellcellAdhesion;
   }  
   
	@Hidden
	@NoExport
   public double getAdhesionFactorForCell(AbstractCell cell){   	 	
   	return cell != null && this.cellcellAdhesion.containsKey(cell.getID()) ? this.cellcellAdhesion.get(cell.getID()).doubleValue():0;
   }

	public double getCellVolume(){
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

	
   public double getBmContactArea(){
   
   	return bmContactArea;
   }

   @Hidden
   public void setBmContactArea(double bmContactArea) {
   
   	this.bmContactArea = bmContactArea;
   }
   
   public boolean getIsNucleated(){ return this.isNucleated; }
   
   public void setIsNucleated(boolean val){ this.isNucleated = val; }

	
   public double getTotalContactArea() {
   
   	return totalContactArea;
   }

   @Hidden
   public void setTotalContactArea(double totalContactArea) {
   
   	this.totalContactArea = totalContactArea;
   }

	
   public boolean getBasalCellContact() {
   
   	return basalCellContact;
   }

   @Hidden
   public void setBasalCellContact(boolean basalCellContact) {
   
   	this.basalCellContact = basalCellContact;
   }

   @Hidden
   public boolean getIsImmuneCell() {
	   // needs to be implemented but has no purpose here
	   return false;
   }

	@Hidden
   public void setIsImmuneCell(boolean isImmuneCell) {
	   // needs to be implemented but has no purpose here
   }

	public double getBiasX() {
		return biasX;
	}

	public void setBiasX(double biasX) {
		this.biasX = biasX;
	}

	public double getBiasY() {
		return biasY;
	}

	public void setBiasY(double biasY) {
		this.biasY = biasY;
	}
}