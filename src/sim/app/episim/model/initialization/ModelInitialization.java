package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.DOMException;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.UniversalCell;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModelGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.tissue.TissueController;
import sim.portrayal.Portrayal;
import sim.util.Double2D;

public class ModelInitialization {

	private SimulationStateData simulationStateData;

	private BiomechanicalModelInitializer biomechanicalModelInitializer;
	private CellBehavioralModelInitializer cellbehavioralModelInitializer;

	public ModelInitialization() {
		this(null);
	}

	public ModelInitialization(SimulationStateData simStateData) {
		this.simulationStateData = simStateData;
		if (simStateData == null) {
			this.biomechanicalModelInitializer = ModelController.getInstance().getBioMechanicalModelController().getBiomechanicalModelInitializer();
			this.cellbehavioralModelInitializer = ModelController.getInstance().getCellBehavioralModelController().getCellBehavioralModelInitializer();
		} else {
			this.biomechanicalModelInitializer = ModelController.getInstance().getBioMechanicalModelController().getBiomechanicalModelInitializer(simStateData);
			this.cellbehavioralModelInitializer = ModelController.getInstance().getCellBehavioralModelController().getCellBehavioralModelInitializer(simStateData);
		}
		if (this.cellbehavioralModelInitializer == null || this.biomechanicalModelInitializer == null)
			throw new IllegalArgumentException("Neither the CellBehavioralModelInitializer nor the BiomechanicalModelInitializer must be null!");
	}

	public ArrayList<UniversalCell> getCells() {

		if (this.simulationStateData == null)
			return buildStandardCellList();

		else {
			return buildLoadedCellList();
		}
	}

	private ArrayList<UniversalCell> buildLoadedCellList() {
		ArrayList<UniversalCell> loadedCells = new ArrayList<UniversalCell>();

		ArrayList<XmlUniversalCell> xmlCells = simulationStateData.getCells();
		for (XmlUniversalCell xCell : xmlCells) {
			try {
				xCell.importParametersFromXml();
				simulationStateData.cellsToBeLoaded.put((Long) xCell.get("iD"), xCell);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (DOMException e) {
				e.printStackTrace();
			}
		}
		for(XmlUniversalCell xCell : xmlCells){
			buildCell(xCell);
		}

		this.cellbehavioralModelInitializer.initializeCellEnsemble(loadedCells);
		this.biomechanicalModelInitializer.initializeCellEnsembleBasedOnRandomAgeDistribution(loadedCells);
		return loadedCells;
	}

	private UniversalCell buildCell(XmlUniversalCell xCell) {
		ArrayList<XmlUniversalCell> xmlCells = simulationStateData.getCells();
		UniversalCell loadCell = null;
		
		if((Long) xCell.get("iD") == (Long) xCell.get("motherId")){
			loadCell = new UniversalCell();
			simulationStateData.alreadyLoadedCells.put((Long) xCell.get("iD"), loadCell);
		} else{
			UniversalCell mother = simulationStateData.alreadyLoadedCells.get((Long) xCell.get("iD"));
			if(mother == null){
				mother = buildCell(simulationStateData.cellsToBeLoaded.get((Long) xCell.get("motherId")));
			}
			loadCell = new UniversalCell(mother, null, null);
		}
		return loadCell;
	}

	public EpisimPortrayal getCellPortrayal() {
		return biomechanicalModelInitializer.getCellPortrayal();
	}

	public EpisimPortrayal[] getAdditionalPortrayalsCellBackground() {
		return biomechanicalModelInitializer.getAdditionalPortrayalsCellBackground();
	}

	public EpisimPortrayal[] getAdditionalPortrayalsCellForeground() {
		return biomechanicalModelInitializer.getAdditionalPortrayalsCellForeground();
	}

	private ArrayList<UniversalCell> buildStandardCellList() {
		ArrayList<UniversalCell> initiallyExistingCells = this.biomechanicalModelInitializer.buildStandardInitialCellEnsemble();

		this.cellbehavioralModelInitializer.initializeCellEnsemble(initiallyExistingCells);
		//this.biomechanicalModelInitializer.initializeCellEnsembleBasedOnRandomAgeDistribution(initiallyExistingCells);
		return initiallyExistingCells;
	}

}
