package sim.app.episim.datamonitoring.charts;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
	
	

	
	private JPanel buttonPanel;

	
	
	private 	JButton okButton;
	
	private JDialog dialog;
	
	
	
	private Set<String> newActivatedCharts;
	
	private JPanel checkBoxPanel;
	
	public DefaultChartSelectDialog(Frame owner, String title, boolean modal, Map<String, Boolean> namesOfDefaultChartsAndActivationStatus){
		super(owner, title, modal);
		
		
		newActivatedCharts = new HashSet<String>();
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
	   
	   int height = 130 + namesOfDefaultChartsAndActivationStatus.size()*30;
	   if(height > 400) height = 400;
	   setSize(350, height);
		validate();
		centerMe();
		dialog = this;
		
		dialog.addWindowListener(new WindowAdapter(){			

				public void windowClosing(WindowEvent e) {
	
		        resetSelectionStatus();
		         
	         }

			});
	}
	
	
	
	
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	public void setVisible(boolean v){
		super.setVisible(v);
		if(v && this.newActivatedCharts != null) newActivatedCharts.clear();
		
   }
	
	
	private JPanel buildCheckBoxPanel(Map<String, Boolean> namesAndActivation){
		checkBoxPanel = new JPanel(new GridLayout(namesAndActivation.size(), 1, 5, 5));
		checkBoxPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		 
		for(final String actString: namesAndActivation.keySet()){
			final JCheckBox checkBox = new JCheckBox();
			checkBox.setText(actString);
			checkBox.setSelected(namesAndActivation.get(actString));
			checkBox.addChangeListener(new ChangeListener(){

				public void stateChanged(ChangeEvent e) {

	            if(checkBox.isSelected()){
	            	newActivatedCharts.add(actString);
	            	
	            	if(newActivatedCharts.size() >0){
	            		okButton.setEnabled(true);
	            	}	            	
	            	ChartController.getInstance().activateDefaultChart(actString);
	            }
	            else if(!checkBox.isSelected()){
	            	newActivatedCharts.remove(actString);
	            	if(newActivatedCharts.size() <=0){
	            		okButton.setEnabled(false);
	            	}
	            	
	            	ChartController.getInstance().deactivateDefaultChart(actString);
	            }
	            
            }
				
			});
			checkBoxPanel.add(checkBox);
		}
		
		return checkBoxPanel;
	}
	
	
	private void resetSelectionStatus(){
		if(checkBoxPanel != null){
			for(Component comp: checkBoxPanel.getComponents()){
				if(comp instanceof JCheckBox){
					JCheckBox check = (JCheckBox) comp;
					if(this.newActivatedCharts.contains(check.getText())) check.setSelected(false);			
				}
			}
			this.newActivatedCharts.clear();
		}
	}
	
   private JPanel buildButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());
		JPanel bInnerPanel = new JPanel(new BorderLayout(10,10));
		GridBagConstraints c = new GridBagConstraints();

		

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
							resetSelectionStatus();			
					//dialog.setVisible(false);
					//dialog.dispose();
					
				

			}
		});
		bInnerPanel.add(cancelButton, BorderLayout.WEST);
		

		okButton = new JButton("  OK  ");
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

						ChartController.getInstance().registerDefaultChartsAtServer();
										
						dialog.setVisible(false);
						dialog.dispose();
					}
		});
		
		
		bInnerPanel.add(okButton, BorderLayout.EAST);
		

		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		
		bPanel.add(bInnerPanel, c);
		return bPanel;

	}

}



