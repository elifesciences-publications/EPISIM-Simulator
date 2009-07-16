package episiminterfaces.calc;

import sim.app.episim.CellType;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.ResultSet;

public interface CalculationAlgorithm extends java.io.Serializable{

	
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
	
	
	CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id);
	void registerCells(GenericBag<CellType> allCells);
	void reset();
	void restartSimulation();	
	void calculate(CalculationHandler handler, ResultSet<Double> results);
	
	
}
