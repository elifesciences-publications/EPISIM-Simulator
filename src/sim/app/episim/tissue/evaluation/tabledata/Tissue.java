package sim.app.episim.tissue.evaluation.tabledata;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import sim.app.episim.tissue.xmlread.ImportedCellData;
import sim.app.episim.tissue.xmlread.ImportedNucleusData;
import sim.app.episim.tissue.xmlread.ImportedTissueData;

public class Tissue {
	private ArrayList<Point2D> basalLayerPoints;
	private ArrayList<Point2D> surfacePoints;
	private ArrayList<Cell> cells;
	private ArrayList<Nucleus> nuclei;
	private double micrometerPerPixel;
	private String tissueImageid = "";
	private String tissueDescription = "no description";
	private double epidermalWidth;
	private double epidermalHeight;
	private double meanEpidermalThickness;
	private double maximumEpidermalThickness;
	private double scalingFactor = 1;

	public Tissue(ImportedTissueData tissueData) {
		this.basalLayerPoints = tissueData.getBasalLayerPoints();
		this.surfacePoints = tissueData.getSurfacePoints();
		this.micrometerPerPixel = tissueData.getResolutionInMicrometerPerPixel();
		this.tissueImageid = tissueData.getTissueImageID();
		this.tissueDescription = tissueData.getTissueDescription();
		this.epidermalWidth = tissueData.getEpidermalWidth();
		this.epidermalHeight = tissueData.getEpidermalHeight();
		this.meanEpidermalThickness = tissueData.getMeanEpidermalThickness();
		this.maximumEpidermalThickness = tissueData.getMaximumEpidermalThickness();
		this.scalingFactor = tissueData.getScalingFactor();
		this.nuclei = new ArrayList<Nucleus>();
		for(ImportedNucleusData nucleiData : tissueData.getNuclei()){
			this.nuclei.add(new Nucleus(nucleiData,micrometerPerPixel));
		}
		
		this.cells = new ArrayList<Cell>();
		for(ImportedCellData cellData : tissueData.getCells()){
			this.cells.add(new Cell(cellData,micrometerPerPixel, nuclei));
		}
	}



	public ArrayList<Point2D> getBasalLayerPoints() {
		return basalLayerPoints;
	}

	public void setBasalLayerPoints(ArrayList<Point2D> basalLayerPoints) {
		this.basalLayerPoints = basalLayerPoints;
	}

	public ArrayList<Point2D> getSurfacePoints() {
		return surfacePoints;
	}

	public void setSurfacePoints(ArrayList<Point2D> surfacePoints) {
		this.surfacePoints = surfacePoints;
	}

	public double getResolutionInMicrometerPerPixel() {
		return micrometerPerPixel;
	}

	public void setResolutionInMicrometerPerPixel(double resolution) {
		this.micrometerPerPixel = resolution;
	}

	public String getTissueImageID() {
		return tissueImageid;
	}

	public void setTissueImageID(String tissueImageid) {
		this.tissueImageid = tissueImageid;
	}

	public String getTissueDescription() {
		return tissueDescription;
	}

	public void setTissueDescription(String tissueDescription) {
		this.tissueDescription = tissueDescription;
	}

	public double getEpidermalWidth() {
		return epidermalWidth;
	}

	public void setEpidermalWidth(double epidermalWidth) {
		this.epidermalWidth = epidermalWidth;
	}

	public double getMeanEpidermalThickness() {
		return meanEpidermalThickness;
	}

	public void setMeanEpidermalThickness(double meanEpidermalWidth) {
		this.meanEpidermalThickness = meanEpidermalWidth;
	}

	public double getMaximumEpidermalThickness() {
		return maximumEpidermalThickness;
	}

	public void setMaximumEpidermalThickness(double maximumEpidermalWidth) {
		this.maximumEpidermalThickness = maximumEpidermalWidth;
	}

	public double getEpidermalHeight() {
		return epidermalHeight;
	}

	public void setEpidermalHeight(double epidermalHeight) {
		this.epidermalHeight = epidermalHeight;
	}

	public double getScalingFactor() {
		return scalingFactor;
	}

	public void setScalingFactor(double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}

	public ArrayList<Cell> getCells() {
		return cells;
	}

	public void setCells(ArrayList<Cell> cells) {
		this.cells = cells;
	}

}
