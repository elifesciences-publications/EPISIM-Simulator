package sim.app.episim.charts;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.lang.reflect.Method;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;

import sim.app.episim.charts.parser.ParseException;
import sim.app.episim.charts.parser.TokenMgrError;


public class ChartCreationWizard extends JDialog {
	
	private JList cellTypeList;
	private JList cellTypeParameterList;
	
	
	
	
	
	
	
	private JTextArea chartExpressionArea;
	
	private JPanel variableListPanel;
	private JPanel textAreaPanel;
	private JPanel formulaPanel;
	private JPanel buttonPanel;
	
	private JTextArea messageTextArea;
	
	private Set<String> varNameSet;
	
	private Map<String, ChartMonitoredCellType> cellTypesMap;
	
	private JDialog dialog;
	
	public ChartCreationWizard(Frame owner, String title, boolean modal){
		super(owner, title, modal);
			
	   getContentPane().setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   
	   variableListPanel = buildVariableListPanel();
	   
	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   getContentPane().add(variableListPanel, c);
	   
	   
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   c.weighty =1;
	   formulaPanel = buildFormulaPanel();
	   getContentPane().add(formulaPanel, c);
	   
	   
	  
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   this.textAreaPanel=buildTextAreaPanel();
	   
	   getContentPane().add(textAreaPanel, c);
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   this.buttonPanel = buildButtonPanel();
	   getContentPane().add(buttonPanel, c);
	   
	   this.addWindowListener(new WindowAdapter() {
	   	public void windowActivated(WindowEvent e) {
	   		chartExpressionArea.requestFocusInWindow();
	   	}
	   	});
	   
	   setSize(500, 400);
		validate();
		dialog = this;
	}
	
	public void showCellTypes(Map<String, ChartMonitoredCellType> cellTypes){
		if(cellTypes != null){
			this.cellTypesMap = cellTypes;
			buildVarNameSet(cellTypes);

			
			DefaultListModel listModel = new DefaultListModel();

			Set<String> cellTypeNames = cellTypes.keySet();
			for(String actCellTypeName : cellTypeNames)
				listModel.addElement(actCellTypeName);

			cellTypeList.setModel(listModel);

			repaint();
			centerMe();
			setVisible(true);
		}
		
	}
	
	private void showParameters(ChartMonitoredClass monitoredClass){
	
		DefaultListModel listModel = new DefaultListModel();
		for(Method actMethod :monitoredClass.getParameters()){
			
			if(isValidReturnType(actMethod.getReturnType()))
				listModel.addElement(getParameterName(actMethod.getName()));
			
		}
		cellTypeParameterList.setModel(listModel);
		
	}
	
	
	private boolean isValidReturnType(Class<?> cls){
		if(cls.isPrimitive() 
				&&!cls.isAssignableFrom(String.class)
				&&!cls.isAssignableFrom(Boolean.TYPE)) 
			return true;
		
		return false;
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	private void buildVarNameSet(Map<String, ChartMonitoredCellType> cellTypes){
		
		this.varNameSet = new HashSet<String>();
		Set<String> cellTypeNames = cellTypes.keySet();
		for(String actCellTypeName :cellTypeNames){
			ChartMonitoredCellType actClass = cellTypes.get(actCellTypeName); 
		
			for(Method actMethod :actClass.getParameters())varNameSet.add(actCellTypeName+"."+getParameterName(actMethod.getName()));
		}	
	}
	
	
	private JPanel buildVariableListPanel(){
		
		
		JPanel listPanel = new JPanel(new GridLayout(1,2,5,5));
		cellTypeList = new JList();
		cellTypeParameterList = new JList();
		

		cellTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellTypeParameterList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		
		cellTypeList.addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {

			        if ((e.getValueIsAdjusting() != false) && cellTypeList.getSelectedIndex() != -1) {
			        
			      	  showParameters(cellTypesMap.get(((String) cellTypeList.getSelectedValue())));
			            
			        }
			}
			
			
		});
		cellTypeParameterList.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent e) {
				
			        if ((cellTypeParameterList.getSelectedIndex() != -1)&& e.getClickCount() == 2) {
			          	  insertStringInChartExpressionAtCursor(cellTypeList.getSelectedValue()
			       				+"."
			        				+cellTypeParameterList.getSelectedValue());
			       }
		    }
		});
		
		cellTypeParameterList.setToolTipText("double-click to select!");
		
		
		
		
		
		
		JScrollPane cellTypeListScroll = new JScrollPane(cellTypeList);
		JScrollPane cellTypeParameterListScroll = new JScrollPane(cellTypeParameterList);
	
	   listPanel.add(cellTypeListScroll);
	   
	   listPanel.add(cellTypeParameterListScroll);
	   
	 
		
	   listPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Variables"), 
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		return listPanel;
			
	
	}
	
	private String getParameterName(String str){
		if(str.startsWith("get")) return str.substring(3);
		else if(str.startsWith("is")) return str.substring(2);
		else return null;
	}
	
	private void insertStringInChartExpressionAtCursor(String str){
		int curPos = chartExpressionArea.getCaretPosition();
		int selStart = chartExpressionArea.getSelectionStart();
		int selEnd = chartExpressionArea.getSelectionEnd(); 
		
		if(chartExpressionArea.getSelectedText() == null)
			
		chartExpressionArea.setText(
				chartExpressionArea.getText().trim().substring(0,curPos)
				+str
				+chartExpressionArea.getText().trim().substring(curPos));
		else{
			chartExpressionArea.setText(
					chartExpressionArea.getText().trim().substring(0,selStart)
					+str
					+chartExpressionArea.getText().trim().substring(selEnd));
		}
	}
	
	
	private JPanel buildTextAreaPanel(){
		
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

	

		chartExpressionArea = new JTextArea();
		JScrollPane scroll = new JScrollPane(chartExpressionArea);
		chartExpressionArea.setFocusCycleRoot(true);
		chartExpressionArea.setFocusable(true);
		chartExpressionArea.setInputVerifier(new InputVerifier() {

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
							chartExpressionArea.getText().trim(), varNameSet);
					messageTextArea.setText(result);
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


