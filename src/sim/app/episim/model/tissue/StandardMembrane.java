package sim.app.episim.model.tissue;

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
import sim.util.Double3D;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.NoExport;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters.ModelDimensionality;


public class StandardMembrane {
	
	
	private int startXOfStandardMembrane = 0;
	
	private GeneralPath drawPolygon;	
	private GeneralPath drawBasalLayer;
	private GeneralPath polygon;
	private boolean isStandardMembrane2DGauss = false;	
	private StandardMembrane3DCoordinates standardMembraneCoordinates3D;
	
	private int discretizationStepsX = 0;
	private int discretizationStepsZ = 0;
	private double cellContactTimeThreshold= 0;
	private BasalMembraneDiscretizationStep2D[] basalMembraneDiscretization2D = null;
	private BasalMembraneDiscretizationStep3D[][] basalMembraneDiscretization3D = null;
	private final int DRAWING_STEPSIZE = 1;
	private ArrayList<Double> contactTimeToMembraneSegmentList2D;
	private ArrayList<Double> contactTimeToMembraneSegmentList3D;
	boolean isDiscretizedMembrane = false;
	EpisimBiomechanicalModelGlobalParameters globalParameters;
	protected StandardMembrane(){
		this(0,0,0);
	}
	protected StandardMembrane(int discretizationSteps, double cellContactTimeThreshold){
		this(discretizationSteps, discretizationSteps,cellContactTimeThreshold);		
	}
	
	protected StandardMembrane(int discretizationStepsX,int discretizationStepsZ, double cellContactTimeThreshold){
		globalParameters =ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		if(globalParameters.getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL){			
			if(discretizationStepsX > 1){
				this.discretizationStepsX = discretizationStepsX; 
				this.cellContactTimeThreshold = cellContactTimeThreshold;
				buildAdhesionDiscretization2D();
				isDiscretizedMembrane=true;
			}
			buildStandardMembrane2D();
		}
		if(globalParameters.getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL){
			if(discretizationStepsX > 1 && discretizationStepsZ > 1){
				this.discretizationStepsX = discretizationStepsX; 
				this.discretizationStepsZ = discretizationStepsZ;
				this.cellContactTimeThreshold = cellContactTimeThreshold;
				buildAdhesionDiscretization3D();
				isDiscretizedMembrane=true;
			}
			buildStandardMembrane3D(false);
		}
		
	}
	
