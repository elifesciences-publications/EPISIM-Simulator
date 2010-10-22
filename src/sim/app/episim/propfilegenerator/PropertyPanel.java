package sim.app.episim.propfilegenerator;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.util.gui.NumberTextField;


public class PropertyPanel extends JPanel{
	
	private Class<?> type = null;
	private String propertyName = "";
	private String propertysDefaultValue = "";
	private NumberTextField numberTextField;
	
	public PropertyPanel(String propertyName,  Class<?> type, String propertysDefaultValue){
		if(type == null || propertyName == null) throw new IllegalArgumentException("Arguments property and type must not be null!");
		
		double defaultValue = 0;
		this.propertyName = propertyName;
		
		if(propertysDefaultValue != null){
			defaultValue = Double.parseDouble(propertysDefaultValue);
		}		
		if(Double.TYPE.isAssignableFrom(type)
				|| Float.TYPE.isAssignableFrom(type)){
			
			numberTextField = new NumberTextField(defaultValue, false);
			
		}
		else if(Integer.TYPE.isAssignableFrom(type)
				|| Short.TYPE.isAssignableFrom(type)
				|| Byte.TYPE.isAssignableFrom(type)){
			
			int defaultValInt = (int) defaultValue;
			numberTextField = new NumberTextField((double)defaultValInt, false){
				public double newValue(double newValue)
		      {				
					newValue = Math.round(newValue);				  
					return newValue;
		      }
			};			
		}
		
		this.setLayout(new BorderLayout(10,10));
		this.add(new JLabel(propertyName +":"), BorderLayout.WEST);
		this.add(numberTextField, BorderLayout.CENTER);
		
	}

}
