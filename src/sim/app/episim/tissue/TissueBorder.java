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
	
	
	private int basalPeriod=70;      // width of an undulation at the foot
	private int startXOfStandardMembrane = 0;
	
	
	
		
	private ImportedTissue actImportedTissue;	
	private boolean standardMembraneLoaded = false;	
	private boolean noMembraneLoaded = false;
	
	private StandardMembrane3DCoordinates standardMembraneCoordinates3D;
	
	
	private boolean isStandardMembrane2DGauss = false;
	
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
		
		actImportedTissue = null;
		
		standardMembraneLoaded = false;	
		noMembraneLoaded = false;
		
		
		basalPeriod=70;
		startXOfStandardMembrane = 0;
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
			
			return calculateStandardMembraneValue(xCell, zCell);
		}
		else if(noMembraneLoaded) return Double.NEGATIVE_INFINITY;
		else if(this.actImportedTissue != null) return getLowerYCoordinateForXCoordinate(xCell, zCell);			
		return Double.NEGATIVE_INFINITY;
	 }
	
	
	private double calculateStandardMembraneValue(double xCell, double yCell){
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL
		   ||(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.instance() instanceof MiscalleneousGlobalParameters3D
		       && !(((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.instance()).isStandardMembrane_2_Dim_Gauss()))){
			// Gaussche Glockenkurve
		     double p=basalPeriod; 
		     
		     double partition=xCell-((int)(xCell/p))*p - p/2;
		     double v=Math.exp(-partition*partition/globalParameters.getBasalOpening_mikron());
		     double result= (globalParameters.getBasalAmplitude_mikron()+2)-globalParameters.getBasalAmplitude_mikron()*v;
		     return result;
		}
		else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.instance() instanceof MiscalleneousGlobalParameters3D
		       && (((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.instance()).isStandardMembrane_2_Dim_Gauss())){
			// Gaussche Glockenkurve
		     double p=basalPeriod; 
		     
		     double partitionX=xCell-((int)(xCell/p))*p - p/2;
		     double partitionY=yCell-((int)(yCell/p))*p - p/2;
		     double v=Math.exp(-1*((partitionX*partitionX)+ (partitionY*partitionY))/(globalParameters.getBasalOpening_mikron()+100));
		     double result= (globalParameters.getBasalAmplitude_mikron()+2)-globalParameters.getBasalAmplitude_mikron()*v;
		     return result;
		}
		return 0;
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
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		
		standardMembraneLoaded = true;
		if(globalParameters.getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL)buildStandardMembrane2D();
		if(globalParameters.getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL)buildStandardMembrane3D();
	}
	
	private void buildStandardMembrane2D(){
		GeneralPath polygon = new GeneralPath();
 		final int STEPSIZE = 1;
 		((GeneralPath)polygon).moveTo(startXOfStandardMembrane, (getHeightInMikron()-lowerBoundInMikron(startXOfStandardMembrane, 0)));
 		for(double i = startXOfStandardMembrane; i <= (startXOfStandardMembrane+getWidthInMikron()); i += STEPSIZE){
 		((GeneralPath)polygon).lineTo(i, (getHeightInMikron()-lowerBoundInMikron(i, 0)));
 		}
 		this.polygon = polygon;
 		this.drawBasalLayer = (GeneralPath)polygon.clone();
 		drawPolygon = (GeneralPath)polygon.clone();
	}
	
	private void buildStandardMembrane3D(){
		ArrayList<Point3f> coordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> leftCoordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> rightCoordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> frontCoordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> backCoordinatesList = new ArrayList<Point3f>();
		final float STEPSIZE = 2;
		
		float width = (float)getWidthInMikron();
		float length = (float)getLengthInMikron();	
		
		for(float x = startXOfStandardMembrane; x <= (startXOfStandardMembrane+width); x += STEPSIZE){
			for(float z = 0; z <= length; z += STEPSIZE){
				coordinatesList.add(new Point3f(x, (float)lowerBoundInMikron(x, z), z));			
				coordinatesList.add(new Point3f(x, (float)lowerBoundInMikron(x, z+STEPSIZE), z+STEPSIZE));
				coordinatesList.add(new Point3f(x+STEPSIZE, (float)lowerBoundInMikron(x+STEPSIZE, z+STEPSIZE), z+STEPSIZE));
				coordinatesList.add(new Point3f(x+STEPSIZE, (float)lowerBoundInMikron(x+STEPSIZE, z), z));
				
				if(x==startXOfStandardMembrane){
					leftCoordinatesList.add(new Point3f(x, (float)lowerBoundInMikron(x, z), z));			
					leftCoordinatesList.add(new Point3f(x, (float)lowerBoundInMikron(x, z+STEPSIZE), z+STEPSIZE));
				}
				if((x+STEPSIZE) >(startXOfStandardMembrane+width)){
					rightCoordinatesList.add(new Point3f(x+STEPSIZE, (float)lowerBoundInMikron(x+STEPSIZE, z), z));
					rightCoordinatesList.add(new Point3f(x+STEPSIZE, (float)lowerBoundInMikron(x+STEPSIZE, z+STEPSIZE), z+STEPSIZE));					
				}
				if(z==0){
					frontCoordinatesList.add(new Point3f(x, (float)lowerBoundInMikron(x, z), z));
					frontCoordinatesList.add(new Point3f(x+STEPSIZE, (float)lowerBoundInMikron(x+STEPSIZE, z), z));
				}
				if((z+STEPSIZE) > length){
					backCoordinatesList.add(new Point3f(x, (float)lowerBoundInMikron(x, z+STEPSIZE), z+STEPSIZE));
					backCoordinatesList.add(new Point3f(x+STEPSIZE, (float)lowerBoundInMikron(x+STEPSIZE, z+STEPSIZE), z+STEPSIZE));
				}				
			}
		}
		
		
		this.standardMembraneCoordinates3D = new StandardMembrane3DCoordinates();
		this.standardMembraneCoordinates3D.coordinates = new Point3f[coordinatesList.size()];
		this.standardMembraneCoordinates3D.leftCoordinates = new Point3f[leftCoordinatesList.size()];
		this.standardMembraneCoordinates3D.rightCoordinates = new Point3f[rightCoordinatesList.size()];
		this.standardMembraneCoordinates3D.frontCoordinates = new Point3f[frontCoordinatesList.size()];
		this.standardMembraneCoordinates3D.backCoordinates = new Point3f[backCoordinatesList.size()];
		for(int i = 0; i < this.standardMembraneCoordinates3D.coordinates.length; i++) this.standardMembraneCoordinates3D.coordinates[i] = coordinatesList.get(i);
		for(int i = 0; i < this.standardMembraneCoordinates3D.leftCoordinates.length; i++) this.standardMembraneCoordinates3D.leftCoordinates[i] = leftCoordinatesList.get(i);
		for(int i = 0; i < this.standardMembraneCoordinates3D.rightCoordinates.length; i++) this.standardMembraneCoordinates3D.rightCoordinates[i] = rightCoordinatesList.get(i);
		for(int i = 0; i < this.standardMembraneCoordinates3D.frontCoordinates.length; i++) this.standardMembraneCoordinates3D.frontCoordinates[i] = frontCoordinatesList.get(i);
		for(int i = 0; i < this.standardMembraneCoordinates3D.backCoordinates.length; i++) this.standardMembraneCoordinates3D.backCoordinates[i] = backCoordinatesList.get(i);
	}
	
	public StandardMembrane3DCoordinates getStandardMembraneCoordinates3D(boolean update){
		boolean wasUpdated = false;
		if((ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.instance() instanceof MiscalleneousGlobalParameters3D
		       && (((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.instance()).isStandardMembrane_2_Dim_Gauss()) && !isStandardMembrane2DGauss)){
			isStandardMembrane2DGauss = true;
			wasUpdated= true;
			buildStandardMembrane3D();
		}
		else if((ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.instance() instanceof MiscalleneousGlobalParameters3D
		       && !(((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.instance()).isStandardMembrane_2_Dim_Gauss()) && isStandardMembrane2DGauss)){
			isStandardMembrane2DGauss = false;
			wasUpdated= true;
			buildStandardMembrane3D();
		}
		if(update && !wasUpdated)buildStandardMembrane3D();
		return this.standardMembraneCoordinates3D;
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
		if(isStandardMembraneLoaded())buildStandardMembrane2D();
		return (GeneralPath)drawBasalLayer.clone();		
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
	
	public class StandardMembrane3DCoordinates{
		public Point3f[] coordinates;
		public Point3f[] leftCoordinates;
		public Point3f[] rightCoordinates;
		public Point3f[] frontCoordinates;
		public Point3f[] backCoordinates;
	}
	
	
	
}

