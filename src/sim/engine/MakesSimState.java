package sim.engine;

public interface MakesSimState
    {
    public SimState newInstance(long seed, String[] args);
    public Class simulationClass();
    }
