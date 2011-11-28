package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.DOMException;

import episiminterfaces.EpisimPortrayal;

import sim.app.episim.UniversalCell;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.portrayal.Portrayal;

public abstract class BiomechanicalModelInitializer {

	private SimulationStateData simulationStateData;

	public BiomechanicalModelInitializer() {
		this(null);
	}

	public BiomechanicalModelInitializer(SimulationStateData simulationStateData) {
		this.simulationStateData = simulationStateData;
	}

	protected ArrayList<UniversalCell> getInitialCellEnsemble() {
		if (this.simulationStateData == null)
			return buildStandardInitialCellEnsemble();
		else
			return buildInitialCellEnsemble();
	}

	protected abstract ArrayList<UniversalCell> buildStandardInitialCellEnsemble();

	protected abstract void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble);

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		simulationStateData.clearLoadedCells();
		ArrayList<UniversalCell> loadedCells = new ArrayList<UniversalCell>();

		ArrayList<XmlUniversalCell> xmlCells = simulationStateData.getCells();
		for (XmlUniversalCell xCell : xmlCells) {

//			xCell.importParametersFromXml();
			simulationStateData.putCellToBeLoaded((Long) xCell.getId(), xCell);

		}
		for (XmlUniversalCell xCell : xmlCells) {
			buildCell(xCell, loadedCells);
		}
		return loadedCells;
	}

	private UniversalCell buildCell(XmlUniversalCell xCell,
			ArrayList<UniversalCell> loadedCells) {
		UniversalCell loadCell = null;

		long id = (Long) xCell.getId();
		long motherID = (Long) xCell.getMotherId();
		if (id == motherID) {
			loadCell = new UniversalCell();
		} else {
			UniversalCell mother = simulationStateData.getAlreadyLoadedCell(id);
			if (mother == null) {
				if (simulationStateData.getCellToBeLoaded(motherID) != null)
					buildCell(
							simulationStateData.getCellToBeLoaded(motherID), loadedCells);
				// else
				// System.out.println(); //TODO was tun wenn mutter gelöscht
				// ist?
			}
			loadCell = new UniversalCell(mother, null, null);
		}
		xCell.copyValuesToTarget(loadCell);
		simulationStateData.addLoadedCell(xCell, loadCell);
		loadedCells.add(loadCell);
		return loadCell;
	}

	/**
	 * Get component for visualizing the cells
	 * 
	 */
	protected abstract EpisimPortrayal getCellPortrayal();

	/**
	 * Other visualizing components
	 * 
	 */
	protected abstract EpisimPortrayal[] getAdditionalPortrayalsCellForeground();

	/**
	 * Other visualizing components
	 * 
	 */
	protected abstract EpisimPortrayal[] getAdditionalPortrayalsCellBackground();

	protected SimulationStateData getModelInitializationFile() {
		return simulationStateData;
	}

}