	protected void buildStandardMembrane2D(){
		GeneralPath polygon = new GeneralPath();
		if(this.isDiscretizedMembrane) contactTimeToMembraneSegmentList2D = new ArrayList<Double>();
 		((GeneralPath)polygon).moveTo(startXOfStandardMembrane, (getHeightInMikron()-lowerBoundInMikron(startXOfStandardMembrane, 0)));
 		for(double i = startXOfStandardMembrane; i <= (startXOfStandardMembrane+getWidthInMikron()); i += DRAWING_STEPSIZE){
 		((GeneralPath)polygon).lineTo(i, (getHeightInMikron()-lowerBoundInMikron(i, 0)));
	 		if(this.isDiscretizedMembrane){ 
	 		  BasalMembraneDiscretizationStep2D step = getBasalMembraneDiscretizationStep2DForCoordinates(new Double2D(i, lowerBoundInMikron(i, 0)));
	 		  if(step != null)contactTimeToMembraneSegmentList2D.add(step.contactTime);
	 		}
 		}
 		this.polygon = polygon;
 		this.drawBasalLayer = (GeneralPath)polygon.clone();
 		drawPolygon = (GeneralPath)polygon.clone();
	}
	protected void buildAdhesionDiscretization2D(){
		double width = getWidthInMikron();
		double intervalSize = width/((double)discretizationStepsX);
		basalMembraneDiscretization2D = new BasalMembraneDiscretizationStep2D[discretizationStepsX];
		for(int i = 0; i < discretizationStepsX; i++){
			this.basalMembraneDiscretization2D[i] = new BasalMembraneDiscretizationStep2D();
			this.basalMembraneDiscretization2D[i].lowerBound = new Double2D(i*intervalSize, lowerBoundInMikron(i*intervalSize, 0));
			this.basalMembraneDiscretization2D[i].upperBound = new Double2D((i+1)*intervalSize, lowerBoundInMikron((i+1)*intervalSize, 0));
			double referenceX = (this.basalMembraneDiscretization2D[i].lowerBound.x + this.basalMembraneDiscretization2D[i].upperBound.x) /2d;
			this.basalMembraneDiscretization2D[i].referenceCoordinates = new Double2D(referenceX, lowerBoundInMikron(referenceX, 0));
			this.basalMembraneDiscretization2D[i].contactTime = 0;
		}
	}
	protected void buildAdhesionDiscretization3D(){
		double width = getWidthInMikron();
		double length = getLengthInMikron();
		double intervalSizeX = width / ((double)discretizationStepsX);
		double intervalSizeZ = length / ((double)discretizationStepsZ);
		basalMembraneDiscretization3D = new BasalMembraneDiscretizationStep3D[discretizationStepsZ][discretizationStepsX];
		for(int i = 0; i < discretizationStepsZ; i++){
			for(int n = 0; n < discretizationStepsX; n++){
				
				this.basalMembraneDiscretization3D[i][n] = new BasalMembraneDiscretizationStep3D();
				
				this.basalMembraneDiscretization3D[i][n].lowerBoundZ1 = new Double3D(n*intervalSizeX, lowerBoundInMikron(n*intervalSizeX, i*intervalSizeZ),i*intervalSizeZ);
				this.basalMembraneDiscretization3D[i][n].upperBoundZ1 = new Double3D((n+1)*intervalSizeX, lowerBoundInMikron((n+1)*intervalSizeX, i*intervalSizeZ),i*intervalSizeZ);
				
				this.basalMembraneDiscretization3D[i][n].lowerBoundZ2 = new Double3D(n*intervalSizeX, lowerBoundInMikron(n*intervalSizeX, (i+1)*intervalSizeZ),(i+1)*intervalSizeZ);
				this.basalMembraneDiscretization3D[i][n].upperBoundZ2 = new Double3D((n+1)*intervalSizeX, lowerBoundInMikron((n+1)*intervalSizeX, (i+1)*intervalSizeZ), (i+1)*intervalSizeZ);
				
				double referenceX = (this.basalMembraneDiscretization3D[i][n].lowerBoundZ1.x 
											+ this.basalMembraneDiscretization3D[i][n].upperBoundZ1.x
											+ this.basalMembraneDiscretization3D[i][n].lowerBoundZ2.x 
											+ this.basalMembraneDiscretization3D[i][n].upperBoundZ2.x) /4d;
				double referenceZ = (this.basalMembraneDiscretization3D[i][n].lowerBoundZ1.z 
											+ this.basalMembraneDiscretization3D[i][n].upperBoundZ1.z
											+ this.basalMembraneDiscretization3D[i][n].lowerBoundZ2.z 
											+ this.basalMembraneDiscretization3D[i][n].upperBoundZ2.z) /4d;
				this.basalMembraneDiscretization3D[i][n].referenceCoordinates = new Double3D(referenceX, lowerBoundInMikron(referenceX, referenceZ), referenceZ);
				this.basalMembraneDiscretization3D[i][n].contactTime = 0;
			}
		}
	}
	
	
	protected void buildStandardMembrane3D(boolean optimized){
		ArrayList<Point3f> coordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> leftCoordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> rightCoordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> frontCoordinatesList = new ArrayList<Point3f>();
		ArrayList<Point3f> backCoordinatesList = new ArrayList<Point3f>();
		final float STEPSIZE = optimized ?0.75f:2f;
		
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
	
	protected StandardMembrane3DCoordinates getStandardMembraneCoordinates3D(boolean update, boolean optimized){
		boolean wasUpdated = false;		
		if((ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
		       && (((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss()) && !isStandardMembrane2DGauss)){
			isStandardMembrane2DGauss = true;
			wasUpdated= true;
			buildStandardMembrane3D(optimized);
		}
		else if((ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
		       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
		       && !(((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss()) && isStandardMembrane2DGauss)){
			isStandardMembrane2DGauss = false;
			wasUpdated= true;
			buildStandardMembrane3D(optimized);
		}
		if(update && !wasUpdated)buildStandardMembrane3D(optimized);
		return this.standardMembraneCoordinates3D;
	}
	
	protected double calculateStandardMembraneValue(double xCell, double yCell){
		
		if(globalParameters.getBasalAmplitude_mikron()!=0){		
			if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.TWO_DIMENSIONAL
			   ||(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
			       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
			       && !(((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss()))){
				  // Gaussche Glockenkurve
			     double p=globalParameters.getBasalPeriod_mikron();		     
			     double partition=xCell-((int)(xCell/p))*p - p/2;
			     double v=Math.exp(-partition*partition/globalParameters.getBasalOpening_mikron());
			     double result= (globalParameters.getBasalAmplitude_mikron()+globalParameters.getBasalYDelta_mikron())-globalParameters.getBasalAmplitude_mikron()*v;
			     return result;
			}
			else if(ModelController.getInstance().getModelDimensionality() == ModelDimensionality.THREE_DIMENSIONAL 
			       && MiscalleneousGlobalParameters.getInstance() instanceof MiscalleneousGlobalParameters3D
			       && (((MiscalleneousGlobalParameters3D)MiscalleneousGlobalParameters.getInstance()).getStandardMembrane_2_Dim_Gauss())){			
				  // Gaussche Glockenkurve
			     double p=globalParameters.getBasalPeriod_mikron(); 
			     double partitionX=xCell-((int)(xCell/p))*p - p/2;
			     double partitionY=yCell-((int)(yCell/p))*p - p/2;
			     double v=Math.exp(-1*((partitionX*partitionX)+ (partitionY*partitionY))/(globalParameters.getBasalOpening_mikron()+100));
			     double result= (globalParameters.getBasalAmplitude_mikron()+globalParameters.getBasalYDelta_mikron())-globalParameters.getBasalAmplitude_mikron()*v;
			     return result;
			}
			return 0;
		}
		else return 2;
		
	}
	private double getHeight(boolean inPixels){	
		if(globalParameters == null) globalParameters = ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters(); 
		return inPixels ? globalParameters.getHeightInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getHeightInMikron();
	}
	
	private double getWidth(boolean inPixels){
		return inPixels ? globalParameters.getWidthInMikron()*getNumberOfPixelsPerMicrometer() : globalParameters.getWidthInMikron();	
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
		startXOfStandardMembrane = 0;
		discretizationStepsX = 0;
		cellContactTimeThreshold= 0;
		contactTimeToMembraneSegmentList2D=null;
		contactTimeToMembraneSegmentList3D=null;
		isDiscretizedMembrane=false;
		
	}
	
	public boolean isDiscretizedMembrane(){ return this.isDiscretizedMembrane; }
	
	private BasalMembraneDiscretizationStep2D getBasalMembraneDiscretizationStep2DForCoordinates(Double2D coord){
		if(this.basalMembraneDiscretization2D != null){
			double width = getWidthInMikron();
			double intervalSize = width/((double)discretizationStepsX);
			int index = (int)(coord.x /intervalSize);
			return this.basalMembraneDiscretization2D[index >= this.basalMembraneDiscretization2D.length ? (this.basalMembraneDiscretization2D.length-1):index< 0? 0: index];
		}
		return null;
	}
	private BasalMembraneDiscretizationStep3D getBasalMembraneDiscretizationStep3DForCoordinates(Double3D coord){
		if(this.basalMembraneDiscretization3D != null){
			double width = getWidthInMikron();
			double length = getLengthInMikron();
			double intervalSizeX = width/((double)discretizationStepsX);
			double intervalSizeZ = length/((double)discretizationStepsZ);
			int indexX = (int)(coord.x /intervalSizeX);
			int indexZ = (int)(coord.z /intervalSizeZ);
			indexZ = indexZ >= this.basalMembraneDiscretization3D.length ? (this.basalMembraneDiscretization3D.length-1):indexZ< 0? 0: indexZ;
			indexX = indexX >= this.basalMembraneDiscretization3D[indexZ].length ? (this.basalMembraneDiscretization3D[indexZ].length-1):indexX< 0? 0: indexX;
			return this.basalMembraneDiscretization3D[indexZ][indexX];
		}
		return null;
	}
	
	public BasalMembraneDiscretizationStep2D[] getBasalMembraneDiscretizationSteps2DCopy(){
		if(this.basalMembraneDiscretization2D != null){
			BasalMembraneDiscretizationStep2D[] copy = new BasalMembraneDiscretizationStep2D[this.basalMembraneDiscretization2D.length];
			for(int i = 0; i < this.basalMembraneDiscretization2D.length; i++){
				if(this.basalMembraneDiscretization2D[i] != null){
					copy[i] = new BasalMembraneDiscretizationStep2D();
					copy[i].contactTime = this.basalMembraneDiscretization2D[i].contactTime;
					copy[i].upperBound = new Double2D(this.basalMembraneDiscretization2D[i].upperBound.x, this.basalMembraneDiscretization2D[i].upperBound.y);
					copy[i].lowerBound = new Double2D(this.basalMembraneDiscretization2D[i].lowerBound.x, this.basalMembraneDiscretization2D[i].lowerBound.y);
					copy[i].referenceCoordinates = new Double2D(this.basalMembraneDiscretization2D[i].referenceCoordinates.x, this.basalMembraneDiscretization2D[i].referenceCoordinates.y);
				}
			}
			return copy;
		}
		return null;
	}
	public BasalMembraneDiscretizationStep3D[][] getBasalMembraneDiscretizationSteps3DCopy(){
		if(this.basalMembraneDiscretization3D != null && this.basalMembraneDiscretization3D[0] != null){
			BasalMembraneDiscretizationStep3D[][] copy = new BasalMembraneDiscretizationStep3D[this.basalMembraneDiscretization3D.length][this.basalMembraneDiscretization3D[0].length];
			for(int i = 0; i < this.basalMembraneDiscretization3D.length; i++){
				if(this.basalMembraneDiscretization2D[i] != null){
					for(int n = 0; n < this.basalMembraneDiscretization3D[i].length; n++){
						copy[i][n] = new BasalMembraneDiscretizationStep3D();
						copy[i][n].contactTime = this.basalMembraneDiscretization3D[i][n].contactTime;
						copy[i][n].upperBoundZ1 = new Double3D(this.basalMembraneDiscretization3D[i][n].upperBoundZ1.x,
																			this.basalMembraneDiscretization3D[i][n].upperBoundZ1.y,
																			this.basalMembraneDiscretization3D[i][n].upperBoundZ1.z);
						
						copy[i][n].lowerBoundZ1 = new Double3D(this.basalMembraneDiscretization3D[i][n].lowerBoundZ1.x,
																			this.basalMembraneDiscretization3D[i][n].lowerBoundZ1.y,
																			this.basalMembraneDiscretization3D[i][n].lowerBoundZ1.z);
						
						copy[i][n].upperBoundZ2 = new Double3D(this.basalMembraneDiscretization3D[i][n].upperBoundZ2.x,
																			this.basalMembraneDiscretization3D[i][n].upperBoundZ2.y,
																			this.basalMembraneDiscretization3D[i][n].upperBoundZ2.z);
						
						copy[i][n].lowerBoundZ2 = new Double3D(this.basalMembraneDiscretization3D[i][n].lowerBoundZ2.x,
																			this.basalMembraneDiscretization3D[i][n].lowerBoundZ2.y,
																			this.basalMembraneDiscretization3D[i][n].lowerBoundZ2.z);
						
						copy[i][n].referenceCoordinates = new Double3D(this.basalMembraneDiscretization3D[i][n].referenceCoordinates.x, 
																					  this.basalMembraneDiscretization3D[i][n].referenceCoordinates.y,
																					  this.basalMembraneDiscretization3D[i][n].referenceCoordinates.z);
					
					}
				}
			}
			return copy;
		}
		return null;
	}
	
	public Double2D getBasalAdhesionReferenceCoordinates2D(Double2D cellCoordinates){
		if(this.basalMembraneDiscretization2D != null){
			BasalMembraneDiscretizationStep2D step = getBasalMembraneDiscretizationStep2DForCoordinates(cellCoordinates);
			if(step != null){
				return step.referenceCoordinates;
			}
		}
		return null;
	}
	public double getContactTimeForReferenceCoordinate2D(Double2D cellCoordinates){
		if(this.basalMembraneDiscretization2D != null){
			BasalMembraneDiscretizationStep2D step = getBasalMembraneDiscretizationStep2DForCoordinates(cellCoordinates);
			if(step != null){
				return step.contactTime;
			}
		}
		return -1;
	}
	public void setContactTimeForReferenceCoordinate2D(Double2D cellCoordinates, double contactTime){
		if(this.basalMembraneDiscretization2D != null){
			BasalMembraneDiscretizationStep2D step = getBasalMembraneDiscretizationStep2DForCoordinates(cellCoordinates);
			if(step != null){
				step.contactTime = contactTime;
			}
		}		
	}
	public void inkrementContactTimeForReferenceCoordinate2D(Double2D cellCoordinates){
		if(this.basalMembraneDiscretization2D != null){
			BasalMembraneDiscretizationStep2D step = getBasalMembraneDiscretizationStep2DForCoordinates(cellCoordinates);
			if(step != null){
				step.contactTime++;
			}
		}
	}
	
	
	public Double3D getBasalAdhesionReferenceCoordinates3D(Double3D cellCoordinates){
		if(this.basalMembraneDiscretization3D != null){
			BasalMembraneDiscretizationStep3D step = getBasalMembraneDiscretizationStep3DForCoordinates(cellCoordinates);
			if(step != null){
				return step.referenceCoordinates;
			}
		}
		return null;
	}
	public double getContactTimeForReferenceCoordinate3D(Double3D cellCoordinates){
		if(this.basalMembraneDiscretization3D != null){
			BasalMembraneDiscretizationStep3D step = getBasalMembraneDiscretizationStep3DForCoordinates(cellCoordinates);
			if(step != null){
				return step.contactTime;
			}
		}
		return -1;
	}
	public void setContactTimeForReferenceCoordinate3D(Double3D cellCoordinates, double contactTime){
		if(this.basalMembraneDiscretization3D != null){
			BasalMembraneDiscretizationStep3D step = getBasalMembraneDiscretizationStep3DForCoordinates(cellCoordinates);
			if(step != null){
				step.contactTime = contactTime;
			}
		}		
	}
	public void inkrementContactTimeForReferenceCoordinate3D(Double3D cellCoordinates){
		if(this.basalMembraneDiscretization3D != null){
			BasalMembraneDiscretizationStep3D step = getBasalMembraneDiscretizationStep3DForCoordinates(cellCoordinates);
			if(step != null){
				step.contactTime++;
			}
		}
	}
	
	
	
	
	private class BasalMembraneDiscretizationStep2D{
		public double contactTime = 0;
		public Double2D lowerBound = null;
		public Double2D referenceCoordinates = null;
		public Double2D upperBound = null;
	}
	
	private class BasalMembraneDiscretizationStep3D{
		public double contactTime = 0;
		public Double3D lowerBoundZ1 = null;
		public Double3D lowerBoundZ2 = null;
		public Double3D referenceCoordinates = null;
		public Double3D upperBoundZ1 = null;
		public Double3D upperBoundZ2 = null;
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
	
   public ArrayList<Double> getContactTimeToMembraneSegmentList2D() {
   
   	return contactTimeToMembraneSegmentList2D;
   }
	
}
