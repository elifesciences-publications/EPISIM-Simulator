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
	
	public BiomechanicalModelInitializer(){
		this(null);
	}
	
	public BiomechanicalModelInitializer(SimulationStateData simulationStateData){
		this.simulationStateData = simulationStateData;
	}
	
	protected ArrayList<UniversalCell> getInitialCellEnsemble(){
		if(this.simulationStateData == null) return buildStandardInitialCellEnsemble();
		else return buildInitialCellEnsemble();
	}
	
	protected abstract ArrayList<UniversalCell> buildStandardInitialCellEnsemble();
	
	protected abstract void initializeCellEnsembleBasedOnRandomAgeDistribution(ArrayList<UniversalCell> cellEnsemble);
	
	
	protected ArrayList<UniversalCell> buildInitialCellEnsemble(){
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
			loadedCells.add(buildCell(xCell, loadedCells));
		}

		return loadedCells;
	}

	private UniversalCell buildCell(XmlUniversalCell xCell, ArrayList<UniversalCell> loadedCells) {
		UniversalCell loadCell = null;
		
		long id = (Long) xCell.get("iD");
		long motherID = (Long) xCell.get("motherId");
		if(id == motherID){
			loadCell = new UniversalCell();
			simulationStateData.alreadyLoadedCells.put(id, loadCell);
		} else{
			UniversalCell mother = simulationStateData.alreadyLoadedCells.get(id);
			if(mother == null){
				if(simulationStateData.cellsToBeLoaded.get(motherID) != null)
				loadedCells.add(mother = buildCell(simulationStateData.cellsToBeLoaded.get(motherID), loadedCells));
			//	else
					//System.out.println(); //TODO was tun wenn mutter gelöscht ist?
			}
			loadCell = new UniversalCell(mother, null, null);
		}
		xCell.copyValuesToTarget(loadCell);
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
	
   protected SimulationStateData getModelInitializationFile(){   
   	return simulationStateData;
   }

}
