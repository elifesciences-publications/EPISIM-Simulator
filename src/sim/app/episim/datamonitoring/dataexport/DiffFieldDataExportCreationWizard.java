package sim.app.episim.datamonitoring.dataexport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.ExtendedLabelledList;
import sim.util.gui.NumberTextField;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.monitoring.EpisimDiffFieldDataExport;


public class DiffFieldDataExportCreationWizard extends JDialog{
	
	private EpisimDiffFieldDataExport episimDiffFieldDataExport;
	private boolean okButtonPressed = false;
	
	
   private NumberTextField dataExportFrequencyInSimulationSteps;
  
   private JLabel dataExportFrequencyLabel;
   
   private JPanel mainPanel;
   
   private JTextField csvPathField;
   
   private JTextField dataExportNameField;
   private JTextArea dataExportDescriptionField;
   
   
   private JComboBox diffFieldsCombo;
   private DefaultComboBoxModel diffFieldsCombBoxModel;
  
	
   private final int WIDTH = 700;
   private final int HEIGHT = 325;
   
   private boolean isDirty = false;
   
   private ExtendedFileChooser csvChooser = new ExtendedFileChooser("csv");
   
   public DiffFieldDataExportCreationWizard(Frame owner, String title, boolean modal){
   	super(owner, title, modal);
   	this.episimDiffFieldDataExport = new EpisimDiffFieldDataExportImpl(DataExportController.getInstance().getNextDataExportId());
   	setPreferredSize(new Dimension(WIDTH, HEIGHT));
   	
   	mainPanel = new JPanel(new GridBagLayout());
   	GridBagConstraints c = new GridBagConstraints();
		   
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(buildDataExportOptionPanel(), c);
   	
   	
   	
   	JPanel layoutCorrectingPanel = new JPanel(new BorderLayout());
		layoutCorrectingPanel.add(mainPanel,BorderLayout.NORTH);
		layoutCorrectingPanel.setPreferredSize(new Dimension((int)(getPreferredSize().width*0.85),	(int)(getPreferredSize().height*0.7)));
		getContentPane().add(new JScrollPane(layoutCorrectingPanel), BorderLayout.CENTER);
   	getContentPane().add(buildOKCancelButtonPanel(), BorderLayout.SOUTH);
      setSize(WIDTH, HEIGHT);
      this.setTitle("Unnamed Data Export");
 		validate();
   }
   
