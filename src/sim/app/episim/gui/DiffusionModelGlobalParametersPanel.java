package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sim.SimStateServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.DiffusionModelGlobalParameters;
import sim.app.episim.model.diffusion.DiffusionModelGlobalParameters.DiffusionModelGlobalParameters3D;
import sim.app.episim.model.diffusion.DiffusionModelGlobalParameters.BoundaryCondition;
import sim.util.gui.LabelledList;
import sim.util.gui.LabelledListHack;
import sim.util.gui.NumberTextField;


public class DiffusionModelGlobalParametersPanel extends JPanel {
	
	private DiffusionModelGlobalParameters globalParameters;
	
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
	
	public DiffusionModelGlobalParametersPanel(){
		if(ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields() >0){
			globalParameters = ModelController.getInstance().getExtraCellularDiffusionController().getDiffusionModelGlobalParameters();
			buildPanel();
		}
	}
	private void buildPanel(){
		this.setLayout(new BorderLayout());
		LabelledListHack optionList = new LabelledListHack();
		optionList.setInsets(new Insets(10, 10, 0, 10));
		comboBoxBoundaryX = new JComboBox<BoundaryCondition>(BoundaryCondition.values());
		comboBoxBoundaryX.setSelectedIndex(0);
		comboBoxBoundaryX.addActionListener(new ActionListener(){		
			public void actionPerformed(ActionEvent e) {
				BoundaryCondition selectedBoundaryConditon = (BoundaryCondition)comboBoxBoundaryX.getSelectedItem();
				if(selectedBoundaryConditon == BoundaryCondition.DIRICHLET){
					constantValueLabelX.setVisible(true);
					constantFlowLabelX.setVisible(false);
					
					constantValueNumberFieldX.setValue(globalParameters.getConstantValueX());
					
					constantValueNumberFieldX.setVisible(true);
					constantFlowNumberFieldX.setVisible(false);
					
				}
				else if(selectedBoundaryConditon == BoundaryCondition.NEUMANN){
					constantValueLabelX.setVisible(false);
					constantFlowLabelX.setVisible(true);
					
					constantFlowNumberFieldX.setValue(globalParameters.getConstantFlowX());					
					constantValueNumberFieldX.setVisible(false);
					constantFlowNumberFieldX.setVisible(true);
				}
				else if(selectedBoundaryConditon == BoundaryCondition.PERIODIC){
					constantValueLabelX.setVisible(false);
					constantFlowLabelX.setVisible(false);
					constantValueNumberFieldX.setVisible(false);
					constantFlowNumberFieldX.setVisible(false);
				}
				globalParameters.setBoundaryConditionX(selectedBoundaryConditon);
			}
		});
		optionList.addLabelled("Boundary Condition X-Axis", comboBoxBoundaryX);
		constantValueLabelX = new JLabel("Constant Value X-Axis:");
		constantValueNumberFieldX = new NumberTextField(globalParameters.getConstantValueX()){
			public double newValue(final double val){
				if(val>=0){
					globalParameters.setConstantValueX(val);
					return val;
				}
				return getValue();
			}
		};
		
		
		
		constantFlowLabelX = new JLabel("Constant Flow X-Axis:");
		constantFlowLabelX.setVisible(false);
		constantFlowNumberFieldX = new NumberTextField(globalParameters.getConstantFlowX()){
			public double newValue(final double val){
				globalParameters.setConstantFlowX(val);
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
					
					constantValueNumberFieldY.setValue(globalParameters.getConstantValueY());
					
					constantValueNumberFieldY.setVisible(true);
					constantFlowNumberFieldY.setVisible(false);
					
				}
				else if(selectedBoundaryConditon == BoundaryCondition.NEUMANN){
					constantValueLabelY.setVisible(false);
					constantFlowLabelY.setVisible(true);
					
					constantFlowNumberFieldY.setValue(globalParameters.getConstantFlowY());					
					constantValueNumberFieldY.setVisible(false);
					constantFlowNumberFieldY.setVisible(true);
				}
				else if(selectedBoundaryConditon == BoundaryCondition.PERIODIC){
					constantValueLabelY.setVisible(false);
					constantFlowLabelY.setVisible(false);
					constantValueNumberFieldY.setVisible(false);
					constantFlowNumberFieldY.setVisible(false);
				}
				globalParameters.setBoundaryConditionY(selectedBoundaryConditon);
			}
		});
		optionList.addLabelled("Boundary Condition Y-Axis", comboBoxBoundaryY);
		constantValueLabelY = new JLabel("Constant Value Y-Axis:");
		constantValueNumberFieldY = new NumberTextField(globalParameters.getConstantValueY()){
			public double newValue(final double val){
				if(val>=0){
					globalParameters.setConstantValueY(val);
					return val;
				}
				return getValue();
			}
		};
		
		constantFlowLabelY = new JLabel("Constant Flow Y-Axis:");
		constantFlowLabelY.setVisible(false);
		constantFlowNumberFieldY = new NumberTextField(globalParameters.getConstantFlowY()){
			public double newValue(final double val){
				globalParameters.setConstantFlowY(val);
				return val;
			}
		};
		
		constantFlowNumberFieldY.setVisible(false);
		optionList.add(constantValueLabelY, constantValueNumberFieldY);
		optionList.add(constantFlowLabelY, constantFlowNumberFieldY);
		
		
		 
		if(globalParameters instanceof DiffusionModelGlobalParameters3D){
			comboBoxBoundaryZ = new JComboBox<BoundaryCondition>(BoundaryCondition.values());
			comboBoxBoundaryZ.setSelectedIndex(0);
			comboBoxBoundaryZ.addActionListener(new ActionListener(){		
				public void actionPerformed(ActionEvent e) {
					BoundaryCondition selectedBoundaryConditon = (BoundaryCondition)comboBoxBoundaryZ.getSelectedItem();
					if(selectedBoundaryConditon == BoundaryCondition.DIRICHLET){
						constantValueLabelZ.setVisible(true);
						constantFlowLabelZ.setVisible(false);
						
						constantValueNumberFieldZ.setValue(((DiffusionModelGlobalParameters3D)globalParameters).getConstantValueZ());
						
						constantValueNumberFieldZ.setVisible(true);
						constantFlowNumberFieldZ.setVisible(false);
						
					}
					else if(selectedBoundaryConditon == BoundaryCondition.NEUMANN){
						constantValueLabelZ.setVisible(false);
						constantFlowLabelZ.setVisible(true);
						
						constantFlowNumberFieldZ.setValue(((DiffusionModelGlobalParameters3D)globalParameters).getConstantFlowZ());					
						constantValueNumberFieldZ.setVisible(false);
						constantFlowNumberFieldZ.setVisible(true);
					}
					else if(selectedBoundaryConditon == BoundaryCondition.PERIODIC){
						constantValueLabelZ.setVisible(false);
						constantFlowLabelZ.setVisible(false);
						constantValueNumberFieldZ.setVisible(false);
						constantFlowNumberFieldZ.setVisible(false);
					}
					((DiffusionModelGlobalParameters3D)globalParameters).setBoundaryConditionZ(selectedBoundaryConditon);
				}
			});
			optionList.addLabelled("Boundary Condition Z-Axis", comboBoxBoundaryZ);
			constantValueLabelZ = new JLabel("Constant Value Z-Axis:");
			constantValueNumberFieldZ = new NumberTextField(((DiffusionModelGlobalParameters3D)globalParameters).getConstantValueZ()){
				public double newValue(final double val){
					if(val>=0){
						((DiffusionModelGlobalParameters3D)globalParameters).setConstantValueZ(val);
						return val;
					}
					return getValue();				
				}
			};
			constantFlowLabelZ = new JLabel("Constant Flow Z-Axis:");
			constantFlowLabelZ.setVisible(false);
			constantFlowNumberFieldZ = new NumberTextField(((DiffusionModelGlobalParameters3D)globalParameters).getConstantFlowZ()){
				public double newValue(final double val){
					((DiffusionModelGlobalParameters3D)globalParameters).setConstantFlowZ(val);
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
				constantFlowNumberFieldX.setEnabled(true);
				comboBoxBoundaryX.setEnabled(true);
				constantValueNumberFieldY.setEnabled(true);
				constantFlowNumberFieldY.setEnabled(true);
				comboBoxBoundaryY.setEnabled(true);				
				
				if(constantValueNumberFieldZ != null) constantValueNumberFieldZ.setEnabled(true);
				if(constantFlowNumberFieldZ != null) constantFlowNumberFieldZ.setEnabled(true);
				if(comboBoxBoundaryZ != null)comboBoxBoundaryZ.setEnabled(true);
			}			
			
			public void simulationWasStarted(){
				constantValueNumberFieldX.setEnabled(false);
				constantValueNumberFieldX.setValue(globalParameters.getConstantValueX());
				constantFlowNumberFieldX.setEnabled(false);
				constantFlowNumberFieldX.setValue(globalParameters.getConstantFlowX());
				comboBoxBoundaryX.setEnabled(false);
				comboBoxBoundaryX.setSelectedItem(globalParameters.getBoundaryConditionX());
				
				constantValueNumberFieldY.setEnabled(false);
				constantValueNumberFieldY.setValue(globalParameters.getConstantValueY());
				constantFlowNumberFieldY.setEnabled(false);
				constantFlowNumberFieldY.setValue(globalParameters.getConstantFlowY());
				comboBoxBoundaryY.setEnabled(false);
				comboBoxBoundaryY.setSelectedItem(globalParameters.getBoundaryConditionY());
				
				if(constantValueNumberFieldZ != null){
					constantValueNumberFieldZ.setEnabled(false);
					constantValueNumberFieldZ.setValue(((DiffusionModelGlobalParameters3D)globalParameters).getConstantValueZ());
				}
				if(constantFlowNumberFieldZ != null){ 
					constantFlowNumberFieldZ.setEnabled(false);
					constantFlowNumberFieldZ.setValue(((DiffusionModelGlobalParameters3D)globalParameters).getConstantFlowZ());
				}
				if(comboBoxBoundaryZ != null){
					comboBoxBoundaryZ.setEnabled(false);
					comboBoxBoundaryZ.setSelectedItem(((DiffusionModelGlobalParameters3D)globalParameters).getBoundaryConditionZ());
				}
				
			}
			
			
			public void simulationWasPaused() {
			
				simulationWasStarted();
				
			}
		});
		
		this.add(optionList, BorderLayout.CENTER);
		
	}
	
	

}
