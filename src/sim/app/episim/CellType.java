package sim.app.episim;

import java.lang.reflect.Method;
import java.util.List;

import episiminterfaces.EpisimCellDiffModel;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class CellType implements Steppable, Stoppable, sim.portrayal.Oriented2D, java.io.Serializable{
	
	private double lastDrawInfoX;
   private double lastDrawInfoY;
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
   private boolean isMembraneCell=false;    // cells directly sitting on membrane, very strict
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   private final int identity;
   private final int motherIdentity;   // -1 means not yet set
   
   public CellType(int identity, int motherIdentity){
   	inNirvana=false;
   	isOuterCell=false;
   	this.identity = identity;
   	this.motherIdentity = motherIdentity;
   }
	
	public abstract String getCellName();
	
	public abstract Class<? extends EpisimCellDiffModel> getEpisimCellDiffModelClass();
	
	
	public abstract EpisimCellDiffModel getEpisimCellDiffModelObject();
	
	
	public abstract List<Method> getParameters();
	
	public abstract void killCell();
	
	public double getLastDrawInfoX() { return lastDrawInfoX;	}
	public double getLastDrawInfoY() { return lastDrawInfoY; }
	public void setLastDrawInfoX(double lastDrawInfoX) { this.lastDrawInfoX = lastDrawInfoX; }
	public void setLastDrawInfoY(double lastDrawInfoY) { this.lastDrawInfoY = lastDrawInfoY; }
	
	
	public boolean isInNirvana() { return inNirvana; }
	public void setInNirvana(boolean inNirvana) { this.inNirvana = inNirvana; }
	
	public int getIdentity() { return identity; }   
	
	public boolean isBasalStatisticsCell() { return isBasalStatisticsCell; }
	public void setIsBasalStatisticsCell(boolean val){ this.isBasalStatisticsCell = val; }
	
	public boolean isMembraneCell() { return isMembraneCell; }  
	
	public boolean isOuterCell() { return isOuterCell; } 
	public void setIsMembraneCell(boolean isMembraneCell) {	this.isMembraneCell = isMembraneCell; }
	public void setIsOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell;}
	
	
	protected int getMotherIdentity(){ return this.motherIdentity;}
}