	private JPanel buildDataExportOptionPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		Box globalAttributes = Box.createVerticalBox();
		dataExportNameField = new JTextField();
		dataExportNameField.setText("");
		episimDiffFieldDataExport.setName(dataExportNameField.getText());
		dataExportNameField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setTitle(dataExportNameField.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					dataExportNameField.setText(getTitle());
			}
		});
		dataExportNameField.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				setTitle(dataExportNameField.getText());
			}
		});

		ExtendedLabelledList list = new ExtendedLabelledList("Extracellular Diffusion Field Data Export");
		list.setInsets(new Insets(2,2,2,2));
		list.add(new JLabel("Name: "), dataExportNameField);
		
		dataExportDescriptionField = new JTextArea();		
		JScrollPane descriptionScroll = new JScrollPane(dataExportDescriptionField);
		descriptionScroll.setPreferredSize(new Dimension(getPreferredSize().width, 50));
		descriptionScroll.setMaximumSize(new Dimension(getMaximumSize().width, 50));
		descriptionScroll.setMinimumSize(new Dimension(getMinimumSize().width, 50));
		descriptionScroll.setSize(new Dimension(getSize().width, 50));
		descriptionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		descriptionScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		dataExportDescriptionField.setFont(dataExportNameField.getFont());
		dataExportDescriptionField.setText("");
		dataExportDescriptionField.setLineWrap(true);
		dataExportDescriptionField.setWrapStyleWord(true);
		this.episimDiffFieldDataExport.setDescription("");
		dataExportDescriptionField.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;				
			}
		});
		dataExportDescriptionField.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				
				episimDiffFieldDataExport.setDescription(dataExportDescriptionField.getText());
			}
		});	
		
		list.add(new JLabel("Description: "), descriptionScroll);
		

		diffFieldsCombo = new JComboBox();
		diffFieldsCombBoxModel = (DefaultComboBoxModel) diffFieldsCombo.getModel();
		EpisimDiffusionFieldConfiguration[] diffConfigs = ModelController.getInstance().getExtraCellularDiffusionController().getEpisimExtraCellularDiffusionFieldsConfigurations();
		
		for(int i = 0; i < diffConfigs.length; i++){ 
			diffFieldsCombBoxModel.addElement(diffConfigs[i].getDiffusionFieldName());
		}
		diffFieldsCombo.setSelectedIndex(-1);
		diffFieldsCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent evt) {
			    episimDiffFieldDataExport.setDiffusionFieldName((String)diffFieldsCombo.getSelectedItem());
			    isDirty = true;
			}
		});		
		list.add(new JLabel("Diffusion Field"), diffFieldsCombo);
		
		
      
      dataExportFrequencyLabel = new JLabel("Data Export Frequency in Simulation Steps: ");
		
		dataExportFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				isDirty = true; 
				newValue = Math.round(newValue);
				if(newValue <= 0) newValue=1;
				episimDiffFieldDataExport.setDataExportFrequencyInSimulationSteps((int) newValue);
	        return newValue;
	      }
		};
		dataExportFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					dataExportFrequencyInSimulationSteps.newValue(dataExportFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					dataExportFrequencyInSimulationSteps.newValue(dataExportFrequencyInSimulationSteps.getValue());
			}
		});
		dataExportFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				dataExportFrequencyInSimulationSteps.newValue(dataExportFrequencyInSimulationSteps.getValue());
			}
		});
		
		
		list.add(dataExportFrequencyLabel, dataExportFrequencyInSimulationSteps);
		
		JPanel fieldButtonPanel = new JPanel(new BorderLayout(5, 0));
		csvPathField = new JTextField("");
		csvPathField.setEditable(false);
		JButton editCSVPathButton = new JButton("Edit Path");
		editCSVPathButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				isDirty = true;
				if(csvPathField.getText() != null && !csvPathField.getText().trim().equals("")){
					episimDiffFieldDataExport.setCSVFilePath(showCSVPathDialog(csvPathField.getText()));
				}
				else episimDiffFieldDataExport.setCSVFilePath(showCSVPathDialog(""));
				if(episimDiffFieldDataExport.getCSVFilePath() != null) csvPathField.setText(episimDiffFieldDataExport.getCSVFilePath().getAbsolutePath());
            
         }});
		fieldButtonPanel.add(csvPathField, BorderLayout.CENTER);
		fieldButtonPanel.add(editCSVPathButton, BorderLayout.EAST);
		
		list.add(new JLabel("CSV-File-Path: "), fieldButtonPanel);
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
	}
	
	private void restoreDataExportValues(EpisimDiffFieldDataExport dataExport){
		if(dataExport != null){
			
			this.episimDiffFieldDataExport = dataExport.clone();
			
			this.dataExportNameField.setText(episimDiffFieldDataExport.getName());
			this.dataExportDescriptionField.setText(episimDiffFieldDataExport.getDescription());
			this.setTitle(episimDiffFieldDataExport.getName());
			
			this.diffFieldsCombo.setSelectedItem(episimDiffFieldDataExport.getDiffusionFieldName());			
						
			this.csvPathField.setText(dataExport.getCSVFilePath().getAbsolutePath());		
			this.dataExportFrequencyInSimulationSteps.setValue(episimDiffFieldDataExport.getDataExportFrequncyInSimulationSteps());

			this.isDirty = false;
		}
		
	}		
	
	public void showWizard(EpisimDiffFieldDataExport dataExport){
		isDirty = false;
		if(dataExport != null) restoreDataExportValues(dataExport);
		repaint();
		centerMe();
		setVisible(true);
		 
	}
	
	public EpisimDiffFieldDataExport getEpisimDiffFieldDataExport(){
		if(this.okButtonPressed){
			return this.episimDiffFieldDataExport;
		}
		return null;
	}
	
	
	public void setTitle(String title)
   {
		episimDiffFieldDataExport.setName(title);	
		super.setTitle("Data Export Creation Wizard: "+ title);
		dataExportNameField.setText(title);
   }
	public String getTitle()
	{
	   return episimDiffFieldDataExport.getName();
	}
	
	private File showCSVPathDialog(String path){
   	
   	if(path!= null && !path.equals("")) csvChooser.setCurrentDirectory(new File(path));
   	
   	csvChooser.setDialogTitle("Choose CSV-File-Path");
		if(csvChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) return csvChooser.getSelectedFile();
		return null;
	}
	
	public void showWizard(){
		
		isDirty = false;
		showWizard(null);
	}
	
	private JPanel buildOKCancelButtonPanel() {

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
				okButtonPressed();
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
				DiffFieldDataExportCreationWizard.this.okButtonPressed = false;
				DiffFieldDataExportCreationWizard.this.setVisible(false);
				DiffFieldDataExportCreationWizard.this.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}
	
	private void okButtonPressed(){
		
		boolean errorFound = false;
				
		if(episimDiffFieldDataExport.getName() == null || episimDiffFieldDataExport.getName().trim().equals("")){
			errorFound = true;
			JOptionPane.showMessageDialog(DiffFieldDataExportCreationWizard.this, "Please enter valid Title!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		if(!errorFound&&(episimDiffFieldDataExport.getDiffusionFieldName() == null || episimDiffFieldDataExport.getDiffusionFieldName().trim().equals(""))){
			errorFound = true;
			JOptionPane.showMessageDialog(DiffFieldDataExportCreationWizard.this, "Please choose a Diffusion Field!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		
		if(!errorFound&& episimDiffFieldDataExport.getCSVFilePath() == null){
			errorFound = true;
			JOptionPane.showMessageDialog(DiffFieldDataExportCreationWizard.this, "Please choose a path for the csv-file!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		
		if(!errorFound){
			episimDiffFieldDataExport.setIsDirty(isDirty);
			DiffFieldDataExportCreationWizard.this.okButtonPressed = true;
			DiffFieldDataExportCreationWizard.this.setVisible(false);
			DiffFieldDataExportCreationWizard.this.dispose();
		}
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	

}
