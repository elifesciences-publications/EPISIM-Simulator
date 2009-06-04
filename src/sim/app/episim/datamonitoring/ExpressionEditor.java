package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartCreationWizard;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.Names;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.app.episim.util.TissueCellDataFieldsInspector.ParameterSelectionListener;


public class ExpressionEditor extends JDialog implements ParameterSelectionListener{
	
	public static final int CHARTBASELINEROLE = 1;
	public static final int CHARTSERIESROLE = 2;
	public static final int DATAEXPORTROLE = 3;
	
	
	
	
	private static final int MATHEMATICALEXPRESSIONTEXTAREA = 1;
	private static final int BOOLEANEXPRESSIONTEXTAREA = 2;
	
	private JTextArea arithmeticExpressionTextArea;
	private JTextArea booleanExpressionTextArea;
	
	
	private JPanel arithmeticMessagePanel;
	private JPanel arithmeticExpressionPanel;
	private JPanel booleanMessagePanel;
	private JPanel booleanExpressionPanel;
	private JPanel buttonPanel;
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	private JTextArea arithmeticMessageTextArea;
	private JTextArea booleanMessageTextArea;
	
	private int role;
	
	private JDialog dialog;
	//index 0: expression not compiled; index 1: expression compiled
	private String [] arithmeticExpression = new String[2];
	private String [] booleanExpression = new String[2];
	
	private int activatedTextArea = 0;
	private boolean booleanCondition = false;
	
	public ExpressionEditor(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector, int role){
		this(owner, title, modal, _dataFieldsInspector, role, false);
	}
	
	
	public ExpressionEditor(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector, int role, boolean _booleanCondition){
		super(owner, title, modal);
		this.role = role;
		this.booleanCondition = _booleanCondition;
	   getContentPane().setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   
	   if(_dataFieldsInspector != null)this.dataFieldsInspector = _dataFieldsInspector;
	   else throw new IllegalArgumentException("Datafield Inspector is null!");
	   
	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   getContentPane().add(this.dataFieldsInspector.getVariableListPanel(), c);
	  	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   arithmeticExpressionPanel = buildArithmeticExpressionPanel();
	   getContentPane().add(arithmeticExpressionPanel, c);
	  	  
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.arithmeticMessageTextArea = new JTextArea();
	   this.arithmeticMessagePanel=buildMessageTextAreaPanel(arithmeticMessageTextArea);
	   getContentPane().add(arithmeticMessagePanel, c);
	   this.arithmeticMessagePanel.setVisible(false);
	   
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =0.7;
	   booleanExpressionPanel = buildBooleanExpressionPanel();
	   getContentPane().add(booleanExpressionPanel, c);
	  	this.booleanExpressionPanel.setVisible(booleanCondition);  
	   
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   this.booleanMessageTextArea = new JTextArea();
	   this.booleanMessagePanel=buildMessageTextAreaPanel(booleanMessageTextArea);
	   getContentPane().add(booleanMessagePanel, c);
	   this.booleanMessagePanel.setVisible(false);
	   
	   
	   
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   this.buttonPanel = buildButtonPanel();
	   getContentPane().add(buttonPanel, c);
	   
	   this.dataFieldsInspector.addParameterSelectionListener(this);
	   
	   
	   this.addWindowListener(new WindowAdapter() {
	   	public void windowActivated(WindowEvent e) {
	   		arithmeticExpressionTextArea.requestFocusInWindow();
	   	}
	   	});
	   
	   setSize(750, 800);
		validate();
		dialog = this;
	}
	public void parameterWasSelected() {

		if(this.activatedTextArea == MATHEMATICALEXPRESSIONTEXTAREA) insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.arithmeticExpressionTextArea);
		else if(this.activatedTextArea == BOOLEANEXPRESSIONTEXTAREA) insertStringInChartExpressionAtCursor(dataFieldsInspector.getActualSelectedParameter(), this.booleanExpressionTextArea);
      
   }
	
	public String[][] getExpressions(String[] oldArithmeticExpression, String[] oldBooleanExpression){
					
			if(oldArithmeticExpression != null && oldArithmeticExpression.length >=2){
				arithmeticExpression = oldArithmeticExpression;
				if(arithmeticExpression[0] != null) arithmeticExpressionTextArea.setText(arithmeticExpression[0]);
				if(arithmeticExpression[1] != null && !arithmeticExpression[1].equals("")){
					arithmeticMessageTextArea.setText(arithmeticExpression[1]);
					arithmeticMessagePanel.setVisible(true);
				}
				
			}
			if(booleanCondition && oldBooleanExpression != null && oldBooleanExpression.length >=2){
				booleanExpression = oldBooleanExpression;
				if(booleanExpression[0] != null) booleanExpressionTextArea.setText(booleanExpression[0]);
				if(booleanExpression[1] != null && !booleanExpression[1].equals("")){
					booleanMessageTextArea.setText(booleanExpression[1]);
					booleanMessagePanel.setVisible(true);
				}
				
			}
									
			repaint();
			centerMe();
			setVisible(true);
			
		return new String[][]{arithmeticExpression, booleanExpression};
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
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
		
		
		area = new JTextArea();
		
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
   
   
   
   
   private JPanel buildButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;

		JButton okButton = new JButton("  OK  ");
		okButton.addActionListener(new ActionListener() {
			
			

			public void actionPerformed(ActionEvent e) {

				try{
					String result = ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(arithmeticExpressionTextArea.getText().trim(), dataFieldsInspector);
					arithmeticExpression[0]=arithmeticExpressionTextArea.getText().trim();
					if(result != null && !result.trim().equals("")){ 
						arithmeticMessageTextArea.setText(result);
						arithmeticMessageTextArea.setVisible(true);
						arithmeticExpression[1]=result.trim();
					}

					
					if(!booleanCondition){
						dialog.setVisible(false);
						dialog.dispose();
					}
				}
				catch (ParseException e1){
					arithmeticMessageTextArea.setText(e1.getMessage());
					arithmeticMessageTextArea.setVisible(true);
				}
				catch (TokenMgrError e1){
					arithmeticMessageTextArea.setText(e1.getMessage());
					arithmeticMessageTextArea.setVisible(true);
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
	
							dialog.setVisible(false);
							dialog.dispose();
						
					}
					catch (ParseException e1){
						booleanMessageTextArea.setText(e1.getMessage());
						booleanMessageTextArea.setVisible(true);
					}
					catch (TokenMgrError e1){
						booleanMessageTextArea.setText(e1.getMessage());
						booleanMessageTextArea.setVisible(true);
					}
				}

			}
		});
		bPanel.add(okButton, c);

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		c.gridwidth = 1;

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}

}


