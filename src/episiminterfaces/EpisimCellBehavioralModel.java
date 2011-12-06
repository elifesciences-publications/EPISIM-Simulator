package episiminterfaces;

import episimbiomechanics.EpisimModelConnector;

public interface EpisimCellBehavioralModel {
	
	void setEpisimModelConnector(EpisimModelConnector _modelConnector);
	void setSendReceiveAlgorithm(SendReceiveAlgorithm _sendReceiveAlgorithm);
	String getIdOfRequiredEpisimModelConnector();
	
	void setNumberProperty(int propertycode, double val);
	void setMinNumberProperty(int propertycode, double val);
	void setMaxNumberProperty(int propertycode, double val);
	void setStringProperty(int propertycode, String val);
	void setBooleanProperty(int propertycode, boolean val);
	
	double returnNumberProperty(int propertycode);
	double returnMinNumberProperty(int propertycode);
	double returnMaxNumberProperty(int propertycode);
	String returnStringProperty(int propertycode);
	boolean returnBooleanProperty(int propertycode);
	
	EpisimDifferentiationLevel getDiffLevel();
	void setDiffLevel(EpisimDifferentiationLevel val);
	
	EpisimCellType getCellType();
	void setCellType(EpisimCellType val);
	
	double getAge();
	void setAge(double val);	
	double getMaxAge();
	void setMaxAge(double val);
		
	boolean getIsAlive();
	void setIsAlive(boolean val);
	
	int getColorR();
	void setColorR(int val);
	
	int getColorG();
	void setColorG(int val);
	
	int getColorB();
	void setColorB(int val);
	
	int getId();
	void setId(int val);
	
	double getDnaContent();
	void setDnaContent(double val);
	
	void setNumberOfNeighbours(int val);
	
	EpisimCellBehavioralModel[] oneStep(EpisimCellBehavioralModel[] neighbours);
	
	EpisimSbmlModelConfiguration[] getAllEpisimSbmlModelConfigurations();
	
	void setEpisimSbmlModelConnector(EpisimSbmlModelConnector _modelConnector);
	
	EpisimSbmlModelConnector getEpisimSbmlModelConnector();
	void updateAllSbmlModelParameterValuesFromConnector();
	
}

