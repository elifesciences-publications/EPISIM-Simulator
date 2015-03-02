package sim.app.episim.model.biomechanics.centerbased2D.oldmodel;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;













import ec.util.MersenneTwisterFast;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimBiomechanicalModelGlobalParameters;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimCellType;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import episimmcc.EpisimModelConnector;
import episimmcc.centerbased.CenterBasedMechModelInit;
import episimmcc.centerbased.EpisimCenterBasedMC;
import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.UniversalCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractBiomechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim2DCellShape;
import sim.app.episim.model.biomechanics.centerbased2D.AbstractCenterBased2DModel;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;


import sim.app.episim.visualization.CellEllipse;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;


public class CenterBased2DModel extends AbstractCenterBased2DModel {
	
	   
   //The width of the keratinocyte must be bigger or equals the hight
   public static final double INITIAL_KERATINO_HEIGHT=5; // Default: 5
   public static final double INITIAL_KERATINO_WIDTH=5; // Default: 5
   
   public static final double KERATINO_WIDTH_GRANU=9; // default: 10
   public static final double KERATINO_HEIGHT_GRANU=4;
   
   public final double NEXT_TO_OUTERCELL=INITIAL_KERATINO_HEIGHT*1.2;
   private double MINDIST=0.1;   
   
   
   private static final double MAX_DISPLACEMENT_FACT = 0.6;
   
   private double keratinoWidth=-1; // breite keratino
   private double keratinoHeight=-1; // höhe keratino
   
   private Vector2d externalForce = new Vector2d(0,0);
      
   private HitResult finalHitResult;
   
   //maybe more neighbours than real neighbours included inside a circle
   private GenericBag<AbstractCell> neighbouringCells;
   
   private EpisimCenterBasedMC modelConnector;
   
   private boolean isMembraneCell = false;
   
   private CellEllipse cellEllipseObject;
   
   private DrawInfo2D lastDrawInfo2D;  
   
   private static Continuous2D cellField;
   
   private CenterBased2DModelGP globalParameters = null;
   
   private double surfaceAreaRatio =0;
   private boolean isSurfaceCell = false;
   
  
   
   
   public CenterBased2DModel(){
   	this(null);
   }
   
