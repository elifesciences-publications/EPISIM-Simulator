package sim.app.episim.model.biomechanics.centerbased;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector;
import episimbiomechanics.centerbased.EpisimCenterBasedModelConnector;
import episimexceptions.GlobalParameterException;

import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.NoExport;

import episiminterfaces.monitoring.CannotBeMonitored;

import sim.app.episim.AbstractCell;
import sim.app.episim.UniversalCell;

import sim.app.episim.model.biomechanics.AbstractMechanicalModel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.initialization.BiomechanicalModelInitializer;
import sim.app.episim.model.initialization.CenterBasedMechanicalModelInitializer;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.visualization.CellEllipse;

import sim.app.episim.tissue.TissueController;

import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.Vector2D;

import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;


public class CenterBasedMechanicalModel extends AbstractMechanicalModel {
	
	public static final double GOPTIMALKERATINODISTANCE=4; // Default: 4
   public static final double GOPTIMALKERATINODISTANCEGRANU=4; // Default: 3
   //The width of the keratinocyte must be bigger or equals the hight
   public static final int GINITIALKERATINOHEIGHT=5; // Default: 5
   public static final int GINITIALKERATINOWIDTH=5; // Default: 5
   
   public final int NEXTTOOUTERCELL=7;
   private double MINDIST=0.1;   
   private static final double CONSISTENCY=0.0;
   
   private int gKeratinoWidthGranu=9; // default: 10
   private int gKeratinoHeightGranu=4;
   
   private int keratinoWidth=-11; // breite keratino
   private int keratinoHeight=-1; // höhe keratino
   
   private Vector2D extForce = new Vector2D(0,0);
   private Double2D lastd = new Double2D(0,0);
   
   
   private Double2D oldLoc;
   private Double2D newLoc;
   
   private HitResultClass finalHitResult;
   
   //maybe more neighbours than real neighbours included inside a circle
   private GenericBag<AbstractCell> neighbouringCells;
   
   private EpisimCenterBasedModelConnector modelConnector;
   
   private boolean isMembraneCell = false;
   
   private CellEllipse cellEllipseObject;
   
   private DrawInfo2D lastDrawInfo2D;
   
   //TODO: plus 2 Korrektur überprüfen
   private static Continuous2D cellField;
  
   public CenterBasedMechanicalModel(){
   	this(null);
   }
   
