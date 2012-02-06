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
import javax.vecmath.Vector3d;

import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased3d.EpisimCenterBased3DMC;
import episimexceptions.GlobalParameterException;
import episiminterfaces.EpisimCellShape;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;
import episiminterfaces.monitoring.CannotBeMonitored;
import sim.SimStateServer;
import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;
import sim.app.episim.gui.EpisimGUIState;
import sim.app.episim.gui.EpisimGUIState.SimulationDisplayProperties;
import sim.app.episim.model.biomechanics.AbstractMechanical3DModel;
import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.biomechanics.CellBoundaries;
import sim.app.episim.model.biomechanics.Episim3DCellShape;
import sim.app.episim.model.biomechanics.hexagonbased3d.Ellipsoid;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.model.visualization.EpisimDrawInfo;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.field.continuous.Continuous3D;
import sim.util.Bag;
import sim.util.Double3D;
import sun.security.action.GetLongAction;


public class CenterBased3DMechanicalModel extends AbstractMechanical3DModel{
	

	public static final double GOPTIMALKERATINODISTANCE=4; // Default: 4
   public static final double GOPTIMALKERATINODISTANCEGRANU=4; // Default: 3
   
   //The width of the keratinocyte must be bigger or equals the hight
   public static final int GINITIALKERATINOHEIGHT=5; // Default: 5
   public static final int GINITIALKERATINOLENGTH=5; // Default: 5
   public static final int GINITIALKERATINOWIDTH=5; // Default: 5
   
   public static final int GKERATINOWIDTHGRANU=9; // default: 10
   public static final int GKERATINOHEIGHTGRANU=4;
   public static final int GKERATINOLENGTHGRANU=4;
   
   public final int NEXTTOOUTERCELL=7;
   private double MINDIST=0.1;   
   private static final double CONSISTENCY=0.0;
   
  
   
   private int keratinoWidth=-11; 
   private int keratinoLength=-1; 
   private int keratinoHeight=-1; 
   
   private Vector3d extForce = new Vector3d(0,0,0);
   private Double3D lastd = new Double3D(0,0,0);
   
   
   private Double3D oldLoc;
   private Double3D newLoc;
   
   private HitResultClass finalHitResult;
   
   //maybe more neighbours than real neighbours included inside a circle
   private GenericBag<AbstractCell> neighbouringCells;
   
   private EpisimCenterBased3DMC modelConnector;
   
   private boolean isMembraneCell = false;
   
  
   //TODO: plus 2 Korrektur überprüfen
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
   	
   	extForce=new Vector3d(0,0,0);      
      keratinoWidth=GINITIALKERATINOWIDTH; //theEpidermis.InitialKeratinoSize;
      keratinoHeight=GINITIALKERATINOHEIGHT; //theEpidermis.InitialKeratinoSize;
      lastd=new Double3D(0.0,-3, 0.0);
     
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
   
  
   public Double3D momentum(){
       return lastd;
   }  
   
   
   public Double3D randomness(MersenneTwisterFast r)
   {
       double x = r.nextDouble() * 2 - 1.0;
       double y = r.nextDouble() * 2 - 1.0;
       double z = r.nextDouble() * 2 - 1.0;
       double l = Math.sqrt((x*x) + (y*y) +(z*z));
       return new Double3D(0.05*x/l,0.05*y/l, 0.05*z/l);
   }
   
 
   private class HitResultClass
   {        
       int numhits;    // number of hits
       long otherId; // when only one hit, then how id of this hit (usually this will be the mother)
       long otherMotherId; // mother of other
       Vector3d adhForce;
       Vector3d otherMomentum;
       boolean nextToOuterCell;
               
       HitResultClass()
       {
           nextToOuterCell=false;
           numhits=0;
           otherId=0;
           otherMotherId=0;
           adhForce=new Vector3d(0,0,0);
           otherMomentum=new Vector3d(0,0,0);
       }
   }
       
