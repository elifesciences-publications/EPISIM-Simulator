package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.DOMException;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.EpisimProperties;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2Df.oldmodel.CenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2Df.oldmodel.CenterBased2DModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.portrayal.Portrayal;
import sim.util.Double2D;

public class ModelInitialization {

	private SimulationStateData simulationStateData;

	private BiomechanicalModelInitializer biomechanicalModelInitializer;
	private CellBehavioralModelInitializer cellbehavioralModelInitializer;
	private ExtraCellularDiffusionInitializer extraCellularDiffusionInitializer;

	public ModelInitialization() {
		this(null);
	}

	public ModelInitialization(SimulationStateData simStateData) {
		this.simulationStateData = simStateData;
		if (simStateData == null) {
			this.biomechanicalModelInitializer = ModelController.getInstance()
					.getBioMechanicalModelController()
					.getBiomechanicalModelInitializer();
			this.cellbehavioralModelInitializer = ModelController.getInstance()
					.getCellBehavioralModelController()
					.getCellBehavioralModelInitializer();
			this.extraCellularDiffusionInitializer = ModelController.getInstance()
					.getExtraCellularDiffusionController()
					.getExtraCellularDiffusionInitializer();
		} else {
			this.biomechanicalModelInitializer = ModelController.getInstance()
					.getBioMechanicalModelController()
					.getBiomechanicalModelInitializer(simStateData);
			this.cellbehavioralModelInitializer = ModelController.getInstance()
					.getCellBehavioralModelController()
					.getCellBehavioralModelInitializer(simStateData);
			this.extraCellularDiffusionInitializer = ModelController.getInstance()
					.getExtraCellularDiffusionController()
					.getExtraCellularDiffusionInitializer(simStateData);
		}
		if (this.cellbehavioralModelInitializer == null
				|| this.biomechanicalModelInitializer == null
				|| this.extraCellularDiffusionInitializer == null)
			throw new IllegalArgumentException(
					"Neither the CellBehavioralModelInitializer nor the BiomechanicalModelInitializer nor ExtraCellularDiffusionInitializer must be null!");
	}

	public ArrayList<UniversalCell> getCells() {

		return buildCellList();
	}
	
	public boolean testCBMFileLoadedSimStateCompatibility(){
		UniversalCell sampleCell = this.biomechanicalModelInitializer.getSampleCell();		
		if(sampleCell != null){
			this.cellbehavioralModelInitializer.initializeSampleCell(sampleCell);
			return true;
		}
		return false;
	}

	public EpisimPortrayal getCellPortrayal() {
		return biomechanicalModelInitializer.getCellPortrayal();
	}
	
	public EpisimPortrayal[] getExtraCellularDiffusionPortrayals(){
		return extraCellularDiffusionInitializer.getExtraCellularDiffusionPortrayals();
	}

	public EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return biomechanicalModelInitializer
				.getAdditionalPortrayalsCellBackground();
	}

	public EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return biomechanicalModelInitializer
				.getAdditionalPortrayalsCellForeground();
	}

	private ArrayList<UniversalCell> buildCellList() {
		ArrayList<UniversalCell> initiallyExistingCells = this.biomechanicalModelInitializer
				.getInitialCellEnsemble();
		
		
		this.cellbehavioralModelInitializer
				.initializeCellEnsemble(initiallyExistingCells);
		if(EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT) != null &&
				EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT).equals(EpisimProperties.ON)){
			this.biomechanicalModelInitializer
					.initializeCellEnsembleBasedOnRandomAgeDistribution(initiallyExistingCells);
		}
		this.extraCellularDiffusionInitializer.buildExtraCellularDiffusionFields();
		if (simulationStateData != null) {
			
			

//			simulationStateData.getEpisimBioMechanicalModelGlobalParameters()
//					.importParametersFromXml();
			

//			simulationStateData.getEpisimCellBehavioralModelGlobalParameters()
//					.importParametersFromXml();
			

//			simulationStateData.getMiscalleneousGlobalParameters()
//					.importParametersFromXml();
			
			
			
		}

		return initiallyExistingCells;
	}

}
