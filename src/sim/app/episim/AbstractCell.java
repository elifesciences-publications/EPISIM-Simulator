package sim.app.episim;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.util.GenericBag;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class AbstractCell implements Steppable, Stoppable, java.io.Serializable{
	
	
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
  
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   private final long id;
   private final AbstractCell motherCell;   // -1 means not yet set
   
   private static int cellCounter = 0;
   
   private boolean tracked = false;
      
   private Stoppable stoppable = null;
   
   private Set<CellDeathListener> cellDeathListeners;
      
   private EpisimCellBehavioralModel cellBehavioralModelObject;
   private EpisimBiomechanicalModel mechanicalModelObject;
   
   
   
   public AbstractCell(AbstractCell motherCell, EpisimCellBehavioralModel cellBehavioralModel){
   	inNirvana=false;
   	isOuterCell=false;
   	this.id = getNextCellId();
   	this.motherCell = ((motherCell == null) ? this : motherCell);   	
   	this.cellBehavioralModelObject = cellBehavioralModel;
   	final EpisimModelConnector modelConnector = ModelController.getInstance().getBioMechanicalModelController().getNewEpisimModelConnector();
   	if(cellBehavioralModel == null){ 
   		this.cellBehavioralModelObject = ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject();
   	}
   	else{
   		this.cellBehavioralModelObject.setSendReceiveAlgorithm(ModelController.getInstance().getCellBehavioralModelController().getNewInstanceOfSendReceiveAlgorithm());
   	}
   	this.cellBehavioralModelObject.setId(this.id);
   	mechanicalModelObject =  ModelController.getInstance().getNewBioMechanicalModelObject(this);
   	this.mechanicalModelObject.setEpisimModelConnector(modelConnector);
   	this.cellBehavioralModelObject.setEpisimModelConnector(modelConnector);
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
	
	public boolean getIsInNirvana() { return inNirvana; }
	public void setInNirvana(boolean inNirvana) { this.inNirvana = inNirvana; }
	
	@CannotBeMonitored
	public long getID() { return id; }   
	
	public boolean getIsBasalStatisticsCell() { return isBasalStatisticsCell; }
	public void setIsBasalStatisticsCell(boolean val){ this.isBasalStatisticsCell = val; }
	public boolean getIsOuterCell() { return isOuterCell; } 	
	public void setIsOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell; }	
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
   public GenericBag<AbstractCell> getNeighbouringCells(){ return this.mechanicalModelObject.getRealNeighbours(); }   
   public void stop(){}   
	public void removeFromSchedule(){ if(stoppable != null) stoppable.stop(); }
		
	@CannotBeMonitored @NoExport
	public EpisimCellBehavioralModel getEpisimCellBehavioralModelObject(){ return this.cellBehavioralModelObject; }
	
	@CannotBeMonitored @NoExport
   public Class<? extends EpisimCellBehavioralModel> getEpisimCellBehavioralModelClass(){ return this.cellBehavioralModelObject.getClass(); }	
   
	
	public void removeCellDeathListener(){ this.cellDeathListeners.clear(); }   
	public void addCellDeathListener(CellDeathListener listener){ this.cellDeathListeners.add(listener); }   
   
   public void killCell(){
   	 if(this.getEpisimBioMechanicalModelObject() instanceof AbstractMechanicalModel)((AbstractMechanicalModel)this.getEpisimBioMechanicalModelObject()).removeCellFromCellField();	
	  	 for(CellDeathListener listener: cellDeathListeners) listener.cellIsDead(this);	  	 
	  	 setInNirvana(true);
	  	 this.getEpisimCellBehavioralModelObject().setIsAlive(false);
	  	 removeFromSchedule();
   }   
   
   @CannotBeMonitored @NoExport
   public EpisimBiomechanicalModel getEpisimBioMechanicalModelObject(){ return this.mechanicalModelObject; }   
   public void step(SimState state) {		
		
   }
   
   public boolean equals(Object obj){
	   if(obj instanceof AbstractCell){
	   	return this.getID() == ((AbstractCell)obj).getID();
	   }
	   else return super.equals(obj);
   }
   
	
}
