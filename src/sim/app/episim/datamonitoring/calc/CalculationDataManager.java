package sim.app.episim.datamonitoring.calc;


public interface CalculationDataManager<K,V> {	
	
	long getID();
	boolean isXScaleLogarithmic();
	boolean isYScaleLogarithmic();
	void addNewValue(K key, V value);
	void observedEntityHasChanged();
	void restartSimulation();
}
