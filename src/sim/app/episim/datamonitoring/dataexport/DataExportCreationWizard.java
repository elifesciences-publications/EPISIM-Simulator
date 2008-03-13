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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import sim.app.episim.CellType;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.ExpressionCheckerController;
import sim.app.episim.datamonitoring.ExpressionEditor;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartCreationWizard;
import sim.app.episim.datamonitoring.charts.EpisimChartImpl;
import sim.app.episim.datamonitoring.charts.EpisimChartSeriesImpl;


import sim.app.episim.util.TissueCellDataFieldsInspector;

import sim.util.gui.LabelledList;
import sim.util.gui.NumberTextField;



import episiminterfaces.EpisimChartSeries;
import episiminterfaces.EpisimDataExport;
import episiminterfaces.EpisimDataExportColumn;



public class DataExportCreationWizard extends JDialog {
	
	
   private TissueCellDataFieldsInspector cellDataFieldsInspector;
   
   private EpisimDataExport episimDataExport;
   private boolean okButtonPressed = false;
   
   private Map<String, CellType> cellTypesMap;
   private Map<Integer, Long> columnsIdMap;
   private JTextField dataExportNameField;
  
   
   
   private JLabel dataExportFrequencyLabel;
   
   private JPanel previewPanel;
   private JPanel columnsPanel;
   private JPanel propertiesPanel;
	private JSplitPane mainSplit;
   private JComboBox columnCombo;
   private NumberTextField dataExportFrequencyInSimulationSteps;
   private DefaultComboBoxModel comboModel;
  
   
   private final String DEFAULTCOLUMNNAME = "Data Export Column ";
 
   private final int WIDTH = 1200;
   private final int HEIGHT = 600;
   
   
  

