package sim.app.episim.tissue;


import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.util.PointSorter;

public class TissueBorder {
	
	private ArrayList<Point2D> fullcontour;
	
	private HashMap<Double, TreeSet<Double>> organizedXPoints;
	
	private GeneralPath polygon;
	private GeneralPath surface;
	private GeneralPath basalLayer;
	
	private GeneralPath drawPolygon;	
	private GeneralPath drawSurface;
	private GeneralPath drawBasalLayer;
	
	
	
	private static  TissueBorder instance;
	
	private int basalY=58;          // y coordinate at which undulations start, the base line    
	private int basalPeriod=70;      // width of an undulation at the foot
	private int startXOfStandardMembrane = 0;
	
	
	
	private static EpisimBiomechanicalModelGlobalParameters globalParameters;	
	private ImportedTissue tissue;	
	private boolean standardMembraneLoaded = false;	
	private boolean noMembraneLoaded = false;
	
	private TissueBorder(){		
		resetTissueBorderSettings();
	}
	
	public void resetTissueBorderSettings(){
		fullcontour = new ArrayList<Point2D>();
		
		polygon = new GeneralPath();
		surface = new GeneralPath();
		basalLayer = new GeneralPath();
		
		drawPolygon = new GeneralPath();
		drawSurface = new GeneralPath();
		drawBasalLayer = new GeneralPath();
		
		organizedXPoints = new HashMap<Double, TreeSet<Double>>();
		
		tissue = null;
		globalParameters = null;
		standardMembraneLoaded = false;	
		noMembraneLoaded = false;
		
		basalY=58;
		basalPeriod=70;
		startXOfStandardMembrane = 0;
	}
	
	
	public int getUndulationBaseLine(){
		return basalY;
	}
	
	public void setUndulationBaseLineInMikron(int _basalY){
		basalY = _basalY;
	}
	
	public void setBasalPeriodInMikron(int period){
		basalPeriod = period;
	}
	
	public int getBasalPeriodInMikron(){
		return basalPeriod;
	}
	
	public void setStartXOfStandardMembraneInMikron(int start){ startXOfStandardMembrane = start; }
	
	@NoExport
	public boolean isStandardMembraneLoaded() { return this.standardMembraneLoaded;}
	
	@NoExport
	public boolean isNoMembraneLoaded() { return this.noMembraneLoaded;}
	
	public void loadNoMembrane() { this.noMembraneLoaded = true;}	
	
	
	@NoExport
	public double getNumberOfPixelsPerMicrometer(){
		if(standardMembraneLoaded || noMembraneLoaded){
			return ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNumberOfPixelsPerMicrometer();
		}
		else{
			double resolutionMicoMPerPixel = tissue != null ? tissue.getResolutionInMicrometerPerPixel() : 1;
			
			return  1 /resolutionMicoMPerPixel;
		}
	}
	
	
	public String getTissueID(){
		if(standardMembraneLoaded || noMembraneLoaded){
			return "";
		}
		else{
			return tissue.getTissueImageID();
		}
	}
	
	public String getTissueDescription(){
		if(standardMembraneLoaded || noMembraneLoaded){
			return "";
		}
		else{
			return tissue.getTissueDescription();
		}
	}
	@NoExport
	public double getHeightInPixels(){
		return getHeight(true);
	}
	@NoExport
	public double getHeightInMikron(){
		return getHeight(false);
	}
	@NoExport
	public double getWidthInPixels(){
		return getWidth(true);
	}
	@NoExport
	public double getWidthInMikron(){
		return getWidth(false);
	}
	
	private double getHeight(boolean inPixels){
		if(standardMembraneLoaded || noMembraneLoaded){
			if(globalParameters == null) globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
			return inPixels ? globalParameters.getHeightInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getHeightInMikron();
		}
		else{
			//Bei der Berechnung durch GeneralPath geht ein Pixel verloren
			return inPixels ? (polygon.getBounds().height + 1):((polygon.getBounds().height + 1)/getNumberOfPixelsPerMicrometer());
		}
	}	
	
