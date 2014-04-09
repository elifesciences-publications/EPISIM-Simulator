package sim.app.episim.model.biomechanics.centerbased3d.newversion;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased.newversion.EpisimCenterBasedMC;
import episimbiomechanics.centerbased3d.newversion.EpisimCenterBased3DMC;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.app.episim.AbstractCell;
import sim.app.episim.model.biomechanics.AbstractMechanical3DModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Ellipsoid;
import sim.app.episim.model.biomechanics.Episim3DCellShape;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;
import sim.util.Double3D;


public class CenterBased3DMechanicalModel extends AbstractMechanical3DModel{
	
	public final double NEXT_TO_OUTERCELL_FACT=1.2;
   private double MIN_OVERLAP_MICRON=0.1;   
	
   private boolean isChemotaxisEnabled = false;
   private boolean isContinuousInXDirection = true;
   private boolean isContinuousInYDirection = false;
   private boolean isContinuousInZDirection = true;
   
   private InteractionResult finalInteractionResult;
   
   private EpisimCenterBased3DMC modelConnector;
   
   private CenterBased3DMechanicalModelGP globalParameters = null;
   private double surfaceAreaRatio =0;
   
	private static Continuous3D cellField;
	
   private static double FIELD_RESOLUTION_IN_MIKRON=7;   
   private static double DELTA_TIME_IN_SECONDS_PER_EULER_STEP = 36;   
   private static final double MAX_DISPLACEMENT = 10;
   
	public CenterBased3DMechanicalModel(){
   	this(null);
   }
   
