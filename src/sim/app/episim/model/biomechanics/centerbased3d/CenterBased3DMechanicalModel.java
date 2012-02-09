package sim.app.episim.model.biomechanics.centerbased3d;

import java.awt.Color;
import java.awt.Shape;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased3d.EpisimCenterBased3DMC;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanical3DModel;

import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim3DCellShape;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.hexagonbased3d.Ellipsoid;
import sim.app.episim.model.controller.ModelController;

import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;

import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;
import sim.util.Double2D;

import sim.util.Double3D;



public class CenterBased3DMechanicalModel extends AbstractMechanical3DModel{
	
	
   //The width of the keratinocyte must be bigger or equals the hight
   public static final double INITIAL_KERATINO_HEIGHT=5; // Default: 5
   public static final double INITIAL_KERATINO_LENGTH=5; // Default: 5
   public static final double INITIAL_KERATINO_WIDTH=5; // Default: 5
   
   public static final double KERATINO_WIDTH_GRANU=9; // default: 10
   public static final double KERATINO_HEIGHT_GRANU=4;
   public static final double KERATINO_LENGTH_GRANU=4;
   
   private static final double MAX_DISPLACEMENT_FACT = 0.6;
   
   public final int NEXT_TO_OUTERCELL=7;
   private double MINDIST=0.1;   
   
   private double keratinoWidth=-1; 
   private double keratinoLength=-1; 
   private double keratinoHeight=-1; 
   
   private Vector3d externalForce = new Vector3d(0,0,0);

   private Double3D oldCellLocation;
   private Double3D newCellLocation;
   
   private HitResult finalHitResult;
   
   //maybe more neighbours than real neighbours included inside a circle
   private GenericBag<AbstractCell> neighbouringCells;
   
   private EpisimCenterBased3DMC modelConnector;
   
   private boolean isMembraneCell = false;
   
   private static Continuous3D cellField;
  
   public CenterBased3DMechanicalModel(){
   	this(null);
   }
   
