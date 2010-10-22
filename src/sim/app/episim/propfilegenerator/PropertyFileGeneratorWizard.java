package sim.app.episim.propfilegenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;


import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.propfilegenerator.GlobalPropertiesObjectInspector.GlobalParameterSelectionListener;



public class PropertyFileGeneratorWizard {
	
	private JDialog dialog;
	private JButton okButton;
	private JComboBox paramTypeCombo;
	private JPanel propertiesListPanel;
	private JPanel selectedParametersPanel;
	private HashSet<String> markerPrefixes;
	private HashSet<Class<?>> validDataTypes;	
	private HashSet<String> alreadyAddedParameterNames;
	private JScrollPane selectedParametersPanelScroll;
	
	
	private PropertyFileType actSelectedParameterType = null;
	
	private enum PropertyFileType{
		CELL_BEHAVIORAL_MODEL_PROPERTIES("Global Cell Behavioral Model Properties"),
		BIOMECHANICAL_MODEL_PROPERTIES("Global Biomechanical Model Properties"),
		MISC_PROPERTIES("Miscellaneous Properties");		
		private String name;
		private PropertyFileType(String _name){ this.name = _name;}
		public String toString(){ return name;}
	}
	
	
	public PropertyFileGeneratorWizard(Frame owner, String title, boolean modal){
		
		markerPrefixes = new HashSet<String>();
		validDataTypes = new HashSet<Class<?>>();
		alreadyAddedParameterNames = new HashSet<String>();
		
		markerPrefixes.add("get");
		
		
		validDataTypes.add(Integer.TYPE);
		validDataTypes.add(Short.TYPE);
		validDataTypes.add(Byte.TYPE);
		validDataTypes.add(Long.TYPE);
		validDataTypes.add(Float.TYPE);
		validDataTypes.add(Double.TYPE);
		validDataTypes.add(Boolean.TYPE);
		
		
		
		
		dialog = new JDialog(owner, title, modal);
		
		dialog.getContentPane().setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.fill = GridBagConstraints.NONE;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   dialog.getContentPane().add(buildComboMenuPanel(), c);
	   
	   
	   
	   
	   	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.4;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   propertiesListPanel = new JPanel(new BorderLayout());
	   dialog.getContentPane().add(propertiesListPanel, c);
	   
	   c.anchor =GridBagConstraints.CENTER; 
	   
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =0.6;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   selectedParametersPanel = new JPanel(new GridLayout(1,1,5,5));
	  
	   JPanel wrapperPanelBorder = new JPanel(new BorderLayout());
	   wrapperPanelBorder.add(selectedParametersPanel, BorderLayout.NORTH);
	  
	   selectedParametersPanelScroll =new JScrollPane(wrapperPanelBorder);
	   selectedParametersPanelScroll.setViewportBorder(new EmptyBorder(10,10,10,10));
	   selectedParametersPanelScroll.setBorder(null);
	   dialog.getContentPane().add(selectedParametersPanelScroll, c);	  	
	   
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(5,10,10,10);
	   
	   dialog.getContentPane().add(buildButtonPanel(), c);
	   
	   dialog.setSize(new Dimension(750,500));
	   dialog.validate();
	   centerMe();
	   dialog.setVisible(true);
	}
	