   public CenterBased3DMechanicalModel(AbstractCell cell){
   	super(cell);   	
   	if(cellField == null){
   		cellField = new Continuous3D(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
					TissueController.getInstance().getTissueBorder().getWidthInMikron(), 
					TissueController.getInstance().getTissueBorder().getHeightInMikron(),
					TissueController.getInstance().getTissueBorder().getLengthInMikron());
   		
   	}   	
   	
      if(cell != null && cell.getMotherCell() != null){
	      double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025;
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005; 
	      double deltaZ = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.005-0.0025;    
	      
	      if(cell.getMotherCell().getEpisimBioMechanicalModelObject() instanceof CenterBased3DMechanicalModel){
	      	EpisimModelConnector motherCellConnector =((CenterBased3DMechanicalModel) cell.getMotherCell().getEpisimBioMechanicalModelObject()).getEpisimModelConnector();
	      	if(motherCellConnector instanceof EpisimCenterBased3DMC){
	      		setCellWidth(((EpisimCenterBased3DMC)motherCellConnector).getWidth()); 
	      		setCellHeight(((EpisimCenterBased3DMC)motherCellConnector).getHeight());
	      		setCellLength(((EpisimCenterBased3DMC)motherCellConnector).getLength());
	      	}
	      }
	      
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
   		Double3D loc = cellField.getObjectLocation(getCell());
   		if(loc != null){
   			this.modelConnector.setX(loc.x);
   			this.modelConnector.setY(loc.y);
   			this.modelConnector.setZ(loc.z);
   		}
   		
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
       boolean nextToOuterCell;
               
       InteractionResult()
       {
           nextToOuterCell=false;
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
       if (neighbours==null || neighbours.numObjs == 0) return interactionResult;
       
       double thisSemiAxisA = getCellWidth()/2;      
       double thisSemiAxisB = getCellHeight()/2;
       double thisSemiAxisC = getCellLength()/2;
      
       for(int i=0;i<neighbours.numObjs;i++)
       {
          
      	 if (!(neighbours.objs[i] instanceof AbstractCell)) continue;          
       
          AbstractCell other = (AbstractCell)(neighbours.objs[i]);
          
          if (other != getCell())
          {
             
         	 CenterBased3DMechanicalModel mechModelOther = (CenterBased3DMechanicalModel) other.getEpisimBioMechanicalModelObject();
             
         	 double otherSemiAxisA = mechModelOther.getCellWidth()/2;             
             double otherSemiAxisB = mechModelOther.getCellHeight()/2;
             double otherSemiAxisC = mechModelOther.getCellLength()/2;
         	 
             Double3D otherloc=cellField.getObjectLocation(other);
             
             double dx = cellField.tdx(thisloc.x,otherloc.x); 
             double dy = cellField.tdy(thisloc.y,otherloc.y);
             double dz = cellField.tdz(thisloc.z,otherloc.z);             
                                      
             double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point3d(thisloc.x, thisloc.y, thisloc.z), 
							otherPosToroidalCorrection(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(otherloc.x, otherloc.y, otherloc.z)), 
							thisSemiAxisA, thisSemiAxisB, thisSemiAxisC);
             double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point3d(otherloc.x, otherloc.y, otherloc.z), 
								otherPosToroidalCorrection(new Point3d(otherloc.x, otherloc.y, otherloc.z),new Point3d(thisloc.x, thisloc.y, thisloc.z)), 
								otherSemiAxisA, otherSemiAxisB, otherSemiAxisC);            
             double optDistScaled = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther)*globalParameters.getOptDistanceScalingFactor();
             //double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther);    
          
                                     
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
            else if(((optDistScaled-actDist)<=-1*MIN_OVERLAP_MICRON) &&(actDist < optDistScaled*globalParameters.getOptDistanceAdhesionFact())) // attraction forces 
             {
            	//contact area approximated according to Dallon and Othmer 2004
            	//calculated for ellipsoids not ellipses
               double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
               double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
               double d_membrane_this=requiredDistanceToMembraneThis*globalParameters.getOptDistanceScalingFactor();
               double d_membrane_other=requiredDistanceToMembraneOther*globalParameters.getOptDistanceScalingFactor();
            	double radius_this_square = Math.pow((adh_Dist_Fact*d_membrane_this),2);
            	double radius_other_square = Math.pow((adh_Dist_Fact*d_membrane_other),2);
            	double actDist_square = Math.pow(actDist, 2);
            	double intercell_gap = actDist - optDistScaled;
                               	
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
             if(this.modelConnector instanceof episimbiomechanics.centerbased3d.newversion.epidermis.EpisimEpidermisCenterBased3DMC && finalSimStep){
            	 double d_membrane_this=requiredDistanceToMembraneThis*globalParameters.getOptDistanceScalingFactor();
                double d_membrane_other=requiredDistanceToMembraneOther*globalParameters.getOptDistanceScalingFactor();
          		double contactAreaCorrect = 0;
          		if(actDist < optDistScaled*globalParameters.getOptDistanceAdhesionFact()){
          			contactAreaCorrect = calculateContactAreaNew(new Point3d(thisloc.x, thisloc.y, thisloc.z),
          					otherPosToroidalCorrection(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(mechModelOther.getX(), mechModelOther.getY(), mechModelOther.getZ())),
          					dy, thisSemiAxisA, thisSemiAxisB, thisSemiAxisB, otherSemiAxisA, otherSemiAxisB,otherSemiAxisC, d_membrane_this, d_membrane_other, actDist, optDistScaled);
          		}
          		((episimbiomechanics.centerbased3d.newversion.epidermis.EpisimEpidermisCenterBased3DMC)this.modelConnector).setContactArea(other.getID(), Math.abs(contactAreaCorrect));
          	 }
             if (actDist <= (getCellHeight()*NEXT_TO_OUTERCELL_FACT) && dy < 0 && other.getIsOuterCell()){                    	
                    interactionResult.nextToOuterCell=true;  
             }
           }          
        }       
       // calculate basal adhesion
       if(modelConnector.getAdhesionBasalMembrane() >0){
      		Point3d membraneReferencePoint = findReferencePositionOnBoundary(new Point3d(thisloc.x, thisloc.y, thisloc.z), thisloc.x - (getCellWidth()/2), thisloc.x + (getCellWidth()/2), thisloc.z - (getCellLength()/2), thisloc.z + (getCellLength()/2));
      		double dx = cellField.tdx(thisloc.x,membraneReferencePoint.x); 
            double dy = cellField.tdy(thisloc.y,membraneReferencePoint.y);
            double dz = cellField.tdy(thisloc.z,membraneReferencePoint.z);
      		double distToMembrane = Math.sqrt(Math.pow(dx,2)+Math.pow(dy,2)+Math.pow(dz,2));
      		double optDist = calculateDistanceToCellCenter(new Point3d(thisloc.x, thisloc.y, thisloc.z), new Point3d(membraneReferencePoint.x, membraneReferencePoint.y, membraneReferencePoint.z), getCellWidth()/2, getCellHeight()/2,  getCellLength()/2);
      		optDist*=globalParameters.getOptDistanceToBMScalingFactor();
      		
      		if(distToMembrane < optDist*globalParameters.getOptDistanceAdhesionFact()){
      			double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
               double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
               double radius_this = (adh_Dist_Fact*optDist);
            	double gap = distToMembrane - optDist;
            	
            	double contactArea = Math.PI*radius_this*(radius_this-distToMembrane);
            	
            	double smoothingFunction = (((-1*adh_Dist_Perc*optDist) < gap)
							 && (gap < (adh_Dist_Perc*optDist)))
							 ? Math.abs(Math.sin((0.5*Math.PI)*(gap/(adh_Dist_Perc*optDist))))
							 : 1;
							 
					double adhesionCoefficient = globalParameters.getAdhSpringStiffness_N_per_square_micro_m()*modelConnector.getAdhesionBasalMembrane();
							 
			      double adhesion = adhesionCoefficient*(contactArea*smoothingFunction);
			      
			      interactionResult.adhesionForce.x += adhesion * ((-dx)/distToMembrane);
			      interactionResult.adhesionForce.y += adhesion * ((-dy)/distToMembrane);
			      interactionResult.adhesionForce.z += adhesion * ((-dz)/distToMembrane);
      		}    		
       }
       /*if(isChemotaxisEnabled){
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
			}*/
      return interactionResult;
   }
   