	private double getWidth(boolean inPixels){
		if(standardMembraneLoaded || noMembraneLoaded){
			if(globalParameters == null) globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
			return inPixels ? globalParameters.getWidthInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getWidthInMikron();
		}
		else{
			//Bei der Berechnung durch GeneralPath geht ein Pixel verloren
			return inPixels ? (polygon.getBounds().width + 1):((polygon.getBounds().width + 1)/getNumberOfPixelsPerMicrometer());
		}
	}	
	
	
	public double lowerBoundInMikron(double x)
	 {
		if(standardMembraneLoaded){
			if(globalParameters == null) globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
				// y = a * e ^ (-b * x * x) Gaussche Glockenkurve
		     double p=basalPeriod; 
		     
		     double partition=x-(int)(x/p)*p - p/2; // alle 10 einen buckel 5=10/2        
		     double v=Math.exp(-partition*partition/globalParameters.getBasalOpening_mikron());
		     //System.out.println("x:"+x+" p:"+partition+" v:"+v+" Av:"+basalAmplitude*v);
		     return basalY+globalParameters.getBasalAmplitude_mikron()*v;
		}
		else return Double.POSITIVE_INFINITY;
	 }
	
	
	public void setImportedTissueBorder(ImportedTissue _tissue) {
		standardMembraneLoaded = false;
		ArrayList<Point2D> surface = null, basalLayer = null;		
		Point2D[] surfaceArray = null, basalLayerArray = null;
		if(_tissue != null){
			tissue = _tissue;
			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().setNumberOfPixelsPerMicrometer((1/ tissue.getResolutionInMicrometerPerPixel()));
			surface = tissue.getSurfacePoints();
			basalLayer = tissue.getBasalLayerPoints();
//			Collections.shuffle(surface);
//			Collections.shuffle(basalLayer);
			surfaceArray = surface.toArray(new Point2D[surface.size()]);
			basalLayerArray = basalLayer.toArray(new Point2D[basalLayer.size()]);
		
			//SimulatedAnnealingForOrderingPoints sorting = null;
			PointSorter sorting = null;
			if(!surface.isEmpty()){
				//sorting = new SimulatedAnnealingForOrderingPoints(surfaceArray);
			   sorting = new PointSorter(surfaceArray);
				surfaceArray = sorting.getSortedPoints();
			}
			if(!basalLayer.isEmpty()){
				
				//sorting = new SimulatedAnnealingForOrderingPoints(basalLayerArray);
				sorting = new PointSorter(basalLayerArray);
			   basalLayerArray = sorting.getSortedPoints();
				
			}		
			
			fullcontour = new ArrayList<Point2D>();
			for(Point2D point : basalLayerArray)fullcontour.add(point);
			
			for(int i = surfaceArray.length-1; i >= 0 ; i--) fullcontour.add(surfaceArray[i]);				
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
				
				this.basalLayer = new GeneralPath();
				if(!tissue.getBasalLayerPoints().isEmpty()){
					this.basalLayer.moveTo(basalLayerArray[0].getX(), basalLayerArray[0].getY());
					for(Point2D p : basalLayerArray)this.basalLayer.lineTo(p.getX(), p.getY());
				}
				drawBasalLayer = (GeneralPath)this.basalLayer.clone();		
				
				this.surface = new GeneralPath();
				if(!tissue.getSurfacePoints().isEmpty()){
					this.surface.moveTo(surfaceArray[0].getX(), surfaceArray[0].getY());
					for(Point2D p : tissue.getSurfacePoints())this.surface.lineTo(p.getX(), p.getY());
				}
				drawSurface = (GeneralPath)this.surface.clone();
				
				
				
				//polygon.closePath();
				
				organizeBasalLayerPoints();
			}
			
		}
	
	public void loadStandardMembrane(){
		standardMembraneLoaded = true;
		GeneralPath polygon = new GeneralPath();
	 		final int STEPSIZE = 1;
	 		((GeneralPath)polygon).moveTo(startXOfStandardMembrane, lowerBoundInMikron(startXOfStandardMembrane));
	 		for(double i = startXOfStandardMembrane; i <= (startXOfStandardMembrane+getWidthInPixels()); i += STEPSIZE){
	 		((GeneralPath)polygon).lineTo(i, lowerBoundInMikron(i));
	 		}
	 		this.polygon = polygon;
	 		drawPolygon = (GeneralPath)polygon.clone();
	}	
	
	protected static synchronized TissueBorder getInstance(){
		if(instance == null) instance =  new TissueBorder();
		return instance;
	}
	
	@NoExport
	public GeneralPath getFullContourDrawPolygon(){
		return (GeneralPath)drawPolygon.clone();		
	}
	@NoExport
	public GeneralPath getSurfaceDrawPolygon(){
		return (GeneralPath)drawSurface.clone();		
	}
	@NoExport
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
	@NoExport	
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