   public CenterBased3DMechanicalModel(AbstractCell cell){
   	super(cell);   	
   	if(cellField == null){
   		cellField = new Continuous3D(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNeighborhood_mikron() / 1.5, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron(),
					TissueController.getInstance().getTissueBorder().getLengthInMikron());
   	}   	
   	
   	externalForce=new Vector3d(0,0,0);      
      keratinoWidth=INITIAL_KERATINO_WIDTH; 
      keratinoHeight=INITIAL_KERATINO_HEIGHT; 
      keratinoLength=INITIAL_KERATINO_LENGTH; 
      
     
      if(cell != null && cell.getMotherCell() != null){
	      double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.1; 
	      double deltaZ = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;    
	      
	      Double3D oldLoc=cellField.getObjectLocation(cell.getMotherCell());	   
	       if(oldLoc != null){
		      Double3D newloc=new Double3D(oldLoc.x + deltaX, oldLoc.y+deltaY, oldLoc.z+deltaZ);		      
		      cellField.setObjectLocation(cell, newloc);		      
	      }
      }      
   }
      
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimCenterBased3DMC){
   		this.modelConnector = (EpisimCenterBased3DMC) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimCenterBased3DMC");
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
      
       boolean nextToOuterCell;
               
       HitResult()
       {
           nextToOuterCell=false;
           numhits=0;
           otherId=0;
           otherMotherId=0;
           adhForce=new Vector3d(0,0,0);          
       }
   }
       
   public HitResult hitsOther(Bag neighbours, Double3D thisloc, boolean finalPosition)
   {
       // check of actual position involves a collision, if so return TRUE, otherwise return FALSE
       // for each collision calc a pressure vector and add it to the other's existing one
       HitResult hitResult=new HitResult();            
       if (neighbours==null || neighbours.numObjs == 0) return hitResult;  
          
       for(int i=0;i<neighbours.numObjs;i++)
       {
          if (!(neighbours.objs[i] instanceof AbstractCell)) continue;
          
       
          AbstractCell other = (AbstractCell)(neighbours.objs[i]);
          if (other != getCell())
          {
             Double3D otherloc=cellField.getObjectLocation(other);
             CenterBased3DMechanicalModel mechModelOther = (CenterBased3DMechanicalModel) other.getEpisimBioMechanicalModelObject();
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             double dz = cellField.tdz(thisloc.z,otherloc.z);
              
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(otherloc.x, otherloc.y, otherloc.z), getKeratinoWidth()/2, getKeratinoHeight()/2, getKeratinoLength()/2);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point3d(otherloc.x, otherloc.y, otherloc.z), new Point3d(thisloc.x, thisloc.y, thisloc.z), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2, mechModelOther.getKeratinoLength()/2);
             
             double optDist = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), other); 
             
             double actdist=Math.sqrt(dx*dx+dy*dy+dz*dz);
                   
             if (optDist-actdist>MINDIST) // is the difference from the optimal distance really significant
             {
                double fx=(actdist>0)?(optDist/actdist)*dx-dx:0;    
                double fy=(actdist>0)?(optDist/actdist)*dy-dy:0;
                double fz=(actdist>0)?(optDist/actdist)*dz-dz:0;
                                       
               
                
                
                
                hitResult.numhits++;
                hitResult.otherId=other.getID();
                hitResult.otherMotherId=other.getMotherId();
                ((CenterBased3DMechanicalModel) other.getEpisimBioMechanicalModelObject()).externalForce.add(new Vector3d(-fx,-fy,-fz)); //von mir wegzeigende kraefte addieren
                externalForce.add(new Vector3d(fx,fy,fz));                                      
              }

                  

              if (actdist <= NEXT_TO_OUTERCELL && dy < 0 && other.getIsOuterCell()){
                    	// lipids do not diffuse
                    hitResult.nextToOuterCell=true; // if the one above is an outer cell, I belong to the barrier 
              }
           }
        }     
       return hitResult;
   }  
   
   
   private double getMaxDisplacementFactor(){
		EpisimDifferentiationLevel thisDiffLevel = getCell().getEpisimCellBehavioralModelObject().getDiffLevel();
   	
   	if(thisDiffLevel.ordinal()==EpisimDifferentiationLevel.GRANUCELL){
   		return MAX_DISPLACEMENT_FACT*1.4;
   	}
   	else{
   		return MAX_DISPLACEMENT_FACT;
   	}
		
	}
   
   private double calculateDistanceToCellCenter(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
		 
		 Vector3d rayDirection = new Vector3d((cellCenter.x-otherCellCenter.x), (cellCenter.y-otherCellCenter.y), (cellCenter.z-otherCellCenter.z));
		 rayDirection.normalize();
		 Point3d rayPosition = new Point3d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y), (otherCellCenter.z-cellCenter.z));
		 //calculates the intersection of an ray with an ellipsoid
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;
		 double cAxis_2=cAxis * cAxis;
		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2))
	            + ((rayDirection.z * rayDirection.z) / (cAxis_2));
	 
	    double b = ((2 * rayPosition.x * rayDirection.x) / (aAxis_2))
	            + ((2 * rayPosition.y * rayDirection.y) / (bAxis_2))
	            + ((2 * rayPosition.z * rayDirection.z) / (cAxis_2));
	 
	    double c = ((rayPosition.x * rayPosition.x) / (aAxis_2))
	            + ((rayPosition.y * rayPosition.y) / (bAxis_2))
	            + ((rayPosition.z * rayPosition.z) / (cAxis_2))
	            - 1;
	 
	    double d = ((b * b) - (4.0d * a * c));
	 
	    if (d < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return -1;
	    }
	    else
	    {
	        d = Math.sqrt(d);
	    }	 
	    double hit = (-b + d) / (2.0f * a);
	    double hitsecond = (-b - d) / (2.0f * a);
	    
	    double linefactor = hit < hitsecond ? hit : hitsecond;
	    Point3d intersectionPointEllipsoid = new Point3d((cellCenter.x+ rayPosition.x + linefactor*rayDirection.x),(cellCenter.y+ rayPosition.y + linefactor*rayDirection.y),(cellCenter.z+ rayPosition.z + linefactor*rayDirection.z));
	   
	    return cellCenter.distance(intersectionPointEllipsoid);
	}
   
   private double normalizeOptimalDistance(double distance, AbstractCell otherCell){
   	EpisimDifferentiationLevel thisDiffLevel = getCell().getEpisimCellBehavioralModelObject().getDiffLevel();
   	EpisimDifferentiationLevel otherDiffLevel = otherCell.getEpisimCellBehavioralModelObject().getDiffLevel();
   	if(thisDiffLevel.ordinal()==EpisimDifferentiationLevel.GRANUCELL && otherDiffLevel.ordinal()==EpisimDifferentiationLevel.GRANUCELL){
   		return distance* 0.65;
   	}
   	else{
   		return distance*0.8;
   	}
   }
   
	public void setPositionRespectingBounds(Double3D p_potentialLoc)
	{
	  
	  
	   double newx=p_potentialLoc.x;
	   double newy=p_potentialLoc.y;  
	   double newz=p_potentialLoc.z;
	   double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(p_potentialLoc.x, p_potentialLoc.y, p_potentialLoc.z);  
	   
	  
	   if (newy<minY)
	   {
	   	 Point3d newPoint = calculateLowerBoundaryPositionForCell(new Point3d(newx, newy, newz));
	       newx = newPoint.x;
	       newy = newPoint.y;
	       newz = newPoint.z;
	   }	 
	
	   Double3D newloc = new Double3D(newx,newy,newz);
	   cellField.setObjectLocation(getCell(), newloc);
	}
	
	

	public Double3D calcBoundedPos(Continuous3D pC2dHerd, double xPos, double yPos, double zPos)
	{
	
	   double newx=0, newy=0, newz=0;	   
	   
	   newx=xPos;   
	   newy=yPos;
	   newz=zPos;
	   
	   double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newx, newy, newz);        
	           
	   if ((newy)<minY)  // border crossed
	   {
	       if (newy<=0) 
	       {
	           newy=0;       
	       }	
	       Point3d newPoint = calculateLowerBoundaryPositionForCell(new Point3d(newx, newy, newz));
	       newx = newPoint.x;
	       newy = newPoint.y;
	       newz = newPoint.z;
	   }  
	   return new Double3D(newx, newy, newz);        
	}
	
	public Point3d calculateLowerBoundaryPositionForCell(Point3d cellPosition){
		Point3d minXPositionOnBoundary = findMinXPositionOnBoundary(cellPosition, cellPosition.x - (getKeratinoWidth()/2), cellPosition.x + (getKeratinoWidth()/2));
		Vector3d cellPosBoundaryDirVect = new Vector3d((cellPosition.x-minXPositionOnBoundary.x),(cellPosition.y-minXPositionOnBoundary.y),(cellPosition.z-minXPositionOnBoundary.z));
		if(cellPosBoundaryDirVect.x==0 && cellPosBoundaryDirVect.y==0 && cellPosBoundaryDirVect.z==0){
			Vector3d tangentVect = new Vector3d();
			tangentVect.x = (cellPosition.x+1)-(cellPosition.x-1);
			tangentVect.y = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x+1, cellPosition.y, cellPosition.z)-TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x-1, cellPosition.y, cellPosition.z);
			tangentVect.z = 0;
			cellPosBoundaryDirVect.x=tangentVect.y;
			cellPosBoundaryDirVect.y=-1*tangentVect.x;
			cellPosBoundaryDirVect.z=tangentVect.z;
		}
		double distanceCorrectionFactor = 1;
		if((cellPosition.y-(getKeratinoWidth()/2)) <TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x, cellPosition.y, cellPosition.z)){
			cellPosBoundaryDirVect.negate();
			distanceCorrectionFactor = (getKeratinoWidth()/2)+ cellPosition.distance(minXPositionOnBoundary);
		}
		else{
			distanceCorrectionFactor = (getKeratinoWidth()/2)- cellPosition.distance(minXPositionOnBoundary);
		}
		cellPosBoundaryDirVect.normalize();		
		cellPosBoundaryDirVect.scale(distanceCorrectionFactor);
		return new Point3d((cellPosition.x+cellPosBoundaryDirVect.x),(cellPosition.y+cellPosBoundaryDirVect.y),(cellPosition.z+cellPosBoundaryDirVect.z));
	}
	
	private Point3d findMinXPositionOnBoundary(Point3d cellPosition, double minX, double maxX){
		double minDist = Double.POSITIVE_INFINITY;
		Point3d actMinPoint=null;
		for(double x = minX; x <= maxX; x+=0.5){
			double actY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, cellPosition.y, cellPosition.z);
			Point3d actPos = new Point3d(x, actY,cellPosition.z);
			if(actPos.distance(cellPosition) < minDist){
				minDist= actPos.distance(cellPosition);
				actMinPoint = actPos;
			}			
		}
		return actMinPoint;
	}

  
   
   public void newSimStep(long simstepNumber){
   	
   	CenterBased3DMechanicalModelGP globalParameters = null;
   	
   	if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
   			instanceof CenterBased3DMechanicalModelGP){
   		globalParameters = (CenterBased3DMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}
   	
   	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
   			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
   	
   	
   	
   	
   	//////////////////////////////////////////////////
		// calculate ACTION force
		//////////////////////////////////////////////////
		
		
		if(getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.GRANUCELL){
   	 	setKeratinoWidth(CenterBased3DMechanicalModel.KERATINO_WIDTH_GRANU);
   		setKeratinoHeight(CenterBased3DMechanicalModel.KERATINO_HEIGHT_GRANU);
   		setKeratinoLength(CenterBased3DMechanicalModel.KERATINO_LENGTH_GRANU);   		
   	}
		// calc potential location from gravitation and external pressures
		oldCellLocation = cellField.getObjectLocation(getCell());
		if(oldCellLocation != null){
			
		if(externalForce.length() > getMaxDisplacementFactor()) externalForce = setVector3dLength(externalForce, getMaxDisplacementFactor());
		
		Double3D randi = new Double3D(
				globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
				globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5),
				globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
		Vector3d actionForce = new Vector3d(externalForce.x * globalParameters.getExternalPush() + randi.x, 
														externalForce.y * globalParameters.getExternalPush(),
														externalForce.z * globalParameters.getExternalPush() + randi.z);
		Double3D potentialLoc = null;
		
		potentialLoc = new Double3D(cellField.stx(actionForce.x + oldCellLocation.x), cellField.sty(actionForce.y + oldCellLocation.y), cellField.stz(actionForce.z + oldCellLocation.z));
		
		externalForce.x = 0; // alles einberechnet
		externalForce.y = 0;
		externalForce.z = 0;

		//////////////////////////////////////////////////
		// try ACTION force
		//////////////////////////////////////////////////
		Bag neighbours = cellField.getObjectsWithinDistance(potentialLoc, globalParameters.getNeighborhood_mikron(), false); 
		HitResult hitResult1;
		hitResult1 = hitsOther(neighbours, potentialLoc, false);

		//////////////////////////////////////////////////
		// estimate optimised POS from REACTION force
		//////////////////////////////////////////////////
		// optimise my own position by giving way to the calculated pressures
		Vector3d reactionForce = externalForce;
		hitResult1.adhForce.scale(globalParameters.getCohesion());
		reactionForce.add(hitResult1.adhForce);

		if(reactionForce.length() > actionForce.length())reactionForce = setVector3dLength(reactionForce,actionForce.length());

		externalForce.x = 0;
		externalForce.y = 0;
		externalForce.z = 0;

		// bound also by borders
		double potX = oldCellLocation.x + actionForce.x + reactionForce.x;
		double potY = oldCellLocation.y + actionForce.y + reactionForce.y;
		double potZ = oldCellLocation.z + actionForce.z + reactionForce.z;
		potentialLoc = new Double3D(cellField.stx(potX), cellField.sty(potY), cellField.stz(potZ));
		potentialLoc = calcBoundedPos(cellField, potentialLoc.x, potentialLoc.y, potentialLoc.z);

		// ////////////////////////////////////////////////
		// try optimised POS
		// ////////////////////////////////////////////////
		// check whether there is anything in the way at the new position

		// aufgrund der gnaedigen Kollisionspruefung, die bei Aktueller Zelle
		// (2)
		// und Vorgaenger(1) keine Kollision meldet,
		// ueberlappen beide ein wenig, falls es eng wird. Wird dann die (2)
		// selber nachgefolgt von (3), so wird (2) rausgeschoben, aber (3)
		// nicht
		// damit ueberlappen 3 und 1 und es kommt zum Stillstand.

		neighbours = cellField.getObjectsWithinDistance(potentialLoc, globalParameters.getNeighborhood_mikron(), false);
		HitResult hitResult2;
		hitResult2 = hitsOther(neighbours, potentialLoc, true);

		// move only on pressure when not stem cell
		if(getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL){
			if((hitResult2.numhits == 0)
					|| ((hitResult2.numhits == 1) && ((hitResult2.otherId == getCell().getMotherId()) || (hitResult2.otherMotherId == getCell().getID())))){
				
				setPositionRespectingBounds(potentialLoc);
			}
		}

		newCellLocation = cellField.getObjectLocation(getCell());
		double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newCellLocation.x, newCellLocation.y, newCellLocation.z);
		if(((newCellLocation.y-(getKeratinoWidth()/2))-minY) < globalParameters.getBasalLayerWidth())
			getCell().setIsBasalStatisticsCell(true);
		else
			getCell().setIsBasalStatisticsCell(false); 

		if(((newCellLocation.y-(getKeratinoWidth()/2))-minY) < globalParameters.getMembraneCellsWidthInMikron()){
			modelConnector.setIsMembrane(true);
			this.isMembraneCell = true;
		}
		else{
			modelConnector.setIsMembrane(false);
			isMembraneCell = false;
		}
		
		
		neighbouringCells = new GenericBag<AbstractCell>();
		for(int i = 0; i < neighbours.size(); i++){
			if(neighbours.get(i) instanceof AbstractCell && ((AbstractCell) neighbours.get(i)).getID() != this.getCell().getID()){
				neighbouringCells.add((AbstractCell)neighbours.get(i));
			}
		}
		finalHitResult = hitResult2;
		modelConnector.setHasCollision(hitsOtherCell() != 0);
		
		
		modelConnector.setX(getNewPosition().getX());
  	 	modelConnector.setY(getNewPosition().getY());
  	 	modelConnector.setZ(getNewPosition().getZ());
  	   modelConnector.setIsSurface(this.getCell().getIsOuterCell() || nextToOuterCell());		
   }
   
   }   
  
   
   
   
   
   
   public boolean isMembraneCell(){ return isMembraneCell;}
   
   @NoExport
   public GenericBag<AbstractCell> getRealNeighbours(){
   	GenericBag<AbstractCell> neighbours = getNeighbouringCells();
   	GenericBag<AbstractCell> neighbourCells = new GenericBag<AbstractCell>();
   	for(int i=0;neighbours != null && i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
         neighbourCells.add(actNeighbour);    
      }
  	 	return neighbourCells;
   }
      
   
   
   
   
   public double getKeratinoHeight() {	return keratinoHeight; }	
	public double getKeratinoWidth() {return keratinoWidth;}
	public double getKeratinoLength() {return keratinoLength;}
	
	public void setKeratinoHeight(double keratinoHeight) { this.keratinoHeight = keratinoHeight;	}
	
	public void setKeratinoWidth(double keratinoWidth) { this.keratinoWidth = keratinoWidth; }
	
	public void setKeratinoLength(double keratinoLength) { this.keratinoLength = keratinoLength; }

	public Double3D getNewPosition(){ return newCellLocation; }
	public void setNewPosition(Double3D loc){ newCellLocation=loc; }

	public Double3D getOldPosition(){ return oldCellLocation; }
	public void setOldPosition(Double3D loc){ oldCellLocation=loc; }

	public int hitsOtherCell(){ return finalHitResult.numhits; }
	
	public boolean nextToOuterCell(){ return finalHitResult.nextToOuterCell; }

	private GenericBag<AbstractCell> getNeighbouringCells() {return neighbouringCells;}
	
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
	   Double3D loc = cellField.getObjectLocation(getCell());
	   return loc!= null ? loc : new Double3D(-1,-1,-1);
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
	
   protected void newSimStepGloballyFinished(long simStepNumber){
   // updates the isOuterSurface Flag for the surface exposed cells
   	double binResolutionXInMikron = CenterBased3DMechanicalModel.INITIAL_KERATINO_WIDTH;
   	double binResolutionZInMikron = CenterBased3DMechanicalModel.INITIAL_KERATINO_LENGTH;
   	int MAX_Z_BINS= ((int)(TissueController.getInstance().getTissueBorder().getLengthInMikron()/binResolutionZInMikron))+1;
 	  	int MAX_X_BINS= ((int)(TissueController.getInstance().getTissueBorder().getWidthInMikron()/binResolutionXInMikron))+1; 
      AbstractCell[][] x_z_LookUp=new AbstractCell[MAX_Z_BINS][MAX_X_BINS];                                         
      double [][] yLookUp=new double[MAX_Z_BINS][MAX_X_BINS];    
      GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
      if(allCells!= null){
      	AbstractCell[] cellArray = allCells.toArray(new AbstractCell[allCells.size()]);
	      int numberOfCells = cellArray.length;
	      for (int i=0; i < numberOfCells; i++)
	      {
	          // iterate through all cells and determine the KCyte with lowest Y at bin
	         if(cellArray[i] != null){
	         	 cellArray[i].setIsOuterCell(false);
		          CenterBased3DMechanicalModel mechModel = (CenterBased3DMechanicalModel)cellArray[i].getEpisimBioMechanicalModelObject();
		          Double3D loc= mechModel.getCellLocationInCellField();
		          int xbin=(int)(loc.x / binResolutionXInMikron);
		          int zbin=(int)(loc.z / binResolutionZInMikron);
		          if (x_z_LookUp[zbin][xbin]==null || loc.y>yLookUp[zbin][xbin]) 
		          {
		             x_z_LookUp[zbin][xbin]=cellArray[i];                            
		             yLookUp[zbin][xbin]=loc.y;
		          }
	         }
	      }      
	      for (int z=0; z < MAX_Z_BINS; z++){
		      for (int x=0; x < MAX_X_BINS; x++)
		      {
		          if((x_z_LookUp[z][x]==null) || (x_z_LookUp[z][x].getEpisimCellBehavioralModelObject().getDiffLevel().ordinal()==EpisimDifferentiationLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
		          x_z_LookUp[z][x].setIsOuterCell(true);
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
 	  double width = getKeratinoWidth();
 	  double height = getKeratinoHeight();
 	  double length = getKeratinoLength();
 	  
 	  minVector = new Vector3d((fieldLocMikron.x-width/2),
				   						(fieldLocMikron.y-height/2),
				   						(fieldLocMikron.z-length/2));

 	  maxVector = new Vector3d((fieldLocMikron.x+width/2),
										  (fieldLocMikron.y+height/2),
										  (fieldLocMikron.z+length/2));
 	   	 
 	 Transform3D trans = new Transform3D();
 	 trans.setTranslation(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z));
 	 trans.setScale(new Vector3d(width/height, height/height, length/height));
 	 return new CellBoundaries(new Ellipsoid(trans, ((height/2)+sizeDelta)), minVector, maxVector);
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
