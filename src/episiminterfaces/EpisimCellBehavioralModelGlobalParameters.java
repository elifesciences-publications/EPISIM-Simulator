package episiminterfaces;

import episiminterfaces.monitoring.CannotBeMonitored;

public interface EpisimCellBehavioralModelGlobalParameters{
	
	int getCellCycleStem();
 	void setCellCycleStem(int val);
	
	int getCellCycleTA();
 	void setCellCycleTA(int val);
 		 	
	void resetInitialGlobalValues();
		
	@CannotBeMonitored
	EpisimCellType[] getAvailableCellTypes();
	@CannotBeMonitored
	EpisimCellType getCellTypeForOrdinal(int ordinal);
	
	@CannotBeMonitored
	EpisimDifferentiationLevel[] getAvailableDifferentiationLevels();
	@CannotBeMonitored
	EpisimDifferentiationLevel getDifferentiationLevelForOrdinal(int ordinal);
	
	@CannotBeMonitored
	EpisimDiffusionFieldConfiguration[] getAllExtraCellularDiffusionFieldConfigurations();
	
}
