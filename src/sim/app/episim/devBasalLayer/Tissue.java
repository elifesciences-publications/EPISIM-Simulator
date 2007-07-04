package sim.app.episim.devBasalLayer;

import java.awt.geom.Point2D;
import java.util.ArrayList;


public class Tissue {
	
	private ArrayList<Point2D> basalLayerPoints;
	private ArrayList<Point2D> surfacePoints;
	private double resolution;
	private String imageid;
	private String tissueDescription;
	private double epidermalWidth;
	private double meanEpidermalWidth;
	private double maximumEpidermalWidth;
	
	public Tissue(ArrayList<Point2D> basalLayerPoints, ArrayList<Point2D> surfacePoints, double resolution, String imageid, String tissueDescription, 
			double epidermalWidth, double meanEpidermalWidth, double maximumEpidermalWidth){
		this.basalLayerPoints = basalLayerPoints;
		this.surfacePoints = surfacePoints;
		this.resolution = resolution;
		this.imageid = imageid;
		this.tissueDescription = tissueDescription;
		this.epidermalWidth = epidermalWidth;
		this.meanEpidermalWidth = meanEpidermalWidth;
		this.maximumEpidermalWidth = maximumEpidermalWidth;
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
	
	public double getMeanEpidermalWidth() {
	
		return meanEpidermalWidth;
	}
	
	public void setMeanEpidermalWidth(double meanEpidermalWidth) {
	
		this.meanEpidermalWidth = meanEpidermalWidth;
	}
	
	public double getMaximumEpidermalWidth() {
	
		return maximumEpidermalWidth;
	}
	
	public void setMaximumEpidermalWidth(double maximumEpidermalWidth) {
	
		this.maximumEpidermalWidth = maximumEpidermalWidth;
	}
	
	
	

}
