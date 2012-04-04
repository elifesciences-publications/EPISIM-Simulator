package sim.app.episim.datamonitoring.xml;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;


import episiminterfaces.calc.CalculationAlgorithmConfigurator;


public class CalculationAlgorithmConfiguratorAdapter extends XmlAdapter<AdaptedCalculationAlgorithmConfigurator, CalculationAlgorithmConfigurator> implements java.io.Serializable{
	
   public CalculationAlgorithmConfigurator unmarshal(final AdaptedCalculationAlgorithmConfigurator v) throws Exception {
	   
	   return new CalculationAlgorithmConfigurator(){			
         public int getCalculationAlgorithmID() {
	         return v.getCalculationAlgorithmID();
         }
			
         public String[] getBooleanExpression() {
	        return v.getBooleanExpression();
         }

			
         public String[] getArithmeticExpression() {
	         return v.getArithmeticExpression();
         }

         public Map<String, Object> getParameters() {	        
	         return v.getParameters();
         }

         public boolean isBooleanExpressionOnlyInitiallyChecked() {
	         return v.isBooleanExpressionOnlyInitiallyChecked();
         }};
   }

   public AdaptedCalculationAlgorithmConfigurator marshal(CalculationAlgorithmConfigurator v) throws Exception {
   	
   	AdaptedCalculationAlgorithmConfigurator config = new AdaptedCalculationAlgorithmConfigurator();
	   
   	config.setArithmeticExpression(v.getArithmeticExpression());
   	config.setBooleanExpression(v.getBooleanExpression());
   	config.setBooleanExpressionOnlyInitiallyChecked(v.isBooleanExpressionOnlyInitiallyChecked());
   	config.setCalculationAlgorithmID(v.getCalculationAlgorithmID());
   	config.setParameters(v.getParameters());
   	
	   return config;
   }

}
