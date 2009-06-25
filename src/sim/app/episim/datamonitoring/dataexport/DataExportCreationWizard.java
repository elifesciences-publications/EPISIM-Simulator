package sim.app.episim.datamonitoring.dataexport;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.jfree.data.xy.XYSeries;

import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.datamonitoring.DataEvaluationWizard;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.datamonitoring.calc.CalculationController;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartCreationWizard;
import sim.app.episim.datamonitoring.charts.EpisimChartImpl;
import sim.app.episim.datamonitoring.charts.EpisimChartSeriesImpl;
import sim.app.episim.gui.ExtendedFileChooser;



import sim.app.episim.util.Names;
import sim.app.episim.util.TissueCellDataFieldsInspector;

import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;



import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.monitoring.EpisimChartSeries;
import episiminterfaces.monitoring.EpisimDataExportColumn;
import episiminterfaces.monitoring.EpisimDataExportDefinition;



public class DataExportCreationWizard extends JDialog {
	
	
   private TissueCellDataFieldsInspector cellDataFieldsInspector;
   
   private EpisimDataExportDefinition episimDataExportDefinition;
   private boolean okButtonPressed = false;
   
   private Map<String, CellType> cellTypesMap;
   private Map<Integer, Long> columnsIdMap;
   private JTextField dataExportNameField;
  
   protected ArrayList<ColumnAttributes> attributesList = new ArrayList<ColumnAttributes>();
   
   private JLabel dataExportFrequencyLabel;
   
   private JPanel previewPanel;
   private JPanel columnsPanel;
   private JPanel propertiesPanel;
	private JSplitPane mainSplit;
   private JComboBox columnCombo;
   private NumberTextField dataExportFrequencyInSimulationSteps;
   private DefaultComboBoxModel comboModel;
  
   private JTextField csvPathField;

   
   
   
   private final String DEFAULTCOLUMNNAME = "Data Export Column ";
   private CardLayout columnsCards;
   private final int WIDTH = 1200;
   private final int HEIGHT = 450;
   
   private ExtendedFileChooser edeChooser = new ExtendedFileChooser("ede");
   private ExtendedFileChooser csvChooser = new ExtendedFileChooser("csv");
   
   private boolean isDirty = false;
  

