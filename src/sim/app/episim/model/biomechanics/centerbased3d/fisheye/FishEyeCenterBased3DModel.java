package sim.app.episim.model.biomechanics.centerbased3d.fisheye;

import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import sim.app.episim.EpisimProperties;
import sim.app.episim.model.AbstractCell;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Ellipsoid;
import sim.app.episim.model.biomechanics.Episim3DCellShape;
import sim.app.episim.model.biomechanics.centerbased3d.AbstractCenterBased3DModel;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.controller.TissueController;
import sim.app.episim.model.diffusion.ExtraCellularDiffusionField3D;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Loop;
import sim.app.episim.visualization.EpisimDrawInfo;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.field.continuous.Continuous3DExt;
import sim.util.Bag;
import sim.util.Double3D;
import ec.util.MersenneTwisterFast;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import episimmcc.EpisimModelConnector;
import episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC;


public class FishEyeCenterBased3DModel extends AbstractCenterBased3DModel{
		
	public final double NEXT_TO_OUTERCELL_FACT=1.2;
   private double MIN_OVERLAP_MICRON=0.1;   
	   
   private double standardCellWidth=0; 
   private double standardCellHeight=0; 
   private double standardCellLength=0;
   
   private InteractionResult finalInteractionResult;
   
   private EpisimFishEyeCenterBased3DMC modelConnector;
   
   private FishEyeCenterBased3DModelGP globalParameters = null;
   
   private double migrationDistPerSimStep = 0;
   
	private static Continuous3DExt cellField;
	
   private static double FIELD_RESOLUTION_IN_MIKRON=7;   
   private static double DELTA_TIME_IN_SECONDS_PER_EULER_STEP = 36;
   private static final double DELTA_TIME_IN_SECONDS_PER_EULER_STEP_MAX = 36;
   private static final double MAX_DISPLACEMENT = 10;
   
   private Double3D cellLocation=null;
   private Double3D oldCellLocation = null;
   private GenericBag<AbstractCell> directNeighbours;
   private HashSet<Long> directNeighbourIDs;
   private HashMap<Long, Integer> lostNeighbourContactInSimSteps;
   
   private double motherCellInnerEyeRadius = 0;
      
   private static Continuous3DExt dummyCellField;
   private static boolean dummyCellsAdded = false;
   private static double dummyCellSize = 0;   
   
	public FishEyeCenterBased3DModel(){
   	this(null);
   }
   