   /** Generates a new ChartGenerator with a blank chart. */
   public DataExportCreationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector cellDataFieldsInspector){
		super(owner, title, modal);
		
		this.cellDataFieldsInspector= cellDataFieldsInspector;
		if(cellDataFieldsInspector == null) throw new IllegalArgumentException("TissueCellDataFieldsInspector was null !");
		
		
		
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		
		propertiesPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		  		   
		c.anchor =GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.weighty =0;
		c.insets = new Insets(10,10,10,10);
		c.gridwidth = GridBagConstraints.REMAINDER;
		propertiesPanel.add(buildChartOptionPanel(), c);
         
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
									
				
				columnsPanel = new JPanel(new CardLayout());
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
     
  
   public int addDataExportColumn()
   {
   	 int i = this.episimDataExport.getEpisimDataExportColumns().size();
   	 EpisimDataExportColumn episimDataExportColumn = new EpisimDataExportColumnImpl(System.currentTimeMillis());
   	 
   	 episimDataExportColumn.setName(DEFAULTCOLUMNNAME+ (i+1));
   	 
   	 
   	 ColumnAttributes columnAttr = new ColumnAttributes(previewPanel,i);
      
       columnsPanel.add(columnAttr, ""+i);
      
       
       validate();
       this.episimDataExport.addEpisimDataExportColumn(episimDataExportColumn);
       
       this.previewPanel.removeAll();
       this.previewPanel.setLayout(new BorderLayout());
       this.previewPanel.add(buildPreviewPanel(), BorderLayout.CENTER);
       
       validate();
       
       rebuildColumnsIdMap();
       return 0;
   }
   
   private void rebuildColumnsIdMap(){
   	columnsIdMap = new HashMap<Integer, Long>();
   	int i = 0;
   	for(EpisimDataExportColumn actColumn :this.episimDataExport.getEpisimDataExportColumns()){
   		columnsIdMap.put(i, actColumn.getId());
   		i++;
   	}
   }
   
   private void addDataExportColumn(int index, EpisimDataExportColumn column){
   	 	
     
      
      validate();
      
   }
   
  
       
   /* Removes the series at the given index and returns it. */
   public boolean removeColumn(int index)
   {
   	 this.episimDataExport.removeEpisimDataExportColumn(columnsIdMap.get(index));
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
   	if(this.episimDataExport != null){
   		
   		final String [] columnNamesFinal = new String[this.episimDataExport.getEpisimDataExportColumns().size()];
   		this.episimDataExport.getEpisimDataExportColumns().toArray(columnNamesFinal);
   		
   		previewTable.setModel(new AbstractTableModel() {

				private String[] columnNames = null;

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

					return getValueAt(0, c).getClass();
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
   	
   	previewPanel.add(previewTable, BorderLayout.CENTER);
   	
   	return previewPanel;
   }
    	
	public void showWizard(){
		
			
			showWizard(null);
	}
		
	private void restoreDataExportValues(EpisimDataExport dataExport){
		if(dataExport != null){
			
		}
		
	}
	
	
		
	public void showWizard(EpisimDataExport dataExport){
		if(dataExport != null) restoreDataExportValues(dataExport);
		repaint();
		centerMe();
		setVisible(true);
	}
	
	public EpisimDataExport getEpisimDataExport(){
		if(this.okButtonPressed){
			this.episimDataExport.getRequiredClasses().clear();
			for(Class<?> actClass: this.cellDataFieldsInspector.getRequiredClasses()){
				this.episimDataExport.addRequiredClass(actClass);
			}
			return this.episimDataExport;
		}
		return null;
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
		
		try{
			for(EpisimDataExportColumn col : this.episimDataExport.getEpisimDataExportColumns()){
				ExpressionCheckerController.getInstance().checkChartExpression(col.getCalculationExpression()[0], this.cellDataFieldsInspector);
			}
		}
		catch (Exception e1){
			ExceptionDisplayer.getInstance().displayException(e1);
		}
		
		
		if(this.episimDataExport.getName() == null || this.episimDataExport.getName().trim().equals("") ){
			errorFound = true;
			JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Please enter a Name for the Data Export!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		if(this.episimDataExport.getEpisimDataExportColumns().size() == 0){
			errorFound = true;
			JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Please add at least one column for Data Export!", "Error", JOptionPane.ERROR_MESSAGE);
		
		}
		if(!errorFound){ 
			errorFound = !hasEveryColumnAnExpression();
			if(errorFound)
				JOptionPane.showMessageDialog(DataExportCreationWizard.this, "Not every Column has an Calculation Expression!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		if(!errorFound){ 
			DataExportCreationWizard.this.okButtonPressed = true;
			DataExportCreationWizard.this.setVisible(false);
			DataExportCreationWizard.this.dispose();
		}
	}
	
	private boolean hasEveryColumnAnExpression(){
		for(EpisimDataExportColumn col : this.episimDataExport.getEpisimDataExportColumns()){
			if(col.getCalculationExpression() == null
			 || col.getCalculationExpression().length < 2
			 || col.getCalculationExpression()[0] == null
			 || col.getCalculationExpression()[1] == null
			 || col.getCalculationExpression()[0].trim().equals("")
			 || col.getCalculationExpression()[1].trim().equals("")) return false;
			else{
				try{
					ExpressionCheckerController.getInstance().checkChartExpression(col.getCalculationExpression()[0], this.cellDataFieldsInspector);
				}
				catch (Exception e1){
					ExceptionDisplayer.getInstance().displayException(e1);
				}
			}
		}
		
		return true;
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	private JPanel buildChartOptionPanel() {

		JPanel optionsPanel = new JPanel(new BorderLayout());
		Box globalAttributes = Box.createVerticalBox();
		dataExportNameField = new JTextField();
		dataExportNameField.setText("Untitled");
		this.episimDataExport.setName("Untitled");
		dataExportNameField.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

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

		LabelledList list = new LabelledList("Data Export");
		
		list.add(new JLabel("Name"), dataExportNameField);

		      
      dataExportFrequencyLabel = new JLabel("Data Export Frequency in Simulation Steps: ");
		
		dataExportFrequencyInSimulationSteps = new NumberTextField(100,false){
			public double newValue(double newValue)
	      {
				 newValue = Math.round(newValue);
				 episimDataExport.setDataExportFrequncyInSimulationSteps((int) newValue);
	        return newValue;
	      }
		};
		dataExportFrequencyInSimulationSteps.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent keyEvent) {

				if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
					episimDataExport.setDataExportFrequncyInSimulationSteps((int)dataExportFrequencyInSimulationSteps.getValue());
				}
				else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
					dataExportFrequencyInSimulationSteps.setValue(dataExportFrequencyInSimulationSteps.getValue());
			}
		});
		dataExportFrequencyInSimulationSteps.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {

				episimDataExport.setDataExportFrequncyInSimulationSteps((int)dataExportFrequencyInSimulationSteps.getValue());
			}
		});
		
		
		list.add(dataExportFrequencyLabel, dataExportFrequencyInSimulationSteps);
		
			
		
		
		
		optionsPanel.add(list, BorderLayout.CENTER);
		
		return optionsPanel;
	}
	
	
   private class ColumnAttributes extends LabelledList {

		private String[] expression = new String[2];

		//Color fillColor = CLEAR;

		private JTextField columnName;

		private JTextField formulaField;

		private int columnIndex;

		private JPanel previewPanel;

		public ColumnAttributes(JPanel panel, int index) {

			super("" + episimDataExport.getEpisimDataExportColumn(columnsIdMap.get(index)));

			previewPanel = panel;
			columnIndex = index;
			columnName = new JTextField(DEFAULTCOLUMNNAME + (index + 1));
			columnName.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					int index = columnCombo.getSelectedIndex();
					if(index > -1){
						episimDataExport.getEpisimDataExportColumn(columnsIdMap.get(index)).setName(columnName.getText());
						comboModel.removeElementAt(index);
						comboModel.insertElementAt(columnName.getText(), index);
						columnCombo.setSelectedIndex(index);
					}
					previewPanel.repaint();

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

					if(JOptionPane.showOptionDialog(null, "Remove the Series " + columnName.getText() + "?", "Confirm",
					      JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					      new Object[] { "Remove", "Cancel" }, null) == 0) // remove
						DataExportCreationWizard.this.removeColumn(columnIndex);
				}
			});

			final JButton formulaButton = new JButton("Add Expression");
			formulaField = new JTextField("");
			formulaButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					ExpressionEditor editor = new ExpressionEditor(((Frame) DataExportCreationWizard.this.getOwner()),
					      "Calculation Expression Editor: " + ((String) columnCombo.getSelectedItem()), true,
					      cellDataFieldsInspector);
					expression = editor.getExpression(expression);
					if(expression != null && expression[0] != null && expression[1] != null){
						formulaButton.setText("Edit Expression");
						formulaField.setText(expression[0]);
						int index = columnCombo.getSelectedIndex();
						episimDataExport.getEpisimDataExportColumn(columnsIdMap.get(index)).setCalculationExpression(
						      expression);
					}

				}

			});
			formulaField.setEditable(false);
			add(formulaButton, formulaField);

			Box b = new Box(BoxLayout.X_AXIS);
			b.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
			b.add(removeButton);
			b.add(Box.createGlue());
			add(b);

		}

		public String[] getExpression() {

			return expression;
		}

		public void setExpression(String[] expression) {

			if(expression != null && expression.length >= 2 && expression[0] != null && expression[1] != null){
				this.expression = expression;
				this.formulaField.setText(expression[0]);
			}

		}

		public int getColumnIndex() {

			return columnIndex;
		}

		public void setColumnIndex(int columnIndex) {

			this.columnIndex = columnIndex;
		}

		public void setName(String name) {

			this.columnName.setText(name);
		}

	}
   

	

	}
