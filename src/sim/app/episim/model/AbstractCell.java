package sim.app.episim.model;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import episim_mcc_init.EpisimModelConnector;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelExt;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.AbstractBiomechanicalModel;
import sim.app.episim.model.biomechanics.centerbased2D.AbstractCenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2D.oldmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased3D.AbstractCenterBased3DModel;
import sim.app.episim.model.biomechanics.centerbased3D.oldmodel.CenterBased3DModel;
import sim.app.episim.model.biomechanics.vertexbased2D.VertexBasedModel;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardCellType;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.CellEllipse;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class AbstractCell implements Steppable, Stoppable, java.io.Serializable{
	
	
 
   private final long id;
   private final AbstractCell motherCell;   
   
   private static int cellCounter = 0;
   
   private boolean tracked = false;
      
   private Stoppable stoppable = null;
   
   private Set<CellDeathListener> cellDeathListeners;
      
   private EpisimCellBehavioralModel cellBehavioralModelObject;
   private EpisimBiomechanicalModel mechanicalModelObject;
   
   public AbstractCell(AbstractCell motherCell, EpisimCellBehavioralModel cellBehavioralModel){
   	this(Long.MIN_VALUE, motherCell, cellBehavioralModel);
   }
   
   public AbstractCell(long cellId, AbstractCell motherCell, EpisimCellBehavioralModel cellBehavioralModel){
   	
   	this.id = cellId ==Long.MIN_VALUE ? getNextCellId(): cellId;
   	this.motherCell = ((motherCell == null) ? this : motherCell);   	
   	this.cellBehavioralModelObject = cellBehavioralModel;
   	
   	if(cellBehavioralModel == null){ 
   		this.cellBehavioralModelObject = ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject();
   	}
   	else{
   		this.cellBehavioralModelObject.setSendReceiveAlgorithm(ModelController.getInstance().getCellBehavioralModelController().getNewInstanceOfSendReceiveAlgorithm());
   	}
   	this.cellBehavioralModelObject.setId(this.id);
   	mechanicalModelObject =  ModelController.getInstance().getNewBioMechanicalModelObject(this);
   	if(cellBehavioralModel instanceof EpisimCellBehavioralModelExt && ((EpisimCellBehavioralModelExt)cellBehavioralModel).getEpisimModelConnector() != null){
   		final EpisimModelConnector modelConnector = ((EpisimCellBehavioralModelExt)cellBehavioralModel).getEpisimModelConnector();
      	this.mechanicalModelObject.setEpisimModelConnector(modelConnector);
   	}else{
   		final EpisimModelConnector modelConnector = ModelController.getInstance().getBioMechanicalModelController().getNewEpisimModelConnector();
      	this.mechanicalModelObject.setEpisimModelConnector(modelConnector);
      	this.cellBehavioralModelObject.setEpisimModelConnector(modelConnector);
   	}
   	if(this.motherCell.getEpisimBioMechanicalModelObject() instanceof AbstractCenterBased3DModel){
   		AbstractCenterBased3DModel motherMech = (AbstractCenterBased3DModel)this.motherCell.getEpisimBioMechanicalModelObject();
   		AbstractCenterBased3DModel thisMech = (AbstractCenterBased3DModel)mechanicalModelObject;
   		thisMech.setStandardCellHeight(motherMech.getStandardCellHeight());
   		thisMech.setStandardCellWidth(motherMech.getStandardCellWidth());
   		thisMech.setStandardCellLength(motherMech.getStandardCellLength());
   	}
   	if(this.motherCell.getEpisimBioMechanicalModelObject() instanceof AbstractCenterBased2DModel){
   		AbstractCenterBased2DModel motherMech = (AbstractCenterBased2DModel)this.motherCell.getEpisimBioMechanicalModelObject();
   		AbstractCenterBased2DModel thisMech = (AbstractCenterBased2DModel)mechanicalModelObject;
   		thisMech.setStandardCellHeight(motherMech.getStandardCellHeight());
   		thisMech.setStandardCellWidth(motherMech.getStandardCellWidth());   		
   	}
   	
   	this.cellBehavioralModelObject.setEpisimSbmlModelConnector(ModelController.getInstance().getNewEpisimSbmlModelConnector());
   
   	cellDeathListeners = new HashSet<CellDeathListener>();      
      cellDeathListeners.add(TissueController.getInstance().getActEpidermalTissue());      
      cellDeathListeners.add(GlobalStatistics.getInstance());
      
      
   }
	
   @CannotBeMonitored
   private synchronized static final long getNextCellId(){
   	cellCounter++;
   	return (System.currentTimeMillis() + cellCounter);
   }
   
	public abstract String getCellName();
	
	@NoExport
	public abstract List<Method> getParameters();
		
	@CannotBeMonitored
	public long getID() { return id; }   
		
	public long getMotherId(){ return this.motherCell != null ? this.motherCell.getID(): -1; }
	@NoExport
	public AbstractCell getMotherCell(){ return this.motherCell; }
	
	@CannotBeMonitored
	@NoExport
   public boolean getIsTracked(){ return tracked; }	
   public void setTracked(boolean tracked) {	this.tracked = tracked; }
   @NoExport
   public void setStoppable(Stoppable stopperparam){ this.stoppable = stopperparam;}
	   
   @CannotBeMonitored
   @NoExport
   public GenericBag<AbstractCell> getNeighbouringCells(){ return this.mechanicalModelObject.getDirectNeighbours(); }   
   public void stop(){}   
	public void removeFromSchedule(){ if(stoppable != null) stoppable.stop(); }
		
	@CannotBeMonitored @NoExport
	public EpisimCellBehavioralModel getEpisimCellBehavioralModelObject(){ return this.cellBehavioralModelObject; }
	
	@CannotBeMonitored @NoExport
   public Class<? extends EpisimCellBehavioralModel> getEpisimCellBehavioralModelClass(){ return this.cellBehavioralModelObject.getClass(); }	
   
	
	public void removeCellDeathListener(){ this.cellDeathListeners.clear(); }   
	public void addCellDeathListener(CellDeathListener listener){ this.cellDeathListeners.add(listener); }   
   
   public void killCell(){
   	 if(this.getEpisimBioMechanicalModelObject() instanceof AbstractBiomechanicalModel)((AbstractBiomechanicalModel)this.getEpisimBioMechanicalModelObject()).removeCellFromCellField();	
	  	 for(CellDeathListener listener: cellDeathListeners) listener.cellIsDead(this);	  	 
	  	 this.getEpisimCellBehavioralModelObject().setIsAlive(false);
	  	 removeFromSchedule();
   }   
   
   @CannotBeMonitored @NoExport
   public EpisimBiomechanicalModel getEpisimBioMechanicalModelObject(){ return this.mechanicalModelObject; }   
   public void step(SimState state) {		
		/* DOES NOTHING */
   }
   
   public boolean equals(Object obj){
	   if(obj instanceof AbstractCell){
	   	return this.getID() == ((AbstractCell)obj).getID();
	   }
	   else return super.equals(obj);
   }
   
   
   public Color getCellColoring(){
   	return getFillColor(this);
   }
   
   private Color getFillColor(AbstractCell kcyte){  	
      
      int  red=kcyte.getEpisimCellBehavioralModelObject().getColorR();
      int  green=kcyte.getEpisimCellBehavioralModelObject().getColorG();
      int  blue=kcyte.getEpisimCellBehavioralModelObject().getColorB();       
        
      // Limit the colors to 255
      green=(green>255)?255:((green<0)?0:green);
      red=(red>255)?255:((red<0)?0:red);
      blue=(blue>255)?255:((blue<0)?0:blue);
      
      if(kcyte.getIsTracked() && MiscalleneousGlobalParameters.getInstance().getHighlightTrackedCells()) return Color.RED;
      return new Color(red, green, blue);
   }
   public void assignDefaultCellType(){
  	 if(ModelController.getInstance().isStandardKeratinocyteModel()){
  		 EpisimCellType[] cellTypes = ModelController.getInstance().getCellBehavioralModelController().getAvailableCellTypes();
  		 for(EpisimCellType cellType: cellTypes){
  			 if(cellType.toString().equals(StandardCellType.KERATINOCYTE.toString())){
  				 this.getEpisimCellBehavioralModelObject().setCellType(cellType);
  				 break;
  			 }
  		 }
  	 }
   }
   
   public void assignDefaultDiffLevel(){
  	 if(ModelController.getInstance().isStandardKeratinocyteModel()){
  		 EpisimDifferentiationLevel[] diffLevels = ModelController.getInstance().getCellBehavioralModelController().getAvailableDifferentiationLevels();
  		 for(EpisimDifferentiationLevel diffLevel: diffLevels){
  			 if(diffLevel.toString().equals(StandardDiffLevel.STEMCELL.toString())){
  				 this.getEpisimCellBehavioralModelObject().setDiffLevel(diffLevel);
  				 break;
  			 }
  		 }
  	 }
   }
   
   public StandardDiffLevel getStandardDiffLevel(){
  	 return convertToStandardDiffLevel(this.getEpisimCellBehavioralModelObject().getDiffLevel());
   }
   
   public StandardCellType getStandardCellType(){
  	return convertToStandardCellType(this.getEpisimCellBehavioralModelObject().getCellType());
   }
   
   public StandardDiffLevel convertToStandardDiffLevel(EpisimDifferentiationLevel diffLevel){
  	 if(ModelController.getInstance().isStandardKeratinocyteModel()){
  		 StandardDiffLevel[] sDiffLevels = StandardDiffLevel.values();
  		 for(StandardDiffLevel sDiffLevel: sDiffLevels){
  			 if(sDiffLevel.toString().equals(diffLevel.name())) return sDiffLevel;
  		 }
  	 }
  	 return null;
   }
   
   public StandardCellType convertToStandardCellType(EpisimCellType cellType){
  	 if(ModelController.getInstance().isStandardKeratinocyteModel()){
  		 
  		 StandardCellType[] sCellTypes = StandardCellType.values();
  		 for(StandardCellType sCellType: sCellTypes){
  			 if(sCellType.toString().equals(cellType.name())) return sCellType;
  		 }
  	 }
  	 return null;
   }
}
