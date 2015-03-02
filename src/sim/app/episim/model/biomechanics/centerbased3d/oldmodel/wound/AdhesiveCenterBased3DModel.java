package sim.app.episim.model.biomechanics.centerbased3d.oldmodel.wound;



import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


















import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical3DModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Ellipsoid;
import sim.app.episim.model.biomechanics.Episim3DCellShape;


import sim.app.episim.model.biomechanics.centerbased3d.AbstractCenterBased3DModel;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.tissue.StandardMembrane;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;
import sim.util.Double3D;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import episimmcc.EpisimModelConnector;
import episimmcc.centerbased3d.adhesion.EpisimAdhesiveCenterBased3DMC;

public class AdhesiveCenterBased3DModel extends AbstractCenterBased3DModel {
	

   
   private double MINDIST=0.1;   
   
   
   private static final double MAX_DISPLACEMENT_FACT = 1.2;
   
   private double keratinoWidth=-1; 
   private double keratinoHeight=-1; 
   private double keratinoLength=-1;
   
   private double standardCellWidth=0; 
   private double standardCellHeight=0; 
   private double standardCellLength=0;
   
   private Vector3d externalForce = new Vector3d(0,0,0);
      
   private HitResult finalHitResult;
   
   //maybe more neighbours than real neighbours included inside a circle
   private GenericBag<AbstractCell> neighbouringCells;
   
   private EpisimAdhesiveCenterBased3DMC modelConnector;
   
  
   
   private static Continuous3D cellField;
   
   private AdhesiveCenterBased3DModelGP globalParameters = null;
   
   private double surfaceAreaRatio =0;
   private boolean isSurfaceCell = false;
   
   private boolean hasFixedPosition = false;
   
   private boolean shapeChangeActive=false;
   private Double3D finalDimensions = null;
   private Double3D oldDimensions = null;
   private Double3D sizeChangeDelta = null;
      
   private int positionChangeDeniedCounter = 0;
   private static double FIELD_RESOLUTION_IN_MIKRON=18;
   private Double3D cellFieldCenter;
   private double cellFieldRadius=0;
   private final double MIN_Y;
  
   public AdhesiveCenterBased3DModel(){
   	this(null);
   }
   
