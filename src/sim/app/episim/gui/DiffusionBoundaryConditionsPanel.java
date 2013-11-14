package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.SimStateServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig2D.BoundaryCondition;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig3D;
import sim.util.gui.LabelledListHack;
import sim.util.gui.NumberTextField;


public class DiffusionBoundaryConditionsPanel extends JPanel {
			
	private JComboBox<BoundaryCondition> comboBoxBoundaryX;
	private JLabel constantValueLabelX;
	private JLabel constantFlowLabelX;
	private NumberTextField  constantValueNumberFieldX;
	private NumberTextField  constantFlowNumberFieldX;
	
	JComboBox<BoundaryCondition> comboBoxBoundaryY;
	private JLabel constantValueLabelY;
	private JLabel constantFlowLabelY;
	private NumberTextField  constantValueNumberFieldY;
	private NumberTextField  constantFlowNumberFieldY;
	
	JComboBox<BoundaryCondition> comboBoxBoundaryZ;
	private JLabel constantValueLabelZ;
	private JLabel constantFlowLabelZ;
	private NumberTextField  constantValueNumberFieldZ;
	private NumberTextField  constantFlowNumberFieldZ;
	
	private ExtracellularDiffusionFieldBCConfig2D fieldBCConfig;
	
	
	public DiffusionBoundaryConditionsPanel(ExtracellularDiffusionFieldBCConfig2D fieldBCConfig){		
		this.fieldBCConfig = fieldBCConfig;		
		buildPanel();	
	}
	
