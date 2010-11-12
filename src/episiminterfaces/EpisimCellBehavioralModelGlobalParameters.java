package episiminterfaces;



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
	
	EpisimCellType[] getAvailableCellTypes();
	EpisimCellType getCellTypeForOrdinal(int ordinal);
	
	EpisimDifferentiationLevel[] getAvailableDifferentiationLevels();
	EpisimDifferentiationLevel getDifferentiationLevelForOrdinal(int ordinal);
	
}
