package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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

public class ExpressionEditorPanel implements ParameterSelectionListener{	
	
	private static final int MATHEMATICALEXPRESSIONTEXTAREA = 1;
	private static final int BOOLEANEXPRESSIONTEXTAREA = 2;
	private JTextArea arithmeticExpressionTextArea;
	private JTextArea booleanExpressionTextArea;
	
	
	private JPanel parametersPanel;
	private JPanel arithmeticMessagePanel;
	private JPanel arithmeticExpressionPanel;
	private JPanel booleanMessagePanel;
	private JPanel booleanExpressionPanel;
	
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	private JTextArea arithmeticMessageTextArea;
	private JTextArea booleanMessageTextArea;
	
	
	
	private JPanel panel;
	//index 0: expression not compiled; index 1: expression compiled
	private String [] arithmeticExpression = new String[2];
	private String [] booleanExpression = new String[2];
	private Map<String, Object> parameterValues = new HashMap<String, Object>();
	private int calculationAlgorithmID;
	
	private int activatedTextArea = 0;
	private boolean booleanCondition = false;
	
	private boolean hasParameters = false;
	
	public ExpressionEditorPanel(TissueCellDataFieldsInspector _dataFieldsInspector, CalculationAlgorithmDescriptor descriptor){
		if(_dataFieldsInspector == null) throw new IllegalArgumentException(this.getClass().getName() + "One of the Constructor Parameters was null!");
		this.calculationAlgorithmID = descriptor.getID();
		
		panel = new JPanel();
		this.booleanCondition = descriptor.hasCondition();
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
	   panel.add(this.dataFieldsInspector.getVariableListPanel(), c);
	   
	   if(descriptor != null && descriptor.getParameters() != null && descriptor.getParameters().size() > 0){
	   	hasParameters = true;
		   c.fill = GridBagConstraints.BOTH;
		   c.gridwidth = GridBagConstraints.REMAINDER;
		   c.weighty =0.7;
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
	   
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   booleanExpressionPanel = buildBooleanExpressionPanel();
	   panel.add(booleanExpressionPanel, c);
	  	this.booleanExpressionPanel.setVisible(booleanCondition);  
	   
	   
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

				arithmeticExpressionTextArea.requestFocusInWindow();
	         
         }});
	  
	   
	   
	}
	public void parameterWasSelected() {

		if(this.activatedTextArea == MATHEMATICALEXPRESSIONTEXTAREA) insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.arithmeticExpressionTextArea);
		else if(this.activatedTextArea == BOOLEANEXPRESSIONTEXTAREA) insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.booleanExpressionTextArea);
      
   }
	
	public JPanel getExpressionEditorPanel(){ return panel; }
	
	
	public void setExpressionEditorPanelData(CalculationAlgorithmConfigurator config){
		if(config.getCalculationAlgorithmID() != this.calculationAlgorithmID) throw new IllegalArgumentException("The CalculationAlgorithmConfigurator does not match the selected calculation algorithm. ID found:  " + config.getCalculationAlgorithmID() + " ID required: " + this.calculationAlgorithmID);
		this.parameterValues = new HashMap<String, Object>();
			if(config !=null){			
			if(config.getArithmeticExpression() != null && config.getArithmeticExpression().length >=2){
				arithmeticExpression = config.getArithmeticExpression();
				if(arithmeticExpression[0] != null) arithmeticExpressionTextArea.setText(arithmeticExpression[0]);
				if(arithmeticExpression[1] != null && !arithmeticExpression[1].equals("")){
					arithmeticMessageTextArea.setText(arithmeticExpression[1]);
					arithmeticMessagePanel.setVisible(true);
				}
				
			}
			if(booleanCondition && config.getBooleanExpression() != null && config.getBooleanExpression().length >=2){
				booleanExpression = config.getBooleanExpression();
				if(booleanExpression[0] != null) booleanExpressionTextArea.setText(booleanExpression[0]);
				if(booleanExpression[1] != null && !booleanExpression[1].equals("")){
					booleanMessageTextArea.setText(booleanExpression[1]);
					booleanMessagePanel.setVisible(true);
				}
				
			}
			setParameterValues(config.getParameters());
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
				n.setValue((Double) map.get(n.getName()));
			}		
		}	
	}
	
	public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator(){
		try{
			 if(hasParameters)fetchParameterValues();
			String result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(arithmeticExpressionTextArea.getText().trim(), dataFieldsInspector);
			arithmeticExpression[0]=arithmeticExpressionTextArea.getText().trim();
			if(result != null && !result.trim().equals("")){ 
				arithmeticMessageTextArea.setText(result);
				arithmeticMessageTextArea.setVisible(true);
				arithmeticExpression[1]=result.trim();
			}					
			if(!booleanCondition){
				return CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(calculationAlgorithmID, arithmeticExpression, new String[]{null, null}, parameterValues);
					
			}
		}
		catch (ParseException e1){
			arithmeticMessagePanel.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextArea.setText(e1.getMessage());
		}
		catch (TokenMgrError e1){
			arithmeticMessagePanel.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextArea.setText(e1.getMessage());
		}
		
		if(booleanCondition){
			try{
				String result = ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(booleanExpressionTextArea.getText().trim(), dataFieldsInspector);
				booleanExpression[0]=booleanExpressionTextArea.getText().trim();
				if(result != null && !result.trim().equals("")){ 
					booleanMessageTextArea.setText(result);
					booleanMessageTextArea.setVisible(true);
					booleanExpression[1]=result.trim();
				}

				return CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(calculationAlgorithmID, arithmeticExpression, booleanExpression, parameterValues);
				
			}
			catch (ParseException e1){
				booleanMessagePanel.setVisible(true);
				this.panel.validate();
				booleanMessageTextArea.setText(e1.getMessage());
			}
			catch (TokenMgrError e1){
				booleanMessagePanel.setVisible(true);
				this.panel.validate();
				booleanMessageTextArea.setText(e1.getMessage());
			}
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

			public void focusGained(FocusEvent e) {activatedTextArea = MATHEMATICALEXPRESSIONTEXTAREA; }

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
					propField = new PropertyField(name, "false", true, null, PropertyField.SHOW_CHECKBOX){
						public void setVerifyInputWhenFocusTarget(boolean
						      verifyInputWhenFocusTarget) {							  
						  super.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
						  checkField.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
						  if(!verifyInputWhenFocusTarget)checkField.setInputVerifier( new InputVerifier() {			
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
					propField = new NumberTextField(name, 0, false){
						 public double newValue(double newValue){	return (int)newValue; }
						  public void setVerifyInputWhenFocusTarget(boolean
							      verifyInputWhenFocusTarget) {							  
							  super.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  valField.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  if(!verifyInputWhenFocusTarget)valField.setInputVerifier( new InputVerifier() {			
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
					propField = new NumberTextField(name, 0, false){
						 public void setVerifyInputWhenFocusTarget(boolean
							      verifyInputWhenFocusTarget) {							  
							  super.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  valField.setVerifyInputWhenFocusTarget(verifyInputWhenFocusTarget);
							  if(!verifyInputWhenFocusTarget)valField.setInputVerifier( new InputVerifier() {			
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

			public void focusGained(FocusEvent e) {activatedTextArea = BOOLEANEXPRESSIONTEXTAREA;   }

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
				parameterValues.put(n.getName(),n.getValue());
   		}
   	}
   }
   
   
   


}
