package sim.app.episim.devBasalLayer;


import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;


public class TissueBorderDev {
	
	private ArrayList<Point2D> fullcontour;
	
	private HashMap<Double, TreeSet<Double>> organizedXPoints;
	
	private GeneralPath polygon;
	
	private GeneralPath drawPolygon;
	
	private static final int THRESHHOLD = 5;
	
	private static  TissueBorderDev instance;
	
	private LoadedTissue tissue;
	
	private TissueBorderDev(){
		fullcontour = new ArrayList<Point2D>();
		polygon = new GeneralPath();
		organizedXPoints = new HashMap<Double, TreeSet<Double>>();
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
				
				organizeBasalLayerPoints();
			}
			
		}

	
	
	public static synchronized TissueBorderDev getInstance(){
		if(instance == null) instance =  new TissueBorderDev();
		return instance;
	}
	
	public GeneralPath getBasementMembraneDrawPolygon(){
		return (GeneralPath)drawPolygon.clone();
		
	}
	Point2D previousPoint = null;
	
	public boolean isOverBasalLayer(Point2D point){
		boolean result = false;
		if((previousPoint !=null&& previousPoint.getX() != point.getX()&&
				previousPoint.getY() != point.getY())|| previousPoint ==null)System.out.println("Point: "+ new Double((int)point.getX())+ ", "+ point.getY());
		if(organizedXPoints.containsKey(new Double((int)point.getX()))){
			
			TreeSet<Double> yPoints = organizedXPoints.get(new Double((int)point.getX()));
			NavigableSet<Double> yPointsSorted = yPoints.descendingSet();
			Double first =yPointsSorted.first();
			if((previousPoint !=null&& previousPoint.getX() != point.getX()&&
					previousPoint.getY() != point.getY())|| previousPoint ==null){
			
				
				Iterator<Double> iter =yPoints.descendingIterator();
				System.out.print("Y-Points: ");
				while(iter.hasNext()) System.out.print(iter.next() +", ");
				System.out.println();
				
			}
			
			
			 if(first != null){
				 Double next = yPointsSorted.higher(first);
				 
				 while(result != true && next !=null && first != null){
				  
					 if(point.getY() <= first.doubleValue() && point.getY() >= next.doubleValue()) result = true;
					
					 
					 if((previousPoint !=null&& previousPoint.getX() != point.getX()&&
								previousPoint.getY() != point.getY())|| previousPoint ==null){
						 System.out.println("First : "+ first);
						 System.out.println("Next : "+ next);
						 System.out.println("Result : "+ result);
						 System.out.println();
					 }
					 // jeder wird nur einmal zur Bildung eines Wertpaares herangezogen, damit Schlaufen korrekt behandelt werden
					 first = yPointsSorted.higher(next);
					 if(first != null)next = yPointsSorted.higher(first);
					 else{ 
						 if(point.getY() <= next.doubleValue()) result = true;
						 next = null;
					 }
				 }
				 if(next == null && first != null && result == false && point.getY() <= first.doubleValue()){ 
					 result = true;
					 
					 if((previousPoint !=null&& previousPoint.getX() != point.getX()&&
								previousPoint.getY() != point.getY())|| previousPoint ==null){
						 System.out.println("First : "+ first);
						 System.out.println("Result : "+ result);
						 System.out.println();
					 }
				 }
		
			 }	 
		}
		else System.out.println("X-Wert liegt nicht drin: "+new Double((int)point.getX()));
		previousPoint = point;
		
		return result;
	}
		      
	private void organizeBasalLayerPoints(){
		for(Point2D actPoint: tissue.getBasalLayerPoints()){
			if(organizedXPoints.containsKey(actPoint.getX()))
				organizedXPoints.get(actPoint.getX()).add(actPoint.getY());
			else{
				TreeSet<Double> tmp =new TreeSet<Double>();
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