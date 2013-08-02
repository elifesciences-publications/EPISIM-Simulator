package sim.app.episim.model.biomechanics.centerbased.adhesion;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased.adhesion.EpisimAdhesiveCenterBasedMC;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim2DCellShape;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.StandardMembrane;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;

public class AdhesiveCenterBasedMechanicalModel extends AbstractMechanical2DModel {
	

   
   private double MINDIST=0.1;   
   
   
   private static final double MAX_DISPLACEMENT_FACT = 1.2;
   
   private double keratinoWidth=-1; // breite keratino
   private double keratinoHeight=-1; // höhe keratino
   
   private Vector2d externalForce = new Vector2d(0,0);
      
   private HitResult finalHitResult;
   
   //maybe more neighbours than real neighbours included inside a circle
   private GenericBag<AbstractCell> neighbouringCells;
   
   private EpisimAdhesiveCenterBasedMC modelConnector;
   
   private CellEllipse cellEllipseObject;
   
   private DrawInfo2D lastDrawInfo2D;  
   
   private static Continuous2D cellField;
   
   private AdhesiveCenterBasedMechanicalModelGP globalParameters = null;
   
   private double surfaceAreaRatio =0;
   
   
   private boolean hasFixedPosition = false;
   
   private boolean shapeChangeActive=false;
   private Double2D finalDimensions = null;
   private Double2D oldDimensions = null;
   private Double2D sizeChangeDelta = null;
   
   private boolean dividesToTheLeft = false;
   
   private int positionChangeDeniedCounter = 0;
   private static double FIELD_RESOLUTION_IN_MIKRON=18;
   
   private final double MIN_Y;
   
   public AdhesiveCenterBasedMechanicalModel(){
   	this(null);
   }
   
