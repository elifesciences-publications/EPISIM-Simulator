package sim.app.episim.charts;

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

import sim.app.episim.charts.parser.ParseException;
import sim.app.episim.charts.parser.TokenMgrError;
import sim.app.episim.util.Names;
import sim.app.episim.util.TissueCellDataFieldsInspector;


public class ChartExpressionEditor extends JDialog {
	
	

	private JTextArea chartExpressionTextArea;
	
	
	private JPanel textAreaPanel;
	private JPanel formulaPanel;
	private JPanel buttonPanel;
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	private JTextArea messageTextArea;
	
	
	
	private JDialog dialog;
	//index 0: expression not compiled; index 1: expression compiled
	private String [] expression = new String[2];
	
	public ChartExpressionEditor(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector){
		super(owner, title, modal);
			
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
	   formulaPanel = buildFormulaPanel();
	   getContentPane().add(formulaPanel, c);
	  	  
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.7;
	   c.insets = new Insets(10,10,10,10);
	   this.textAreaPanel=buildMessageTextAreaPanel();
	   
	   getContentPane().add(textAreaPanel, c);
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   this.buttonPanel = buildButtonPanel();
	   getContentPane().add(buttonPanel, c);
	   
	   
	   this.dataFieldsInspector.getCellParameterList().addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((dataFieldsInspector.getCellParameterList().getSelectedIndex() != -1) && e.getClickCount() == 2){
					insertStringInChartExpressionAtCursor(dataFieldsInspector.getCellTypeList().getSelectedValue() + "."
					      + dataFieldsInspector.getCellParameterList().getSelectedValue());
				}
			}
		});
	   
	   this.dataFieldsInspector.getTissueParameterList().addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if((dataFieldsInspector.getTissueParameterList().getSelectedIndex() != -1) && e.getClickCount() == 2){
					insertStringInChartExpressionAtCursor(Names.GLOBALSIMULATIONVALUE +"."
					      + dataFieldsInspector.getTissueParameterList().getSelectedValue());
				}
			}
		});
	   
	   this.addWindowListener(new WindowAdapter() {
	   	public void windowActivated(WindowEvent e) {
	   		chartExpressionTextArea.requestFocusInWindow();
	   	}
	   	});
	   
	   setSize(500, 600);
		validate();
		dialog = this;
	}
	
	public String[] getExpression(String[] oldExpression){
					
			if(oldExpression != null && oldExpression.length >=2){
				expression = oldExpression;
				if(expression[0] != null) chartExpressionTextArea.setText(expression[0]);
				if(expression[1] != null) messageTextArea.setText(expression[1]);
				
			}
									
			repaint();
			centerMe();
			setVisible(true);
			
		return expression;
	}
	
	
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	
	
	
	
	
	
	
	private void insertStringInChartExpressionAtCursor(String str){
		int curPos = chartExpressionTextArea.getCaretPosition();
		int selStart = chartExpressionTextArea.getSelectionStart();
		int selEnd = chartExpressionTextArea.getSelectionEnd(); 
		
		if(chartExpressionTextArea.getSelectedText() == null)
			
		chartExpressionTextArea.setText(
				chartExpressionTextArea.getText().trim().substring(0,curPos)
				+str
				+chartExpressionTextArea.getText().trim().substring(curPos));
		else{
			chartExpressionTextArea.setText(
					chartExpressionTextArea.getText().trim().substring(0,selStart)
					+str
					+chartExpressionTextArea.getText().trim().substring(selEnd));
		}
	}
	
	
	private JPanel buildMessageTextAreaPanel(){
		
		JPanel areaPanel = new JPanel(new BorderLayout(5,5));
		
		
		this.messageTextArea = new JTextArea();
		
		messageTextArea.setEditable(false);
		messageTextArea.setLineWrap(true);
		
		JScrollPane scroll = new JScrollPane(messageTextArea);
		
		 areaPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Messages"), 
					BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		 areaPanel.add(scroll, BorderLayout.CENTER);
		 return areaPanel;
		
	}
	
   private JPanel buildFormulaPanel() {

		JPanel formPanel = new JPanel(new BorderLayout(5, 5));

	

		chartExpressionTextArea = new JTextArea();
		JScrollPane scroll = new JScrollPane(chartExpressionTextArea);
		chartExpressionTextArea.setFocusCycleRoot(true);
		chartExpressionTextArea.setFocusable(true);
		chartExpressionTextArea.setInputVerifier(new InputVerifier() {

			public boolean verify(JComponent input) {

				return false;
			}

			public boolean shouldYieldFocus(JComponent input) {

				return verify(input);
			}

		});

		formPanel.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Calculation Formula"), 
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
					String result = ChartController.getInstance().checkChartExpression(
							chartExpressionTextArea.getText().trim(), dataFieldsInspector.getVarNameSet() );
					messageTextArea.setText(result);
					expression[0]=chartExpressionTextArea.getText().trim();
					expression[1]=result;
					
					dialog.setVisible(false);
					dialog.dispose();
					
				}
				catch (ParseException e1){
					messageTextArea.setText(e1.getMessage());
				}
				catch (TokenMgrError e1){
					messageTextArea.setText(e1.getMessage());
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


