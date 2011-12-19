package sim.app.episim.model.initialization;

import java.util.HashMap;

import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.visualization.ExtraCellularDiffusionPortrayal;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.tissue.TissueController;

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
			ExtraCellularDiffusionField[] diffusionFields = ModelController
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
			ExtraCellularDiffusionField[] diffusionFields) {
		if (this.simStateData != null) {

		}
	}

	public void buildExtraCellularDiffusionFields() {
		rebuildExtraCellularDiffusionFields();
		ExtraCellularDiffusionField[] diffusionFields = ModelController
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

			HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField>();

			double widthInMikron = TissueController.getInstance()
					.getTissueBorder().getWidthInMikron();
			double heightInMikron = TissueController.getInstance()
					.getTissueBorder().getHeightInMikron();

			ExtraCellularDiffusionField actField;
			for (int i = 0; i < fieldConfigurations.length; i++) {
				actField = new ExtraCellularDiffusionField(
						fieldConfigurations[i], widthInMikron, heightInMikron,
						true);

				// TODO Remove this initialization loops
				 int delta = 8;
				 int width_half =
				 actField.getExtraCellularField().getWidth()/2;
				 int height_half =
				 actField.getExtraCellularField().getHeight()/2;
				 for(int y = (height_half-delta); y < height_half+delta; y++){
				 for(int x = (width_half-delta-2); x < width_half+delta+2;
				 x++){
				 actField.getExtraCellularField().set(x, y, 255);
				 }
				 }

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
			ExtraCellularDiffusionField ecDiffField) {
		ModelController.getInstance().getExtraCellularDiffusionController()
				.getAllExtraCellularDiffusionFields();
	}

	private void setExtraCellularDiffusionFieldInPortrayal(
			ExtraCellularDiffusionField diffusionField) {
		if (this.currentDiffusionFieldPortrayals != null) {
			// the name of a diffusionField is unique
			for (int i = 0; i < this.currentDiffusionFieldPortrayals.length; i++) {
				if (this.currentDiffusionFieldPortrayals[i]
						.getExtraCellularDiffusionField().getName()
						.equals(diffusionField.getName())) {
					this.currentDiffusionFieldPortrayals[i]
							.setExtraCellularDiffusionField(diffusionField);
					break;
				}
			}
		}
	}

}
