package episiminterfaces;


public interface EpisimCellDiffModel {
	
	void setNumberProperty(int propertycode, double val);
	void setStringProperty(int propertycode, String val);
	void setBooleanProperty(int propertycode, boolean val);
	
	double getNumberProperty(int propertycode);
	String getStringProperty(int propertycode);
	boolean getBooleanProperty(int propertycode);
	

	double getMaxCa();
 	void setMaxCa(double val);
	 
 	double getMaxLam();
 	void setMaxLam(double val);

 	int getMaxAge();
	void setMaxAge(int val);
	
	double getCa();
	void setCa(double val);
	
	double getLam();
	void setLam(double val);
	
	double getLip();
	void setLip(double val);
	
	int getDifferentiation();
	void setDifferentiation(int val);
	
	int getAge();
	void setAge(int val);

}
