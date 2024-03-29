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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
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

import episimexceptions.CompilationFailedException;
import episiminterfaces.monitoring.EpisimCellVisualizationChart;
import episiminterfaces.monitoring.EpisimChart;
import episiminterfaces.monitoring.EpisimChartSet;
import episiminterfaces.monitoring.EpisimDiffFieldChart;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.datamonitoring.charts.ChartController.ChartType;
import sim.app.episim.datamonitoring.charts.io.ECSFileWriter;
import sim.app.episim.datamonitoring.dataexport.DataExportController;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.gui.EpisimProgressWindow;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.gui.EpisimProgressWindow.EpisimProgressWindowCallback;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectManipulations;



public class ChartSetDialog extends JDialog {
	
	
	private EpisimChartSet episimChartSet;
	private EpisimChartSet episimChartSetOld;
	
	private JTextArea chartExpressionTextArea;
	
	
	
	private JPanel buttonPanel;
	
	private JList chartsList;
	
	private JTextField chartSetName;
	private JTextField pathText;
	
	private Map<Integer, Long> indexChartIdMap;
	private ExtendedFileChooser ecsChooser = new ExtendedFileChooser("ecs");
	
	private JDialog dialog;
	
	private JButton editButton;
	private JButton removeButton;
	JButton okButton;
	private EpisimProgressWindow progressWindow;
	private Frame owner;
	
	private boolean okButtonPressed = false;
	
	private boolean isDirty = false;
	
