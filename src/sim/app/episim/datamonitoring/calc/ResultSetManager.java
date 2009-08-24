package sim.app.episim.datamonitoring.calc;

import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;
import sim.app.episim.datamonitoring.calc.CalculationDataManager.CalculationDataManagerType;
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
	
	public static void copyResultSetToDataManager(ResultSet<Double> results1, ResultSet<Double> results2, CalculationDataManager<Double> dataManager){
		if(dataManager != null && dataManager.getCalculationDataManagerType()==CalculationDataManagerType.ONEDIMTYPE) throw new IllegalArgumentException("CalculationDataManager is of ONEDIMTYPE. TWODIMTYPE is required.");
		if(results1.getResultSetType() == ResultSetType.ONEDIMRESULTS && results2.getResultSetType() == ResultSetType.ONEDIMRESULTS){
			if(results1.size() >0 && results2.size() >0){
				if((results1.size() != results2.size()) && !(results1.size()==1 || results2.size()==1)) throw new IllegalArgumentException("The two resultsets don't match. The resulsets must either have the same size or one of the two resultsets must have size 1.");
				else{
					if(results1.size() == 1 && results2.size() > 1){
						for(int i = 0; i < results2.size(); i++){
							dataManager.addNewValue(results1.get(0).get(0), results2.get(i).get(0));
						}
					}
					else if(results2.size() == 1 && results1.size() > 1){
						for(int i = 0; i < results1.size(); i++){
							dataManager.addNewValue(results1.get(i).get(0), results2.get(0).get(0));
						}
					}
					else if(results2.size() > 1 && results1.size() > 1){
						for(int i = 0; i < results1.size(); i++){
							dataManager.addNewValue(results1.get(i).get(0), results2.get(i).get(0));
						}
					}
					else if(results2.size() == 1 && results1.size() == 1){
						
							dataManager.addNewValue(results1.get(0).get(0), results2.get(0).get(0));
						
					}
				}
			}
		}
		else throw new IllegalArgumentException("Cannot handle ResultSets of type TWODIMRESULTS");
	}
	
	public static void copyResultSetToDataManager(ResultSet<Double> results,  CalculationDataManager<Double> dataManager){
		if(results.getResultSetType() == ResultSetType.ONEDIMRESULTS){
			if(dataManager != null && dataManager.getCalculationDataManagerType()==CalculationDataManagerType.TWODIMTYPE) throw new IllegalArgumentException("CalculationDataManager is of TWODIMTYPE. ONEDIMTYPE is required.");	
			else{
				for(int i = 0; i < results.size(); i++){
					dataManager.addNewValue(results.get(i).get(0));
				}
			}
		}
		else if(results.getResultSetType() == ResultSetType.TWODIMRESULTS){
			if(dataManager != null && dataManager.getCalculationDataManagerType()==CalculationDataManagerType.ONEDIMTYPE) throw new IllegalArgumentException("CalculationDataManager is of ONEDIMTYPE. TWODIMTYPE is required.");	
			else{
				for(int i = 0; i < results.size(); i++){
					dataManager.addNewValue(results.get(i).get(0), results.get(i).get(1));
				}
			}
		}
	}

}
