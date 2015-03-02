package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.EpisimDiffusionFieldConfigurationEx;
import sim.app.episim.SimStateServer;
import sim.app.episim.SimStateChangeListener;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D;
import sim.util.gui.LabelledList;
import sim.util.gui.LabelledListHack;
import sim.util.gui.NumberTextField;


public class DiffusionModelGlobalParametersPanel extends JPanel {
	
	private EpisimDiffusionFieldConfiguration[] diffFieldConfigs;
	
	private JComboBox<String> comboBoxDiffusionFields;
	
	private JPanel propertiesPanel;
	private CardLayout propertiesPanelCL;
	private boolean hasNewExtraCellularDiffusionFieldConfig = false;
	public DiffusionModelGlobalParametersPanel(){
		if(ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields() >0){
			diffFieldConfigs = ModelController.getInstance().getExtraCellularDiffusionController().getEpisimExtraCellularDiffusionFieldsConfigurations();
			hasNewExtraCellularDiffusionFieldConfig = diffFieldConfigs[0] != null && diffFieldConfigs[0] instanceof EpisimDiffusionFieldConfigurationEx;
			buildPanel();
		}
	}
	private void buildPanel(){
		this.setLayout(new BorderLayout());
		LabelledListHack optionList = new LabelledListHack();
		optionList.setInsets(new Insets(10, 10, 0, 10));
		String[] diffFieldNames = new String[diffFieldConfigs.length];
		for(int i = 0; i < diffFieldNames.length; i++) diffFieldNames[i] = diffFieldConfigs[i].getDiffusionFieldName();
		comboBoxDiffusionFields = new JComboBox<String>(diffFieldNames);
		comboBoxDiffusionFields.setSelectedIndex(0);
		comboBoxDiffusionFields.addActionListener(new ActionListener(){		
			public void actionPerformed(ActionEvent e) {
				String diffFieldName = (String)comboBoxDiffusionFields.getSelectedItem();
				propertiesPanelCL.show(propertiesPanel, diffFieldName);
			}
		});
		optionList.addLabelled("Diffusion Field", comboBoxDiffusionFields);
		this.add(optionList, BorderLayout.NORTH);
		propertiesPanelCL =new CardLayout();
		propertiesPanel = new JPanel(propertiesPanelCL);
		
		for(int i = 0; i< diffFieldNames.length; i++){
			JPanel fieldPropertiesPanel = new JPanel(new BorderLayout(5,5));
			fieldPropertiesPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
			if(hasNewExtraCellularDiffusionFieldConfig){
				fieldPropertiesPanel.add(new DiffusionFieldConfigurationPanel((EpisimDiffusionFieldConfigurationEx)diffFieldConfigs[i]), BorderLayout.NORTH);
			}
			ExtracellularDiffusionFieldBCConfig2D ecFieldConfig =ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularFieldBCConfiguration(diffFieldNames[i]);
			if(ecFieldConfig != null){
				fieldPropertiesPanel.add(new DiffusionBoundaryConditionsPanel(ecFieldConfig), BorderLayout.SOUTH);
			}
			propertiesPanel.add(fieldPropertiesPanel, diffFieldNames[i]);			
		}
		
		this.add(propertiesPanel, BorderLayout.CENTER);		
		propertiesPanelCL.show(propertiesPanel, diffFieldNames[0]);
		SimStateServer.getInstance().addSimulationStateChangeListener(new SimStateChangeListener() {		
			
			public void simulationWasStopped(){	
				comboBoxDiffusionFields.setEnabled(true);
			}			
			
			public void simulationWasStarted(){				
				comboBoxDiffusionFields.setEnabled(false);			
			}
			
			public void simulationWasPaused() {			
				simulationWasStarted();				
			}
		});
		
		
		
	}
	
	

}
