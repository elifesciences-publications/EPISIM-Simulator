package sim.app.episim.tissue;


import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import episiminterfaces.EpisimMechanicalModelGlobalParameters;


import sim.app.episim.model.CellBehavioralModelController;
import sim.app.episim.model.ModelController;
public class TissueBorder {
	
	private ArrayList<Point2D> fullcontour;
	
	private HashMap<Double, TreeSet<Double>> organizedXPoints;
	
	private GeneralPath polygon;
	private GeneralPath surface;
	private GeneralPath basalLayer;
	
	private GeneralPath drawPolygon;	
	private GeneralPath drawSurface;
	private GeneralPath drawBasalLayer;
	
	private static final int THRESHHOLD = 5;
	
	private static  TissueBorder instance;
	
	private static int basalY=58;          // y coordinate at which undulations start, the base line    
	private static int basalPeriod=70;      // width of an undulation at the foot
	private static int startXOfStandardMembrane = 0;
	
	private static  EpisimMechanicalModelGlobalParameters globalParameters;  
	
	private ImportedTissue tissue;
	
	private boolean standardMembraneLoaded = false;
	
	private final double STANDARDHEIGHT = 100;
	
	private TissueBorder(){
		fullcontour = new ArrayList<Point2D>();
		polygon = new GeneralPath();
		organizedXPoints = new HashMap<Double, TreeSet<Double>>();
	}
	
	public int getUndulationBaseLine(){
		return basalY;
	}
	
	public void setUndulationBaseLine(int _basalY){
		basalY = _basalY;
	}
	
	public void setBasalPeriod(int period){
		basalPeriod = period;
	}
	
	public void setStartXOfStandardMembrane(int start){ startXOfStandardMembrane = start; }
	
	public boolean isStandardMembraneLoaded() { return this.standardMembraneLoaded;}
	
	
	public  double getWidth(){
		if(standardMembraneLoaded){
			if(globalParameters == null) globalParameters = ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters(); 
			return globalParameters.getWidth();
		}
		else{
			return tissue.getEpidermalWidth();
		}
	}
	
	public double getNumberOfPixelsPerMicrometer(){
		if(standardMembraneLoaded){
			return 1;
		}
		else{
			double resolutionMicoMPerPixel = tissue.getResolutionInMicrometerPerPixel();
			
			return  1 /resolutionMicoMPerPixel;
		}
	}
	
	
	public String getTissueID(){
		if(standardMembraneLoaded){
			return "";
		}
		else{
			return tissue.getTissueImageID();
		}
	}
	
	public String getTissueDescription(){
		if(standardMembraneLoaded){
			return "";
		}
		else{
			return tissue.getTissueDescription();
		}
	}
	public double getHeight(){
		if(standardMembraneLoaded){
			return this.STANDARDHEIGHT;
		}
		else{
			//Bei der Berechnung durch GeneralPath geht ein Pixel verloren
			return polygon.getBounds().height + 1;
		}
	}
	
	public double lowerBound(double x)
	 {
	
		if(globalParameters == null) globalParameters = ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters(); 
		// y = a * e ^ (-b * x * x) Gaussche Glockenkurve
	     double p=basalPeriod; 
	     
	     double partition=x-(int)(x/p)*p - p/2; // alle 10 einen buckel 5=10/2        
	     double v=Math.exp(-partition*partition/globalParameters.getBasalOpening_µm());
	     //System.out.println("x:"+x+" p:"+partition+" v:"+v+" Av:"+basalAmplitude*v);
	     return basalY+globalParameters.getBasalAmplitude_µm()*v;        
	 }
	
	
	public void setImportedTissueBorder(ImportedTissue _tissue) {
		standardMembraneLoaded = false;
		if(_tissue != null){
			
			   tissue = _tissue;
			
				fullcontour = new ArrayList<Point2D>();
				fullcontour.addAll(tissue.getBasalLayerPoints());
				ArrayList<Point2D> surface = tissue.getSurfacePoints();
				
				for(int i = surface.size()-1; i >= 0 ; i--) fullcontour.add(surface.get(i));
				
				
			}
			
			if(this.fullcontour.size() > 0){

				polygon = new GeneralPath();
				polygon.moveTo(this.fullcontour.get(0).getX(), this.fullcontour.get(0).getY());
				for(int i = 0; i < this.fullcontour.size(); i++){

					polygon.lineTo(this.fullcontour.get(i).getX(), this.fullcontour.get(i).getY());

				}
				
				//polygon.closePath();
				drawPolygon = (GeneralPath)polygon.clone();
				drawPolygon.closePath();
				polygon.lineTo(polygon.getBounds().getMinX(), polygon.getBounds().getMinY());
				
				basalLayer = new GeneralPath();
				basalLayer.moveTo(tissue.getBasalLayerPoints().get(0).getX(), tissue.getBasalLayerPoints().get(0).getY());
				for(Point2D p : tissue.getBasalLayerPoints())basalLayer.lineTo(p.getX(), p.getY());
				drawBasalLayer = (GeneralPath)basalLayer.clone();
				
				surface = new GeneralPath();
				surface.moveTo(tissue.getSurfacePoints().get(0).getX(), tissue.getSurfacePoints().get(0).getY());
				for(Point2D p : tissue.getSurfacePoints())surface.lineTo(p.getX(), p.getY());
				drawSurface = (GeneralPath)surface.clone();
				
				
				//polygon.closePath();
				
				organizeBasalLayerPoints();
			}
			
		}
	
	public void loadStandardMebrane(){
		standardMembraneLoaded = true;
		GeneralPath polygon = new GeneralPath();
	 		final int STEPSIZE = 1;
	 		((GeneralPath)polygon).moveTo(startXOfStandardMembrane, lowerBound(startXOfStandardMembrane));
	 		for(double i = startXOfStandardMembrane; i <= (startXOfStandardMembrane+getWidth()); i += STEPSIZE){
	 		((GeneralPath)polygon).lineTo(i, lowerBound(i));
	 		}
	 		this.polygon = polygon;
	 		drawPolygon = (GeneralPath)polygon.clone();
	}	
	
	protected static synchronized TissueBorder getInstance(){
		if(instance == null) instance =  new TissueBorder();
		return instance;
	}
	
	public GeneralPath getFullContourDrawPolygon(){
		return (GeneralPath)drawPolygon.clone();		
	}
	
	public GeneralPath getSurfaceDrawPolygon(){
		return (GeneralPath)drawSurface.clone();		
	}
	
	public GeneralPath getBasalLayerDrawPolygon(){
		return (GeneralPath)drawBasalLayer.clone();		
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