	public ChartSetDialog(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		
		
		
		indexChartIdMap = new HashMap<Integer, Long>();
		
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
	  
	   
	  chartsList = new JList();
	  chartsList.setModel(new DefaultListModel());
	  chartsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  chartsList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {

				if((e.getValueIsAdjusting() != false) && chartsList.getSelectedIndex() != -1){
					editButton.setEnabled(true);
					removeButton.setEnabled(true);
				}
				else if((e.getValueIsAdjusting() != false) && chartsList.getSelectedIndex() == -1){
					editButton.setEnabled(false);
					removeButton.setEnabled(false);
				}
			}

		});
	  
	  JScrollPane chartsListScroll = new JScrollPane(chartsList);
	  chartsListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("EPISIM-Charts"),
		      BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.RELATIVE;
	   getContentPane().add(chartsListScroll, c);
	   
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
	
	public EpisimChartSet showChartSet(EpisimChartSet chartSet){
		
		if(chartSet == null) throw new IllegalArgumentException("ChartSet to display was null!");
		okButtonPressed = false;
		indexChartIdMap = new HashMap<Integer, Long>();
		this.episimChartSet = chartSet;
		this.episimChartSetOld = chartSet.clone();
		this.chartSetName.setText(chartSet.getName());
		DefaultListModel listModel = new DefaultListModel();
		int i = 0;
		
		for(EpisimChart actChart : chartSet.getEpisimCharts()){
			indexChartIdMap.put(i, actChart.getId());
			listModel.addElement(actChart.getTitle());
			i++;
		}
		for(EpisimCellVisualizationChart actChart : chartSet.getEpisimCellVisualizationCharts()){
			indexChartIdMap.put(i, actChart.getId());
			listModel.addElement(actChart.getTitle());
			i++;
		}
		for(EpisimDiffFieldChart actChart : chartSet.getEpisimDiffFieldCharts()){
			indexChartIdMap.put(i, actChart.getId());
			listModel.addElement(actChart.getChartTitle());
			i++;
		}
		
		this.chartsList.setModel(listModel);
		if(chartSet.getPath() != null) this.pathText.setText(chartSet.getPath().getAbsolutePath());
		centerMe();
		isDirty = false;
		this.setVisible(true);
		if(okButtonPressed) return this.episimChartSet;
		
		return this.episimChartSetOld;
	}
	
	public EpisimChartSet showNewChartSet(){
		isDirty=true;
		EpisimChartSet newChartSet = showChartSet(new EpisimChartSetImpl());
		if(okButtonPressed) return newChartSet;
		
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
		   namePanel.add(new JLabel("Chartset-Name: "), c);
		   
		   chartSetName = new JTextField("noname"); 
		   chartSetName.addKeyListener(new KeyAdapter() {

				public void keyPressed(KeyEvent keyEvent) {
					isDirty = true;
					if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER){
						episimChartSet.setName(chartSetName.getText());
					}
					else if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
						chartSetName.setText(chartSetName.getText());
				}
			});
		   chartSetName.addFocusListener(new FocusAdapter() {

				public void focusLost(FocusEvent e) {

					episimChartSet.setName(chartSetName.getText());
				}
			});
		   c.anchor =GridBagConstraints.CENTER;
		   c.fill = GridBagConstraints.HORIZONTAL;
		   c.weightx = 1;
		   c.weighty =0;
		   c.insets = new Insets(10,10,10,10);
		   c.gridwidth = GridBagConstraints.REMAINDER;
		   namePanel.add(chartSetName, c);
		   
		   return namePanel;
	}
	
	private JPanel buildAddRemoveEditButtonPanel(){
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10 ,10 ));
		
		JButton addButton = new JButton("Add Chart");
		
		addButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Object result = null;
				if(ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfEpisimExtraCellularDiffusionFieldConfigurations() > 0){
						ChartType[] types = ChartType.values();
						result= JOptionPane.showInputDialog(ChartSetDialog.this, "Choose Chart-Type:", "Chart-Type", JOptionPane.QUESTION_MESSAGE,
							  																null, types, ChartType.REGULAR_2D_CHART);
				}
				else{
					ChartType[] types = new ChartType[]{ChartType.REGULAR_2D_CHART, ChartType.CELL_VISUALIZATION_CHART};
					result= JOptionPane.showInputDialog(ChartSetDialog.this, "Choose Chart-Type:", "Chart-Type", JOptionPane.QUESTION_MESSAGE,
								null, types, ChartType.REGULAR_2D_CHART);
				}
				String title =null;
				long id = Long.MAX_VALUE;
				if(result == ChartType.REGULAR_2D_CHART){
				    EpisimChart newChart = ChartController.getInstance().showChartCreationWizard(ChartSetDialog.this.owner);
				    if(newChart != null){
				    	 isDirty=true;
				    	 title = newChart.getTitle();
				     	 id = newChart.getId();
				     	 episimChartSet.addEpisimChart(newChart);
				      }
				 }
				 else if(result == ChartType.CELL_VISUALIZATION_CHART){
				    EpisimCellVisualizationChart newChart = ChartController.getInstance().showCellVisualizationChartCreationWizard(ChartSetDialog.this.owner);
				    if(newChart != null){
				    	 isDirty=true;
				    	 title = newChart.getTitle();
				     	 id = newChart.getId();
				     	 episimChartSet.addEpisimChart(newChart);
				      }
				 }
				 else if(result == ChartType.DIFF_FIELD_CHART){
					 EpisimDiffFieldChart newChart = ChartController.getInstance().showDiffFieldChartCreationWizard(ChartSetDialog.this.owner);
				    if(newChart != null){
				    	 isDirty=true;
				     	 title = newChart.getChartTitle();
				     	 id = newChart.getId();
				     	 episimChartSet.addEpisimChart(newChart);
				    }
				 }
				if(title!=null && id != Long.MAX_VALUE){
					 ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).addElement(title);
			     	 indexChartIdMap.put((ChartSetDialog.this.chartsList.getModel().getSize()-1), id);
				}
         }});
		
		editButton = new JButton("Edit Chart");

		editButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				boolean chartEdited = false;
				String editedChartTitle ="";
				EpisimChart chartToBeEdited =  episimChartSet.getEpisimChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
				
				if(chartToBeEdited != null){
					EpisimChart editedChart = ChartController.getInstance().showChartCreationWizard(ChartSetDialog.this.owner, chartToBeEdited);
					if(editedChart != null){ 
						episimChartSet.updateChart(editedChart);
						editedChartTitle = editedChart.getTitle();
						chartEdited= true;
					}
				}
				EpisimCellVisualizationChart cellVisualizationChartToBeEdited =  episimChartSet.getEpisimCellVisualizationChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
				
				if(cellVisualizationChartToBeEdited != null){
					EpisimCellVisualizationChart editedChart = ChartController.getInstance().showCellVisualizationChartCreationWizard(ChartSetDialog.this.owner, cellVisualizationChartToBeEdited);
					if(editedChart != null){ 
						episimChartSet.updateChart(editedChart);
						editedChartTitle = editedChart.getTitle();
						chartEdited= true;
					}
				}
				EpisimDiffFieldChart diffFieldChartToBeEdited =  episimChartSet.getEpisimDiffFieldChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
				if(diffFieldChartToBeEdited != null){
					EpisimDiffFieldChart editedChart = ChartController.getInstance().showDiffFieldChartCreationWizard(ChartSetDialog.this.owner, diffFieldChartToBeEdited);
					if(editedChart != null){ 
						episimChartSet.updateChart(editedChart);
						editedChartTitle = editedChart.getChartTitle();
						chartEdited= true;						
					}
				}
				if(chartEdited){
					int index = chartsList.getSelectedIndex();
					if(index > -1){
					 ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).remove(index);
					 ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).insertElementAt(editedChartTitle, index); 
					 editButton.setEnabled(false);
					 removeButton.setEnabled(false);
					}
				}	         
         }});
		
		editButton.setEnabled(false);
		removeButton = new JButton("Remove Chart");
		removeButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				  isDirty = true;
				  int removeIndex =chartsList.getSelectedIndex();
	           episimChartSet.removeEpisimChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
	           episimChartSet.removeEpisimCellVisualizationChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
	           episimChartSet.removeEpisimDiffFieldChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
	           ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).remove(chartsList.getSelectedIndex());
	           updateIndexMap(removeIndex);
	           
	           editButton.setEnabled(false);
					 removeButton.setEnabled(false);
	         
         }});
		removeButton.setEnabled(false);
		
		buttonPanel.add(addButton);
		buttonPanel.add(editButton);
		buttonPanel.add(removeButton);
		
		return buttonPanel;
	}
	
	
	private void updateIndexMap(int removeIndex){
		
		Set<Integer> keys = new HashSet<Integer>();
		keys.addAll(indexChartIdMap.keySet());
		for(int i :keys){
			if(i > removeIndex){
				long chartId = indexChartIdMap.get(i);
				indexChartIdMap.remove(i);
				indexChartIdMap.put(i-1, chartId);
			}
			if(i==removeIndex)indexChartIdMap.remove(i);
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
					if(pathText.getText() != null && !pathText.getText().equals(""))episimChartSet.setPath(showPathDialog(pathText.getText()));
					else episimChartSet.setPath(showPathDialog(""));
	          if(episimChartSet.getPath() != null) pathText.setText(episimChartSet.getPath().getAbsolutePath());
	            
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

		 okButton = new JButton("  OK  ");		
		 okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if(((DefaultListModel)(chartsList.getModel())).getSize() > 0 && episimChartSet.getPath() != null){
					okButtonPressed = true;
					dialog.setVisible(false);
					dialog.dispose();
					if(episimChartSet.isOneOfTheChartsDirty() || isDirty){
						resetChartDirtyStatus();
					//	EpisimProgressWindowCallback cb = new EpisimProgressWindowCallback(){
							
						//	public void executeTask() {							
								try{
									ChartController.getInstance().storeEpisimChartSet(episimChartSet);
                        }
                        catch (CompilationFailedException e1){
                           EpisimExceptionHandler.getInstance().displayException(e1);
                        }
		             //  }
						//	public void taskHasFinished(){
								  
						//	}
					
						//};
					//	EpisimProgressWindow.showProgressWindowForTask(owner, "Writing EPISIM-Chartset-Archive", cb);									
					}					
				}
			}
		});
		bPanel.add(okButton, c);

		

		return bPanel;

	}
   private File showPathDialog(String path){
   	
   	if(path!= null && !path.equals("")) ecsChooser.setSelectedFile(new File(path));
   	
   	ecsChooser.setDialogTitle("Choose EPISIM-Chartset-Path");
		if(ecsChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) return ecsChooser.getSelectedFile();
		return path != null && !path.equals("") ? new File(path) : null;
	}

   
  
   
   private void resetChartDirtyStatus(){
   	for(EpisimChart actChart: this.episimChartSet.getEpisimCharts()) {
   		actChart.setIsDirty(false);
   	}
   	for(EpisimDiffFieldChart actChart: this.episimChartSet.getEpisimDiffFieldCharts()) {
   		actChart.setIsDirty(false);
   	}
   	isDirty=false;
   }
}