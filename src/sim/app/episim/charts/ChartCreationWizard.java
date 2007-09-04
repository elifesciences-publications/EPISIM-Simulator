package sim.app.episim.charts;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.lang.reflect.Method;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.NumberFormatter;


public class ChartCreationWizard extends JDialog {
	
	private JList cellTypeList;
	private JList cellTypeParameterList;
	
	
	private JScrollPane cellTypeListScroll;
	private JScrollPane cellTypeParameterListScroll;
	
	
	private JButton selectButton;
	
	private JPanel mathFunctionsPanel;
	
	private JPanel variableListPanel;
	
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
	   
	   
	   
	   
	   c.fill = GridBagConstraints.NONE;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.weightx =0;
	   
	   selectButton = new JButton("OK");
	   getContentPane().add(selectButton, c);
	   selectButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				ChartController.getInstance().checkChartExpression("");
				
			}
	   });
	   
	  
	   c.anchor =GridBagConstraints.WEST;
	   c.fill = GridBagConstraints.NONE;
	   c.weightx = 0;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   
	   
	   
	   
	   mathFunctionsPanel = buildMathOperatorButtonPanel();
	   
	   getContentPane().add(mathFunctionsPanel, c);
	   /*
	   JTextField numberField = new JTextField();
	   numberField.setInputVerifier(new InputVerifier() {

			public boolean verify(javax.swing.JComponent input) {

				javax.swing.JTextField jTF = (javax.swing.JTextField) input;
				String sInput = jTF.getText();
				try{
					Double.parseDouble(sInput);
					input.setForeground(java.awt.Color.BLACK);
					return true;
				}
				catch (NumberFormatException ex){
					input.setForeground(java.awt.Color.RED);
					return false;

				}

			}
			public boolean shouldYieldFocus(javax.swing.JComponent input) {

				return verify(input);
			}
		});
		
		numberField.addKeyListener(new KeyAdapter(){
			public void keyTyped(KeyEvent e) {
			
				if(e.getSource() instanceof JTextField){
					
					String text=((JTextField) e.getSource()).getText()+e.getKeyChar();
					
					if(text != null && !text.equals("")){
					try{
					Double.parseDouble(text.trim());
					((JTextField) e.getSource()).setForeground(java.awt.Color.BLACK);
					
				}
				catch (NumberFormatException ex){
					((JTextField) e.getSource()).setForeground(java.awt.Color.RED);
					

				}
				}
				}
				
			}
			
			
             });

	   
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.gridx = 4;
	   c.gridy = 0;
	   getContentPane().add(numberField, c);
	   
	   */
	  // ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	   setSize(500, 600);
		
	}
	
	public void showCellTypes(Map<String, ChartMonitoredCellType> cellTypes){
		
		final Map<String, ChartMonitoredCellType> cellTypesMap = cellTypes;
		DefaultListModel listModel = new DefaultListModel();
	
		Set<String> cellTypeNames = cellTypes.keySet();
		for(String actCellTypeName :cellTypeNames)listModel.addElement(actCellTypeName); 
		
		cellTypeList.setModel(listModel);
		cellTypeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cellTypeList.addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {

				if (e.getValueIsAdjusting() == false) {

			        if (cellTypeList.getSelectedIndex() == -1) {
			        //No selection
			            

			        } else {
			        
			        		showParameters(cellTypesMap.get(((String) cellTypeList.getSelectedValue())));
			            
			        }
			    }
			}
			
			
		});
		repaint();
		centerMe();
		setVisible(true);
		
	}
	
	private void showParameters(ChartMonitoredClass monitoredClass){
	
		DefaultListModel listModel = new DefaultListModel();
		for(Method actMethod :monitoredClass.getParameters())listModel.addElement(actMethod.getName().substring(3));
		cellTypeParameterList.setModel(listModel);
		
	}
	
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	
	private JPanel buildMathOperatorButtonPanel(){
		JButton plus = new JButton("+");
		JButton minus = new JButton("-");
		JButton mult = new JButton("*");
		JButton div = new JButton("/");
		
		JButton oBra = new JButton("(");
		JButton cBra = new JButton(")");
		
		
		JPanel buttonPanel = new JPanel(new GridLayout(2,3,5,5));
		
		buttonPanel.add(plus);
		buttonPanel.add(minus);
		buttonPanel.add(mult);
		buttonPanel.add(div);
		buttonPanel.add(oBra);
		buttonPanel.add(cBra);
		
		
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Math-Functions"), 
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		return buttonPanel;
			
	
	}
	
	private JPanel buildVariableListPanel(){
		
		
		JPanel listPanel = new JPanel(new GridLayout(1,2,5,5));
		cellTypeList = new JList();
		cellTypeParameterList = new JList();
		

		
		
		cellTypeListScroll = new JScrollPane(cellTypeList);
	   cellTypeParameterListScroll = new JScrollPane(cellTypeParameterList);
	
	   listPanel.add(cellTypeListScroll);
	   
	   listPanel.add(cellTypeParameterListScroll);
	   
	 
		
	   listPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Varibles"), 
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		
		return listPanel;
			
	
	}
}


