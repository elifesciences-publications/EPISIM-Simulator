package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sim.app.episim.datamonitoring.ExpressionEditorPanel.ExpressionState;
import sim.app.episim.datamonitoring.ExpressionEditorPanel.ExpressionType;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmConfiguratorFactory;
import sim.app.episim.datamonitoring.calc.CellColoringConfigurationFactory;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.app.episim.util.TissueCellDataFieldsInspector.ParameterSelectionListener;
import sim.util.gui.NumberTextField;
import sim.util.gui.PropertyField;
import sim.util.gui.PropertyFieldHack;
import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CellColoringConfigurator;


public class CellColoringConfigurationPanel implements ParameterSelectionListener{	
	
	private JTextArea arithmeticExpressionTextAreaColorR;
	private JTextArea arithmeticExpressionTextAreaColorG;
	private JTextArea arithmeticExpressionTextAreaColorB;
	
	
	public static boolean SHOWMESSAGEFIELDS = false;
	
	

	private JPanel arithmeticMessagePanelColorR;
	private JPanel arithmeticExpressionPanelColorR;
	
	private JPanel arithmeticMessagePanelColorG;
	private JPanel arithmeticExpressionPanelColorG;
	
	private JPanel arithmeticMessagePanelColorB;
	private JPanel arithmeticExpressionPanelColorB;

	
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	private JTextArea arithmeticMessageTextAreaColorR;
	private JTextArea arithmeticMessageTextAreaColorG;	
	private JTextArea arithmeticMessageTextAreaColorB;	
	
	private JPanel panel;
	//index 0: expression not compiled; index 1: expression compiled
	private String [] expressionColorR = new String[2];
	private String [] expressionColorG = new String[2];
	private String [] expressionColorB = new String[2];	

	private Component parentComponent;
	
	private ExpressionType expressionType = ExpressionType.MATHEMATICAL_EXPRESSION;
	
	private ExpressionState mathExpressionStateColorR = ExpressionState.ERROR;
	private ExpressionState mathExpressionStateColorG = ExpressionState.ERROR;
	private ExpressionState mathExpressionStateColorB = ExpressionState.ERROR;
	
	private JTextArea activeTextArea = null;
	
	
	public CellColoringConfigurationPanel(Component parent, TissueCellDataFieldsInspector _dataFieldsInspector){
		this.parentComponent = parent;
		if(_dataFieldsInspector == null) throw new IllegalArgumentException(this.getClass().getName() + "One of the Constructor Parameters was null!");
				
		panel = new JPanel();
				
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
	   panel.add(this.dataFieldsInspector.getVariableListPanel(parentComponent), c);
	   
	   //-----------------------------------------------------------------------------------------------------------------
	   // Color R
	   //-----------------------------------------------------------------------------------------------------------------
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   arithmeticExpressionTextAreaColorR = new JTextArea();
	   this.activeTextArea = arithmeticExpressionTextAreaColorR;
	   arithmeticExpressionPanelColorR = buildArithmeticExpressionPanel(arithmeticExpressionTextAreaColorR, "Color-R");
	   panel.add(arithmeticExpressionPanelColorR, c);
	  	this.arithmeticExpressionPanelColorR.setVisible(true);  
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.arithmeticMessageTextAreaColorR = new JTextArea();
	   this.arithmeticMessagePanelColorR=buildMessageTextAreaPanel(arithmeticMessageTextAreaColorR);
	   panel.add(arithmeticMessagePanelColorR, c);
	   this.arithmeticMessagePanelColorR.setVisible(false);
	   
	   //-----------------------------------------------------------------------------------------------------------------
	   // Color G
	   //-----------------------------------------------------------------------------------------------------------------
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   arithmeticExpressionTextAreaColorG = new JTextArea();
	   arithmeticExpressionPanelColorG = buildArithmeticExpressionPanel(arithmeticExpressionTextAreaColorG, "Color-G");
	   panel.add(arithmeticExpressionPanelColorG, c);
	  	this.arithmeticExpressionPanelColorG.setVisible(true);  
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.arithmeticMessageTextAreaColorG = new JTextArea();
	   this.arithmeticMessagePanelColorG=buildMessageTextAreaPanel(arithmeticMessageTextAreaColorG);
	   panel.add(arithmeticMessagePanelColorG, c);
	   this.arithmeticMessagePanelColorG.setVisible(false);
	   
	   //-----------------------------------------------------------------------------------------------------------------
	   // Color B
	   //-----------------------------------------------------------------------------------------------------------------
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   arithmeticExpressionTextAreaColorB = new JTextArea();
	   arithmeticExpressionPanelColorB = buildArithmeticExpressionPanel(arithmeticExpressionTextAreaColorB, "Color-B");
	   panel.add(arithmeticExpressionPanelColorB, c);
	  	this.arithmeticExpressionPanelColorB.setVisible(true);  
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.arithmeticMessageTextAreaColorB = new JTextArea();
	   this.arithmeticMessagePanelColorB=buildMessageTextAreaPanel(arithmeticMessageTextAreaColorB);
	   panel.add(arithmeticMessagePanelColorB, c);
	   this.arithmeticMessagePanelColorB.setVisible(false);
  	   
	   
	   this.dataFieldsInspector.addParameterSelectionListener(this);
	   
	  
	   panel.addComponentListener(new ComponentAdapter(){

			public void componentShown(ComponentEvent e) {
				arithmeticExpressionTextAreaColorR.requestFocusInWindow();	         
         }});
	  
	   
	   
	}
	