   public HitResultClass hitsOther(Bag b, Continuous3D pC2dHerd, Double3D thisloc, boolean pressothers, double pBarrierMemberDist)
   {
       // check of actual position involves a collision, if so return TRUE, otherwise return FALSE
       // for each collision calc a pressure vector and add it to the other's existing one
       HitResultClass hitResult=new HitResultClass();            
       if (b==null || b.numObjs == 0) return hitResult;       
              
       int i=0;
       double adxOpt = GOPTIMALKERATINODISTANCE; //KeratinoWidth-2+theEpidermis.cellSpace;                         was 4 originally then 5
      
       
       
       if (getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal()==EpisimDifferentiationLevel.GRANUCELL) adxOpt=GOPTIMALKERATINODISTANCEGRANU; // was 3 // 4 in modified version
       
       //TODO: diese Stelle überprüfen
       double optDistSq = adxOpt*adxOpt; //+adyOpt*adyOpt;
       double optDist=Math.sqrt(optDistSq);
       //double outerCircleSq = (neigh_p*adxOpt)*(neigh_p*adxOpt)+(neigh_p*adyOpt)*(neigh_p*adyOpt);
       int neighbors=0;
      

       for(i=0;i<b.numObjs;i++)
       {
               if (!(b.objs[i] instanceof UniversalCell))
                   continue;
               
               if(!(((UniversalCell) b.objs[i]).getEpisimBioMechanicalModelObject() instanceof CenterBased3DMechanicalModel)) continue;
       
           UniversalCell other = (UniversalCell)(b.objs[i]);
           if (other != getCell())
           {
              Double3D otherloc=pC2dHerd.getObjectLocation(other);                  
              double dx = pC2dHerd.tdx(thisloc.x,otherloc.x); 
              double dy = pC2dHerd.tdy(thisloc.y,otherloc.y);
              double dz = pC2dHerd.tdy(thisloc.z,otherloc.z);              
                  
              double actdistsq = (dx*dx)+(dy*dy)+(dz*dz);                        
              double actdist=Math.sqrt(actdistsq);             
                   
              if (optDist-actdist>MINDIST) // ist die kollision signifikant ?
              {
                 double fx=(actdist>0)?(optDist+0.1)/actdist*dx-dx:0;    // nur die differenz zum jetzigen abstand draufaddieren
                 double fy=(actdist>0)?(optDist+0.1)/actdist*dy-dy:0;
                 double fz=(actdist>0)?(optDist+0.1)/actdist*dz-dz:0;
                                       
                                      
                 hitResult.numhits++;
                 hitResult.otherId=other.getID();
                 hitResult.otherMotherId=other.getMotherId();
                                              
                 if (pressothers){
                    ((CenterBased3DMechanicalModel) other.getEpisimBioMechanicalModelObject()).extForce.add(new Vector3d(-fx,-fy,-fz)); //von mir wegzeigende kraefte addieren
                 }
                 extForce.add(new Vector3d(fx,fy, fz));
              }

              if (actdistsq <= pBarrierMemberDist * pBarrierMemberDist)
              {
                 
                     Double3D m = ((CenterBased3DMechanicalModel)((UniversalCell)b.objs[i]).getEpisimBioMechanicalModelObject()).momentum();
                     hitResult.otherMomentum.x+=m.x;
                     hitResult.otherMomentum.y+=m.y;
                     hitResult.otherMomentum.z+=m.z;
                     neighbors++;              
                     // lipids do not diffuse
                     if ((dy<0) && (other.getIsOuterCell())) hitResult.nextToOuterCell=true; // if the one above is an outer cell, I belong to the barrier 
               }
           }
       }    

       //hitResult.envSigCalcium=theEpidermis.staticCalciumGradient(thisloc.y);  // noch auf collecten aus umgebund umbauen

       if (neighbors>0)    // average the signals to per cell
       {
           hitResult.otherMomentum.scale(1/neighbors); 
       }
       return hitResult;
   }    

