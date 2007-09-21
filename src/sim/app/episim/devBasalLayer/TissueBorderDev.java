package sim.app.episim.devBasalLayer;


import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TissueBorderDev {
	
	private ArrayList<Point2D> fullcontour;
	
	private HashMap<Double, HashSet<Double>> organizedXPoints;
	
	private GeneralPath polygon;
	
	private GeneralPath drawPolygon;
	
	private static final int THRESHHOLD = 5;
	
	private static  TissueBorderDev instance;
	
	private Tissue tissue;
	
	private TissueBorderDev(){
		fullcontour = new ArrayList<Point2D>();
		polygon = new GeneralPath();
		organizedXPoints = new HashMap<Double, HashSet<Double>>();
	}
	
	
	
	
	
	
	public  double getWidth(){
		
		return tissue.getEpidermalWidth();
	}
	
	public double getNumberOfPixelsPerMicrometer(){
		double resolutionMicoMPerPixel = tissue.getResolutionInMicrometerPerPixel();
		
		return  1 /resolutionMicoMPerPixel;
		
	}
	
	
	public String getTissueID(){
		return tissue.getImageid();
	}
	
	public String getTissueDescription(){
		return tissue.getTissueDescription();
	}
	public double getHeight(){
		
		//Bei der Berechnung durch GeneralPath geht ein Pixel verloren
		return polygon.getBounds().height + 1;
	}
	
	public  double lowerBound(double x)
	 {
	    return -1;
	    
	 }
	public void loadBasementMembrane(File path) {

		if(path != null){

			   tissue = TissueProfileReader.getInstance().loadTissue(path);
			if(tissue != null){
				fullcontour = new ArrayList<Point2D>();
				fullcontour.addAll(tissue.getBasalLayerPoints());
				ArrayList<Point2D> surface = tissue.getSurfacePoints();
				for(int i = surface.size()-1; i >= 0 ; i--) fullcontour.add(surface.get(i));
			}
			}
			
			if(this.fullcontour.size() > 0){

				polygon = new GeneralPath();
				polygon.moveTo(this.fullcontour.get(0).getX(), this.fullcontour.get(0).getY());
				for(int i = 0; i < this.fullcontour.size(); i++){

					polygon.lineTo(this.fullcontour.get(i).getX(), this.fullcontour.get(i).getY());

				}
				drawPolygon = (GeneralPath)polygon.clone();
				drawPolygon.closePath();
				polygon.lineTo(polygon.getBounds().getMinX(), polygon.getBounds().getMinY());
				polygon.closePath();
				
			//	organizePoints();
			}
			
		}

	
	
	public static synchronized TissueBorderDev getInstance(){
		if(instance == null) instance =  new TissueBorderDev();
		return instance;
	}
	
	public GeneralPath getBasementMembraneDrawPolygon(){
		return drawPolygon;
		
	}
	
	public boolean isOverBasalLayer(Point2D point){
		if(polygon != null && (polygon.contains(point) ||
				(point.getX() >= polygon.getBounds().getMinX() && point.getX()<= polygon.getBounds().getMaxX()
						&& point.getY() >=0 && point.getY() <= polygon.getBounds().getMinY())))return true;
		else return false;
	}
		      
	private void organizePoints(){
		for(Point2D actPoint: fullcontour){
			if(organizedXPoints.containsKey(actPoint.getX()))
				organizedXPoints.get(actPoint.getX()).add(actPoint.getY());
			else{
				HashSet<Double> tmp =new HashSet<Double>();
				tmp.add(actPoint.getY());
				organizedXPoints.put(actPoint.getX(), tmp);
			}
		}
		
	}
		
	public Set<Double> getYCoordinateForXCoordinate(double x){
		
		if(organizedXPoints.containsKey(x)) return organizedXPoints.get(x);
		else return new HashSet<Double>();
	}
	/**
	 * Methode, die die Höhe berechnet
	 */
	private void calculateheight(){
	int maxY = 0;
	int minY = 0;
	 for(Point2D point :tissue.getBasalLayerPoints()) if(point.getY() > maxY) maxY = (int)point.getY();
	 for(Point2D point :tissue.getSurfacePoints()) if(point.getY() < minY) minY = (int)point.getY();
	 System.out.println("Die berechnete Höhe ist: " + (maxY - minY));
	}
	
	/**
	 * Methode, die die Höhe berechnet
	 */
	private void calculateWidth(){
	int maxX = 0;
	int minX = 0;
	 for(Point2D point :tissue.getBasalLayerPoints()){
		 if(point.getX() > maxX) maxX = (int)point.getX();
		 else if(point.getX() < minX) minX = (int)point.getX();
	 }
	 for(Point2D point :tissue.getSurfacePoints()){
		 if(point.getX() > maxX) maxX = (int)point.getX();
		 else if(point.getX() < minX) minX = (int)point.getX();
	 }
	 System.out.println("Die berechnete Breite ist: " + (maxX - minX));
	 System.out.println("Die Höhe, die Thora berechnet hat, ist: " + tissue.getEpidermalWidth());
	}
	
}