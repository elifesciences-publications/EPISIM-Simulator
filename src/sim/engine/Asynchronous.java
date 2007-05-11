package sim.engine;

/** Asynchronous objects can be started, stopped, paused, and resumed. */

public interface Asynchronous extends Steppable, Stoppable
    {
    public void pause();
    public void resume();
    }
