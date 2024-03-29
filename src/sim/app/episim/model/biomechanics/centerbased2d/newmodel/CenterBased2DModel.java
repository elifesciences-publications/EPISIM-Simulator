package sim.app.episim.model.biomechanics.centerbased2d.newmodel;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import sim.app.episim.EpisimProperties;
import sim.app.episim.EpisimExceptionHandler;
import sim.app.episim.SimStateServer;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractBiomechanical2DModel;
import sim.app.episim.model.biomechanics.AbstractBiomechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim2DCellShape;
import sim.app.episim.model.biomechanics.centerbased2d.AbstractCenterBased2DModel;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.CenterBased2DModelGP;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.chemotaxis.ChemotaxisCenterBased2DModelGP;
import sim.app.episim.model.biomechanics.centerbased2d.newmodel.psoriasis.PsoriasisCenterBased2DModelGP;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField2D;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Loop;
import sim.app.episim.visualization.CellEllipse;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.continuous.Continuous2DExt;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import episimmcc.EpisimModelConnector;
import episimmcc.centerbased2d.newmodel.EpisimCenterBasedMC;


public class CenterBased2DModel extends AbstractCenterBased2DModel {
	
   
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
   
   private static Continuous2DExt cellField;
   
   private CenterBased2DModelGP globalParameters = null;
   private ChemotaxisCenterBased2DModelGP globalParametersChemotaxis=null;
   
   private double surfaceAreaRatio =0;
   private boolean isSurfaceCell = false;
   
   private double standardCellHeight = 0;
   private double standardCellWidth = 0;
   
   private static double FIELD_RESOLUTION_IN_MIKRON=7;
   
   private static double DELTA_TIME_IN_SECONDS_PER_EULER_STEP = 36;
   private static final double DELTA_TIME_IN_SECONDS_PER_EULER_STEP_MAX=36;
   
   private static final double MAX_DISPLACEMENT = 10;
   
   private Double2D cellLocation = null;
   private Double2D newCellLocation = null;
   private GenericBag<AbstractCell> directNeighbours;
   private HashSet<Long> directNeighbourIDs;
   private HashMap<Long, Integer> lostNeighbourContactInSimSteps;
   	
	public double immuneCellYDelta_mikron = 0;
   
   public CenterBased2DModel(){
   	this(null, null);
   }
   
