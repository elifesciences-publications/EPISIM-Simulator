package sim.app.episim.model.diffusion;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import sim.app.episim.util.EnhancedSteppable;



public interface ExtraCellularDiffusionField extends EnhancedSteppable{
	double getInterval();
	
	/**
	 * sets all array positions to value
	 * @param value
	 */
	void setToValue(double value);
	
	EpisimDiffusionFieldConfiguration getFieldConfiguration();
	
	ExtracellularDiffusionFieldBCConfig2D getFieldBCConfig();
	
	String getName();
}
