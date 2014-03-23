package sim.app.episim.model.biomechanics.centerbased3d.newversion;

import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
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
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.centerbased.newversion.CenterBasedMechanicalModelGP;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.GenericBag;
import sim.engine.SimState;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;


public class CenterBased3DMechanicalModel extends AbstractMechanical3DModel{
	
	public final double NEXT_TO_OUTERCELL_FACT=1.2;
   private double MIN_OVERLAP_MICRON=0.1;   
	
   private double cellWidth=-1; // breite keratino
   private double cellHeight=-1; // höhe keratino
   private double cellLength=-1; // length keratino
   
   private boolean isChemotaxisEnabled = false;
   private boolean isContinuousInXDirection = true;
   private boolean isContinuousInYDirection = false;
   private boolean isContinuousInZDirection = true;
   
   private InteractionResult finalInteractionResult;
   
   private EpisimCenterBased3DMC modelConnector;
   
   private CenterBased3DMechanicalModelGP globalParameters = null;
   
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
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimCenterBasedModelConnector");
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
   
   
   
   //TODO
   protected void newSimStepGloballyFinished(long simStepNumber, SimState state){}
   
   public void newSimStep(long simstepNumber){}
   
   public Point3d calculateLowerBoundaryPositionForCell(Point3d cellPosition){
   	return null;
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
   
   public double getCellHeight() {	return cellHeight; }	
	public double getCellWidth() {return cellWidth;}
	public double getCellLength() {return cellLength;}
	
	public void setCellHeight(double cellHeight) { this.cellHeight = cellHeight>0?cellHeight:this.cellHeight;}	
	public void setCellWidth(double cellWidth) { this.cellWidth = cellWidth>0?cellWidth:this.cellWidth; }
	public void setCellLength(double cellLength) { this.cellLength = cellLength>0?cellLength:this.cellLength; }
	
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