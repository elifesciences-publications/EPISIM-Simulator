package sim.app.episim;


public interface SimStateChangeListener {
	
	void simulationWasStarted();
	void simulationWasPaused();
	void simulationWasStopped();

}