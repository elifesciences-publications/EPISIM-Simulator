package sim.app.episim.propfilegenerator;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.util.gui.NumberTextField;


public class PropertyPanel extends JPanel{
	
	private Class<?> type = null;
	private String propertyName = "";
	private String propertysDefaultValue = "";
	private NumberTextField lowerBoundTextField;
	private NumberTextField upperBoundTextField;
	private NumberTextField stepsTextField;
	
	public PropertyPanel(String propertyName,  Class<?> type, String propertysDefaultValue){
		if(type == null || propertyName == null) throw new IllegalArgumentException("Arguments property and type must not be null!");
		
		double defaultValue = 0;
		this.propertyName = propertyName;
		this.type = type;
		if(propertysDefaultValue != null){
			defaultValue = Double.parseDouble(propertysDefaultValue);
		}		
		
		JPanel textFieldPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		   
		c.anchor =GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.65;
		c.weighty =0;
		c.insets = new Insets(0, 5, 0 ,5);
		c.gridwidth = GridBagConstraints.RELATIVE;
		textFieldPanel.add(buildBoundaryPanel(defaultValue, type), c);
		
		c.anchor =GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.35;
		c.weighty =0;
		c.insets = new Insets(0, 5, 0 ,5);
		c.gridwidth = GridBagConstraints.REMAINDER;
		textFieldPanel.add(buildStepsPanel(type), c);
		
		this.setLayout(new BorderLayout(10,10));
		
		
		this.add(textFieldPanel, BorderLayout.CENTER);
		
	}
	
	private JPanel buildBoundaryPanel(double defaultValue, Class<?> type){
		JPanel mainBoundaryPanel = new JPanel(new BorderLayout(5,5));
		JPanel textFieldPanel = new JPanel(new GridLayout(1,2,5,5));
		lowerBoundTextField = getNumberTextField(defaultValue, type, false);
		upperBoundTextField = getNumberTextField(defaultValue, type, false);
		if(lowerBoundTextField != null && upperBoundTextField != null){
			textFieldPanel.add(lowerBoundTextField);
			textFieldPanel.add(upperBoundTextField);
		}
		
		mainBoundaryPanel.add(new JLabel("Boundaries:"), BorderLayout.WEST);
		mainBoundaryPanel.add(textFieldPanel, BorderLayout.CENTER);
		return mainBoundaryPanel;
	}
	
	private JPanel buildStepsPanel(Class<?> type){
		JPanel mainStepsPanel = new JPanel(new BorderLayout(5,5));
		mainStepsPanel.add(new JLabel("Stepsize:"), BorderLayout.WEST);
		stepsTextField = getNumberTextField(1d, type, true);
		mainStepsPanel.add(stepsTextField, BorderLayout.CENTER);
		return mainStepsPanel;
	}
	
	private NumberTextField getNumberTextField(double defaultValue, Class<?> type, final boolean isStepsField){
		NumberTextField numberTextField = null;
		if(Double.TYPE.isAssignableFrom(type)
				|| Float.TYPE.isAssignableFrom(type)){
			
			numberTextField = new NumberTextField(defaultValue, false){
				public double newValue(double newValue)
		      {				
					if(isStepsField){
						newValue = checkMaxStepValue(newValue);
						if(newValue == 0) newValue = this.getValue(); 
					}
					return newValue;
		      }
			};
		}
		else if(Integer.TYPE.isAssignableFrom(type)
				|| Short.TYPE.isAssignableFrom(type)
				|| Byte.TYPE.isAssignableFrom(type)){
			
			int defaultValInt = (int) defaultValue;
			numberTextField = new NumberTextField((double)defaultValInt, false){
				public double newValue(double newValue)
		      {				
					newValue = Math.round(newValue);
					if(isStepsField){
						newValue = checkMaxStepValue(newValue);
						if(newValue == 0) newValue = this.getValue(); 
					}
					
					return newValue;
		      }
			};			
		}
		return numberTextField;
	}
	
	private double checkMaxStepValue(double newValue){
		if(newValue<0) newValue = 0;
		if(upperBoundTextField != null && lowerBoundTextField != null){
			double difference = Math.abs(upperBoundTextField.getValue()-lowerBoundTextField.getValue());
			return newValue > difference  ? difference : newValue;
		}
		return newValue;
	}
	
	public double getLowerBound(){ return this.lowerBoundTextField != null ? lowerBoundTextField.getValue() : 0; }
	public double getUpperBound(){ return this.upperBoundTextField != null ? upperBoundTextField.getValue() : 0; }
	public double getStepSize(){ return this.stepsTextField != null ? stepsTextField.getValue() : 0; }
	public String getPropertyName(){ return this.propertyName; }
	public PropertyDescriptor getPropertyDescriptor(){ return new PropertyDescriptor(propertyName, type, lowerBoundTextField.getValue(), upperBoundTextField.getValue(), stepsTextField.getValue());}
	

}
