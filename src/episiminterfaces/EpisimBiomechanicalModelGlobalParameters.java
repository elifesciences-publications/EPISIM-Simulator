package episiminterfaces;

public interface EpisimBiomechanicalModelGlobalParameters extends java.io.Serializable{ 	
	
	double getNeighborhood_mikron();
 	void setNeighborhood_mikron(double val);
 	
 	int getBasalOpening_mikron();
 	void setBasalOpening_mikron(int val);
 	
 	int getBasalAmplitude_mikron();
 	void setBasalAmplitude_mikron(int val);
 	
	void setWidth(double val);
	double getWidth();
	
	void setHeight(double val);
	double getHeight();
}
