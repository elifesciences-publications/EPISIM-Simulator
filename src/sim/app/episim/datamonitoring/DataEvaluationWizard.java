package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;

import sim.app.episim.datamonitoring.CalculationAlgorithmSelectionPanel.AlgorithmSelectionListener;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;
import sim.app.episim.datamonitoring.charts.ChartController;
import sim.app.episim.datamonitoring.charts.ChartCreationWizard;
import sim.app.episim.datamonitoring.parser.ParseException;
import sim.app.episim.datamonitoring.parser.TokenMgrError;
import sim.app.episim.util.Names;
import sim.app.episim.util.TissueCellDataFieldsInspector;
import sim.app.episim.util.TissueCellDataFieldsInspector.ParameterSelectionListener;


public class DataEvaluationWizard {
	
	public static final int CHARTBASELINEROLE = 1;
	public static final int CHARTSERIESROLE = 2;
	public static final int DATAEXPORTROLE = 3;
	
	private static final String SELECTIONCARD = "SelectionCard";
	private static final String EXPRESSIONCARD = "ExpressionCard";
	
	private JPanel buttonPanel;
	private JPanel wizardPanel;
	private int role;
	
	private JDialog dialog;
	
	private JButton nextBackButton;
	private JButton okButton;
	private String visibleWizardCard = "";
	
	private CalculationAlgorithmSelectionPanel algorithmSelectionPanel = null;
	private ExpressionEditorPanel actualExpressionEditorPanel = null;
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	
	private CalculationAlgorithmDescriptor actualDescriptor = null;
	private CalculationAlgorithmConfigurator actualConfigurator = null;
	
