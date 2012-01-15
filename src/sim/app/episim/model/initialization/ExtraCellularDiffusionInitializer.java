package sim.app.episim.model.initialization;

import java.util.HashMap;

import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.EpisimProperties;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.visualization.ExtraCellularDiffusionPortrayal;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;
import sim.field.grid.DoubleGrid2D;

public class ExtraCellularDiffusionInitializer {

	private SimulationStateData simStateData;

	private ExtraCellularDiffusionPortrayal[] currentDiffusionFieldPortrayals;

	public ExtraCellularDiffusionInitializer() {
		this(null);
	}

	public ExtraCellularDiffusionInitializer(SimulationStateData simStateData) {
		this.simStateData = simStateData;
	}

	public EpisimPortrayal[] getExtraCellularDiffusionPortrayals() {
		if (ModelController.getInstance().getExtraCellularDiffusionController()
				.getNumberOfEpisimExtraCellularDiffusionFieldConfigurations() > 0) {
			ExtraCellularDiffusionField2D[] diffusionFields = ModelController
					.getInstance().getExtraCellularDiffusionController()
					.getAllExtraCellularDiffusionFields();
			currentDiffusionFieldPortrayals = new ExtraCellularDiffusionPortrayal[diffusionFields.length];
			for (int i = 0; i < diffusionFields.length; i++) {
				currentDiffusionFieldPortrayals[i] = new ExtraCellularDiffusionPortrayal(
						diffusionFields[i]);
			}
		} else {
			currentDiffusionFieldPortrayals = new ExtraCellularDiffusionPortrayal[0];
		}
		return currentDiffusionFieldPortrayals;
	}

	private void initializeExtraCellularDiffusionFields(
			ExtraCellularDiffusionField2D[] diffusionFields) {
		if (this.simStateData != null) {

		}
	}

	public void buildExtraCellularDiffusionFields() {
		rebuildExtraCellularDiffusionFields();
		ExtraCellularDiffusionField2D[] diffusionFields = ModelController
				.getInstance().getExtraCellularDiffusionController()
				.getAllExtraCellularDiffusionFields();
		initializeExtraCellularDiffusionFields(diffusionFields);

	}

	private void rebuildExtraCellularDiffusionFields() {
		if (ModelController.getInstance().getExtraCellularDiffusionController()
				.getNumberOfEpisimExtraCellularDiffusionFieldConfigurations() > 0) {
			EpisimDiffusionFieldConfiguration[] fieldConfigurations = ModelController
					.getInstance().getExtraCellularDiffusionController()
					.getEpisimExtraCellularDiffusionFieldsConfigurations();

			HashMap<String, ExtraCellularDiffusionField2D> extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField2D>();

			double widthInMikron = TissueController.getInstance()
					.getTissueBorder().getWidthInMikron();
			double heightInMikron = TissueController.getInstance()
					.getTissueBorder().getHeightInMikron();

			ExtraCellularDiffusionField2D actField;

			for(int i = 0; i< fieldConfigurations.length; i++){
				actField = new ExtraCellularDiffusionField2D(fieldConfigurations[i],widthInMikron, heightInMikron, 
						ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInXDirection(),ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInYDirection());				
				actField.getExtraCellularField().setTo(actField.getFieldConfiguration().getDefaultConcentration());
				if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE)!= null &&
						EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE).equals(EpisimProperties.ON)){
					testFieldInitialization(actField.getExtraCellularField());
				}	
			
				if(simStateData != null) initializeExtraCellularDiffusionFieldWithSimState(actField);
				

				setExtraCellularDiffusionFieldInPortrayal(actField);
				extraCellularFieldMap.put(actField.getName(), actField);
			}
			ModelController.getInstance().getExtraCellularDiffusionController()
					.setExtraCellularFieldMap(extraCellularFieldMap);
			if (simStateData != null) {
				simStateData.getExtraCellularDiffusionFieldArray()
						.copyValuesToTarget(
								ModelController.getInstance()
										.getExtraCellularDiffusionController()
										.getAllExtraCellularDiffusionFields());
			}
		}
	}

	private void initializeExtraCellularDiffusionFieldWithSimState(
			ExtraCellularDiffusionField2D ecDiffField) {
		ModelController.getInstance().getExtraCellularDiffusionController()
				.getAllExtraCellularDiffusionFields();
	}

	
	private void testFieldInitialization(DoubleGrid2D field){
		int delta = 8;				
		int width_half = field.getWidth()/2;
		int height_half = field.getHeight()/2;
		for(int y = (height_half-delta); y < height_half+delta; y++){
			for(int x = (width_half-delta-2); x < width_half+delta+2; x++){
				field.set(x, y, 255);
			}						
		}
	}
	
	
	
	private void setExtraCellularDiffusionFieldInPortrayal(ExtraCellularDiffusionField2D diffusionField){
		if(this.currentDiffusionFieldPortrayals != null){
			//the name of a diffusionField is unique
			for(int i = 0; i< this.currentDiffusionFieldPortrayals.length; i++){
				if(this.currentDiffusionFieldPortrayals[i].getExtraCellularDiffusionField().getName().equals(diffusionField.getName())){
					this.currentDiffusionFieldPortrayals[i].setExtraCellularDiffusionField(diffusionField);
					break;
				}
			}
		}
	}

}
