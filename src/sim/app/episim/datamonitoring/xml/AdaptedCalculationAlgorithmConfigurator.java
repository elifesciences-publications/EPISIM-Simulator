package sim.app.episim.datamonitoring.xml;

import java.util.Map;


public class AdaptedCalculationAlgorithmConfigurator implements java.io.Serializable{
	private int calculationAlgorithmID;
	private String[] booleanExpression;
	private String[] arithmeticExpression;
	private Map<String, Object> parameters;	
	private  boolean booleanExpressionOnlyInitiallyChecked;

	public AdaptedCalculationAlgorithmConfigurator(){}

	
   public int getCalculationAlgorithmID() {
   
   	return calculationAlgorithmID;
   }

	
   public void setCalculationAlgorithmID(int calculationAlgorithmID) {
   
   	this.calculationAlgorithmID = calculationAlgorithmID;
   }

	
   public String[] getBooleanExpression() {
   
   	return booleanExpression;
   }

	
   public void setBooleanExpression(String[] booleanExpression) {
   
   	this.booleanExpression = booleanExpression;
   }

	
   public String[] getArithmeticExpression() {
   
   	return arithmeticExpression;
   }

	
   public void setArithmeticExpression(String[] arithmeticExpression) {
   
   	this.arithmeticExpression = arithmeticExpression;
   }

	
   public Map<String, Object> getParameters() {
   
   	return parameters;
   }

	
   public void setParameters(Map<String, Object> parameters) {
   
   	this.parameters = parameters;
   }

	
   public boolean isBooleanExpressionOnlyInitiallyChecked() {
   
   	return booleanExpressionOnlyInitiallyChecked;
   }

	
   public void setBooleanExpressionOnlyInitiallyChecked(boolean booleanExpressionOnlyInitiallyChecked) {
   
   	this.booleanExpressionOnlyInitiallyChecked = booleanExpressionOnlyInitiallyChecked;
   }
}
