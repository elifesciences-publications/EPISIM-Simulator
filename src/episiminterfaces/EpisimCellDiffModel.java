package episiminterfaces;

import episimbiomechanics.EpisimModelIntegrator;

public interface EpisimCellDiffModel {
	
	void setEpisimModelIntegrator(EpisimModelIntegrator _modelIntegrator);
	
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
	

	double getMaxCa();
 	void setMaxCa(double val);
	 
 	double getMaxLam();
 	void setMaxLam(double val);

 	double getMaxAge();
	void setMaxAge(double val);
	
	double getCa();
	void setCa(double val);
	
	double getLam();
	void setLam(double val);
	
	double getLip();
	void setLip(double val);
	
	int getDifferentiation();
	void setDifferentiation(int val);
	
	int getSpecies();
	void setSpecies(int val);
	
	double getAge();
	void setAge(double val);
		
	double getX();
	void setX(double val);
	
	double getY();
	void setY(double val);
	
	double getDx();
	void setDx(double val);
	
	double getDy();
	void setDy(double val);
	
	boolean getIsAlive();
	void setIsAlive(boolean val);
	
	boolean getIsSurface();
	void setIsSurface(boolean val);
	
	boolean getIsMembrane();
	void setIsMembrane(boolean val);
	
	boolean getHasCollision();
	void setHasCollision(boolean val);
	
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
	
	EpisimCellDiffModel[] oneStep(EpisimCellDiffModel[] neighbours);
	
	

}

