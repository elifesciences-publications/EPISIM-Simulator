package sim.app.episim.tissue;


import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.vecmath.Point3f;

import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;
import episiminterfaces.NoExport;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.app.episim.tissue.StandardMembrane.StandardMembrane3DCoordinates;
import sim.app.episim.util.ClassLoaderChangeListener;
import sim.app.episim.util.GlobalClassLoader;
import sim.app.episim.util.PointSorter;

public class TissueBorder implements ClassLoaderChangeListener{
	
	private ArrayList<Point2D> fullcontour;
	
	private HashMap<Double, TreeSet<Double>> organizedXPoints;
	
	private GeneralPath polygon;
	private GeneralPath surface;
	private GeneralPath basalLayer;
	
	private GeneralPath drawPolygon;	
	private GeneralPath drawSurface;
	private GeneralPath drawBasalLayer;
	
	
	
	private static  TissueBorder instance;
	
	
	
	
	
	
		
	private ImportedTissue actImportedTissue;	
	private boolean standardMembraneLoaded = false;
	private StandardMembrane standardMembrane = null;
	
	private boolean noMembraneLoaded = false;
	
	
	
	
	
	
	private TissueBorder(){		
		GlobalClassLoader.getInstance().addClassLoaderChangeListener(this);
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
		
		actImportedTissue = null;
		
		standardMembraneLoaded = false;
		if(standardMembrane != null) standardMembrane.resetTissueBorderSettings();
		standardMembrane = null;
		noMembraneLoaded = false;
		

	}	
	
	public void setBasalPeriodInMikron(int period){
		
	}
	
	
	
	public void setStartXOfStandardMembraneInMikron(int start){ 
		if(standardMembrane != null) standardMembrane.setStartXOfStandardMembraneInMikron(start);
	}
	
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
			double resolutionMicoMPerPixel = actImportedTissue != null ? actImportedTissue.getResolutionInMicrometerPerPixel() : 1;
			
