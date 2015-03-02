package episim_mcc_init.centerbased3d.newversion.epidermis;

import java.util.HashMap;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased3D.newmodel.CenterBased3DMechanicalModelGP;
import sim.app.episim.model.biomechanics.centerbased3D.newmodel.CenterBased3DModel;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episim_mcc_init.EpisimModelConnector.Hidden;
import episim_mcc_init.EpisimModelConnector.Pairwise;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;

public class EpisimEpidermisCenterBased3DMC extends episim_mcc_init.centerbased3d.newversion.EpisimCenterBased3DMC {
	
	private static final String ID = "2014-03-12";
	private static final String NAME = "New Epidermis Center Based Biomechanical 3D Model";
	
	private HashMap<Long, Double> contactArea = new HashMap<Long, Double>();
	private HashMap<Long, Double> cellcellAdhesion = new HashMap<Long, Double>();
	private double bmContactArea=0;
	private boolean isNucleated = true;
	private boolean isViable = true;
	
	private double cellVolume = 0;
	private double extCellSpaceVolume = 0;
	private double extCellSpaceMikron = 0.2d;
	private double cellSurfaceArea=0;
	private double totalContactArea=0;
	
	private boolean basalCellContact = false;
	
	public EpisimEpidermisCenterBased3DMC(){}
	
	@NoExport
	public void resetPairwiseParameters(){
		//this.contactArea.clear();
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
		return CenterBased3DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return CenterBased3DMechanicalModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return EpidermisCenterBasedMechModelInit.class;
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
	
   public double getBmContactArea() {
      
   	return bmContactArea;
   }

   @Hidden
   public void setBmContactArea(double bmContactArea) {
   
   	this.bmContactArea = bmContactArea;
   }
   
   public boolean getIsNucleated(){ return this.isNucleated; }
   
   public void setIsNucleated(boolean val){ this.isNucleated = val; }
   
   public boolean getIsViable(){ return this.isViable; }
   
   public void setIsViable(boolean val){ this.isViable = val; }
   
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
}

