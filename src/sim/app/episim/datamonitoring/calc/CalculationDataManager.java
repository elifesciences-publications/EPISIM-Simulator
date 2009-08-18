package sim.app.episim.datamonitoring.calc;

import episiminterfaces.calc.EntityChangeEvent;


public interface CalculationDataManager<X> {	
	
	long getID();
	boolean isXScaleLogarithmic();
	boolean isYScaleLogarithmic();
	void addNewValue(X xValue, X yValue);
	void addNewValue(X xValue);
	void observedEntityHasChanged(EntityChangeEvent event);
	void restartSimulation();
}
