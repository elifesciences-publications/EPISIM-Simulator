package sim.app.episim.datamonitoring.dataexport;

import java.util.Vector;

import episiminterfaces.calc.EntityChangeEvent;


public interface ValueMapListener <T>{
	
			
	public void valueAdded(T value1, T value2);
	public void valueAdded(T value);
	public void valueAdded(Vector<T> value);
	public void observedDataSourceChanged(EntityChangeEvent event);
	public void simStepChanged(long simStep);
}