	public void setEnabled(boolean val){
	   panel.setEnabled(val);
		setEnabled(panel.getComponents(), val);
	}
	private void setEnabled(Component[] comps, boolean enabled) {
	 
	    for (Component component : comps){
	        component.setEnabled(enabled);
	        if(component instanceof Container){
	      	  setEnabled(((Container)component).getComponents(), enabled);
	        }
	      
	    }	    
	}
	
	
	
	
	
	public void parameterWasSelected() {
		if(this.expressionType == ExpressionType.MATHEMATICAL_EXPRESSION && this.activeTextArea != null){
			insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.activeTextArea);
		}      
   }
	
	public JPanel getCellColoringConfigurationPanel(){ return panel; }
	
	public ExpressionState getCellColoringConfigurationState(){
		if(this.mathExpressionStateColorR==ExpressionState.OK
				&&this.mathExpressionStateColorG==ExpressionState.OK
				&&this.mathExpressionStateColorB==ExpressionState.OK) return ExpressionState.OK;
		
		return ExpressionState.ERROR;
	}
	
	
	public void setCellColoringConfigurationPanelData(CellColoringConfigurator config){
		mathExpressionStateColorR = ExpressionState.ERROR;
		mathExpressionStateColorG = ExpressionState.ERROR;
		mathExpressionStateColorB = ExpressionState.ERROR;
		if(config !=null){			
			if(config.getArithmeticExpressionColorR() != null && config.getArithmeticExpressionColorR().length >=2){
				expressionColorR = config.getArithmeticExpressionColorR();
				if(expressionColorR[0] != null) arithmeticExpressionTextAreaColorR.setText(expressionColorR[0]);
				if(expressionColorR[1] != null && !expressionColorR[1].equals("") && SHOWMESSAGEFIELDS){
					arithmeticMessageTextAreaColorR.setText(expressionColorR[1]);
					arithmeticMessagePanelColorR.setVisible(true);
				}				
			}
			if(config.getArithmeticExpressionColorG() != null && config.getArithmeticExpressionColorG().length >=2){
				expressionColorG = config.getArithmeticExpressionColorG();
				if(expressionColorG[0] != null) arithmeticExpressionTextAreaColorG.setText(expressionColorG[0]);
				if(expressionColorG[1] != null && !expressionColorG[1].equals("") && SHOWMESSAGEFIELDS){
					arithmeticMessageTextAreaColorG.setText(expressionColorG[1]);
					arithmeticMessagePanelColorG.setVisible(true);
				}				
			}
			if(config.getArithmeticExpressionColorB() != null && config.getArithmeticExpressionColorB().length >=2){
				expressionColorB = config.getArithmeticExpressionColorB();
				if(expressionColorB[0] != null) arithmeticExpressionTextAreaColorB.setText(expressionColorB[0]);
				if(expressionColorB[1] != null && !expressionColorB[1].equals("") && SHOWMESSAGEFIELDS){
					arithmeticMessageTextAreaColorB.setText(expressionColorB[1]);
					arithmeticMessagePanelColorB.setVisible(true);
				}				
			}	
		}						
	}
		
	public CellColoringConfigurator getCellColoringConfigurator(){
		int actSessionId = ExpressionCheckerController.getInstance().getCheckSessionId();
		mathExpressionStateColorR = ExpressionState.ERROR;
		mathExpressionStateColorG = ExpressionState.ERROR;
		mathExpressionStateColorB = ExpressionState.ERROR;
	
		
		try{		 
			
				String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(actSessionId, arithmeticExpressionTextAreaColorR.getText().trim(), dataFieldsInspector);
				expressionColorR[0]=result[0];
				if(result[1] != null && !result[1].trim().equals("")){ 
					arithmeticMessageTextAreaColorR.setText(result[1]);
					if(SHOWMESSAGEFIELDS)arithmeticMessageTextAreaColorR.setVisible(true);
					expressionColorR[1]=result[1].trim();
				}					
				if(ExpressionCheckerController.getInstance().hasVarNameConflict(actSessionId, dataFieldsInspector)){				
						arithmeticMessagePanelColorR.setVisible(true);
						this.panel.validate();
						mathExpressionStateColorR = ExpressionState.ERROR;
						arithmeticMessageTextAreaColorR.setText("Usage of parameters belonging to different cell types in a single calculation algorithm is not allowed.");					
				} 
				else mathExpressionStateColorR = ExpressionState.OK;			
		}
		catch (ParseException e1){
			arithmeticMessagePanelColorR.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextAreaColorR.setText(e1.getMessage());
			mathExpressionStateColorR = ExpressionState.ERROR;
		}
		catch (TokenMgrError e1){
			arithmeticMessagePanelColorR.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextAreaColorR.setText(e1.getMessage());
			mathExpressionStateColorR = ExpressionState.ERROR;
		}	
		
		try{		
			String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(actSessionId, arithmeticExpressionTextAreaColorG.getText().trim(), dataFieldsInspector);
			expressionColorG[0]=result[0];
			if(result[1] != null && !result[1].trim().equals("")){ 
				arithmeticMessageTextAreaColorG.setText(result[1]);
				if(SHOWMESSAGEFIELDS)arithmeticMessageTextAreaColorG.setVisible(true);
				expressionColorG[1]=result[1].trim();
			}					
			if(ExpressionCheckerController.getInstance().hasVarNameConflict(actSessionId, dataFieldsInspector)){				
					arithmeticMessagePanelColorG.setVisible(true);
					this.panel.validate();
					mathExpressionStateColorG = ExpressionState.ERROR;
					arithmeticMessageTextAreaColorG.setText("Usage of parameters belonging to different cell types in a single calculation algorithm is not allowed.");					
			} 
			else mathExpressionStateColorG = ExpressionState.OK;			
		}
		catch (ParseException e1){
			arithmeticMessagePanelColorG.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextAreaColorG.setText(e1.getMessage());
			mathExpressionStateColorG = ExpressionState.ERROR;
		}
		catch (TokenMgrError e1){
			arithmeticMessagePanelColorG.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextAreaColorG.setText(e1.getMessage());
			mathExpressionStateColorG = ExpressionState.ERROR;
		}
		
		try{		
			String[] result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(actSessionId, arithmeticExpressionTextAreaColorB.getText().trim(), dataFieldsInspector);
			expressionColorB[0]=result[0];
			if(result[1] != null && !result[1].trim().equals("")){ 
				arithmeticMessageTextAreaColorB.setText(result[1]);
				if(SHOWMESSAGEFIELDS)arithmeticMessageTextAreaColorB.setVisible(true);
				expressionColorB[1]=result[1].trim();
			}					
			if(ExpressionCheckerController.getInstance().hasVarNameConflict(actSessionId, dataFieldsInspector)){				
					arithmeticMessagePanelColorB.setVisible(true);
					this.panel.validate();
					mathExpressionStateColorB = ExpressionState.ERROR;
					arithmeticMessageTextAreaColorB.setText("Usage of parameters belonging to different cell types in a single calculation algorithm is not allowed.");					
			} 
			else mathExpressionStateColorB = ExpressionState.OK;			
		}
		catch (ParseException e1){
			arithmeticMessagePanelColorB.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextAreaColorB.setText(e1.getMessage());
			mathExpressionStateColorB = ExpressionState.ERROR;
		}
		catch (TokenMgrError e1){
			arithmeticMessagePanelColorB.setVisible(true);
			this.panel.validate();
			arithmeticMessageTextAreaColorB.setText(e1.getMessage());
			mathExpressionStateColorB = ExpressionState.ERROR;
		}		
		if(mathExpressionStateColorR == ExpressionState.OK && mathExpressionStateColorG == ExpressionState.OK && mathExpressionStateColorB == ExpressionState.OK){
			return CellColoringConfigurationFactory.createCellColoringConfiguratorObject(expressionColorR, expressionColorG, expressionColorB);
		}
		else return null;
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
	
   private JPanel buildArithmeticExpressionPanel(final JTextArea textArea, String borderTitle) {
		JPanel formPanel = new JPanel(new BorderLayout(5, 5));
	
		textArea.setVerifyInputWhenFocusTarget(false);
		JScrollPane scroll = new JScrollPane(textArea);
		textArea.setFocusCycleRoot(true);
		textArea.setFocusable(true);
		textArea.setInputVerifier(new InputVerifier() {			
			public boolean verify(JComponent input) { return false; }
			public boolean shouldYieldFocus(JComponent input) { return verify(input); }
		});
		textArea.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				expressionType = ExpressionType.MATHEMATICAL_EXPRESSION;
				activeTextArea = textArea;
				dataFieldsInspector.setExpressionType(expressionType);
			}
			public void focusLost(FocusEvent e) {}});
		
		formPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(borderTitle), 
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		formPanel.add(scroll, BorderLayout.CENTER);
		return formPanel;
	}  
}
