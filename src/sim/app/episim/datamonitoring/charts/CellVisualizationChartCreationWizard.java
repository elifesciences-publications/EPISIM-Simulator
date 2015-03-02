package sim.app.episim.datamonitoring.charts;

import java.awt.BasicStroke;
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







import javax.swing.BorderFactory;
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

import sim.app.episim.datamonitoring.CellColoringConfigurationPanel;
import sim.app.episim.datamonitoring.ExpressionEditorPanel.ExpressionState;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissueimport.TissueController;
import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.util.ProjectionPlane;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.util.gui.LabelledListHack;
import sim.util.gui.NumberTextField;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.calc.CellColoringConfigurator;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;




public class CellVisualizationChartCreationWizard extends JDialog {
	
	
   private TissueCellDataFieldsInspector cellDataFieldsInspector;
   
   private EpisimCellVisualizationChart episimChart;
   private boolean okButtonPressed = false;
   
   private JTextField chartTitleField;
   private JTextField chartXLabel;
   private JTextField chartYLabel;
  
   private JTextField pngPathField;
   private JButton changePngPathButton;
   private JCheckBox pngCheck;
   
   private NumberTextField pngFrequencyInSimulationSteps;
   private NumberTextField chartFrequencyInSimulationSteps;
   private JLabel pngFrequencyLabel;
   private JLabel chartFrequencyLabel;
   
   private NumberTextField minXField;
   private NumberTextField minYField;
   private NumberTextField minZField;
   private NumberTextField maxXField;
   private NumberTextField maxYField;
   private NumberTextField maxZField;

   private 	JCheckBox defaultColoringCheckBox;
   
   private JPanel propertiesPanel;
    
   private final int WIDTH = 600;
   private final int HEIGHT = 510;
   
   
   private CellColoringConfigurationPanel cellColoringPanel = null;
   private JDialog cellColoringConfigurationDialog = null;
   
   private boolean isDirty = false;
    
   private JComboBox<ProjectionPlane> cellProjectionPlaneCombo;

