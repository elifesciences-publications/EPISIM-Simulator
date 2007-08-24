package sim.app.episim.devBasalLayer;

import java.awt.geom.Point2D;
import java.util.ArrayList;


public class Tissue implements java.io.Serializable {
	
	private ArrayList<Point2D> basalLayerPoints;
	private ArrayList<Point2D> surfacePoints;
	private double resolution;
	private String imageid;
	private String tissueDescription;
	private double epidermalWidth;
	private double meanEpidermalThickness;
	private double maximumEpidermalThickness;
	
	public Tissue(ArrayList<Point2D> basalLayerPoints, ArrayList<Point2D> surfacePoints, double resolution, String imageid, String tissueDescription, 
			double epidermalWidth, double meanEpidermalThickness, double maximumEpidermalThickness){
		this.basalLayerPoints = basalLayerPoints;
		this.surfacePoints = surfacePoints;
		this.resolution = resolution;
		this.imageid = imageid;
		this.tissueDescription = tissueDescription;
		this.epidermalWidth = epidermalWidth;
		this.meanEpidermalThickness = meanEpidermalThickness;
		this.maximumEpidermalThickness = maximumEpidermalThickness;
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
	
	public double getResolution() {
	
		return resolution;
	}
	
	public void setResolution(double resolution) {
	
		this.resolution = resolution;
	}
	
	public String getImageid() {
	
		return imageid;
	}
	
	public void setImageid(String imageid) {
	
		this.imageid = imageid;
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
	
	
	

}
