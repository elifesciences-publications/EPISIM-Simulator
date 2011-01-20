package episiminterfaces;

import episiminterfaces.monitoring.CannotBeMonitored;



public interface EpisimCellBehavioralModelGlobalParameters{
	
	int getCellCycleStem();
 	void setCellCycleStem(int val);
	
	int getCellCycleTA();
 	void setCellCycleTA(int val);
 	
 	int getMaxAge();
	void setMaxAge(int val);
	
	double getMinSigLipidsBarrier();
 	void setMinSigLipidsBarrier(double val);
 	
	void resetInitialGlobalValues();
	
	void setSnapshotValues(EpisimCellBehavioralModelGlobalParameters parametersObj);
	
	@CannotBeMonitored
	EpisimCellType[] getAvailableCellTypes();
	@CannotBeMonitored
	EpisimCellType getCellTypeForOrdinal(int ordinal);
	
	@CannotBeMonitored
	EpisimDifferentiationLevel[] getAvailableDifferentiationLevels();
	@CannotBeMonitored
	EpisimDifferentiationLevel getDifferentiationLevelForOrdinal(int ordinal);
	
}
