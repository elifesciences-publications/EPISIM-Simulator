package episiminterfaces;

import episiminterfaces.monitoring.CannotBeMonitored;

public interface EpisimCellBehavioralModelGlobalParameters{
	
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
