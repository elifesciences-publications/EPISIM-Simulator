package sim.engine;

import ec.util.MersenneTwisterFast;


public class SimStateHack extends SimState implements java.io.Serializable{
	 /**
	 * 
	 */
	private static final long serialVersionUID = -5645592900119653380L;
	/** Creates a SimState with a new random number generator initialized to the given seed,
   plus a new, empty schedule. */
public SimStateHack(long seed)
   {
   this(new MersenneTwisterFast(seed));
   }

/** Creates a SimState with a new, empty Schedule and the provided random number generator. */
public SimStateHack(MersenneTwisterFast random)
   {
   this(random, new Schedule());
   }
   
/** Creates a SimState with the provided random number generator and schedule. */
public SimStateHack(MersenneTwisterFast random, Schedule schedule)
   {
   super(random, schedule);
   
   }
/** Called immediately prior to starting the simulation, or in-between
simulation runs.  This gives you a chance to set up initially,
or reset from the last simulation run. The default version simply
replaces the Schedule with a completely new one.  */
public void start(boolean reloadSnapshot)
{
	// just in case
	cleanupAsynchronous();
	// reset schedule
	if(!reloadSnapshot)schedule.reset();
}
}
