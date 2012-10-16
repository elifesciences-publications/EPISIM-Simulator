package sim.app.episim.datamonitoring;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;

import javax.swing.JButton;

import javax.swing.JDialog;

import javax.swing.JPanel;


import episiminterfaces.calc.CalculationAlgorithmConfigurator;
import episiminterfaces.calc.CalculationAlgorithmDescriptor;
import episiminterfaces.calc.CalculationAlgorithm.CalculationAlgorithmType;

import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.datamonitoring.CalculationAlgorithmSelectionPanel.AlgorithmSelectionListener;
import sim.app.episim.datamonitoring.ExpressionEditorPanel.ExpressionState;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmConfiguratorFactory;
import sim.app.episim.datamonitoring.calc.CalculationAlgorithmServer;

import sim.app.episim.datamonitoring.parser.ParseException;

import sim.app.episim.util.ObjectManipulations;
import sim.app.episim.util.TissueCellDataFieldsInspector;


import java.util.HashMap;

public class DataEvaluationWizard {
	
	
	private static final String SELECTIONCARD = "SelectionCard";
	private static final String EXPRESSIONCARD = "ExpressionCard";
	
	private JPanel buttonPanel;
	private JPanel wizardPanel;
	
	
	private JDialog dialog;
	
	private JButton nextBackButton;
	private JButton okButton;
	private String visibleWizardCard = "";
	
	private CalculationAlgorithmSelectionPanel algorithmSelectionPanel = null;
	private ExpressionEditorPanel actualExpressionEditorPanel = null;
	private TissueCellDataFieldsInspector dataFieldsInspector;
	
	
	private CalculationAlgorithmDescriptor actualDescriptor = null;
	private CalculationAlgorithmConfigurator actualConfigurator = null;
	private CalculationAlgorithmConfigurator oldConfigurator = null;
	
	private Dimension[] dialogSizes = new Dimension[]{new Dimension(750,500), new Dimension(750, 800)};
	
	
	//index 0: expression not compiled; index 1: expression compiled
	
