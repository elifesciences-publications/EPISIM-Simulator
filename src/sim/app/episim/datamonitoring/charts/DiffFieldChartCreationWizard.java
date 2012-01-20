package sim.app.episim.datamonitoring.charts;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.ExtendedLabelledList;
import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.monitoring.EpisimDiffFieldChart;


public class DiffFieldChartCreationWizard extends JDialog{
	
	private EpisimDiffFieldChart episimDiffFieldChart;
	private boolean okButtonPressed = false;
	
	private NumberTextField pngFrequencyInSimulationSteps;
   private NumberTextField chartFrequencyInSimulationSteps;
   private JLabel pngFrequencyLabel;
   private JLabel chartFrequencyLabel;
   
   private JPanel mainPanel;
   
   private JCheckBox pngCheck;
   
   private JButton changePngPathButton;
   
   private JTextField chartTitleField;
   private JTextField pngPathField;
   
   private JComboBox diffFieldsCombo;
   private DefaultComboBoxModel diffFieldsCombBoxModel;
  
	
   private final int WIDTH = 600;
   private final int HEIGHT = 275;
   
   private boolean isDirty = false;
   
   
   
   public DiffFieldChartCreationWizard(Frame owner, String title, boolean modal){
   	super(owner, title, modal);
   	this.episimDiffFieldChart = new EpisimDiffFieldChartImpl(ChartController.getInstance().getNextChartId());
   	
   	setPreferredSize(new Dimension(WIDTH, HEIGHT));
   	
   	mainPanel = new JPanel(new GridBagLayout());
   	GridBagConstraints c = new GridBagConstraints();
		   
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(buildChartOptionPanel(), c);
   	
   	
   	
   	JPanel layoutCorrectingPanel = new JPanel(new BorderLayout());
		layoutCorrectingPanel.add(mainPanel,BorderLayout.NORTH);
		
		getContentPane().add(new JScrollPane(layoutCorrectingPanel), BorderLayout.CENTER);
   	getContentPane().add(buildOKCancelButtonPanel(), BorderLayout.SOUTH);
      setSize(WIDTH, HEIGHT);
      this.setTitle("Untitled Chart");
 		validate();
   }
   
