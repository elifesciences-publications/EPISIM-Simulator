package sim.app.episim.datamonitoring.calc;

import episiminterfaces.calc.EntityChangeEvent;


public interface CalculationDataManager<X> {	
	public enum CalculationDataManagerType {ONEDIMTYPE, TWODIMTYPE};
	
	long getID();
	boolean isXScaleLogarithmic();
	boolean isYScaleLogarithmic();
	void addNewValue(X xValue, X yValue);
	void addNewValue(X xValue);
	void observedEntityHasChanged(EntityChangeEvent event);
	long getSimStep();
	void setSimStep(long step);
	void reset();
	CalculationDataManagerType getCalculationDataManagerType();
}
