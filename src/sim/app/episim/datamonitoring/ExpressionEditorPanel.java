package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmConfiguratorFactory;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.app.episim.util.TissueCellDataFieldsInspector.ParameterSelectionListener;
import sim.util.gui.NumberTextField;
import sim.util.gui.PropertyField;
import sim.util.gui.PropertyFieldHack;

public class ExpressionEditorPanel implements ParameterSelectionListener{
	
	
	public enum ExpressionType {BOOLEAN_EXPRESSION, MATHEMATICAL_EXPRESSION}
	
	public enum ExpressionState {OK, ERROR}
	
	private JTextArea arithmeticExpressionTextArea;
	private JTextArea booleanExpressionTextArea;
	
	public static boolean SHOWMESSAGEFIELDS = false;
	
	
	private JPanel parametersPanel;
	private JPanel arithmeticMessagePanel;
	private JPanel arithmeticExpressionPanel;
	private JPanel booleanMessagePanel;
	private JPanel booleanExpressionPanel;
	private JPanel conditionCheckBoxPanel;
	
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	private JTextArea arithmeticMessageTextArea;
	private JTextArea booleanMessageTextArea;
	
	private JCheckBox conditionOnlyInitiallyCheckedCheckBox;
	
	
	private JPanel panel;
	//index 0: expression not compiled; index 1: expression compiled
	private String [] arithmeticExpression = new String[2];
	private String [] booleanExpression = new String[2];
	private Map<String, Object> parameterValues = new HashMap<String, Object>();
	private int calculationAlgorithmID;

	private boolean hasBooleanCondition = false;
	private boolean hasMathematicalExpression = false;
	private boolean hasParameters = false;
	
	private CalculationAlgorithmDescriptor currentCalculationAlgorithmDescriptor;
	private Component parentComponent;
	
	private ExpressionType expressionType = ExpressionType.MATHEMATICAL_EXPRESSION;
	
	private boolean isConditionOnlyInitiallyChecked = false;
	
	private ExpressionState mathExpressionState = ExpressionState.ERROR;
	private ExpressionState boolExpressionState = ExpressionState.ERROR;
	