	private JPanel buildComboMenuPanel(){
		JPanel comboMenuPanel = new JPanel(new BorderLayout(10, 10));
		comboMenuPanel.add(new JLabel("Select Properties Filetype: "), BorderLayout.WEST);
		
		paramTypeCombo = new JComboBox();
		((DefaultComboBoxModel) paramTypeCombo.getModel()).addElement(PropertyFileType.CELL_BEHAVIORAL_MODEL_PROPERTIES);
		((DefaultComboBoxModel) paramTypeCombo.getModel()).addElement(PropertyFileType.BIOMECHANICAL_MODEL_PROPERTIES);
		((DefaultComboBoxModel) paramTypeCombo.getModel()).addElement(PropertyFileType.MISC_PROPERTIES);
		paramTypeCombo.setSelectedIndex(-1);
		paramTypeCombo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {				
				parameterTypeWasChanged();
			}	
		});
		
		comboMenuPanel.add(paramTypeCombo, BorderLayout.EAST);
		return comboMenuPanel;
	}
	
	private void parameterTypeWasChanged(){
		boolean changeParameterType = true;
		if(!alreadyAddedParameterNames.isEmpty()){
			changeParameterType = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(dialog, "Do you really want to discard all already selected Parameters?", "Discard Parameters", JOptionPane.OK_CANCEL_OPTION);
		}
		if(changeParameterType){
			if(paramTypeCombo.getSelectedItem() != null && paramTypeCombo.getSelectedItem() instanceof PropertyFileType){
				actSelectedParameterType = (PropertyFileType)paramTypeCombo.getSelectedItem();
				propertiesListPanel.removeAll();
				selectedParametersPanel.removeAll();
				alreadyAddedParameterNames = new HashSet<String>();
				Object selObj = paramTypeCombo.getSelectedItem();
				Object paramObject = null;
				if(selObj != null){
					if(selObj == PropertyFileType.CELL_BEHAVIORAL_MODEL_PROPERTIES){
						paramObject = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters();
					}
					else if(selObj == PropertyFileType.BIOMECHANICAL_MODEL_PROPERTIES){
						paramObject = ModelController.getInstance().getEpisimMechanicalModelGlobalParameters();
					}
					else if(selObj == PropertyFileType.MISC_PROPERTIES){
						paramObject = MiscalleneousGlobalParameters.getInstance();
					}
					
					if(paramObject != null){
						final GlobalPropertiesObjectInspector inspector = new GlobalPropertiesObjectInspector(paramObject, markerPrefixes, validDataTypes);
						inspector.addGlobalParameterSelectionListener(new GlobalParameterSelectionListener(){
		
							public void parameterWasSelected() {
								addParameter(inspector.getActualSelectedGlobalParameter(), inspector.getActualSelectedGlobalParameterType(), inspector.getActualSelectedGlobalParametersDefaultValue());                  
		               }});
						propertiesListPanel.add(inspector.getGlobalParameterListPanel(), BorderLayout.CENTER);
						dialog.getContentPane().validate();
						dialog.getContentPane().repaint();
					}
				}
			}
		}
		else{
			this.paramTypeCombo.setSelectedItem(actSelectedParameterType);
		}
	}
	
	
	
	
	
	
	
	private JPanel buildButtonPanel() {

			JPanel bPanel = new JPanel(new GridBagLayout());
			JPanel bInnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
			GridBagConstraints c = new GridBagConstraints();			

			okButton = new JButton("  OK  ");
			okButton.setEnabled(false);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {					
						dialog.setVisible(false);
						dialog.dispose();					
				}
			});
			bInnerPanel.add(okButton);			

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {										
					dialog.setVisible(false);
					dialog.dispose();
				}
			});			
			bInnerPanel.add(cancelButton);
			
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			
			c.weightx = 0;
			c.weighty = 1;
			//c.insets = new Insets(10, 10, 10, 10);
			c.gridwidth = 1;
			c.gridwidth = 1;
			
			bPanel.add(bInnerPanel, c);

			return bPanel;

		}
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation(((int)(screenDim.getWidth() /2) - (dialog.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (dialog.getHeight()/2)));
	}	
	
	private void addParameter(String parameterName, Class<?> type, String defaultValue){
		if(!this.alreadyAddedParameterNames.contains(parameterName)){			
			this.alreadyAddedParameterNames.add(parameterName);
			if(this.selectedParametersPanel.getLayout() instanceof GridLayout) ((GridLayout)this.selectedParametersPanel.getLayout()).setRows(this.alreadyAddedParameterNames.size());
			this.selectedParametersPanel.add(new PropertyPanel(parameterName, type, defaultValue));
			dialog.validate();			
		}
	}

}
