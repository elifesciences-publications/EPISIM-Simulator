package episiminterfaces;

public interface CalculationAlgorithm extends java.io.Serializable{

	public static final int ONEDIMRESULT = 1;
	public static final int TWODIMRESULT = 2;
	public static final int HISTOGRAM = 3;
	
	CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id);
	
	
}