   public FishEyeCenterBased3DModel(AbstractCell cell){
   	super(cell);   	
   	if(cellField == null){
   		cellField = new Continuous3DExt(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron(),
					TissueController.getInstance().getTissueBorder().getLengthInMikron());   		
   	}
     	if(globalParameters == null){
	  		 if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
	  		 			instanceof FishEyeCenterBased3DModelGP){
	  		 		globalParameters = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	  		 	}   	
	  		 	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
	  		 			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
	 
	 	}
      if(cell != null && cell.getMotherCell() != null){
	      double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025;
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025; 
	      double deltaZ = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025;    
	      
	      if(cell.getMotherCell().getEpisimBioMechanicalModelObject() instanceof FishEyeCenterBased3DModel){
	      	EpisimModelConnector motherCellConnector =((FishEyeCenterBased3DModel) cell.getMotherCell().getEpisimBioMechanicalModelObject()).getEpisimModelConnector();
	      	if(motherCellConnector instanceof EpisimFishEyeCenterBased3DMC){
	      		motherCellInnerEyeRadius= ((EpisimFishEyeCenterBased3DMC) motherCellConnector).getInnerEyeRadius();
	      		setCellWidth(((EpisimFishEyeCenterBased3DMC)motherCellConnector).getWidth()); 
	      		setCellHeight(((EpisimFishEyeCenterBased3DMC)motherCellConnector).getHeight());
	      		setCellLength(((EpisimFishEyeCenterBased3DMC)motherCellConnector).getLength());
	      	}
	      }
	      
	      Double3D oldLoc=cellField.getObjectLocation(cell.getMotherCell());	   
	       if(oldLoc != null){
		      Double3D newloc=new Double3D(oldLoc.x + deltaX, oldLoc.y+deltaY, oldLoc.z+deltaZ);
		      cellLocation = newloc;
		      cellField.setObjectLocation(cell, newloc);		      
	      }
      } 
      directNeighbours = new GenericBag<AbstractCell>();
      directNeighbourIDs = new HashSet<Long>();
      lostNeighbourContactInSimSteps=new HashMap<Long, Integer>();
   }
   public static Continuous3D getDummyCellField(){
   	return dummyCellField;
   }
   public static void setDummyCellSize(double dummyCellSize){
   	if(dummyCellSize >0){
   		dummyCellField	= new Continuous3DExt(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
   				TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
   				TissueController.getInstance().getTissueBorder().getHeightInMikron(),
   				TissueController.getInstance().getTissueBorder().getLengthInMikron());
	   		   	
	   		
	   		dummyCellsAdded = true;	   		
	   		FishEyeCenterBased3DModel.dummyCellSize = dummyCellSize;
   	}
   }
   private void generateDummyCells(double cellSize){
		FishEyeCenterBased3DModelGP mechModelGP = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
		double circumference = 2*mechModelGP.getInnerEyeRadius()*Math.PI;
		double scalingFact = mechModelGP.getDummyCellOptDistanceScalingFactor();
		double numberOfCells = Math.ceil(circumference/(cellSize*scalingFact));
		double angleIncrement = (2*Math.PI)/numberOfCells;
		Point3d fishEyeCenter = mechModelGP.getInnerEyeCenter();
		dummyCellField.clear();
		for(double i = 0; i < (2*Math.PI);i+=angleIncrement){
			double z = fishEyeCenter.z + mechModelGP.getInnerEyeRadius()* Math.cos(i);
			double y = fishEyeCenter.y + mechModelGP.getInnerEyeRadius()* Math.sin(i);
			Double3D newPos = new Double3D((fishEyeCenter.x - (cellSize/2d)), y, z);			
			DummyCell dummyCell= new DummyCell(newPos,cellSize, cellSize, cellSize);
			dummyCellField.setObjectLocation(dummyCell, newPos);
		}
		
	}
   
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimFishEyeCenterBased3DMC){
   		this.modelConnector = (EpisimFishEyeCenterBased3DMC) modelConnector;
   		Double3D loc = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;
   		if(loc != null){
   			this.modelConnector.setX(loc.x);
   			this.modelConnector.setY(loc.y);
   			this.modelConnector.setZ(loc.z);
   		}
   		if(motherCellInnerEyeRadius>0){
   			 this.modelConnector.setInnerEyeRadius(motherCellInnerEyeRadius);
   			 motherCellInnerEyeRadius=-1;
   		}
   		else if(globalParameters != null) this.modelConnector.setInnerEyeRadius(globalParameters.getInnerEyeRadius());
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimCenterBased3DMC");
   } 
   
   public EpisimModelConnector getEpisimModelConnector(){
   	return this.modelConnector;
   }
   
   private class InteractionResult
   {        
       int numhits;    // number of hits
       private Vector3d adhesionForce;
       private Vector3d repulsiveForce;
       private Vector3d chemotacticForce;
                      
       InteractionResult()
       {           
           numhits=0;
           adhesionForce=new Vector3d(0,0,0);
           repulsiveForce=new Vector3d(0,0,0);
           chemotacticForce = new Vector3d(0,0,0);
       }
       
   }
   
   public InteractionResult calculateRepulsiveAdhesiveAndChemotacticForces(Bag neighbours, Double3D thisloc, boolean finalSimStep)
   {
       // check of actual position involves a collision, if so return TRUE, otherwise return FALSE
       // for each collision calc a pressure vector and add it to the other's existing one
       InteractionResult interactionResult=new InteractionResult();            
       if (neighbours==null || neighbours.numObjs == 0 || thisloc == null) return interactionResult;
       
       double thisSemiAxisA = getCellWidth()/2;      
       double thisSemiAxisB = getCellHeight()/2;
       double thisSemiAxisC = getCellLength()/2;
       Point3d thislocP= new Point3d(thisloc.x, thisloc.y, thisloc.z);
       double totalContactArea=0;
            
       for(int i=0;i<neighbours.numObjs;i++)
       {
          
      	 if (neighbours.objs[i]==null || !(neighbours.objs[i] instanceof AbstractCell)) continue;          
       
          AbstractCell other = (AbstractCell)(neighbours.objs[i]);
          FishEyeCenterBased3DModel mechModelOther = (FishEyeCenterBased3DModel) other.getEpisimBioMechanicalModelObject();
          if (other != getCell() && mechModelOther !=null)
          {   	 
             
         	 double otherSemiAxisA = mechModelOther.getCellWidth()/2;             
             double otherSemiAxisB = mechModelOther.getCellHeight()/2;
             double otherSemiAxisC = mechModelOther.getCellLength()/2;
         	 
             Double3D otherloc = mechModelOther.cellLocation == null ? cellField.getObjectLocation(other) : mechModelOther.cellLocation;
             
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             double dz = cellField.tdz(thisloc.z,otherloc.z); 
             
            
             Point3d otherlocP = new Point3d(otherloc.x, otherloc.y, otherloc.z);
                                      
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(thislocP, 
							otherPosToroidalCorrection(thislocP, otherlocP), 
							thisSemiAxisA, thisSemiAxisB, thisSemiAxisC);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(otherlocP, 
								otherPosToroidalCorrection(otherlocP, thislocP), 
								otherSemiAxisA, otherSemiAxisB, otherSemiAxisC);            
             double optDistScaled = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther)*globalParameters.getOptDistanceScalingFactor();
             double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);    
          
                                     
             double actDist=Math.sqrt(dx*dx+dy*dy+dz*dz);
                   
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
            	 interactionResult.repulsiveForce.z += force*dz/actDist;                                       
              
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
            	           	
            	double smoothingFunction = (((-1*adh_Dist_Perc*d_membrane_this) < intercell_gap)
            										 && (intercell_gap < (adh_Dist_Perc*d_membrane_this)))
            										 ? Math.abs(Math.sin((0.5*Math.PI)*(intercell_gap/(adh_Dist_Perc*d_membrane_this))))
            										 : 1;
            	double adhesionCoefficient = globalParameters.getAdhSpringStiffness_N_per_square_micro_m()*getAdhesionFactor(other);
            										 
            	//System.out.println("pre-Adhesion: "+((contactArea*smoothingFunction)/sphereArea));									 
            	double adhesion = adhesionCoefficient*(contactArea*smoothingFunction);
            	
            	interactionResult.adhesionForce.x += adhesion*((-dx)/actDist);
            	interactionResult.adhesionForce.y += adhesion*((-dy)/actDist);
            	interactionResult.adhesionForce.z += adhesion*((-dz)/actDist);
            	
             }
             
             if(this.modelConnector instanceof episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC && finalSimStep){
            	 
          		double contactAreaCorrect = 0;
          		if(actDist < optDist*globalParameters.getOptDistanceAdhesionFact()){
          		//	contactAreaCorrect = calculateContactAreaNew(thislocP,
          		//			otherPosToroidalCorrection(thislocP, otherlocP),
          		//			dy, thisSemiAxisA, thisSemiAxisB, thisSemiAxisB, otherSemiAxisA, otherSemiAxisB,otherSemiAxisC, d_membrane_this, d_membrane_other, actDist, optDist);
          			
          			contactAreaCorrect= calculateContactAreaNew(new Point3d(thisloc.x, thisloc.y, thisloc.z),
          						otherPosToroidalCorrection(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(mechModelOther.getX(), mechModelOther.getY(), mechModelOther.getZ())),
          					dy, thisSemiAxisA, thisSemiAxisB, thisSemiAxisC, otherSemiAxisA, otherSemiAxisB, otherSemiAxisC, requiredDistanceToMembraneThis, requiredDistanceToMembraneOther, actDist, optDist);
          			contactAreaCorrect = Double.isNaN(contactAreaCorrect) || Double.isInfinite(contactAreaCorrect) ? 0: contactAreaCorrect;
          			((episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC)this.modelConnector).setContactArea(other.getID(), Math.abs(contactAreaCorrect));
             		totalContactArea+= Math.abs(contactAreaCorrect); 
          		}          		
          		        		
             }
             
           }          
        }
       	//calculate forces of dummy neighbours at boundary
       if(dummyCellsAdded){
      	 Bag dummyNeighbours = dummyCellField.getNeighborsWithinDistance(thisloc, 
 					getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
 					getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
 					getCellLength()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),					
 					true, true);
      	 for(int i = 0; i < dummyNeighbours.size(); i++){
      		 
      		 DummyCell dummyNeighbour = (DummyCell)dummyNeighbours.get(i);
	      	 double otherSemiAxisA = dummyNeighbour.getCellWidth()/2;             
	          double otherSemiAxisB = dummyNeighbour.getCellHeight()/2;
	          double otherSemiAxisC = dummyNeighbour.getCellLength()/2;
	      	 
	          Double3D otherloc = dummyNeighbour.getCellPosition();
	          
	          double dx = dummyCellField.tdx(thisloc.x,otherloc.x); 
	          double dy = dummyCellField.tdy(thisloc.y,otherloc.y);
	          double dz = dummyCellField.tdz(thisloc.z,otherloc.z); 
	          
	         
	          Point3d otherlocP = new Point3d(otherloc.x, otherloc.y, otherloc.z);
	                                   
	          double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(thislocP, 
							otherPosToroidalCorrection(thislocP, otherlocP), 
							thisSemiAxisA, thisSemiAxisB, thisSemiAxisC);
	          double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(otherlocP, 
								otherPosToroidalCorrection(otherlocP, thislocP), 
								otherSemiAxisA, otherSemiAxisB, otherSemiAxisC);            
	          double optDistScaled = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther)*globalParameters.getOptDistanceScalingFactor();
	          double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);    
	       
	                                  
	          double actDist=Math.sqrt(dx*dx+dy*dy+dz*dz);
	                
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
	         	 interactionResult.repulsiveForce.z += force*dz/actDist;                                                           
	           }
      	 }
       }
       	
       
        modelConnector.setContactAreaInnerEye(0);
        if(this.modelConnector instanceof episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC && finalSimStep){
      	 ((episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC)this.modelConnector).setTotalContactArea(totalContactArea);
      	 
      	  double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(thislocP, 
						otherPosToroidalCorrection(thislocP, globalParameters.getInnerEyeCenter()), 
						thisSemiAxisA, thisSemiAxisB, thisSemiAxisC);
      	 Point3d thisLocPoint = new Point3d(thisloc.x, thisloc.y, thisloc.z);
      	 double dy = cellField.tdy(thisloc.y,globalParameters.getInnerEyeCenter().y);
      	 double contactAreaInnerEyeCorrect= calculateContactAreaNew(thisLocPoint,
						otherPosToroidalCorrection(thisLocPoint, globalParameters.getInnerEyeCenter()),
								dy, thisSemiAxisA, thisSemiAxisB, thisSemiAxisC, globalParameters.getInnerEyeRadius(), globalParameters.getInnerEyeRadius(), 
								globalParameters.getInnerEyeRadius(), requiredDistanceToMembraneThis, globalParameters.getInnerEyeRadius(), thisLocPoint.distance(globalParameters.getInnerEyeCenter()), 
								((requiredDistanceToMembraneThis*globalParameters.getOptDistanceToBMScalingFactor())+globalParameters.getInnerEyeRadius()));
      	 contactAreaInnerEyeCorrect = Double.isNaN(contactAreaInnerEyeCorrect) || Double.isInfinite(contactAreaInnerEyeCorrect) || contactAreaInnerEyeCorrect < 0  ? 0: contactAreaInnerEyeCorrect;
      	 modelConnector.setContactAreaInnerEye(contactAreaInnerEyeCorrect);
      	 
       }
       

     /* if(isChemotaxisEnabled){
				String chemotacticFieldName = ((episimbiomechanics.centerbased3d.newversion.chemotaxis.EpisimChemotaxisCenterBased3DMC)modelConnector).getChemotacticField();
				if(chemotacticFieldName != null && !chemotacticFieldName.trim().isEmpty()){
					ExtraCellularDiffusionField3D ecDiffField =  (ExtraCellularDiffusionField3D)ModelController.getInstance().getExtraCellularDiffusionController().getExtraCellularDiffusionField(chemotacticFieldName);
					if(ecDiffField != null){
						double lambda = ((episimbiomechanics.centerbased3d.newversion.chemotaxis.EpisimChemotaxisCenterBased3DMC)modelConnector).getLambdaChem();
						if(lambda > 0){
							interactionResult.chemotacticForce = ecDiffField.getChemotaxisVectorForCellBoundary(getChemotaxisCellBoundariesInMikron());
							interactionResult.chemotacticForce.scale(lambda);
							interactionResult.chemotacticForce.scale(globalParameters.getRepulSpringStiffness_N_per_micro_m());
						}
					}
				}
		}*/
      return interactionResult;
   }
   
   private Point3d addRandomBiasToPoint(Point3d point, double scalingFact){
   	point.setX(point.getX()+((TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5)*scalingFact));
   	point.setY(point.getY()+((TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5)*scalingFact));
   	point.setZ(point.getZ()+((TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5)*scalingFact));
   	return point;
   }	
   
   private double calculateContactAreaNew(Point3d posThis, Point3d posOther, double dy, double thisSemiAxisA, double thisSemiAxisB, double thisSemiAxisC, double otherSemiAxisA, double otherSemiAxisB, double otherSemiAxisC, double d_membrane_this, double d_membrane_other, double actDist, double optDistScaled){
   	double contactArea = 0;
   	double adh_Dist_Fact = 1;//should be 1 because we want the precise contact area here//globalParameters.getOptDistanceAdhesionFact();
      
      final double AXIS_RATIO_THRES = 5;
      if(thisSemiAxisA/thisSemiAxisB >= AXIS_RATIO_THRES || otherSemiAxisA/otherSemiAxisB >=AXIS_RATIO_THRES){		
			Rectangle2D.Double rect1 = new Rectangle2D.Double(posThis.x-thisSemiAxisA, posThis.y-thisSemiAxisB, 2*thisSemiAxisA,2*thisSemiAxisB);
			Rectangle2D.Double rect2 = new Rectangle2D.Double(posOther.x-otherSemiAxisA, posOther.y-otherSemiAxisB, 2*otherSemiAxisA, 2*otherSemiAxisB);
			Rectangle2D.Double intersectionRectXY = new Rectangle2D.Double();
			Rectangle2D.Double.intersect(rect1, rect2, intersectionRectXY);
			double contactRadiusXY =  intersectionRectXY.height < thisSemiAxisB && intersectionRectXY.height < otherSemiAxisB ? intersectionRectXY.width : intersectionRectXY.height;						
			contactRadiusXY/=2;
			
			rect1 = new Rectangle2D.Double(posThis.z-thisSemiAxisC, posThis.y-thisSemiAxisB, 2*thisSemiAxisC, 2*thisSemiAxisB);
			rect2 = new Rectangle2D.Double(posOther.z-otherSemiAxisC, posOther.y-otherSemiAxisB, 2*otherSemiAxisC, 2*otherSemiAxisB);
			Rectangle2D.Double intersectionRectZY = new Rectangle2D.Double();
			Rectangle2D.Double.intersect(rect1, rect2, intersectionRectZY);
			double contactRadiusZY = intersectionRectZY.width;
			contactRadiusZY/=2;
			
			contactArea = Math.PI*contactRadiusXY*contactRadiusZY;
		}
		else{     
	      double r1 = adh_Dist_Fact*d_membrane_this;
	      double r2 = adh_Dist_Fact*d_membrane_other;
	     
	      double[] semiAxesCellThis = calculateIntersectionEllipseSemiAxes(posThis, posOther, thisSemiAxisA, thisSemiAxisB, thisSemiAxisC);
			double[] semiAxesCellOther = calculateIntersectionEllipseSemiAxes(posOther, posThis, otherSemiAxisA, otherSemiAxisB, otherSemiAxisC);
	      
	      double r1_scaled = (semiAxesCellThis[1]*semiAxesCellThis[0])/r1;
	      double r2_scaled = (semiAxesCellOther[1]*semiAxesCellOther[0])/r2;
	      
	      double actDist_scale = ((r1_scaled)*(1/(r1+r2))+(r2_scaled)*(1/(r1+r2)));
	      double actDist_scaled = actDist*actDist_scale;
	      
	      double actDist_square = Math.pow(actDist_scaled, 2);
	      
	      double radius_this_square = Math.pow(r1_scaled,2);
	   	double radius_other_square = Math.pow(r2_scaled,2);
	   	
	                      	
	   	contactArea = (Math.PI/(4*actDist_square))*(2*actDist_square*(radius_this_square+radius_other_square)
	   																		+2*radius_this_square*radius_other_square
	   																		-Math.pow(radius_this_square, 2)-Math.pow(radius_other_square, 2)
	   																		-Math.pow(actDist_square, 2));
	   /*	double intercell_gap = actDist_scaled - optDistScaled;
	   	smoothingFunction = (((-1*adh_Dist_Perc*d_membrane_this) < intercell_gap)
					 && (intercell_gap < (adh_Dist_Perc*d_membrane_this)))
					 ? Math.abs(Math.sin((0.5*Math.PI)*(intercell_gap/(adh_Dist_Perc*d_membrane_this))))
					 : 1;*/
		
		}
	
      
	
	//double adhesionCoefficient = globalParameters.getAdhSpringStiffness_N_per_square_micro_m()*getAdhesionFactor(other);
										 
					 
	//double adhesion = adhesionCoefficient*(contactArea*smoothingFunction);
				 return contactArea;
   }
     
   private double[] calculateIntersectionEllipseSemiAxes(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
		 //According to P.P. Klein 2012 on the Ellipsoid and Plane Intersection Equation
		 Vector3d directR = new Vector3d((otherCellCenter.x-cellCenter.x), (otherCellCenter.y-cellCenter.y), (otherCellCenter.z-cellCenter.z));
		 directR.normalize();
		 Vector3d directTemp = new Vector3d(0, 1, 0);
		 directTemp.normalize();
		 if(directTemp.equals(directR)){
			 directTemp = new Vector3d(1, 0, 0);
			 directTemp.normalize();
		 }
		 
		 Vector3d normalVect =new Vector3d();
		 normalVect.cross(directR, directTemp);
		 normalVect.normalize();
		 
		 Vector3d directS = new Vector3d();		 
		 directS.cross(normalVect, directR);
		 directS.normalize();
		 Matrix3d diagMatrixD = new Matrix3d(1d/aAxis,0,0,0,1d/bAxis,0,0,0,1d/cAxis);
		 double dr_dr = mult(diagMatrixD, directR).dot(mult(diagMatrixD, directR));
		 double ds_ds = mult(diagMatrixD, directS).dot(mult(diagMatrixD, directS));
		 double dr_ds = mult(diagMatrixD, directR).dot(mult(diagMatrixD, directS));
		 
		 double diff_drdr_dsds = dr_dr-ds_ds;
		 
		 double angle =diff_drdr_dsds != 0 ? (0.5*Math.atan((2*dr_ds)/diff_drdr_dsds)) : (Math.PI/4d);
		 
		 double sinAngle = Math.sin(angle);
		 double cosAngle = Math.cos(angle);
		 
		 Vector3d rVect = new Vector3d((cosAngle*directR.x+sinAngle*directS.x),(cosAngle*directR.y+sinAngle*directS.y), (cosAngle*directR.z+sinAngle*directS.z));
		 Vector3d sVect = new Vector3d((cosAngle*directS.x-sinAngle*directR.x),(cosAngle*directS.y-sinAngle*directR.y), (cosAngle*directS.z-sinAngle*directR.z));
		 
		 Vector3d qVect = new Vector3d(0,0,0);//cellCenter.x, cellCenter.y, cellCenter.z);
		 
		 dr_dr = mult(diagMatrixD, rVect).dot(mult(diagMatrixD, rVect));
		 ds_ds = mult(diagMatrixD, sVect).dot(mult(diagMatrixD, sVect));
		 double dq_dq = mult(diagMatrixD, qVect).dot(mult(diagMatrixD, qVect));
		 double dq_dr = mult(diagMatrixD, qVect).dot(mult(diagMatrixD, rVect));
		 double dq_ds = mult(diagMatrixD, qVect).dot(mult(diagMatrixD, sVect));		 
		 double dFact = dq_dq - (Math.pow(dq_dr, 2)/dr_dr) -(Math.pow(dq_ds, 2)/ds_ds);
		 return new double[]{Math.sqrt((1-dFact)/dr_dr),Math.sqrt((1-dFact)/ds_ds)};
	}
   private Vector3d mult(Matrix3d m, Vector3d v ){
		return new Vector3d((m.m00*v.x + m.m01*v.y+ m.m02*v.z), (m.m10*v.x + m.m11*v.y+ m.m12*v.z), (m.m20*v.x + m.m21*v.y+ m.m22*v.z));
	}
   
   
   

   private double getAdhesionFactor(AbstractCell otherCell){   	
   	return modelConnector.getAdhesionFactorForCell(otherCell);
   } 
   
   private double calculateDistanceToCellCenter(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
   	Point3d intersectionPointEllipsoid = calculateIntersectionPointOnEllipsoid(cellCenter, otherCellCenter, aAxis, bAxis, cAxis);	   
	   return cellCenter.distance(intersectionPointEllipsoid);
	}
   
   private Point3d calculateIntersectionPointOnEllipsoid(Point3d cellCenter, Point3d otherCellCenter, double aAxis, double bAxis, double cAxis){
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
	   	 return null;
	    }
	   double sqrtA = Math.sqrt(a);	 
	   double hit = 1 / sqrtA;
	   double hitsecond = -1*(1 / sqrtA);
	    
	   double linefactor = hit;// < hitsecond ? hit : hitsecond;
	   return new Point3d((cellCenter.x+ linefactor*rayDirection.x),(cellCenter.y+ linefactor*rayDirection.y),(cellCenter.z+ linefactor*rayDirection.z));
   }
   
   public void setPositionRespectingBounds(Point3d cellPosition, double aAxis, double bAxis, double cAxis, boolean setPostionInCellField)
	{
   	setPositionRespectingBounds(cellPosition, aAxis, bAxis, cAxis, globalParameters.getOptDistanceToBMScalingFactor(), setPostionInCellField);
	}
   
   public void setPositionRespectingBounds(Point3d cellPosition, double aAxis, double bAxis, double cAxis, double optDistScalingFact, boolean setPostionInCellField)
	{
	   Point3d newloc = calculateLowerBoundaryPositionForCell(cellPosition, aAxis, bAxis, cAxis, optDistScalingFact);
	   oldCellLocation = cellLocation;
	   cellLocation = new Double3D(newloc.x, newloc.y, newloc.z);
	   if(setPostionInCellField)setCellLocationInCellField(cellLocation);
	}   
   
   
   public void setDummyCellsRespectingBounds(){
   	Bag dummyCells = dummyCellField.getAllObjects();
   	for(int i = 0; i < dummyCells.size(); i++){
   		DummyCell dummyCell = (DummyCell) dummyCells.get(i);
   		Double3D actPos = dummyCell.getCellPosition();
   		Point3d newloc = calculateLowerBoundaryPositionForCell(new Point3d(actPos.x, actPos.y, actPos.z), 
   				dummyCell.getCellWidth()/2, dummyCell.getCellHeight()/2, dummyCell.getCellLength()/2, globalParameters.getOptDistanceToBMScalingFactor());   		
   		dummyCell.setCellPosition(new Double3D(newloc.x, newloc.y, newloc.z));
   		dummyCellField.setObjectLocation(dummyCell, dummyCell.getCellPosition());
   	}   	
   }
   
   public Point3d calculateLowerBoundaryPositionForCell(Point3d cellCenter,  double aAxis, double bAxis, double cAxis, double optDistScalingFact){
   	Point3d innerEyeCenter = globalParameters.getInnerEyeCenter();
   	
   	Vector3d rayDirection = new Vector3d((cellCenter.x-innerEyeCenter.x), (cellCenter.y-innerEyeCenter.y), (cellCenter.z-innerEyeCenter.z));
		rayDirection.normalize();
   	double cellMembraneToCenterDistance = calculateDistanceToCellCenter(cellCenter, innerEyeCenter, aAxis, bAxis, cAxis);
   	cellMembraneToCenterDistance*=optDistScalingFact;
   	rayDirection.scale((globalParameters.getInnerEyeRadius()+ cellMembraneToCenterDistance));
   	double newX = innerEyeCenter.x + rayDirection.x;
   	if(!dummyCellsAdded) newX = newX < globalParameters.getInnerEyeCenter().x ? globalParameters.getInnerEyeCenter().x : newX;
   	return new Point3d(newX, innerEyeCenter.y + rayDirection.y, innerEyeCenter.z + rayDirection.z);	
	}
   
   
   private Point3d findReferencePositionOnBoundary(Point3d cellPosition){
   	return calculateIntersectionPointOnEllipsoid(globalParameters.getInnerEyeCenter(), cellPosition, globalParameters.getInnerEyeRadius(), globalParameters.getInnerEyeRadius(), globalParameters.getInnerEyeRadius());
	}   
  	
	public void initNewSimStep(){
		if(globalParameters == null){
	  		 if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
	  		 			instanceof FishEyeCenterBased3DModelGP){
	  		 		globalParameters = (FishEyeCenterBased3DModelGP) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
	  		 	}   	
	  		 	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
	  		 			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
 	 
 	 	}
		migrationDistPerSimStep=0;
	}
	
	public void calculateSimStep(boolean finalSimStep){
		if(finalSimStep){
			this.modelConnector.resetPairwiseParameters();
			if(modelConnector instanceof episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC){				
		  		 ((episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC) modelConnector).getContactArea().clear();
		  	}
		}
		//according to Pathmanathan et al.2008
		Double3D loc = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;
		
		double frictionConstantMedium = 0.0000004;//Galle, Loeffler Drasdo 2005 epithelial cells 0.4 Ns/m (* 10^-6 for conversion to micron )			
				
		if(loc != null){
			
			//Bag neighbours = cellField.getNeighborsWithinDistance(oldCellLocation, getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(), true, true);
			Bag neighbours = cellField.getNeighborsWithinDistance(loc, 
					getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
					getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
					getCellLength()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),					
					true, true);
			InteractionResult interactionResult = calculateRepulsiveAdhesiveAndChemotacticForces(neighbours, loc, finalSimStep);
						
			if(getCell().getStandardDiffLevel()!=StandardDiffLevel.STEMCELL){		
				
				Double3D randomPositionData = new Double3D(globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5),
						globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5),
						globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));				
				
				
				double newX = loc.x+randomPositionData.x+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.x+interactionResult.adhesionForce.x+interactionResult.chemotacticForce.x));
				double newY = loc.y+randomPositionData.y+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.y+interactionResult.adhesionForce.y+interactionResult.chemotacticForce.y));
				double newZ = loc.z+randomPositionData.z+((DELTA_TIME_IN_SECONDS_PER_EULER_STEP/frictionConstantMedium)*(interactionResult.repulsiveForce.z+interactionResult.adhesionForce.z+interactionResult.chemotacticForce.z));
				
				if(Math.abs(newX-loc.x)> MAX_DISPLACEMENT
						|| Math.abs(newY-loc.y)> MAX_DISPLACEMENT
						|| Math.abs(newZ-loc.z)> MAX_DISPLACEMENT){
					System.out.println("Biomechanical Artefakt ");
				}else{
					setPositionRespectingBounds(new Point3d(newX, newY, newZ),getCellWidth()/2d, getCellHeight()/2d, getCellLength()/2d, false);
				}				
			}			
			finalInteractionResult = interactionResult;
		}
		
	}
	
	public void finishNewSimStep(){
		Double3D newCellLocation = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;		
		modelConnector.setX(newCellLocation.getX());		
		modelConnector.setY(newCellLocation.getY());
		modelConnector.setZ(newCellLocation.getZ());
  	 	modelConnector.setInnerEyeRadius(globalParameters.getInnerEyeRadius());
  	 	
  	 	
  	 	if(modelConnector instanceof episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC){
  	 		episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC mc = (episimmcc.centerbased3d.fisheye.EpisimFishEyeCenterBased3DMC) modelConnector;
  	 		mc.setCellSurfaceArea(getSurfaceArea());
  	 		mc.setCellVolume(getCellVolume());
  	 		mc.setExtCellSpaceVolume(getExtraCellSpaceVolume(mc.getExtCellSpaceMikron()));
  	 		
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
   
   
   
   
   public void newSimStep(long simstepNumber){/* NOT NEEDED HERE*/ }
   
   @NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){
   	   	
   	return directNeighbours;
   }
   
   
   private void updateDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood(true);
   	directNeighbours.clear();
   	directNeighbourIDs.clear();
   	Double3D thisloc = this.cellLocation == null ? cellField.getObjectLocation(this) :this.cellLocation;
   	Point3d thisLocP = new Point3d(thisloc.x, thisloc.y, thisloc.z);

   	for(int i=0;neighbours != null && i<neighbours.size();i++)
      {
  		 	 AbstractCell actNeighbour = neighbours.get(i);	  		 	
	  		 if (actNeighbour != getCell())
	       {
	  			 FishEyeCenterBased3DModel mechModelOther = (FishEyeCenterBased3DModel) actNeighbour.getEpisimBioMechanicalModelObject();
	      	 Double3D otherloc = mechModelOther.cellLocation == null ? cellField.getObjectLocation(actNeighbour) : mechModelOther.cellLocation;
	      	 Point3d otherlocP = new Point3d(otherloc.x, otherloc.y, otherloc.z);
	      	 double dx = cellField.tdx(thisLocP.x,otherloc.x); 
	          double dy = cellField.tdy(thisLocP.y,otherloc.y);
	          double dz = cellField.tdz(thisLocP.z,otherloc.z);
	           
	          double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(thisLocP, 
							otherPosToroidalCorrection(thisLocP, otherlocP), 
							getCellWidth()/2, getCellHeight()/2, getCellLength()/2);
	          double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(otherlocP, 
							otherPosToroidalCorrection(otherlocP,thisLocP), 
							mechModelOther.getCellWidth()/2, mechModelOther.getCellHeight()/2, mechModelOther.getCellLength()/2);
	          
	          double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther); 
	          double actDist=Math.sqrt(dx*dx+dy*dy+dz*dz);        
		       if(actDist <= globalParameters.getDirectNeighbourhoodOptDistFact()*optDist){
		      	 directNeighbours.add(actNeighbour);
		      	 directNeighbourIDs.add(actNeighbour.getID());	      	 
		       }
		     //  System.out.println("Neighbourhood radius: " + (2.5*optDist));
	      	 
	       }
      }
   }
   
   public Point3d otherPosToroidalCorrection(Point3d thisCell, Point3d otherCell)
   {
	   double height = cellField.height;
	   double width = cellField.width;
	   double length = cellField.length;
	   double otherZ=-1;
	   double otherY=-1;
	   double otherX=-1;
	   if (Math.abs(thisCell.z-otherCell.z) <= length / 2) otherZ = otherCell.z;
	   else{
	   	otherZ = thisCell.z > otherCell.z ? (otherCell.z+length): (otherCell.z-length);
	   }
	   if (Math.abs(thisCell.y-otherCell.y) <= height / 2) otherY = otherCell.y;
	   else{
	   	otherY = thisCell.y > otherCell.y ? (otherCell.y+height): (otherCell.y-height);
	   }
	   if (Math.abs(thisCell.x-otherCell.x) <= width / 2) otherX = otherCell.x;
	   else{
	   	otherX = thisCell.x > otherCell.x ? (otherCell.x+width): (otherCell.x-width);
	   }
	   return new Point3d(otherX, otherY, otherZ);
   } 
   
   private void setInnerEyeRadius(AbstractCell cell){
   	((FishEyeCenterBased3DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).setInnerEyeRadius(
   			((FishEyeCenterBased3DModel)cell.getEpisimBioMechanicalModelObject()).modelConnector.getInnerEyeRadius());
   }
   
   public void initialisationGlobalSimStep(){
   	newGlobalSimStep(Long.MIN_VALUE, null);
   }
   
   
   protected void newGlobalSimStep(long simStepNumber, SimState state){
   //	long start = System.currentTimeMillis();   	
   	final MersenneTwisterFast random = state!= null ? state.random : new MersenneTwisterFast(System.currentTimeMillis());
   	final GenericBag<AbstractCell> allCells = new GenericBag<AbstractCell>(); 
   	allCells.addAll(TissueController.getInstance().getActEpidermalTissue().getAllCells());
   	setInnerEyeRadius(allCells.get(random.nextInt(allCells.size())));
   	
   	if(dummyCellsAdded){
   		generateDummyCells(dummyCellSize);
   		setDummyCellsRespectingBounds();
   	}
   	
   	double numberOfSeconds = DELTA_TIME_IN_SECONDS_PER_EULER_STEP;   	
   	numberOfSeconds = ((FishEyeCenterBased3DModelGP)ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters()).getNumberOfSecondsPerSimStep();
   	
   	double numberOfIterationsDouble = 1; 
   	if(DELTA_TIME_IN_SECONDS_PER_EULER_STEP > numberOfSeconds){
   		DELTA_TIME_IN_SECONDS_PER_EULER_STEP = numberOfSeconds;   		
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
		            		FishEyeCenterBased3DModel cellBM = ((FishEyeCenterBased3DModel)allCells.get(n).getEpisimBioMechanicalModelObject()); 
				   			if(iterationNo == 0) cellBM.initNewSimStep();
				   			cellBM.calculateSimStep((iterationNo == (numberOfIterations-1)));				   			
		             }
		         });  		
	   		}
	   		else{
	   			for(int cellNo = 0; cellNo < totalCellNumber; cellNo++){
		   			FishEyeCenterBased3DModel cellBM = ((FishEyeCenterBased3DModel)allCells.get(cellNo).getEpisimBioMechanicalModelObject()); 
		   			if(i == 0) cellBM.initNewSimStep();
		   			cellBM.calculateSimStep((i == (numberOfIterations-1)));		   			
		   		}
	   		}
	   		for(int cellNo = 0; cellNo < totalCellNumber; cellNo++){
	   			FishEyeCenterBased3DModel cellBM = ((FishEyeCenterBased3DModel)allCells.get(cellNo).getEpisimBioMechanicalModelObject());
	   			if(cellBM.cellLocation!=null){
	   				if(cellBM.oldCellLocation != null){
	   					cellBM.migrationDistPerSimStep += cellBM.oldCellLocation.distance(cellBM.cellLocation);
	   				}
	   				cellBM.setCellLocationInCellField(cellBM.cellLocation);	   				
	   			}
	   			if(iterationNo == (numberOfIterations-1)){
	   				cellBM.updateDirectNeighbours();
	   				cellBM.finishNewSimStep();
	   			}
	   		}
   	}   	  
   	
   //	long end = System.currentTimeMillis();
  // 	System.out.println("Global BM Sim Step: "+(end-start)+" ms");
   }
   
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state){
   	/* NOT NEEDED IN THIS MODEL */   	
   } 
   
   public boolean hasNucleus(){ 
   	
   	return true;
   }
   
   public boolean hasViablility(){ 
   	
   	return true;
   }
   
   public double getCellHeight() {	return modelConnector == null ? 0 : modelConnector.getHeight(); }	
	public double getCellWidth() {return modelConnector == null ? 0 : modelConnector.getWidth(); }	
	public double getCellLength() {return modelConnector == null ? 0 : modelConnector.getLength(); }
	
	public void setCellHeight(double cellHeight) { if(modelConnector!=null) modelConnector.setHeight(cellHeight>0?cellHeight:getCellHeight()); }	
	public void setCellWidth(double cellWidth) { if(modelConnector!=null) modelConnector.setWidth(cellWidth>0?cellWidth:getCellWidth()); }	
	public void setCellLength(double cellLength) { if(modelConnector!=null) modelConnector.setLength(cellLength>0?cellLength:getCellLength()); }
	
	public int hitsOtherCell(){ return finalInteractionResult.numhits; }
	
	
	
	private GenericBag<AbstractCell> getCellularNeighbourhood(boolean toroidal) {
		Double3D loc = cellLocation == null? cellField.getObjectLocation(getCell()):cellLocation;
		Bag neighbours = cellField.getNeighborsWithinDistance(loc, 
				getCellWidth()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
				getCellHeight()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
				getCellLength()*globalParameters.getMechanicalNeighbourhoodOptDistFact(),
				toroidal, true);
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
	public double getZ(){ return 
			cellLocation != null ? cellLocation.z :
			modelConnector == null ? 
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
	   cellField = new Continuous3DExt(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
				TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
				TissueController.getInstance().getTissueBorder().getHeightInMikron(),
				TissueController.getInstance().getTissueBorder().getLengthInMikron());
	}
	
	public void removeCellFromCellField() {
	   cellField.remove(this.getCell());
	}
	
	public void setCellLocationInCellField(Double3D location){
	   cellField.setObjectLocation(this.getCell(), location);
	   
	   this.cellLocation = new Double3D(location.x, location.y, location.z);
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
	
	
//------------------------------------------------------------------------------------------------------------------------------
// Methods of the old model
//------------------------------------------------------------------------------------------------------------------------------
	
	
	   
	   
	
	
 
   @CannotBeMonitored
   @NoExport  
   public CellBoundaries getChemotaxisCellBoundariesInMikron() {
 /*	  Double3D fieldLocMikron = getCellLocationInCellField();
 	  Vector3d minVector= null;
 	  Vector3d maxVector= null;
 	  double width = getCellWidth();
 	  double height = getCellHeight();
 	  double length = getCellLength();
 	  
 	  double deltaFact = globalParametersChemotaxis.getChemotaxisCellSizeDeltaFact();
 	  width *=deltaFact;
	  height *= deltaFact;
	  length *= deltaFact;
 	  
 	  minVector = new Vector3d((fieldLocMikron.x-width/2d),
				   						(fieldLocMikron.y-height/2d),
				   						(fieldLocMikron.z-length/2d));

 	  maxVector = new Vector3d((fieldLocMikron.x+width/2d),
										  (fieldLocMikron.y+height/2d),
										  (fieldLocMikron.z+length/2d));
 	   	 
 	 Transform3D trans = new Transform3D();
 	 trans.setTranslation(new Vector3d(fieldLocMikron.x, fieldLocMikron.y, fieldLocMikron.z));
 	 trans.setScale(new Vector3d(width/height, height / height, length/height));
 	 return new CellBoundaries(new Ellipsoid(trans, height/2d), minVector, maxVector);*/
   	return null;
  }	
	
	
   @CannotBeMonitored
   @NoExport  
   public CellBoundaries getCellBoundariesInMikron(double sizeDelta) {
 	  Double3D fieldLocMikron = getCellLocationInCellField();
 	  Vector3d minVector= null;
 	  Vector3d maxVector= null;
 	  double width = getCellWidth()+sizeDelta;
 	  double height = getCellHeight()+sizeDelta;
 	  double length = getCellLength()+sizeDelta;
 	  
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
   
   @CannotBeMonitored
   @NoExport  
   public CellBoundaries getNucleusBoundariesInMikron(double sizeDelta) {
   	if(hasNucleus()){
	 	  Double3D fieldLocMikron = getCellLocationInCellField();
	 	  Vector3d minVector= null;
	 	  Vector3d maxVector= null;
	 	  double width = (getCellWidth()+sizeDelta)/3d;
	 	  double height = (getCellHeight()+sizeDelta)/3d;
	 	  double length = (getCellLength()+sizeDelta)/3d;
	 	  
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
   	return null;
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

	
   public double getMigrationDistPerSimStep() {
   
   	return migrationDistPerSimStep;
   }




}
