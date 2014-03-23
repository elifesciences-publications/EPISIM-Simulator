package sim.app.episim.datamonitoring.xml;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import episiminterfaces.calc.CellColoringConfigurator;

public class CellColoringConfiguratorAdapter extends XmlAdapter<AdaptedCellColoringConfigurator, CellColoringConfigurator> implements java.io.Serializable{
	
   public CellColoringConfigurator unmarshal(final AdaptedCellColoringConfigurator v) throws Exception {	   
	   return new CellColoringConfigurator(){
		   	public String[] getArithmeticExpressionColorR(){
		   		return v.getArithmeticExpressionColorR();
		   	}
		   	public String[] getArithmeticExpressionColorG(){
		   		return v.getArithmeticExpressionColorG();
		   	}
		   	public String[] getArithmeticExpressionColorB(){
		   		return v.getArithmeticExpressionColorB();
		   	}
         };
   }

   public AdaptedCellColoringConfigurator marshal(CellColoringConfigurator v) throws Exception {
   	if(v!=null){
   		AdaptedCellColoringConfigurator config = new AdaptedCellColoringConfigurator();
   		config.setArithmeticExpressionColorR(v.getArithmeticExpressionColorR());
   		config.setArithmeticExpressionColorG(v.getArithmeticExpressionColorG());
	   	config.setArithmeticExpressionColorB(v.getArithmeticExpressionColorB());
		   return config;
   	}
   	else return null;
   }

}
