package sim.app.episim;

import java.lang.reflect.Method;
import java.util.List;

import episiminterfaces.EpisimCellDiffModel;
import sim.app.episim.visualization.CellEllipse;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class CellType implements Steppable, Stoppable, sim.portrayal.Oriented2D, java.io.Serializable{
	
	
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
   private boolean isMembraneCell=false;    // cells directly sitting on membrane, very strict
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   private final long id;
   private final long motherId;   // -1 means not yet set
   
   private static int cellCounter = 0;
   
   private boolean tracked = false;
   
   private CellType[] neighbouringCells;
   
   private CellEllipse cellEllipseObject;
   
   public CellType(long identity, long motherIdentity){
   	inNirvana=false;
   	isOuterCell=false;
   	this.id = identity;
   	this.motherId = motherIdentity;
   }
	
   public synchronized static  final long getNextCellId(){
   	cellCounter++;
   	return (System.currentTimeMillis() + cellCounter);
   }
   
	public abstract String getCellName();
	
	public abstract Class<? extends EpisimCellDiffModel> getEpisimCellDiffModelClass();
	
	
	public abstract EpisimCellDiffModel getEpisimCellDiffModelObject();
	
	
	public abstract List<Method> getParameters();
	
	public abstract void killCell();
	
	public abstract SimState getActSimState();
	
	
	
	
	public boolean isInNirvana() { return inNirvana; }
	public void setInNirvana(boolean inNirvana) { this.inNirvana = inNirvana; }
	
	public long getID() { return id; }   
	
	public boolean isBasalStatisticsCell() { return isBasalStatisticsCell; }
	public void setIsBasalStatisticsCell(boolean val){ this.isBasalStatisticsCell = val; }
	
	public boolean isMembraneCell() { return isMembraneCell; }  
	
	public boolean isOuterCell() { return isOuterCell; } 
	public void setIsMembraneCell(boolean isMembraneCell) {	this.isMembraneCell = isMembraneCell; }
	public void setIsOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell;}
	
	
	protected long getMotherID(){ return this.motherId;}

	
   public boolean isTracked() {
   
   	return tracked;
   }

	
   public void setTracked(boolean tracked) {
   
   	this.tracked = tracked;
   }
	
   protected void setNeighbouringCells(CellType[] neighbours){
   	this.neighbouringCells = neighbours;
   }
   
   protected void setCellEllipseObject(CellEllipse cellEllipseObject){
   	this.cellEllipseObject = cellEllipseObject;
   }
   
   public CellType[] getNeighbouringCells(){
   	return this.neighbouringCells;
   }
   
   public CellEllipse getCellEllipseObject(){
   	return this.cellEllipseObject;
   }
	
}
