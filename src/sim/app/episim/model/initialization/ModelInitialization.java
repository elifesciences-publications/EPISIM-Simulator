package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.DOMException;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;

import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.tissue.TissueController;
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
		this.biomechanicalModelInitializer
				.initializeCellEnsembleBasedOnRandomAgeDistribution(initiallyExistingCells);
		//TODO this.extraCellularDiffusionInitializer.buildExtraCellularDiffusionFields();
		if (simulationStateData != null) {
			// TODO global Parameters, sind die hier richtig?

			EpisimCellBehavioralModelGlobalParameters globalBehave = ModelController
					.getInstance()
					.getEpisimCellBehavioralModelGlobalParameters();
			EpisimBiomechanicalModelGlobalParameters globalMech = ModelController
					.getInstance()
					.getEpisimBioMechanicalModelGlobalParameters();

//			simulationStateData.getEpisimBioMechanicalModelGlobalParameters()
//					.importParametersFromXml();
			simulationStateData.getEpisimBioMechanicalModelGlobalParameters()
					.copyValuesToTarget(globalMech);

//			simulationStateData.getEpisimCellBehavioralModelGlobalParameters()
//					.importParametersFromXml();
			simulationStateData.getEpisimCellBehavioralModelGlobalParameters()
					.copyValuesToTarget(globalBehave);

//			simulationStateData.getMiscalleneousGlobalParameters()
//					.importParametersFromXml();
			simulationStateData.getMiscalleneousGlobalParameters()
					.copyValuesToTarget(
							MiscalleneousGlobalParameters.instance());
		}

		return initiallyExistingCells;
	}

}
