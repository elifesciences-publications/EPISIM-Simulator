package sim.engine;

/** Something that can be stepped */

public interface Steppable extends java.io.Serializable
    {
    public void step(SimState state);
    }
