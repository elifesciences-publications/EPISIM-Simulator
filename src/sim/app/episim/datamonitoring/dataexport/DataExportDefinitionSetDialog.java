package sim.app.episim.datamonitoring.dataexport;

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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartSetDialog;
import sim.app.episim.datamonitoring.charts.EpisimChartSetImpl;
import sim.app.episim.datamonitoring.charts.ChartController.ChartType;
import sim.app.episim.datamonitoring.dataexport.DataExportController.DataExportType;
import sim.app.episim.gui.EpisimProgressWindow;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.ObjectManipulations;
import episimexceptions.CompilationFailedException;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;
import episiminterfaces.monitoring.EpisimDataExportDefinition;
import episiminterfaces.monitoring.EpisimDataExportDefinitionSet;
import episiminterfaces.monitoring.EpisimDiffFieldDataExport;


public class DataExportDefinitionSetDialog extends JDialog {
		
		
		private EpisimDataExportDefinitionSet episimDataExportDefinitionSet;
		private EpisimDataExportDefinitionSet episimDataExportDefinitionSetOld;
		
				
		private JPanel buttonPanel;
		
		private JList dataExportDefinitionsList;
		
		private JTextField dataExportDefinitionSetName;
		private JTextField pathText;
		
		private Map<Integer, Long> indexDataExportDefinitionIdMap;
		private ExtendedFileChooser edeChooser = new ExtendedFileChooser("ede");
		
		private JDialog dialog;
		
		private JButton editButton;
		private JButton removeButton;
		private EpisimProgressWindow progressWindow;
		private Frame owner;
		
		private boolean okButtonPressed = false;
		
		private boolean isDirty = false;
		