   public CenterBased2DModel(AbstractCell cell){
   	super(cell);
   	
   	if(cellField == null){
   		cellField = new Continuous2D(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNeighborhood_mikron() / 1.5, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
   	}   	
   	
   	externalForce=new Vector2d(0,0);      
      keratinoWidth=INITIAL_KERATINO_WIDTH; 
      keratinoHeight=INITIAL_KERATINO_HEIGHT; 
      if(cell != null && getCellEllipseObject() == null && cell.getEpisimCellBehavioralModelObject() != null){
			cellEllipseObject = new CellEllipse(cell.getID(), getX(), getY(), (double) keratinoWidth, (double)keratinoHeight, Color.BLUE);    
		}
      if(cell != null && cell.getMotherCell() != null){
	      double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.1; 
	             
	      Double2D oldLoc=cellField.getObjectLocation(cell.getMotherCell());	   
	       if(oldLoc != null){
		      Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y+deltaY);		      
		      cellField.setObjectLocation(cell, newloc);		      
		 	  	SimulationDisplayProperties props = ((CenterBased2DModel)cell.getMotherCell().getEpisimBioMechanicalModelObject()).getCellEllipseObject().getLastSimulationDisplayProps();
		 	  	this.setLastSimulationDisplayPropsForNewCellEllipse(props, newloc);
	      }
      }
      lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0));
   }
   
   public boolean isEpidermisDemoModel(){ return modelConnector.isEpidermisDemoModel();}
   
   public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }
   public DrawInfo2D getLastDrawInfo2D(){
   	return this.lastDrawInfo2D;
   }
   
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimCenterBasedMC){
   		this.modelConnector = (EpisimCenterBasedMC) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimCenterBasedModelConnector");
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
       boolean nextToOuterCell;
               
       HitResult()
       {
           nextToOuterCell=false;
           numhits=0;
           otherId=0;
           otherMotherId=0;
           adhForce=new Vector2d(0,0);
       }
   }
       
   public HitResult hitsOther(Bag neighbours, Double2D thisloc, boolean finalPosition)
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
             CenterBased2DModel mechModelOther = (CenterBased2DModel) other.getEpisimBioMechanicalModelObject();
         	 Double2D otherloc=cellField.getObjectLocation(other);
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             
             //double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Vector2d(-1*dx, -1*dy), getKeratinoWidth()/2, getKeratinoHeight()/2);
             //double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Vector2d(dx, dy), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Point2d(otherloc.x, otherloc.y), getKeratinoWidth()/2, getKeratinoHeight()/2);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Point2d(thisloc.x, thisloc.y), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
            
             
             double optDist = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), other);    
           //  System.out.println("Optimal Distance: "+ optDist);
                                     
             double actdist=Math.sqrt(dx*dx+dy*dy);
                   
             if (optDist-actdist>MINDIST) // is the difference from the optimal distance really significant
             {
                double fx=(actdist>0)?(optDist/actdist)*dx-dx:0;    // nur die differenz zum jetzigen abstand draufaddieren
                double fy=(actdist>0)?(optDist/actdist)*dy-dy:0;                                            
                                       
                // berechneten Vektor anwenden
                hitResult.numhits++;
                hitResult.otherId=other.getID();
                hitResult.otherMotherId=other.getMotherId();
                mechModelOther.externalForce.add(new Vector2d(-fx,-fy)); //von mir wegzeigende kraefte addieren
                externalForce.add(new Vector2d(fx,fy));                                      
              }
             if (actdist <= NEXT_TO_OUTERCELL && dy < 0 && mechModelOther.isSurfaceCell){
                    	// lipids do not diffuse
                    hitResult.nextToOuterCell=true; // if the one above is an outer cell, I belong to the barrier 
              }
           }
        }     
       return hitResult;
   }
   private double normalizeOptimalDistance(double distance, AbstractCell otherCell){
   	if(getCell().getStandardDiffLevel()==StandardDiffLevel.GRANUCELL && otherCell.getStandardDiffLevel()==StandardDiffLevel.GRANUCELL){
   		return distance* 0.8;//0.65;
   	}
   	else{
   		return distance*0.8;
   	}
   }
   
   private double calculateDistanceToCellCenter(Point2d cellCenter, Vector2d directionVectorToOtherCell, double aAxis, double bAxis){
   	Vector2d xAxis = new Vector2d(1d,0d); 
   	double angle = directionVectorToOtherCell.angle(xAxis);   	
   	Point2d pointOnMembrane = new Point2d((cellCenter.x+aAxis*Math.cos(angle)), (cellCenter.y+bAxis*Math.sin(angle)));
   	
   	return cellCenter.distance(pointOnMembrane);
   }
   
   private double calculateDistanceToCellCenter(Point2d cellCenter, Point2d otherCellCenter, double aAxis, double bAxis){
		 
		 Vector2d rayDirection = new Vector2d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y));
		 rayDirection.normalize();
		 //calculates the intersection of an ray with an ellipsoid
		 double aAxis_2=aAxis * aAxis;
		 double bAxis_2=bAxis * bAxis;		 
	    double a = ((rayDirection.x * rayDirection.x) / (aAxis_2))
	            + ((rayDirection.y * rayDirection.y) / (bAxis_2));
	  
	    if (a < 0)
	    {
	       System.out.println("Error in optimal Ellipsoid distance calculation"); 
	   	 return -1;
	    }
	   double sqrtA = Math.sqrt(a);	 
	   double hit = 1 / sqrtA;
	   double hitsecond = -1*(1 / sqrtA);
	    
	   double linefactor = hit;// < hitsecond ? hit : hitsecond;
	   Point2d intersectionPointEllipse = new Point2d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y));
	   
	   return cellCenter.distance(intersectionPointEllipse);
	}

	public void setPositionRespectingBounds(Double2D p_potentialLoc)
	{
	   double newx=p_potentialLoc.x;
	   double newy=p_potentialLoc.y;               
	   double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(p_potentialLoc.x, p_potentialLoc.y); 	  
	   if (newy<minY)
	   {	      
	       Point2d newPoint = calculateLowerBoundaryPositionForCell(new Point2d(newx, newy));
	       newx = newPoint.x;
	       newy = newPoint.y;
	   } 
	
	   Double2D newloc = new Double2D(newx,newy);
	   cellField.setObjectLocation(getCell(), newloc);
	}
	public Double2D calcBoundedPos(double xPos, double yPos)
	{	
	   double newx=xPos, newy=yPos;	 
	   double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newx, newy);      
	           
	   if (newy<minY)  // border crossed
	   {
	       if (newy<=0) 
	       {
	           newy=0;       
	       }	
	       Point2d newPoint = calculateLowerBoundaryPositionForCell(new Point2d(newx, newy));
	       newx = newPoint.x;
	       newy = newPoint.y;	      
	   }  
	   return new Double2D(newx, newy);       
	}
	public Point2d calculateLowerBoundaryPositionForCell(Point2d cellPosition){
		Point2d minXPositionOnBoundary = findMinXPositionOnBoundary(cellPosition, cellPosition.x - (getKeratinoWidth()/2), cellPosition.x + (getKeratinoWidth()/2));
		Vector2d cellPosBoundaryDirVect = new Vector2d((cellPosition.x-minXPositionOnBoundary.x),(cellPosition.y-minXPositionOnBoundary.y));
		if(cellPosBoundaryDirVect.x==0 && cellPosBoundaryDirVect.y==0){
			Vector3d tangentVect = new Vector3d();
			tangentVect.x = (cellPosition.x+1)-(cellPosition.x-1);
			tangentVect.y = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x+1, cellPosition.y)-TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x-1, cellPosition.y);
			
			cellPosBoundaryDirVect.x=tangentVect.y;
			cellPosBoundaryDirVect.y=-1*tangentVect.x;
		}
		double distanceCorrectionFactor = 1;
		if((cellPosition.y-(getKeratinoWidth()/2)) <TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x, cellPosition.y)){
			cellPosBoundaryDirVect.negate();
			distanceCorrectionFactor = (getKeratinoWidth()/2)+ cellPosition.distance(minXPositionOnBoundary);
		}
		else{
			distanceCorrectionFactor = (getKeratinoWidth()/2)- cellPosition.distance(minXPositionOnBoundary);
		}
		cellPosBoundaryDirVect.normalize();		
		cellPosBoundaryDirVect.scale(distanceCorrectionFactor);
		return new Point2d((cellPosition.x+cellPosBoundaryDirVect.x),(cellPosition.y+cellPosBoundaryDirVect.y));
	}
	
	private Point2d findMinXPositionOnBoundary(Point2d cellPosition, double minX, double maxX){
		double minDist = Double.POSITIVE_INFINITY;
		Point2d actMinPoint=null;
		for(double x = minX; x <= maxX; x+=0.5){
			double actY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, cellPosition.y);
			Point2d actPos = new Point2d(x, actY);
			if(actPos.distance(cellPosition) < minDist){
				minDist= actPos.distance(cellPosition);
				actMinPoint = actPos;
			}			
		}
		return actMinPoint;
	}
	
	private double getMaxDisplacementFactor(){
		if(getCell().getStandardDiffLevel()==StandardDiffLevel.GRANUCELL){
   		return MAX_DISPLACEMENT_FACT*1.4;
   	}
   	else{
   		return MAX_DISPLACEMENT_FACT;
   	}
		
	}
  
   
   public void newSimStep(long simstepNumber){
   	
   	
   	
   	if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
   			instanceof CenterBased2DModelGP){
   		globalParameters = (CenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}
   	
   	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
   			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
   	
   	
   	
   	
   	//////////////////////////////////////////////////
		// calculate ACTION force
		//////////////////////////////////////////////////
		
		
	if(getCell().getStandardDiffLevel()==StandardDiffLevel.GRANUCELL){
   	 	setKeratinoWidth(CenterBased2DModel.KERATINO_WIDTH_GRANU);
   		setKeratinoHeight(CenterBased2DModel.KERATINO_HEIGHT_GRANU);
   		getCellEllipseObject().setMajorAxisAndMinorAxis(CenterBased2DModel.KERATINO_WIDTH_GRANU, CenterBased2DModel.KERATINO_HEIGHT_GRANU);
   }
	if(getCellEllipseObject() == null){			
			System.out.println("Field cellEllipseObject is not set");
	}
		
		

		// calc potential location from gravitation and external pressures
	Double2D oldCellLocation = cellField.getObjectLocation(getCell());
	if(oldCellLocation != null){
			if(externalForce.length() > getMaxDisplacementFactor()) externalForce = setVector2dLength(externalForce, getMaxDisplacementFactor());
			
			Double2D randomPositionData = new Double2D(globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
					globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
			Vector2d actionForce = new Vector2d(externalForce.x * globalParameters.getExternalPush()+ randomPositionData.x, externalForce.y * globalParameters.getExternalPush()+randomPositionData.y);
			Double2D potentialLoc = null;
			
			potentialLoc = new Double2D(cellField.stx(actionForce.x + oldCellLocation.x), cellField.sty(actionForce.y + oldCellLocation.y));
			
			externalForce.x = 0; // alles einberechnet
			externalForce.y = 0;
	
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
			Vector2d reactionForce = externalForce;
			double reactLength= reactionForce.length();
			double actLength = actionForce.length();
			double reactionForceLengthScaled = (actLength/(1+ Math.exp((0.75*actLength-reactLength)/0.02*actLength)));
			//if(reactionForce.length() > actionForce.length()) 
				reactionForce = setVector2dLength(reactionForce, reactionForceLengthScaled);
	
			
	
			
			
			externalForce.x = 0;
			externalForce.y = 0;
			
			// bound also by borders
			double potX = oldCellLocation.x + actionForce.x + reactionForce.x;
			double potY = oldCellLocation.y + actionForce.y + reactionForce.y;
			potentialLoc = new Double2D(cellField.stx(potX), cellField.sty(potY));
			potentialLoc = calcBoundedPos(potentialLoc.x, potentialLoc.y);
	
			// ////////////////////////////////////////////////
			
	
			neighbours = cellField.getObjectsWithinDistance(potentialLoc, globalParameters.getNeighborhood_mikron(), true, false); // theEpidermis.neighborhood
			HitResult hitResult2;
			hitResult2 = hitsOther(neighbours, potentialLoc, true);
			externalForce.x = 0;
			externalForce.y = 0;
			// move only on pressure when not stem cell
			if(getCell().getStandardDiffLevel()!=StandardDiffLevel.STEMCELL){
				if((hitResult2.numhits == 0)
						|| ((hitResult2.numhits == 1) && ((hitResult2.otherId == getCell().getMotherId()) || (hitResult2.otherMotherId == getCell().getID())))){				
					setPositionRespectingBounds(potentialLoc);
				}
			}
			Double2D newCellLocation = cellField.getObjectLocation(getCell());
			double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newCellLocation.x, newCellLocation.y);
			
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
			
			
			modelConnector.setX(newCellLocation.getX());
	  	 	modelConnector.setY(newCellLocation.getY());
	  	   modelConnector.setIsSurface(this.isSurfaceCell || nextToOuterCell());
	  	   
	  	   this.getCellEllipseObject().setXY(newCellLocation.x, newCellLocation.y);
  	   
		
   }
   
   }
   
   
   public boolean isMembraneCell(){ return isMembraneCell;}
   
   @NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood();
   	GenericBag<AbstractCell> neighbourCells = new GenericBag<AbstractCell>();
   	for(int i=0;i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
  		
       if (actNeighbour != getCell())
       {
         CenterBased2DModel mechModelOther = (CenterBased2DModel) actNeighbour.getEpisimBioMechanicalModelObject();
      	 Double2D otherloc = mechModelOther.getCellLocationInCellField();
      	 double dx = cellField.tdx(getX(),otherloc.x); 
      	 double dy = cellField.tdy(getY(),otherloc.y);
       
	     //  double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(getX(), getY()), new Vector2d(-1*dx, -1*dy), getKeratinoWidth()/2, getKeratinoHeight()/2);
	      // double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Vector2d(dx, dy), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
	       double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(getX(), getY()), new Point2d(otherloc.x, otherloc.y), getKeratinoWidth()/2, getKeratinoHeight()/2);
	       double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Point2d(getX(), getY()), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
	       
	       
	       double optDist = normalizeOptimalDistance((requiredDistanceToMembraneThis+requiredDistanceToMembraneOther), actNeighbour);	                               
	       double actDist=Math.sqrt(dx*dx+dy*dy);	              
	       if(actDist <= globalParameters.getNeighbourhoodOptDistFact()*optDist)neighbourCells.add(actNeighbour);
	     //  System.out.println("Neighbourhood radius: " + (2.5*optDist));
      	 
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
	
	public void setKeratinoHeight(double keratinoHeight) { this.keratinoHeight = keratinoHeight;	}
	
	public void setKeratinoWidth(double keratinoWidth) { this.keratinoWidth = keratinoWidth; }

	public int hitsOtherCell(){ return finalHitResult.numhits; }
	
	public boolean nextToOuterCell(){ return finalHitResult != null ?finalHitResult.nextToOuterCell:false; }

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
		if(this.getCell().convertToStandardDiffLevel(this.getCell().getEpisimCellBehavioralModelObject().getDiffLevel())!= StandardDiffLevel.GRANUCELL){
			return new Episim2DCellShape<Shape>(createHexagonalPolygon(info != null ? info.getDrawInfo(): null, 2, 2));
		}
		return null;
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
    			if(neighbouringCell.getEpisimBioMechanicalModelObject() instanceof CenterBased2DModel){
    				CenterBased2DModel biomechModelNeighbour = (CenterBased2DModel) neighbouringCell.getEpisimBioMechanicalModelObject();
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
	   cellField = new Continuous2D(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNeighborhood_mikron() / 1.5, 
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
				if(cell.getEpisimBioMechanicalModelObject() instanceof CenterBased2DModel){
					CenterBased2DModel mechModel = (CenterBased2DModel) cell.getEpisimBioMechanicalModelObject();
					if(woundArea.contains(mechModel.lastDrawInfo2D.draw.x, mechModel.lastDrawInfo2D.draw.y)&&
							getCell().getStandardDiffLevel()!=StandardDiffLevel.STEMCELL){  
						deadCells.add(cell);
						i++;
					}
					else{
						 if(cell.getEpisimBioMechanicalModelObject() instanceof AbstractBiomechanicalModel){
								AbstractBiomechanical2DModel mechanicalModel = (AbstractBiomechanical2DModel) cell.getEpisimBioMechanicalModelObject();
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
				if(cell.getEpisimBioMechanicalModelObject() instanceof AbstractBiomechanicalModel){
					AbstractBiomechanical2DModel mechanicalModel = (AbstractBiomechanical2DModel) cell.getEpisimBioMechanicalModelObject();
					mechanicalModel.setCellLocationInCellField(map.get(cell.getID()));
				}
			}	   
   }

	
   /*protected void newSimStepGloballyFinished(long simStepNumber){
   	// updates the isOuterSurface Flag for the surface exposed cells
   	double binResolutionInMikron = CenterBasedMechanicalModel.INITIAL_KERATINO_WIDTH;
 	  	int MAX_XBINS= ((int)(TissueController.getInstance().getTissueBorder().getWidthInMikron()/binResolutionInMikron)+1); 
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
		          CenterBasedMechanicalModel mechModel = (CenterBasedMechanicalModel)cellArray[i].getEpisimBioMechanicalModelObject();
		          Double2D loc= mechModel.getCellLocationInCellField();
		          int xbin=(int)(loc.x / binResolutionInMikron);
		          if (xLookUp[xbin]==null || loc.y>yLookUp[xbin]) 
		          {
		             xLookUp[xbin]=cellArray[i];                            
		             yLookUp[xbin]=loc.y;
		          }
	          }
	      }      
	      for (int k=0; k< MAX_XBINS; k++)
	      {
	          if((xLookUp[k]==null) || (xLookUp[k].getEpisimCellBehavioralModelObject().getDiffLevel().ordinal()==EpisimDifferentiationLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
	          else{
	         	 xLookUp[k].setIsOuterCell(true);
	         	 CenterBasedMechanicalModel mechModel = (CenterBasedMechanicalModel)xLookUp[k].getEpisimBioMechanicalModelObject();
	         	 mechModel.modelConnector.setIsSurface(xLookUp[k].getIsOuterCell() || mechModel.nextToOuterCell());
	          }
	      }
      } 
   }*/
	
	protected void newGlobalSimStep(long simStepNumber, SimState state){ /* NOT NEEDED IN THIS MODEL */ }
	
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state){
   	// updates the isOuterSurface Flag for the surface exposed cells
   	double binResolutionInMikron = 1;// CenterBasedMechanicalModel.INITIAL_KERATINO_WIDTH;
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
	         	
		          CenterBased2DModel mechModel = (CenterBased2DModel)cellArray[i].getEpisimBioMechanicalModelObject();
		          mechModel.isSurfaceCell = false;
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
	          if((xLookUp[k]==null) || (xLookUp[k].getStandardDiffLevel()==StandardDiffLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
	          else{
	         	 CenterBased2DModel mechModel = (CenterBased2DModel)xLookUp[k].getEpisimBioMechanicalModelObject();
	         	 if(mechModel.surfaceAreaRatio > 0) mechModel.isSurfaceCell= true;
	         	 mechModel.modelConnector.setIsSurface(mechModel.isSurfaceCell || mechModel.nextToOuterCell());
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

   public double getStandardCellHeight(){
		return CenterBased2DModel.INITIAL_KERATINO_HEIGHT;
   }

	public void setStandardCellHeight(double val){
	   //Do nothing, is constant	   
   }
	
   public double getStandardCellWidth() {
	  return CenterBased2DModel.INITIAL_KERATINO_WIDTH;
   }
	
   public void setStandardCellWidth(double val) {
	   //Do nothing, is constant	   
   }
	
   public double getCellHeight() {	  
	   return getKeratinoHeight();
   }
   
   public double getCellWidth() {	 
	   return getKeratinoWidth();
   }

	
}