   private double calculateContactAreaNew(Point3d posThis, Point3d posOther, double dy, double thisSemiAxisA, double thisSemiAxisB, double thisSemiAxisC, double otherSemiAxisA, double otherSemiAxisB, double otherSemiAxisC, double d_membrane_this, double d_membrane_other, double actDist, double optDistScaled){
   	double contactArea = 0;
   	double adh_Dist_Fact = globalParameters.getOptDistanceAdhesionFact();
      double adh_Dist_Perc = globalParameters.getOptDistanceAdhesionFact()-1;
      double smoothingFunction = 1;
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
   
   private Point3d findReferencePositionOnBoundary(Point3d cellPosition, double minX, double maxX, double minZ, double maxZ){
		double minDist = Double.POSITIVE_INFINITY;
		Point3d actMinPoint=null;
		if(!TissueController.getInstance().getTissueBorder().isNoMembraneLoaded()){
			for(double x = minX; x <= maxX; x+=0.5){
				for(double z = minZ; z <= maxZ; z+=0.5){
					double actY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(x, cellPosition.y);
					Point3d actPos = new Point3d(x, actY, z);
					double actDist = actPos.distance(cellPosition);
					if(actDist < minDist){
						minDist = actDist;
						actMinPoint = actPos;
					}
				}
			}
			return actMinPoint;
		}
		return new Point3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
   
   
   
   
   
   //TODO
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state){}
   
   public void newSimStep(long simstepNumber){}
   
   public Point3d calculateLowerBoundaryPositionForCell(Point3d cellPosition){
   	return null;
   }
   
   
   
   
   
   
   private double getAdhesionFactor(AbstractCell otherCell){   	
   	return modelConnector.getAdhesionFactorForCell(otherCell);
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
   
   public double getCellHeight() {	return modelConnector == null ? 0 : modelConnector.getHeight(); }	
	public double getCellWidth() {return modelConnector == null ? 0 : modelConnector.getWidth(); }	
	public double getCellLength() {return modelConnector == null ? 0 : modelConnector.getLength(); }
	
	public void setCellHeight(double cellHeight) { if(modelConnector!=null) modelConnector.setHeight(cellHeight>0?cellHeight:getCellHeight()); }	
	public void setCellWidth(double cellWidth) { if(modelConnector!=null) modelConnector.setWidth(cellWidth>0?cellWidth:getCellWidth()); }	
	public void setCellLength(double cellLength) { if(modelConnector!=null) modelConnector.setLength(cellLength>0?cellLength:getCellLength()); }
	
	public int hitsOtherCell(){ return finalInteractionResult.numhits; }
	
	public boolean nextToOuterCell(){ return finalInteractionResult != null ?finalInteractionResult.nextToOuterCell:false; }

	
	@NoExport
   public GenericBag<AbstractCell> getDirectNeighbours(){
   	GenericBag<AbstractCell> neighbours = getCellularNeighbourhood(true);
   	GenericBag<AbstractCell> neighbourCells = new GenericBag<AbstractCell>();
   	for(int i=0;neighbours != null && i<neighbours.size();i++)
      {
  		 	  AbstractCell actNeighbour = neighbours.get(i);  		 	
  		 	
	  		 if (actNeighbour != getCell())
	       {
	  			 CenterBased3DMechanicalModel mechModelOther = (CenterBased3DMechanicalModel) actNeighbour.getEpisimBioMechanicalModelObject();
	      	 Double3D otherloc = mechModelOther.getCellLocationInCellField();
	      	 double dx = cellField.tdx(getX(),otherloc.x); 
	          double dy = cellField.tdy(getY(),otherloc.y);
	          double dz = cellField.tdz(getZ(),otherloc.z);
	           
	          double requiredDistanceToMembraneThis = calculateDistanceToCellCenter(new Point3d(getX(), getY(), getZ()), 
							otherPosToroidalCorrection(new Point3d(getX(), getY(), getZ()), new Point3d(otherloc.x, otherloc.y, otherloc.z)), 
							getCellWidth()/2, getCellHeight()/2, getCellLength()/2);
	          double requiredDistanceToMembraneOther = calculateDistanceToCellCenter(new Point3d(otherloc.x, otherloc.y, otherloc.z), 
							otherPosToroidalCorrection(new Point3d(otherloc.x, otherloc.y, otherloc.z),new Point3d(getX(), getY(), getZ())), 
							mechModelOther.getCellWidth()/2, mechModelOther.getCellHeight()/2, mechModelOther.getCellLength()/2);
	          
	          double optDist = (requiredDistanceToMembraneThis+requiredDistanceToMembraneOther)*globalParameters.getOptDistanceScalingFactor(); 
	          double actDist=Math.sqrt(dx*dx+dy*dy+dz*dz);        
		       if(actDist <= globalParameters.getDirectNeighbourhoodOptDistFact()*optDist){
		      	 neighbourCells.add(actNeighbour);
		       }
		     //  System.out.println("Neighbourhood radius: " + (2.5*optDist));
	      	 
	       }
      }
  	 	return neighbourCells;
   }
	
	
	
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
	public double getZ(){ return modelConnector == null ? 
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
	   cellField = new Continuous3D(FIELD_RESOLUTION_IN_MIKRON / 1.5, 
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
	
	
//------------------------------------------------------------------------------------------------------------------------------
// Methods of the old model
//------------------------------------------------------------------------------------------------------------------------------
	
	
	   
	   
	public static GenericBag<AbstractCell> getAllCellsWithinDistance(Double3D location, double distance){
   	Bag cells =cellField.getNeighborsWithinDistance(location, distance, true);
   	GenericBag<AbstractCell> abstractCells = new GenericBag<AbstractCell>();
   	for(int i = 0; i< cells.size(); i++){
   		if(cells.get(i) != null &&  cells.get(i) instanceof AbstractCell) abstractCells.add((AbstractCell) cells.get(i));
   	}   	
   	return abstractCells;
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