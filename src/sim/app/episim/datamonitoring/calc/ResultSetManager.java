package sim.app.episim.datamonitoring.calc;

import episimexceptions.DataMonitoringException;
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
		else if(type == CalculationAlgorithmType.TWODIMDATASERIESRESULT || type == CalculationAlgorithmType.TWODIMRESULT){  
			return new ResultSet<T>(ResultSetType.TWODIMRESULTS);
		}
		else if(type == CalculationAlgorithmType.MULTIDIMDATASERIESRESULT){  
			return new ResultSet<T>(ResultSetType.MULTIDIMRESULTS);
		}
		return null;
	}
	
	public static void copyResultSetToDataManager(ResultSet<Double> results1, ResultSet<Double> results2, CalculationDataManager<Double> dataManager) throws DataMonitoringException{
	if(results1.getTimeStep() != results2.getTimeStep()) throw new DataMonitoringException("In compatible data: the time step of the two resultSets are not equal.");
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
				dataManager.setSimStep(results1.getTimeStep());
			}
		}
		else throw new IllegalArgumentException("Cannot handle ResultSets of type TWODIMRESULTS");
	}
	
	public static void copyResultSetToDataManager(ResultSet<Double> results,  CalculationDataManager<Double> dataManager){
		if(dataManager == null)throw new IllegalArgumentException("CalculationDataManager is null.");
		if(results.getResultSetType() == ResultSetType.ONEDIMRESULTS && dataManager.getCalculationDataManagerType()==CalculationDataManagerType.ONEDIMTYPE){
			for(int i = 0; i < results.size(); i++){
				dataManager.addNewValue(results.get(i).get(0));
			}
			
		}
		else if(results.getResultSetType() == ResultSetType.TWODIMRESULTS && dataManager.getCalculationDataManagerType()==CalculationDataManagerType.TWODIMTYPE){
			for(int i = 0; i < results.size(); i++){
				dataManager.addNewValue(results.get(i).get(0), results.get(i).get(1));
			}			
		}
		else if(results.getResultSetType() == ResultSetType.MULTIDIMRESULTS && dataManager.getCalculationDataManagerType()==CalculationDataManagerType.MULTIDIMTYPE){
			for(int i = 0; i < results.size(); i++){
					dataManager.addNewValue(results.get(i));
			}			
		}
		else{
			if(dataManager != null){
				throw new IllegalArgumentException("CalculationDataManager is of type: "+ dataManager.getCalculationDataManagerType().toString() + " which cannot be used for result set of type: " + results.getResultSetType().toString());	
			}			
		}
		dataManager.setSimStep(results.getTimeStep());
	}

}