		public DataExportDefinitionSetDialog(Frame owner, String title, boolean modal){
			super(owner, title, modal);
			
			
			
			indexDataExportDefinitionIdMap = new HashMap<Integer, Long>();
			
			this.owner = owner;
		   getContentPane().setLayout(new GridBagLayout());
		   GridBagConstraints c = new GridBagConstraints();
		   
		  
		   c.anchor =GridBagConstraints.CENTER;
		   c.fill = GridBagConstraints.HORIZONTAL;
		   c.weightx = 1;
		   c.weighty =0;
		   c.insets = new Insets(10,10,10,10);
		   c.gridwidth = GridBagConstraints.REMAINDER;
		   getContentPane().add(buildNamePanel(), c);
		  
		   
		  dataExportDefinitionsList = new JList();
		  dataExportDefinitionsList.setModel(new DefaultListModel());
		  dataExportDefinitionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		  dataExportDefinitionsList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {

					if((e.getValueIsAdjusting() != false) && dataExportDefinitionsList.getSelectedIndex() != -1){
						editButton.setEnabled(true);
						removeButton.setEnabled(true);
					}
					else if((e.getValueIsAdjusting() != false) && dataExportDefinitionsList.getSelectedIndex() == -1){
						editButton.setEnabled(false);
						removeButton.setEnabled(false);
					}
				}

			});
		  
		  JScrollPane exportDefinitionsListScroll = new JScrollPane(dataExportDefinitionsList);
		  exportDefinitionsListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("EPISIM-Data-Export-Definitions"),
			      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		   
		   c.anchor =GridBagConstraints.CENTER;
		   c.fill = GridBagConstraints.BOTH;
		   c.weightx = 1;
		   c.weighty =1;
		   c.insets = new Insets(10,10,10,10);
		   c.gridwidth = GridBagConstraints.RELATIVE;
		   getContentPane().add(exportDefinitionsListScroll, c);
		   
		   c.anchor =GridBagConstraints.WEST;
		   c.fill = GridBagConstraints.NONE;
		   c.weightx = 0;
		   c.weighty =1;
		   c.insets = new Insets(10,10,10,10);
		   c.gridwidth = GridBagConstraints.REMAINDER;
		   getContentPane().add(buildAddRemoveEditButtonPanel(), c);
		   
		   c.anchor =GridBagConstraints.CENTER;
		   c.fill = GridBagConstraints.HORIZONTAL;
		   c.weightx = 1;
		   c.weighty =0;
		   c.insets = new Insets(10,10,10,10);
		   c.gridwidth = GridBagConstraints.REMAINDER;
		   getContentPane().add(buildPathPanel(), c); 
		   
		   c.anchor =GridBagConstraints.WEST;
		   c.gridwidth=GridBagConstraints.REMAINDER; 
		   c.fill = GridBagConstraints.HORIZONTAL;
		   c.weightx = 1;
		   c.weighty =0;
		   c.insets = new Insets(10,10,10,10);
		   this.buttonPanel = buildOKCancelButtonPanel();
		   getContentPane().add(buttonPanel, c);
		   
		   
		   
		   
		  
		   
		   setSize(500, 400);
			validate();
			dialog = this;
		}
		
		public EpisimDataExportDefinitionSet showDataExportDefinitionSet(EpisimDataExportDefinitionSet dataExportDefinitonSet){
			
			if(dataExportDefinitonSet == null) throw new IllegalArgumentException("DataExportDefinitionSet to display was null!");
			okButtonPressed = false;
			indexDataExportDefinitionIdMap = new HashMap<Integer, Long>();
			this.episimDataExportDefinitionSet = dataExportDefinitonSet;
			this.episimDataExportDefinitionSetOld = dataExportDefinitonSet.clone();
			this.dataExportDefinitionSetName.setText(dataExportDefinitonSet.getName());
			DefaultListModel listModel = new DefaultListModel();
			int i = 0;
			for(EpisimDataExportDefinition actDef : dataExportDefinitonSet.getEpisimDataExportDefinitions()){
				indexDataExportDefinitionIdMap.put(i, actDef.getId());
				listModel.addElement(actDef.getName());
				i++;
			}
			for(EpisimDiffFieldDataExport actDef : dataExportDefinitonSet.getEpisimDiffFieldDataExportDefinitions()){
				indexDataExportDefinitionIdMap.put(i, actDef.getId());
				listModel.addElement(actDef.getName());
				i++;
			}
			this.dataExportDefinitionsList.setModel(listModel);
			if(dataExportDefinitonSet.getPath() != null) this.pathText.setText(dataExportDefinitonSet.getPath().getAbsolutePath());
			centerMe();
			this.setVisible(true);
			isDirty = false;
			if(okButtonPressed) return this.episimDataExportDefinitionSet;
			
			return this.episimDataExportDefinitionSetOld;
		}
		
		public EpisimDataExportDefinitionSet showNewDataExportDefinitionSet(){
			isDirty = true;
			EpisimDataExportDefinitionSet newDataExportDefinitionSet = showDataExportDefinitionSet(new EpisimDataExportDefinitionSetImpl());
			if(okButtonPressed) return newDataExportDefinitionSet;
			
			return null;
		}
		
		
		
		private void centerMe(){
			if(this.getParent() == null){
				Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
				this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
				((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
			}
			else{
				Dimension parentDim = this.getParent().getSize();
				this.setLocation(((int)(this.getParent().getLocation().getX()+((parentDim.getWidth() /2) - (this.getWidth()/2)))), 
				((int)(this.getParent().getLocation().getY()+((parentDim.getHeight() /2) - (this.getHeight()/2)))));
			}
		}
		
		private JPanel buildNamePanel(){
			JPanel namePanel = new JPanel(new GridBagLayout());
			 GridBagConstraints c = new GridBagConstraints();
			 c.anchor =GridBagConstraints.WEST;
			   c.fill = GridBagConstraints.NONE;
			   c.weightx = 0;
			   c.weighty =0;
			   c.insets = new Insets(10,10,10,10);
			   c.gridwidth = GridBagConstraints.RELATIVE;
			   namePanel.add(new JLabel("Data-Export-Definition-Set-Name: "), c);
			   
			   dataExportDefinitionSetName = new JTextField("noname"); 
			   dataExportDefinitionSetName.addKeyListener(new KeyAdapter() {

					public void keyPressed(KeyEvent keyEvent) {
						isDirty = true;
						if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
							episimDataExportDefinitionSet.setName(dataExportDefinitionSetName.getText());
						}
						else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
							dataExportDefinitionSetName.setText(dataExportDefinitionSetName.getText());
					}
				});
			   dataExportDefinitionSetName.addFocusListener(new FocusAdapter() {

					public void focusLost(FocusEvent e) {

						episimDataExportDefinitionSet.setName(dataExportDefinitionSetName.getText());
					}
				});
			   c.anchor =GridBagConstraints.CENTER;
			   c.fill = GridBagConstraints.HORIZONTAL;
			   c.weightx = 1;
			   c.weighty =0;
			   c.insets = new Insets(10,10,10,10);
			   c.gridwidth = GridBagConstraints.REMAINDER;
			   namePanel.add(dataExportDefinitionSetName, c);
			   
			   return namePanel;
		}
		
		private JPanel buildAddRemoveEditButtonPanel(){
			JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10 ,10 ));
			
			JButton addButton = new JButton("Add DED");
			
			addButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					Object result = null;
					if(ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfEpisimExtraCellularDiffusionFieldConfigurations() > 0){
						DataExportType[] types = DataExportType.values();
						result= JOptionPane.showInputDialog(DataExportDefinitionSetDialog.this, "Choose Data-Export-Type:", "Data-Export-Type", JOptionPane.QUESTION_MESSAGE,
							  																null, types, DataExportType.REGULAR_DATA_EXPORT);
					}
					else result = DataExportType.REGULAR_DATA_EXPORT;
					if(result == DataExportType.REGULAR_DATA_EXPORT){
						 EpisimDataExportDefinition newDefinition = DataExportController.getInstance().showDataExportCreationWizard(DataExportDefinitionSetDialog.this.owner);
				       if(newDefinition != null){
				      	 isDirty = true;
				      	 ((DefaultListModel)(DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel())).addElement(newDefinition.getName());
				      	 indexDataExportDefinitionIdMap.put((DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel().getSize()-1), newDefinition.getId());
				      	 episimDataExportDefinitionSet.addEpisimDataExportDefinition(newDefinition);
				      	 
				       }
					}
					else if(result == DataExportType.DIFF_FIELD_DATA_EXPORT){
						 EpisimDiffFieldDataExport newDefinition = DataExportController.getInstance().showDiffFieldDataExportCreationWizard(DataExportDefinitionSetDialog.this.owner);
				       if(newDefinition != null){
				      	 isDirty = true;
				      	 ((DefaultListModel)(DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel())).addElement(newDefinition.getName());
				      	 indexDataExportDefinitionIdMap.put((DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel().getSize()-1), newDefinition.getId());
				      	 episimDataExportDefinitionSet.addEpisimDataExportDefinition(newDefinition);
				      	 
				       }
					}	         
	         }});
			
			editButton = new JButton("Edit DED");

			editButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					boolean dataExportEdited = false;
					String editedDataExportName = "";
					EpisimDataExportDefinition dataExportToBeEdited = episimDataExportDefinitionSet.getEpisimDataExportDefinition(indexDataExportDefinitionIdMap.get(dataExportDefinitionsList.getSelectedIndex()));
					if(dataExportToBeEdited != null){	
						EpisimDataExportDefinition editedExportDefinition = DataExportController.getInstance().showDataExportCreationWizard(DataExportDefinitionSetDialog.this.owner, dataExportToBeEdited);
						if(editedExportDefinition != null){ 
							episimDataExportDefinitionSet.updateDataExportDefinition(editedExportDefinition);
							editedDataExportName = editedExportDefinition.getName();
							dataExportEdited=true;
							
						}
					}
					EpisimDiffFieldDataExport diffFieldDataExportToBeEdited = episimDataExportDefinitionSet.getEpisimDiffFieldDataExportDefinition(indexDataExportDefinitionIdMap.get(dataExportDefinitionsList.getSelectedIndex()));
					if(diffFieldDataExportToBeEdited != null){	
						EpisimDiffFieldDataExport editedExportDefinition = DataExportController.getInstance().showDiffFieldDataExportCreationWizard(DataExportDefinitionSetDialog.this.owner, diffFieldDataExportToBeEdited);
						if(editedExportDefinition != null){ 
							episimDataExportDefinitionSet.updateDataExportDefinition(editedExportDefinition);
							editedDataExportName = editedExportDefinition.getName();
							dataExportEdited=true;
							
						}
					}
					if(dataExportEdited){
						int index = dataExportDefinitionsList.getSelectedIndex();
						if(index > -1){
						 ((DefaultListModel)(DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel())).remove(index);
						 ((DefaultListModel)(DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel())).insertElementAt(editedDataExportName, index); 
						 editButton.setEnabled(false);
						 removeButton.setEnabled(false);
						}
					}
	         }});
			
			editButton.setEnabled(false);
			removeButton = new JButton("Remove DED");
			removeButton.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					  isDirty = true;	
		           episimDataExportDefinitionSet.removeEpisimDataExportDefinition(indexDataExportDefinitionIdMap.get(dataExportDefinitionsList.getSelectedIndex()));
		           episimDataExportDefinitionSet.removeEpisimDiffFieldDataExportDefinition(indexDataExportDefinitionIdMap.get(dataExportDefinitionsList.getSelectedIndex()));
		           ((DefaultListModel)(DataExportDefinitionSetDialog.this.dataExportDefinitionsList.getModel())).remove(dataExportDefinitionsList.getSelectedIndex());
		           updateIndexMap();
		           editButton.setEnabled(false);
						 removeButton.setEnabled(false);
		         
	         }});
			removeButton.setEnabled(false);
			
			buttonPanel.add(addButton);
			buttonPanel.add(editButton);
			buttonPanel.add(removeButton);
			
			return buttonPanel;
		}
		
		
		private void updateIndexMap(){
			
			int elementCount = ((DefaultListModel)(dataExportDefinitionsList.getModel())).getSize(); 
			for(int i = 0; i < elementCount; i++ ){
				if(!indexDataExportDefinitionIdMap.keySet().contains(i)){
					long chartId = indexDataExportDefinitionIdMap.get(i+1);
					indexDataExportDefinitionIdMap.remove(i+1);
					indexDataExportDefinitionIdMap.put(i, chartId);
				}
			}
		}
		
		 private JPanel buildPathPanel() {

				JPanel pPanel = new JPanel(new GridBagLayout());

				GridBagConstraints c = new GridBagConstraints();

				c.anchor = GridBagConstraints.WEST;
				c.fill = GridBagConstraints.NONE;
				c.weightx = 0;
				c.weighty = 0;
				c.insets = new Insets(10, 10, 10, 10);
				c.gridwidth = 1;
				
				pPanel.add(new JLabel("Path:"), c);
				
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
				

				JButton editPathButton = new JButton("Edit Path");
				
				editPathButton.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						isDirty = true;
						if(pathText.getText() != null && !pathText.getText().equals(""))episimDataExportDefinitionSet.setPath(showPathDialog(pathText.getText()));
						else episimDataExportDefinitionSet.setPath(showPathDialog(""));
		          if(episimDataExportDefinitionSet.getPath() != null) pathText.setText(episimDataExportDefinitionSet.getPath().getAbsolutePath());
		            
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
			c.gridwidth = 1;

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					okButtonPressed = false;
					dialog.setVisible(false);
					dialog.dispose();
				}
			});
			bPanel.add(cancelButton, c);
			

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
					if(((DefaultListModel)(dataExportDefinitionsList.getModel())).getSize() > 0 && episimDataExportDefinitionSet.getPath() != null){
						okButtonPressed = true;
						dialog.setVisible(false);
						dialog.dispose();
						if(checkForDirtyDataExports() || isDirty){
								resetDirtyDataExports();
								EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback(){
									
									public void executeTask() {							
										try{
		                           DataExportController.getInstance().storeDataExportDefinitionSet(episimDataExportDefinitionSet);
	                           }
	                           catch (CompilationFailedException e){
		                           ExceptionDisplayer.getInstance().displayException(e);
	                           }
				               }
									public void taskHasFinished(){
										  
									}
							
								};	
								EpisimProgressWindow.showProgressWindowForTask(owner, "Writing EPISIM-DataExportSet-Archive", cb);			
						}
					}
				}
			});
			bPanel.add(okButton, c);

			return bPanel;

		}
	   private File showPathDialog(String path){
	   	
	   	if(path!= null && !path.equals("")) edeChooser.setSelectedFile(new File(path));
	   	
	   	edeChooser.setDialogTitle("Choose EPISIM-Chartset-Path");
			if(edeChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) return edeChooser.getSelectedFile();
			return path != null && !path.equals("") ? new File(path) : null;
		}
	   
	   private boolean checkForDirtyDataExports(){
	   	for(EpisimDataExportDefinition actDef: this.episimDataExportDefinitionSet.getEpisimDataExportDefinitions()) {
	   		if(actDef.isDirty()) return true;
	   	}
	   	for(EpisimDiffFieldDataExport actDef: this.episimDataExportDefinitionSet.getEpisimDiffFieldDataExportDefinitions()) {
	   		if(actDef.isDirty()) return true;
	   	}
	   	return isDirty;
	   }
	   
	   private void resetDirtyDataExports(){
	   	for(EpisimDataExportDefinition actDef: this.episimDataExportDefinitionSet.getEpisimDataExportDefinitions()) {
	   		actDef.setIsDirty(false);
	   	}
	   	for(EpisimDiffFieldDataExport actDef: this.episimDataExportDefinitionSet.getEpisimDiffFieldDataExportDefinitions()) {
	   		actDef.setIsDirty(false);
	   	}
	   	isDirty = false;
	   }

	}