	public void setPositionRespectingBounds(Double3D p_potentialLoc)
	{
	   // modelling a hole in the wall at position hole holeX with width  holeHalfWidth
	  
	   double newx=p_potentialLoc.x;
	   double newy=p_potentialLoc.y;  
	   double newz=p_potentialLoc.z;
	   double minY=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(p_potentialLoc.x, p_potentialLoc.y, p_potentialLoc.z);  
	   
	  
	   if (newy<minY)
	   {
	   	newy=minY; 
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
	           
	   if (newy<minY)  // border crossed
	   {
	       if (newy<=0) // unterste Auffangebene
	       {
	           newy=0;       
	       }	
	       else            
	           newy=minY;       
	   }  
	   return new Double3D(newx, newy, newz);        
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
		if(!(getCell().getEpisimBioMechanicalModelObject() instanceof CenterBased3DMechanicalModel)) return;
		
		if(getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.GRANUCELL){
   	 	setKeratinoWidth(getGKeratinoWidthGranu());
   		setKeratinoHeight(getGKeratinoHeightGranu());
   		setKeratinoLength(getGKeratinoLengthGranu());   		
   	}
		// calc potential location from gravitation and external pressures
		oldLoc = cellField.getObjectLocation(getCell());
		if(oldLoc != null){
			//TODO: Test this condition
		if(extForce.length() > 0.6) 
			extForce = setVector3dLength(extForce, 0.6);
		
		Double3D randi = new Double3D(
				globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), 
				globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5),
				globalParameters.getRandomness()* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
		Vector3d actionForce = new Vector3d(extForce.x * globalParameters.getExternalPush() + randi.x, 
														extForce.y * globalParameters.getExternalPush(),
														extForce.z * globalParameters.getExternalPush() + randi.z);
		Double3D potentialLoc = null;
		
		potentialLoc = new Double3D(cellField.stx(actionForce.x + oldLoc.x), cellField.sty(actionForce.y + oldLoc.y), cellField.stz(actionForce.z + oldLoc.z));
		
		extForce.x = 0; // alles einberechnet
		extForce.y = 0;
		extForce.z = 0;

		//////////////////////////////////////////////////
		// try ACTION force
		//////////////////////////////////////////////////
		Bag b = cellField.getObjectsWithinDistance(potentialLoc, globalParameters.getNeighborhood_mikron(), false); // theEpidermis.neighborhood
		HitResultClass hitResult1;
		hitResult1 = hitsOther(b, cellField, potentialLoc, true, NEXTTOOUTERCELL);

		//////////////////////////////////////////////////
		// estimate optimised POS from REACTION force
		//////////////////////////////////////////////////
		// optimise my own position by giving way to the calculated pressures
		Vector3d reactionForce = extForce;
		hitResult1.otherMomentum.scale(CONSISTENCY);
		reactionForce.add(hitResult1.otherMomentum);
		hitResult1.adhForce.scale(globalParameters.getCohesion());
		reactionForce.add(hitResult1.adhForce);

		// restrict movement if direction changes to quickly (momentum of a
		// cell
		// movement)

		// bound optimised force
		// problem: each cell bounces back two times as much as it should (to
		// be
		// able to move around immobile objects)
		// but as we don't want bouncing the reaction force must never exceed
		// the
		// action force in its length
		// dx/dy contain move to make
		if(reactionForce.length() > (actionForce.length() + 0.1))
			reactionForce = setVector3dLength(reactionForce,(actionForce.length() + 0.1));

		extForce.x = 0;
		extForce.y = 0;

		// bound also by borders
		double potX = oldLoc.x + actionForce.x + reactionForce.x;
		double potY = oldLoc.y + actionForce.y + reactionForce.y;
		double potZ = oldLoc.z + actionForce.z + reactionForce.z;
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

		b = cellField.getObjectsWithinDistance(potentialLoc, globalParameters.getNeighborhood_mikron(), false); // theEpidermis.neighborhood
		HitResultClass hitResult2;
		hitResult2 = hitsOther(b, cellField, potentialLoc, true, NEXTTOOUTERCELL);

		// move only on pressure when not stem cell
		if(getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL){
			if((hitResult2.numhits == 0)
					|| ((hitResult2.numhits == 1) && ((hitResult2.otherId == getCell().getMotherId()) || (hitResult2.otherMotherId == getCell().getID())))){
				
				lastd = new Double3D(potentialLoc.x - oldLoc.x, potentialLoc.y - oldLoc.y,potentialLoc.z - oldLoc.z);
				setPositionRespectingBounds(potentialLoc);
			}
		}

		newLoc = cellField.getObjectLocation(getCell());
		double minY = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newLoc.x, newLoc.y, newLoc.z);
		if((newLoc.y-minY) < globalParameters.getBasalLayerWidth())
			getCell().setIsBasalStatisticsCell(true);
		else
			getCell().setIsBasalStatisticsCell(false); // ABSOLUTE DISTANZ KONSTANTE

		if((newLoc.y-minY) < globalParameters.getMembraneCellsWidthInMikron()){
			modelConnector.setIsMembrane(true);
			//cell.setIsMembraneCell(true);
			this.isMembraneCell = true;
		}
		else{
			modelConnector.setIsMembrane(false);
			//cell.setIsMembraneCell(false); // ABSOLUTE DISTANZ KONSTANTE
			isMembraneCell = false;
		}
		
		
		neighbouringCells = new GenericBag<AbstractCell>();
		for(int i = 0; i < b.size(); i++){
			if(b.get(i) instanceof AbstractCell && ((AbstractCell) b.get(i)).getID() != this.getCell().getID()){
				neighbouringCells.add((AbstractCell)b.get(i));
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
   	for(int i=0;i<neighbours.size();i++)
      {
  		 	AbstractCell actNeighbour = neighbours.get(i);
         neighbourCells.add(actNeighbour);    
      }
  	 	return neighbourCells;
   }
   
   
   
   
   
   public int getGKeratinoHeightGranu() {	return GKERATINOHEIGHTGRANU;}
   public int getGKeratinoWidthGranu() { return GKERATINOWIDTHGRANU;	}
   public int getGKeratinoLengthGranu() { return GKERATINOLENGTHGRANU;	}
   
   public int getKeratinoHeight() {	return keratinoHeight; }
	
	public int getKeratinoWidth() {return keratinoWidth;}
	public int getKeratinoLength() {return keratinoLength;}
	
	public void setKeratinoHeight(int keratinoHeight) { this.keratinoHeight = keratinoHeight;	}
	
	public void setKeratinoWidth(int keratinoWidth) { this.keratinoWidth = keratinoWidth; }
	
	public void setKeratinoLength(int keratinoLength) { this.keratinoLength = keratinoLength; }

	public Double3D getNewPosition(){ return newLoc; }
	public void setNewPosition(Double3D loc){ newLoc=loc; }

	public Double3D getOldPosition(){ return oldLoc; }
	public void setOldPosition(Double3D loc){ oldLoc=loc; }

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
   	//not needed	   
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
 	 trans.setScale(new Vector3d(width/height, height/height, length/height));
 	 return new CellBoundaries(new Ellipsoid(trans, (height+sizeDelta)), minVector, maxVector);
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
