package episimmcc.centerbased3d.fisheye;
 
import java.util.HashMap;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.fisheye.FishEyeCenterBased3DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;
import episimmcc.EpisimModelConnector.Pairwise;

public class EpisimFishEyeCenterBased3DMC extends EpisimModelConnector {
	private static final String ID = "2015-01-27";
	private static final String NAME = "Fish Eye Center Based Biomechanical 3D Model";
	
	private HashMap<Long, Double> contactArea = new HashMap<Long, Double>();
	private HashMap<Long, Double> cellcellAdhesion = new HashMap<Long, Double>();
	private double bmContactArea=0;
	
	
	private double cellVolume = 0;
	private double extCellSpaceVolume = 0;
	private double extCellSpaceMikron = 0.2d;
	private double cellSurfaceArea=0;
	private double totalContactArea=0;	
	private double contactAreaInnerEye=0;	
   
	private double innerEyeRadius = 100;
	
	private double x=0;
	private double y=0;
	private double z=0;
	private double width=0;
	private double height=0;
	private double length=0;
		
	private double adhesionMembrane=0;	
		
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
   
   public double getAdhesionMembrane() {      
   	return adhesionMembrane;
   }	
   
   public void setAdhesionMembrane(double adhesionMembrane) {   
   	this.adhesionMembrane = adhesionMembrane;
   }
   
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
		return FishEyeCenterBased3DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return FishEyeCenterBased3DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return FishEyeCenterBasedMechModelInit.class;
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
   
   public double getTotalContactArea() {      
   	return totalContactArea;
   }

   @Hidden
   public void setTotalContactArea(double totalContactArea) {   
   	this.totalContactArea = totalContactArea;
   }

	
   public double getInnerEyeRadius() {
   
   	return innerEyeRadius;
   }

	
   public void setInnerEyeRadius(double innerEyeRadius) {
   
   	this.innerEyeRadius = innerEyeRadius;
   }

	
   public double getContactAreaInnerEye() {
   
   	return contactAreaInnerEye;
   }

	
   public void setContactAreaInnerEye(double contactAreaInnerEye) {
   
   	this.contactAreaInnerEye = contactAreaInnerEye;
   }
   
}

