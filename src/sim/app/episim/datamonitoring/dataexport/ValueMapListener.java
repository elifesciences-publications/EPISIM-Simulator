package sim.app.episim.datamonitoring.dataexport;


public interface ValueMapListener <K, V>{
	
	public void valueAdded(K key, V value);	
	
}