	private JPanel buildChartOptionPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		Box globalAttributes = Box.createVerticalBox();
		chartTitleField = new JTextField();
		chartTitleField.setText("");
		episimDiffFieldChart.setChartTitle(chartTitleField.getText());
		chartTitleField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setTitle(chartTitleField.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartTitleField.setText(getTitle());
			}
		});
		chartTitleField.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				setTitle(chartTitleField.getText());
			}
		});

		ExtendedLabelledList list = new ExtendedLabelledList("Extracellular Diffusion Field Chart");
		list.setInsets(new Insets(2,2,2,2));
		list.add(new JLabel("Title"), chartTitleField);

		diffFieldsCombo = new JComboBox();
		diffFieldsCombBoxModel = (DefaultComboBoxModel) diffFieldsCombo.getModel();
		EpisimDiffusionFieldConfiguration[] diffConfigs = ModelController.getInstance().getExtraCellularDiffusionController().getEpisimExtraCellularDiffusionFieldsConfigurations();
		
		for(int i = 0; i < diffConfigs.length; i++){ 
			diffFieldsCombBoxModel.addElement(diffConfigs[i].getDiffusionFieldName());
		}
		diffFieldsCombo.setSelectedIndex(-1);
		diffFieldsCombo.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent evt) {
			    episimDiffFieldChart.setDiffusionFieldName((String)diffFieldsCombo.getSelectedItem());
			    isDirty = true;
			}
		});		
		list.add(new JLabel("Diffusion Field"), diffFieldsCombo);
		
		
      
      chartFrequencyLabel = new JLabel("Chart Updating Frequency in Simulation Steps: ");
		
		chartFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				isDirty = true; 
				newValue = Math.round(newValue);
				if(newValue <= 0) newValue=1;
				episimDiffFieldChart.setChartUpdatingFrequency((int) newValue);
	        return newValue;
	      }
		};
		chartFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					chartFrequencyInSimulationSteps.newValue(chartFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartFrequencyInSimulationSteps.newValue(chartFrequencyInSimulationSteps.getValue());
			}
		});
		chartFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				chartFrequencyInSimulationSteps.newValue(chartFrequencyInSimulationSteps.getValue());
			}
		});
		
		
		list.add(chartFrequencyLabel, chartFrequencyInSimulationSteps);
		
      pngCheck = new JCheckBox();
      pngCheck.setSelected(false);
      episimDiffFieldChart.setPNGPrintingEnabled(false);
		pngCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				if(DiffFieldChartCreationWizard.this.isVisible()){
					if(e.getStateChange() == ItemEvent.SELECTED){
						
						if(!episimDiffFieldChart.isPNGPrintingEnabled())selectPNGPath(false);
						
		          }
					else{
						episimDiffFieldChart.setPNGPrintingEnabled(false);
						pngFrequencyInSimulationSteps.setEnabled(false);
						pngFrequencyLabel.setEnabled(false);
						pngPathField.setEnabled(false);
						changePngPathButton.setEnabled(false);
					}
				}
			}
		});
		JPanel labelCheckPanel = new JPanel(new BorderLayout(5,5));
		labelCheckPanel.add(new JLabel("Save as PNG"), BorderLayout.WEST);
		labelCheckPanel.add(pngCheck, BorderLayout.EAST);
		JPanel fieldButtonPanel = new JPanel(new BorderLayout(5,5));
		this.pngPathField = new JTextField();
		this.pngPathField.setEditable(false);
		pngPathField.setEnabled(false);
		this.changePngPathButton = new JButton("Change");
		this.changePngPathButton.setEnabled(false);
		this.changePngPathButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				isDirty = true;
				selectPNGPath(true);	         
         }});
		fieldButtonPanel.add(this.pngPathField, BorderLayout.CENTER);
		fieldButtonPanel.add(this.changePngPathButton, BorderLayout.EAST);
		list.add(labelCheckPanel, fieldButtonPanel);
		
		pngFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				isDirty = true;
				newValue = Math.round(newValue);
				if(newValue <= 0) newValue=1;
				episimDiffFieldChart.setPNGPrintingFrequency((int) newValue);
	        return newValue;
	      }
		};
		pngFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					
					pngFrequencyInSimulationSteps.newValue(pngFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					pngFrequencyInSimulationSteps.newValue(pngFrequencyInSimulationSteps.getValue());
			}
		});
		pngFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				pngFrequencyInSimulationSteps.newValue(pngFrequencyInSimulationSteps.getValue());
			}
		});
		pngFrequencyInSimulationSteps.setEnabled(false);
		pngFrequencyLabel = new JLabel("PDF Printing Frequency in Simulation Steps: ");
		pngFrequencyLabel.setEnabled(false);
		
		
		
		list.add(pngFrequencyLabel, pngFrequencyInSimulationSteps);
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
	}
	
	private void restoreChartValues(EpisimDiffFieldChart chart){
		if(chart != null){
			
			this.episimDiffFieldChart = chart.clone();
			
			this.chartTitleField.setText(episimDiffFieldChart.getChartTitle());
			this.setTitle(episimDiffFieldChart.getChartTitle());
			
			this.diffFieldsCombo.setSelectedItem(episimDiffFieldChart.getDiffusionFieldName());			
						
			this.pngCheck.setSelected(episimDiffFieldChart.isPNGPrintingEnabled());
			if(episimDiffFieldChart.isPNGPrintingEnabled()){
				pngFrequencyInSimulationSteps.setEnabled(true);
				this.changePngPathButton.setEnabled(true);
				this.pngPathField.setText(episimDiffFieldChart.getPNGPrintingPath().getAbsolutePath());
				pngPathField.setEnabled(true);
			}
			else{
				pngFrequencyInSimulationSteps.setEnabled(false);
				this.changePngPathButton.setEnabled(false);
				pngPathField.setEnabled(false);
			}
			
			this.pngFrequencyInSimulationSteps.setValue(episimDiffFieldChart.getPNGPrintingFrequency());
			this.chartFrequencyInSimulationSteps.setValue(episimDiffFieldChart.getChartUpdatingFrequency());

			this.isDirty = false;
		}
		
	}		
	
	public void showWizard(EpisimDiffFieldChart chart){
		isDirty = false;
		if(chart != null) restoreChartValues(chart);
		repaint();
		centerMe();
		setVisible(true);
		 
	}
	
	public EpisimDiffFieldChart getEpisimDiffFieldChart(){
		if(this.okButtonPressed){
			return this.episimDiffFieldChart;
		}
		return null;
	}
	
	
	public void setTitle(String title)
   {
		episimDiffFieldChart.setChartTitle(title);	
		super.setTitle("Chart Creation Wizard: "+ title);
		chartTitleField.setText(title);
   }
	public String getTitle()
	{
	   return episimDiffFieldChart.getChartTitle();
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
				DiffFieldChartCreationWizard.this.okButtonPressed = false;
				DiffFieldChartCreationWizard.this.setVisible(false);
				DiffFieldChartCreationWizard.this.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}
	
	private void okButtonPressed(){
		
		boolean errorFound = false;
				
		if(episimDiffFieldChart.getChartTitle() == null || episimDiffFieldChart.getChartTitle().trim().equals("")){
			errorFound = true;
			JOptionPane.showMessageDialog(DiffFieldChartCreationWizard.this, "Please enter valid Title!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		if(!errorFound&&(episimDiffFieldChart.getDiffusionFieldName() == null || episimDiffFieldChart.getDiffusionFieldName().trim().equals(""))){
			errorFound = true;
			JOptionPane.showMessageDialog(DiffFieldChartCreationWizard.this, "Please choose a Diffusion Field!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		
		if(!errorFound){
			episimDiffFieldChart.setIsDirty(isDirty);
			DiffFieldChartCreationWizard.this.okButtonPressed = true;
			DiffFieldChartCreationWizard.this.setVisible(false);
			DiffFieldChartCreationWizard.this.dispose();
		}
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	private void selectPNGPath(boolean buttonCall){		
		
		ExtendedFileChooser fileChooser = new ExtendedFileChooser(".png");
		fileChooser.setDialogTitle("Choose PNG Printing Path");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
      fileChooser.setCurrentDirectory(episimDiffFieldChart.getPNGPrintingPath());
      fileChooser.setSelectedFile(episimDiffFieldChart.getPNGPrintingPath());
      
      File selectedPath = null;
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(DiffFieldChartCreationWizard.this) && 
      		(selectedPath = fileChooser.getSelectedFile()) != null)
      {
      	episimDiffFieldChart.setPNGPrintingEnabled(true);
      	episimDiffFieldChart.setPNGPrintingPath(selectedPath);
      	pngFrequencyInSimulationSteps.setEnabled(true);
      	pngFrequencyLabel.setEnabled(true);
      	this.pngPathField.setText(selectedPath.getAbsolutePath());
      	this.changePngPathButton.setEnabled(true);
      	pngPathField.setEnabled(true);
      	pngCheck.setSelected(true);
      	//  	Dimension dim = previewChartPanel.getPreferredSize();
      	//   printChartToPDF( previewChart, dim.width, dim.height, fd.getDirectory() + fileName );
      }
      else{
      	if(!buttonCall){
      	pngCheck.setSelected(false);
      	episimDiffFieldChart.setPNGPrintingEnabled(false);
      	pngFrequencyInSimulationSteps.setEnabled(false);
      	this.changePngPathButton.setEnabled(false);
      	pngPathField.setEnabled(false);
      	}
      }
	}

}
