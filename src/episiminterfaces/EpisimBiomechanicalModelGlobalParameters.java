package episiminterfaces;


public interface EpisimBiomechanicalModelGlobalParameters extends java.io.Serializable{ 	
	
	public enum ModelDimensionality{TWO_DIMENSIONAL, THREE_DIMENSIONAL;}
	
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
	
	void setLengthInMikron(double val);
	double getLengthInMikron();
	
	@NoUserModification
	ModelDimensionality getModelDimensionality();	
	
	@NoUserModification
	boolean areDiffusionFieldsContinousInXDirection();	
	@NoUserModification
	boolean areDiffusionFieldsContinousInYDirection();
	@NoUserModification
	boolean areDiffusionFieldsContinousInZDirection();
	
	void setNumberOfPixelsPerMicrometer(double val);
	@NoUserModification
	double getNumberOfPixelsPerMicrometer();
}
