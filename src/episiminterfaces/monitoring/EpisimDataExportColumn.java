package episiminterfaces.monitoring;



public interface EpisimDataExportColumn extends java.io.Serializable{
	
	long getId();
	String getName();
	String[] getCalculationExpression();	
	
	void setName(String val);
	void setCalculationExpression(String[] exp);
}
