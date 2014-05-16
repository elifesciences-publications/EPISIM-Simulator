package sim.app.episim.model.biomechanics.centerbased.newversion;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.ExceptionDisplayer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.AbstractCenterBasedMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim2DCellShape;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP;
import sim.app.episim.model.biomechanics.centerbased.newversion.chemotaxis.CenterBasedChemotaxisMechanicalModelGP;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Loop;
import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased.newversion.EpisimCenterBasedMC;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;


public class CenterBasedMechanicalModel extends AbstractCenterBasedMechanical2DModel {
	
   
   //The width of the keratinocyte must be bigger or equals the hight
 //  public static final double INITIAL_KERATINO_HEIGHT=5; // Default: 5
 //  public static final double INITIAL_KERATINO_WIDTH=5; // Default: 5
   
 //  public static final double KERATINO_WIDTH_GRANU=9; // default: 10
 //  public static final double KERATINO_HEIGHT_GRANU=4;
   
   public final double NEXT_TO_OUTERCELL_FACT=1.2;
   private double MIN_OVERLAP_MICRON=0.1;   
   
   private boolean isChemotaxisEnabled = false;
   private boolean isContinuousInXDirection = true;
   private boolean isContinuousInYDirection = false;
   
      
   private InteractionResult finalInteractionResult;
   

   private EpisimCenterBasedMC modelConnector;
   
   
   private CellEllipse cellEllipseObject;
   
   private DrawInfo2D lastDrawInfo2D;  
   
   private static Continuous2D cellField;
   
   private CenterBasedMechanicalModelGP globalParameters = null;
   private CenterBasedChemotaxisMechanicalModelGP globalParametersChemotaxis=null;
   
   private double surfaceAreaRatio =0;
   
   private double standardCellHeight = 0;
   private double standardCellWidth = 0;
   
   private static double FIELD_RESOLUTION_IN_MIKRON=7;
   
   private static double DELTA_TIME_IN_SECONDS_PER_EULER_STEP = 36;
   
   private static final double MAX_DISPLACEMENT = 10;
   
   
   
   public CenterBasedMechanicalModel(){
   	this(null);
   }
   
