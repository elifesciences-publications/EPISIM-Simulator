package episiminterfaces.calc;

import sim.app.episim.AbstractCellType;
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
		ONEDIMDATASERIESRESULT("one dimensional data series"), 
		TWODIMDATASERIESRESULT("one dimensional data series (gradient etc.)"), 
		HISTOGRAMRESULT("histogram");
		
		private String description;
		private CalculationAlgorithmType(String _description){ this.description = _description; }
		public String toString(){ return description;}
	};
	
	
	
	public static final String HISTOGRAMMINVALUEPARAMETER = "min value";
	public static final String HISTOGRAMMAXVALUEPARAMETER = "max value";
	public static final String HISTOGRAMNUMBEROFBINSPARAMETER = "number of bins";
	
	CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id);
	void registerCells(GenericBag<AbstractCellType> allCells);
	void reset();
	void restartSimulation();	
	void calculate(CalculationHandler handler, ResultSet<Double> results);
	
	
}