	private Dimension[] dialogSizes = new Dimension[]{new Dimension(750,500), new Dimension(750, 800)};
	
	
	//index 0: expression not compiled; index 1: expression compiled
	
	
	
	
	public DataEvaluationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector, int role){
		this(owner, title, modal, _dataFieldsInspector, role, false);
	}
	
	
	public DataEvaluationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector, int role, boolean _booleanCondition){
		dialog = new JDialog(owner, title, modal);
		this.role = role;
	   
	   if(_dataFieldsInspector == null) throw new IllegalArgumentException("Datafield Inspector is null!");
	   dataFieldsInspector = _dataFieldsInspector;
	   buildWizardPanel();	   
	   
	   dialog.getContentPane().setLayout(new GridBagLayout());
	   GridBagConstraints c = new GridBagConstraints();
	   	   
	   c.anchor =GridBagConstraints.CENTER;
	   c.fill = GridBagConstraints.BOTH;
	   c.weightx = 1;
	   c.weighty =1;
	   c.insets = new Insets(10,10,10,10);
	   c.gridwidth = GridBagConstraints.REMAINDER;
	   dialog.getContentPane().add(wizardPanel, c);
	  		   
	   
	   c.anchor =GridBagConstraints.WEST;
	   c.gridwidth=GridBagConstraints.REMAINDER; 
	   c.fill = GridBagConstraints.HORIZONTAL;
	   c.weightx = 1;
	   c.weighty =0;
	   c.insets = new Insets(10,10,10,10);
	   this.buttonPanel = buildButtonPanel();
	   dialog.getContentPane().add(buttonPanel, c);
	   
	   dialog.setSize(dialogSizes[0]);
	   dialog.validate();
		
	}
	
	private void buildWizardPanel(){
		 wizardPanel = new JPanel(new CardLayout());
		 if(this.role == CHARTBASELINEROLE){
			 algorithmSelectionPanel = new CalculationAlgorithmSelectionPanel(CalculationAlgorithmSelectionPanel.ONLYONEDIMALGORITHMS);
		 }
		 else algorithmSelectionPanel = new CalculationAlgorithmSelectionPanel(CalculationAlgorithmSelectionPanel.ALLALGORITHMS);
		 algorithmSelectionPanel.addAlgorithmSelectionListener(new AlgorithmSelectionListener(){
				public void algorithmWasSelected() { nextBackButton.setEnabled(true); }
				public void noAlgorithmIsSelected() { nextBackButton.setEnabled(false); }
			});		 
		 visibleWizardCard = SELECTIONCARD;
		 wizardPanel.add(algorithmSelectionPanel.getCalculationAlgorithmSelectionPanel(), SELECTIONCARD);	
		 
	}
	
	
	
	public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator oldConfigurator){
			this.actualConfigurator = null;		
			if(oldConfigurator != null) restoreCalculationAlgorithmValues(oldConfigurator);
									
			dialog.repaint();
			centerMe();
			dialog.setVisible(true);
			
		return this.actualConfigurator;
	}
	
	private void restoreCalculationAlgorithmValues(CalculationAlgorithmConfigurator configurator){
		this.actualDescriptor = CalculationAlgorithmServer.getInstance().getCalculationAlgorithmDescriptor(configurator.getCalculationAlgorithmID());
		this.actualConfigurator = configurator;
		if(this.algorithmSelectionPanel != null){
			this.algorithmSelectionPanel.setSelectedAlgorithm(configurator.getCalculationAlgorithmID());
		}
		if(this.actualExpressionEditorPanel != null){
			this.actualExpressionEditorPanel.setExpressionEditorPanelData(configurator);
		}
	}
	
	private void centerMe(){
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation(((int)(screenDim.getWidth() /2) - (dialog.getWidth()/2)), 
		((int)(screenDim.getHeight() /2) - (dialog.getHeight()/2)));
	}	
	
	
	
	
	
	
   
   
   
   
   
   private JPanel buildButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;

		okButton = new JButton("  OK  ");
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
			
			

			public void actionPerformed(ActionEvent e) {
				CalculationAlgorithmConfigurator configurator = null;
				if(actualExpressionEditorPanel != null){
					configurator = actualExpressionEditorPanel.getCalculationAlgorithmConfigurator();
				}
				if(configurator != null){
					actualConfigurator = configurator;
					dialog.setVisible(false);
					dialog.dispose();
				}

			}
		});
		bPanel.add(okButton, c);
		
		
		
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1;
		c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;

		nextBackButton = new JButton("Next>>");
		nextBackButton.setEnabled(false);
		nextBackButton.addActionListener(new ActionListener() {			
			
			public void actionPerformed(ActionEvent e) {
					
			   nextBackButtonPressed();
			}
			
		});
		bPanel.add(nextBackButton, c);
		
		
		

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

				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		bPanel.add(cancelButton, c);

		return bPanel;

	}

   
   private void nextBackButtonPressed(){
   	if(visibleWizardCard.equals(SELECTIONCARD) && algorithmSelectionPanel != null){
   		CalculationAlgorithmDescriptor newDescriptor = algorithmSelectionPanel.getCalculationAlgorithmDescriptor();
   		boolean isNewDescriptor = true;
   		if(actualDescriptor != null && newDescriptor != null){
   			isNewDescriptor = (actualDescriptor.getID()!= newDescriptor.getID());
   		}
			actualDescriptor = newDescriptor;
			
			if(actualDescriptor != null){
				
				CardLayout cl = (CardLayout) wizardPanel.getLayout();
				if((actualExpressionEditorPanel == null) || (isNewDescriptor)){
					if(actualExpressionEditorPanel != null) cl.removeLayoutComponent(actualExpressionEditorPanel.getExpressionEditorPanel());	
				
					actualExpressionEditorPanel = new ExpressionEditorPanel(dataFieldsInspector, actualDescriptor);
					wizardPanel.add(actualExpressionEditorPanel.getExpressionEditorPanel(), EXPRESSIONCARD);
					if(actualConfigurator != null && actualDescriptor!= null && actualDescriptor.getID() == actualConfigurator.getCalculationAlgorithmID()){
						actualExpressionEditorPanel.setExpressionEditorPanelData(actualConfigurator);
					}
					
				}
				else if(actualConfigurator != null && actualExpressionEditorPanel != null){
					actualExpressionEditorPanel.setExpressionEditorPanelData(actualConfigurator);
				}
				cl.show(wizardPanel, EXPRESSIONCARD);
				this.visibleWizardCard = EXPRESSIONCARD;
				this.nextBackButton.setText("<<Back");
				okButton.setEnabled(true);
				dialog.setSize(this.dialogSizes[1]);
				
			}			
   	}
   	else if(visibleWizardCard.equals(EXPRESSIONCARD)){
   		CardLayout cl = (CardLayout) wizardPanel.getLayout();
   		cl.show(wizardPanel, SELECTIONCARD);
			this.visibleWizardCard = SELECTIONCARD;
			this.nextBackButton.setText("Next>>");
			dialog.setSize(this.dialogSizes[0]);
			okButton.setEnabled(false);
   	}
   	dialog.repaint();
		centerMe();
   }   
}


