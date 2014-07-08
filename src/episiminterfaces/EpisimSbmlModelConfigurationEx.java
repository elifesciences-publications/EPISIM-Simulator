package episiminterfaces;

public interface EpisimSbmlModelConfigurationEx extends EpisimSbmlModelConfiguration{	
	double getAbsoluteErrorTolerance();
	double getRelativeErrorTolerance();
	boolean isSimulationOnByDefault();
}
