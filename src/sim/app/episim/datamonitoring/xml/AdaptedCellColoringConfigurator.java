package sim.app.episim.datamonitoring.xml;


public class AdaptedCellColoringConfigurator implements java.io.Serializable{
	private String[] arithmeticExpressionColorR ;
	private String[] arithmeticExpressionColorG;
	private String[] arithmeticExpressionColorB;			

	public AdaptedCellColoringConfigurator(){}

	
   public String[] getArithmeticExpressionColorR() {
   
   	return arithmeticExpressionColorR;
   }

	
   public void setArithmeticExpressionColorR(String[] arithmeticExpressionColorR) {
   
   	this.arithmeticExpressionColorR = arithmeticExpressionColorR;
   }

	
   public String[] getArithmeticExpressionColorG() {
   
   	return arithmeticExpressionColorG;
   }

	
   public void setArithmeticExpressionColorG(String[] arithmeticExpressionColorG) {
   
   	this.arithmeticExpressionColorG = arithmeticExpressionColorG;
   }

	
   public String[] getArithmeticExpressionColorB() {
   
   	return arithmeticExpressionColorB;
   }

	
   public void setArithmeticExpressionColorB(String[] arithmeticExpressionColorB) {
   
   	this.arithmeticExpressionColorB = arithmeticExpressionColorB;
   }

}
