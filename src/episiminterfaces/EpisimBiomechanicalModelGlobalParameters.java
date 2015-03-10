package episiminterfaces;


public interface EpisimBiomechanicalModelGlobalParameters extends java.io.Serializable{ 	
	
	public enum ModelDimensionality{TWO_DIMENSIONAL, THREE_DIMENSIONAL;}
	
	double getNeighborhood_mikron();
 	void setNeighborhood_mikron(double val);
 	
	void setWidthInMikron(double val);
	double getWidthInMikron();
	
	void setHeightInMikron(double val);
	double getHeightInMikron();
	
	void setLengthInMikron(double val);
	double getLengthInMikron();
	
	@NoUserModification
	ModelDimensionality getModelDimensionality();	
	
	void setNumberOfPixelsPerMicrometer(double val);
	@NoUserModification
	double getNumberOfPixelsPerMicrometer();
}
