package sim.app.episim;


public interface SimulationStateChangeListener {
	
	void simulationWasStarted();
	void simulationWasPaused();
	void simulationWasStopped();

}
