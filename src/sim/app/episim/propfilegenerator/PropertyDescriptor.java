package sim.app.episim.propfilegenerator;


public class PropertyDescriptor {
	
	private double lowerBound;
	private double upperBound;
	private double stepSize;
	private String propertyName;
	private Class<?> type;
	
	public PropertyDescriptor(String propertyName, Class<?> type, double lowerBound, double upperBound, double stepSize){
		this.propertyName = propertyName;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.stepSize = stepSize;
		this.type = type;
	}
	
	public double getLowerBound() {
	
		return lowerBound;
	}
	
	public double getUpperBound() {
	
		return upperBound;
	}
	
	public double getStepSize() {
	
		return stepSize;
	}
	
	public String getPropertyName() {
	
		return propertyName;
	}
	
	public Class<?> getType() {
		
		return type;
	}

}
