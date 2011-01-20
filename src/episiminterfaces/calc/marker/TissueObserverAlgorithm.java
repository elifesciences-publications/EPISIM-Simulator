package episiminterfaces.calc.marker;

/*
 * This marker interface is used to identify calculation algorithms that calculate a data series each update cycle based on the cells in the tissue 
 */
public interface TissueObserverAlgorithm {
	void addTissueObserver(long[] associatedCalculationHandlerIds, TissueObserver observer);
}
