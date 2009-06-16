package episiminterfaces.calc;


public interface CalculationAlgorithm extends java.io.Serializable{

	public static final int ONEDIMRESULT = 1;
	public static final int TWODIMRESULT = 2;
	public static final int TWODIMDATASERIESRESULT = 3;
	public static final int HISTOGRAMRESULT = 4;
	CalculationAlgorithmDescriptor getCalculationAlgorithmDescriptor(int id);
	
	
}
