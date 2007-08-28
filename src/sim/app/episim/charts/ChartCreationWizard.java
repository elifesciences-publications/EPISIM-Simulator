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
	private JList selectedCellTypeParameters;
	
	private JScrollPane cellTypeListScroll;
	private JScrollPane cellTypeParameterListScroll;
	private JScrollPane selectedCellTypeParametersScroll;
	
	private JButton addButton;
	
	
	public ChartCreationWizard(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		cellTypeList = new JList();
		cellTypeParameterList = new JList();
		selectedCellTypeParameters = new JList();

		
		
		cellTypeListScroll = new JScrollPane(cellTypeList);
	   cellTypeParameterListScroll = new JScrollPane(cellTypeParameterList);
		selectedCellTypeParametersScroll = new JScrollPane(selectedCellTypeParameters);
		
		addButton = new JButton("Select");
		addButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				
				
			}
		});	
		

		
		
	
	   getContentPane().setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridx = 0;
	   c.gridy = 0;
	   getContentPane().add(cellTypeListScroll, c);
	   
	   c.gridx = 1;
	   c.gridy = 0;
	   getContentPane().add(cellTypeParameterListScroll, c);
	   
	   c.fill = GridBagConstraints.NONE;
	   c.weightx =0;
	   c.gridx = 2;
	   c.gridy = 0;
	   
	   getContentPane().add(addButton, c);
	   addButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

				System.out.println("Add Button");
				
			}
	   });
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.gridx = 3;
	   c.gridy = 0;
	   getContentPane().add(selectedCellTypeParametersScroll, c);
	   
	   
	   
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
	   
	   
	  // ((JPanel) this.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	   setSize(500, 200);
		
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
	

}
