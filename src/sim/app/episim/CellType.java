package sim.app.episim;

import sim.engine.Steppable;
import sim.engine.Stoppable;


public abstract class CellType implements Steppable, Stoppable, sim.portrayal.Oriented2D, java.io.Serializable{
	
	
	
	public abstract String getName();
	
	
	
}