   /** Generates a new ChartGenerator with a blank chart. */
   public DataExportCreationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector cellDataFieldsInspector){
		super(owner, title, modal);
		
		
		
		this.cellDataFieldsInspector= cellDataFieldsInspector;
		if(cellDataFieldsInspector == null) throw new IllegalArgumentException("TissueCellDataFieldsInspector was null !");
		
		this.episimDataExportDefinition = new EpisimDataExportImpl(DataExportController.getInstance().getNextDataExportId());
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		
		propertiesPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		  		   
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		propertiesPanel.add(buildDataExportPropertiesPanel(), c);
         
		JPanel columnsMainPanel = new JPanel(new GridBagLayout());
		columnsMainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Data Export Columns"),
				                                                       BorderFactory.createEmptyBorder(5, 5, 5, 5)));
				c.anchor =GridBagConstraints.WEST;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.weightx = 0.3;
				c.weighty =0;
				c.insets = new Insets(5,5,5,5);
				c.gridwidth = GridBagConstraints.RELATIVE;
				
				comboModel = new DefaultComboBoxModel();
				columnCombo = new JComboBox(comboModel);
				columnCombo.addItemListener(new ItemListener(){
					public void itemStateChanged(ItemEvent evt) {
						 isDirty = true;	
					    CardLayout cl = (CardLayout)(columnsPanel.getLayout());
					    cl.show(columnsPanel, ""+ columnCombo.getSelectedIndex());
					}
				});
				
				columnsMainPanel.add(columnCombo, c);
				
			
				c.gridwidth = GridBagConstraints.REMAINDER;
				c.fill = GridBagConstraints.NONE;
				c.weightx = 0.0;
				JButton addSeriesButton = new JButton("Add Data Export Column");
				addSeriesButton.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						isDirty = true;	
						int index = addDataExportColumn();
	               	comboModel.addElement(DEFAULTCOLUMNNAME + (index+1));
	               	columnCombo.setSelectedIndex(index);
               }
					
				});
				columnsMainPanel.add(addSeriesButton, c);
				
				c.anchor =GridBagConstraints.CENTER;
				c.fill = GridBagConstraints.BOTH;
				c.weightx = 1;
				c.weighty =1;
				c.insets = new Insets(5,5,5,5);
				c.gridwidth = GridBagConstraints.REMAINDER;
									
				columnsCards = new CardLayout();
				columnsPanel = new JPanel(columnsCards);
				columnsMainPanel.add(columnsPanel, c);
		
		
		
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		
			
		propertiesPanel.add(columnsMainPanel, c);
		
		previewPanel = this.buildPreviewPanel();
		previewPanel.setPreferredSize(new Dimension(getPreferredSize().width,	(int)(getPreferredSize().height*0.7)));
		previewPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Preview"), 
                                                                      BorderFactory.createEmptyBorder(5,5,5,5)));
		
		JPanel layoutCorrectingPanel = new JPanel(new BorderLayout());
		layoutCorrectingPanel.add(propertiesPanel,BorderLayout.NORTH);
		mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
		new JScrollPane(layoutCorrectingPanel), previewPanel);
      mainSplit.setOneTouchExpandable(false);
      mainSplit.setDividerLocation(((int)WIDTH /2));
	        
      getContentPane().add(mainSplit, BorderLayout.CENTER);
      getContentPane().add(buildOKCancelButtonPanel(), BorderLayout.SOUTH);
      setSize(WIDTH, HEIGHT);
 		validate();
   }
     
  
   
   
   private int addDataExportColumn()
   {
   	 int i = this.episimDataExportDefinition.getEpisimDataExportColumns().size();
   	 EpisimDataExportColumn episimDataExportColumn = new EpisimDataExportColumnImpl(System.currentTimeMillis());
   	 this.episimDataExportDefinition.addEpisimDataExportColumn(episimDataExportColumn);
   	 episimDataExportColumn.setName(DEFAULTCOLUMNNAME+ (i+1));
   	 
   	 this.columnsIdMap.put(i, episimDataExportColumn.getId());
   	 
   	 ColumnAttributes columnAttr = new ColumnAttributes(previewPanel,i);
      
       columnsPanel.add(columnAttr, ""+i);
       attributesList.add(columnAttr);
     
      
       refreshPreview();
       validate();
       
       rebuildColumnsIdMap();
       return i;
   }
   
   private void refreshPreview(){
   	 this.previewPanel.removeAll();
       this.previewPanel.setLayout(new BorderLayout());
       this.previewPanel.add(buildPreviewPanel(), BorderLayout.CENTER);
       this.previewPanel.validate();
       this.previewPanel.repaint();
   }
   
   private void rebuildColumnsIdMap(){
   	columnsIdMap = new HashMap<Integer, Long>();
   	int i = 0;
   	for(EpisimDataExportColumn actColumn :this.episimDataExportDefinition.getEpisimDataExportColumns()){
   		columnsIdMap.put(i, actColumn.getId());
   		i++;
   	}
   }
   
   private void addDataExportColumn(int index, EpisimDataExportColumn column) {

		if(column != null){

			this.columnsIdMap.put(index, column.getId());

			ColumnAttributes columnAttr = new ColumnAttributes(previewPanel, index);
			columnAttr.setName(column.getName());
			columnAttr.setCalculationAlgorithmConfigurator(column.getCalculationAlgorithmConfigurator());
			columnAttr.getFormulaButton().setText("Edit");
			columnsPanel.add(columnAttr, "" + index);
			attributesList.add(columnAttr);
			comboModel.addElement(column.getName());
			refreshPreview();
			validate();

			rebuildColumnsIdMap();
		}
	}
   
  
       
   /* Removes the series at the given index and returns it. */
   public boolean removeColumn(int index) {

		this.episimDataExportDefinition.removeEpisimDataExportColumn(columnsIdMap.get(index));

		Iterator<ColumnAttributes> iter = attributesList.iterator();
		while (iter.hasNext()){

			ColumnAttributes columnAttr = iter.next();

			if(columnAttr.columnIndex == index){

				columnsPanel.remove(index);
				comboModel.removeElementAt(index);

				Component[] comps = columnsPanel.getComponents();
				columnsPanel.removeAll();
				for(int i = 0; i < comps.length; i++)
					columnsPanel.add(comps[i], "" + i);
				columnsCards.show(columnsPanel, "" + (index-1));
				columnsPanel.validate();
				columnsPanel.repaint();
				iter.remove();
			}
			else if(columnAttr.columnIndex > index){
				columnAttr.columnIndex--;

			}
		}
		rebuildColumnsIdMap();
		refreshPreview();
		validate();

		return true;
	}
               
   
   
   private void addRandomValues(JTable table){
   	Random rand = new Random();
   	for(int i = 0; i < table.getModel().getRowCount(); i++){
   		for(int n = 0; n < table.getModel().getColumnCount(); n++){
   			table.getModel().setValueAt(rand.nextDouble(), i, n);
   		}
   	}
   }
   
   private JPanel buildPreviewPanel(){
   	JPanel previewPanel = new JPanel(new BorderLayout());
   
   	JTable previewTable = new JTable();	
   	if(this.episimDataExportDefinition != null && this.episimDataExportDefinition.getEpisimDataExportColumns().size() > 0){
   		
   		final String [] columnNamesFinal = new String[this.episimDataExportDefinition.getEpisimDataExportColumns().size()];
   		int i = 0;
   		for(EpisimDataExportColumn column: this.episimDataExportDefinition.getEpisimDataExportColumns()){
   			columnNamesFinal[i] = column.getName();
   			i++;
   		}
   		
   		
   		previewTable.setModel(new AbstractTableModel() {

				private String[] columnNames = columnNamesFinal;

				private Object[][] data = new Object[10][columnNames.length];

				public int getColumnCount() {

					return columnNames.length;
				}

				public int getRowCount() {

					return data.length;
				}

				public String getColumnName(int col) {

					return columnNames[col];
				}

				public Object getValueAt(int row, int col) {

					return data[row][col];
				}

				public Class getColumnClass(int c) {

					return super.getColumnClass(c);
				}

				public boolean isCellEditable(int row, int col) {
					
					return false;
				}

				public void setValueAt(Object value, int row, int col) {

					data[row][col] = value;
					fireTableCellUpdated(row, col);
				}

			});
   	 }
   	
   	
   	JLabel l=(JLabel)previewTable.getTableHeader().getDefaultRenderer();
   	l.setPreferredSize(new Dimension(0,25));
   	addRandomValues(previewTable);
 //previewTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
 //previewTable.setFillsViewportHeight(true);
   	previewPanel.add(new JScrollPane(previewTable), BorderLayout.CENTER);
   	
   	return previewPanel;
   }
    	
	public void showWizard(){
		
			isDirty = false;
			showWizard(null);
	}
		
	private void restoreDataExportValues(EpisimDataExportDefinition dataExport){
		if(dataExport != null){
			this.episimDataExportDefinition = dataExport;
			this.columnsIdMap = new HashMap<Integer, Long>();
			this.dataExportNameField.setText(dataExport.getName());
			this.csvPathField.setText(dataExport.getCSVFilePath().getAbsolutePath());
			this.dataExportFrequencyInSimulationSteps.setValue(dataExport.getDataExportFrequncyInSimulationSteps());
			int i = 0;
			for(EpisimDataExportColumn col : dataExport.getEpisimDataExportColumns()){
				addDataExportColumn(i, col);
				i++;
			}
			isDirty = false;
		}
		
	}
	
	
		
	public void showWizard(EpisimDataExportDefinition dataExport){
		isDirty = false;
		if(dataExport != null) restoreDataExportValues(dataExport);
		rebuildColumnsIdMap();
		repaint();
		centerMe();
		setVisible(true);
	}
	
	public EpisimDataExportDefinition getEpisimDataExport(){
		if(this.okButtonPressed){
			this.episimDataExportDefinition.getRequiredClasses().clear();
			for(Class<?> actClass: this.cellDataFieldsInspector.getRequiredClasses()){
				this.episimDataExportDefinition.addRequiredClass(actClass);
			}
			return this.episimDataExportDefinition;
		}
		return null;
	}
	private JPanel buildOKCancelButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
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

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		c.gridwidth = 1;

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				DataExportCreationWizard.this.okButtonPressed = false;
				DataExportCreationWizard.this.setVisible(false);
				DataExportCreationWizard.this.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}
	
	private void okButtonPressed(){
		boolean errorFound = false;
			
		if(this.episimDataExportDefinition.getName() == null || this.episimDataExportDefinition.getName().trim().equals("") ){
			errorFound = true;
			JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Please enter a Name for the Data Export!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if(this.episimDataExportDefinition.getEpisimDataExportColumns().size() == 0){
			errorFound = true;
			JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Please add at least one column for Data Export!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		if(this.episimDataExportDefinition.getCSVFilePath() == null){
			errorFound = true;
			JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Please define Path for CSV-File!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		if(!errorFound){ 
			errorFound = !hasEveryColumnAnExpression();
			if(errorFound)
				JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Not every Column has an Calculation Expression!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		if(!errorFound){
			this.episimDataExportDefinition.setIsDirty(isDirty);
			DataExportCreationWizard.this.okButtonPressed = true;
			DataExportCreationWizard.this.setVisible(false);
			DataExportCreationWizard.this.dispose();
         
			
		}
	}
	
	private boolean hasEveryColumnAnExpression(){
		for(EpisimDataExportColumn col : this.episimDataExportDefinition.getEpisimDataExportColumns()){
			if(!CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(col.getCalculationAlgorithmConfigurator(), true, this.cellDataFieldsInspector)) return false;
		}
		
		return true;
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	private JPanel buildDataExportPropertiesPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		Box globalAttributes = Box.createVerticalBox();
		dataExportNameField = new JTextField();
		dataExportNameField.setText("Untitled");
		this.episimDataExportDefinition.setName("Untitled");
		dataExportNameField.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					setTitle(dataExportNameField.getText());
					episimDataExportDefinition.setName(dataExportNameField.getText());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					dataExportNameField.setText(getTitle());
			}
		});
		dataExportNameField.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				setTitle(dataExportNameField.getText());
				episimDataExportDefinition.setName(dataExportNameField.getText());
			}
		});

		LabelledList list = new LabelledList("Data Export");
		
		list.add(new JLabel("Name"), dataExportNameField);

		      
      dataExportFrequencyLabel = new JLabel("Data Export Frequency in Simulation Steps: ");
		
		dataExportFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				isDirty = true;
				 newValue = Math.round(newValue);
				 episimDataExportDefinition.setDataExportFrequncyInSimulationSteps((int) newValue);
	        return newValue;
	      }
		};
		dataExportFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {
				isDirty = true;
				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					episimDataExportDefinition.setDataExportFrequncyInSimulationSteps((int)dataExportFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					dataExportFrequencyInSimulationSteps.setValue(dataExportFrequencyInSimulationSteps.getValue());
			}
		});
		dataExportFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				episimDataExportDefinition.setDataExportFrequncyInSimulationSteps((int)dataExportFrequencyInSimulationSteps.getValue());
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
					episimDataExportDefinition.setCSVFilePath(showCSVPathDialog(csvPathField.getText()));
				}
				else episimDataExportDefinition.setCSVFilePath(showCSVPathDialog(""));
				if(episimDataExportDefinition.getCSVFilePath() != null) csvPathField.setText(episimDataExportDefinition.getCSVFilePath().getAbsolutePath());
            
         }});
		fieldButtonPanel.add(csvPathField, BorderLayout.CENTER);
		fieldButtonPanel.add(editCSVPathButton, BorderLayout.EAST);
		
		list.add(new JLabel("CSV-File-Path: "), fieldButtonPanel);
		
		
		
		
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
	}
	
	
	private File showCSVPathDialog(String path){
   	
   	if(path!= null && !path.equals("")) csvChooser.setCurrentDirectory(new File(path));
   	
   	csvChooser.setDialogTitle("Choose CSV-File-Path");
		if(csvChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) return csvChooser.getSelectedFile();
		return null;
	}
	private File showDataExportDefinitionPathDialog(String path){
   	
   	if(path!= null && !path.equals("")) edeChooser.setCurrentDirectory(new File(path));
   	
   	edeChooser.setDialogTitle("Choose Data-Export-Definition-Path");
		if(edeChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) return edeChooser.getSelectedFile();
		return null;
	}
	
	
   private class ColumnAttributes extends LabelledList {

		private CalculationAlgorithmConfigurator calculationConfig = null;

		//Color fillColor = CLEAR;

		private JTextField columnName;

		private JTextField formulaField;

		private int columnIndex;

		private JPanel previewPanel;
		
		private JButton formulaButton;

		public ColumnAttributes(JPanel panel, int index) {

			super(episimDataExportDefinition.getEpisimDataExportColumn(columnsIdMap.get(index)).getName());

			previewPanel = panel;
			columnIndex = index;
			columnName = new JTextField(DEFAULTCOLUMNNAME + (index + 1));
			columnName.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					isDirty = true;
					setBorderTitle(columnName.getText());
					int index = columnCombo.getSelectedIndex();
					if(index > -1){
						episimDataExportDefinition.getEpisimDataExportColumn(columnsIdMap.get(index)).setName(columnName.getText());
						comboModel.removeElementAt(index);
						comboModel.insertElementAt(columnName.getText(), index);
						columnCombo.setSelectedIndex(index);
					}
					refreshPreview();
					repaint();

				}
			});
			columnName.addFocusListener(new FocusAdapter() {

				public void focusLost(FocusEvent e) {

					for(ActionListener actList : columnName.getActionListeners()){
						actList.actionPerformed(new ActionEvent(columnName, 0, ""));
					}
				}
			});
			addLabelled("Name", columnName);

			JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					isDirty = true;
					if(JOptionPane.showOptionDialog(null, "Remove the Series " + columnName.getText() + "?", "Confirm",
					      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					      new Object[] { "Remove", "Cancel" }, null) == 0) // remove
						DataExportCreationWizard.this.removeColumn(columnIndex);
				}
			});

			formulaButton = new JButton("Add");
			formulaField = new JTextField("");
			formulaButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					isDirty = true;
					DataEvaluationWizard editor = new DataEvaluationWizard(((Frame) DataExportCreationWizard.this.getOwner()),
					      "Calculation Expression Editor: " + ((String) columnCombo.getSelectedItem()), true,
					      cellDataFieldsInspector);
					calculationConfig = editor.getCalculationAlgorithmConfigurator(calculationConfig);
					if(CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(calculationConfig, false, cellDataFieldsInspector)){
						formulaButton.setText("Edit");
						formulaField.setText(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(calculationConfig.getCalculationAlgorithmID()).getName());
						int index = columnCombo.getSelectedIndex();
						episimDataExportDefinition.getEpisimDataExportColumn(columnsIdMap.get(index)).setCalculationAlgorithmConfigurator(
						      calculationConfig);
					}

				}

			});
			formulaField.setEditable(false);
			JPanel fieldButtonPanel = new JPanel(new BorderLayout(5,0));
			fieldButtonPanel.add(formulaField, BorderLayout.CENTER);
			fieldButtonPanel.add(formulaButton, BorderLayout.EAST);
			add(new JLabel("Expression:"), fieldButtonPanel);

			Box b = new Box(BoxLayout.X_AXIS);
			b.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
			b.add(removeButton);
			b.add(Box.createGlue());
			add(b);

		}

		public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator() {

			return calculationConfig;
		}

		public void setCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator config) {

			if(CalculationController.getInstance().isValidCalculationAlgorithmConfiguration(config, false, cellDataFieldsInspector)){
				this.calculationConfig = config;
				this.formulaField.setText(CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(config.getCalculationAlgorithmID()).getName());
			}

		}
		
		public JButton getFormulaButton(){ return this.formulaButton;}
		
		public int getColumnIndex() {

			return columnIndex;
		}

		public void setColumnIndex(int columnIndex) {

			this.columnIndex = columnIndex;
		}

		public void setName(String name) {

			this.columnName.setText(name);
		}
		
		public void setBorderTitle(String title){
		   
	      if (title != null) setBorder(new javax.swing.border.TitledBorder(title));
	      
	   }

	}
   

	

	}