   public CenterBasedMechanicalModel(AbstractCell cell){
   	super(cell);
   	
   	if(cellField == null){
   		cellField = new Continuous2D(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getNeighborhood_mikron() / 1.5, 
					ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getWidthInMikron() + 2, 
					ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getHeightInMikron());
   	}
   	
   	
   	
   	extForce=new Vector2D(0,0);      
      keratinoWidth=GINITIALKERATINOWIDTH; //theEpidermis.InitialKeratinoSize;
      keratinoHeight=GINITIALKERATINOHEIGHT; //theEpidermis.InitialKeratinoSize;
      lastd=new Double2D(0.0,-3);
      if(cell != null && getCellEllipseObject() == null && cell.getEpisimCellBehavioralModelObject() != null){
			cellEllipseObject = new CellEllipse(cell.getID(), (int)getX(), (int)getY(), keratinoWidth, keratinoHeight, Color.BLUE);    
		}
      if(cell != null && cell.getMotherCell() != null){
	      double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;
	      double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.1; 
	             
	      Double2D oldLoc=cellField.getObjectLocation(cell.getMotherCell());	   
	       if(oldLoc != null){
		      Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y-deltaY);		      
		      cellField.setObjectLocation(cell, newloc);		      
		 	  	DrawInfo2D info = ((CenterBasedMechanicalModel)cell.getMotherCell().getEpisimBioMechanicalModelObject()).getCellEllipseObject().getLastDrawInfo2D();
		 	  	this.setLastDrawInfo2DForNewCellEllipse(info, newloc, oldLoc);
	      }
      }
      lastDrawInfo2D = new DrawInfo2D(null, null, new Rectangle2D.Double(0, 0, 0, 0), new Rectangle2D.Double(0, 0, 0, 0));
   }
   
   public void setLastDrawInfo2D(DrawInfo2D info){
   	this.lastDrawInfo2D = info;
   }
   
   public void setEpisimModelConnector(EpisimModelConnector modelConnector){
   	if(modelConnector instanceof EpisimCenterBasedModelConnector){
   		this.modelConnector = (EpisimCenterBasedModelConnector) modelConnector;
   	}
   	else throw new IllegalArgumentException("Episim Model Connector must be of type: EpisimCenterBasedModelConnector");
   } 
   
   public EpisimModelConnector getEpisimModelConnector(){
   	return this.modelConnector;
   }
   
  
   public Double2D momentum(){
       return lastd;
   }  
   
   public final Double2D forceFromBound(Continuous2D pC2dHerd, double x) // Calculate the Force orthogonal to lower bound
   {        
       double yleft=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(pC2dHerd.stx(x-5));
       double yright=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(pC2dHerd.stx(x+5));
       return new Double2D(-(yright-yleft),10);
   }    
   public Double2D randomness(MersenneTwisterFast r)
   {
       double x = r.nextDouble() * 2 - 1.0;
       double y = r.nextDouble() * 2 - 1.0;
       double l = Math.sqrt(x * x + y * y);
       return new Double2D(0.05*x/l,0.05*y/l);
   }
   public final double elastic(double x)
   {        
       if ((x>0.3) || (x<-0.3)) return x;
       else return x*x*x;
   }
 
   private class HitResultClass
   {        
       int numhits;    // number of hits
       long otherId; // when only one hit, then how id of this hit (usually this will be the mother)
       long otherMotherId; // mother of other
       Vector2D adhForce;
       Vector2D otherMomentum;
       boolean nextToOuterCell;
               
       HitResultClass()
       {
           nextToOuterCell=false;
           numhits=0;
           otherId=0;
           otherMotherId=0;
           adhForce=new Vector2D(0,0);
           otherMomentum=new Vector2D(0,0);
       }
   }
       
   public HitResultClass hitsOther(Bag b, Continuous2D pC2dHerd, Double2D thisloc, boolean pressothers, double pBarrierMemberDist)
   {
       // check of actual position involves a collision, if so return TRUE, otherwise return FALSE
       // for each collision calc a pressure vector and add it to the other's existing one
       HitResultClass hitResult=new HitResultClass();            
       if (b==null || b.numObjs == 0 || getCell().getIsInNirvana()) return hitResult;
       
       
       
              
       int i=0;
       double adxOpt = GOPTIMALKERATINODISTANCE; //KeratinoWidth-2+theEpidermis.cellSpace;                         was 4 originally then 5
       //double adxOpt = KeratinoWidth; //KeratinoWidth-2+theEpidermis.cellSpace;                        
       //double adyOpt = 5; // 3+theEpidermis.cellSpace;
       
       
       if (getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal()==EpisimDifferentiationLevel.GRANUCELL) adxOpt=GOPTIMALKERATINODISTANCEGRANU; // was 3 // 4 in modified version
       
       double optDistSq = adxOpt*adxOpt; //+adyOpt*adyOpt;
       double optDist=Math.sqrt(optDistSq);
       //double outerCircleSq = (neigh_p*adxOpt)*(neigh_p*adxOpt)+(neigh_p*adyOpt)*(neigh_p*adyOpt);
       int neighbors=0;
      

       for(i=0;i<b.numObjs;i++)
       {
               if (!(b.objs[i] instanceof UniversalCell))
                   continue;
               
               if(!(((UniversalCell) b.objs[i]).getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel)) continue;
       
           UniversalCell other = (UniversalCell)(b.objs[i]);
           if (other != getCell())
               {
                   Double2D otherloc=pC2dHerd.getObjectLocation(other);
                   double dx = pC2dHerd.tdx(thisloc.x,otherloc.x); // dx, dy is what we add to other to get to this
                   double dy = pC2dHerd.tdy(thisloc.y,otherloc.y);
                                       //dx=Math.rint(dx*1000)/1000;
                                       //dy=Math.rint(dy*1000)/1000;
                   
                  
                   double actdistsq = dx*dx+dy*dy;                        
                   double actdist=Math.sqrt(actdistsq);
                   
                                      
                   
                   
                   if (optDist-actdist>MINDIST) // ist die kollision signifikant ?
                               {
                                       double fx=(actdist>0)?(optDist+0.1)/actdist*dx-dx:0;    // nur die differenz zum jetzigen abstand draufaddieren
                                       double fy=(actdist>0)?(optDist+0.1)/actdist*dy-dy:0;                                            
                                       
                                       // berechneten Vektor anwenden
                                       //fx=elastic(fx);
                                       //fy=elastic(fy);
                                       hitResult.numhits++;
                                       hitResult.otherId=other.getID();
                                       hitResult.otherMotherId=other.getMotherId();
                                       
                                      
                                       
                                       if (pressothers){
                                           ((CenterBasedMechanicalModel) other.getEpisimBioMechanicalModelObject()).extForce=((CenterBasedMechanicalModel) other.getEpisimBioMechanicalModelObject()).extForce.add(new Vector2D(-fx,-fy)); //von mir wegzeigende kraefte addieren
                                       }
                                       extForce=extForce.add(new Vector2D(fx,fy));

                                       
                                      
                               }

                  

                   // all the shit that happens in the neighborhood
                   // consistency = neighborhood momentum
                   if (actdistsq <= pBarrierMemberDist * pBarrierMemberDist)
                   {
                                       
                     Double2D m = ((CenterBasedMechanicalModel)((UniversalCell)b.objs[i]).getEpisimBioMechanicalModelObject()).momentum();
                     hitResult.otherMomentum.x+=m.x;
                     hitResult.otherMomentum.y+=m.y;
                     neighbors++;                         
                     
                     // lipids do not diffuse
                     if ((dy>0) && (other.getIsOuterCell())) hitResult.nextToOuterCell=true; // if the one above is an outer cell, I belong to the barrier 
                   }
               }
           }    

       //hitResult.envSigCalcium=theEpidermis.staticCalciumGradient(thisloc.y);  // noch auf collecten aus umgebund umbauen

       if (neighbors>0)    // average the signals to per cell
       {
           hitResult.otherMomentum.amplify(1/neighbors); 
       }
       return hitResult;
   }    

	public void setPositionRespectingBounds(Double2D p_potentialLoc)
	{
	   // modelling a hole in the wall at position hole holeX with width  holeHalfWidth
	   if (getCell().getIsInNirvana()) 
	           return;
	   double newx=p_potentialLoc.x;
	   double newy=p_potentialLoc.y;               
	   double maxy=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(p_potentialLoc.x);  
	   
	  
	   if (newy>maxy)
	   {
	       newy=maxy;          
	   
	      
	   }
	   else if (newy<10) newy=10;
	
	   Double2D newloc = new Double2D(newx,newy);
	   cellField.setObjectLocation(getCell(), newloc);
	}


	public Double2D calcBoundedPos(Continuous2D pC2dHerd, double xPos, double yPos)
	{
	
	   double newx=0, newy=0;
	   
	   
	   newx=xPos;
	   
	   
	   newy=yPos;
	   double maxy=TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newx);        
	           
	   if (newy>maxy)  // border crossed
	   {
	       if (newy>pC2dHerd.height) // unterste Auffangebene
	       {
	           newy=pC2dHerd.height; 
	           getCell().setInNirvana(true);
	          
	       }
	
	       else            
	           newy=maxy;       
	   }
	   else if (newy<10) newy=10;        
	   return new Double2D(newx, newy);        
	}

   public final double sigmoid(double x)
   {
       // maximum der funktion ist ca. fuer x>3 erreicht
       // bei x gegen 0 hat die funktionen einen sehr kleinen wert, der aber groesser als Null ist.
       return 4/(1+0.1*Math.exp((-x-4)/1));
   }
   
   public void newSimStep(long simstepNumber){
   	
   	CenterBasedMechanicalModelGlobalParameters globalParameters = null;
   	
   	if(ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters() 
   			instanceof CenterBasedMechanicalModelGlobalParameters){
   		globalParameters = (CenterBasedMechanicalModelGlobalParameters) ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters();
   	}
   	
   	else throw new GlobalParameterException("Datatype of Global Mechanical Model Parameters does not fit : "+
   			ModelController.getInstance().getEpisimBioMechanicalModelGlobalParameters().getClass().getName());
   	
   	
   	
   	
   	//////////////////////////////////////////////////
		// calculate ACTION force
		//////////////////////////////////////////////////
		if(!(getCell().getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel)) return;
		
		if(getCell().getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.GRANUCELL){
   	 	setKeratinoWidth(getGKeratinoWidthGranu());
   		setKeratinoHeight(getGKeratinoHeightGranu());
   		getCellEllipseObject().setMajorAxisAndMinorAxis(getGKeratinoWidthGranu(), getGKeratinoHeightGranu());
   	}
		if(getCellEllipseObject() == null){
			//CellEllipse ellipse = new CellEllipse(cell.getID(), ((int)modelConnector.getX()), ((int)modelConnector.getY()), keratinoWidth, keratinoHeight, Color.BLUE);
	      //cell.setCellEllipseObject(ellipse);
			System.out.println("Field cellEllipseObject is not set to a value different from null!");
		}
		
		

		// calc potential location from gravitation and external pressures
		oldLoc = cellField.getObjectLocation(getCell());
		if(oldLoc != null){
		if(extForce.length() > 0.6)
			extForce = extForce.setLength(0.6);
		
		// die funktion muss 0 bei 0 liefern, daher absenkung auf sigmoid(0)
		Double2D gravi = new Double2D(0, globalParameters.getGravitation()); // Vector which avoidance has to process
		Double2D randi = new Double2D(globalParameters.getRandomness()
				* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), globalParameters.getRandomness()
				* (TissueController.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
		Vector2D actionForce = new Vector2D(gravi.x + extForce.x * globalParameters.getExternalPush()
				+ randi.x, gravi.y + extForce.y * globalParameters.getExternalPush());
		Double2D potentialLoc = null;
		
		potentialLoc = new Double2D(cellField.stx(actionForce.x + oldLoc.x), cellField.sty(actionForce.y + oldLoc.y));
		
		extForce.x = 0; // alles einberechnet
		extForce.y = 0;

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
		Vector2D reactionForce = extForce;
		reactionForce = reactionForce.add(hitResult1.otherMomentum.amplify(CONSISTENCY));
		reactionForce = reactionForce.add(hitResult1.adhForce.amplify(globalParameters.getCohesion()));

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
			reactionForce = reactionForce.setLength(actionForce.length() + 0.1);

		extForce.x = 0;
		extForce.y = 0;

		// bound also by borders
		double potX = oldLoc.x + actionForce.x + reactionForce.x;
		double potY = oldLoc.y + actionForce.y + reactionForce.y;
		potentialLoc = new Double2D(cellField.stx(potX), cellField.sty(potY));
		potentialLoc = calcBoundedPos(cellField, potentialLoc.x, potentialLoc.y);

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
				double dx = potentialLoc.x - oldLoc.x;
				lastd = new Double2D(potentialLoc.x - oldLoc.x, potentialLoc.y - oldLoc.y);
				setPositionRespectingBounds(potentialLoc);
			}
		}

		newLoc = cellField.getObjectLocation(getCell());
		double maxy = TissueController.getInstance().getTissueBorder().lowerBoundInMikron(newLoc.x);
		if((maxy - newLoc.y) < globalParameters.getBasalLayerWidth())
			getCell().setIsBasalStatisticsCell(true);
		else
			getCell().setIsBasalStatisticsCell(false); // ABSOLUTE DISTANZ KONSTANTE

		if((maxy - newLoc.y) < globalParameters.getMembraneCellsWidthInMikron()){
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
  	 	modelConnector.setY(TissueController.getInstance().getTissueBorder().getHeightInMikron()- getNewPosition().getY());
  	   modelConnector.setIsSurface(this.getCell().getIsOuterCell() || nextToOuterCell());
  	   
  	   calculateCellEllipse(simstepNumber);
  	   
		
   }
   
   }
   
   private void calculateCellEllipse(long simstepNumber){
   	DrawInfo2D info = this.getCellEllipseObject().getLastDrawInfo2D();
		DrawInfo2D newInfo = null;
		if( info != null){
			newInfo = new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width,info.draw.height), info.clip);
			newInfo.draw.x = ((newInfo.draw.x - newInfo.draw.width*getOldPosition().x) + newInfo.draw.width* getNewPosition().x);
			newInfo.draw.y = ((newInfo.draw.y - newInfo.draw.height*getOldPosition().y) + newInfo.draw.height*getNewPosition().y);
			this.getCellEllipseObject().setLastDrawInfo2D(newInfo, true);
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
    
         Double2D otherloc=cellField.getObjectLocation(actNeighbour);
         double dx = cellField.tdx(getNewPosition().getX(),otherloc.x); // dx, dy is what we add to other to get to this
         double dy = cellField.tdy(getNewPosition().getY(),otherloc.y);
         if(actNeighbour.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
            	CenterBasedMechanicalModel neighbourBiomechModel = (CenterBasedMechanicalModel) actNeighbour.getEpisimBioMechanicalModelObject();
            	neighbourBiomechModel.modelConnector.setDy(-1*dy);
            	neighbourBiomechModel.modelConnector.setDx(dx);
            	
            }
            //  double distance = Math.sqrt(dx*dx + dy*dy);
              
            //  if(distance > 0 && distance <= biomechModelController.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm()){
              
         neighbourCells.add(actNeighbour);
              	
            //}
      }
  	 	return neighbourCells;
   }
   
   
   
   public double getExtForceX () { return extForce.x; }   // for inspector 
   public double getExtForceY () { return extForce.y; }   // for inspector
   
   
   
   public int getGKeratinoHeightGranu() {	return gKeratinoHeightGranu;}
   public int getGKeratinoWidthGranu() { return gKeratinoWidthGranu;	}
   
   public int getKeratinoHeight() {	return keratinoHeight; }
	
	public int getKeratinoWidth() {return keratinoWidth;}
	
	public void setKeratinoHeight(int keratinoHeight) { this.keratinoHeight = keratinoHeight;	}
	
	public void setKeratinoWidth(int keratinoWidth) { this.keratinoWidth = keratinoWidth; }

	public Double2D getNewPosition(){ return newLoc; }

	public Double2D getOldPosition(){ return oldLoc; }

	public int hitsOtherCell(){ return finalHitResult.numhits; }
	
	public boolean nextToOuterCell(){ return finalHitResult.nextToOuterCell; }

	private GenericBag<AbstractCell> getNeighbouringCells() {return neighbouringCells;}
	
	@CannotBeMonitored
	public double getX(){return modelConnector == null ? 
			0//TissueController.getInstance().getActEpidermalTissue().getCellContinous2D().getObjectLocation(cell).getX()
 		 : modelConnector.getX();
	}
	
	@CannotBeMonitored
	public double getY(){return modelConnector == null ? 
			 		0	//	TissueController.getInstance().getTissueBorder().getHeight()- TissueController.getInstance().getActEpidermalTissue().getCellContinous2D().getObjectLocation(cell).getY()
			 	 : modelConnector.getY();
	}
	
	@CannotBeMonitored
	public double getZ(){ return 0;}
	
	@CannotBeMonitored @NoExport
	public Polygon getPolygonCell(){
		return getPolygonCell(null);
	}
	
	@CannotBeMonitored @NoExport
	public Polygon getPolygonNucleus(){
		return getPolygonNucleus(null);
	}
	
	@CannotBeMonitored @NoExport
	public Polygon getPolygonCell(DrawInfo2D info){
		return createHexagonalPolygon(info, getKeratinoWidth(), getKeratinoHeight());
	}
	
	@CannotBeMonitored
	public Polygon getPolygonNucleus(DrawInfo2D info){
		return createHexagonalPolygon(info, 2, 2);
	}
	
	@CannotBeMonitored
	@NoExport
   public CellEllipse getCellEllipseObject(){
   	return this.cellEllipseObject;
   }
	
	private Polygon createHexagonalPolygon(DrawInfo2D info, double width, double height){
				
		double infoX = getX();
		double infoY = getY();
		double infoWidth = 1;
		double infoHeight = 1;
		
		if(info != null){
			infoX = info.draw.x;
			infoY = info.draw.y;
			infoWidth = info.draw.width;
			infoHeight = info.draw.height;
		}
		
		Polygon polygon = new Polygon(); 
		polygon.addPoint((int)(infoX+infoWidth/2.0*width), (int)(infoY));
		polygon.addPoint((int)(infoX+infoWidth/4.0*width), (int)(infoY-infoHeight/2.0*height));
		polygon.addPoint((int)(infoX-infoWidth/4.0*width), (int)(infoY-infoHeight/2.0*height));
		polygon.addPoint((int)(infoX-infoWidth/2.0*width), (int)(infoY));
		polygon.addPoint((int)(infoX-infoWidth/4.0*width), (int)(infoY+infoHeight/2.0*height));
		polygon.addPoint((int)(infoX+infoWidth/4.0*width), (int)(infoY+infoHeight/2.0*height)); 
     
      
      return polygon;
	}
	
	public void setLastDrawInfo2DForNewCellEllipse(DrawInfo2D info, Double2D newloc, Double2D oldLoc){
		DrawInfo2D newInfo = null;
		if(info != null){
			newInfo = new DrawInfo2D(info.gui, info.fieldPortrayal, new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width,info.draw.height), info.clip);
			newInfo.draw.x = ((newInfo.draw.x - newInfo.draw.width*oldLoc.x) + newInfo.draw.width*newloc.x);
			newInfo.draw.y = ((newInfo.draw.y - newInfo.draw.height*oldLoc.y) + newInfo.draw.height*newloc.y);
			getCellEllipseObject().setLastDrawInfo2D(newInfo, true);
		}  
	}
	
	
	public void calculateClippedCell(long simstepNumber){
  	 
    	CellEllipse cellEllipseCell = this.getCellEllipseObject();
    	GenericBag<AbstractCell> realNeighbours = this.getNeighbouringCells();
    	 
    	if(realNeighbours != null && realNeighbours.size() > 0 && cellEllipseCell.getLastDrawInfo2D()!= null){
    		for(AbstractCell neighbouringCell : realNeighbours){
    			if(neighbouringCell.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
    				CenterBasedMechanicalModel biomechModelNeighbour = (CenterBasedMechanicalModel) neighbouringCell.getEpisimBioMechanicalModelObject();
	 	   		 if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(cellEllipseCell.getId(), biomechModelNeighbour.getCellEllipseObject().getId(), simstepNumber)){
	 	   			 CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(cellEllipseCell.getId(),biomechModelNeighbour.getCellEllipseObject().getId());
	 	   			 EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(cellEllipseCell, biomechModelNeighbour.getCellEllipseObject());
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
   public void removeCellFromCellField() {
	   cellField.remove(this.getCell());
   }
	
   public void setCellLocationInCellField(Double2D location){
	   cellField.setObjectLocation(this.getCell(), location);
   }
	
   public Double2D getCellLocationInCellField() {	   
	   return cellField.getObjectLocation(getCell());
   }

   protected Object getCellField() {	  
	   return cellField;
   }
   
   protected void setReloadedCellField(Object cellField) {
   	if(cellField instanceof Continuous2D){
   		CenterBasedMechanicalModel.cellField = (Continuous2D) cellField;
   	}
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
							cell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL){  
						deadCells.add(cell);
						i++;
					}
					else{
						 if(cell.getEpisimBioMechanicalModelObject() instanceof AbstractMechanicalModel){
								AbstractMechanicalModel mechanicalModel = (AbstractMechanicalModel) cell.getEpisimBioMechanicalModelObject();
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
					AbstractMechanicalModel mechanicalModel = (AbstractMechanicalModel) cell.getEpisimBioMechanicalModelObject();
					mechanicalModel.setCellLocationInCellField(map.get(cell.getID()));
				}
			}
			
	   
   }

	
   protected void newSimStepGloballyFinished(long simStepNumber){
   	//not needed	   
   }
}
