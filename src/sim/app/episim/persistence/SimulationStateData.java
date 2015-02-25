package sim.app.episim.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import sim.SimStateServer;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.persistence.dataconvert.XmlEpisimCellBehavioralModel;
import sim.app.episim.persistence.dataconvert.XmlExtraCellularDiffusionFieldArray2D;
import sim.app.episim.persistence.dataconvert.XmlExtraCellularDiffusionFieldArray3D;
import sim.app.episim.persistence.dataconvert.XmlObject;
import sim.app.episim.persistence.dataconvert.XmlTissueBorder;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.tissue.TissueController;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;

/*
 * update = Objekte sammeln
 */
public class SimulationStateData {

	private ArrayList<XmlUniversalCell> cells = new ArrayList<XmlUniversalCell>();
	private XmlObject<EpisimBiomechanicalModelGlobalParameters> episimBioMechanicalModelGlobalParameters;
	private XmlObject<EpisimCellBehavioralModelGlobalParameters> episimCellBehavioralModelGlobalParameters;
	private XmlObject<MiscalleneousGlobalParameters> miscalleneousGlobalParameters;

	private XmlExtraCellularDiffusionFieldArray2D extraCellularDiffusionFieldArray2D;
	private XmlExtraCellularDiffusionFieldArray3D extraCellularDiffusionFieldArray3D;
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
				MiscalleneousGlobalParameters.getInstance());
		this.episimBioMechanicalModelGlobalParameters = new XmlObject<EpisimBiomechanicalModelGlobalParameters>(
				ModelController.getInstance()
						.getEpisimBioMechanicalModelGlobalParameters());
		if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){
			this.extraCellularDiffusionFieldArray2D = new XmlExtraCellularDiffusionFieldArray2D(
					ModelController.getInstance()
							.getExtraCellularDiffusionController()
							.getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField2D[ModelController.getInstance()
							                                                    						.getExtraCellularDiffusionController().getNumberOfFields()]));
		}
		if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
			this.extraCellularDiffusionFieldArray3D = new XmlExtraCellularDiffusionFieldArray3D(
					ModelController.getInstance()
							.getExtraCellularDiffusionController()
							.getAllExtraCellularDiffusionFields(new ExtraCellularDiffusionField3D[ModelController.getInstance()
							                                                    						.getExtraCellularDiffusionController().getNumberOfFields()]));
		}
		
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

	public XmlExtraCellularDiffusionFieldArray2D getExtraCellularDiffusionFieldArray2D() {
		return extraCellularDiffusionFieldArray2D;
	}

	public void setExtraCellularDiffusionFieldArray2D(
			XmlExtraCellularDiffusionFieldArray2D extraCellularDiffusionFieldArray) {
		this.extraCellularDiffusionFieldArray2D = extraCellularDiffusionFieldArray;
	}
	
	public XmlExtraCellularDiffusionFieldArray3D getExtraCellularDiffusionFieldArray3D() {
		return extraCellularDiffusionFieldArray3D;
	}

	public void setExtraCellularDiffusionFieldArray3D(
			XmlExtraCellularDiffusionFieldArray3D extraCellularDiffusionFieldArray) {
		this.extraCellularDiffusionFieldArray3D = extraCellularDiffusionFieldArray;
	}
}
