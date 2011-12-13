package episiminterfaces;

public interface EpisimDiffusionFieldConfiguration {
	
	String getDiffusionFieldName();
	double getDiffusionCoefficient();
	double getLatticeSiteSizeInMikron();
	double getDegradationRate();
	int getNumberOfIterationsPerCBMSimStep();
	double getDeltaTimeInSecondsPerIteration();
	double getDefaultConcentration();
	double getMaximumConcentration();
	double getMinimumConcentration();

}