   /** Generates a new ChartGenerator with a blank chart. */
   public CellVisualizationChartCreationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector cellDataFieldsInspector){
		super(owner, title, modal);
		
		this.cellDataFieldsInspector= cellDataFieldsInspector;
		if(cellDataFieldsInspector == null) throw new IllegalArgumentException("TissueCellDataFieldsInspector was null !");
		
		this.episimChart = new EpisimCellVisualizationChartImpl(ChartController.getInstance().getNextChartId());
		
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		buildCellColoringConfigurationDialog();
				
		propertiesPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		  		   
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,5,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		propertiesPanel.add(buildChartOptionPanel(), c);
         
		
		  
		
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(5,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;		
			
		propertiesPanel.add(buildCrossSectionDefinitionPanel(), c);
		
		JPanel layoutCorrectingPanel = new JPanel(new BorderLayout());
		layoutCorrectingPanel.add(propertiesPanel,BorderLayout.NORTH);      
      getContentPane().add(new JScrollPane(layoutCorrectingPanel), BorderLayout.CENTER);
      getContentPane().add(buildOKCancelButtonPanel(), BorderLayout.SOUTH);
      setSize(WIDTH, HEIGHT);
 		validate();
   }
                
   public void setTitle(String title)
   {
   	 episimChart.setTitle(title);	
       super.setTitle("Chart Creation Wizard: "+ title);
       chartTitleField.setText(title);
   }

   public String getTitle()
   {
      return episimChart.getTitle();
   }
               
   public void setRangeAxisLabel(String val)
   {
   	 episimChart.setYLabel(val);
       chartYLabel.setText(val);
   }
               
   public String getRangeAxisLabel()
   {
      return  episimChart.getYLabel();
   }
               
   public void setDomainAxisLabel(String val)
   {
   	episimChart.setXLabel(val);      
      chartXLabel.setText(val);
   }
               
   public String getDomainAxisLabel()
   {
       return episimChart.getXLabel();
   
   }
  
	public void showWizard(){		
		isDirty = false;
		showWizard(null);
	}
	
	
	private void restoreChartValues(EpisimCellVisualizationChart chart){
		if(chart != null){
			
			this.episimChart = chart.clone();
			
			this.chartTitleField.setText(episimChart.getTitle());
			this.setTitle(episimChart.getTitle());
			
			this.chartXLabel.setText(episimChart.getXLabel());
			this.setDomainAxisLabel(episimChart.getXLabel());
			
			this.chartYLabel.setText(episimChart.getYLabel());
			this.setRangeAxisLabel(episimChart.getYLabel());
			
			
			this.maxXField.setValue(episimChart.getMaxXMikron());
			this.maxYField.setValue(episimChart.getMaxYMikron());
			
			
			this.minXField.setValue(episimChart.getMinXMikron());
			this.minYField.setValue(episimChart.getMinYMikron());
			
			
			if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
				this.maxZField.setValue(episimChart.getMaxZMikron());
				this.minZField.setValue(episimChart.getMinZMikron());
			}
			
			this.cellProjectionPlaneCombo.setSelectedItem(episimChart.getCellProjectionPlane());
						
			this.defaultColoringCheckBox.setSelected(chart.getDefaultColoring());
			this.pngCheck.setSelected(episimChart.isPNGPrintingEnabled());
			this.pngFrequencyLabel.setEnabled(episimChart.isPNGPrintingEnabled());
			this.pngFrequencyInSimulationSteps.setEnabled(episimChart.isPNGPrintingEnabled());
			this.changePngPathButton.setEnabled(episimChart.isPNGPrintingEnabled());
			this.pngPathField.setEnabled(episimChart.isPNGPrintingEnabled());
			if(this.cellColoringPanel != null) this.cellColoringPanel.setEnabled(!chart.getDefaultColoring());
			if(episimChart.isPNGPrintingEnabled()){			
				
				this.pngPathField.setText(episimChart.getPNGPrintingPath().getAbsolutePath());
				
			}	
			this.pngFrequencyInSimulationSteps.setValue(episimChart.getPNGPrintingFrequency());
			this.chartFrequencyInSimulationSteps.setValue(episimChart.getChartUpdatingFrequency());
			this.isDirty = false;
		}		
	}	
		
	public void showWizard(EpisimCellVisualizationChart chart){
		isDirty = false;
		if(chart != null) restoreChartValues(chart);
		repaint();
		centerMe(this);
		setVisible(true);
	}
	
	public EpisimCellVisualizationChart getEpisimCellVisualizationChart(){
		if(this.okButtonPressed){
			return this.episimChart;
		}
		return null;
	}
	
	private JPanel buildCrossSectionDefinitionPanel(){
		JPanel crossSectionDefinitionPanel = new JPanel(new BorderLayout());
		
		LabelledListHack list = new LabelledListHack("Cross-Section Definition");
		list.setInsets(new Insets(2,0,2,5));
		cellProjectionPlaneCombo = new JComboBox<ProjectionPlane>(ProjectionPlane.values());
		if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
		cellProjectionPlaneCombo.setSelectedIndex(0);
			cellProjectionPlaneCombo.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent evt) {
					episimChart.setCellProjectionPlane((ProjectionPlane) cellProjectionPlaneCombo.getSelectedItem());
				   isDirty = true;
				}
			});
			
			list.add(new JLabel("Cell Projection Plane"),cellProjectionPlaneCombo);		
		}
		
		minXField = new NumberTextField(0,false){
			public double newValue(double newValue)
	      {
				isDirty = true;				
				if(newValue <= 0) newValue=0;
				if(newValue > episimChart.getMaxXMikron()){
					newValue=episimChart.getMaxXMikron();
					JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Min-value cannot be bigger than the according max-value", "Constraint", JOptionPane.INFORMATION_MESSAGE);
				}
				episimChart.setMinXMikron(newValue);
	        return newValue;
	      }
		};
		minXField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){					
					minXField.newValue(minXField.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					minXField.newValue(minXField.getValue());
			}
		});
		minXField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				minXField.newValue(minXField.getValue());
			}
		});
		list.add(new JLabel("Min-X in Mikron"), minXField);
		
		maxXField = new NumberTextField(0,false){
			public double newValue(double newValue)
	      {
				isDirty = true;				
				if(newValue <= 0) newValue=0;
				if(newValue < episimChart.getMinXMikron()){
					newValue=episimChart.getMinXMikron();
					JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Max-value cannot be smaller than the according min-value", "Constraint", JOptionPane.INFORMATION_MESSAGE);
				}
				if(newValue > TissueController.getInstance().getTissueBorder().getWidthInMikron()){
					newValue=TissueController.getInstance().getTissueBorder().getWidthInMikron();					
				}
				episimChart.setMaxXMikron(newValue);
	        return newValue;
	      }
		};
		maxXField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){					
					maxXField.newValue(maxXField.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					maxXField.newValue(maxXField.getValue());
			}
		});
		maxXField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				maxXField.newValue(maxXField.getValue());
			}
		});
		list.add(new JLabel("Max-X in Mikron"), maxXField);		
		
		
		
		
		minYField = new NumberTextField(0,false){
			public double newValue(double newValue)
	      {
				isDirty = true;				
				if(newValue <= 0) newValue=0;
				if(newValue > episimChart.getMaxYMikron()){
					newValue=episimChart.getMaxYMikron();
					JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Min-value cannot be bigger than the according max-value", "Constraint", JOptionPane.INFORMATION_MESSAGE);
				}
				episimChart.setMinYMikron(newValue);
	        return newValue;
	      }
		};
		minYField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){					
					minYField.newValue(minYField.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					minYField.newValue(minYField.getValue());
			}
		});
		minYField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				minYField.newValue(minYField.getValue());
			}
		});
		list.add(new JLabel("Min-Y in Mikron"), minYField);
		
		maxYField = new NumberTextField(0,false){
			public double newValue(double newValue)
	      {
				isDirty = true;				
				if(newValue <= 0) newValue=0;
				if(newValue < episimChart.getMinYMikron()){
					newValue=episimChart.getMinYMikron();
					JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Max-value cannot be smaller than the according min-value", "Constraint", JOptionPane.INFORMATION_MESSAGE);
				}
				if(newValue > TissueController.getInstance().getTissueBorder().getHeightInMikron()){
					newValue=TissueController.getInstance().getTissueBorder().getHeightInMikron();					
				}
				episimChart.setMaxYMikron(newValue);
	        return newValue;
	      }
		};
		maxYField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){					
					maxYField.newValue(maxYField.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					maxYField.newValue(maxYField.getValue());
			}
		});
		maxYField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				maxYField.newValue(maxYField.getValue());
			}
		});
		list.add(new JLabel("Max-Y in Mikron"), maxYField);		
		
		
		
		if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){		
			minZField = new NumberTextField(0,false){
				public double newValue(double newValue)
		      {
					isDirty = true;				
					if(newValue <= 0) newValue=0;
					if(newValue > episimChart.getMaxZMikron()){
						newValue=episimChart.getMaxZMikron();
						JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Min-value cannot be bigger than the according max-value", "Constraint", JOptionPane.INFORMATION_MESSAGE);
					}
					episimChart.setMinZMikron(newValue);
		        return newValue;
		      }
			};
			minZField.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent keyEvent) {
					isDirty = true;
					if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){					
						minZField.newValue(minZField.getValue());
					}
					else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
						minZField.newValue(minZField.getValue());
				}
			});
			minZField.addFocusListener(new FocusAdapter(){
				public void focusLost(FocusEvent e) {
					minZField.newValue(minZField.getValue());
				}
			});
			list.add(new JLabel("Min-Z in Mikron"), minZField);
			
			maxZField = new NumberTextField(0,false){
				public double newValue(double newValue)
		      {
					isDirty = true;				
					if(newValue <= 0) newValue=0;
					if(newValue < episimChart.getMinZMikron()){
						newValue=episimChart.getMinZMikron();
						JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Max-value cannot be smaller than the according min-value", "Constraint", JOptionPane.INFORMATION_MESSAGE);
					}
					if(newValue > TissueController.getInstance().getTissueBorder().getLengthInMikron()){
						newValue=TissueController.getInstance().getTissueBorder().getLengthInMikron();					
					}
					episimChart.setMaxZMikron(newValue);
		        return newValue;
		      }
			};
			maxZField.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent keyEvent) {
					isDirty = true;
					if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){					
						maxZField.newValue(maxZField.getValue());
					}
					else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
						maxZField.newValue(maxZField.getValue());
				}
			});
			maxZField.addFocusListener(new FocusAdapter(){
				public void focusLost(FocusEvent e) {
					maxZField.newValue(maxZField.getValue());
				}
			});
			list.add(new JLabel("Max-Z in Mikron"), maxZField);
		}
		
		crossSectionDefinitionPanel.add(list, BorderLayout.CENTER);
		
		return crossSectionDefinitionPanel;
	}
	
	private void buildCellColoringConfigurationDialog(){
		this.cellColoringConfigurationDialog = new JDialog(this,"Cell Coloring Configuration",true);
		
		cellColoringPanel  = new CellColoringConfigurationPanel(this,this.cellDataFieldsInspector);
		JPanel cellColoringConfigPanel = new JPanel(new BorderLayout());
		final JPanel cellColoringJPanel = cellColoringPanel.getCellColoringConfigurationPanel();
		 defaultColoringCheckBox = new JCheckBox("Use Default Coloring");
		defaultColoringCheckBox.setSelected(episimChart.getDefaultColoring());
		cellColoringPanel.setEnabled(!episimChart.getDefaultColoring());
		defaultColoringCheckBox.addActionListener(new ActionListener(){			
         public void actionPerformed(ActionEvent e) {
         		isDirty=true;
	         	if(defaultColoringCheckBox.isSelected()){
	         		episimChart.setCellColoringConfigurator(null);
	         		cellColoringPanel.setEnabled(false);
	         	}
	         	else{
	         		cellColoringPanel.setEnabled(true);
	         	}
	         	episimChart.setDefaultColoring(defaultColoringCheckBox.isSelected());
         }});
		JPanel checkBoxPanel = new JPanel(new BorderLayout());
		checkBoxPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createTitledBorder("")));
		checkBoxPanel.add(defaultColoringCheckBox, BorderLayout.NORTH);
		cellColoringConfigPanel.add(checkBoxPanel, BorderLayout.NORTH);
		cellColoringConfigPanel.add(cellColoringJPanel, BorderLayout.CENTER);
		
		
		
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
				if(!episimChart.getDefaultColoring()){
					CellColoringConfigurator config = cellColoringPanel.getCellColoringConfigurator();
					if(cellColoringPanel.getCellColoringConfigurationState()== ExpressionState.OK){
						isDirty=true;
						episimChart.setCellColoringConfigurator(config);
						episimChart.setRequiredClasses(cellDataFieldsInspector.getRequiredClasses());
						cellColoringConfigurationDialog.setVisible(false);
						cellColoringConfigurationDialog.dispose();
					}
				}
				else{
					cellColoringConfigurationDialog.setVisible(false);
					cellColoringConfigurationDialog.dispose();
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
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cellColoringConfigurationDialog.setVisible(false);
				cellColoringConfigurationDialog.dispose();
			}
		});
		bPanel.add(cancelButton, c);
		cellColoringConfigPanel.add(bPanel, BorderLayout.SOUTH);
		this.cellColoringConfigurationDialog.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.cellColoringConfigurationDialog.getContentPane().add(cellColoringConfigPanel,BorderLayout.CENTER);
		this.cellColoringConfigurationDialog.setSize(700, 800);
		this.cellColoringConfigurationDialog.validate();
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
				CellVisualizationChartCreationWizard.this.okButtonPressed = false;
				CellVisualizationChartCreationWizard.this.setVisible(false);
				CellVisualizationChartCreationWizard.this.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}
	
	private void okButtonPressed(){		
		boolean errorFound = false;
		if(!episimChart.getDefaultColoring() && episimChart.getCellColoringConfigurator()==null){
			errorFound = true;
			JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Please define the cell coloring!", "Error", JOptionPane.ERROR_MESSAGE);
		}		
		if(episimChart.getTitle() == null || episimChart.getTitle().trim().equals("")){
			errorFound = true;
			JOptionPane.showMessageDialog(CellVisualizationChartCreationWizard.this, "Please enter valid Title!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}		
		if(!errorFound){
			episimChart.setIsDirty(isDirty);
			CellVisualizationChartCreationWizard.this.okButtonPressed = true;
			CellVisualizationChartCreationWizard.this.setVisible(false);
			CellVisualizationChartCreationWizard.this.dispose();
		}
	}	
	
	private void centerMe(JDialog dialog){
		if(dialog.getParent() == null){
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation(((int)(screenDim.getWidth() /2) - (dialog.getWidth()/2)), 
			((int)(screenDim.getHeight() /2) - (dialog.getHeight()/2)));
		}
		else{
			Dimension parentDim = dialog.getParent().getSize();
			dialog.setLocation(((int)(dialog.getParent().getLocation().getX()+((parentDim.getWidth() /2) - (dialog.getWidth()/2)))), 
			((int)(dialog.getParent().getLocation().getY()+((parentDim.getHeight() /2) - (dialog.getHeight()/2)))));
		}
	}
	
	private JPanel buildChartOptionPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		chartTitleField = new JTextField();
		chartTitleField.setText(episimChart != null && episimChart.getTitle()!=null && !episimChart.getTitle().trim().isEmpty() ? episimChart.getTitle() : "Untitled");
		episimChart.setTitle(chartTitleField.getText());
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

		
		LabelledListHack list = new LabelledListHack("General Chart Options");
		list.setInsets(new Insets(2,0,2,5));
		
		list.add(new JLabel("Title"), chartTitleField);

		chartXLabel = new JTextField();
		chartXLabel.setText(episimChart != null && episimChart.getXLabel()!=null && !episimChart.getXLabel().trim().isEmpty() ? episimChart.getXLabel():"X");
		episimChart.setXLabel(chartXLabel.getText());
		chartXLabel.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setDomainAxisLabel(chartXLabel.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartXLabel.setText(getDomainAxisLabel());
			}
		});
		chartXLabel.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				setDomainAxisLabel(chartXLabel.getText());
			}
		});

		list.add(new JLabel("X Label"), chartXLabel);

		chartYLabel = new JTextField();
		chartYLabel.setText(episimChart != null && episimChart.getYLabel()!=null && !episimChart.getYLabel().trim().isEmpty() ? episimChart.getYLabel():"Y");
		episimChart.setYLabel(chartYLabel.getText());
		chartYLabel.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setRangeAxisLabel(chartYLabel.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					chartYLabel.setText(getRangeAxisLabel());
			}
		});
		chartYLabel.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				
				setRangeAxisLabel(chartYLabel.getText());
			}
		});
		list.add(new JLabel("Y Label"), chartYLabel);

		
		
      chartFrequencyLabel = new JLabel("Chart Updating Frequency in Simulation Steps: ");
		
		chartFrequencyInSimulationSteps = new NumberTextField(episimChart != null && episimChart.getChartUpdatingFrequency()>0? episimChart.getChartUpdatingFrequency():10,false){
			public double newValue(double newValue)
	      {
				isDirty = true; 
				newValue = Math.round(newValue);
				if(newValue <= 0) newValue=1;
				episimChart.setChartUpdatingFrequency((int) newValue);
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
		
		
		JButton changeCellColoringConfiguration = new JButton("Define");
		changeCellColoringConfiguration.addActionListener(new ActionListener(){			
         public void actionPerformed(ActionEvent e) {
 	         if(cellColoringConfigurationDialog != null){
 	         	if(cellColoringPanel != null && episimChart.getCellColoringConfigurator() != null){
 	         		cellColoringPanel.setCellColoringConfigurationPanelData(ObjectManipulations.cloneObject(episimChart.getCellColoringConfigurator()));
 	         	}
 	         	centerMe(cellColoringConfigurationDialog);
 	         	cellColoringConfigurationDialog.setVisible(true);
 	         }
         }});
		
		JPanel defineButtonPanel = new JPanel(new BorderLayout());
		defineButtonPanel.add(changeCellColoringConfiguration, BorderLayout.WEST);
		
		list.add(new JLabel("Cell Coloring Definition"), defineButtonPanel);		
		
		list.add(chartFrequencyLabel, chartFrequencyInSimulationSteps);		
			
      pngCheck = new JCheckBox();
      pngCheck.setSelected(false);
      episimChart.setPNGPrintingEnabled(false);
		pngCheck.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				isDirty = true;
				if(CellVisualizationChartCreationWizard.this.isVisible()){
					if(e.getStateChange() == ItemEvent.SELECTED){
						
						if(!episimChart.isPNGPrintingEnabled())selectPNGPath(false);
						
		          }
					else{
						episimChart.setPNGPrintingEnabled(false);
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
				episimChart.setPNGPrintingFrequency((int) newValue);
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
	
	private void selectPNGPath(boolean buttonCall){		
		
		ExtendedFileChooser fileChooser = new ExtendedFileChooser(".png");
		fileChooser.setDialogTitle("Choose PNG Printing Path");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
      fileChooser.setCurrentDirectory(episimChart.getPNGPrintingPath());
      fileChooser.setSelectedFile(episimChart.getPNGPrintingPath());
      
      File selectedPath = null;
      if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(CellVisualizationChartCreationWizard.this) && 
      		(selectedPath = fileChooser.getSelectedFile()) != null)
      {
      	episimChart.setPNGPrintingEnabled(true);
      	episimChart.setPNGPrintingPath(selectedPath);
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
	      	episimChart.setPNGPrintingEnabled(false);
	      	pngFrequencyInSimulationSteps.setEnabled(false);
	      	this.changePngPathButton.setEnabled(false);
	      	pngPathField.setEnabled(false);
      	}
      }
	}   
}
