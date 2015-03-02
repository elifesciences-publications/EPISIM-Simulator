package sim.app.episim.model.initialization;

import java.io.File;
import java.util.ArrayList;

import org.w3c.dom.DOMException;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import episiminterfaces.EpisimPortrayal;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.centerbased2D.oldmodel.CenterBased2DModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.SimulationStateData;
import sim.app.episim.persistence.dataconvert.XmlEpisimBiomechanicalModel;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.tissue.TissueController;
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
	
	protected UniversalCell getSampleCell() {
		if (this.simulationStateData == null){
			ArrayList<UniversalCell> cells = buildStandardInitialCellEnsemble();
			return cells == null ? null : cells.get(0);
		}
		else{
			simulationStateData.clearLoadedCells();
			ArrayList<XmlUniversalCell> xmlCells = simulationStateData.getCells();
			if(xmlCells != null && !xmlCells.isEmpty()){
				XmlUniversalCell xCell = xmlCells.get(0);
				if(xCell!= null){
					simulationStateData.putCellToBeLoaded((Long) xCell.getId(), xCell);
					buildCell(xCell);
					ArrayList<UniversalCell> universalCells = simulationStateData.getAlreadyLoadedCellsAsList();
					if(universalCells != null && ! universalCells.isEmpty()){
						UniversalCell uCell = universalCells.get(0);
						if(uCell != null){
							xCell = simulationStateData.getAlreadyLoadedXmlCellNewID(uCell.getID());
							if (xCell != null) {
								XmlEpisimBiomechanicalModel xCellMechModel = xCell.getEpisimBiomechanicalModel();
								if(xCellMechModel != null)xCellMechModel.copyValuesToTarget(uCell.getEpisimBioMechanicalModelObject());
								return uCell;
							}
						}
					}
				}
			}
		}
		return null;
	}

	protected abstract ArrayList<UniversalCell> buildStandardInitialCellEnsemble();

	protected abstract void initializeCellEnsembleBasedOnRandomAgeDistribution(
			ArrayList<UniversalCell> cellEnsemble);

	protected ArrayList<UniversalCell> buildInitialCellEnsemble() {
		EpisimBiomechanicalModelGlobalParameters globalMech = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	//	if(simulationStateData.getEpisimBioMechanicalModelGlobalParameters() != null)simulationStateData.getEpisimBioMechanicalModelGlobalParameters().copyValuesToTarget(globalMech);
	//	if(simulationStateData.getTissueBorder() != null)simulationStateData.getTissueBorder().copyValuesToTarget(TissueController.getInstance().getTissueBorder());
		simulationStateData.clearLoadedCells();

		ArrayList<XmlUniversalCell> xmlCells = simulationStateData.getCells();
		for (XmlUniversalCell xCell : xmlCells) {

			// xCell.importParametersFromXml();
			simulationStateData.putCellToBeLoaded((Long) xCell.getId(), xCell);

		}
		for (XmlUniversalCell xCell : xmlCells) {
			buildCell(xCell);
		}

		//System.out.println(xmlCells.size() + " vs. " + simulationStateData.getAlreadyLoadedCellsAsList().size());

		ArrayList<UniversalCell> universalCells = simulationStateData.getAlreadyLoadedCellsAsList();
		
		

		for (UniversalCell uCell : universalCells) {
			XmlUniversalCell xCell = simulationStateData.getAlreadyLoadedXmlCellNewID(uCell.getID());
			if (xCell != null) {
				XmlEpisimBiomechanicalModel xCellMechModel = xCell.getEpisimBiomechanicalModel();
				if(xCellMechModel != null)xCellMechModel.copyValuesToTarget(uCell.getEpisimBioMechanicalModelObject());
			}
		}
		return universalCells;
	}

	private UniversalCell buildCell(XmlUniversalCell xCell) {
		if(xCell == null) return null;
		UniversalCell loadCell = simulationStateData.getAlreadyLoadedCell(xCell.getId());
		if (loadCell != null)
			return loadCell;
		
		long id = xCell.getId();
	
			long motherID = (Long) xCell.getMotherId();
			if (id == motherID || xCell.getMotherId() == Long.MIN_VALUE) {
				loadCell = new UniversalCell(id, true);
			} else {
				UniversalCell mother = buildCell(simulationStateData.getCellToBeLoaded(motherID));
					// TODO was tun wenn mutter gelöscht ist?
				loadCell = new UniversalCell(id, mother, null, true);
			}
		if(loadCell != null){
			xCell.copyValuesToTarget(loadCell);
			simulationStateData.addLoadedCell(xCell, loadCell);
		}
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
