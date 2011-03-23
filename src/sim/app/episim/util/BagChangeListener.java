package sim.app.episim.util;


public interface BagChangeListener<T> {

	public void bagHasChanged(BagChangeEvent<T> event);
	
}
