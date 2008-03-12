package sim.app.episim.charts;

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

import episiminterfaces.EpisimChart;
import episiminterfaces.EpisimChartSet;

import sim.app.episim.charts.io.ECSFileWriter;
import sim.app.episim.charts.parser.ParseException;
import sim.app.episim.charts.parser.TokenMgrError;
import sim.app.episim.gui.ExtendedFileChooser;
import sim.app.episim.util.Names;
import sim.app.episim.util.ObjectCloner;



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
	private JWindow progressWindow;
	private Frame owner;
	
	private boolean okButtonPressed = false;
	public ChartSetDialog(Frame owner, String title, boolean modal){
		super(owner, title, modal);
		
		progressWindow = new JWindow(owner);
		
		progressWindow.getContentPane().setLayout(new BorderLayout(5, 5));
		if(progressWindow.getContentPane() instanceof JPanel)
			((JPanel)progressWindow.getContentPane()).setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createBevelBorder(BevelBorder.RAISED), BorderFactory.createEmptyBorder(10,10, 10, 10)));
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		JLabel progressLabel = new JLabel("Writing Episim-Chartset-Archive");
		progressWindow.getContentPane().add(progressLabel, BorderLayout.NORTH);
		progressWindow.getContentPane().add(progressBar, BorderLayout.CENTER);
		
		progressWindow.setSize(400, 65);
		
		progressWindow.setLocation(owner.getLocation().x + (owner.getWidth()/2) - (progressWindow.getWidth()/2), 
				owner.getLocation().y + (owner.getHeight()/2) - (progressWindow.getHeight()/2));
		
		
		
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
	  chartsListScroll.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Episim-Charts"),
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
		this.episimChartSetOld = ObjectCloner.cloneObject(chartSet);
		this.chartSetName.setText(chartSet.getName());
		DefaultListModel listModel = new DefaultListModel();
		int i = 0;
		for(EpisimChart actChart : chartSet.getEpisimCharts()){
			indexChartIdMap.put(i, actChart.getId());
			listModel.addElement(actChart.getTitle());
			i++;
		}
		this.chartsList.setModel(listModel);
		if(chartSet.getPath() != null) this.pathText.setText(chartSet.getPath().getAbsolutePath());
		centerMe();
		this.setVisible(true);
		if(okButtonPressed) return this.episimChartSet;
		
		return this.episimChartSetOld;
	}
	
	public EpisimChartSet showNewChartSet(){
		
		EpisimChartSet newChartSet = showChartSet(new EpisimChartSetImpl());
		if(okButtonPressed) return newChartSet;
		
		return null;
	}
	
	
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
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

	       EpisimChart newChart = ChartController.getInstance().showChartCreationWizard(ChartSetDialog.this.owner);
	       if(newChart != null){ 
	      	 ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).addElement(newChart.getTitle());
	      	 indexChartIdMap.put((ChartSetDialog.this.chartsList.getModel().getSize()-1), newChart.getId());
	      	 episimChartSet.addEpisimChart(newChart);
	      	 
	       }
	         
         }});
		
		editButton = new JButton("Edit Chart");

		editButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				EpisimChart editedChart = ChartController.getInstance().showChartCreationWizard(ChartSetDialog.this.owner, 
	      		      episimChartSet.getEpisimChart(indexChartIdMap.get(chartsList.getSelectedIndex())));
				if(editedChart != null){ 
					episimChartSet.updateChart(editedChart);
					int index = chartsList.getSelectedIndex();
					if(index > -1){
					 ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).remove(index);
					 ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).insertElementAt(editedChart.getTitle(), index); 
					 editButton.setEnabled(false);
					 removeButton.setEnabled(false);
					}
				}
	         
         }});
		
		editButton.setEnabled(false);
		removeButton = new JButton("Remove Chart");
		removeButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {

	           episimChartSet.removeEpisimChart(indexChartIdMap.get(chartsList.getSelectedIndex()));
	           ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).remove(chartsList.getSelectedIndex());
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
		
		int elementCount = ((DefaultListModel)(chartsList.getModel())).getSize(); 
		for(int i = 0; i < elementCount; i++ ){
			if(!indexChartIdMap.keySet().contains(i)){
				long chartId = indexChartIdMap.get(i+1);
				indexChartIdMap.remove(i+1);
				indexChartIdMap.put(i, chartId);
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
				if(((DefaultListModel)(chartsList.getModel())).getSize() > 0 && episimChartSet.getPath() != null){
					okButtonPressed = true;
					Runnable r = new Runnable(){

						public void run() {
							progressWindow.setVisible(true);
							dialog.setVisible(false);
							dialog.dispose();
							ChartController.getInstance().storeEpisimChartSet(episimChartSet);
							
							progressWindow.setVisible(false);
                  }
				
					};
					Thread writingThread = new Thread(r);
					writingThread.start();
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
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				okButtonPressed = false;
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}
   private File showPathDialog(String path){
   	
   	if(path!= null && !path.equals("")) ecsChooser.setCurrentDirectory(new File(path));
   	
   	ecsChooser.setDialogTitle("Choose Episim-Chartset-Path");
		if(ecsChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) return ecsChooser.getSelectedFile();
		return null;
	}

}