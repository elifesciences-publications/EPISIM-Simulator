package sim.app.episim.persistence;

import java.awt.geom.Rectangle2D.Double;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.snapshot.SnapshotListener;
import sim.app.episim.snapshot.SnapshotObject;
import sim.engine.SimStateHack.TimeSteps;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;

/*
 * update = Objekte sammeln
 * restoreData = Objekte verteilen
 */
public class SimulationStateData {

	private static SimulationStateData instance = null;

	private List<SnapshotListener> listeners;

	private ArrayList<UniversalCell> cells = new ArrayList<UniversalCell>();
	private Continuous2D cellContinuous;
	private TimeSteps timeSteps;
	private EpisimBiomechanicalModelGlobalParameters episimBioMechanicalModelGlobalParameters;
	private EpisimCellBehavioralModelGlobalParameters episimCellBehavioralModelGlobalParameters;
	private List<Double2D> woundRegionCoordinates;
	private Double[] deltaInfo;
	private MiscalleneousGlobalParameters miscalleneousGlobalParameters;

	private SimulationStateData() {
		listeners = new LinkedList<SnapshotListener>();
	}

	public static synchronized SimulationStateData getInstance() {
		if (instance == null) {
			instance = new SimulationStateData();
		}
		return instance;
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void addSnapshotListener(SnapshotListener listener) {
		listeners.add(listener);
	}

	public void updateData() {
		for (SnapshotListener listener : listeners) {
			for (SnapshotObject object : listener.collectSnapshotObjects()) {
				if (object.getIdentifier().equals(SnapshotObject.CELL)) {
					cells.add((UniversalCell) object.getSnapshotObject());
				} else if (object.getIdentifier().equals(SnapshotObject.CELLFIELD)) {
					this.cellContinuous = (Continuous2D) object.getSnapshotObject();

				} else if (object.getIdentifier().equals(SnapshotObject.TIMESTEPS)) {
					this.timeSteps = (TimeSteps) object.getSnapshotObject();

				} else if (object.getIdentifier().equals(SnapshotObject.CELLBEHAVIORALMODELGLOBALPARAMETERS)) {
					episimCellBehavioralModelGlobalParameters = (EpisimCellBehavioralModelGlobalParameters) object.getSnapshotObject();

				} else if (object.getIdentifier().equals(SnapshotObject.MECHANICALMODELGLOBALPARAMETERS)) {
					episimBioMechanicalModelGlobalParameters = (EpisimBiomechanicalModelGlobalParameters) object.getSnapshotObject();

				} else if (object.getIdentifier().equals(SnapshotObject.WOUND)) {
					Object obj = null;
					if ((obj = object.getSnapshotObject()) instanceof List)
						woundRegionCoordinates = (List<Double2D>) obj;
					else
						deltaInfo = (java.awt.geom.Rectangle2D.Double[]) object.getSnapshotObject();

				}

			}
		}
		this.miscalleneousGlobalParameters = MiscalleneousGlobalParameters.instance();
		this.episimBioMechanicalModelGlobalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	}

	public void restoreData() {
//		SnapshotLoader bzw. EpidermisSimulator.loadSnapshot!
		// TODO ObjectManipulations.resetInitialGlobalValues
	}

	public ArrayList<UniversalCell> getCells() {
		return cells;
	}

	public void setCells(ArrayList<UniversalCell> cells) {
		this.cells = cells;
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
}