   public CenterBasedMechanicalModel(AbstractCell cell){
   	super(cell);
   	
   	if(cellField == null){
   		cellField = new Continuous2D(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
   	}   	
   	  
      if(cell != null&& getCellEllipseObject() == null){
			cellEllipseObject = new CellEllipse(cell.getID(), getX(), getY(), (double) getCellWidth(), (double)getCellHeight(), Color.BLUE);    
		}
      if(cell != null && cell.getMotherCell() != null){
	      double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025;
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005;
	      
	      if(cell.getMotherCell().getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
	      	EpisimModelConnector motherCellConnector =((CenterBasedMechanicalModel) cell.getMotherCell().getEpisimBioMechanicalModelObject()).getEpisimModelConnector();
	      	if(motherCellConnector instanceof EpisimCenterBasedMC){
	      		setCellWidth(((EpisimCenterBasedMC)motherCellConnector).getWidth()); 
	      		setCellHeight(((EpisimCenterBasedMC)motherCellConnector).getHeight());
	      		cellEllipseObject.setMajorAxisAndMinorAxis(((EpisimCenterBasedMC)motherCellConnector).getWidth(), ((EpisimCenterBasedMC)motherCellConnector).getHeight());
	      	}
	      }
	             
	      Double2D oldLoc=cellField.getObjectLocation(cell.getMotherCell());	   
	       if(oldLoc != null){
		      Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y + deltaY);		      
		      cellField.setObjectLocation(cell, newloc);
		     	SimulationDisplayProperties props = ((CenterBasedMechanicalModel)cell.getMotherCell().getEpisimBioMechanicalModelObject()).getCellEllipseObject().getLastSimulationDisplayProps();
		 	  	this.setLastSimulationDisplayPropsForNewCellEllipse(props, newloc);
	      }
      }
      lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0));
   }
   
   public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }
   public DrawInfo2D getLastDrawInfo2D(){
   	return this.lastDrawInfo2D;
   }
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimCenterBasedMC){
   		this.modelConnector = (EpisimCenterBasedMC) modelConnector;
   		Double2D loc = cellField.getObjectLocation(getCell());
   		if(loc != null){
   			this.modelConnector.setX(loc.x);
   			this.modelConnector.setY(loc.y);
   		}
   		
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimCenterBasedModelConnector");
   } 
   
   public EpisimModelConnector getEpisimModelConnector(){
   	return this.modelConnector;
   }
   
   private class InteractionResult
   {        
       int numhits;    // number of hits
       private Vector2d adhesionForce;
       private Vector2d repulsiveForce;
       private Vector2d chemotacticForce;
       boolean nextToOuterCell;
               
       InteractionResult()
       {
           nextToOuterCell=false;
           numhits=0;
           adhesionForce=new Vector2d(0,0);
           repulsiveForce=new Vector2d(0,0);
           chemotacticForce = new Vector2d(0,0);
       }
       
   }
   
   public InteractionResult calculateRepulsiveAdhesiveAndChemotacticForces(Bag neighbours, Double2D thisloc, boolean finalSimStep)
   {
       // check of actual position involves a collision, if so return TRUE, otherwise return FALSE
       // for each collision calc a pressure vector and add it to the other's existing one
       InteractionResult interactionResult=new InteractionResult();            
       if (neighbours==null || neighbours.numObjs == 0) return interactionResult;
       double majorAxisThis = getCellWidth()/2;
       double minorAxisThis = getCellHeight()/2;
      
       for(int i=0;i<neighbours.numObjs;i++)
       {
          if (!(neighbours.objs[i] instanceof AbstractCell)) continue;
          
       
          AbstractCell other = (AbstractCell)(neighbours.objs[i]);
          if (other != getCell())
          {
             CenterBasedMechanicalModel mechModelOther = (CenterBasedMechanicalModel) other.getEpisimBioMechanicalModelObject();
             double majorAxisOther = mechModelOther.getCellWidth()/2;
             double minorAxisOther = mechModelOther.getCellHeight()/2;
         	 Double2D otherloc=cellField.getObjectLocation(other);
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             
             
             //double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Point2d(otherloc.x, otherloc.y), majorAxisThis, minorAxisThis);
             //double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Point2d(thisloc.x, thisloc.y), majorAxisOther, minorAxisOther);
            
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), 
							otherPosToroidalCorrection(new Point2d(thisloc.x, thisloc.y), new Point2d(otherloc.x, otherloc.y)), 
							majorAxisThis, minorAxisThis);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), 
								otherPosToroidalCorrection(new Point2d(otherloc.x, otherloc.y),new Point2d(thisloc.x, thisloc.y)), 
								majorAxisOther, minorAxisOther);            
             double optDistScaled = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther)*globalParameters.getOptDistanceScalingFactor();
             double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);    
          
                                     
             double actDist=Math.sqrt(dx*dx+dy*dy);
                   
             if (optDistScaled-actDist>MIN_OVERLAP_MICRON && actDist > 0) // is the difference from the optimal distance really significant
             {
                //According to Pathmanathan et al. 2009
            	 double overlap = optDistScaled - actDist;
            	 double stiffness = globalParameters.getRepulSpringStiffness_N_per_micro_m(); //Standard: 2.2x10^-3Nm^-1*1*10^-6 conversion in micron 
            	 double linearToExpMaxOverlapPerc = globalParameters.getLinearToExpMaxOverlap_perc();
            	 double alpha=1;
            	 
            	 //without hard core
            	 double force = overlap<= (optDistScaled*linearToExpMaxOverlapPerc) ? overlap*stiffness: stiffness * (optDistScaled*linearToExpMaxOverlapPerc)*Math.exp(alpha*((overlap/(optDistScaled*linearToExpMaxOverlapPerc))-1));
            	 interactionResult.repulsiveForce.x += force*dx/actDist;
            	 interactionResult.repulsiveForce.y += force*dy/actDist;
                                       
              
                interactionResult.numhits++;
                                                                
              }
            else if(((optDist-actDist)<=-1*MIN_OVERLAP_MICRON) &&(actDist < optDist*globalParameters.getOptDistanceAdhesionFact())) // attraction forces 
             {
            	//contact area approximated according to Dallon and Othmer 2004
            	//calculated for ellipsoids not ellipses
               double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
               double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
               double d_membrane_this=requiredDistanceToMembraneThis;
               double d_membrane_other=requiredDistanceToMembraneOther;
            	double radius_this_square = Math.pow((adh_Dist_Fact*d_membrane_this),2);
            	double radius_other_square = Math.pow((adh_Dist_Fact*d_membrane_other),2);
            	double actDist_square = Math.pow(actDist, 2);
            	double intercell_gap = actDist - optDist;
                               	
            	double contactArea = (Math.PI/(4*actDist_square))*(2*actDist_square*(radius_this_square+radius_other_square)
            																		+2*radius_this_square*radius_other_square
            																		-Math.pow(radius_this_square, 2)-Math.pow(radius_other_square, 2)
            																		-Math.pow(actDist_square, 2));
            	
            	
            //	double contactArea = calculateContactAreaNew(new Point2d(mechModelOther.getX(), mechModelOther.getY()),dy, majorAxisThis, minorAxisThis, majorAxisOther, minorAxisOther, d_membrane_this, d_membrane_other, actDist, optDistScaled);
            	           	
            	double smoothingFunction = (((-1*adh_Dist_Perc*d_membrane_this) < intercell_gap)
            										 && (intercell_gap < (adh_Dist_Perc*d_membrane_this)))
            										 ? Math.abs(Math.sin((0.5*Math.PI)*(intercell_gap/(adh_Dist_Perc*d_membrane_this))))
            										 : 1;
            	double adhesionCoefficient = globalParameters.getAdhSpringStiffness_N_per_square_micro_m()*getAdhesionFactor(other);
            										 
            	//System.out.println("pre-Adhesion: "+((contactArea*smoothingFunction)/sphereArea));									 
            	double adhesion = adhesionCoefficient*(contactArea*smoothingFunction);
            	
            	interactionResult.adhesionForce.x += adhesion*((-dx)/actDist);
            	interactionResult.adhesionForce.y += adhesion*((-dy)/actDist);
            	
             }
             if(this.modelConnector instanceof episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC && finalSimStep){
            	
          		double contactAreaCorrect =0; 
          		if(actDist < optDist*globalParameters.getOptDistanceAdhesionFact()){	
          			contactAreaCorrect= calculateContactAreaNew(otherPosToroidalCorrection(new Point2d(thisloc.x, thisloc.y), new Point2d(mechModelOther.getX(), mechModelOther.getY())),
          					dy, majorAxisThis, minorAxisThis, majorAxisOther, minorAxisOther, getCellLength(), mechModelOther.getCellLength(), requiredDistanceToMembraneThis, requiredDistanceToMembraneOther, actDist, optDist);
          			contactAreaCorrect = Double.isNaN(contactAreaCorrect) || Double.isInfinite(contactAreaCorrect) ? 0: contactAreaCorrect;
          		}
          		((episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC)this.modelConnector).setContactArea(other.getID(), Math.abs(contactAreaCorrect));
          	 }
             if (actDist <= (getCellHeight()*NEXT_TO_OUTERCELL_FACT) && dy < 0 && other.getIsOuterCell()){
                    	
                    interactionResult.nextToOuterCell=true;  
             }
           }          
        }       
       // calculate basal adhesion
       if(modelConnector.getAdhesionBasalMembrane() >=0){
      		Point2d membraneReferencePoint = findReferencePositionOnBoundary(new Point2d(thisloc.x, thisloc.y), thisloc.x - (getCellWidth()/2), thisloc.x + (getCellWidth()/2));
      		double dx = cellField.tdx(thisloc.x,membraneReferencePoint.x); 
            double dy = cellField.tdy(thisloc.y,membraneReferencePoint.y);
            if(dx==0 && dy==0){
            	addRandomBiasToPoint(membraneReferencePoint,  0.1);
            	dx = cellField.tdx(thisloc.x,membraneReferencePoint.x); 
               dy = cellField.tdy(thisloc.y,membraneReferencePoint.y);
            }
      		double distToMembrane = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
      		double optDist = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Point2d(membraneReferencePoint.x, membraneReferencePoint.y), getCellWidth()/2, getCellHeight()/2);
      		     		
      		if(distToMembrane < optDist*globalParameters.getOptDistanceAdhesionFact()){
      			double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
               double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
               double radius_this = (adh_Dist_Fact*optDist);
            	double gap = distToMembrane - optDist;
            	
            	double contactArea = Math.PI*radius_this*(radius_this-distToMembrane);
            	if(this.modelConnector instanceof episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC){
            		((episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC)this.modelConnector).setBmContactArea(contactArea);
            	}
            	
            	double smoothingFunction = (((-1*adh_Dist_Perc*optDist) < gap)
							 && (gap < (adh_Dist_Perc*optDist)))
							 ? Math.abs(Math.sin((0.5*Math.PI)*(gap/(adh_Dist_Perc*optDist))))
							 : 1;
							 
					double adhesionCoefficient = globalParameters.getAdhSpringStiffness_N_per_square_micro_m()*modelConnector.getAdhesionBasalMembrane();
							 
			      double adhesion = adhesionCoefficient*(contactArea*smoothingFunction);
			      
			      interactionResult.adhesionForce.x += adhesion * ((-dx)/distToMembrane);
			      interactionResult.adhesionForce.y += adhesion * ((-dy)/distToMembrane);
      		}else{
      			if(this.modelConnector instanceof episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC){
            		((episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC)this.modelConnector).setBmContactArea(0);
            	}
      		}
       }else{
      	 if(this.modelConnector instanceof episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC){
       		((episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC)this.modelConnector).setBmContactArea(0);
       	}
       }
       if(isChemotaxisEnabled){
				String chemotacticFieldName = ((episimbiomechanics.centerbased.newversion.chemotaxis.EpisimCenterBasedMC)modelConnector).getChemotacticField();
				if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
					ExtraCellularDiffusionField2D ecDiffField =  (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
					if(ecDiffField != null){
						double lambda = ((episimbiomechanics.centerbased.newversion.chemotaxis.EpisimCenterBasedMC)modelConnector).getLambdaChem();
						if(lambda > 0){
							interactionResult.chemotacticForce = ecDiffField.getChemotaxisVectorForCellBoundary(getChemotaxisCellBoundariesInMikron());
							interactionResult.chemotacticForce.scale(lambda);
							interactionResult.chemotacticForce.scale(globalParameters.getRepulSpringStiffness_N_per_micro_m());
						}
					}
				}
			}
      return interactionResult;
   } 
   
   private Point2d addRandomBiasToPoint(Point2d point, double scalingFact){
   	point.setX(point.getX()+((TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5)*scalingFact));
   	point.setY(point.getY()+((TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5)*scalingFact));
   	return point;
   }
   
   private double calculateContactAreaNew(Point2d posOther,double dy, double majorAxisThis, double minorAxisThis, double majorAxisOther, double minorAxisOther, double lengthThis, double lengthOther, double d_membrane_this, double d_membrane_other, double actDist, double optDistScaled){
   	double contactArea = 0;
   	double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
      double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
      double smoothingFunction = 1;
      final double AXIS_RATIO_THRES = 5;
      if(majorAxisThis/minorAxisThis >= AXIS_RATIO_THRES || majorAxisOther/minorAxisOther >=AXIS_RATIO_THRES){
				
			Rectangle2D.Double rect1 = new Rectangle2D.Double(getX()-majorAxisThis, getY()-minorAxisThis, 2*majorAxisThis,2*minorAxisThis);
			Rectangle2D.Double rect2 = new Rectangle2D.Double(posOther.x-majorAxisOther, posOther.y-minorAxisOther, 2*majorAxisOther,2*minorAxisOther);
			Rectangle2D.Double intersectionRectXY = new Rectangle2D.Double();
			Rectangle2D.Double.intersect(rect1, rect2, intersectionRectXY);
			double contactRadiusXY =  intersectionRectXY.height < minorAxisThis && intersectionRectXY.height < minorAxisOther? intersectionRectXY.width : intersectionRectXY.height;
			contactRadiusXY/=2;

			double contactRadiusZY = Math.min(lengthThis, lengthOther);			
			contactRadiusZY/=2;
			
			contactArea = Math.PI*contactRadiusXY*contactRadiusZY;
	}
	else{     
      double r1 = adh_Dist_Fact*d_membrane_this;
      double r2 = adh_Dist_Fact*d_membrane_other;
     
      double r1_scaled = (minorAxisThis/r1)*(majorAxisThis);
      double r2_scaled = (minorAxisOther/r2)*(majorAxisOther);
      
      double actDist_scale = ((r1_scaled/r1)*(r1/(r1+r2))+(r2_scaled/r2)*(r2/(r1+r2)));
      double actDist_scaled = actDist*actDist_scale;
      
      double actDist_square = Math.pow(actDist_scaled, 2);
      
      double radius_this_square = Math.pow(r1_scaled,2);
   	double radius_other_square = Math.pow(r2_scaled,2);
   	
                      	
   	contactArea = (Math.PI/(4*actDist_square))*(2*actDist_square*(radius_this_square+radius_other_square)
   																		+2*radius_this_square*radius_other_square
   																		-Math.pow(radius_this_square, 2)-Math.pow(radius_other_square, 2)
   																		-Math.pow(actDist_square, 2));
   	//double intercell_gap = actDist_scaled - optDistScaled;
   /*	smoothingFunction = (((-1*adh_Dist_Perc*d_membrane_this) < intercell_gap)
				 && (intercell_gap < (adh_Dist_Perc*d_membrane_this)))
				 ? Math.abs(Math.sin((0.5*Math.PI)*(intercell_gap/(adh_Dist_Perc*d_membrane_this))))
				 : 1;*/
	
	}
	
      
	
	//double adhesionCoefficient = globalParameters.getAdhSpringStiffness_N_per_square_micro_m()*getAdhesionFactor(other);
										 
					 
	//double adhesion = adhesionCoefficient*(contactArea*smoothingFunction);
				 return contactArea;
   }
   
     
   private double getAdhesionFactor(AbstractCell otherCell){   	
   	return modelConnector.getAdhesionFactorForCell(otherCell);
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

	public void setPositionRespectingBounds(Point2d cellPosition)
	{
		
		double tissueWidth = TissueController.getInstance().getTissueBorder().getWidthInMikron();
		double tissueHeight = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		double newx = isContinuousInXDirection ? cellField.tx(cellPosition.x): cellPosition.x < 0 ? 0 : cellPosition.x > tissueWidth ? tissueWidth : cellPosition.x;
	   double newy = isContinuousInYDirection ? cellField.ty(cellPosition.y): cellPosition.y < 0 ? 0 : cellPosition.y > tissueHeight ? tissueHeight : cellPosition.y;
	   
	   if(!this.isContinuousInYDirection){
		   
	   	Point2d membraneReferencePoint = findReferencePositionOnBoundary(cellPosition, cellPosition.x - (getCellWidth()/2), cellPosition.x + (getCellWidth()/2));
			
		   double dx = cellField.tdx(cellPosition.x, membraneReferencePoint.x); 
	      double dy = cellField.tdy(cellPosition.y, membraneReferencePoint.y);
	      if(dx==0 && dy==0){
         	addRandomBiasToPoint(membraneReferencePoint,  0.1);
         	dx = cellField.tdx(cellPosition.x,membraneReferencePoint.x); 
            dy = cellField.tdy(cellPosition.y,membraneReferencePoint.y);
         }
			double distToMembrane = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
			double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(cellPosition.x, cellPosition.y), new Point2d(membraneReferencePoint.x, membraneReferencePoint.y), getCellWidth()/2, getCellHeight()/2);
			double optDistScaled = requiredDistanceToMembraneThis*globalParameters.getOptDistanceToBMScalingFactor();
		   
			if(optDistScaled > distToMembrane)
		   {	      
		       Point2d newPoint = calculateLowerBoundaryPositionForCell(new Point2d(newx, newy), membraneReferencePoint, optDistScaled);
		       newx = newPoint.x;
		       newy = newPoint.y;
		   }	
	   }
	   Double2D newloc = new Double2D(newx,newy);
	   setCellLocationInCellField(newloc);
	}
	
	public Point2d calculateLowerBoundaryPositionForCell(Point2d cellPosition, Point2d referencePosition, double optDistance){
		Vector2d cellPosBoundaryDirVect = new Vector2d((cellPosition.x-referencePosition.x),(cellPosition.y-referencePosition.y));
		//Should not be needed as in this case the method is called with a slightly randomly biased reference point
		/*if(cellPosBoundaryDirVect.x == 0 && cellPosBoundaryDirVect.y == 0){
			System.out.println("Das ist so!");
			Vector2d tangentVect = new Vector2d();
			tangentVect.x = (cellPosition.x+1)-(cellPosition.x-1);
			tangentVect.y = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x+1, cellPosition.y)-TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x-1, cellPosition.y);
			
			cellPosBoundaryDirVect.x = tangentVect.y;
			cellPosBoundaryDirVect.y = -1*tangentVect.x;
		}*/
		double distanceCorrectionFactor = 1;
		if(cellPosition.y < TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellPosition.x, cellPosition.y)){
			cellPosBoundaryDirVect.negate();
			distanceCorrectionFactor = optDistance + cellPosition.distance(referencePosition);
		}
		else{
			distanceCorrectionFactor = optDistance - cellPosition.distance(referencePosition);
		}
		cellPosBoundaryDirVect.normalize();		
		cellPosBoundaryDirVect.scale(distanceCorrectionFactor);
		return new Point2d((cellPosition.x+cellPosBoundaryDirVect.x),(cellPosition.y+cellPosBoundaryDirVect.y));
	}
	
	private Point2d findReferencePositionOnBoundary(Point2d cellPosition, double minX, double maxX){
		double minDist = Double.POSITIVE_INFINITY;
		Point2d actMinPoint=null;
		if(!TissueController.getInstance().getTissueBorder().isNoMembraneLoaded()){
			for(double x = minX; x <= maxX; x+=0.5){
				double actY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, cellPosition.y);
				Point2d actPos = new Point2d(x, actY);
				double actDist = actPos.distance(cellPosition);
				if(actDist < minDist){
					minDist= actDist;
					actMinPoint = actPos;
				}			
			}
			return actMinPoint;
		}
		return new Point2d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
   
	private double oldWidth =0;
	private double oldHeight=0;
	
  	
	public void initNewSimStep(){
		if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
   			instanceof CenterBasedMechanicalModelGP){
   		globalParameters = (CenterBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}   	
   	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
   			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
   	
   	
   	boolean shapeChangeActive = ((modelConnector.getHeight() != oldHeight) ||(modelConnector.getWidth() != oldWidth));
   	oldHeight = modelConnector.getHeight();
   	oldWidth = modelConnector.getWidth();
   	if(shapeChangeActive){
   		getCellEllipseObject().setMajorAxisAndMinorAxis(modelConnector.getWidth(), modelConnector.getHeight());
   	}
   	if(getCellEllipseObject() == null){			
			System.out.println("Field cellEllipseObject is not set");
   	}
	}
	
	public void calculateSimStep(boolean finalSimStep){
		if(finalSimStep)this.modelConnector.resetPairwiseParameters();
		//according to Pathmanathan et al.2008
		Double2D oldCellLocation = cellField.getObjectLocation(getCell());
		
		double frictionConstantMedium = 0.0000004;//Galle, Loeffler Drasdo 2005 epithelial cells 0.4 Ns/m (* 10^-6 for conversion to micron )			
				
		if(oldCellLocation != null){
			
			Bag actNeighborBag = cellField.getNeighborsWithinDistance(oldCellLocation, getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), true, true);	
			InteractionResult interactionResult = calculateRepulsiveAdhesiveAndChemotacticForces(actNeighborBag, oldCellLocation, finalSimStep);
						
			if(getCell().getStandardDiffLevel()!=StandardDiffLevel.STEMCELL){		
				
				Double2D randomPositionData = new Double2D(globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
						globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
				
				
				
				
				
				double newX = oldCellLocation.x+randomPositionData.x+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.x+interactionResult.adhesionForce.x+interactionResult.chemotacticForce.x));
				double newY = oldCellLocation.y+randomPositionData.y+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.y+interactionResult.adhesionForce.y+interactionResult.chemotacticForce.y));
				
				if(Math.abs(newX-oldCellLocation.x)> MAX_DISPLACEMENT
						|| Math.abs(newY-oldCellLocation.y)> MAX_DISPLACEMENT){
					System.out.println("Biomechanical Artefakt ");
				}else{
					setPositionRespectingBounds(new Point2d(newX, newY));
				}
				
				
			}			
			finalInteractionResult = interactionResult;
		}
	}
	
	public void finishNewSimStep(){
		Double2D newCellLocation = cellField.getObjectLocation(getCell());
		Point2d minPositionOnBoundary = findReferencePositionOnBoundary(new Point2d(newCellLocation.x, newCellLocation.y), newCellLocation.x - (getCellWidth()/2), newCellLocation.x + (getCellWidth()/2));
		
		double distanceToBasalMembrane = Math.sqrt(Math.pow((newCellLocation.x-minPositionOnBoundary.x), 2)+Math.pow((newCellLocation.y-minPositionOnBoundary.y), 2));	
		
		
		
		if(distanceToBasalMembrane <= ((getCellHeight()))){///2)*globalParameters.getOptDistanceAdhesionFact())){
			modelConnector.setIsBasal(true);			
		}
		else{
			modelConnector.setIsBasal(false);			
		}
		
		
		modelConnector.setHasCollision(hitsOtherCell() > 0);		
		modelConnector.setX(newCellLocation.getX());		
		modelConnector.setY(newCellLocation.getY());
  	 	modelConnector.setEpidermalSurfaceRatio(surfaceAreaRatio);
  	 	modelConnector.setIsSurface(this.getCell().getIsOuterCell());// || nextToOuterCell());
  	 	
  	 	if(modelConnector instanceof episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC){
  	 		episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC mc = (episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC) modelConnector;
  	 		mc.setCellSurfaceArea(getSurfaceArea());
  	 		mc.setCellVolume(getCellVolume());
  	 		mc.setExtCellSpaceVolume(getExtraCellSpaceVolume(mc.getExtCellSpaceMikron()));
  	 	}
  	 	
  	 	
  	   this.getCellEllipseObject().setXY(newCellLocation.x, newCellLocation.y);
	   
	}
	
	private double getSurfaceArea(){
		//this method was implemented according to Knud Thomsens Approximation
		final double p=1.6075d;
		
		double a = modelConnector.getWidth()/2d;
		double b = modelConnector.getHeight()/2d;
		double c = modelConnector.getLength()/2d;
		
		double axisSum = (Math.pow(a, p)*Math.pow(b, p))+(Math.pow(a, p)*Math.pow(c, p))+(Math.pow(b, p)*Math.pow(c, p));
		double p_root = Math.pow((axisSum/3), (1.0/p));
		
		double result= 4*Math.PI*p_root;		
		return result;
	}
	
	private double getCellVolume(){
		double a = modelConnector.getWidth()/2d;
		double b = modelConnector.getHeight()/2d;
		double c = modelConnector.getLength()/2d;		
		double result= (4.0d/3.0d)*Math.PI*a*b*c;		
		return result;
	}
	
	private double getExtraCellSpaceVolume(double extCellSpaceDelta){
		double a = (modelConnector.getWidth()/2d)+extCellSpaceDelta;
		double b = (modelConnector.getHeight()/2d)+extCellSpaceDelta;
		double c = (modelConnector.getLength()/2d)+extCellSpaceDelta;		
		return ((4.0d/3.0d)*Math.PI*a*b*c)-getCellVolume();
	}
			
	
   
 public void newSimStep(long simstepNumber){
	 if(globalParameters == null){
		 if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
		 			instanceof CenterBasedMechanicalModelGP){
		 		globalParameters = (CenterBasedMechanicalModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		 	}   	
		 	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
		 			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
	 
	 }
	 if(globalParameters instanceof CenterBasedChemotaxisMechanicalModelGP){
		 this.globalParametersChemotaxis = ((CenterBasedChemotaxisMechanicalModelGP) globalParameters);
		 this.isChemotaxisEnabled = globalParametersChemotaxis.isChemotaxisEnabled();
		 this.isContinuousInXDirection = globalParametersChemotaxis.isContinousDiffusionInXDirection();
		 this.isContinuousInYDirection = globalParametersChemotaxis.isContinousDiffusionInYDirection();
	 }
 }
   
   @NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood(true);
   	GenericBag<AbstractCell> neighbourCells = new GenericBag<AbstractCell>();
   	
   	for(int i=0;i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
  		
       if (actNeighbour != getCell())
       {
         CenterBasedMechanicalModel mechModelOther = (CenterBasedMechanicalModel) actNeighbour.getEpisimBioMechanicalModelObject();
      	 Double2D otherloc = mechModelOther.getCellLocationInCellField();
      	 double dx = cellField.tdx(getX(),otherloc.x); 
      	 double dy = cellField.tdy(getY(),otherloc.y);
       
      	// double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(getX(), getY()), new Vector2d(-1*dx, -1*dy), getKeratinoWidth()/2, getKeratinoHeight()/2);
	      // double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Vector2d(dx, dy), mechModelOther.getKeratinoWidth()/2, mechModelOther.getKeratinoHeight()/2);
	       double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(getX(), getY()), 
	      		 																					otherPosToroidalCorrection(new Point2d(getX(), getY()), new Point2d(otherloc.x, otherloc.y)), 
	      		 																					getCellWidth()/2, getCellHeight()/2);
	       double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), 
	      		 																						otherPosToroidalCorrection(new Point2d(otherloc.x, otherloc.y),new Point2d(getX(), getY())), 
	      		 																						mechModelOther.getCellWidth()/2, mechModelOther.getCellHeight()/2);
	       
	       
	       double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);	                               
	       double actDist=Math.sqrt(dx*dx+dy*dy);	              
	       if(actDist <= globalParameters.getDirectNeighbourhoodOptDistFact()*optDist){
	      	 neighbourCells.add(actNeighbour);
	       }
	     
      	 
       }
      }
  	 	return neighbourCells;
   }
   
   
   public Point2d otherPosToroidalCorrection(Point2d thisCell, Point2d otherCell)
   {
	   double height = cellField.height;
	   double width = cellField.width;
	   double otherY=-1;
	   double otherX=-1;
	   if (Math.abs(thisCell.y-otherCell.y) <= height / 2) otherY = otherCell.y;
	   else{
	   	otherY = thisCell.y > otherCell.y ? (otherCell.y+height): (otherCell.y-height);
	   }
	   if (Math.abs(thisCell.x-otherCell.x) <= width / 2) otherX = otherCell.x;
	   else{
	   	otherX = thisCell.x > otherCell.x ? (otherCell.x+width): (otherCell.x-width);
	   }
	   return new Point2d(otherX, otherY);
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
   
   public double getCellHeight() {	return modelConnector == null ? 0 : modelConnector.getHeight(); }	
	public double getCellWidth() {return modelConnector == null ? 0 : modelConnector.getWidth();}	
	public double getCellLength() {return modelConnector == null ? 0 : modelConnector.getLength();}
	
	public void setCellHeight(double cellHeight) { if(modelConnector!=null)modelConnector.setHeight(cellHeight>0?cellHeight:getCellHeight());	}	
	public void setCellWidth(double cellWidth) { if(modelConnector!=null)modelConnector.setWidth(cellWidth>0?cellWidth:getCellWidth());	 }	
	public void setCellLength(double cellLength) { if(modelConnector!=null)modelConnector.setLength(cellLength>0?cellLength:getCellLength());	 }

	public int hitsOtherCell(){ return finalInteractionResult.numhits; }
	
	public boolean nextToOuterCell(){ return finalInteractionResult != null ?finalInteractionResult.nextToOuterCell:false; }

	private GenericBag<AbstractCell> getCellularNeighbourhood(boolean toroidal) {
		Bag neighbours = cellField.getNeighborsWithinDistance(cellField.getObjectLocation(getCell()), getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), toroidal, true);
		GenericBag<AbstractCell> neighbouringCells = new GenericBag<AbstractCell>();
		for(int i = 0; i < neighbours.size(); i++){
			if(neighbours.get(i) instanceof AbstractCell && ((AbstractCell) neighbours.get(i)).getID() != this.getCell().getID()){
				neighbouringCells.add((AbstractCell)neighbours.get(i));
			}
		}
		return neighbouringCells;
	}
	
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
		int i = 0;
		return getPolygonNucleus(null);
	}
	
	@CannotBeMonitored @NoExport
	public EpisimCellShape<Shape> getPolygonCell(EpisimDrawInfo<DrawInfo2D> info){
		return new Episim2DCellShape<Shape>(createHexagonalPolygon(info != null ? info.getDrawInfo(): null, getCellWidth(), getCellHeight()));
	}
	
	@CannotBeMonitored
	public EpisimCellShape<Shape> getPolygonNucleus(EpisimDrawInfo<DrawInfo2D> info){
		if(modelConnector instanceof episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC){
			episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC modelConnectorEpidermis = (episimbiomechanics.centerbased.newversion.epidermis.EpisimCenterBasedMC)this.modelConnector;
			if(modelConnectorEpidermis.getIsNucleated()){
				return new Episim2DCellShape<Shape>(createHexagonalPolygon(info != null ? info.getDrawInfo(): null, getCellWidth()/3, getCellHeight()/3));
			}
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
    	GenericBag<AbstractCell> neighbourhood = this.getCellularNeighbourhood(false);
    	 
    	if(neighbourhood != null && neighbourhood.size() > 0 && cellEllipseCell.getLastSimulationDisplayProps()!= null){
    		for(AbstractCell neighbouringCell : neighbourhood){
    			if(neighbouringCell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
    				CenterBasedMechanicalModel biomechModelNeighbour = (CenterBasedMechanicalModel) neighbouringCell.getEpisimBioMechanicalModelObject();
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
	   cellField = new Continuous2D(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
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
				if(cell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
					CenterBasedMechanicalModel mechModel = (CenterBasedMechanicalModel) cell.getEpisimBioMechanicalModelObject();
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
	
  protected void newSimStepGloballyFinished(long simStepNumber, SimState state){
   	final MersenneTwisterFast random = state.random;
   	final GenericBag<AbstractCell> allCells = new GenericBag<AbstractCell>(); 
   	allCells.addAll(TissueController.getInstance().getActEpidermalTissue().getAllCells());
   	double numberOfSeconds = DELTA_TIME_IN_SECONDS_PER_EULER_STEP;
   	if(allCells.size() >0){
   		numberOfSeconds = ((CenterBasedMechanicalModel)allCells.get(0).getEpisimBioMechanicalModelObject()).modelConnector.getNumberOfSecondsPerSimStep();
   	}
   	double numberOfIterationsDouble = (numberOfSeconds/DELTA_TIME_IN_SECONDS_PER_EULER_STEP); //according to Pathmanathan et al.2008
   	if(numberOfIterationsDouble < 1){
   		DELTA_TIME_IN_SECONDS_PER_EULER_STEP=numberOfSeconds;
   		numberOfIterationsDouble=1;
   	}
   	final int numberOfIterations = ((int)numberOfIterationsDouble);
   	for(int i = 0; i<numberOfIterations; i++){
   		allCells.shuffle(random);
   		final int totalCellNumber = allCells.size();
   		final int iterationNo =i;
   		 Loop.withIndex(0, totalCellNumber, new Loop.Each() {
             public void run(int n) {
            		CenterBasedMechanicalModel cellBM = ((CenterBasedMechanicalModel)allCells.get(n).getEpisimBioMechanicalModelObject()); 
         			if(iterationNo == 0) cellBM.initNewSimStep();
         			cellBM.calculateSimStep((iterationNo == (numberOfIterations-1)));
         			if(iterationNo == (numberOfIterations-1)) cellBM.finishNewSimStep();
         			//System.out.println("Zelle: "+n);
             }
         });
   	//	System.out.println("neue iteration");
   	/*for(int cellNo = 0; cellNo < totalCellNumber; cellNo++){
   			CenterBasedMechanicalModel cellBM = ((CenterBasedMechanicalModel)allCells.get(cellNo).getEpisimBioMechanicalModelObject()); 
   			if(i == 0) cellBM.initNewSimStep();
   			cellBM.calculateSimStep((i == (numberOfIterations-1)));
   			if(i == (numberOfIterations-1)) cellBM.finishNewSimStep();
   		}*/
   	}
   	calculateSurfaceCells();
   }
   
   private void calculateSurfaceCells(){
   // updates the isOuterSurface Flag for the surface exposed cells
   	double binResolutionInMikron = 1;// CenterBasedMechanicalModel.INITIAL_KERATINO_WIDTH;
 	  	int MAX_XBINS= ((int)(TissueController.getInstance().getTissueBorder().getWidthInMikron()));///binResolutionInMikron)+1); 
      AbstractCell[] xLookUp=new AbstractCell[MAX_XBINS];                                         
      double [] yLookUp=new double[MAX_XBINS]; 
      GenericBag<AbstractCell> allCells = TissueController.getInstance().getActEpidermalTissue().getAllCells();
      HashMap<Long, Integer> cellIdToNumberOfBinsMap = new HashMap<Long, Integer>();
      if(allCells!= null){
      	AbstractCell[] cellArray = allCells.toArray(new AbstractCell[allCells.size()]);
	      int numberOfCells = cellArray.length;
	      for (int i=0; i<numberOfCells; i++)
	      {
	          // iterate through all cells and determine the KCyte with lowest Y at bin
	          if(cellArray[i]!=null){
	         	 cellArray[i].setIsOuterCell(false);
		          CenterBasedMechanicalModel mechModel = (CenterBasedMechanicalModel)cellArray[i].getEpisimBioMechanicalModelObject();
		          mechModel.surfaceAreaRatio=0;
		          Double2D loc= mechModel.getCellLocationInCellField();
		          double width = mechModel.getCellWidth();
		         
		          int xbinRight= Math.round((float) ((loc.x+(width/2)) / binResolutionInMikron));
		          int xbinLeft= Math.round((float) ((loc.x-(width/2)) / binResolutionInMikron));
		   
		         // calculate score for free bins and add threshhold
		          	 
		         	 for(int n = xbinLeft; n <= xbinRight; n++){
		         		 	int index = n < 0 ? xLookUp.length + n : n >= xLookUp.length ? n - xLookUp.length : n;
		         		 	if(index >=0 && index < xLookUp.length && index < yLookUp.length){
			         		 	if (xLookUp[index]==null || loc.y>yLookUp[index]){
				         		 	xLookUp[index]=cellArray[i];                            
					             	yLookUp[index]=loc.y;	             	
			         		 	}
		         		 	}
		         		 	else{
		         		 		System.out.println("Lookup Error: xbinRight: "+xbinRight+"  xbinLeft: "+xbinLeft+"  index: "+index);
		         		 	}
			          }		        
		         	 cellIdToNumberOfBinsMap.put(cellArray[i].getID(), ((xbinRight+1)-xbinLeft));
	          }
	      }
	      
	      HashMap<Long, Integer> numberOfAssignedBinsMap = new HashMap<Long, Integer>();
	      for (int k=0; k< MAX_XBINS; k++){
	      	  if((xLookUp[k]==null) || (xLookUp[k].getStandardDiffLevel()==StandardDiffLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
		        else{
		      	  long id= xLookUp[k].getID();
		      	  if(numberOfAssignedBinsMap.containsKey(id)){
		      		  int numberOfAssignedBins= numberOfAssignedBinsMap.get(id);
		      		  numberOfAssignedBinsMap.put(id, (numberOfAssignedBins+1));
		      	  }
		      	  else numberOfAssignedBinsMap.put(id, 1);
		        }
	      }	      
	      for (int k=0; k< MAX_XBINS; k++)
	      {
	          if((xLookUp[k]==null) || (xLookUp[k].getStandardDiffLevel()==StandardDiffLevel.STEMCELL)) continue; // stem cells cannot be outer cells (Assumption)                        
	          else{
	         	 CenterBasedMechanicalModel mechModel = (CenterBasedMechanicalModel)xLookUp[k].getEpisimBioMechanicalModelObject();
	         	 
	         	 if(numberOfAssignedBinsMap.containsKey(xLookUp[k].getID())
	         			 &&numberOfAssignedBinsMap.get(xLookUp[k].getID())>0){
	         		 mechModel.surfaceAreaRatio = cellIdToNumberOfBinsMap.get(xLookUp[k].getID())<=0?0d:(double)((double)(numberOfAssignedBinsMap.get(xLookUp[k].getID()))/(double)(cellIdToNumberOfBinsMap.get(xLookUp[k].getID())));
	         		 xLookUp[k].setIsOuterCell(true);
	         	 }
	         	 mechModel.modelConnector.setEpidermalSurfaceRatio(mechModel.surfaceAreaRatio);
	         	 mechModel.modelConnector.setIsSurface(xLookUp[k].getIsOuterCell());// || mechModel.nextToOuterCell());
	          }
	      }
      } 
   }
    
   @CannotBeMonitored
   @NoExport  
   private CellBoundaries getChemotaxisCellBoundariesInMikron() {
   	double x = getX();
		double y = getY();		
		
		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		y = heightInMikron - y;	
		
		double width = (double)getCellWidth();
		double height = (double)getCellHeight();
		
		double deltaFact = globalParametersChemotaxis.getChemotaxisCellSizeDeltaFact();
		width *=deltaFact;
		height *= deltaFact;
		
	   return new CellBoundaries(new Ellipse2D.Double(x-(width/2), y-(height/2), width, height));
   }

   
   @CannotBeMonitored
   @NoExport  
   public CellBoundaries getCellBoundariesInMikron(double sizeDelta) {
   	double x = getX();
		double y = getY();		
		
		double heightInMikron = TissueController.getInstance().getTissueBorder().getHeightInMikron();
		y = heightInMikron - y;	
		
		double width = (double)getCellWidth();
		double height = (double)getCellHeight();
		
		width +=sizeDelta;
		height+=sizeDelta;
		
	   return new CellBoundaries(new Ellipse2D.Double(x-(width/2), y-(height/2), width, height));
   }

	
   public double getStandardCellHeight() {
   	return this.standardCellHeight;
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

	
}