	public DiffusionBoundaryConditionsPanel(ExtracellularDiffusionFieldBCConfig3D fieldBCConfig){
		this.fieldBCConfig = fieldBCConfig;	
		buildPanel();		
	}
	
	
	private void buildPanel(){
		this.setLayout(new BorderLayout());
		LabelledListHack optionList = new LabelledListHack();
		optionList.setInsets(new Insets(5, 10, 0, 10));
		comboBoxBoundaryX = new JComboBox<BoundaryCondition>(BoundaryCondition.values());
		comboBoxBoundaryX.setSelectedIndex(0);
		comboBoxBoundaryX.addActionListener(new ActionListener(){		
			public void actionPerformed(ActionEvent e) {
				BoundaryCondition selectedBoundaryConditon = (BoundaryCondition)comboBoxBoundaryX.getSelectedItem();
				if(selectedBoundaryConditon == BoundaryCondition.DIRICHLET){
					constantValueLabelX.setVisible(true);
					constantFlowLabelX.setVisible(false);
					
					constantValueNumberFieldX.setValue(fieldBCConfig.getConstantValueX());
					
					constantValueNumberFieldX.setVisible(true);
					constantFlowNumberFieldX.setVisible(false);
					
				}
				else if(selectedBoundaryConditon == BoundaryCondition.NEUMANN){
					constantValueLabelX.setVisible(false);
					constantFlowLabelX.setVisible(true);
					
					constantFlowNumberFieldX.setValue(fieldBCConfig.getConstantFlowX());					
					constantValueNumberFieldX.setVisible(false);
					constantFlowNumberFieldX.setVisible(true);
				}
				else if(selectedBoundaryConditon == BoundaryCondition.PERIODIC){
					constantValueLabelX.setVisible(false);
					constantFlowLabelX.setVisible(false);
					constantValueNumberFieldX.setVisible(false);
					constantFlowNumberFieldX.setVisible(false);
				}
				fieldBCConfig.setBoundaryConditionX(selectedBoundaryConditon);
			}
		});
		optionList.addLabelled("Boundary Condition X-Axis", comboBoxBoundaryX);
		constantValueLabelX = new JLabel("Constant Value X-Axis:");
		constantValueNumberFieldX = new NumberTextField(fieldBCConfig.getConstantValueX()){
			public double newValue(final double val){
				if(val>=0){
					fieldBCConfig.setConstantValueX(val);
					return val;
				}
				return fieldBCConfig.getConstantValueX();
			}
		};
		
		
		
		constantFlowLabelX = new JLabel("Constant Flow X-Axis:");
		constantFlowLabelX.setVisible(false);
		constantFlowNumberFieldX = new NumberTextField(fieldBCConfig.getConstantFlowX()){
			public double newValue(final double val){
				fieldBCConfig.setConstantFlowX(val);
				return val;
			}
		};
		constantFlowNumberFieldX.setVisible(false);
		optionList.add(constantValueLabelX, constantValueNumberFieldX);
		optionList.add(constantFlowLabelX, constantFlowNumberFieldX);
		
		
		
		comboBoxBoundaryY = new JComboBox<BoundaryCondition>(BoundaryCondition.values());
		comboBoxBoundaryY.setSelectedIndex(0);
		comboBoxBoundaryY.addActionListener(new ActionListener(){		
			public void actionPerformed(ActionEvent e) {
				BoundaryCondition selectedBoundaryConditon = (BoundaryCondition)comboBoxBoundaryY.getSelectedItem();
				if(selectedBoundaryConditon == BoundaryCondition.DIRICHLET){
					constantValueLabelY.setVisible(true);
					constantFlowLabelY.setVisible(false);
					
					constantValueNumberFieldY.setValue(fieldBCConfig.getConstantValueY());
					
					constantValueNumberFieldY.setVisible(true);
					constantFlowNumberFieldY.setVisible(false);
					
				}
				else if(selectedBoundaryConditon == BoundaryCondition.NEUMANN){
					constantValueLabelY.setVisible(false);
					constantFlowLabelY.setVisible(true);
					
					constantFlowNumberFieldY.setValue(fieldBCConfig.getConstantFlowY());					
					constantValueNumberFieldY.setVisible(false);
					constantFlowNumberFieldY.setVisible(true);
				}
				else if(selectedBoundaryConditon == BoundaryCondition.PERIODIC){
					constantValueLabelY.setVisible(false);
					constantFlowLabelY.setVisible(false);
					constantValueNumberFieldY.setVisible(false);
					constantFlowNumberFieldY.setVisible(false);
				}
				fieldBCConfig.setBoundaryConditionY(selectedBoundaryConditon);
			}
		});
		optionList.addLabelled("Boundary Condition Y-Axis", comboBoxBoundaryY);
		constantValueLabelY = new JLabel("Constant Value Y-Axis:");
		constantValueNumberFieldY = new NumberTextField(fieldBCConfig.getConstantValueY()){
			public double newValue(final double val){
				if(val>=0){
					fieldBCConfig.setConstantValueY(val);
					return val;
				}
				return fieldBCConfig.getConstantValueY();
			}
		};
		
		constantFlowLabelY = new JLabel("Constant Flow Y-Axis:");
		constantFlowLabelY.setVisible(false);
		constantFlowNumberFieldY = new NumberTextField(fieldBCConfig.getConstantFlowY()){
			public double newValue(final double val){
				fieldBCConfig.setConstantFlowY(val);
				return val;
			}
		};
		
		constantFlowNumberFieldY.setVisible(false);
		optionList.add(constantValueLabelY, constantValueNumberFieldY);
		optionList.add(constantFlowLabelY, constantFlowNumberFieldY);
		
		
		 
		if(fieldBCConfig instanceof ExtracellularDiffusionFieldBCConfig3D){
			comboBoxBoundaryZ = new JComboBox<BoundaryCondition>(BoundaryCondition.values());
			comboBoxBoundaryZ.setSelectedIndex(0);
			comboBoxBoundaryZ.addActionListener(new ActionListener(){		
				public void actionPerformed(ActionEvent e) {
					BoundaryCondition selectedBoundaryConditon = (BoundaryCondition)comboBoxBoundaryZ.getSelectedItem();
					if(selectedBoundaryConditon == BoundaryCondition.DIRICHLET){
						constantValueLabelZ.setVisible(true);
						constantFlowLabelZ.setVisible(false);
						
						constantValueNumberFieldZ.setValue(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantValueZ());
						
						constantValueNumberFieldZ.setVisible(true);
						constantFlowNumberFieldZ.setVisible(false);
						
					}
					else if(selectedBoundaryConditon == BoundaryCondition.NEUMANN){
						constantValueLabelZ.setVisible(false);
						constantFlowLabelZ.setVisible(true);
						
						constantFlowNumberFieldZ.setValue(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantFlowZ());					
						constantValueNumberFieldZ.setVisible(false);
						constantFlowNumberFieldZ.setVisible(true);
					}
					else if(selectedBoundaryConditon == BoundaryCondition.PERIODIC){
						constantValueLabelZ.setVisible(false);
						constantFlowLabelZ.setVisible(false);
						constantValueNumberFieldZ.setVisible(false);
						constantFlowNumberFieldZ.setVisible(false);
					}
					((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).setBoundaryConditionZ(selectedBoundaryConditon);
				}
			});
			optionList.addLabelled("Boundary Condition Z-Axis", comboBoxBoundaryZ);
			constantValueLabelZ = new JLabel("Constant Value Z-Axis:");
			constantValueNumberFieldZ = new NumberTextField(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantValueZ()){
				public double newValue(final double val){
					if(val>=0){
						((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).setConstantValueZ(val);
						return val;
					}
					return ((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantValueZ();				
				}
			};
			constantFlowLabelZ = new JLabel("Constant Flow Z-Axis:");
			constantFlowLabelZ.setVisible(false);
			constantFlowNumberFieldZ = new NumberTextField(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantFlowZ()){
				public double newValue(final double val){
					((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).setConstantFlowZ(val);
					return val;
				}
			};
			constantFlowNumberFieldZ.setVisible(false);
			optionList.add(constantValueLabelZ, constantValueNumberFieldZ);
			optionList.add(constantFlowLabelZ, constantFlowNumberFieldZ);
		}
		
		SimStateServer.getInstance().addSimulationStateChangeListener(new SimulationStateChangeListener() {
			
			
			public void simulationWasStopped() {		
				constantValueNumberFieldX.setEnabled(true);
				constantValueNumberFieldX.setValue(fieldBCConfig.getConstantValueX());
				constantFlowNumberFieldX.setEnabled(true);
				constantFlowNumberFieldX.setValue(fieldBCConfig.getConstantFlowX());
				comboBoxBoundaryX.setEnabled(true);
				comboBoxBoundaryX.setSelectedItem(fieldBCConfig.getBoundaryConditionX());
				
				constantValueNumberFieldY.setEnabled(true);
				constantValueNumberFieldY.setValue(fieldBCConfig.getConstantValueY());
				constantFlowNumberFieldY.setEnabled(true);
				constantFlowNumberFieldY.setValue(fieldBCConfig.getConstantFlowY());
				comboBoxBoundaryY.setEnabled(true);
				comboBoxBoundaryY.setSelectedItem(fieldBCConfig.getBoundaryConditionY());
				
				if(constantValueNumberFieldZ != null){
					constantValueNumberFieldZ.setEnabled(true);
					constantValueNumberFieldZ.setValue(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantValueZ());
				}
				if(constantFlowNumberFieldZ != null){
					constantFlowNumberFieldZ.setEnabled(true);
					constantFlowNumberFieldZ.setValue(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantFlowZ());
				}
				if(comboBoxBoundaryZ != null){
					comboBoxBoundaryZ.setEnabled(true);
					comboBoxBoundaryZ.setSelectedItem(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getBoundaryConditionZ());
				}
			}			
			
			public void simulationWasStarted(){
				constantValueNumberFieldX.setEnabled(false);
				constantValueNumberFieldX.setValue(fieldBCConfig.getConstantValueX());
				constantFlowNumberFieldX.setEnabled(false);
				constantFlowNumberFieldX.setValue(fieldBCConfig.getConstantFlowX());
				comboBoxBoundaryX.setEnabled(false);
				comboBoxBoundaryX.setSelectedItem(fieldBCConfig.getBoundaryConditionX());
				
				constantValueNumberFieldY.setEnabled(false);
				constantValueNumberFieldY.setValue(fieldBCConfig.getConstantValueY());
				constantFlowNumberFieldY.setEnabled(false);
				constantFlowNumberFieldY.setValue(fieldBCConfig.getConstantFlowY());
				comboBoxBoundaryY.setEnabled(false);				
				comboBoxBoundaryY.setSelectedItem(fieldBCConfig.getBoundaryConditionY());
				
				if(constantValueNumberFieldZ != null){
					constantValueNumberFieldZ.setEnabled(false);
					constantValueNumberFieldZ.setValue(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantValueZ());
				}
				if(constantFlowNumberFieldZ != null){ 
					constantFlowNumberFieldZ.setEnabled(false);
					constantFlowNumberFieldZ.setValue(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getConstantFlowZ());
				}
				if(comboBoxBoundaryZ != null){
					comboBoxBoundaryZ.setEnabled(false);
					comboBoxBoundaryZ.setSelectedItem(((ExtracellularDiffusionFieldBCConfig3D)fieldBCConfig).getBoundaryConditionZ());
				}				
			}
			
			
			public void simulationWasPaused() {
			
				simulationWasStarted();
				
			}
		});
		
		this.add(optionList, BorderLayout.CENTER);
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Boundary Conditions"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	}
	
	

}
