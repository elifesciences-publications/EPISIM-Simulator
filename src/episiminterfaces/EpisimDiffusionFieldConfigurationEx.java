package episiminterfaces;


public interface EpisimDiffusionFieldConfigurationEx extends EpisimDiffusionFieldConfiguration {
	void setDiffusionFieldName(String val);
	void setDiffusionCoefficient(double val);
	void setLatticeSiteSizeInMikron(double val);
	void setDegradationRate(double val);
	void setNumberOfIterationsPerCBMSimStep(int val);
	void setDeltaTimeInSecondsPerIteration(double val);
	void setDefaultConcentration(double val);
	void setMaximumConcentration(double val);
	void setMinimumConcentration(double val);
}