	private Set<CalculationAlgorithmType> allowedTypes;
	
	
	public DataEvaluationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector){
		this(owner, title, modal, _dataFieldsInspector, null);
	}
	
	
	public DataEvaluationWizard(Frame owner, String title, boolean modal, TissueCellDataFieldsInspector _dataFieldsInspector, Set<CalculationAlgorithmType> _allowedTypes){
		dialog = new JDialog(owner, title, modal);
		
		if(_allowedTypes == null){
			this.allowedTypes = new HashSet<CalculationAlgorithmType>();
			for(CalculationAlgorithmType type: CalculationAlgorithmType.values()) allowedTypes.add(type);
		}
		else this.allowedTypes = _allowedTypes;
	   
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
	   c.insets = new Insets(5,10,10,10);
	   this.buttonPanel = buildButtonPanel();
	   dialog.getContentPane().add(buttonPanel, c);
	   
	   dialog.setSize(dialogSizes[0]);
	   dialog.validate();
		
	}
	
	private void buildWizardPanel(){
		 wizardPanel = new JPanel(new CardLayout());
		 
		 algorithmSelectionPanel = new CalculationAlgorithmSelectionPanel(allowedTypes);
		 
		 algorithmSelectionPanel.addAlgorithmSelectionListener(new AlgorithmSelectionListener(){
				public void algorithmWasSelected() {
					if(!algorithmSelectionPanel.getCalculationAlgorithmDescriptor().hasCondition()
							&& !algorithmSelectionPanel.getCalculationAlgorithmDescriptor().hasMathematicalExpression()
							&& algorithmSelectionPanel.getCalculationAlgorithmDescriptor().getParameters().isEmpty()){
						okButton.setEnabled(true);
						nextBackButton.setEnabled(false);
					}
					else{ 
						nextBackButton.setEnabled(true);
						okButton.setEnabled(false);
					}
				}
				public void noAlgorithmIsSelected() { 
					nextBackButton.setEnabled(false); 
					okButton.setEnabled(false); 
				}
			});		 
		 visibleWizardCard = SELECTIONCARD;
		 wizardPanel.add(algorithmSelectionPanel.getCalculationAlgorithmSelectionPanel(), SELECTIONCARD);	
		 
	}
	
	
	
	public CalculationAlgorithmConfigurator getCalculationAlgorithmConfigurator(CalculationAlgorithmConfigurator oldConfigurator){
			this.actualConfigurator = null;		
			if(oldConfigurator != null){ 
				this.oldConfigurator = ObjectManipulations.cloneObject(oldConfigurator);
				restoreCalculationAlgorithmValues(oldConfigurator);
			}
									
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
   
   
   
   private JPanel buildButtonPanel() {

		JPanel bPanel = new JPanel(new GridBagLayout());
		JPanel bInnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		GridBagConstraints c = new GridBagConstraints();

		nextBackButton = new JButton("Next>>");		
		nextBackButton.setEnabled(false);
		nextBackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {					
			   nextBackButtonPressed();
			}			
		});
		
		JPanel nextBackBPanel = new JPanel(new BorderLayout());
		nextBackBPanel.add(nextBackButton, BorderLayout.EAST);
		nextBackBPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		bInnerPanel.add(nextBackBPanel);		

		okButton = new JButton("  OK  ");
		okButton.setEnabled(false);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CalculationAlgorithmConfigurator configurator = null;
				
					if(!algorithmSelectionPanel.getCalculationAlgorithmDescriptor().hasCondition()
							&& !algorithmSelectionPanel.getCalculationAlgorithmDescriptor().hasMathematicalExpression()
							&& algorithmSelectionPanel.getCalculationAlgorithmDescriptor().getParameters().isEmpty()){
						configurator = CalculationAlgorithmConfiguratorFactory.createCalculationAlgorithmConfiguratorObject(algorithmSelectionPanel.getCalculationAlgorithmDescriptor().getID(), 
								new String[]{null, null}, new String[]{null, null}, false, new HashMap<String, Object>());
					}
					else if(actualExpressionEditorPanel != null) configurator = actualExpressionEditorPanel.getCalculationAlgorithmConfigurator();
				if(configurator != null 
						&& (actualExpressionEditorPanel == null || actualExpressionEditorPanel.getMathematicalConditionState() == ExpressionState.OK)
						&& (actualExpressionEditorPanel == null || actualExpressionEditorPanel.getBooleanConditionState() == ExpressionState.OK)){
					actualConfigurator = configurator;
					dialog.setVisible(false);
					dialog.dispose();
				}
				else configurator = null;
			}
		});
		bInnerPanel.add(okButton);

		

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actualConfigurator = oldConfigurator;
				
				if(actualConfigurator != null){
					int sessionID =  ExpressionCheckerController.getInstance().getCheckSessionId();
					try{
						if(actualDescriptor.hasMathematicalExpression()) ExpressionCheckerController.getInstance().checkArithmeticDataMonitoringExpression(sessionID, actualConfigurator.getArithmeticExpression()[0], dataFieldsInspector);
               
	               if(actualDescriptor.hasCondition()) ExpressionCheckerController.getInstance().checkBooleanDataMonitoringExpression(sessionID, actualConfigurator.getBooleanExpression()[0], dataFieldsInspector);
                 }
                  catch (NumberFormatException e1){
                  	ExceptionDisplayer.getInstance().displayException(e1);
                  }
                  catch (ParseException e1){
                  	ExceptionDisplayer.getInstance().displayException(e1);
                  }
						
				}
				
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		
		bInnerPanel.add(cancelButton);
		
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		c.weightx = 0;
		c.weighty = 1;
		//c.insets = new Insets(10, 10, 10, 10);
		c.gridwidth = 1;
		c.gridwidth = 1;
		
		bPanel.add(bInnerPanel, c);

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
					dataFieldsInspector.resetRequiredClasses();
					actualExpressionEditorPanel = new ExpressionEditorPanel(dialog, dataFieldsInspector, actualDescriptor);
					wizardPanel.add(actualExpressionEditorPanel.getExpressionEditorPanel(), EXPRESSIONCARD);
					if(actualConfigurator != null && actualDescriptor!= null && actualDescriptor.getID() == actualConfigurator.getCalculationAlgorithmID()){
						actualExpressionEditorPanel.setExpressionEditorPanelData(actualConfigurator);
					}
					
				}
				else if(actualConfigurator != null && actualExpressionEditorPanel != null && actualDescriptor!= null && actualDescriptor.getID() == actualConfigurator.getCalculationAlgorithmID()){
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


