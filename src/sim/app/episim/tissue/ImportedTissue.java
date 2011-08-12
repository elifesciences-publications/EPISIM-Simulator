package sim.app.episim.tissue;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import sim.app.episim.visualization.CellEllipse;


public class ImportedTissue implements java.io.Serializable {
	
	private ArrayList<Point2D> basalLayerPoints;
	private ArrayList<Point2D> surfacePoints;
	private ArrayList<CellEllipse> cells;
	private double resolution;
	private String tissueImageid = "";
	private String tissueDescription = "no description";
	private double epidermalWidth;
	private double epidermalHeight;
	private double meanEpidermalThickness;
	private double maximumEpidermalThickness;
	private double scalingFactor = 1;	
	
	public ImportedTissue(){
		
	}
	
	
	public ArrayList<Point2D> getBasalLayerPoints() { return  basalLayerPoints; }
	
	public void setBasalLayerPoints(ArrayList<Point2D> basalLayerPoints) { this.basalLayerPoints = basalLayerPoints; }
	
	public ArrayList<Point2D> getSurfacePoints() { return surfacePoints; }
	
	public void setSurfacePoints(ArrayList<Point2D> surfacePoints){ this.surfacePoints = surfacePoints; }
	
	public double getResolutionInMicrometerPerPixel() { return resolution; }
	
	public void setResolutionInMicrometerPerPixel(double resolution) { this.resolution = resolution; }
	
	public String getTissueImageID() { return tissueImageid; }
	
	public void setTissueImageID(String tissueImageid) { this.tissueImageid = tissueImageid; }
	
	public String getTissueDescription() { return tissueDescription; }
	
	public void setTissueDescription(String tissueDescription) { this.tissueDescription = tissueDescription;	}
	
	public double getEpidermalWidth() { return epidermalWidth; }
	
	public void setEpidermalWidth(double epidermalWidth) { this.epidermalWidth = epidermalWidth;	}
	
	public double getMeanEpidermalThickness() { return meanEpidermalThickness;	}
	
	public void setMeanEpidermalThickness(double meanEpidermalWidth) { this.meanEpidermalThickness = meanEpidermalWidth; }
	
	public double getMaximumEpidermalThickness() { return maximumEpidermalThickness;	}
	
	public void setMaximumEpidermalThickness(double maximumEpidermalWidth) { this.maximumEpidermalThickness = maximumEpidermalWidth;	}
	
   public double getEpidermalHeight() { return epidermalHeight; }
	
   public void setEpidermalHeight(double epidermalHeight) { this.epidermalHeight = epidermalHeight; }
	
   public double getScalingFactor() { return scalingFactor; }
	
   public void setScalingFactor(double scalingFactor) { this.scalingFactor = scalingFactor; }
	
   public ArrayList<CellEllipse> getCells() { return cells; }

   public void setCells(ArrayList<CellEllipse> cells) { this.cells = cells; }
	
}
