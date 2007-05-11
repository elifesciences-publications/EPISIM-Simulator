package sim.engine;

/** Stoppable objects can be prevented from being stepped any further by calling their stop() method. */

public interface Stoppable extends java.io.Serializable
    {
    public void stop();
    }
