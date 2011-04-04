package episiminterfaces;

public interface EpisimBiomechanicalModelGlobalParameters extends java.io.Serializable{ 	
	
	double getNeighborhood_µm();
 	void setNeighborhood_µm(double val);
 	
 	int getBasalOpening_µm();
 	void setBasalOpening_µm(int val);
 	
 	int getBasalAmplitude_µm();
 	void setBasalAmplitude_µm(int val);
 	
	void setWidth(double val);
	double getWidth();
	
	void setHeight(double val);
	double getHeight();
}
