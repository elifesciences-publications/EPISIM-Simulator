package sim.app.episim.datamonitoring.calc;


public interface  OneCellTrackingDataManager<K,V> {
	
	void addNewValue(K key, V value);
	void cellHasChanged();
	void restartSimulation();

}