   public AdhesiveCenterBasedMechanicalModel(AbstractCell cell){
   	super(cell);
   	
   	if(cellField == null){
   		cellField = new Continuous2D(FIELD_RESOLUTION_IN_MIKRON, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
   		
   	}
   
   	MIN_Y= TissueController.getInstance().getTissueBorder().lowerBoundInMikron(0, 0);
   	externalForce=new Vector2d(0,0);      
   	 if(cell != null && getCellEllipseObject() == null && cell.getEpisimCellBehavioralModelObject() != null){
				cellEllipseObject = new CellEllipse(cell.getID(), getX(), getY(), (double) keratinoWidth, (double)keratinoHeight, Color.BLUE);    
			}
      if(cell != null && cell.getMotherCell() != null && cell.getMotherCell().getEpisimBioMechanicalModelObject() != null){
      	double motherCellWidth= 1;
      	motherCellWidth= ((AdhesiveCenterBasedMechanicalModel)(cell.getMotherCell().getEpisimBioMechanicalModelObject())).getKeratinoWidth();
	      double deltaX = 0;
	      if(((AdhesiveCenterBasedMechanicalModel)(cell.getMotherCell().getEpisimBioMechanicalModelObject())).dividesToTheLeft){
	      	deltaX = -0.25*motherCellWidth-(TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.25*motherCellWidth);
	      }
	      else{
	      	deltaX = 0.25*motherCellWidth +(TissueController.getInstance().getActEpidermalTissue().random.nextDouble())*0.25*motherCellWidth;
	      }	      
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*-0.1; 
	      
	      if(cell.getMotherCell().getEpisimBioMechanicalModelObject() instanceof AdhesiveCenterBasedMechanicalModel){
	      	EpisimModelConnector motherCellConnector =((AdhesiveCenterBasedMechanicalModel) cell.getMotherCell().getEpisimBioMechanicalModelObject()).getEpisimModelConnector();
	      	if(motherCellConnector instanceof EpisimAdhesiveCenterBasedMC){
	      		setKeratinoWidth(((EpisimAdhesiveCenterBasedMC)motherCellConnector).getWidth()); 
	      		setKeratinoHeight(((EpisimAdhesiveCenterBasedMC)motherCellConnector).getHeight()); 
	    	     
	      	}
	      }   
	      
	      Double2D oldLoc=cellField.getObjectLocation(cell.getMotherCell());	   
	       if(oldLoc != null){
		      Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y+deltaY);		      
		      cellField.setObjectLocation(cell, newloc);		      
		 	  	SimulationDisplayProperties props = ((AdhesiveCenterBasedMechanicalModel)cell.getMotherCell().getEpisimBioMechanicalModelObject()).getCellEllipseObject().getLastSimulationDisplayProps();
		 	  	this.setLastSimulationDisplayPropsForNewCellEllipse(props, newloc);
	      }
      }
      lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0));
   }
   
   public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }
   
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimAdhesiveCenterBasedMC){
   		this.modelConnector = (EpisimAdhesiveCenterBasedMC) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimAdhesiveCenterBasedModelConnector");
   } 
   
   public EpisimModelConnector getEpisimModelConnector(){
   	return this.modelConnector;
   }
   
   private class HitResult
   {        
       int numhits;    // number of hits
       long otherId; // when only one hit, then how id of this hit (usually this will be the mother)
       long otherMotherId; // mother of other
       Vector2d adhForce;
       boolean motherCellHit;
       HitResult()
       {
           
           numhits=0;
           otherId=0;
           otherMotherId=0;
           adhForce=new Vector2d(0,0);
           motherCellHit=false;
       }
   }
       
   public HitResult hitsOther(Bag neighbours, Double2D thisloc, boolean finalPosition)
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
         	 AdhesiveCenterBasedMechanicalModel mechModelOther = (AdhesiveCenterBasedMechanicalModel) other.getEpisimBioMechanicalModelObject();
         	 Double2D otherloc=cellField.getObjectLocation(other);
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Vector2d(-1*dx, -1*dy), getKeratinoWidth()/2, getKeratinoHeight()/2);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Vector2d(dx, dy), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
                          
             double optDistScaled = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), other);
             double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);
           //  System.out.println("Optimal Distance: "+ optDist);
                                     
             double actdist=Math.sqrt(dx*dx+dy*dy);
                   
             if (optDistScaled-actdist>MINDIST) // is the difference from the optimal distance really significant
             {
                double fx=(actdist>0)?(optDistScaled/actdist)*dx-dx:0;    // nur die differenz zum jetzigen abstand draufaddieren
                double fy=(actdist>0)?(optDistScaled/actdist)*dy-dy:0;                                            
                                       
                // berechneten Vektor anwenden
                hitResult.numhits++;
                hitResult.otherId=other.getID();
                hitResult.otherMotherId=other.getMotherId();
                if(other.getID() == getCell().getMotherId()) hitResult.motherCellHit = true;
                mechModelOther.externalForce.add(new Vector2d(-fx,-fy)); //von mir wegzeigende kraefte addieren
                externalForce.add(new Vector2d(fx,fy));                                      
             }             
             else if(((optDistScaled-actdist)<=MINDIST) &&(actdist < optDist*globalParameters.getOptDistanceAdhesionFact())) // attraction forces 
             {
                 double adhfac=getAdhesionFactor(other);                          
                 double sx=dx-dx*optDist/actdist;    // nur die differenz zum jetzigen abstand draufaddieren
                 double sy=dy-dy*optDist/actdist;                  
                 hitResult.adhForce.add(new Vector2d(-sx*adhfac,-sy*adhfac));                                                                 
             }
           }
         
          StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
          if(membrane != null && membrane.isDiscretizedMembrane()){
         	 Point2d res = new Point2d(thisloc.x, MIN_Y);//findMinXPositionOnBoundary(new Point2d(thisloc.x, thisloc.y), thisloc.x - (getKeratinoWidth()/2), thisloc.x + (getKeratinoWidth()/2));
         	 Double2D cellCoordinates = new Double2D(res.x, thisloc.y);
         	 Double2D membraneReference = membrane.getBasalAdhesionReferenceCoordinates2D(cellCoordinates);
         	 double dx = cellCoordinates.x - membraneReference.x;
         	 double dy = cellCoordinates.y - membraneReference.y;
         	 double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Vector2d(-1*dx, -1*dy), getKeratinoWidth()/2, getKeratinoHeight()/2);
         	 double actdist=Math.sqrt(dx*dx+dy*dy);
         	 if(actdist < requiredDistanceToMembraneThis*globalParameters.getOptDistanceAdhesionFact()){
         		  double sx=dx-dx*requiredDistanceToMembraneThis/actdist;    
                 double sy=dy-dy*requiredDistanceToMembraneThis/actdist;
                 double adhfac=modelConnector.getAdhesionBasalMembrane();
                 double highAdhesionFactDifference = adhfac*globalParameters.getBasalMembraneHighAdhesionFactor() - adhfac;
                 adhfac= adhfac + (highAdhesionFactDifference- highAdhesionFactDifference*(membrane.getContactTimeForReferenceCoordinate2D(cellCoordinates)/((double)globalParameters.getBasalMembraneContactTimeThreshold())));
                 hitResult.adhForce.add(new Vector2d(-sx*adhfac,-sy*adhfac));                 
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
   

   
   
   
   private double normalizeOptimalDistance(double distance, AbstractCell otherCell){   	
   	return distance*globalParameters.getOptDistanceScalingFactor();   	
   }
   
   private double calculateDistanceToCellCenter(Point2d cellCenter, Vector2d directionVectorToOtherCell, double aAxis, double bAxis){
   	Vector2d xAxis = new Vector2d(1d,0d); 
   	double angle = directionVectorToOtherCell.angle(xAxis);   	
   	Point2d pointOnMembrane = new Point2d((cellCenter.x+aAxis*Math.cos(angle)), (cellCenter.y+bAxis*Math.sin(angle)));   	
   	return cellCenter.distance(pointOnMembrane);
   }

	public void setPositionRespectingBounds(Double2D p_potentialLoc)
	{
	   double newx= p_potentialLoc.x <0 ? (getKeratinoWidth()/2): p_potentialLoc.x > globalParameters.getWidthInMikron()?(globalParameters.getWidthInMikron()-(getKeratinoWidth()/2)):p_potentialLoc.x;
	   double newy=p_potentialLoc.y;               
	  // double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(p_potentialLoc.x, p_potentialLoc.y);
	   if ((newy -(getKeratinoHeight()/2.5))<MIN_Y)
	   {	      
	       Point2d newPoint = calculateLowerBoundaryPositionForCell(new Point2d(newx, newy));
	      
	       newy = newPoint.y;
	   } 
	   
	   Double2D newloc = new Double2D(newx,newy);
	   cellField.setObjectLocation(getCell(), newloc);
	}
	
	public Double2D calcBoundedPos(double xPos, double yPos)
	{	
	   double newx=xPos, newy=yPos;	 
	  // double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newx, newy);      
	           
	   if ((newy -(getKeratinoHeight()/2.5))<MIN_Y)  // border crossed
	   {
	       if (newy<=0) 
	       {
	           newy=0;       
	       }
	       if (newx<=0) 
	       {
	           newx=0;       
	       }
	       Point2d newPoint = calculateLowerBoundaryPositionForCell(new Point2d(newx, newy));
	       newx = newPoint.x;
	       newy = newPoint.y;	      
	   }  
	   return new Double2D(newx, newy);       
	}
	public Point2d calculateLowerBoundaryPositionForCell(Point2d cellPosition){
		Point2d minXPositionOnBoundary = new Point2d(cellPosition.x, MIN_Y);//findMinXPositionOnBoundary(cellPosition, cellPosition.x - (getKeratinoWidth()/2), cellPosition.x + (getKeratinoWidth()/2));
		Vector2d cellPosBoundaryDirVect = new Vector2d((cellPosition.x-minXPositionOnBoundary.x),(cellPosition.y-minXPositionOnBoundary.y));
		if(cellPosBoundaryDirVect.x==0 && cellPosBoundaryDirVect.y==0){
			Vector3d tangentVect = new Vector3d();
			tangentVect.x = (cellPosition.x+1)-(cellPosition.x-1);
			tangentVect.y = 0;//TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x+1, cellPosition.y)-TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x-1, cellPosition.y);
			
			cellPosBoundaryDirVect.x=tangentVect.y;
			cellPosBoundaryDirVect.y=-1*tangentVect.x;
		}
		double distanceCorrectionFactor = 1;
		if((cellPosition.y-(getKeratinoHeight()/2)) <TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x, cellPosition.y)){
			cellPosBoundaryDirVect.negate();
			distanceCorrectionFactor = (getKeratinoHeight()/2)+ cellPosition.distance(minXPositionOnBoundary);
		}
		else{
			distanceCorrectionFactor = (getKeratinoHeight()/2)- cellPosition.distance(minXPositionOnBoundary);
		}
		cellPosBoundaryDirVect.normalize();		
		cellPosBoundaryDirVect.scale(distanceCorrectionFactor);
		return new Point2d((cellPosition.x+cellPosBoundaryDirVect.x),(cellPosition.y+cellPosBoundaryDirVect.y));
	}
	
	private Point2d findMinXPositionOnBoundary(Point2d cellPosition, double minX, double maxX){
		double minDist = Double.POSITIVE_INFINITY;
		Point2d actMinPoint=null;
		for(double x = minX; x <= maxX; x+=0.5){
			double actY = MIN_Y;//TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, cellPosition.y);
			Point2d actPos = new Point2d(x, actY);
			if(actPos.distance(cellPosition) < minDist){
				minDist= actPos.distance(cellPosition);
				actMinPoint = actPos;
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
		if(modelConnector.getWidth() > 0 && modelConnector.getHeight() > 0){
	  		 if(modelConnector.getWidth() != this.oldDimensions.x || modelConnector.getHeight() != this.oldDimensions.y){
	  			 double deltaSteps = (double)globalParameters.getCellSizeDeltaSimSteps();
	  			 this.finalDimensions = new Double2D(modelConnector.getWidth(), modelConnector.getHeight());
	  			 this.sizeChangeDelta = new Double2D(((this.finalDimensions.x - this.oldDimensions.x)/deltaSteps), ((this.finalDimensions.y - this.oldDimensions.y)/deltaSteps));
	  			 this.oldDimensions = new Double2D(modelConnector.getWidth(), modelConnector.getHeight());
	  			 this.shapeChangeActive = true;
	  		 }
	  		 if(oldDimensions.x <=0 ) setKeratinoWidth(modelConnector.getWidth());
	  		 if(oldDimensions.y <=0 )setKeratinoHeight(modelConnector.getHeight());
		}
		if(shapeChangeActive){
			this.setKeratinoWidth(this.keratinoWidth + this.sizeChangeDelta.x);
			this.setKeratinoHeight(this.keratinoHeight + this.sizeChangeDelta.y);
			//Double2D oldCellLocation = cellField.getObjectLocation(getCell());
			//Double2D newCellLocation = new Double2D(oldCellLocation.x, oldCellLocation.y+ (this.sizeChangeDelta.y/2d));
			//cellField.setObjectLocation(getCell(),newCellLocation);
			if(Math.abs(this.keratinoWidth - this.finalDimensions.x) < Math.abs(this.sizeChangeDelta.x)
					&& Math.abs(this.keratinoHeight - this.finalDimensions.y) < Math.abs(this.sizeChangeDelta.y)){
				shapeChangeActive = false;
				this.sizeChangeDelta = null;
				this.finalDimensions = null;			
			}
		}
	}
	
   public void newSimStep(long simstepNumber){
   
   	if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() instanceof AdhesiveCenterBasedMechanicalModelGP){
   		globalParameters = (AdhesiveCenterBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}   	
   	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
   			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
   	if(this.oldDimensions == null) oldDimensions= new Double2D(this.keratinoWidth, this.keratinoHeight);
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
		
		
		if(getCellEllipseObject() == null){			
				System.out.println("Field cellEllipseObject is not set");
		}
		
		

		// calc potential location from gravitation and external pressures
	Double2D oldCellLocation = cellField.getObjectLocation(getCell());
	if(oldCellLocation != null){
			if(externalForce.length() > getMaxDisplacementFactor()) externalForce = setVector2dLength(externalForce, getMaxDisplacementFactor());
			
			Double2D randomPositionData = new Double2D(globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
					globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
			Double2D gravitation = new Double2D(0,globalParameters.getRandomGravity() * (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 1));
			Vector2d actionForce = new Vector2d(externalForce.x * globalParameters.getExternalPush()+ randomPositionData.x, externalForce.y * globalParameters.getExternalPush()+randomPositionData.y+gravitation.y);
			Double2D potentialLoc = null;
			
			potentialLoc = new Double2D((actionForce.x + oldCellLocation.x), (actionForce.y + oldCellLocation.y));
			if(isFixedCell) potentialLoc = oldCellLocation;
			externalForce.x = 0; // alles einberechnet
			externalForce.y = 0;
	
			//////////////////////////////////////////////////
			// try ACTION force
			//////////////////////////////////////////////////
			Bag neighbours = cellField.getObjectsWithinDistance(potentialLoc, getKeratinoWidth()*globalParameters.getNeighbourhoodOptDistFact(), false); 
			HitResult hitResult1;
			hitResult1 = hitsOther(neighbours, potentialLoc, false);
	
			//////////////////////////////////////////////////
			// estimate optimised POS from REACTION force
			//////////////////////////////////////////////////
			// optimise my own position by giving way to the calculated pressures
			Vector2d reactionForce = new Vector2d(externalForce.x, externalForce.y);
			reactionForce.add(hitResult1.adhForce);
			//double reactLength= reactionForce.length();
			//double actLength = actionForce.length();
			//double reactionForceLengthScaled = (actLength/(1+ Math.exp((0.75*actLength-reactLength)/0.02*actLength)));
			if(reactionForce.length() > actionForce.length()) reactionForce = setVector2dLength(reactionForce, actionForce.length());
			
			externalForce.x = 0;
			externalForce.y = 0;
			
			// bound also by borders
			double potX = oldCellLocation.x + actionForce.x + reactionForce.x;
			double potY = oldCellLocation.y + actionForce.y + reactionForce.y;
			potentialLoc = new Double2D((potX), potY);
			potentialLoc = calcBoundedPos(potentialLoc.x, potentialLoc.y);
	
		
			if(isFixedCell) potentialLoc = oldCellLocation;
			neighbours = cellField.getObjectsWithinDistance(potentialLoc, getKeratinoWidth()*globalParameters.getNeighbourhoodOptDistFact(), false, false); // theEpidermis.neighborhood
			HitResult hitResult2;
			hitResult2 = hitsOther(neighbours, potentialLoc, true);
			externalForce.x = 0;
			externalForce.y = 0;
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
			Double2D newCellLocation = cellField.getObjectLocation(getCell());
			//double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newCellLocation.x, newCellLocation.y);
				
			if((newCellLocation.y-getKeratinoHeight()) <= MIN_Y){
				modelConnector.setIsBasal(true);
				getCell().setIsBasalCell(true);
				StandardMembrane membrane = TissueController.getInstance().getTissueBorder().getStandardMembrane();
				if(membrane != null){
					membrane.inkrementContactTimeForReferenceCoordinate2D(new Double2D(newCellLocation.x, newCellLocation.y));
				}
			}
			else{
				modelConnector.setIsBasal(false);
				getCell().setIsBasalCell(false); 
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
	  	   this.getCellEllipseObject().setXY(newCellLocation.x, newCellLocation.y);
	  }
   
   }
      
   @NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood();
   	GenericBag<AbstractCell> neighbourCells = new GenericBag<AbstractCell>();
   	for(int i=0;neighbours!= null &&i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
  		
       if (actNeighbour != getCell())
       {
      	 AdhesiveCenterBasedMechanicalModel mechModelOther = (AdhesiveCenterBasedMechanicalModel) actNeighbour.getEpisimBioMechanicalModelObject();
      	 Double2D otherloc = mechModelOther.getCellLocationInCellField();
      	 double dx = cellField.tdx(getX(),otherloc.x); 
      	 double dy = cellField.tdy(getY(),otherloc.y);
       
	       double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(getX(), getY()), new Vector2d(-1*dx, -1*dy), getKeratinoWidth()/2, getKeratinoHeight()/2);
	       double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Vector2d(dx, dy), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
	       
	       
	       double optDist = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), actNeighbour);	                               
	       double actDist=Math.sqrt(dx*dx+dy*dy);	              
	       if(actDist <= globalParameters.getOptDistanceAdhesionFact()*optDist)neighbourCells.add(actNeighbour);	       	 
       }
      }
  	 	return neighbourCells;
   }
   
   private Vector2d setVector2dLength(Vector2d vector, double length)
   {
	   if( length == 0 )
	       return new Vector2d( 0, 0);
	   if( vector.x == 0 && vector.y == 0)
	       return new Vector2d(0, 0);
	   double temp = /*Strict*/Math.sqrt(vector.x*vector.x+vector.y*vector.y);
	   
	   return new Vector2d(vector.x * length / temp, vector.y * length / temp);
   }	   
   
   public double getKeratinoHeight() {	return keratinoHeight; }
	
	public double getKeratinoWidth() {return keratinoWidth;}
	
	public void setKeratinoHeight(double keratinoHeight) { 
		this.keratinoHeight = keratinoHeight;
		getCellEllipseObject().setMajorAxisAndMinorAxis(getKeratinoWidth(), getKeratinoHeight());
	}
	
	
	
	
	public void setKeratinoWidth(double keratinoWidth) { 
		this.keratinoWidth = keratinoWidth; 
		getCellEllipseObject().setMajorAxisAndMinorAxis(getKeratinoWidth(), getKeratinoHeight());
	}

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
	public double getZ(){ return 0;}
	
	@CannotBeMonitored @NoExport
	public EpisimCellShape<Shape> getPolygonCell(){
		return getPolygonCell(null);
	}
	
	@CannotBeMonitored @NoExport
	public EpisimCellShape<Shape> getPolygonNucleus(){
		return getPolygonNucleus(null);
	}
	
	@CannotBeMonitored @NoExport
	public EpisimCellShape<Shape> getPolygonCell(EpisimDrawInfo<DrawInfo2D> info){
		return new Episim2DCellShape<Shape>(createHexagonalPolygon(info != null ? info.getDrawInfo(): null, getKeratinoWidth(), getKeratinoHeight()));
	}
	
	@CannotBeMonitored
	public EpisimCellShape<Shape> getPolygonNucleus(EpisimDrawInfo<DrawInfo2D> info){
		return new Episim2DCellShape<Shape>(createHexagonalPolygon(info != null ? info.getDrawInfo(): null, getKeratinoWidth()/3, getKeratinoHeight()/3));
	}
	
	@CannotBeMonitored
	@NoExport
   public CellEllipse getCellEllipseObject(){
   	return this.cellEllipseObject;
   }
	
	private Shape createHexagonalPolygon(DrawInfo2D info, double width, double height){	
		
		EpisimGUIState guiState = SimStateServer.getInstance().getEpisimGUIState();
	
		SimulationDisplayProperties props = guiState.getSimulationDisplayProperties(new EpisimDrawInfo<DrawInfo2D>(info));
		double x = getX()*props.displayScaleX;
		double y = getY();
		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		y = heightInMikron - y;
		y*= props.displayScaleY;
				
		x+=props.offsetX;
		y+=props.offsetY;
		
		height *= props.displayScaleY;
		width *= props.displayScaleX;		
		
		Path2D.Double path = new Path2D.Double();
		path.moveTo((x+width/2.0), (y));
		path.lineTo((x+width/4.0), (y-height/2.0));
		path.lineTo((x-width/4.0), (y-height/2.0));
		path.lineTo((x-width/2.0), (y));
		path.lineTo((x-width/4.0), (y+height/2.0));
		path.lineTo((x+width/4.0), (y+height/2.0)); 
		path.closePath();		
      return path;
     // return new Ellipse2D.Double(x-(width/2), y-(height/2), width, height);
	}
	
	public void setLastSimulationDisplayPropsForNewCellEllipse(SimulationDisplayProperties displayProps, Double2D newloc){		
		if(displayProps != null){		
			getCellEllipseObject().setLastSimulationDisplayProps(displayProps, true);
			getCellEllipseObject().setXY(newloc.x, newloc.y);
		}  
	}
	
	
	public void calculateClippedCell(long simstepNumber){
  	 
    	CellEllipse cellEllipseCell = this.getCellEllipseObject();
    	GenericBag<AbstractCell> neighbourhood = this.getCellularNeighbourhood();
    	 
    	if(neighbourhood != null && neighbourhood.size() > 0 && cellEllipseCell.getLastSimulationDisplayProps()!= null){
    		for(AbstractCell neighbouringCell : neighbourhood){
    			if(neighbouringCell.getEpisimBioMechanicalModelObject() instanceof AdhesiveCenterBasedMechanicalModel){
    				AdhesiveCenterBasedMechanicalModel biomechModelNeighbour = (AdhesiveCenterBasedMechanicalModel) neighbouringCell.getEpisimBioMechanicalModelObject();
	 	   		 if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(cellEllipseCell.getId(), biomechModelNeighbour.getCellEllipseObject().getId(), simstepNumber)){
	 	   			 CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(cellEllipseCell.getId(),biomechModelNeighbour.getCellEllipseObject().getId());
	 	   			 EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndIntersectionPoints(cellEllipseCell, biomechModelNeighbour.getCellEllipseObject());
	 	   		 }
 	   		 }
 	   	 }
    	 }    	
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
	   cellField = new Continuous2D(FIELD_RESOLUTION_IN_MIKRON, 
				TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
				TissueController.getInstance().getTissueBorder().getHeightInMikron());
   }
   public void removeCellFromCellField() {
	   cellField.remove(this.getCell());
   }
	
   public void setCellLocationInCellField(Double2D location){
	   cellField.setObjectLocation(this.getCell(), location);
	   if(modelConnector!=null){
	   	modelConnector.setX(location.x);
	   	modelConnector.setY(location.y);
	   }
   }
	
   public Double2D getCellLocationInCellField() {	   
	  return new Double2D(getX(), getY());
   }

   protected Object getCellField() {	  
	   return cellField;
   }   

	protected void removeCellsInWoundArea(GeneralPath woundArea) {
		Iterator<AbstractCell> iter = TissueController.getInstance().getActEpidermalTissue().getAllCells().iterator();
		Map<Long, Double2D> map = new HashMap<Long, Double2D>();
		List<AbstractCell> deadCells = new LinkedList<AbstractCell>();
			int i = 0;
			while(iter.hasNext()){
				AbstractCell cell = iter.next();
				if(cell.getEpisimBioMechanicalModelObject() instanceof AdhesiveCenterBasedMechanicalModel){
					AdhesiveCenterBasedMechanicalModel mechModel = (AdhesiveCenterBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject();
					if(woundArea.contains(mechModel.lastDrawInfo2D.draw.x, mechModel.lastDrawInfo2D.draw.y)&&
							getCell().getStandardDiffLevel()!=StandardDiffLevel.STEMCELL){  
						deadCells.add(cell);
						i++;
					}
					else{
						 if(cell.getEpisimBioMechanicalModelObject() instanceof AbstractMechanicalModel){
								AbstractMechanical2DModel mechanicalModel = (AbstractMechanical2DModel) cell.getEpisimBioMechanicalModelObject();
								map.put(cell.getID(), mechanicalModel.getCellLocationInCellField());
						 }
					}
				}
			}
			for(AbstractCell cell: deadCells){
				cell.killCell();
			}			
			
			ModelController.getInstance().getBioMechanicalModelController().clearCellField();
			for(AbstractCell cell: TissueController.getInstance().getActEpidermalTissue().getAllCells()){
				if(cell.getEpisimBioMechanicalModelObject() instanceof AbstractMechanicalModel){
					AbstractMechanical2DModel mechanicalModel = (AbstractMechanical2DModel) cell.getEpisimBioMechanicalModelObject();
					mechanicalModel.setCellLocationInCellField(map.get(cell.getID()));
				}
			}	   
   }

   
   protected void newSimStepGloballyFinished(long simStepNumber){
   	// updates the isOuterSurface Flag for the surface exposed cells
   	double binResolutionInMikron = 1;
 	  	int MAX_XBINS= ((int)(TissueController.getInstance().getTissueBorder().getWidthInMikron()));///binResolutionInMikron)+1); 
      AbstractCell[] xLookUp=new AbstractCell[MAX_XBINS];                                         
      double [] yLookUp=new double[MAX_XBINS]; 
      GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
      if(allCells!= null){
      	AbstractCell[] cellArray = allCells.toArray(new AbstractCell[allCells.size()]);
	      int numberOfCells = cellArray.length;
	      for (int i=0; i<numberOfCells; i++)
	      {
	          // iterate through all cells and determine the KCyte with lowest Y at bin
	          if(cellArray[i]!=null){
	         	 cellArray[i].setIsOuterCell(false);
	         	 AdhesiveCenterBasedMechanicalModel mechModel = (AdhesiveCenterBasedMechanicalModel)cellArray[i].getEpisimBioMechanicalModelObject();
		          Double2D loc= mechModel.getCellLocationInCellField();
		          double width = mechModel.getKeratinoWidth();
		         
		          int xbinRight= Math.round((float) ((loc.x+(width/2)) / binResolutionInMikron));
		          int xbinLeft= Math.round((float) ((loc.x-(width/2)) / binResolutionInMikron));
		   
		         // calculate score for free bins and add threshhold
		          	 double numberOfBinsAssigned = 0;	
		         	 for(int n = xbinLeft; n <= xbinRight; n++){
		         		 	int index = n < 0 ? xLookUp.length + n : n >= xLookUp.length ? n - xLookUp.length : n;
		         		 	if (xLookUp[index]==null || loc.y>yLookUp[index]){
			         		 	xLookUp[index]=cellArray[i];                            
				             	yLookUp[index]=loc.y;
				             	numberOfBinsAssigned++;
		         		 	}
			          }		        
		         	 mechModel.surfaceAreaRatio = numberOfBinsAssigned > 0 ? (numberOfBinsAssigned/((double)((xbinRight+1)-xbinLeft))) : 0;    
	          }
	      }      
	      for (int k=0; k< MAX_XBINS; k++)
	      {
	          if((xLookUp[k]==null)) continue; // stem cells cannot be outer cells (Assumption)                        
	          else{
	         	 AdhesiveCenterBasedMechanicalModel mechModel = (AdhesiveCenterBasedMechanicalModel)xLookUp[k].getEpisimBioMechanicalModelObject();
	         	 if(mechModel.surfaceAreaRatio > 0) xLookUp[k].setIsOuterCell(true);
	         	 mechModel.modelConnector.setIsSurface(xLookUp[k].getIsOuterCell());
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
   	double x = getX();
		double y = getY();
		
		
		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		y = heightInMikron - y;
		double infoWidth = 1;
		double infoHeight = 1;
		double width = (double)getKeratinoWidth();
		double height = (double)getKeratinoHeight();
		
		width *=1.15;
		height*=1.15;
		
		Path2D.Double path = new Path2D.Double();
		path.moveTo((x+infoWidth/2.0*width), (y));
		path.lineTo((x+infoWidth/4.0*width), (y-infoHeight/2.0*height));
		path.lineTo((x-infoWidth/4.0*width), (y-infoHeight/2.0*height));
		path.lineTo((x-infoWidth/2.0*width), (y));
		path.lineTo((x-infoWidth/4.0*width), (y+infoHeight/2.0*height));
		path.lineTo((x+infoWidth/4.0*width), (y+infoHeight/2.0*height));
		path.closePath();
	  
	   return new CellBoundaries(new Ellipse2D.Double(x-(width/2), y-(height/2), width, height));
   }

	
   public boolean isHasFixedPosition() {
   
   	return hasFixedPosition;
   }

	
   public void setHasFixedPosition(boolean hasFixedPosition) {
   
   	this.hasFixedPosition = hasFixedPosition;
   }

	
   public void setDividesToTheLeft(boolean dividesToTheLeft) {
   
   	this.dividesToTheLeft = dividesToTheLeft;
   }

	
}
