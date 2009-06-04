package episiminterfaces;


public interface CalculationAlgorithmDescriptor {
	
	long getID();
	int getType();
	String getName();
	String getDescription();
	boolean hasCondition();
	

}
