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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sim.app.episim.charts.parser.ParseException;
import sim.app.episim.charts.parser.TokenMgrError;
import sim.app.episim.util.Names;



public class ChartSetDialog extends JDialog {
	
	
	private EpisimChartSet episimChartSet;
	
	private JTextArea chartExpressionTextArea;
	
	
	
	private JPanel buttonPanel;
	
	private JList chartsList;
	
	private JTextField chartSetName;
	
	
	private JDialog dialog;
	
	private JButton editButton;
	private JButton removeButton;
	private Frame owner;
	public ChartSetDialog(Frame owner, String title, boolean modal){
		super(owner, title, modal);
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
	
	public void showChartSet(EpisimChartSet chartSet){
		if(chartSet == null) throw new IllegalArgumentException("ChartSet to display was null!");
		this.episimChartSet = chartSet;
		DefaultListModel listModel = new DefaultListModel();
		for(EpisimChart actChart : chartSet.getEpisimCharts()) listModel.addElement(actChart.getTitle());
		
		centerMe();
		this.setVisible(true);
	}
	
	
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(((int)(screenDim.getWidth() /2) - (this.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (this.getHeight()/2)));
	}
	
	private JPanel buildNamePanel(){
		JPanel namePanel = new JPanel(new GridBagLayout());
		 GridBagConstraints c = new GridBagConstraints();
		 c.anchor =GridBagConstraints.EAST;
		   c.fill = GridBagConstraints.NONE;
		   c.weightx = 0;
		   c.weighty =0;
		   c.insets = new Insets(10,10,10,10);
		   c.gridwidth = GridBagConstraints.RELATIVE;
		   namePanel.add(new JLabel("Chartset-Name: "), c);
		   
		   chartSetName = new JTextField("noname"); 
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
	       if(newChart != null) ((DefaultListModel)(ChartSetDialog.this.chartsList.getModel())).addElement(newChart.getTitle());
	         
         }});
		
		editButton = new JButton("Edit Chart");
		editButton.setEnabled(false);
		removeButton = new JButton("Remove Chart");
		removeButton.setEnabled(false);
		
		buttonPanel.add(addButton);
		buttonPanel.add(editButton);
		buttonPanel.add(removeButton);
		
		return buttonPanel;
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

				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}

}