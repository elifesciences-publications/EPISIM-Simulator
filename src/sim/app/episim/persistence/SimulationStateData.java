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
import sim.app.episim.persistence.dataconvert.XmlObject;
import sim.app.episim.persistence.dataconvert.XmlUniversalCell;
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
	private XmlObject<EpisimBiomechanicalModelGlobalParameters> episimBioMechanicalModelGlobalParameters;
	private XmlObject<EpisimCellBehavioralModelGlobalParameters> episimCellBehavioralModelGlobalParameters;
	private List<Double2D> woundRegionCoordinates;
	private Double[] deltaInfo;
	private XmlObject<MiscalleneousGlobalParameters> miscalleneousGlobalParameters;
	private File loadedModelFile;

	private HashMap<Long, UniversalCell> alreadyLoadedCells = new HashMap<Long, UniversalCell>();
	private HashMap<Long, XmlUniversalCell> alreadyLoadedXmlCellsNewID = new HashMap<Long, XmlUniversalCell>();
	private HashMap<Long, XmlUniversalCell> cellsToBeLoaded = new HashMap<Long, XmlUniversalCell>();
	
	private static File tissueExportPath;
	private static SimulationStateData lastSimulationStateLoaded;

	public SimulationStateData() {
		reset();
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
	
   public static void setTissueExportPath(File tissueExportPath){
	   SimulationStateData.tissueExportPath = tissueExportPath;
   }   
   public static File getTissueExportPath() {
	   return tissueExportPath;
   }   
   public static SimulationStateData getLastSimulationStateLoaded(){
	   return lastSimulationStateLoaded;
   }

	public void addLoadedCell(XmlUniversalCell xmlCell, UniversalCell loadedCell) {
		alreadyLoadedCells.put(xmlCell.getId(), loadedCell);
		alreadyLoadedXmlCellsNewID.put(loadedCell.getID(), xmlCell);
	}
	
	public void clearLoadedCells(){
		alreadyLoadedXmlCellsNewID.clear();
		alreadyLoadedCells.clear();
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

		this.loadedModelFile = ModelController.getInstance()
				.getCellBehavioralModelController().getActLoadedModelFile();
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
}
