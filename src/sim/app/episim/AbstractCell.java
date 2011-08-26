package sim.app.episim;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import episimbiomechanics.EpisimModelConnector;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.CellEllipse;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class AbstractCell implements Steppable, Stoppable, java.io.Serializable{
	
	
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
  
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   private final long id;
   private final long motherId;   // -1 means not yet set
   
   private static int cellCounter = 0;
   
   private boolean tracked = false;
      
   private Stoppable stoppable = null;
   
   private List<CellDeathListener> cellDeathListeners;
      
   private EpisimCellBehavioralModel cellBehavioralModelObject;
   private EpisimBiomechanicalModel mechanicalModelObject;
   
   private SimState actSimState;
   
   public AbstractCell(long identity, long motherIdentity, EpisimCellBehavioralModel cellBehavioralModel, SimState simState){
   	inNirvana=false;
   	isOuterCell=false;
   	this.id = identity;
   	this.motherId = motherIdentity;   	
   	this.cellBehavioralModelObject = cellBehavioralModel;
   	this.actSimState = simState;
   	final EpisimModelConnector modelConnector = ModelController.getInstance().getBioMechanicalModelController().getNewEpisimModelConnector();
   	if(cellBehavioralModel == null){ 
   		this.cellBehavioralModelObject = ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject();
   	}
   	else{
   		this.cellBehavioralModelObject.setSendReceiveAlgorithm(ModelController.getInstance().getCellBehavioralModelController().getNewInstanceOfSendReceiveAlgorithm());
   	}
   	mechanicalModelObject =  ModelController.getInstance().getNewBioMechanicalModelObject(this);
   	this.mechanicalModelObject.setEpisimModelConnector(modelConnector);
   	this.cellBehavioralModelObject.setEpisimModelConnector(modelConnector);
   	this.cellBehavioralModelObject.setEpisimSbmlModelConnector(ModelController.getInstance().getNewEpisimSbmlModelConnector());
   
   	cellDeathListeners = new LinkedList<CellDeathListener>();      
      cellDeathListeners.add(TissueController.getInstance().getActEpidermalTissue());      
      cellDeathListeners.add(GlobalStatistics.getInstance());
   }
	
   @CannotBeMonitored
   public synchronized static final long getNextCellId(){
   	cellCounter++;
   	return (System.currentTimeMillis() + cellCounter);
   }
   
	public abstract String getCellName();
	
	@NoExport
	public abstract List<Method> getParameters();
	
	public boolean isInNirvana() { return inNirvana; }
	public void setInNirvana(boolean inNirvana) { this.inNirvana = inNirvana; }
	
	@CannotBeMonitored
	public long getID() { return id; }   
	
	public boolean isBasalStatisticsCell() { return isBasalStatisticsCell; }
	public void setIsBasalStatisticsCell(boolean val){ this.isBasalStatisticsCell = val; }
	public boolean isOuterCell() { return isOuterCell; } 	
	public void setIsOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell; }	
	public long getMotherId(){ return this.motherId; }
	@CannotBeMonitored
   public boolean isTracked(){ return tracked; }	
   public void setTracked(boolean tracked) {	this.tracked = tracked; }
   
   public void setStoppable(Stoppable stopperparam){ this.stoppable = stopperparam;}
	   
   @CannotBeMonitored
   public GenericBag<AbstractCell> getNeighbouringCells(){ return this.mechanicalModelObject.getRealNeighbours(); }   
   public void stop(){}   
	public void removeFromSchedule(){ if(stoppable != null) stoppable.stop(); }
		
	@CannotBeMonitored
	public EpisimCellBehavioralModel getEpisimCellBehavioralModelObject(){ return this.cellBehavioralModelObject; }
	
	@CannotBeMonitored
   public Class<? extends EpisimCellBehavioralModel> getEpisimCellBehavioralModelClass(){ return this.cellBehavioralModelObject.getClass(); }	
   
	public SimState getActSimState() { return this.actSimState; }      
	public void removeCellDeathListener(){ this.cellDeathListeners.clear(); }   
	public void addCellDeathListener(CellDeathListener listener){ this.cellDeathListeners.add(listener); }   
   
   public void killCell(){   	 
	  	 for(CellDeathListener listener: cellDeathListeners) listener.cellIsDead(this);	  	 
	  	 setInNirvana(true);
	  	 this.getEpisimCellBehavioralModelObject().setIsAlive(false);
	  	 removeFromSchedule();
   }   
   
   @CannotBeMonitored
   public EpisimBiomechanicalModel getEpisimBioMechanicalModelObject(){ return this.mechanicalModelObject; }   
   public void step(SimState state) {		
		this.actSimState = state;		
   }
   
  
   
	
}
