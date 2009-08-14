package sim.app.episim.datamonitoring.calc;

import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import sim.app.episim.util.ResultSet;
import sim.app.episim.util.ResultSet.ResultSetType;


public abstract class ResultSetManager {
	
	
	public static <T> ResultSet<T> createResultSetForCalculationAlgorithm(int calculationAlgorithmID){
		CalculationAlgorithmType type = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(calculationAlgorithmID).getType();
		if(type == CalculationAlgorithmType.ONEDIMRESULT || type == CalculationAlgorithmType.HISTOGRAMRESULT || type == CalculationAlgorithmType.ONEDIMDATASERIESRESULT){
			return new ResultSet<T>(ResultSetType.ONEDIMRESULTS);
		}
		else return new ResultSet<T>(ResultSetType.TWODIMRESULTS);
	}
	
	public static void copyResultSetToDataManager(ResultSet<? extends Number> results1, ResultSet<? extends Number> results2, CalculationDataManager<? extends Number, ? extends Number> dataManager){
		
	}
	
	public static void copyResultSetToDataManager(ResultSet<? extends Number> results, ResultSet<? extends Number> results2, CalculationDataManager<? extends Number, ? extends Number> dataManager){
		
	}

}