			return  1 /resolutionMicoMPerPixel;
		}
	}
	
	
	public String getTissueID(){
		if(standardMembraneLoaded || noMembraneLoaded){
			return "";
		}
		else{
			return actImportedTissue.getTissueID();
		}
	}
	
	public String getTissueDescription(){
		if(standardMembraneLoaded || noMembraneLoaded){
			return "";
		}
		else{
			return actImportedTissue.getTissueDescription();
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
	@NoExport
	public double getLengthInPixels(){
		return getLength(true);
	}
	@NoExport
	public double getLengthInMikron(){
		return getLength(false);
	}
	
	private static final double maxResolution = 500;
	@NoExport
	public double get3DTissueCrosssectionXYResolutionFactor(){
		double height = getHeightInMikron();
		double width = getWidthInMikron();
		return Math.min((maxResolution/height), (maxResolution/width));
	}
	@NoExport
	public double get3DTissueCrosssectionXZResolutionFactor(){		
		double width = getWidthInMikron();
		double length = getLengthInMikron();
		return Math.min((maxResolution/width), (maxResolution/length));
	}
	@NoExport
	public double get3DTissueCrosssectionYZResolutionFactor(){
		double height = getHeightInMikron();
		double length = getLengthInMikron();		
		return Math.min((maxResolution/height), (maxResolution/length));
	}
	
	private double getHeight(boolean inPixels){
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
		if(standardMembraneLoaded || noMembraneLoaded){
			if(globalParameters == null) globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
			return inPixels ? globalParameters.getHeightInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getHeightInMikron();
		}
		else{
			//Bei der Berechnung durch GeneralPath geht ein Pixel verloren
			return inPixels ? (actImportedTissue.getEpidermalHeight()+1) :((actImportedTissue.getEpidermalHeight()+1)/getNumberOfPixelsPerMicrometer());
		}
	}	
	
	private double getWidth(boolean inPixels){
		if(standardMembraneLoaded || noMembraneLoaded){
			EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
			return inPixels ? globalParameters.getWidthInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getWidthInMikron();
		}
		else{
			//Bei der Berechnung durch GeneralPath geht ein Pixel verloren
			return inPixels ? (actImportedTissue.getEpidermalWidth()+1) :((actImportedTissue.getEpidermalWidth()+1)/getNumberOfPixelsPerMicrometer());
		}
	}	
	private double getLength(boolean inPixels){
		
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		return inPixels ? globalParameters.getLengthInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getLengthInMikron();
	}
	
	public double lowerBoundInMikron(double xCell, double yCell, double zCell){
		return lowerBoundInMikron(xCell, zCell);
	}
	
	public double lowerBoundInMikron(double xCell, double zCell)
	 {
		
		 
		if(standardMembraneLoaded){
			
			return standardMembrane.lowerBoundInMikron(xCell, zCell);
		}
		else if(noMembraneLoaded) return Double.NEGATIVE_INFINITY;
		else if(this.actImportedTissue != null) return getLowerYCoordinateForXCoordinate(xCell, zCell);			
		return Double.NEGATIVE_INFINITY;
	 }
	
	
	
	
	
	
	public double upperBoundInMikron(double xCell, double yCell)
	 {		
		if(this.actImportedTissue != null){
			return getUpperYCoordinateForXCoordinate(xCell, yCell);			
		}
		else	return Double.POSITIVE_INFINITY;
	 }
	
	public ImportedTissue getImportedTissue(){ return this.actImportedTissue; }
	
	public void setImportedTissue(ImportedTissue _tissue, boolean tissueVisualizationMode) {
		standardMembraneLoaded = false;
		standardMembrane = null;
		noMembraneLoaded = false;
	 
		ArrayList<Point2D> surface = null, basalLayer = null;		
		Point2D[] surfaceArray = null, basalLayerArray = null;
		if(_tissue != null){
			actImportedTissue = _tissue;
			
			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().setNumberOfPixelsPerMicrometer((1/ actImportedTissue.getResolutionInMicrometerPerPixel()));
			surface = actImportedTissue.getSurfacePoints();
			basalLayer = actImportedTissue.getBasalLayerPoints();

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
			actImportedTissue.getSurfacePoints().clear();
			actImportedTissue.getSurfacePoints().addAll(Arrays.asList(surfaceArray));
			actImportedTissue.getBasalLayerPoints().clear();
			actImportedTissue.getBasalLayerPoints().addAll(Arrays.asList(basalLayerArray));
			if(!tissueVisualizationMode){
				convertPointsToMikron();
				setMinYInPointsToZero();		
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
				if(!actImportedTissue.getBasalLayerPoints().isEmpty()){
					this.basalLayer.moveTo(basalLayerArray[0].getX(), basalLayerArray[0].getY());
					for(Point2D p : basalLayerArray)this.basalLayer.lineTo(p.getX(), p.getY());
				}
				drawBasalLayer = (GeneralPath)this.basalLayer.clone();		
				
				this.surface = new GeneralPath();
				if(!actImportedTissue.getSurfacePoints().isEmpty()){
					this.surface.moveTo(surfaceArray[0].getX(), surfaceArray[0].getY());
					for(Point2D p : actImportedTissue.getSurfacePoints())this.surface.lineTo(p.getX(), p.getY());
				}
				drawSurface = (GeneralPath)this.surface.clone();				
				
				//polygon.closePath();
				
				organizeBasalLayerPoints();
			}
			
	}
	
	private void setMinYInPointsToZero(){
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		if(this.actImportedTissue != null){
				for(Point2D p: this.actImportedTissue.getSurfacePoints()){
					if(p.getY() < minY) minY = p.getY();
					if(p.getY() > maxY) maxY = p.getY();
				}
				for(Point2D p: this.actImportedTissue.getBasalLayerPoints()){
					if(p.getY() < minY) minY = p.getY();
					if(p.getY() > maxY) maxY = p.getY();
				}
				
				double deltaHeight = (this.actImportedTissue.getEpidermalHeight()*this.actImportedTissue.getResolutionInMicrometerPerPixel()) -(maxY-minY);
				for(Point2D p: this.actImportedTissue.getSurfacePoints()){
					p.setLocation(p.getX(), (p.getY()-minY+deltaHeight));
				}
				for(Point2D p: this.actImportedTissue.getBasalLayerPoints()){
					p.setLocation(p.getX(), (p.getY()-minY+deltaHeight));
				}
		}
	}
	
	private void convertPointsToMikron(){
		if(this.actImportedTissue != null){
			for(Point2D p: this.actImportedTissue.getSurfacePoints()){
					p.setLocation(p.getX()*actImportedTissue.getResolutionInMicrometerPerPixel(), p.getY()*actImportedTissue.getResolutionInMicrometerPerPixel());
			}
			for(Point2D p: this.actImportedTissue.getBasalLayerPoints()){
					p.setLocation(p.getX()*actImportedTissue.getResolutionInMicrometerPerPixel(), p.getY()*actImportedTissue.getResolutionInMicrometerPerPixel());
			}			
				
		}
	}
	
	public void loadStandardMembrane(){		
		standardMembraneLoaded = true;
		standardMembrane = new StandardMembrane();
	}
	public void loadStandardMembrane(int discretizationSteps, int contactTimeThreshold){		
		standardMembraneLoaded = true;
		standardMembrane = new StandardMembrane(discretizationSteps, (double)contactTimeThreshold);
	}
	
	public void loadStandardMembrane(int discretizationStepsX, int discretizationStepsZ, int contactTimeThreshold){		
		standardMembraneLoaded = true;
		standardMembrane = new StandardMembrane(discretizationStepsX, discretizationStepsZ, (double)contactTimeThreshold);
	}
	
	
	
	public StandardMembrane3DCoordinates getStandardMembraneCoordinates3D(boolean update){
		return standardMembrane.getStandardMembraneCoordinates3D(update);
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
		if(isStandardMembraneLoaded()){
			standardMembrane.buildStandardMembrane2D();
			return standardMembrane.getBasalLayerDrawPolygon();
		}
		else return (GeneralPath)drawBasalLayer.clone();		
	}
	@NoExport
	public StandardMembrane getStandardMembrane(){
		return this.standardMembrane;
	}
		
		      
	private void organizeBasalLayerPoints(){
		double height = actImportedTissue.getEpidermalHeight();
		for(Point2D actPoint: actImportedTissue.getBasalLayerPoints()){
			double x = actPoint.getX();
			double y = actPoint.getY();
			if(organizedXPoints.containsKey(x))
				organizedXPoints.get(x).add(height-y);
			else{
				TreeSet<Double> tmp =new TreeSet<Double>();
				tmp.add(height-y);
				organizedXPoints.put(x, tmp);
			}
		}
		
	}
	@NoExport	
	public double getLowerYCoordinateForXCoordinate(double xCell, double yCell){
		
		if(organizedXPoints.containsKey(xCell)){
			TreeSet<Double> yValues = organizedXPoints.get(xCell);
			Iterator<Double> iter= yValues.iterator();
		
			while(iter.hasNext()){
				double yValue = iter.next();
				if(yValue < yCell){
					
					return yValue;
				}
			}
			return yValues.first();
		}
		else return Double.NEGATIVE_INFINITY;
	}
	
	@NoExport	
	public double getUpperYCoordinateForXCoordinate(double xCell, double yCell){
		
		if(organizedXPoints.containsKey(xCell)){ 
			TreeSet<Double> yValues = organizedXPoints.get(xCell);
			Iterator<Double> iter= yValues.iterator();
		
			while(iter.hasNext()){
				double yValue = iter.next();
				if(yValue > yCell){
					
					return yValue;
				}
			}
			return yValues.last();
		}
		else return Double.POSITIVE_INFINITY;
	}
	
   public void classLoaderHasChanged() {
		instance = null;
	   
   }
	
	
	
}

