package episimmcc.centerbased3d.apicalmeristem;


/*
 * Model connector for fish eye biomechanical parameters
 */

import java.util.HashMap;

import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.centerbased3d.apicalmeristem.ApicalMeristemCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3d.apicalmeristem.ApicalMeristemCenterBased3DModelGP;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episimmcc.EpisimModelConnector;
import episimmcc.EpisimModelConnector.Hidden;
import episimmcc.EpisimModelConnector.Pairwise;

public class EpisimApicalMeristemCenterBased3DMC extends EpisimModelConnector {
	
	private static final String ID = "2016-01-28";
	private static final String NAME = "Apical Meristem Center Based Biomechanical 3D Model";
	
	private HashMap<Long, Double> contactArea = new HashMap<Long, Double>();
	private HashMap<Long, Double> cellcellAdhesion = new HashMap<Long, Double>();
	private double bmContactArea=0;

	private double average_overlap = 0;
	
	private double cellVolume = 0;
	private double extCellSpaceVolume = 0;
	private double extCellSpaceMikron = 0.2d;
	private double cellSurfaceArea = 0;
	private double totalContactArea = 0;	
	   
	private double x = 0;
	private double y = 0;
	private double z = 0;
	private double width = 0;
	private double height = 0;
	private double length = 0;
		
	private boolean L1 = false;
	private boolean L2 = false;
	private boolean L3 = false;
	
	private boolean inStemCellNiche = false;
	
	private double boundaryCrossedMikron = 0;
	
	private double adhesionMembrane = 0;
	
	private int cellId = -1;
		
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
		return ApicalMeristemCenterBased3DModel.class;
	}
	
	@NoExport
	public Class<? extends EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParametersClass(){
		return ApicalMeristemCenterBased3DModelGP.class;
	}
	
	@NoExport
	public Class<? extends BiomechanicalModelInitializer> getEpisimBioMechanicalModelInitializerClass(){
		return ApicalMeristemCenterBasedMechModelInit.class;
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

   public double getAverage_overlap() {

	   return average_overlap;
   }
	
	@Hidden
	public void setAverage_overlap(double average_overlap) {

	   this.average_overlap = average_overlap;
   }
	
   public int getCellId() {
   
   	return cellId;
   }

   @Hidden
   public void setCellId(int cellId) {   
   	this.cellId = cellId;
   }

   public boolean getL1() {
   
   	return L1;
   }

   @Hidden
   public void setL1(boolean l1) {   
   	L1 = l1;   	
   }

	
   public boolean getL2() {   
   	return L2;
   }

   @Hidden
   public void setL2(boolean l2) {   
   	L2 = l2;
   }

	
   public boolean getL3() {   
   	return L3;
   }

   @Hidden
   public void setL3(boolean l3) {   
   	L3 = l3;
   }

	
   public double getBoundaryCrossedMikron() {
   
   	return boundaryCrossedMikron;
   }

   @Hidden
   public void setBoundaryCrossedMikron(double boundaryCrossedMikron) {
   
   	this.boundaryCrossedMikron = boundaryCrossedMikron;
   }

	
   public boolean getInStemCellNiche() {
   
   	return inStemCellNiche;
   }

   @Hidden
   public void setInStemCellNiche(boolean inStemCellNiche) {
   
   	this.inStemCellNiche = inStemCellNiche;
   }	
	
}

