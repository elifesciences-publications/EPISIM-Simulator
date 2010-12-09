package sim.app.episim;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import episimbiomechanics.EpisimModelIntegrator;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimMechanicalModel;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.visualization.CellEllipse;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class AbstractCell implements Steppable, Stoppable, sim.portrayal.Oriented2D, java.io.Serializable{
	
	
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
   private boolean isMembraneCell=false;    // cells directly sitting on membrane, very strict
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   private final long id;
   private final long motherId;   // -1 means not yet set
   
   private static int cellCounter = 0;
   
   private boolean tracked = false;
   
   private AbstractCell[] neighbouringCells;
   
   private CellEllipse cellEllipseObject;
   
   private Stoppable stoppable = null;
   
   private List<CellDeathListener> cellDeathListeners;
      
   private EpisimCellBehavioralModel cellBehavioralModelObjekt;
   private EpisimMechanicalModel mechanicalModelObject;
   
   private SimState actSimState;
   
   public AbstractCell(long identity, long motherIdentity, EpisimCellBehavioralModel cellBehavioralModel){
   	inNirvana=false;
   	isOuterCell=false;
   	this.id = identity;
   	this.motherId = motherIdentity;   	
   	this.cellBehavioralModelObjekt = cellBehavioralModel;
   	if(cellBehavioralModel == null) this.cellBehavioralModelObjekt = ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject();
   	else cellBehavioralModel.setEpisimModelIntegrator((EpisimModelIntegrator)ModelController.getInstance().getBioMechanicalModelController().getEpisimModelIntegrator());
   	mechanicalModelObject =  ModelController.getInstance().getNewMechanicalModelObject(this);
   	cellDeathListeners = new LinkedList<CellDeathListener>();      
      cellDeathListeners.add(TissueServer.getInstance().getActEpidermalTissue());      
      cellDeathListeners.add(GlobalStatistics.getInstance());
   }
	
   public synchronized static  final long getNextCellId(){
   	cellCounter++;
   	return (System.currentTimeMillis() + cellCounter);
   }
   
	public abstract String getCellName();
	
	public abstract List<Method> getParameters();
	
	public boolean isInNirvana() { return inNirvana; }
	public void setInNirvana(boolean inNirvana) { this.inNirvana = inNirvana; }
	
	public long getID() { return id; }   
	
	public boolean isBasalStatisticsCell() { return isBasalStatisticsCell; }
	public void setIsBasalStatisticsCell(boolean val){ this.isBasalStatisticsCell = val; }
	
	public boolean isMembraneCell() { return isMembraneCell; }  
	
	public boolean isOuterCell() { return isOuterCell; } 
	public void setIsMembraneCell(boolean isMembraneCell) {	this.isMembraneCell = isMembraneCell; }
	public void setIsOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell;}
	
	
	public long getMotherID(){ return this.motherId;}

	
   public boolean isTracked() {
   
   	return tracked;
   }

	
   public void setTracked(boolean tracked) {
   
   	this.tracked = tracked;
   }
   
   public void setStoppable(Stoppable stopperparam)   { this.stoppable = stopperparam;}
	
   protected void setNeighbouringCells(AbstractCell[] neighbours){
   	this.neighbouringCells = neighbours;
   }
   
   public void setCellEllipseObject(CellEllipse cellEllipseObject){
   	this.cellEllipseObject = cellEllipseObject;
   }
   
   public AbstractCell[] getNeighbouringCells(){
   	return this.neighbouringCells;
   }
   
   public CellEllipse getCellEllipseObject(){
   	return this.cellEllipseObject;
   }
   public void stop(){	
   	
	}
	public void removeFromSchedule(){
		if(stoppable != null) stoppable.stop();			
	}
	
	public EpisimCellBehavioralModel getEpisimCellBehavioralModelObject(){
		return this.cellBehavioralModelObjekt;
	}
	
   public Class<? extends EpisimCellBehavioralModel> getEpisimCellBehavioralModelClass() {
	  
	   return this.cellBehavioralModelObjekt.getClass();
   }
	
   public SimState getActSimState() { return this.actSimState; }
   
   public void removeCellDeathListener(){
  	 this.cellDeathListeners.clear();
   } 
   
   public void addCellDeathListener(CellDeathListener listener){
  	 this.cellDeathListeners.add(listener);
   }
   
   public void killCell(){
   	 
  	 for(CellDeathListener listener: cellDeathListeners) listener.cellIsDead(this);
  	 
  	 setInNirvana(true);
  	 this.getEpisimCellBehavioralModelObject().setIsAlive(false);
  	 removeFromSchedule();  	
   }
   
   public EpisimMechanicalModel getEpisimMechanicalModelObject(){ return this.mechanicalModelObject; }
   
   public void step(SimState state) {		
		this.actSimState = state;		
   }
   
   public double orientation2D(){	   
	   return mechanicalModelObject.orientation2D();
   }	
   
	
}
