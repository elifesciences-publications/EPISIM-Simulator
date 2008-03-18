package sim.app.episim;

import java.lang.reflect.Method;
import java.util.List;

import episiminterfaces.EpisimCellDiffModel;
import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class CellType implements Steppable, Stoppable, sim.portrayal.Oriented2D, java.io.Serializable{
	
	
	
	public abstract String getCellName();
	
	public abstract Class<? extends EpisimCellDiffModel> getEpisimCellDiffModelClass();
	
	
	public abstract EpisimCellDiffModel getEpisimCellDiffModelObject();
	
	
	public abstract List<Method> getParameters();
	
}
