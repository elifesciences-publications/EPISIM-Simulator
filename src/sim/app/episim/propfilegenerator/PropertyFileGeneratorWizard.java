package sim.app.episim.propfilegenerator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import sim.app.episim.EpisimProperties;
import sim.app.episim.ModeServer;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.controller.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.propfilegenerator.GlobalPropertiesObjectInspector.GlobalParameterSelectionListener;


public class PropertyFileGeneratorWizard {
	
	private JDialog dialog;
	private JButton okButton;
	private JComboBox paramTypeCombo;
	private JTextField pathText;
	private JPanel propertiesListPanel;
	private JPanel selectedParametersPanel;
	private JPanel selectedParametersDeletePanel;
	private JPanel selectedParametersNamesPanel;
	private JPanel pathPanel;
	private HashSet<String> markerPrefixes;
	private HashSet<Class<?>> validDataTypes;	
	private HashSet<String> alreadyAddedParameterNames;
	private JScrollPane selectedParametersPanelScroll;
	
	
	private PropertyFileType actSelectedParameterType = null;
	private ImageIcon deleteIcon = null;
	
	private ExtendedFileChooser propertyInputFileChooser;
	
	private JWindow progressWindow;
	private JProgressBar progressBar;
	
	
	private File propertyFileGenerationPath = null;
	
	
	private enum PropertyFileType{
		CELL_BEHAVIORAL_MODEL_PROPERTIES("Global Cell Behavioral Model Properties"),
		BIOMECHANICAL_MODEL_PROPERTIES("Global Biomechanical Model Properties"),
		MISC_PROPERTIES("Miscellaneous Properties");		
		private String name;
		private PropertyFileType(String _name){ this.name = _name;}
		public String toString(){ return name;}
	}
	
	
	public PropertyFileGeneratorWizard(Frame owner, String title, boolean modal){
		
		
		
		deleteIcon = new ImageIcon(PropertyFileGeneratorWizard.class.getResource("delete_icon.png").getPath());
		
		if(ModeServer.guiMode()){ 
			propertyInputFileChooser = new ExtendedFileChooser(".properties");
			
			progressWindow = new JWindow(owner);
			
			progressWindow.getContentPane().setLayout(new BorderLayout(5, 5));
			if(progressWindow.getContentPane() instanceof JPanel)
				((JPanel)progressWindow.getContentPane()).setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(10,10, 10, 10)));
			progressBar = new JProgressBar();
			progressBar.setStringPainted(true);
			progressBar.setIndeterminate(true);
			JLabel progressLabel = new JLabel("Generating Episim-Simulation Input Files");
			progressWindow.getContentPane().add(progressLabel, BorderLayout.NORTH);
			progressWindow.getContentPane().add(progressBar, BorderLayout.CENTER);
			
			progressWindow.setSize(400, 65);
			
