package sim.app.episim.datamonitoring.charts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.TissueCellDataFieldsInspector;


public class DefaultChartSelectDialog extends JDialog {
	
	

	private JTextArea chartExpressionTextArea;
	
	
	private JPanel textAreaPanel;
	private JPanel formulaPanel;
	private JPanel buttonPanel;
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	private JTextArea messageTextArea;
	
	
	
	private JDialog dialog;
	
	
	public DefaultChartSelectDialog(Frame owner, String title, boolean modal, Map<String, Boolean> namesOfDefaultChartsAndActivationStatus){
		super(owner, title, modal);
			
	   getContentPane().setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   
	   if(namesOfDefaultChartsAndActivationStatus == null) throw new IllegalArgumentException("Default Charts Name and Activation Status Map was null!");
	   
	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   getContentPane().add(new JScrollPane(buildCheckBoxPanel(namesOfDefaultChartsAndActivationStatus)), c);
	  	   
	  
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   this.buttonPanel = buildButtonPanel();
	   getContentPane().add(buttonPanel, c);
	   
	   this.addWindowListener(new WindowAdapter(){
	   	public void windowClosing(WindowEvent e){
	   	 		ChartController.getInstance().resetToOldDefaultChartSelectionValues();
	   	}
	   });
	   
	   this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	   
	   setSize(300, 200);
		validate();
		centerMe();
		dialog = this;
	}
	
	
	
	
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	
	
	
	private JPanel buildCheckBoxPanel(Map<String, Boolean> namesAndActivation){
		JPanel checkBoxPanel = new JPanel(new GridLayout(namesAndActivation.size(), 1, 5, 5));
		checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		 
		for(final String actString: namesAndActivation.keySet()){
			final JCheckBox checkBox = new JCheckBox();
			checkBox.setText(actString);
			checkBox.setSelected(namesAndActivation.get(actString));
			checkBox.addChangeListener(new ChangeListener(){

				public void stateChanged(ChangeEvent e) {

	            if(checkBox.isSelected()){
	            	ChartController.getInstance().activateDefaultChart(actString);
	            }
	            else ChartController.getInstance().deactivateDefaultChart(actString);
	            
            }
				
			});
			checkBoxPanel.add(checkBox);
		}
		
		return checkBoxPanel;
	}
	
	
	
	
   private JPanel buildButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.CENTER;
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

					ChartController.getInstance().registerDefaultChartsAtServer();
										
					dialog.setVisible(false);
					dialog.dispose();
					
				

			}
		});
		bPanel.add(okButton, c);
		return bPanel;

	}

}



