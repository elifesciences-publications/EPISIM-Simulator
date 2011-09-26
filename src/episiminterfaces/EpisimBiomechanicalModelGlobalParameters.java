package episiminterfaces;

import sim.app.episim.util.NoUserModification;

public interface EpisimBiomechanicalModelGlobalParameters extends java.io.Serializable{ 	
	
	double getNeighborhood_mikron();
 	void setNeighborhood_mikron(double val);
 	
 	int getBasalOpening_mikron();
 	void setBasalOpening_mikron(int val);
 	
 	int getBasalAmplitude_mikron();
 	void setBasalAmplitude_mikron(int val);
 	
	void setWidthInMikron(double val);
	double getWidthInMikron();
	
	void setHeightInMikron(double val);
	double getHeightInMikron();
	
	
	void setNumberOfPixelsPerMicrometer(double val);
	@NoUserModification
	double getNumberOfPixelsPerMicrometer();
}