	public ExpressionEditorPanel(Component parent, TissueCellDataFieldsInspector _dataFieldsInspector, CalculationAlgorithmDescriptor descriptor){
		this.parentComponent = parent;
		if(_dataFieldsInspector == null) throw new IllegalArgumentException(this.getClass().getName() + "One of the Constructor Parameters was null!");
		this.calculationAlgorithmID = descriptor.getID();
		this.currentCalculationAlgorithmDescriptor = descriptor;
		
		panel = new JPanel();
		
		this.hasBooleanCondition = descriptor.hasCondition();
		this.hasMathematicalExpression = descriptor.hasMathematicalExpression();
		
		panel.setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   
	   if(_dataFieldsInspector != null)this.dataFieldsInspector = _dataFieldsInspector;
	   else throw new IllegalArgumentException("Datafield Inspector is null!");
	   
	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   if(this.hasBooleanCondition || this.hasMathematicalExpression)panel.add(this.dataFieldsInspector.getVariableListPanel(parentComponent), c);
	   
	   if(descriptor != null && descriptor.getParameters() != null && descriptor.getParameters().size() > 0){
	   	hasParameters = true;
		   c.fill = GridBagConstraints.BOTH;
		   c.gridwidth = GridBagConstraints.REMAINDER;
		   c.weighty =0.35;
		   parametersPanel = buildParameterPanel(descriptor);
		   JScrollPane scroll = new JScrollPane(parametersPanel);
		   scroll.setBorder(null);
		   
		   JPanel framePanel = new JPanel(new BorderLayout());
		   framePanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Parameters"), 
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));		
		   framePanel.add(scroll, BorderLayout.CENTER);
		   panel.add(framePanel, c);
		} 
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   arithmeticExpressionPanel = buildArithmeticExpressionPanel();
	   panel.add(arithmeticExpressionPanel, c);
	  	this.arithmeticExpressionPanel.setVisible(hasMathematicalExpression);  
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.arithmeticMessageTextArea = new JTextArea();
	   this.arithmeticMessagePanel=buildMessageTextAreaPanel(arithmeticMessageTextArea);
	   panel.add(arithmeticMessagePanel, c);
	   this.arithmeticMessagePanel.setVisible(false);
	   
	   
	   
	   c.fill = GridBagConstraints.NONE;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0;
	   c.weightx =1;
	   conditionCheckBoxPanel = new JPanel(new BorderLayout());
	   conditionOnlyInitiallyCheckedCheckBox = new JCheckBox("Check fulfillment of condition only once", false);
	   conditionOnlyInitiallyCheckedCheckBox.addActionListener(new ActionListener(){
	   	public void actionPerformed(ActionEvent e) {
	   		isConditionOnlyInitiallyChecked = conditionOnlyInitiallyCheckedCheckBox.isSelected();
	         
         }});
	   conditionCheckBoxPanel.add(conditionOnlyInitiallyCheckedCheckBox, BorderLayout.WEST);
	   panel.add(conditionCheckBoxPanel, c);
	  	this.conditionCheckBoxPanel.setVisible(hasBooleanCondition);
	   
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   booleanExpressionPanel = buildBooleanExpressionPanel();
	   panel.add(booleanExpressionPanel, c);
	  	this.booleanExpressionPanel.setVisible(hasBooleanCondition);  
	   
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.booleanMessageTextArea = new JTextArea();
	   this.booleanMessagePanel=buildMessageTextAreaPanel(booleanMessageTextArea);
	   panel.add(booleanMessagePanel, c);
	   this.booleanMessagePanel.setVisible(false);
	   	   
	   
	   this.dataFieldsInspector.addParameterSelectionListener(this);
	   
	  
	   panel.addComponentListener(new ComponentAdapter(){

			public void componentShown(ComponentEvent e) {

				if(hasMathematicalExpression)arithmeticExpressionTextArea.requestFocusInWindow();
				else booleanExpressionTextArea.requestFocusInWindow();
	         
         }});
	  
	   
	   
	}
	public void parameterWasSelected() {

		if(this.expressionType == ExpressionType.MATHEMATICAL_EXPRESSION) insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.arithmeticExpressionTextArea);
		else if(this.expressionType == ExpressionType.BOOLEAN_EXPRESSION) insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.booleanExpressionTextArea);
      
   }
	
	public JPanel getExpressionEditorPanel(){ return panel; }
	
	public ExpressionState getBooleanConditionState(){ return this.boolExpressionState; }
	public ExpressionState getMathematicalConditionState(){ return this.mathExpressionState;}
	
	public void setExpressionEditorPanelData(CalculationAlgorithmConfigurator config){
		mathExpressionState = ExpressionState.ERROR;
		boolExpressionState = ExpressionState.ERROR;
		if(config.getCalculationAlgorithmID() != this.calculationAlgorithmID) throw new IllegalArgumentException("The CalculationAlgorithmConfigurator does not match the selected calculation algorithm. ID found:  " + config.getCalculationAlgorithmID() + " ID required: " + this.calculationAlgorithmID);
		this.parameterValues = new HashMap<String, Object>();
			if(config !=null){			
			if(hasMathematicalExpression && config.getArithmeticExpression() != null && config.getArithmeticExpression().length >=2){
				arithmeticExpression = config.getArithmeticExpression();
				if(arithmeticExpression[0] != null) arithmeticExpressionTextArea.setText(arithmeticExpression[0]);
				if(arithmeticExpression[1] != null && !arithmeticExpression[1].equals("") && SHOWMESSAGEFIELDS){
					arithmeticMessageTextArea.setText(arithmeticExpression[1]);
					arithmeticMessagePanel.setVisible(true);
				}
				
			}
			if(hasBooleanCondition && config.getBooleanExpression() != null && config.getBooleanExpression().length >=2){
				booleanExpression = config.getBooleanExpression();
				if(booleanExpression[0] != null) booleanExpressionTextArea.setText(booleanExpression[0]);
				if(booleanExpression[1] != null && !booleanExpression[1].equals("")&& SHOWMESSAGEFIELDS){
					booleanMessageTextArea.setText(booleanExpression[1]);
					booleanMessagePanel.setVisible(true);
				}
				
			}
			if(hasBooleanCondition){
				isConditionOnlyInitiallyChecked=config.isBooleanExpressionOnlyInitiallyChecked();
				conditionOnlyInitiallyCheckedCheckBox.setSelected(isConditionOnlyInitiallyChecked);
			}
			if(hasParameters)setParameterValues(config.getParameters());
		}						
	}
	
	private void setParameterValues(Map<String, Object> map){		
		
		for(Component comp: this.parametersPanel.getComponents()){
			if(comp instanceof PropertyField){
				PropertyField p = (PropertyField) comp;
				parameterValues.put(p.getName(), map.get(p.getName()));
				
				
				if(Boolean.class.isAssignableFrom(map.get(p.getName()).getClass())) p.setValue(Boolean.toString((Boolean) map.get(p.getName())));
			}
			else if(comp instanceof NumberTextField){
				NumberTextField n = (NumberTextField) comp;
				parameterValues.put(n.getName(),map.get(n.getName()));
				n.setValue(getDoubleValue(map.get(n.getName())));
			}		
		}	
	}
	
	public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator(){
		int actSessionId = ExpressionCheckerController.getInstance().getCheckSessionId();
		mathExpressionState = ExpressionState.ERROR;
		boolExpressionState = ExpressionState.ERROR;
		if(hasParameters)fetchParameterValues();
		try{
			 
			 if(hasMathematicalExpression){
				String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(actSessionId, arithmeticExpressionTextArea.getText().trim(), dataFieldsInspector);
				arithmeticExpression[0]=result[0];
				if(result[1] != null && !result[1].trim().equals("")){ 
					arithmeticMessageTextArea.setText(result[1]);
					if(SHOWMESSAGEFIELDS)arithmeticMessageTextArea.setVisible(true);
					arithmeticExpression[1]=result[1].trim();
				}					
				if(!hasBooleanCondition){
					if(!ExpressionCheckerController.getInstance().hasVarNameConflict(actSessionId, dataFieldsInspector)){
						mathExpressionState = ExpressionState.OK;
						boolExpressionState = ExpressionState.OK;
						return CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(calculationAlgorithmID, arithmeticExpression, new String[]{null, null}, false, parameterValues);
					}
					else{
						arithmeticMessagePanel.setVisible(true);
						this.panel.validate();
						mathExpressionState = ExpressionState.ERROR;
						arithmeticMessageTextArea.setText("Usage of parameters belonging to different cell types in a single calculation algorithm is not allowed.");
					}
				} 
				else mathExpressionState = ExpressionState.OK;
			 }
			 else mathExpressionState = ExpressionState.OK;
		}
		catch (ParseException e1){
			arithmeticMessagePanel.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextArea.setText(e1.getMessage());
			mathExpressionState = ExpressionState.ERROR;
		}
		catch (TokenMgrError e1){
			arithmeticMessagePanel.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextArea.setText(e1.getMessage());
			mathExpressionState = ExpressionState.ERROR;
		}
		
		if(hasBooleanCondition){
			try{
				String[] result = ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(actSessionId, booleanExpressionTextArea.getText().trim(), dataFieldsInspector);
				booleanExpression[0]=result[0];
				if(result[1] != null && !result[1].trim().equals("")){ 
					booleanMessageTextArea.setText(result[1]);
					if(SHOWMESSAGEFIELDS)booleanMessageTextArea.setVisible(true);
					booleanExpression[1]=result[1].trim();
				}
				if(!ExpressionCheckerController.getInstance().hasVarNameConflict(actSessionId, dataFieldsInspector)){
					if(!hasMathematicalExpression)mathExpressionState = ExpressionState.OK;
					boolExpressionState = ExpressionState.OK;
					if(hasMathematicalExpression){
						return CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(calculationAlgorithmID, arithmeticExpression, booleanExpression, isConditionOnlyInitiallyChecked, parameterValues);
					}
					else{
						return CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(calculationAlgorithmID, new String[]{null, null}, booleanExpression, isConditionOnlyInitiallyChecked, parameterValues);
					}
				}
				else{
					if(hasMathematicalExpression){
						arithmeticMessagePanel.setVisible(true);
						this.panel.validate();
						arithmeticMessageTextArea.setText("Usage of parameters belonging to different cell types in a single calculation algorithm is not allowed.");
					}
					else{
						booleanMessageTextArea.setVisible(true);
						this.panel.validate();
						booleanMessageTextArea.setText("Usage of parameters belonging to different cell types in a single calculation algorithm is not allowed.");
					}
					mathExpressionState = ExpressionState.ERROR;
					boolExpressionState = ExpressionState.ERROR;
				}
			}
			catch (ParseException e1){
				booleanMessagePanel.setVisible(true);
				this.panel.validate();
				booleanMessageTextArea.setText(e1.getMessage());
				boolExpressionState = ExpressionState.ERROR;
			}
			catch (TokenMgrError e1){
				booleanMessagePanel.setVisible(true);
				this.panel.validate();
				booleanMessageTextArea.setText(e1.getMessage());
				boolExpressionState = ExpressionState.ERROR;
			}
		}
		else boolExpressionState = ExpressionState.OK;
		if(!hasBooleanCondition && !hasMathematicalExpression && !parameterValues.isEmpty()){
			return CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(calculationAlgorithmID, new String[]{null, null}, new String[]{null, null}, false, parameterValues);
		}
		return null;
	}
	
	
	
	
	private void insertStringInChartExpressionAtCursor(String str, JTextArea area){
		int curPos = area.getCaretPosition();
		int selStart = area.getSelectionStart();
		int selEnd = area.getSelectionEnd(); 
		if(curPos > area.getText().trim().length()) curPos = area.getText().trim().length();
		if(selEnd > area.getText().trim().length()) selEnd = area.getText().trim().length();
		if(selStart > area.getText().trim().length()) selStart = area.getText().trim().length();	
		if(area.getSelectedText() == null)		
			area.setText(
					area.getText().trim().substring(0,curPos)
				+str
				+area.getText().trim().substring(curPos));
		else{
			area.setText(
					area.getText().trim().substring(0,selStart)
					+str
					+area.getText().trim().substring(selEnd));
		}
	}
	
	
	private JPanel buildMessageTextAreaPanel(JTextArea area){
		
		JPanel areaPanel = new JPanel(new BorderLayout(5,5));
		
		
		
		
		area.setEditable(false);
		area.setLineWrap(true);
		
		JScrollPane scroll = new JScrollPane(area);
		
		 areaPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Messages"), 
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		 areaPanel.add(scroll, BorderLayout.CENTER);
		 return areaPanel;
		
	}
	
   private JPanel buildArithmeticExpressionPanel() {

		JPanel formPanel = new JPanel(new BorderLayout(5, 5));	

		arithmeticExpressionTextArea = new JTextArea();
		arithmeticExpressionTextArea.setVerifyInputWhenFocusTarget(false);
		JScrollPane scroll = new JScrollPane(arithmeticExpressionTextArea);
		arithmeticExpressionTextArea.setFocusCycleRoot(true);
		arithmeticExpressionTextArea.setFocusable(true);
		arithmeticExpressionTextArea.setInputVerifier(new InputVerifier() {			
			public boolean verify(JComponent input) { return false; }
			public boolean shouldYieldFocus(JComponent input) { return verify(input); }
		});
		arithmeticExpressionTextArea.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {
				expressionType = ExpressionType.MATHEMATICAL_EXPRESSION;
				dataFieldsInspector.setExpressionType(expressionType);
			}

			public void focusLost(FocusEvent e) {}});
		
		formPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Mathematical Expression"), 
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		formPanel.add(scroll, BorderLayout.CENTER);

		return formPanel;

	}
   
   
   private JPanel buildParameterPanel(final CalculationAlgorithmDescriptor descriptor) {
   	JPanel formPanel = null;
   	if(descriptor.getParameters() != null){
			formPanel = new JPanel(new GridBagLayout());	
			GridBagConstraints c = new GridBagConstraints();
		  	  
		   
			for(String name: descriptor.getParameters().keySet()){
				c.anchor =GridBagConstraints.NORTHWEST;
			   c.fill = GridBagConstraints.HORIZONTAL;
			   c.weightx = 1;
			   c.weighty = 0;
			   c.insets = new Insets(5,10,5,10);
			   c.gridwidth = GridBagConstraints.REMAINDER;
				JComponent propField = null;
				if(Boolean.TYPE.isAssignableFrom(descriptor.getParameters().get(name))){
					propField = new PropertyFieldHack(name, "false", true, null, PropertyField.SHOW_CHECKBOX){
						public void setVerifyInputWhenFocusTarget(boolean verifyInputWhenFocusTarget) {							  
						  super.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
						  getCheckField().setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
						  if(!verifyInputWhenFocusTarget)getCheckField().setInputVerifier( new InputVerifier() {			
								public boolean verify(JComponent input) { return false; }
								public boolean shouldYieldFocus(JComponent input) { return verify(input); 
								}
						  });
					  }
					}; 
				}
				else if(Integer.TYPE.isAssignableFrom(descriptor.getParameters().get(name)) 
						|| Byte.TYPE.isAssignableFrom(descriptor.getParameters().get(name))
						|| Short.TYPE.isAssignableFrom(descriptor.getParameters().get(name))){
					propField = new NumberTextField(name, 1, false){
						 public double newValue(double newValue){	return (int)newValue; }
						 public double getValue(){submit(); return super.getValue();}
						  public void setVerifyInputWhenFocusTarget(boolean
							      verifyInputWhenFocusTarget) {							  
							  super.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  getField().setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  if(!verifyInputWhenFocusTarget)getField().setInputVerifier( new InputVerifier() {			
									public boolean verify(JComponent input) { return false; }
									public boolean shouldYieldFocus(JComponent input) { return verify(input); 
									}
							  });
						  }
					};
					((NumberTextField) propField).setFocusable(true);
				}
				else if(Double.TYPE.isAssignableFrom(descriptor.getParameters().get(name)) 
						|| Float.TYPE.isAssignableFrom(descriptor.getParameters().get(name))){
					propField = new NumberTextField(name, 1, false){
						 public double getValue(){submit(); return super.getValue();}
						 public void setVerifyInputWhenFocusTarget(boolean
							      verifyInputWhenFocusTarget) {							  
							  super.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  getField().setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  if(!verifyInputWhenFocusTarget)getField().setInputVerifier( new InputVerifier() {			
									public boolean verify(JComponent input) { return false; }
									public boolean shouldYieldFocus(JComponent input) { return verify(input); 
									}
							  });
						  }
					};
				}
				if(propField != null){
					propField.setName(name);
					
					propField.setVerifyInputWhenFocusTarget(false);
					propField.setInputVerifier(new InputVerifier() {			
						public boolean verify(JComponent input) { return false; }
						public boolean shouldYieldFocus(JComponent input) { return verify(input); }
					});
					formPanel.add(propField, c);
				}
			}
			
			
				
   	}
		return formPanel;

	}
   
   
   
   private JPanel buildBooleanExpressionPanel() {

		JPanel formPanel = new JPanel(new BorderLayout(5, 5));	

		booleanExpressionTextArea = new JTextArea();
		booleanExpressionTextArea.setVerifyInputWhenFocusTarget(false);
		JScrollPane scroll = new JScrollPane(booleanExpressionTextArea);
		booleanExpressionTextArea.setFocusCycleRoot(true);
		booleanExpressionTextArea.setFocusable(true);
		booleanExpressionTextArea.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) { return false; }
			public boolean shouldYieldFocus(JComponent input) { return verify(input); }
		});
		
		booleanExpressionTextArea.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent e) {   
				expressionType = ExpressionType.BOOLEAN_EXPRESSION;
				dataFieldsInspector.setExpressionType(expressionType);
			}

			public void focusLost(FocusEvent e) {}});

		formPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Boolean Expression"), 
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		formPanel.add(scroll, BorderLayout.CENTER);

		return formPanel;

	}
   
   private void fetchParameterValues(){
   	for(Component comp: this.parametersPanel.getComponents()){
   		if(comp instanceof PropertyField){
   			PropertyField p = (PropertyField) comp;
   			this.parameterValues.put(p.getName(), Boolean.parseBoolean(p.getValue()));
   		}
   		else if(comp instanceof NumberTextField){
   			NumberTextField n = (NumberTextField) comp;
   			if(Integer.TYPE.isAssignableFrom(currentCalculationAlgorithmDescriptor.getParameters().get(n.getName()))) parameterValues.put(n.getName(),(int) n.getValue());
   			else if(Byte.TYPE.isAssignableFrom(currentCalculationAlgorithmDescriptor.getParameters().get(n.getName()))) parameterValues.put(n.getName(),(byte) n.getValue());
   			else if(Short.TYPE.isAssignableFrom(currentCalculationAlgorithmDescriptor.getParameters().get(n.getName()))) parameterValues.put(n.getName(),(short) n.getValue());
   			
   			else if(Float.TYPE.isAssignableFrom(currentCalculationAlgorithmDescriptor.getParameters().get(n.getName()))) parameterValues.put(n.getName(),(float) n.getValue());
   			else if(Double.TYPE.isAssignableFrom(currentCalculationAlgorithmDescriptor.getParameters().get(n.getName()))) parameterValues.put(n.getName(),(double) n.getValue());
   			else parameterValues.put(n.getName(),n.getValue());
						
				
   		}
   	}
   }
   
   private double getDoubleValue(Object obj){
   	if(Integer.TYPE.isAssignableFrom(obj.getClass()) || obj instanceof Integer) return (((Integer) obj)+ 0.0d);
		else if(Byte.TYPE.isAssignableFrom(obj.getClass()) || obj instanceof Byte) return (((Byte) obj)+ 0.0d);
		else if(Short.TYPE.isAssignableFrom(obj.getClass()) || obj instanceof Short) return (((Short) obj)+ 0.0d);
		
		else if(Float.TYPE.isAssignableFrom(obj.getClass()) || obj instanceof Float) return (((Float) obj)+ 0.0d);
		else if(Double.TYPE.isAssignableFrom(obj.getClass()) || obj instanceof Double)return (((Double) obj)+ 0.0d);
   	
   	return 0.0d;
   }


}
