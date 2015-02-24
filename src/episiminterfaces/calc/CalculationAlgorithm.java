package episiminterfaces.calc;

import sim.app.episim.AbstractCell;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.ResultSet;

public interface CalculationAlgorithm extends java.io.Serializable{

	/*
	 * An Calculation Algorithm of type HISTOGRAMRESULT must define the double parameters minvalue, maxvalue and the int value numberofbins
	 * in the CalculatoinAlgorithmDescriptor in the aforementioned order
	 * 
	 * 
	 */
	enum CalculationAlgorithmType {
		
		ONEDIMRESULT("one dimensional result"), 
		TWODIMRESULT("two dimensional result"), 
		TWODIMDATASERIESRESULT("two dimensional data series (gradient etc.)"),
		MULTIDIMDATASERIESRESULT("multi-dimensional data series"),
		HISTOGRAMRESULT("histogram");
		
		private String description;
		private CalculationAlgorithmType(String _description){ this.description = _description; }
		public String toString(){ return description;}
	};
	
	
	

	
	
	
	
	CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id);
	void registerCells(GenericBag<AbstractCell> allCells);
	void reset();
	void restartSimulation();	
	void calculate(CalculationHandler handler, ResultSet<Double> results);
	void newSimStep();
	
}
