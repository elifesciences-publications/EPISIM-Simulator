package episiminterfaces;

public interface EpisimBiomechanicalModelGlobalParameters extends java.io.Serializable{ 	
	
	double getNeighborhood_�m();
 	void setNeighborhood_�m(double val);
 	
 	int getBasalOpening_�m();
 	void setBasalOpening_�m(int val);
 	
 	int getBasalAmplitude_�m();
 	void setBasalAmplitude_�m(int val);
 	
	void setWidth(double val);
	double getWidth();
	
	void setHeight(double val);
	double getHeight();
}
