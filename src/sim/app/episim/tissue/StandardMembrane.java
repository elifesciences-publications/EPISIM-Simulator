package sim.app.episim.tissue;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.vecmath.Point3f;

import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters.MiscalleneousGlobalParameters3D;
import sim.util.Double2D;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class StandardMembrane {
	
	private int basalPeriod=70;      // width of an undulation at the foot
	private int startXOfStandardMembrane = 0;
	
	private GeneralPath drawPolygon;	
	private GeneralPath drawBasalLayer;
	private GeneralPath polygon;
	private boolean isStandardMembrane2DGauss = false;	
	private StandardMembrane3DCoordinates standardMembraneCoordinates3D;
	
	private int discretizationSteps = 0;
	private double cellContactTimeThreshold= 0;
	private BasalMembraneDiscretizationStep[] basalMembraneDiscretization = null;
	private final int DRAWING_STEPSIZE = 1;
	private ArrayList<Double> contactTimeToMembraneSegmentList;
	boolean isDiscretizedMembrane = false;
	protected StandardMembrane(){
		this(0,0);
	}
	protected StandardMembrane(int discretizationSteps, double cellContactTimeThreshold){
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(globalParameters.getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){			
			if(discretizationSteps > 1){
				this.discretizationSteps = discretizationSteps; 
				this.cellContactTimeThreshold = cellContactTimeThreshold;
				buildAdhesionDiscretization();
				isDiscretizedMembrane=true;
			}
			buildStandardMembrane2D();
		}
		if(globalParameters.getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL)buildStandardMembrane3D();
		
	}
	
	protected void buildStandardMembrane2D(){
		GeneralPath polygon = new GeneralPath();
		if(this.isDiscretizedMembrane) contactTimeToMembraneSegmentList = new ArrayList<Double>();
 		((GeneralPath)polygon).moveTo(startXOfStandardMembrane, (getHeightInMikron()-lowerBoundInMikron(startXOfStandardMembrane, 0)));
 		for(double i = startXOfStandardMembrane; i <= (startXOfStandardMembrane+getWidthInMikron()); i += DRAWING_STEPSIZE){
 		((GeneralPath)polygon).lineTo(i, (getHeightInMikron()-lowerBoundInMikron(i, 0)));
	 		if(this.isDiscretizedMembrane){ 
	 		  BasalMembraneDiscretizationStep step = getBasalMembraneDiscretizationStepForCoordinates(new Double2D(i, lowerBoundInMikron(i, 0)));
	 		  if(step != null)contactTimeToMembraneSegmentList.add(step.contactTime);
	 		}
 		}
 		this.polygon = polygon;
 		this.drawBasalLayer = (GeneralPath)polygon.clone();
 		drawPolygon = (GeneralPath)polygon.clone();
	}
	protected void buildAdhesionDiscretization(){
		double width = getWidthInMikron();
		double intervalSize = width/((double)discretizationSteps);
		basalMembraneDiscretization = new BasalMembraneDiscretizationStep[discretizationSteps];
		for(int i = 0; i < discretizationSteps; i++){
			this.basalMembraneDiscretization[i] = new BasalMembraneDiscretizationStep();
			this.basalMembraneDiscretization[i].lowerBound = new Double2D(i*intervalSize, lowerBoundInMikron(i*intervalSize, 0));
			this.basalMembraneDiscretization[i].upperBound = new Double2D((i+1)*intervalSize, lowerBoundInMikron((i+1)*intervalSize, 0));
			double referenceX = (this.basalMembraneDiscretization[i].lowerBound.x + this.basalMembraneDiscretization[i].upperBound.x) /2d;
			this.basalMembraneDiscretization[i].referenceCoordinates = new Double2D(referenceX, lowerBoundInMikron(referenceX, 0));
			this.basalMembraneDiscretization[i].contactTime = 0;
		}
	}
	
	
	protected void buildStandardMembrane3D(){
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
	
	protected StandardMembrane3DCoordinates getStandardMembraneCoordinates3D(boolean update){
		boolean wasUpdated = false;
		if((ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
		       && (((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss()) && !isStandardMembrane2DGauss)){
			isStandardMembrane2DGauss = true;
			wasUpdated= true;
			buildStandardMembrane3D();
		}
		else if((ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
		       && !(((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss()) && isStandardMembrane2DGauss)){
			isStandardMembrane2DGauss = false;
			wasUpdated= true;
			buildStandardMembrane3D();
		}
		if(update && !wasUpdated)buildStandardMembrane3D();
		return this.standardMembraneCoordinates3D;
	}
	
	protected double calculateStandardMembraneValue(double xCell, double yCell){
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL
		   ||(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
		       && !(((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss()))){
			// Gaussche Glockenkurve
		     double p=basalPeriod; 
		     
		     double partition=xCell-((int)(xCell/p))*p - p/2;
		     double v=Math.exp(-partition*partition/globalParameters.getBasalOpening_mikron());
		     double result= (globalParameters.getBasalAmplitude_mikron()+2)-globalParameters.getBasalAmplitude_mikron()*v;
		     return result;
		}
		else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
		       && (((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss())){
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
	private double getHeight(boolean inPixels){
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
		if(globalParameters == null) globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
		return inPixels ? globalParameters.getHeightInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getHeightInMikron();
	}
	
	private double getWidth(boolean inPixels){
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
		return inPixels ? globalParameters.getWidthInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getWidthInMikron();	
	}
	
	public void setBasalPeriodInMikron(int period){
		this.basalPeriod = period;
	}
	public void setStartXOfStandardMembraneInMikron(int start){ startXOfStandardMembrane = start; }
	public double getNumberOfPixelsPerMicrometer(){
		return ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNumberOfPixelsPerMicrometer();
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
	
	private double getLength(boolean inPixels){
		
		EpisimBiomechanicalModelGlobalParameters globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		return inPixels ? globalParameters.getLengthInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getLengthInMikron();
	}
	public double lowerBoundInMikron(double xCell, double yCell, double zCell){
		return lowerBoundInMikron(xCell, zCell);
	}
	
	public double lowerBoundInMikron(double xCell, double zCell)
	{
		return calculateStandardMembraneValue(xCell, zCell);	
	}
	
	public void resetTissueBorderSettings(){
		polygon = new GeneralPath();
		drawPolygon = new GeneralPath();		
		drawBasalLayer = new GeneralPath();
		basalPeriod=70;
		startXOfStandardMembrane = 0;
		discretizationSteps = 0;
		cellContactTimeThreshold= 0;
		contactTimeToMembraneSegmentList=null;
		isDiscretizedMembrane=false;
		
	}
	
	public boolean isDiscretizedMembrane(){ return this.isDiscretizedMembrane; }
	private BasalMembraneDiscretizationStep getBasalMembraneDiscretizationStepForCoordinates(Double2D coord){
		if(this.basalMembraneDiscretization != null){
			double width = getWidthInMikron();
			double intervalSize = width/((double)discretizationSteps);
			int index = (int)(coord.x /intervalSize);
			return this.basalMembraneDiscretization[index >= this.basalMembraneDiscretization.length ? (this.basalMembraneDiscretization.length-1):index< 0? 0: index];
		}
		return null;
	}
	
	public BasalMembraneDiscretizationStep[] getBasalMembraneDiscretizationStepsCopy(){
		if(this.basalMembraneDiscretization != null){
			BasalMembraneDiscretizationStep[] copy = new BasalMembraneDiscretizationStep[this.basalMembraneDiscretization.length];
			for(int i = 0; i < this.basalMembraneDiscretization.length; i++){
				if(this.basalMembraneDiscretization[i] != null){
					copy[i] = new BasalMembraneDiscretizationStep();
					copy[i].contactTime = this.basalMembraneDiscretization[i].contactTime;
					copy[i].upperBound = new Double2D(this.basalMembraneDiscretization[i].upperBound.x, this.basalMembraneDiscretization[i].upperBound.y);
					copy[i].lowerBound = new Double2D(this.basalMembraneDiscretization[i].lowerBound.x, this.basalMembraneDiscretization[i].lowerBound.y);
					copy[i].referenceCoordinates = new Double2D(this.basalMembraneDiscretization[i].referenceCoordinates.x, this.basalMembraneDiscretization[i].referenceCoordinates.y);
				}
			}
			return copy;
		}
		return null;
	}
	
	public Double2D getBasalAdhesionReferenceCoordinates2D(Double2D cellCoordinates){
		if(this.basalMembraneDiscretization != null){
			BasalMembraneDiscretizationStep step = getBasalMembraneDiscretizationStepForCoordinates(cellCoordinates);
			if(step != null){
				return step.referenceCoordinates;
			}
		}
		return null;
	}
	public double getContactTimeForReferenceCoordinate2D(Double2D cellCoordinates){
		if(this.basalMembraneDiscretization != null){
			BasalMembraneDiscretizationStep step = getBasalMembraneDiscretizationStepForCoordinates(cellCoordinates);
			if(step != null){
				return step.contactTime;
			}
		}
		return -1;
	}
	public void setContactTimeForReferenceCoordinate2D(Double2D cellCoordinates, double contactTime){
		if(this.basalMembraneDiscretization != null){
			BasalMembraneDiscretizationStep step = getBasalMembraneDiscretizationStepForCoordinates(cellCoordinates);
			if(step != null){
				step.contactTime = contactTime;
			}
		}		
	}
	public void inkrementContactTimeForReferenceCoordinate2D(Double2D cellCoordinates){
		if(this.basalMembraneDiscretization != null){
			BasalMembraneDiscretizationStep step = getBasalMembraneDiscretizationStepForCoordinates(cellCoordinates);
			if(step != null){
				step.contactTime++;
			}
		}
	}
	
	private class BasalMembraneDiscretizationStep{
		public double contactTime = 0;
		public Double2D lowerBound = null;
		public Double2D referenceCoordinates = null;
		public Double2D upperBound = null;
	}
	
	
	public class StandardMembrane3DCoordinates{
		public Point3f[] coordinates;
		public Point3f[] leftCoordinates;
		public Point3f[] rightCoordinates;
		public Point3f[] frontCoordinates;
		public Point3f[] backCoordinates;
	}


	
   public double getCellContactTimeThreshold() {
   
   	return cellContactTimeThreshold;
   }
   public GeneralPath getBasalLayerDrawPolygon(){
		return (GeneralPath)drawBasalLayer.clone();		
	}
	
   public ArrayList<Double> getContactTimeToMembraneSegmentList() {
   
   	return contactTimeToMembraneSegmentList;
   }
	
}
