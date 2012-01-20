package sim.app.episim.model.initialization;

import java.util.HashMap;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimDiffusionFieldConfiguration;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.EpisimProperties;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.model.visualization.ExtraCellularDiffusionCrossSectionPortrayal3D;
import sim.app.episim.model.visualization.ExtraCellularDiffusionPortrayal;
import sim.app.episim.model.visualization.ExtraCellularDiffusionPortrayal2D;
import sim.app.episim.model.visualization.ExtraCellularDiffusionPortrayal3D;
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
			ExtraCellularDiffusionField[] diffusionFields = ModelController.getInstance().getExtraCellularDiffusionController().
														getAllExtraCellularDiffusionFields(
																			new ExtraCellularDiffusionField[ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields()]);
			currentDiffusionFieldPortrayals = new ExtraCellularDiffusionPortrayal[diffusionFields.length];
			for (int i = 0; i < diffusionFields.length; i++) {
				if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
					currentDiffusionFieldPortrayals[i] = new ExtraCellularDiffusionPortrayal2D(diffusionFields[i]);
				}
				if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
					if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_3DVISUALIZATION) != null
							&& EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_3DVISUALIZATION).toLowerCase().equals(EpisimProperties.SIMULATOR_DF_3DVISUALIZATION_BLOCK_MODE)){
							currentDiffusionFieldPortrayals[i] = new ExtraCellularDiffusionPortrayal3D(diffusionFields[i]);
						}
						else{
							currentDiffusionFieldPortrayals[i] = new ExtraCellularDiffusionCrossSectionPortrayal3D(diffusionFields[i]);
						}				
				}
			}
		} else {
			currentDiffusionFieldPortrayals = new ExtraCellularDiffusionPortrayal[0];
		}
		return currentDiffusionFieldPortrayals;
	}	

	public void buildExtraCellularDiffusionFields() {
		rebuildExtraCellularDiffusionFields();
	}

	
	
	private void rebuildExtraCellularDiffusionFields() {
		if (ModelController.getInstance().getExtraCellularDiffusionController()
				.getNumberOfEpisimExtraCellularDiffusionFieldConfigurations() > 0) {
			EpisimDiffusionFieldConfiguration[] fieldConfigurations = ModelController.getInstance().getExtraCellularDiffusionController().getEpisimExtraCellularDiffusionFieldsConfigurations();

			HashMap<String, ExtraCellularDiffusionField> extraCellularFieldMap = new HashMap<String, ExtraCellularDiffusionField>();

			double widthInMikron = TissueController.getInstance().getTissueBorder().getWidthInMikron();
			double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
			double lengthInMikron = TissueController.getInstance().getTissueBorder().getLengthInMikron();

			ExtraCellularDiffusionField actField = null;

			for(int i = 0; i< fieldConfigurations.length; i++){
				if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
					actField = new ExtraCellularDiffusionField2D(fieldConfigurations[i],widthInMikron, heightInMikron, 
							ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInXDirection(),ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInYDirection());				
				}
				if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
					actField = new ExtraCellularDiffusionField3D(fieldConfigurations[i],widthInMikron, heightInMikron, lengthInMikron,
							ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInXDirection(),ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInYDirection(), ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().areDiffusionFieldsContinousInZDirection());				
				}
				actField.setToValue(actField.getFieldConfiguration().getDefaultConcentration());
				if(EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE)!= null &&
						EpisimProperties.getProperty(EpisimProperties.SIMULATOR_DIFFUSION_FIELD_TESTMODE).equals(EpisimProperties.ON)){
					testFieldInitialization(actField);
				}	
							
				setExtraCellularDiffusionFieldInPortrayal(actField);
				extraCellularFieldMap.put(actField.getName(), actField);
			}
			ModelController.getInstance().getExtraCellularDiffusionController().setExtraCellularFieldMap(extraCellularFieldMap);
			if (simStateData != null) {
				if(ModelController.getInstance().getModelDimensionality()== ModelDimensionality.TWO_DIMENSIONAL){
					simStateData.getExtraCellularDiffusionFieldArray2D()
							.copyValuesToTarget(
									ModelController.getInstance()
											.getExtraCellularDiffusionController()
											.getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField2D[ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields()]));
				}
				if(ModelController.getInstance().getModelDimensionality()== ModelDimensionality.THREE_DIMENSIONAL){
					simStateData.getExtraCellularDiffusionFieldArray3D()
							.copyValuesToTarget(
									ModelController.getInstance()
											.getExtraCellularDiffusionController()
											.getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField3D[ModelController.getInstance().getExtraCellularDiffusionController().getNumberOfFields()]));
				}
			}
		}
	}

	
	
	private void testFieldInitialization(ExtraCellularDiffusionField field){
		int delta = 10;
		if(field instanceof ExtraCellularDiffusionField2D){
			delta = 8;
			ExtraCellularDiffusionField2D field2D = (ExtraCellularDiffusionField2D) field;
			int width_half = field2D.getExtraCellularField().getWidth()/2;
			int height_half = field2D.getExtraCellularField().getHeight()/2;
			for(int y = (height_half-delta); y < height_half+delta; y++){
				for(int x = (width_half-delta-2); x < width_half+delta+2; x++){
					field2D.getExtraCellularField().set(x, y, 255);
				}						
			}
		}
		if(field instanceof ExtraCellularDiffusionField3D){
			ExtraCellularDiffusionField3D field3D = (ExtraCellularDiffusionField3D) field;
		 int width_half = field3D.getExtraCellularField().getWidth()/2;
			int height_half = field3D.getExtraCellularField().getHeight()/2;
			int length_half = field3D.getExtraCellularField().getLength()/2;
			for(int z = (length_half-delta); z < length_half+delta; z++){
				for(int y = (height_half-delta); y < height_half+delta; y++){
					for(int x = (width_half-delta); x < width_half+delta; x++){
						field3D.getExtraCellularField().set(x, y, z, 255);
					}						
				}
			}	
			/*field3D.getExtraCellularField().set(0, 1, 0, 255);
			field3D.getExtraCellularField().set(0, 2, 0, 255);
			field3D.getExtraCellularField().set(0, 3, 0, 255);
			field3D.getExtraCellularField().set(1, 1, 0, 255);
			field3D.getExtraCellularField().set(1, 2, 0, 255);
			field3D.getExtraCellularField().set(1, 3, 0, 255);*/
		}
	}
	
	
	
	private void setExtraCellularDiffusionFieldInPortrayal(ExtraCellularDiffusionField diffusionField){
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