   public CenterBased2DModel(AbstractCell cell, EpisimModelConnector modelConnector){
   	super(cell, modelConnector);
   	
   	if(cellField == null){
   		cellField = new Continuous2DExt(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron());
   	}
   	
		if(globalParameters == null){
	  		 if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
	  		 			instanceof CenterBased2DModelGP){
	  		 		globalParameters = (CenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	  		 	}   	
	  		 	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
	  		 			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
	  	 
	  	 }
   	  
      if(cell != null&& getCellEllipseObject() == null){
			cellEllipseObject = new CellEllipse(cell.getID(), getX(), getY(), (double) getCellWidth(), (double)getCellHeight(), Color.BLUE);    
		}
      if(cell != null && cell.getMotherCell() != null){
	      
	      double cellWidth = 0;
	      double cellHeight = 0;
	      if(cell.getMotherCell().getEpisimBioMechanicalModelObject() instanceof CenterBased2DModel){
	      	EpisimModelConnector motherCellConnector =((CenterBased2DModel) cell.getMotherCell().getEpisimBioMechanicalModelObject()).getEpisimModelConnector();
	      	if(motherCellConnector instanceof EpisimCenterBasedMC){
	      		cellWidth= ((EpisimCenterBasedMC)motherCellConnector).getWidth();
	      		cellHeight =((EpisimCenterBasedMC)motherCellConnector).getHeight();
	      		setCellWidth(cellWidth); 
	      		setCellHeight(cellHeight);
	      		cellEllipseObject.setMajorAxisAndMinorAxis(((EpisimCenterBasedMC)motherCellConnector).getWidth(), ((EpisimCenterBasedMC)motherCellConnector).getHeight());
	      	}
	      }
	             
	      Double2D motherCellLocation=cellField.getObjectLocation(cell.getMotherCell());
	      
	      
	       if(motherCellLocation != null){		     
		      Double2D newLoc = calculateInitialCellLocation(motherCellLocation, cellWidth);
		      cellLocation = new Double2D(cellField.tx(newLoc.x), cellField.ty(newLoc.y));
		   
		      cellField.setObjectLocation(cell, cellLocation);
		     	SimulationDisplayProperties props = ((CenterBased2DModel)cell.getMotherCell().getEpisimBioMechanicalModelObject()).getCellEllipseObject().getLastSimulationDisplayProps();
		 	  	this.setLastSimulationDisplayPropsForNewCellEllipse(props, cellLocation);
	      }
      }
      if(modelConnector != null){
			setEpisimModelConnector(modelConnector);
		}
      lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0));
      directNeighbours = new GenericBag<AbstractCell>();
      directNeighbourIDs = new HashSet<Long>();
      lostNeighbourContactInSimSteps=new HashMap<Long, Integer>();
   }
   
   private Double2D calculateInitialCellLocation(Double2D motherCellLocation, double cellWidth){
   	
      if(globalParameters.isForceLateralCellDivision()){
      	
      	Point2d locThis = findReferencePositionOnBoundary(new Point2d(motherCellLocation.x, motherCellLocation.y),
      			motherCellLocation.x - (cellWidth/2), motherCellLocation.x + (cellWidth/2));
      	
       	
      	double biasX = (cellWidth/2);
      	double locX = motherCellLocation.x + biasX;
      	Point2d locA = findReferencePositionOnBoundary(new Point2d(locX, motherCellLocation.y),
      			locX - (cellWidth/2), locX + (cellWidth/2));
      	
      	locX = motherCellLocation.x - biasX;
      	Point2d locB = findReferencePositionOnBoundary(new Point2d(locX, motherCellLocation.y),
      			locX - (cellWidth/2), locX + (cellWidth/2));
      	
      	double delta = cellWidth*0.7; 
      	Vector2d vectA = new Vector2d((locA.x - locThis.x), (locA.y - locThis.y));
      	Vector2d vectB = new Vector2d((locB.x - locThis.x), (locB.y - locThis.y));
    
      	if(vectA.length() > 0){
      		vectA.normalize();
      		vectA.scale(delta);
      	}
      	if(vectB.length() > 0){
      		vectB.normalize();
      		vectB.scale(delta);
      	}     
      	
      	if(vectA.length()== 0) return new Double2D(motherCellLocation.x+vectB.x, motherCellLocation.y+vectB.y);
      	if(vectB.length()== 0) return new Double2D(motherCellLocation.x+vectA.x, motherCellLocation.y+vectA.y);
      	
      	else return TissueController.getInstance().getActEpidermalTissue().random.nextBoolean() ?
      			 new Double2D(motherCellLocation.x+vectA.x, motherCellLocation.y+vectA.y) :
      			 new Double2D(motherCellLocation.x+vectB.x, motherCellLocation.y+vectB.y);
      }
      else{
      	double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025;
         double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005;      	
      	return new Double2D(motherCellLocation.x + deltaX, motherCellLocation.y + deltaY);
      }
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
   		Double2D loc = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;
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
       
               
       InteractionResult()
       {           
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
       if (neighbours==null || neighbours.numObjs == 0 || thisloc == null) return interactionResult;
       double majorAxisThis = getCellWidth()/2;
       double minorAxisThis = getCellHeight()/2;
       Point2d thislocP= new Point2d(thisloc.x, thisloc.y);
       double totalContactArea = 0;
       
       for(int i=0;i<neighbours.numObjs;i++)
       {
          if (neighbours.objs[i]==null || !(neighbours.objs[i] instanceof AbstractCell)) continue;          
       
          AbstractCell other = (AbstractCell)(neighbours.objs[i]);
          CenterBased2DModel mechModelOther = (CenterBased2DModel) other.getEpisimBioMechanicalModelObject();
          
          if (other != getCell() && mechModelOther != null)
          {             
             double majorAxisOther = mechModelOther.getCellWidth()/2;
             double minorAxisOther = mechModelOther.getCellHeight()/2;
         	 Double2D otherloc = mechModelOther.cellLocation == null? cellField.getObjectLocation(other) : mechModelOther.cellLocation;
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             
             
             //double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Point2d(otherloc.x, otherloc.y), majorAxisThis, minorAxisThis);
             //double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), new Point2d(thisloc.x, thisloc.y), majorAxisOther, minorAxisOther);
             
             Point2d otherlocP= new Point2d(otherloc.x, otherloc.y);
                          
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(thislocP, 
							otherPosToroidalCorrection(thislocP, otherlocP), 
							majorAxisThis, minorAxisThis);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(otherlocP, 
								otherPosToroidalCorrection(otherlocP,thislocP), 
								majorAxisOther, minorAxisOther);            
             double optDistScaled = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther)*globalParameters.getOptDistanceScalingFactor();
             double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);    
             
             double difference = optDist - optDistScaled;
             if(difference > requiredDistanceToMembraneThis || difference > requiredDistanceToMembraneOther){
            	 double maxOverlap  = difference > requiredDistanceToMembraneThis ? requiredDistanceToMembraneThis : requiredDistanceToMembraneOther;
            	 double correctedOverlapFactor = (1- maxOverlap/(requiredDistanceToMembraneThis+requiredDistanceToMembraneOther));
            	 optDistScaled = optDist*correctedOverlapFactor;            	
             }
                                     
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
             if(this.modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC && finalSimStep){
            	
          		double contactAreaCorrect =0; 
          		if(actDist < optDist*globalParameters.getOptDistanceAdhesionFact()){	
          			contactAreaCorrect= calculateContactAreaNew(new Point2d(thisloc.x, thisloc.y), otherPosToroidalCorrection(new Point2d(thisloc.x, thisloc.y), new Point2d(mechModelOther.getX(), mechModelOther.getY())),
          					dy, majorAxisThis, minorAxisThis, majorAxisOther, minorAxisOther, getCellLength(), mechModelOther.getCellLength(), requiredDistanceToMembraneThis, requiredDistanceToMembraneOther, actDist, optDist);
          			contactAreaCorrect = Double.isNaN(contactAreaCorrect) || Double.isInfinite(contactAreaCorrect) ? 0: contactAreaCorrect;
          			((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC)this.modelConnector).setContactArea(other.getID(), Math.abs(contactAreaCorrect));
             		totalContactArea+=Math.abs(contactAreaCorrect);
          		}
          		
          	 }                     
           }          
        }
	     if(this.modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC && finalSimStep){
	      	 ((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC)this.modelConnector).setTotalContactArea(totalContactArea);
	     }       
       // calculate basal adhesion
       if(modelConnector.getAdhesionBasalMembrane() >=0){
      	 	Point2d membraneReferencePoint = findReferencePositionOnBoundary(thislocP, thisloc.x - (getCellWidth()/2), thisloc.x + (getCellWidth()/2));
      	 	double dx = cellField.tdx(thisloc.x, membraneReferencePoint.x); 
            double dy = cellField.tdy(thisloc.y, membraneReferencePoint.y);
            if(dx==0 && dy==0){
            	addRandomBiasToPoint(membraneReferencePoint,  0.1);
            	dx = cellField.tdx(thisloc.x, membraneReferencePoint.x); 
               dy = cellField.tdy(thisloc.y, membraneReferencePoint.y);
            }
      		double distToMembrane = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2));
      		double optDist = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), new Point2d(membraneReferencePoint.x, membraneReferencePoint.y), getCellWidth()/2, getCellHeight()/2);
      		     		
      		if(distToMembrane < optDist*globalParameters.getOptDistanceAdhesionFact()){
      			double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
               double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
               double radius_this = (adh_Dist_Fact*optDist);
            	double gap = distToMembrane - optDist;
            	
            	double contactArea = Math.PI*radius_this*(radius_this-distToMembrane);
            	if(this.modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC){
            		((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC)this.modelConnector).setBmContactArea(contactArea);
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
      			if(this.modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC){
            		((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC)this.modelConnector).setBmContactArea(0);
            	}
      		}
       }else{
      	 if(this.modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC){
       		((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC)this.modelConnector).setBmContactArea(0);
       	}
       }
       if(isChemotaxisEnabled){
				String chemotacticFieldName = ((episimmcc.centerbased2d.newmodel.chemotaxis.EpisimCenterBasedMC)modelConnector).getChemotacticField();
				if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
					ExtraCellularDiffusionField2D ecDiffField =  (ExtraCellularDiffusionField2D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
					if(ecDiffField != null){
						double lambda = ((episimmcc.centerbased2d.newmodel.chemotaxis.EpisimCenterBasedMC)modelConnector).getLambdaChem();
						if(lambda > 0){
							interactionResult.chemotacticForce = ecDiffField.getChemotaxisVectorForCellBoundary(getChemotaxisCellBoundariesInMikron(), true);
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
   
   private double calculateContactAreaNew(Point2d posThis, Point2d posOther,double dy, double majorAxisThis, double minorAxisThis, double majorAxisOther, double minorAxisOther, double lengthThis, double lengthOther, double d_membrane_this, double d_membrane_other, double actDist, double optDistScaled){
   	double contactArea = 0;
   	double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
      double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
      double smoothingFunction = 1;
      final double AXIS_RATIO_THRES = 5;
      if(majorAxisThis/minorAxisThis >= AXIS_RATIO_THRES || majorAxisOther/minorAxisOther >=AXIS_RATIO_THRES){
				
			Rectangle2D.Double rect1 = new Rectangle2D.Double(posThis.x-majorAxisThis, posThis.y-minorAxisThis, 2*majorAxisThis,2*minorAxisThis);
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

	public void setPositionRespectingBounds(Point2d cellPosition, boolean setPositionInField)
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
	 
	   if(setPositionInField){
	   	setCellLocationInCellField(newloc);
	   	cellLocation = newloc;
	   }
	   else{
	   	newCellLocation = newloc;
	   }
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
		if(globalParameters == null){
  		 if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
  		 			instanceof CenterBased2DModelGP){
  		 		globalParameters = (CenterBased2DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
  		 	}   	
  		 	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
  		 			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
  	 
  	 }
	if(globalParameters instanceof ChemotaxisCenterBased2DModelGP){
  		 this.globalParametersChemotaxis = ((ChemotaxisCenterBased2DModelGP) globalParameters);
  		 this.isChemotaxisEnabled = globalParametersChemotaxis.isChemotaxisEnabled();
  		 this.isContinuousInXDirection = globalParametersChemotaxis.isContinousDiffusionInXDirection();
  		 this.isContinuousInYDirection = globalParametersChemotaxis.isContinousDiffusionInYDirection();
  	 }
   	
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
		if(finalSimStep){
			this.modelConnector.resetPairwiseParameters();
			if(modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC){
	   		((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC) modelConnector).getContactArea().clear();
	   	}
		}
		
		//according to Pathmanathan et al.2008		
		double frictionConstantMedium = 0.0000004;//Galle, Loeffler Drasdo 2005 epithelial cells 0.4 Ns/m (* 10^-6 for conversion to micron )			
		Double2D loc =  cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;		
		if(loc != null){
			
		//	Bag actNeighborBag = cellField.getNeighborsWithinDistance(oldCellLocation, getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), true, true);
			Bag actNeighborBag = cellField.getNeighborsWithinDistance(loc, 
					getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
					getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
					true, true);
			InteractionResult interactionResult = calculateRepulsiveAdhesiveAndChemotacticForces(actNeighborBag, loc, finalSimStep);
				
			if(isImmuneCell() && (this.modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermisimmune.EpisimCenterBasedMC))
			{
				episimmcc.centerbased2d.newmodel.epidermisimmune.EpisimCenterBasedMC mcc = ((episimmcc.centerbased2d.newmodel.epidermisimmune.EpisimCenterBasedMC)this.modelConnector);
				// Old random walk model: randomWalkX scales RNG
				//Double2D randomPositionData = new Double2D(mcc.getRandomWalkX()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
				//		mcc.getRandomWalkX()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
				
				//Double2D randomDownwardsBias = new Double2D(0, mcc.getScaleRWDownBiasImmCell()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 1));
				
				//double newX = loc.x+randomPositionData.x+mcc.getScaleBMInfNeighImmCell()*((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.x+interactionResult.adhesionForce.x+interactionResult.chemotacticForce.x));
				//double newY = loc.y+randomPositionData.y+randomDownwardsBias.y+mcc.getScaleBMInfNeighImmCell()*((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.y+interactionResult.adhesionForce.y+interactionResult.chemotacticForce.y));
				
				// New random walk model: random walk parameters obtained from ABM model
				//Double2D randomPositionData = new Double2D(mcc.getRandomWalkX()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
				//		mcc.getRandomWalkX()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
				
				Double2D randomDownwardsBias = new Double2D(0, mcc.getScaleRWDownBiasImmCell()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 1));
				
				double newX = loc.x+mcc.getRandomWalkX()+mcc.getScaleBMInfNeighImmCell()*((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.x+interactionResult.adhesionForce.x+interactionResult.chemotacticForce.x));
				double newY = loc.y+mcc.getRandomWalkY()+randomDownwardsBias.y+mcc.getScaleBMInfNeighImmCell()*((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.y+interactionResult.adhesionForce.y+interactionResult.chemotacticForce.y));
				
				if(Math.abs(newX-loc.x)> MAX_DISPLACEMENT
						|| Math.abs(newY-loc.y)> MAX_DISPLACEMENT){
					System.out.println("Biomechanical Artefakt ");
				}else{
					setPositionRespectingBounds(new Point2d(newX, newY), false);
				}	
			}
			else{
				if(!isImmuneCell() && (getCell().getStandardDiffLevel()!=StandardDiffLevel.STEMCELL || globalParameters.isMotileStemCells())){		
					
					Double2D randomPositionData = new Double2D(globalParameters.getRandomnessCellMov()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
							globalParameters.getRandomnessCellMov()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));					
					
					double newX = loc.x+randomPositionData.x+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.x+interactionResult.adhesionForce.x+interactionResult.chemotacticForce.x));
					double newY = loc.y+randomPositionData.y+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.y+interactionResult.adhesionForce.y+interactionResult.chemotacticForce.y));
					
					if(Math.abs(newX-loc.x)> MAX_DISPLACEMENT
							|| Math.abs(newY-loc.y)> MAX_DISPLACEMENT){
						System.out.println("Biomechanical Artefakt ");
					}else{
						setPositionRespectingBounds(new Point2d(newX, newY), false);
					}			
				}
			}
			
						
			finalInteractionResult = interactionResult;
		}
	}
	
	public void finishNewSimStep(){
		Double2D newCellLocation = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;
		boolean isPsoriasisModel = (this.modelConnector instanceof episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC);
		if(getCell().getStandardDiffLevel()==StandardDiffLevel.STEMCELL 
				&& !TissueController.getInstance().getTissueBorder().isNoMembraneLoaded()
				&& !globalParameters.isMotileStemCells()){			
			double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellLocation.x, cellLocation.y);
			newCellLocation =new Double2D(cellLocation.x, minY);
			if(Math.abs(cellLocation.y - minY) > 0.1)setCellLocationInCellField(newCellLocation);
		}
		if(isPsoriasisModel && isImmuneCell() && !TissueController.getInstance().getTissueBorder().isNoMembraneLoaded()){
			double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(cellLocation.x, cellLocation.y)-immuneCellYDelta_mikron;
			newCellLocation =new Double2D(cellLocation.x, minY);
			if(Math.abs(cellLocation.y - minY) > 0.1)setCellLocationInCellField(newCellLocation);
		}
		
		Point2d minPositionOnBoundary = findReferencePositionOnBoundary(new Point2d(newCellLocation.x, newCellLocation.y), newCellLocation.x - (getCellWidth()/2), newCellLocation.x + (getCellWidth()/2));
		double distanceToBasalMembrane = Math.sqrt(Math.pow((newCellLocation.x-minPositionOnBoundary.x), 2)+Math.pow((newCellLocation.y-minPositionOnBoundary.y), 2));	
		if(distanceToBasalMembrane <= ((getCellHeight()))){///2)*globalParameters.getOptDistanceAdhesionFact())){
			modelConnector.setIsBasal(true);			
		}
		else{
			modelConnector.setIsBasal(false);			
		}
		boolean basalNeighbouringCellFound = false;
		if(directNeighbours != null){
			for(AbstractCell neighbour : directNeighbours){
				CenterBased2DModel cellBM = ((CenterBased2DModel)neighbour.getEpisimBioMechanicalModelObject());
				if(cellBM.modelConnector.getIsBasal())basalNeighbouringCellFound=true;
			}
		}
		
		
		modelConnector.setHasCollision(hitsOtherCell() > 0);		
		modelConnector.setX(newCellLocation.getX());		
		modelConnector.setY(newCellLocation.getY());
  	 	modelConnector.setEpidermalSurfaceRatio(surfaceAreaRatio);
  	 	modelConnector.setIsSurface(this.isSurfaceCell);
  	 	
  	 	if(modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC){
  	 		episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC mc = (episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC) modelConnector;
  	 		mc.setCellSurfaceArea(getSurfaceArea());
  	 		mc.setCellVolume(getCellVolume());
  	 		mc.setExtCellSpaceVolume(getExtraCellSpaceVolume(mc.getExtCellSpaceMikron()));
  	 		mc.setBasalCellContact(mc.getIsBasal()||basalNeighbouringCellFound);
  	 		Set<Long> keySet = new HashSet<Long>();
	 		keySet.addAll(mc.getCellCellAdhesion().keySet());
	 		HashMap<Long, Double> cellCellAdhesion = mc.getCellCellAdhesion();
	 		for(Long key : keySet){
	 			if(!directNeighbourIDs.contains(key)){
	 				int neighbourLostSteps = 0;
	 				if(!lostNeighbourContactInSimSteps.containsKey(key))lostNeighbourContactInSimSteps.put(key, neighbourLostSteps);
	 				else{
	 					neighbourLostSteps =lostNeighbourContactInSimSteps.get(key) +1;
	 					lostNeighbourContactInSimSteps.put(key, neighbourLostSteps);
	 				}
	 				if(neighbourLostSteps>= globalParameters.getNeighbourLostThres()){
	 					cellCellAdhesion.remove(key);
	 					lostNeighbourContactInSimSteps.remove(key);
	 				}
	 			}
	 			else{
	 				if(lostNeighbourContactInSimSteps.containsKey(key)){
	 				//	int steps = lostNeighbourContactInSimSteps.get(key);
	 				//	if(steps > 10)System.out.println("Didn't see you for: "+steps);
	 					lostNeighbourContactInSimSteps.remove(key);
	 				}
	 			}
	 		}
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
	/* NOT NEEDED*/
 }
   
   @NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){   
   		if(directNeighbours == null || directNeighbours.isEmpty()){
   			updateDirectNeighbours();
   		}
   		return directNeighbours;
   }
   
   private void updateDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood(true);   	
   	directNeighbours.clear();
   	directNeighbourIDs.clear();
   	Double2D thisloc = this.cellLocation == null ? cellField.getObjectLocation(this) :this.cellLocation;
   	boolean psoriasisModelImmuneCellMode = (globalParameters instanceof PsoriasisCenterBased2DModelGP 
   										&& modelConnector instanceof episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC
   										&& ((PsoriasisCenterBased2DModelGP) globalParameters).isAddImmuneCells());
   	if(psoriasisModelImmuneCellMode){
   		updateDirectNeighboursPsoriasisModel(thisloc, neighbours);
   	}
   	else{
	   	for(int i=0;i<neighbours.size();i++)
	      {
	  		 	AbstractCell actNeighbour = neighbours.get(i);
	  		
	       if (actNeighbour != getCell())
	       {
	         CenterBased2DModel mechModelOther = (CenterBased2DModel) actNeighbour.getEpisimBioMechanicalModelObject();
	      	 Double2D otherloc = mechModelOther.cellLocation == null ? cellField.getObjectLocation(actNeighbour) : mechModelOther.cellLocation;      	
	      	 double dx = cellField.tdx(thisloc.x,otherloc.x); 
	      	 double dy = cellField.tdy(thisloc.y,otherloc.y);
	      	 double actDist=Math.sqrt(dx*dx+dy*dy);
	      	 
			    double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), 
			   		 																						otherPosToroidalCorrection(new Point2d(thisloc.x, thisloc.y), new Point2d(otherloc.x, otherloc.y)), 
				      		 																				getCellWidth()/2d, getCellHeight()/2d);
				 double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), 
				  		 																						otherPosToroidalCorrection(new Point2d(otherloc.x, otherloc.y),new Point2d(thisloc.x, thisloc.y)), 
				  		 																						mechModelOther.getCellWidth()/2d, mechModelOther.getCellHeight()/2d);			       
				 double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);	                               
				                     
				 if(actDist <= globalParameters.getDirectNeighbourhoodOptDistFact()*optDist){
				    	 directNeighbours.add(actNeighbour);
				    	 directNeighbourIDs.add(actNeighbour.getID());	      	 
				 }
		  	 }
	     }	      
   	}
   }
   
   private void updateDirectNeighboursPsoriasisModel(Double2D thisloc, GenericBag<AbstractCell> neighbours){
   	for(int i=0;i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
  		
       if (actNeighbour != getCell())
       {
         CenterBased2DModel mechModelOther = (CenterBased2DModel) actNeighbour.getEpisimBioMechanicalModelObject();
      	 Double2D otherloc = mechModelOther.cellLocation == null ? cellField.getObjectLocation(actNeighbour) : mechModelOther.cellLocation;      	
      	 double dx = cellField.tdx(thisloc.x,otherloc.x); 
      	 double dy = cellField.tdy(thisloc.y,otherloc.y);
      	 double actDist=Math.sqrt(dx*dx+dy*dy);
      	 if(isImmuneCell()){
      		 double neighRangeMikron = ((episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC) modelConnector).getImmuneCellContactMikron();
      		 if(actDist <= neighRangeMikron){
		      	 directNeighbours.add(actNeighbour);
		      	 directNeighbourIDs.add(actNeighbour.getID());	      	 
		       }
      	 }
      	 else{
      		 if(mechModelOther.isImmuneCell()){
      			 double neighRangeMikron = ((episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC) modelConnector).getImmuneCellContactMikron();
         		 if(actDist <= neighRangeMikron){
   		      	 directNeighbours.add(actNeighbour);
   		      	 directNeighbourIDs.add(actNeighbour.getID());	      	 
   		       }
      		 }
	      	 else{
			       double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point2d(thisloc.x, thisloc.y), 
			      		 																					otherPosToroidalCorrection(new Point2d(thisloc.x, thisloc.y), new Point2d(otherloc.x, otherloc.y)), 
			      		 																					getCellWidth()/2d, getCellHeight()/2d);
			       double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point2d(otherloc.x, otherloc.y), 
			      		 																						otherPosToroidalCorrection(new Point2d(otherloc.x, otherloc.y),new Point2d(thisloc.x, thisloc.y)), 
			      		 																						mechModelOther.getCellWidth()/2d, mechModelOther.getCellHeight()/2d);			       
			       double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);	                               
			                     
			       if(actDist <= globalParameters.getDirectNeighbourhoodOptDistFact()*optDist){
			      	 directNeighbours.add(actNeighbour);
			      	 directNeighbourIDs.add(actNeighbour.getID());	      	 
			       }
	      	 }
      	 }
       }
      } 	 	
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
   
   protected void newGlobalSimStep(long simStepNumber, SimState state){
 //	long start = System.currentTimeMillis();
   	
   	final MersenneTwisterFast random = state.random;
   	final GenericBag<AbstractCell> allCells = new GenericBag<AbstractCell>(); 
   	allCells.addAll(TissueController.getInstance().getActEpidermalTissue().getAllCells());
   	double numberOfSeconds = DELTA_TIME_IN_SECONDS_PER_EULER_STEP;
   	
   	numberOfSeconds = ((CenterBased2DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).getNumberOfSecondsPerSimStep();
   	
   	double numberOfIterationsDouble = 1; 
   	if(DELTA_TIME_IN_SECONDS_PER_EULER_STEP > numberOfSeconds){
   		DELTA_TIME_IN_SECONDS_PER_EULER_STEP=numberOfSeconds;   		
   	}
   	else if(numberOfSeconds > DELTA_TIME_IN_SECONDS_PER_EULER_STEP){
   		DELTA_TIME_IN_SECONDS_PER_EULER_STEP = numberOfSeconds > DELTA_TIME_IN_SECONDS_PER_EULER_STEP_MAX ? DELTA_TIME_IN_SECONDS_PER_EULER_STEP_MAX : numberOfSeconds;   		  		
   	}
   	numberOfIterationsDouble =(numberOfSeconds/DELTA_TIME_IN_SECONDS_PER_EULER_STEP); //according to Pathmanathan et al.2008 
   	final int numberOfIterations = ((int)numberOfIterationsDouble);
   	boolean parallelizationOn = EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION) == null || EpisimProperties.getProperty(EpisimProperties.SIMULATION_PARALLELIZATION).equalsIgnoreCase(EpisimProperties.ON);
   	for(int i = 0; i<numberOfIterations; i++){
   		allCells.shuffle(random);
   		final int totalCellNumber = allCells.size();
   		final int iterationNo =i;
   		if(parallelizationOn){
	   		 Loop.withIndex(0, totalCellNumber, new Loop.Each() {
	             public void run(int n) {
	            		CenterBased2DModel cellBM = ((CenterBased2DModel)allCells.get(n).getEpisimBioMechanicalModelObject()); 
	         			if(iterationNo == 0) cellBM.initNewSimStep();
	         			cellBM.calculateSimStep((iterationNo == (numberOfIterations-1)));    			
	              }
	         });
   		}
   		else{
   			for(int cellNo = 0; cellNo < totalCellNumber; cellNo++){
	   			CenterBased2DModel cellBM = ((CenterBased2DModel)allCells.get(cellNo).getEpisimBioMechanicalModelObject()); 
	   			if(i == 0) cellBM.initNewSimStep();
	   			cellBM.calculateSimStep((i == (numberOfIterations-1)));	   			
   			}
   		}
   		for(int cellNo = 0; cellNo < totalCellNumber; cellNo++){
   			CenterBased2DModel cellBM = ((CenterBased2DModel)allCells.get(cellNo).getEpisimBioMechanicalModelObject());
   			if(cellBM.newCellLocation != null)cellBM.setCellLocationInCellField(cellBM.newCellLocation);
   			else if(cellBM.cellLocation != null)cellBM.setCellLocationInCellField(cellBM.cellLocation);
   			if(iterationNo == (numberOfIterations-1)){
   				cellBM.updateDirectNeighbours();
   				cellBM.finishNewSimStep();
   			}
   		}
   	}
   	calculateSurfaceCells();
 // 	long end = System.currentTimeMillis();
  // 	System.out.println("Global BM Sim Step: "+(end-start)+" ms");
   }
   
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state){ /* NOT NEEDED IN THIS MODEL */ }
   
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
	         	
		          CenterBased2DModel mechModel = (CenterBased2DModel)cellArray[i].getEpisimBioMechanicalModelObject();
		          mechModel.isSurfaceCell=false;
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
	         	 CenterBased2DModel mechModel = (CenterBased2DModel)xLookUp[k].getEpisimBioMechanicalModelObject();
	         	 
	         	 if(numberOfAssignedBinsMap.containsKey(xLookUp[k].getID())
	         			 &&numberOfAssignedBinsMap.get(xLookUp[k].getID())>0){
	         		 mechModel.surfaceAreaRatio = cellIdToNumberOfBinsMap.get(xLookUp[k].getID())<=0?0d:(double)((double)(numberOfAssignedBinsMap.get(xLookUp[k].getID()))/(double)(cellIdToNumberOfBinsMap.get(xLookUp[k].getID())));
	         		 mechModel.isSurfaceCell= true;
	         	 }
	         	 mechModel.modelConnector.setEpidermalSurfaceRatio(mechModel.surfaceAreaRatio);
	         	 mechModel.modelConnector.setIsSurface(mechModel.isSurfaceCell);
	          }
	      }
      } 
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
	public int getNumberOfOverlappingCells(){return hitsOtherCell();}
	
	private GenericBag<AbstractCell> getCellularNeighbourhood(boolean toroidal) {
		Double2D thisLoc = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;
		Bag neighbours = cellField.getNeighborsWithinDistance(thisLoc, 
				getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
				getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), 
				toroidal, true);
		if(globalParameters instanceof PsoriasisCenterBased2DModelGP && modelConnector instanceof episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC){
			neighbours= getCellularNeighbourhoodPsoriasisModel(thisLoc, toroidal);
		}
		else{
			neighbours = cellField.getNeighborsWithinDistance(thisLoc, 
					getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
					getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), 
					toroidal, true);
		}
		GenericBag<AbstractCell> neighbouringCells = new GenericBag<AbstractCell>();
		if(neighbours != null){
			for(int i = 0; i < neighbours.size(); i++){
				if(neighbours.get(i) instanceof AbstractCell && ((AbstractCell) neighbours.get(i)).getID() != this.getCell().getID()){
					neighbouringCells.add((AbstractCell)neighbours.get(i));
				}
			}
		}		
		return neighbouringCells;
	}
	
	private Bag getCellularNeighbourhoodPsoriasisModel(Double2D thisLoc, boolean toroidal){
		PsoriasisCenterBased2DModelGP gpPsoriasis = (PsoriasisCenterBased2DModelGP) globalParameters;
		episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC mcc = (episimmcc.centerbased2d.newmodel.psoriasis.EpisimCenterBasedMC) modelConnector;
		Bag neighbours=null;
		if(gpPsoriasis.isAddImmuneCells()){
			double searchWidth = mcc.getImmuneCellContactMikron() < getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact()
										? getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact()
										: mcc.getImmuneCellContactMikron();
			double searchHeight = mcc.getImmuneCellContactMikron() < getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact()
										? getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact()
										: mcc.getImmuneCellContactMikron();
			neighbours = cellField.getNeighborsWithinDistance(thisLoc, searchWidth, searchHeight, toroidal, true);
		}
		else{
			neighbours = cellField.getNeighborsWithinDistance(thisLoc, 
					getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
					getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), 
					toroidal, true);
		}
		return neighbours;
	}
	
	@CannotBeMonitored
	@NoExport
	public double getX(){return 
			cellLocation != null ? cellLocation.x :
			modelConnector == null ? 
			0
 		 : modelConnector.getX();
	}
	
	@CannotBeMonitored
	@NoExport
	public double getY(){return 
			cellLocation != null ? cellLocation.y :
			modelConnector == null ? 
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
		boolean isNucleated = false;
		if(modelConnector instanceof episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC){
			isNucleated = ((episimmcc.centerbased2d.newmodel.epidermis.EpisimCenterBasedMC)this.modelConnector).getIsNucleated();
		}
		if(isNucleated){
				return new Episim2DCellShape<Shape>(createHexagonalPolygon(info != null ? info.getDrawInfo(): null, getCellWidth()/3, getCellHeight()/3));
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
	   cellField = new Continuous2DExt(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
				TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
				TissueController.getInstance().getTissueBorder().getHeightInMikron());
   }
   public void removeCellFromCellField() {
	   cellField.remove(this.getCell());
   }
	
   public void setCellLocationInCellField(Double2D location){
	   cellField.setObjectLocation(this.getCell(), location);
	   this.cellLocation = new Double2D(location.x, location.y);
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
   
   public boolean isImmuneCell() {
      
   	return this.modelConnector.getIsImmuneCell();
   }  
	
   public double getImmuneCellYDelta_mikron() {
   
   	return immuneCellYDelta_mikron;
   }

	
   public void setImmuneCellYDelta_mikron(double immuneCellYDelta_mikron) {
   
   	this.immuneCellYDelta_mikron = immuneCellYDelta_mikron;
   }

	
}