			progressWindow.setLocation(owner.getLocation().x + (owner.getWidth()/2) - (progressWindow.getWidth()/2), 
					owner.getLocation().y + (owner.getHeight()/2) - (progressWindow.getHeight()/2));
		}
		
		
		
		
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
	   selectedParametersDeletePanel = new JPanel(new GridLayout(1,1,5,5));
	   selectedParametersNamesPanel = new JPanel(new GridLayout(1,1,5,5));
	   JPanel gridLayoutWrapperPanel = new JPanel(new BorderLayout());
	   gridLayoutWrapperPanel.add(selectedParametersNamesPanel, BorderLayout.WEST);
	   gridLayoutWrapperPanel.add(selectedParametersPanel, BorderLayout.CENTER);
	   gridLayoutWrapperPanel.add(selectedParametersDeletePanel, BorderLayout.EAST);
	   JPanel wrapperPanelBorder = new JPanel(new BorderLayout());
	   wrapperPanelBorder.add(gridLayoutWrapperPanel, BorderLayout.NORTH);
	  
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
	   pathPanel = buildPathPanel();
	   pathPanel.setVisible(false);
	   dialog.getContentPane().add(pathPanel, c);
	   
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
				selectedParametersDeletePanel.removeAll();
				selectedParametersNamesPanel.removeAll();
				
				
				alreadyAddedParameterNames = new HashSet<String>();
				propertyFileGenerationPath = null;
				pathPanel.setVisible(false);
				okButton.setEnabled(false);
				
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
						paramObject = MiscalleneousGlobalParameters.instance();
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
	
	
	
	private JPanel buildPathPanel(){
		JPanel pPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		
		pPanel.add(new JLabel("Property-File-Path:"), c);
		
		pathText = new JTextField("");
		pathText.setEnabled(true);
		pathText.setEditable(false);
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		
		pPanel.add(pathText, c);
		

		final JButton editPathButton = new JButton("Select Path");
		
		editPathButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(pathText.getText() != null && !pathText.getText().equals("")) propertyFileGenerationPath = showPathDialog(pathText.getText());
				else propertyFileGenerationPath =showPathDialog("");
				if(propertyFileGenerationPath != null){ 
					pathText.setText(propertyFileGenerationPath.getAbsolutePath());
					editPathButton.setText("Edit Path");
					okButton.setEnabled(true);
				}
				else{
					pathText.setText("");
					editPathButton.setText("Select Path");
				}
            
         }});
		
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		
		pPanel.add(editPathButton, c);
		
		
		return pPanel;
	}
	
	
	
	private JPanel buildButtonPanel() {

			JPanel bPanel = new JPanel(new GridBagLayout());
			JPanel bInnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
			GridBagConstraints c = new GridBagConstraints();			

			okButton = new JButton("  OK  ");
			okButton.setEnabled(false);
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
						okButtonPressed();
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
			
			if(!pathPanel.isVisible()) pathPanel.setVisible(true);
			if(propertyFileGenerationPath != null) okButton.setEnabled(true);
			if(this.selectedParametersPanel.getLayout() instanceof GridLayout) ((GridLayout)this.selectedParametersPanel.getLayout()).setRows(this.alreadyAddedParameterNames.size());
			if(this.selectedParametersDeletePanel.getLayout() instanceof GridLayout) ((GridLayout)this.selectedParametersDeletePanel.getLayout()).setRows(this.alreadyAddedParameterNames.size());
			if(this.selectedParametersNamesPanel.getLayout() instanceof GridLayout) ((GridLayout)this.selectedParametersNamesPanel.getLayout()).setRows(this.alreadyAddedParameterNames.size());
			
			final PropertyPanel panel = new PropertyPanel(parameterName, type, defaultValue);
			
			this.selectedParametersPanel.add(panel);
			
			final JLabel propertyNameLabel = new JLabel(parameterName +" - ");
			Font f = propertyNameLabel.getFont();
			f = new Font(f.getFontName(), Font.BOLD, f.getSize());
			propertyNameLabel.setFont(f);
			this.selectedParametersNamesPanel.add(propertyNameLabel);
			
			final JButton deleteButton = new JButton();
			deleteButton.setPreferredSize(new Dimension(22,22));
			deleteButton.setBorder(null);
			deleteButton.setContentAreaFilled(false);
			deleteButton.setBorderPainted(false);			
			deleteButton.setIcon(deleteIcon);
			deleteButton.addMouseListener(new MouseAdapter() {				
				public void mouseExited(MouseEvent e) { deleteButton.setContentAreaFilled(false); }				
				public void mouseEntered(MouseEvent e) { deleteButton.setContentAreaFilled(true); }				
			});
			deleteButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					selectedParametersPanel.remove(panel);
					selectedParametersDeletePanel.remove(deleteButton);
					selectedParametersNamesPanel.remove(propertyNameLabel);
					alreadyAddedParameterNames.remove(panel.getPropertyName());
					if(selectedParametersPanel.getLayout() instanceof GridLayout) ((GridLayout)selectedParametersPanel.getLayout()).setRows(alreadyAddedParameterNames.size());
					if(selectedParametersDeletePanel.getLayout() instanceof GridLayout) ((GridLayout)selectedParametersDeletePanel.getLayout()).setRows(alreadyAddedParameterNames.size());
					if(selectedParametersNamesPanel.getLayout() instanceof GridLayout) ((GridLayout)selectedParametersNamesPanel.getLayout()).setRows(alreadyAddedParameterNames.size());
					if(alreadyAddedParameterNames.isEmpty()){ 
						pathPanel.setVisible(false);
						okButton.setEnabled(false);
					}
					dialog.validate();
					
				}});
			selectedParametersDeletePanel.add(deleteButton);
			
			
			dialog.validate();			
		}
	}
	 private File showPathDialog(String path){
	   	
	   	if(path!= null && !path.equals("")) propertyInputFileChooser.setCurrentDirectory(new File(path));
	   	
	   	propertyInputFileChooser.setDialogTitle("Select Property-Files Path");
			if(propertyInputFileChooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) return propertyInputFileChooser.getSelectedFile();
			return propertyFileGenerationPath;
		}
	
	 private List<PropertyDescriptor> getPropertyDescriptorList(){
		 List<PropertyDescriptor> propDescriptors = new ArrayList<PropertyDescriptor>();
		 
		 for(Component comp: this.selectedParametersPanel.getComponents()){
			 if(comp instanceof PropertyPanel){
				 propDescriptors.add(((PropertyPanel)comp).getPropertyDescriptor());
			 }
		 }
		 
		 return propDescriptors;
	 }
	 
	 private void okButtonPressed(){
		 dialog.setVisible(false);
		 dialog.dispose();
		 final PropertyFileGenerator propGen = new PropertyFileGenerator('_', ".properties");
		 Runnable r = new Runnable(){
				
				public void run() {
					progressWindow.setVisible(true);
					
					propGen.generatePropertyFiles(propertyFileGenerationPath, getPropertyDescriptorList());
					
					progressWindow.setVisible(false);
          }
		
			};
			
		Thread writingThread = new Thread(r);
		writingThread.start();
	 }

}
