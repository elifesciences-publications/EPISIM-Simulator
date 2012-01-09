package sim.app.episim.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.dataconvert.XmlExtraCellularDiffusionFieldArray;
import sim.app.episim.persistence.dataconvert.XmlObject;
import sim.app.episim.persistence.dataconvert.XmlTissueBorder;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.tissue.TissueController;

/*
 * update = Objekte sammeln
 */
public class SimulationStateData {

	private ArrayList<XmlUniversalCell> cells = new ArrayList<XmlUniversalCell>();
	private XmlObject<EpisimBiomechanicalModelGlobalParameters> episimBioMechanicalModelGlobalParameters;
	private XmlObject<EpisimCellBehavioralModelGlobalParameters> episimCellBehavioralModelGlobalParameters;
	private XmlObject<MiscalleneousGlobalParameters> miscalleneousGlobalParameters;

	private XmlExtraCellularDiffusionFieldArray extraCellularDiffusionFieldArray;
	private XmlTissueBorder tissueBorder;

	private HashMap<Long, UniversalCell> alreadyLoadedCells = new HashMap<Long, UniversalCell>();
	private HashMap<Long, XmlUniversalCell> alreadyLoadedXmlCellsNewID = new HashMap<Long, XmlUniversalCell>();
	private HashMap<Long, XmlUniversalCell> cellsToBeLoaded = new HashMap<Long, XmlUniversalCell>();

	private File loadedModelFile;
	private long simStepNumber = 0;

	private static SimulationStateData lastSimulationStateLoaded;


	public SimulationStateData() {
		lastSimulationStateLoaded = this;
	}

	public UniversalCell getAlreadyLoadedCell(long id) {

		return alreadyLoadedCells.get(id);
	}

	public XmlUniversalCell getAlreadyLoadedXmlCellNewID(long id) {

		return alreadyLoadedXmlCellsNewID.get(id);
	}

	public XmlUniversalCell getCellToBeLoaded(long id) {

		return cellsToBeLoaded.get(id);
	}

	public void putCellToBeLoaded(long id, XmlUniversalCell cellToBeLoaded) {
		cellsToBeLoaded.put(id, cellToBeLoaded);
	}

	public static SimulationStateData getLastSimulationStateLoaded() {
		return lastSimulationStateLoaded;
	}

	public ArrayList<UniversalCell> getAlreadyLoadedCellsAsList() {
		return new ArrayList<UniversalCell>(alreadyLoadedCells.values());
	}

	public void addLoadedCell(XmlUniversalCell xmlCell, UniversalCell loadedCell) {
		alreadyLoadedCells.put(xmlCell.getId(), loadedCell);
		alreadyLoadedXmlCellsNewID.put(loadedCell.getID(), xmlCell);
	}

	public void clearLoadedCells() {
		alreadyLoadedXmlCellsNewID.clear();
		alreadyLoadedCells.clear();
	}

	public void updateData() {

		this.simStepNumber = SimStateServer.getInstance().getSimStepNumber();

		this.loadedModelFile = ModelController.getInstance()
				.getCellBehavioralModelController().getActLoadedModelFile();

		this.tissueBorder = new XmlTissueBorder(TissueController.getInstance()
				.getTissueBorder());

		for (AbstractCell cell : TissueController.getInstance()
				.getActEpidermalTissue().getAllCells()) {
			if (cell != null)
				cells.add(new XmlUniversalCell(cell));
		}
		this.episimCellBehavioralModelGlobalParameters = new XmlObject<EpisimCellBehavioralModelGlobalParameters>(
				ModelController.getInstance()
						.getEpisimCellBehavioralModelGlobalParameters());
		this.miscalleneousGlobalParameters = new XmlObject<MiscalleneousGlobalParameters>(
				MiscalleneousGlobalParameters.instance());
		this.episimBioMechanicalModelGlobalParameters = new XmlObject<EpisimBiomechanicalModelGlobalParameters>(
				ModelController.getInstance()
						.getEpisimBioMechanicalModelGlobalParameters());

		this.extraCellularDiffusionFieldArray = new XmlExtraCellularDiffusionFieldArray(
				ModelController.getInstance()
						.getExtraCellularDiffusionController()
						.getAllExtraCellularDiffusionFields());
	}

	public ArrayList<XmlUniversalCell> getCells() {
		return cells;
	}

	public void setCells(ArrayList<XmlUniversalCell> cells) {
		this.cells = cells;
	}

	public void addCell(XmlUniversalCell cell) {
		this.cells.add(cell);
	}

	public XmlObject<EpisimBiomechanicalModelGlobalParameters> getEpisimBioMechanicalModelGlobalParameters() {
		return episimBioMechanicalModelGlobalParameters;
	}

	public void setEpisimBioMechanicalModelGlobalParameters(
			XmlObject<EpisimBiomechanicalModelGlobalParameters> episimBioMechanicalModelGlobalParameters) {
		this.episimBioMechanicalModelGlobalParameters = episimBioMechanicalModelGlobalParameters;
	}

	public XmlObject<EpisimCellBehavioralModelGlobalParameters> getEpisimCellBehavioralModelGlobalParameters() {
		return episimCellBehavioralModelGlobalParameters;
	}

	public void setEpisimCellBehavioralModelGlobalParameters(
			XmlObject<EpisimCellBehavioralModelGlobalParameters> episimCellBehavioralModelGlobalParameters) {
		this.episimCellBehavioralModelGlobalParameters = episimCellBehavioralModelGlobalParameters;
	}

	public long getSimStepNumber() {
		return simStepNumber;
	}

	public void setSimStepNumber(long simStepNumber) {
		this.simStepNumber = simStepNumber;
	}

	public XmlObject<MiscalleneousGlobalParameters> getMiscalleneousGlobalParameters() {
		return miscalleneousGlobalParameters;
	}

	public void setMiscalleneousGlobalParameters(
			XmlObject<MiscalleneousGlobalParameters> miscalleneousGlobalParameters) {
		this.miscalleneousGlobalParameters = miscalleneousGlobalParameters;
	}

	public File getLoadedModelFile() {
		return loadedModelFile;
	}

	public void setLoadedModelFile(String file) {
		this.loadedModelFile = new File(file);
	}

	public void setTissueBorder(XmlTissueBorder tissueBorder) {
		this.tissueBorder = tissueBorder;
	}

	public XmlTissueBorder getTissueBorder() {
		return tissueBorder;
	}

	public XmlExtraCellularDiffusionFieldArray getExtraCellularDiffusionFieldArray() {
		return extraCellularDiffusionFieldArray;
	}

	public void setExtraCellularDiffusionFieldArray(
			XmlExtraCellularDiffusionFieldArray extraCellularDiffusionFieldArray) {
		this.extraCellularDiffusionFieldArray = extraCellularDiffusionFieldArray;
	}
}