   public AdhesiveCenterBased3DModel(AbstractCell cell){
   	super(cell);
   	
   	if(cellField == null){
   		cellField = new Continuous3D(FIELD_RESOLUTION_IN_MIKRON, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron(),
					TissueController.getInstance().getTissueBorder().getLengthInMikron());
   		
   	}
   	MIN_Y= TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0, 0,0);
   	if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof AdhesiveCenterBased3DModelGP){
   		globalParameters = (AdhesiveCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}
   	if(cellFieldCenter==null){
   		cellFieldCenter = new Double3D(globalParameters.getWidthInMikron()/2d, getKeratinoHeight()/2d, globalParameters.getLengthInMikron()/2d);
   	}
   	cellFieldRadius =((globalParameters.getWidthInMikron()-getKeratinoWidth())/2d);
   	
   	externalForce=new Vector3d(0,0,0);      
   	
      if(cell != null && cell.getMotherCell() != null && cell.getMotherCell().getEpisimBioMechanicalModelObject() != null){
      	double motherCellWidth= 1;
      	motherCellWidth= ((AdhesiveCenterBased3DModel)(cell.getMotherCell().getEpisimBioMechanicalModelObject())).getKeratinoWidth();
      	
      	 if(cell.getMotherCell().getEpisimBioMechanicalModelObject() instanceof AdhesiveCenterBased3DModel){
 	      	EpisimModelConnector motherCellConnector =((AdhesiveCenterBased3DModel) cell.getMotherCell().getEpisimBioMechanicalModelObject()).getEpisimModelConnector();
 	      	if(motherCellConnector instanceof EpisimAdhesiveCenterBased3DMC){
 	      		setKeratinoWidth(((EpisimAdhesiveCenterBased3DMC)motherCellConnector).getWidth()); 
 	      		setKeratinoHeight(((EpisimAdhesiveCenterBased3DMC)motherCellConnector).getHeight());
 	      		setKeratinoLength(((EpisimAdhesiveCenterBased3DMC)motherCellConnector).getLength());	    	     
 	      	}
 	      }  
	      
      	
	      Double3D oldLoc= cellField.getObjectLocation(cell.getMotherCell());	   
	      if(oldLoc != null){
	      	
		      Vector3d directionVect = new Vector3d(globalParameters.getWidthInMikron()/2d, getKeratinoHeight()/2d, globalParameters.getLengthInMikron()/2d);
		      directionVect.sub(new Vector3d(oldLoc.x, oldLoc.y, oldLoc.z));
		      directionVect.normalize();
		      directionVect.scale(TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5*motherCellWidth);		           
		      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*-0.1;		      
		      Double3D newloc=new Double3D(oldLoc.x + directionVect.x, oldLoc.y+deltaY,  oldLoc.z+directionVect.z);		      
		      cellField.setObjectLocation(cell, newloc);		      
	      }
      }
     
   }
   
  
   
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimAdhesiveCenterBased3DMC){
   		this.modelConnector = (EpisimAdhesiveCenterBased3DMC) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimAdhesiveCenterBased3DMC");
   } 
   
   public EpisimModelConnector getEpisimModelConnector(){
   	return this.modelConnector;
   }
   
   private class HitResult
   {        
       int numhits;    // number of hits
       long otherId; // when only one hit, then how id of this hit (usually this will be the mother)
       long otherMotherId; // mother of other
       Vector3d adhForce;
       boolean motherCellHit;
       HitResult()
       {           
           numhits=0;
           otherId=0;
           otherMotherId=0;
           adhForce=new Vector3d(0,0,0);
           motherCellHit=false;
       }
   }
       
   public HitResult hitsOther(Bag neighbours, Double3D thisloc, boolean finalPosition)
   {
      	 	
       HitResult hitResult=new HitResult();            
       if (neighbours==null || neighbours.numObjs == 0){ 
      	 return hitResult;      
       }
      
       for(int i=0;i<neighbours.numObjs;i++)
       {
          if (!(neighbours.objs[i] instanceof AbstractCell)) continue;
          
       
          AbstractCell other = (AbstractCell)(neighbours.objs[i]);
          if (other != getCell())
          {
         	 AdhesiveCenterBased3DModel mechModelOther = (AdhesiveCenterBased3DModel) other.getEpisimBioMechanicalModelObject();
         	 Double3D otherloc=cellField.getObjectLocation(other);
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             double dz = cellField.tdz(thisloc.z,otherloc.z);
             
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(otherloc.x, otherloc.y, otherloc.z), getKeratinoWidth()/2, getKeratinoHeight()/2, getKeratinoLength()/2);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point3d(otherloc.x, otherloc.y, otherloc.z), new Point3d(thisloc.x, thisloc.y, thisloc.z), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2, mechModelOther.getKeratinoLength()/2);
                          
             double optDistScaled = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), other);
             double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);
           //  System.out.println("Optimal Distance: "+ optDist);
                                     
             double actdist=Math.sqrt(dx*dx+dy*dy+dz*dz);
                   
             if (optDistScaled-actdist>MINDIST) // is the difference from the optimal distance really significant
             {
                double fx=(actdist>0)?(optDistScaled/actdist)*dx-dx:0;    // nur die differenz zum jetzigen abstand draufaddieren
                double fy=(actdist>0)?(optDistScaled/actdist)*dy-dy:0;
                double fz=(actdist>0)?(optDistScaled/actdist)*dz-dz:0;
                                       
                // berechneten Vektor anwenden
                hitResult.numhits++;
                hitResult.otherId=other.getID();
                hitResult.otherMotherId=other.getMotherId();
                if(other.getID() == getCell().getMotherId()) hitResult.motherCellHit = true;
                mechModelOther.externalForce.add(new Vector3d(-fx,-fy,-fz)); //von mir wegzeigende kraefte addieren
                externalForce.add(new Vector3d(fx,fy, fz));                                      
             }             
             else if(((optDistScaled-actdist)<=MINDIST) &&(actdist < optDist*globalParameters.getOptDistanceAdhesionFact())) // attraction forces 
             {
                 double adhfac=getAdhesionFactor(other);                          
                 double sx=dx-dx*optDist/actdist;    // nur die differenz zum jetzigen abstand draufaddieren
                 double sy=dy-dy*optDist/actdist;
                 double sz=dz-dz*optDist/actdist;
                 hitResult.adhForce.add(new Vector3d(-sx*adhfac,-sy*adhfac,-sz*adhfac));                                                                 
             }
           }
         
          StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
          if(membrane != null && membrane.isDiscretizedMembrane()){
         	 //Point3d res = new Point3d(thisloc.x, TissueController.getInstance().getTissueBorder().lowerBoundInMikron(thisloc.x, thisloc.y,thisloc.z),thisloc.z);
         			 
         	 Point3d res =new Point3d(thisloc.x, MIN_Y, thisloc.z);//findMinXZPositionOnBoundary(new Point3d(thisloc.x, thisloc.y,thisloc.z), thisloc.x - (getKeratinoWidth()/2), thisloc.x + (getKeratinoWidth()/2),thisloc.z - (getKeratinoLength()/2), thisloc.z + (getKeratinoLength()/2));
         	 Double3D cellCoordinates = new Double3D(res.x, thisloc.y, res.z);
         	 Double3D membraneReference = membrane.getBasalAdhesionReferenceCoordinates3D(cellCoordinates);
         	 double dx = cellCoordinates.x - membraneReference.x;         	 
         	 double dy = cellCoordinates.y - membraneReference.y;
         	 double dz = cellCoordinates.z - membraneReference.z;
         	 
         	 double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(membraneReference.x, membraneReference.y, membraneReference.z), getKeratinoWidth()/2, getKeratinoHeight()/2, getKeratinoLength()/2);
         	 double actdist=Math.sqrt(dx*dx+dy*dy+dz*dz);
         	 if(actdist < requiredDistanceToMembraneThis*globalParameters.getOptDistanceAdhesionFact()){
         		  double sx=dx-dx*requiredDistanceToMembraneThis/actdist;    
                 double sy=dy-dy*requiredDistanceToMembraneThis/actdist;
                 double sz=dz-dz*requiredDistanceToMembraneThis/actdist;
                 double adhfac=modelConnector.getAdhesionBasalMembrane();
                 double highAdhesionFactDifference = adhfac*globalParameters.getBasalMembraneHighAdhesionFactor() - adhfac;
                 adhfac= adhfac + (highAdhesionFactDifference- highAdhesionFactDifference*(membrane.getContactTimeForReferenceCoordinate3D(cellCoordinates)/((double)globalParameters.getBasalMembraneContactTimeThreshold())));
                 hitResult.adhForce.add(new Vector3d(-sx*adhfac,-sy*adhfac,-sz*adhfac));                 
         	 }
          } 
        }     
       return hitResult;
   }
   
   private double getAdhesionFactor(AbstractCell otherCell){
   	
   	EpisimDifferentiationLevel otherCellDiffLevel = otherCell.getEpisimCellBehavioralModelObject().getDiffLevel();
   	if(otherCellDiffLevel.name().equals(modelConnector.getNameDiffLevelBasalCell())) return modelConnector.getAdhesionBasalCell();
   	else if(otherCellDiffLevel.name().equals(modelConnector.getNameDiffLevelFastDividingCell())) return modelConnector.getAdhesionFastDividingCell();
   	else if(otherCellDiffLevel.name().equals(modelConnector.getNameDiffLevelSuprabasalCell())) return modelConnector.getAdhesionSuprabasalCell();	
   	else if(otherCellDiffLevel.name().equals(modelConnector.getNameDiffLevelEarlySuprabasalCell())) return modelConnector.getAdhesionEarlySuprabasalCell();
   	return 0;
   } 
   
   private static double calculateDistanceToCellCenter(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
		 
		 Vector3d rayDirection = new Vector3d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y), (otherCellCenter.z-cellCenter.z));
		 rayDirection.normalize();
		 //calculates the intersection of an ray with an ellipsoid
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;
		 double cAxis_2=cAxis * cAxis;
		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2))
	            + ((rayDirection.z * rayDirection.z) / (cAxis_2));
	  
	    if (a < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return -1;
	    }
	   double sqrtA = Math.sqrt(a);	 
	   double hit = 1 / sqrtA;
	   double hitsecond = -1*(1 / sqrtA);
	    
	   double linefactor = hit;// < hitsecond ? hit : hitsecond;
	   Point3d intersectionPointEllipsoid = new Point3d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y),(cellCenter.z+ linefactor*rayDirection.z));
	   
	   return cellCenter.distance(intersectionPointEllipsoid);
	}
   
   private double normalizeOptimalDistance(double distance, AbstractCell otherCell){   	
   	return distance*globalParameters.getOptDistanceScalingFactor();   	
   }

	public void setPositionRespectingBounds(Double3D p_potentialLoc)
	{
	   double newx= p_potentialLoc.x <0 ? (getKeratinoWidth()/2): p_potentialLoc.x > globalParameters.getWidthInMikron()?(globalParameters.getWidthInMikron()-(getKeratinoWidth()/2)):p_potentialLoc.x;
	   double newz= p_potentialLoc.z <0 ? (getKeratinoLength()/2): p_potentialLoc.z > globalParameters.getLengthInMikron()?(globalParameters.getLengthInMikron()-(getKeratinoLength()/2)):p_potentialLoc.z;
	   double newy=p_potentialLoc.y;               
	   //double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(p_potentialLoc.x, p_potentialLoc.y, p_potentialLoc.z);
	   if ((newy -(getKeratinoHeight()/2.5))<MIN_Y)
	   {	      
	       Point3d newPoint = calculateLowerBoundaryPositionForCell(new Point3d(newx, newy, newz));
	      
	      // newy = minY+(getKeratinoHeight()/2);//newPoint.y;
	   		newy = newPoint.y;
	   } 
	   if (newx < (cellFieldCenter.x - cellFieldRadius))
      {
          newx=(cellFieldCenter.x - cellFieldRadius);       
      }
      else  if (newx > (cellFieldCenter.x + cellFieldRadius))
      {
          newx=(cellFieldCenter.x + cellFieldRadius);       
      }
      double discriminantZ= Math.sqrt(Math.pow(cellFieldRadius, 2)-Math.pow((newx-cellFieldCenter.x), 2));
      double calcSmallZ= cellFieldCenter.z-discriminantZ;
      double calcBigZ = cellFieldCenter.z +discriminantZ;
      if (newz< calcSmallZ) 
      {
          newz=calcSmallZ;       
      }	      
      else if(newz > calcBigZ) 
      {
          newz=calcBigZ;       
      }
	   Double3D newloc = new Double3D(newx,newy, newz);
	   cellField.setObjectLocation(getCell(), newloc);
	   
	}
	
	public Double3D calcBoundedPos(double xPos, double yPos, double zPos)
	{	
		double newx=0, newy=0, newz=0;	   
	   newx=xPos;   
	   newy=yPos;
	   newz=zPos;
	   
	  // double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newx, newy, newz);           
	           
	   if ((newy -(getKeratinoHeight()/2.5))<MIN_Y)  // border crossed
	   {
	   	 //newy=minY+(getKeratinoHeight()/2);
	       if (newx < (cellFieldCenter.x - cellFieldRadius))
	       {
	           newx=(cellFieldCenter.x - cellFieldRadius);       
	       }
	       else  if (newx > (cellFieldCenter.x + cellFieldRadius))
	       {
	           newx=(cellFieldCenter.x + cellFieldRadius);       
	       }
	       double discriminantZ= Math.sqrt(Math.pow(cellFieldRadius, 2)-Math.pow((newx-cellFieldCenter.x), 2));
	       double calcSmallZ= cellFieldCenter.z-discriminantZ;
	       double calcBigZ = cellFieldCenter.z +discriminantZ;
	       if (newz< calcSmallZ) 
	       {
	           newz=calcSmallZ;       
	       }	      
	       else if(newz > calcBigZ) 
	       {
	           newz=calcBigZ;       
	       }
	       Point3d newPoint = calculateLowerBoundaryPositionForCell(new Point3d(newx, newy, newz));
	       newx = newPoint.x;
	       newy = newPoint.y;
	       newz = newPoint.z;	      
	   }  
	   return new Double3D(newx, newy, newz);       
	}
	
	
	public Point3d calculateLowerBoundaryPositionForCell(Point3d cellPosition){
		Point3d minXPositionOnBoundary = new Point3d(cellPosition.x, MIN_Y, cellPosition.z);//findMinXZPositionOnBoundary(cellPosition, cellPosition.x - (getKeratinoWidth()/2), cellPosition.x + (getKeratinoWidth()/2), cellPosition.z - (getKeratinoLength()/2), cellPosition.z + (getKeratinoLength()/2));
		Vector3d cellPosBoundaryDirVect = new Vector3d((cellPosition.x-minXPositionOnBoundary.x),(cellPosition.y-minXPositionOnBoundary.y),(cellPosition.z-minXPositionOnBoundary.z));
		boolean sittingDirectlyOnMembrane = false;
		if(cellPosBoundaryDirVect.x==0 && cellPosBoundaryDirVect.y==0 && cellPosBoundaryDirVect.z==0){
			minXPositionOnBoundary = new Point3d(cellPosition.x, MIN_Y, cellPosition.z);//findMinXZPositionOnBoundary(new Point3d(cellPosition.x,cellPosition.y+1,cellPosition.z), cellPosition.x - (getKeratinoWidth()/2), cellPosition.x + (getKeratinoWidth()/2), cellPosition.z - (getKeratinoLength()/2), cellPosition.z + (getKeratinoLength()/2));
			cellPosBoundaryDirVect = new Vector3d((cellPosition.x-minXPositionOnBoundary.x),((cellPosition.y+1)-minXPositionOnBoundary.y),(cellPosition.z-minXPositionOnBoundary.z));
			sittingDirectlyOnMembrane= true;
		}
		double requiredDistanceToMembrane = calculateDistanceToCellCenter(new Point3d(getX(), getY(), getZ()), new Point3d(minXPositionOnBoundary.x, minXPositionOnBoundary.y, minXPositionOnBoundary.z), getKeratinoWidth()/2, getKeratinoHeight()/2, getKeratinoLength()/2);
		double actualDistanceToMembrane = sittingDirectlyOnMembrane ? 0:cellPosition.distance(minXPositionOnBoundary);
		
		
	
		if(requiredDistanceToMembrane > actualDistanceToMembrane){
			cellPosBoundaryDirVect.normalize();		
			cellPosBoundaryDirVect.scale(requiredDistanceToMembrane-actualDistanceToMembrane);
			Point3d newPoint = new Point3d((cellPosition.x+cellPosBoundaryDirVect.x),(cellPosition.y+cellPosBoundaryDirVect.y),(cellPosition.z+cellPosBoundaryDirVect.z));
			if(MIN_Y > newPoint.y){
				cellPosBoundaryDirVect.negate();
				newPoint = new Point3d((cellPosition.x+cellPosBoundaryDirVect.x),(cellPosition.y+cellPosBoundaryDirVect.y),(cellPosition.z+cellPosBoundaryDirVect.z));
			}
			if(Math.abs(requiredDistanceToMembrane-actualDistanceToMembrane) > requiredDistanceToMembrane*0.2)return calculateLowerBoundaryPositionForCell(newPoint);
			return newPoint;
		}
		
		
		return new Point3d((cellPosition.x),(cellPosition.y),(cellPosition.z));
	}
	

	
	private Point3d findMinXZPositionOnBoundary(Point3d cellPosition, double minX, double maxX, double minZ, double maxZ){
		double minDist = Double.POSITIVE_INFINITY;
		Point3d actMinPoint=null;
		for(double x = minX; x <= maxX; x+=0.5){
			for(double z = minZ; z <= maxZ; z+=0.5){
			double actY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, cellPosition.y, z);
				Point3d actPos = new Point3d(x, actY, z);
				if(actPos.distance(cellPosition) < minDist){
					minDist= actPos.distance(cellPosition);
					actMinPoint = actPos;
				}	
			}
		}
		return actMinPoint;
		
	}
	
	private double getMaxDisplacementFactor(){
		EpisimDifferentiationLevel cellDiffLevel = getCell().getEpisimCellBehavioralModelObject().getDiffLevel();
   	if(cellDiffLevel.name().equals(modelConnector.getNameDiffLevelSuprabasalCell())
   	   || cellDiffLevel.name().equals(modelConnector.getNameDiffLevelEarlySuprabasalCell())) return MAX_DISPLACEMENT_FACT*1;
		return MAX_DISPLACEMENT_FACT;
	}
  
   
	private void simulateCellSizeChanges(){
		if(modelConnector.getWidth() > 0 && modelConnector.getHeight() > 0 && modelConnector.getLength() > 0){
	  		 if(modelConnector.getWidth() != this.oldDimensions.x || modelConnector.getHeight() != this.oldDimensions.y || modelConnector.getLength() != this.oldDimensions.z){
	  			 double deltaSteps = (double)globalParameters.getCellSizeDeltaSimSteps();
	  			 this.finalDimensions = new Double3D(modelConnector.getWidth(), modelConnector.getHeight(), modelConnector.getLength());
	  			 this.sizeChangeDelta = new Double3D(((this.finalDimensions.x - this.oldDimensions.x)/deltaSteps), ((this.finalDimensions.y - this.oldDimensions.y)/deltaSteps), ((this.finalDimensions.z - this.oldDimensions.z)/deltaSteps));
	  			 this.oldDimensions = new Double3D(modelConnector.getWidth(), modelConnector.getHeight(), modelConnector.getLength());
	  			 this.shapeChangeActive = true;
	  		 }
	  		 if(oldDimensions.x <=0 ) setKeratinoWidth(modelConnector.getWidth());
	  		 if(oldDimensions.y <=0 )setKeratinoHeight(modelConnector.getHeight());
	  		 if(oldDimensions.z <=0 )setKeratinoLength(modelConnector.getLength());
		}
		if(shapeChangeActive){
			this.setKeratinoWidth(this.keratinoWidth + this.sizeChangeDelta.x);
			this.setKeratinoHeight(this.keratinoHeight + this.sizeChangeDelta.y);
			this.setKeratinoLength(this.keratinoLength + this.sizeChangeDelta.z);
			//Double2D oldCellLocation = cellField.getObjectLocation(getCell());
			//Double2D newCellLocation = new Double2D(oldCellLocation.x, oldCellLocation.y+ (this.sizeChangeDelta.y/2d));
			//cellField.setObjectLocation(getCell(),newCellLocation);
			if(Math.abs(this.keratinoWidth - this.finalDimensions.x) < Math.abs(this.sizeChangeDelta.x)
					&& Math.abs(this.keratinoHeight - this.finalDimensions.y) < Math.abs(this.sizeChangeDelta.y)
					&& Math.abs(this.keratinoLength - this.finalDimensions.z) < Math.abs(this.sizeChangeDelta.z)){
				shapeChangeActive = false;
				this.sizeChangeDelta = null;
				this.finalDimensions = null;			
			}
		}
	}
	
   public void newSimStep(long simstepNumber){
   
   	if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof AdhesiveCenterBased3DModelGP){
   		globalParameters = (AdhesiveCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}   	
   	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
   			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
   	if(this.oldDimensions == null) oldDimensions= new Double3D(this.keratinoWidth, this.keratinoHeight,this.keratinoLength);
   	simulateCellSizeChanges();
   	
   	
   	
   	
   	boolean isFixedCell=
   			getCell().getEpisimCellBehavioralModelObject().getDiffLevel().name().equals(modelConnector.getNameDiffLevelFastDividingCell())
   			||(!modelConnector.getIsMigratory() && isHasFixedPosition());
		
   	
   	
   	//this.keratinoHeight = modelConnector.getHeight() > 0 ? modelConnector.getHeight():this.keratinoHeight;
   	//this.keratinoWidth = modelConnector.getWidth() > 0? modelConnector.getWidth() : this.keratinoWidth;   	
   
   	modelConnector.setIsBasal(false);
   	//////////////////////////////////////////////////
		// calculate ACTION force
		//////////////////////////////////////////////////		

		// calc potential location from gravitation and external pressures
	Double3D oldCellLocation = cellField.getObjectLocation(getCell());
	if(oldCellLocation != null){
			if(externalForce.length() > getMaxDisplacementFactor()) externalForce = setVector3dLength(externalForce, getMaxDisplacementFactor());
			
			Double3D randomPositionData = new Double3D(
					globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5),
					globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5),
					globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
			Double3D gravitation = new Double3D(0,globalParameters.getRandomGravity() * (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 1),0);
			Vector3d actionForce = new Vector3d(externalForce.x * globalParameters.getExternalPush()+ randomPositionData.x, 
															externalForce.y * globalParameters.getExternalPush()+randomPositionData.y+gravitation.y,
															externalForce.z * globalParameters.getExternalPush()+ randomPositionData.z);
			Double3D potentialLoc = null;
			
			potentialLoc = new Double3D((actionForce.x + oldCellLocation.x), (actionForce.y + oldCellLocation.y),(actionForce.z + oldCellLocation.z));
			if(isFixedCell) potentialLoc = oldCellLocation;
			externalForce.x = 0; // alles einberechnet
			externalForce.y = 0;
			externalForce.z = 0;
	
			//////////////////////////////////////////////////
			// try ACTION force
			//////////////////////////////////////////////////
			double neighbourhoodDist = Math.max(getKeratinoWidth(), getKeratinoHeight());
			neighbourhoodDist = Math.max(neighbourhoodDist, getKeratinoLength());
			Bag neighbours = cellField.getObjectsWithinDistance(potentialLoc, neighbourhoodDist*globalParameters.getNeighbourhoodOptDistFact(), false); 
			HitResult hitResult1;
			hitResult1 = hitsOther(neighbours, potentialLoc, false);
	
			//////////////////////////////////////////////////
			// estimate optimised POS from REACTION force
			//////////////////////////////////////////////////
			// optimise my own position by giving way to the calculated pressures
			Vector3d reactionForce = new Vector3d(externalForce.x, externalForce.y, externalForce.z);
			reactionForce.add(hitResult1.adhForce);
			//double reactLength= reactionForce.length();
			//double actLength = actionForce.length();
			//double reactionForceLengthScaled = (actLength/(1+ Math.exp((0.75*actLength-reactLength)/0.02*actLength)));
			if(reactionForce.length() > actionForce.length()) reactionForce = setVector3dLength(reactionForce, actionForce.length());
			
			externalForce.x = 0;
			externalForce.y = 0;
			externalForce.z = 0;
			
			// bound also by borders
			double potX = oldCellLocation.x + actionForce.x + reactionForce.x;
			double potY = oldCellLocation.y + actionForce.y + reactionForce.y;
			double potZ = oldCellLocation.z + actionForce.z + reactionForce.z;
			potentialLoc = new Double3D(potX, potY, potZ);
			potentialLoc = calcBoundedPos(potentialLoc.x, potentialLoc.y, potentialLoc.z);
	
		
			if(isFixedCell) potentialLoc = oldCellLocation;
			neighbours = cellField.getObjectsWithinDistance(potentialLoc, neighbourhoodDist*globalParameters.getNeighbourhoodOptDistFact(), false, false); // theEpidermis.neighborhood
			HitResult hitResult2;
			hitResult2 = hitsOther(neighbours, potentialLoc, true);
			externalForce.x = 0;
			externalForce.y = 0;
			externalForce.z = 0;
			// move only on pressure when not stem cell
			
			if(modelConnector.getIsMigratory() && !isHasFixedPosition()){
				if((hitResult2.numhits <2)//){
						 || shapeChangeActive){ //((hitResult2.numhits == 1 && (hitResult2.otherId == getCell().getMotherId() || hitResult2.otherMotherId == getCell().getID())))){				
					setPositionRespectingBounds(potentialLoc);
					positionChangeDeniedCounter=0;
				}
				else{
					positionChangeDeniedCounter++;
				}
			}
			Double3D newCellLocation = cellField.getObjectLocation(getCell());
			//double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newCellLocation.x, newCellLocation.y, newCellLocation.z);
				
			if((newCellLocation.y-getKeratinoHeight()) <= MIN_Y){
				modelConnector.setIsBasal(true);
				StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
				if(membrane != null){
					membrane.inkrementContactTimeForReferenceCoordinate3D(new Double3D(newCellLocation.x, newCellLocation.y, newCellLocation.z));
				}
			}
			else{
				modelConnector.setIsBasal(false);
			}
			
			
			neighbouringCells = new GenericBag<AbstractCell>();
			for(int i = 0; i < neighbours.size(); i++){
				if(neighbours.get(i) instanceof AbstractCell && ((AbstractCell) neighbours.get(i)).getID() != this.getCell().getID()){
					neighbouringCells.add((AbstractCell)neighbours.get(i));
				}
			}
			finalHitResult = hitResult2;	
			modelConnector.setHasCollision(hitsOtherCell() > 0);			
			modelConnector.setX(newCellLocation.getX());
	  	 	modelConnector.setY(newCellLocation.getY());	  	   
	  	 	modelConnector.setZ(newCellLocation.getZ());	  	 
	  }   
   }
      

   @NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood();
   	GenericBag<AbstractCell> neighbourCells = new GenericBag<AbstractCell>();
   	for(int i=0;neighbours != null && i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
  		 	
  		 	
  		 	
  		 if (actNeighbour != getCell())
       {
  			AdhesiveCenterBased3DModel mechModelOther = (AdhesiveCenterBased3DModel) actNeighbour.getEpisimBioMechanicalModelObject();
      	 Double3D otherloc = mechModelOther.getCellLocationInCellField();
      	 double dx = cellField.tdx(getX(),otherloc.x); 
          double dy = cellField.tdy(getY(),otherloc.y);
          double dz = cellField.tdz(getZ(),otherloc.z);
           
          double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point3d(getX(), getY(), getZ()), new Point3d(otherloc.x, otherloc.y, otherloc.z), getKeratinoWidth()/2, getKeratinoHeight()/2, getKeratinoLength()/2);
          double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point3d(otherloc.x, otherloc.y, otherloc.z), new Point3d(getX(), getY(), getZ()), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2, mechModelOther.getKeratinoLength()/2);
          
          double optDist = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), actNeighbour); 
          
          double actDist=Math.sqrt(dx*dx+dy*dy+dz*dz);        
	       if(actDist <= globalParameters.getOptDistanceAdhesionFact()*optDist)neighbourCells.add(actNeighbour);
	     //  System.out.println("Neighbourhood radius: " + (2.5*optDist));
      	 
       }
      }
  	 	return neighbourCells;
   }
   
   public double getKeratinoHeight() {	return keratinoHeight; }	
	public double getKeratinoWidth() { return keratinoWidth; }
	public double getKeratinoLength() { return keratinoLength; }
	
	public void setKeratinoHeight(double keratinoHeight) { this.keratinoHeight = keratinoHeight; }	
	public void setKeratinoWidth(double keratinoWidth) { this.keratinoWidth = keratinoWidth; }
	public void setKeratinoLength(double keratinoLength) { this.keratinoLength = keratinoLength; }

	public int hitsOtherCell(){ return finalHitResult!=null?finalHitResult.numhits:0; }
	
	private GenericBag<AbstractCell> getCellularNeighbourhood() {return neighbouringCells;}
	
	@CannotBeMonitored
	@NoExport
	public double getX(){return modelConnector == null ? 
			0
 		 : modelConnector.getX();
	}
	
	@CannotBeMonitored
	@NoExport
	public double getY(){return modelConnector == null ? 
			 		0	
			 	 : modelConnector.getY();
	}
	
	@CannotBeMonitored
	@NoExport
	public double getZ(){return modelConnector == null ? 
			 		0	
			 	 : modelConnector.getZ();
	}	
			
   protected void clearCellField() {
	   if(!cellField.getAllObjects().isEmpty()){
	   	cellField.clear();
	   }
   }
   protected void resetCellField() {
	   if(!cellField.getAllObjects().isEmpty()){
	   	cellField.clear();
	   }
	   cellField = new Continuous3D(FIELD_RESOLUTION_IN_MIKRON, 
				TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
				TissueController.getInstance().getTissueBorder().getHeightInMikron(),
				TissueController.getInstance().getTissueBorder().getLengthInMikron());
   }
   public void removeCellFromCellField() {
	   cellField.remove(this.getCell());
   }
	
   public void setCellLocationInCellField(Double3D location){
	   cellField.setObjectLocation(this.getCell(), location);
	   if(modelConnector!=null){
	   	modelConnector.setX(location.x);
	   	modelConnector.setY(location.y);
	   	modelConnector.setZ(location.z);
	   }
   }
	
   public Double3D getCellLocationInCellField() {	   
	  return new Double3D(getX(), getY(), getZ());
   }

   protected Object getCellField() {	  
	   return cellField;
   }
   
   private Vector3d setVector3dLength(Vector3d vector, double length)
   {
	   if( length == 0 )
	       return new Vector3d( 0, 0, 0);
	   if( vector.x == 0 && vector.y == 0 && vector.z == 0)
	       return new Vector3d(0, 0, 0);
	   double temp = /*Strict*/Math.sqrt(vector.x*vector.x+vector.y*vector.y +vector.z*vector.z);
	   
	   return new Vector3d( vector.x * length / temp, vector.y * length / temp, vector.z * length / temp );
   }	

	
   protected void newGlobalSimStep(long simStepNumber, SimState state){ /* NOT NEEDED IN THIS MODEL */ }
   
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state){
      // updates the isOuterSurface Flag for the surface exposed cells
      	double binResolutionXInMikron = 1;//CenterBased3DMechanicalModel.INITIAL_KERATINO_WIDTH;
      	double binResolutionZInMikron = 1;//CenterBased3DMechanicalModel.INITIAL_KERATINO_LENGTH*0.8;
      	int MAX_Z_BINS= ((int)(TissueController.getInstance().getTissueBorder().getLengthInMikron()));///binResolutionZInMikron))+1;
    	  	int MAX_X_BINS= ((int)(TissueController.getInstance().getTissueBorder().getWidthInMikron()));///binResolutionXInMikron))+1; 
         AbstractCell[][] x_z_LookUp=new AbstractCell[MAX_Z_BINS][MAX_X_BINS];                                         
         double [][] yLookUp=new double[MAX_Z_BINS][MAX_X_BINS];    
         GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
         if(allCells!= null){
         	AbstractCell[] cellArray = allCells.toArray(new AbstractCell[allCells.size()]);
   	      int numberOfCells = cellArray.length;
   	      double maxYFound = 0;
   	      for (int i=0; i < numberOfCells; i++)
   	      {
   	          // iterate through all cells and determine the KCyte with lowest Y at bin
   	         if(cellArray[i] != null){
   	         	 AdhesiveCenterBased3DModel mechModel = (AdhesiveCenterBased3DModel)cellArray[i].getEpisimBioMechanicalModelObject();
   	         	 mechModel.isSurfaceCell = false;
   		          Double3D loc= mechModel.getCellLocationInCellField();
   		          
   		          double width = mechModel.getKeratinoWidth();
   		          double length = mechModel.getKeratinoLength();
   		          
   		          int xbinRight= Math.round((float) ((loc.x+(width/2)) / binResolutionXInMikron));
   		          int xbinLeft= Math.round((float) ((loc.x-(width/2)) / binResolutionXInMikron));
   		          
   		          int zbinFront= Math.round((float) ((loc.z+(length/2)) / binResolutionZInMikron));
   		          int zbinBack= Math.round((float) ((loc.z-(length/2)) / binResolutionZInMikron));
   		          
   		         
   		          

   			       // calculate score for free bins and add threshhold
   			       double numberOfBinsAssigned = 0;	
   		          for(int m = zbinBack; m <= zbinFront; m++){
   		         	 int zIndex = m < 0 ? x_z_LookUp.length + m : m >= x_z_LookUp.length ? m - x_z_LookUp.length : m;
   		         	 for(int n = xbinLeft; n<= xbinRight; n++){
   		         		 int xIndex = n < 0 ? x_z_LookUp[zIndex].length + n : n >= x_z_LookUp[zIndex].length ? n - x_z_LookUp[zIndex].length : n;
   		         		 if (x_z_LookUp[zIndex][xIndex]==null || loc.y>yLookUp[zIndex][xIndex]) 
   	   		          {
   	   		             x_z_LookUp[zIndex][xIndex]=cellArray[i];                            
   	   		             yLookUp[zIndex][xIndex]=loc.y;
   	   		             numberOfBinsAssigned++;
   	   		          }		         	 
   		         	 }
   		          }
   		          mechModel.surfaceAreaRatio = numberOfBinsAssigned > 0 ? (numberOfBinsAssigned/((double)((xbinRight+1)-xbinLeft)*((zbinFront+1)-zbinBack))) : 0;
   		         
   	         }
   	      }      
   	      for (int z=0; z < MAX_Z_BINS; z++){
   		      for (int x=0; x < MAX_X_BINS; x++)
   		      {  	
   		      	
   		      	if((x_z_LookUp[z][x]==null) || (x_z_LookUp[z][x].getStandardDiffLevel()==StandardDiffLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
   		      	else{
   		      		AdhesiveCenterBased3DModel mechModel = (AdhesiveCenterBased3DModel)x_z_LookUp[z][x].getEpisimBioMechanicalModelObject();
   		      		if(mechModel.surfaceAreaRatio > 0) mechModel.isSurfaceCell = true;
   		      		mechModel.modelConnector.setIsSurface(mechModel.isSurfaceCell);
   		      	}   		      	
   		      }
   	      }   	     
         }
      }
    

   /**
    * Parameter sizeDelta is ignored
    */
   @CannotBeMonitored
   @NoExport  
   public CellBoundaries getCellBoundariesInMikron(double sizeDelta) {
 	  Double3D fieldLocMikron = getCellLocationInCellField();
 	  Vector3d minVector= null;
 	  Vector3d maxVector= null;
 	  double width = getKeratinoWidth()+sizeDelta;
 	  double height = getKeratinoHeight()+sizeDelta;
 	  double length = getKeratinoLength()+sizeDelta;
 	  
 	  minVector = new Vector3d((fieldLocMikron.x-width/2d),
				   						(fieldLocMikron.y-height/2d),
				   						(fieldLocMikron.z-length/2d));

 	  maxVector = new Vector3d((fieldLocMikron.x+width/2d),
										  (fieldLocMikron.y+height/2d),
										  (fieldLocMikron.z+length/2d));
 	   	 
 	 Transform3D trans = new Transform3D();
 	 trans.setTranslation(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z));
 	 trans.setScale(new Vector3d(width/height, height / height, length/height));
 	 return new CellBoundaries(new Ellipsoid(trans, height/2d), minVector, maxVector);
  }

	
   public boolean isHasFixedPosition() {
   
   	return hasFixedPosition;
   }

	
   public void setHasFixedPosition(boolean hasFixedPosition) {
   
   	this.hasFixedPosition = hasFixedPosition;
   }

	
   public double getStandardCellHeight() {
	   return standardCellHeight;
   }

   public void setStandardCellHeight(double val) {
   	this.standardCellHeight = val;	   
   }

   public double getStandardCellWidth() {
	   return this.standardCellWidth;
   }
   
   public void setStandardCellWidth(double val) {
   	this.standardCellWidth = val;
   }

   public double getStandardCellLength() {
	   return this.standardCellLength;
   }
   
   public void setStandardCellLength(double val) {
   	this.standardCellLength = val;
   }	
   public double getCellHeight() {
	   return getKeratinoHeight();
   }

   public double getCellWidth() {
	   return getKeratinoWidth();
   }

   public double getCellLength() {
	   return getCellLength();
   }
   
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   // NOT YET NEEDED METHODS
   //--------------------------------------------------------------------------------------------------------------------------------------------------------------
   @CannotBeMonitored
   @NoExport
   public EpisimCellShape<Shape3D> getPolygonCell() {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape3D> getPolygonCell(EpisimDrawInfo<TransformGroup> info) {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape3D> getPolygonNucleus() {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
   @CannotBeMonitored
   @NoExport
	public EpisimCellShape<Shape3D> getPolygonNucleus(EpisimDrawInfo<TransformGroup> info) {
		//not yet needed
		return new Episim3DCellShape<Shape3D>(new Shape3D());
	}
}

