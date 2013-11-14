package sim.app.episim.gui;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import sim.SimStateServer;
import sim.app.episim.SimulationStateChangeListener;
import sim.app.episim.model.diffusion.ExtracellularDiffusionFieldBCConfig3D;
import sim.util.gui.LabelledListHack;
import sim.util.gui.NumberTextField;

import episiminterfaces.EpisimDiffusionFieldConfigurationEx;


public class DiffusionFieldConfigurationPanel extends JPanel{
	
	private EpisimDiffusionFieldConfigurationEx fieldConfig=null;
	
	private NumberTextField diffusionCoefficient;
	private NumberTextField degradationRate;
	
	private NumberTextField defaultConcentration;
	private NumberTextField minimumConcentration;
	private NumberTextField maximumConcentration;
	
	public DiffusionFieldConfigurationPanel(EpisimDiffusionFieldConfigurationEx fieldConfig){
		this.fieldConfig = fieldConfig;
		buildPanel();
	}
	
	private void buildPanel(){
		this.setLayout(new BorderLayout());
		LabelledListHack optionList = new LabelledListHack();
		optionList.setInsets(new Insets(5, 10, 0, 10));
		
		diffusionCoefficient = new NumberTextField(fieldConfig.getDiffusionCoefficient()){
			public double newValue(final double val){
				if(val > 0){
					fieldConfig.setDiffusionCoefficient(val);
					return val;
				}
				else return fieldConfig.getDiffusionCoefficient();
			}
		};		
		optionList.addLabelled("Diffusion Coefficient" ,diffusionCoefficient);
		
		degradationRate = new NumberTextField(fieldConfig.getDegradationRate()){
			public double newValue(final double val){
				if(val > 0 && val <=1){
					fieldConfig.setDegradationRate(val);
					return val;
				}
				else return fieldConfig.getDegradationRate();
			}
		};		
		optionList.addLabelled("Degradation Coefficient", degradationRate);
		
		defaultConcentration = new NumberTextField(fieldConfig.getDefaultConcentration()){
			public double newValue(final double val){
				if(val >= fieldConfig.getMinimumConcentration() && val <= fieldConfig.getMaximumConcentration()){
					fieldConfig.setDefaultConcentration(val);
					return val;
				}
				else return fieldConfig.getDefaultConcentration();
			}
		};		
		optionList.addLabelled("Default Concentration", defaultConcentration);
		
		minimumConcentration = new NumberTextField(fieldConfig.getMinimumConcentration()){
			public double newValue(final double val){
				if(val >= 0 && val <= fieldConfig.getDefaultConcentration() && val < fieldConfig.getMaximumConcentration()){
					fieldConfig.setMinimumConcentration(val);
					return val;
				}
				else return fieldConfig.getMinimumConcentration();
			}
		};		
		optionList.addLabelled("Minimum Concentration", minimumConcentration);
		
		maximumConcentration = new NumberTextField(fieldConfig.getMaximumConcentration()){
			public double newValue(final double val){
				if(val >= 0 && val >= fieldConfig.getDefaultConcentration() && val > fieldConfig.getMinimumConcentration()){
					fieldConfig.setMaximumConcentration(val);
					return val;
				}
				else return fieldConfig.getMaximumConcentration();
			}
		};		
		optionList.addLabelled("Maximum Concentration", maximumConcentration);
		
		
		SimStateServer.getInstance().addSimulationStateChangeListener(new SimulationStateChangeListener() {
			
			
			public void simulationWasStopped() {		
				diffusionCoefficient.setEnabled(true);
				degradationRate.setEnabled(true);
				defaultConcentration.setEnabled(true);
				minimumConcentration.setEnabled(true);
				maximumConcentration.setEnabled(true);
			}			
			
			public void simulationWasStarted(){
				diffusionCoefficient.setEnabled(false);
				degradationRate.setEnabled(false);
				defaultConcentration.setEnabled(false);
				minimumConcentration.setEnabled(false);
				maximumConcentration.setEnabled(false);
			}
			
			
			public void simulationWasPaused() {
			
				simulationWasStarted();
				
			}
		});
		
		this.add(optionList, BorderLayout.CENTER);
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Field Parameters"), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	}

}
