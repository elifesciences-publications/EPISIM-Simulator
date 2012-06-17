package episiminterfaces;


public interface EpisimSbmlModelConnector {
	
	void setEpisimModelConfigurations(EpisimSbmlModelConfiguration[] episimModelConfigurations);
	
	void setParameterValue(String originalName, String sbmlFile, double value);
	void setSpeciesQuantity(String originalName, String sbmlFile, double value);
	void setSpeciesInitialQuantity(String originalName, String sbmlFile, double value);
	
	double getSpeciesValue(String originalName, String sbmlFile);
	double getParameterValue(String originalName, String sbmlFile);
	double getFluxValue(String originalName, String sbmlFile);
	
	void simulateSbmlModels();
	void switchSbmlModelSimulationOnOrOff(String sbmlModelFile, boolean isSimulationOn);	
}
