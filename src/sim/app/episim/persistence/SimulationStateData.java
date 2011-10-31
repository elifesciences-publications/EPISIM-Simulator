package sim.app.episim.persistence;

import java.awt.geom.Rectangle2D.Double;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpidermisSimulator;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.app.episim.tissue.TissueController;
import sim.engine.SimStateHack.TimeSteps;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/*
 * update = Objekte sammeln
 */
public class SimulationStateData {

	private ArrayList<XmlUniversalCell> cells = new ArrayList<XmlUniversalCell>();
	private Continuous2D cellContinuous;
	private TimeSteps timeSteps;
	private EpisimBiomechanicalModelGlobalParameters episimBioMechanicalModelGlobalParameters;
	private EpisimCellBehavioralModelGlobalParameters episimCellBehavioralModelGlobalParameters;
	private List<Double2D> woundRegionCoordinates;
	private Double[] deltaInfo;
	private MiscalleneousGlobalParameters miscalleneousGlobalParameters;
	private File loadedModelFile;
	
	public HashMap<Long, UniversalCell> alreadyLoadedCells = new HashMap<Long, UniversalCell>();
	public HashMap<Long, XmlUniversalCell> alreadyLoadedXmlCellsNewID = new HashMap<Long, XmlUniversalCell>();
	public HashMap<Long, XmlUniversalCell> cellsToBeLoaded = new HashMap<Long, XmlUniversalCell>();

	public SimulationStateData() {
		reset();
	}
	
	public void addLoadedCell(XmlUniversalCell xmlCell, UniversalCell loadedCell){
		alreadyLoadedCells.put(xmlCell.getId(), loadedCell);
		alreadyLoadedXmlCellsNewID.put(loadedCell.getID(),xmlCell);
	}

	public void reset() {
		cells = new ArrayList<XmlUniversalCell>();
		cellContinuous = null;
		timeSteps = null;
		episimBioMechanicalModelGlobalParameters = null;
		episimCellBehavioralModelGlobalParameters = null;
		woundRegionCoordinates = null;
		deltaInfo = null;
		miscalleneousGlobalParameters = null;
		loadedModelFile = null;
	}

	public void updateData() {
//		for (SnapshotListener listener : listeners) {
//			if (listener instanceof EpidermisSimulator) {
//				loadedModelFile = ((EpidermisSimulator) listener).getActLoadedJarFile();
//			}
//			for (SnapshotObject object : listener.collectSnapshotObjects()) {
//				if (object.getIdentifier().equals(SnapshotObject.CELL)) {
//					cells.add(new XmlUniversalCell(object.getSnapshotObject()));
//				} else if (object.getIdentifier().equals(SnapshotObject.CELLFIELD)) {
//					this.cellContinuous = (Continuous2D) object.getSnapshotObject();
//
//				} else if (object.getIdentifier().equals(SnapshotObject.TIMESTEPS)) {
//					this.timeSteps = (TimeSteps) object.getSnapshotObject();
//
//				} else if (object.getIdentifier().equals(SnapshotObject.CELLBEHAVIORALMODELGLOBALPARAMETERS)) {
//					episimCellBehavioralModelGlobalParameters = (EpisimCellBehavioralModelGlobalParameters) object.getSnapshotObject();
//
//				} else if (object.getIdentifier().equals(SnapshotObject.MECHANICALMODELGLOBALPARAMETERS)) {
//					episimBioMechanicalModelGlobalParameters = (EpisimBiomechanicalModelGlobalParameters) object.getSnapshotObject();
//
//				} else if (object.getIdentifier().equals(SnapshotObject.WOUND)) {
//					Object obj = null;
//					if ((obj = object.getSnapshotObject()) instanceof List)
//						woundRegionCoordinates = (List<Double2D>) obj;
//					else
//						deltaInfo = (java.awt.geom.Rectangle2D.Double[]) object.getSnapshotObject();
//
//				}
//
//			}
//		}
		
		this.loadedModelFile = ModelController.getInstance().getCellBehavioralModelController().getActLoadedModelFile();
		for(AbstractCell cell : TissueController.getInstance().getActEpidermalTissue().getAllCells()){
			cells.add(new XmlUniversalCell(cell));
		}

		this.miscalleneousGlobalParameters = MiscalleneousGlobalParameters.instance();
		this.episimBioMechanicalModelGlobalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	}

	public void restoreData() {
		// SnapshotLoader bzw. EpidermisSimulator.loadSnapshot!
		// TODO ObjectManipulations.resetInitialGlobalValues
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

	public Continuous2D getCellContinuous() {
		return cellContinuous;
	}

	public void setCellContinuous(Continuous2D cellContinuous) {
		this.cellContinuous = cellContinuous;
	}

	public TimeSteps getTimeSteps() {
		return timeSteps;
	}

	public void setTimeSteps(TimeSteps timeSteps) {
		this.timeSteps = timeSteps;
	}

	public EpisimBiomechanicalModelGlobalParameters getEpisimBioMechanicalModelGlobalParameters() {
		return episimBioMechanicalModelGlobalParameters;
	}

	public void setEpisimBioMechanicalModelGlobalParameters(EpisimBiomechanicalModelGlobalParameters episimBioMechanicalModelGlobalParameters) {
		this.episimBioMechanicalModelGlobalParameters = episimBioMechanicalModelGlobalParameters;
	}

	public EpisimCellBehavioralModelGlobalParameters getEpisimCellBehavioralModelGlobalParameters() {
		return episimCellBehavioralModelGlobalParameters;
	}

	public void setEpisimCellBehavioralModelGlobalParameters(EpisimCellBehavioralModelGlobalParameters episimCellBehavioralModelGlobalParameters) {
		this.episimCellBehavioralModelGlobalParameters = episimCellBehavioralModelGlobalParameters;
	}

	public List<Double2D> getWoundRegionCoordinates() {
		return woundRegionCoordinates;
	}

	public void setWoundRegionCoordinates(List<Double2D> woundRegionCoordinates) {
		this.woundRegionCoordinates = woundRegionCoordinates;
	}

	public Double[] getDeltaInfo() {
		return deltaInfo;
	}

	public void setDeltaInfo(Double[] deltaInfo) {
		this.deltaInfo = deltaInfo;
	}

	public MiscalleneousGlobalParameters getMiscalleneousGlobalParameters() {
		return miscalleneousGlobalParameters;
	}

	public void setMiscalleneousGlobalParameters(MiscalleneousGlobalParameters miscalleneousGlobalParameters) {
		this.miscalleneousGlobalParameters = miscalleneousGlobalParameters;
	}

	public File getLoadedModelFile() {
		return loadedModelFile;
	}

	public void setLoadedModelFile(String file) {
		this.loadedModelFile = new File(file);
	}
}
