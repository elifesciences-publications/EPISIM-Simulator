package sim.app.episim.datamonitoring.dataexport;


public interface ValueMapListener <T>{
	
	public void valueAdded(T value1, T value2);
	public void valueAdded(T value);
	
}
