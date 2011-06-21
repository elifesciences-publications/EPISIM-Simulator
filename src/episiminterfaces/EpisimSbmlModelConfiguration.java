package episiminterfaces;

public interface EpisimSbmlModelConfiguration {
	
	double getErrorTolerance();
	String getModelFilename();
	String getSbmlId();
	int getNoOfStepsPerCBMSimstep();
	double getNoOfTimeUnitsPerCBMSimstep();
}
