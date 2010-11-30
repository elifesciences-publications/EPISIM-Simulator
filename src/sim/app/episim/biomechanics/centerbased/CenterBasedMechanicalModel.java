package sim.app.episim.biomechanics.centerbased;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import ec.util.MersenneTwisterFast;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.EpisimMechanicalModel;

import sim.app.episim.AbstractCell;
import sim.app.episim.TissueServer;
import sim.app.episim.UniversalCell;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.Vector2D;
import sim.app.episim.visualization.CellEllipse;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.util.Bag;
import sim.util.Double2D;


public class CenterBasedMechanicalModel implements EpisimMechanicalModel {
	
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
   private int keratinoHeight=-1; // h�he keratino
   
   private Vector2D extForce = new Vector2D(0,0);
   private Double2D lastd = new Double2D(0,0);
   private AbstractCell cell;
   
   private Double2D oldLoc;
   private Double2D newLoc;
   
   private HitResultClass finalHitResult;
   
   private Bag neighbouringCells;
   
   public CenterBasedMechanicalModel(){
   	this(null);   	
   }
   
   public CenterBasedMechanicalModel(AbstractCell cell){
   	extForce=new Vector2D(0,0);      
      keratinoWidth=GINITIALKERATINOWIDTH; //theEpidermis.InitialKeratinoSize;
      keratinoHeight=GINITIALKERATINOHEIGHT; //theEpidermis.InitialKeratinoSize;
      lastd=new Double2D(0.0,-3);
      this.cell = cell;
      if(cell != null && cell.getCellEllipseObject() == null && cell.getEpisimCellBehavioralModelObject() != null){
			CellEllipse ellipse = new CellEllipse(cell.getID(), ((int)cell.getEpisimCellBehavioralModelObject().getX()), ((int)cell.getEpisimCellBehavioralModelObject().getY()), keratinoWidth, keratinoHeight, Color.BLUE);
	      cell.setCellEllipseObject(ellipse);
		}
   }
   
   public double orientation2D()
   {
       if (lastd.x == 0 && lastd.y == 0) return 0;
       return Math.atan2(lastd.y, lastd.x);
   }
   public double orientation2D(double p_dx, double p_dy)
   {
       if (p_dx == 0 && p_dy == 0) return 0;
       return Math.atan2(p_dy, p_dx);
   }    
   public Double2D momentum()
   {
       return lastd;
   }  
   
   public final Double2D forceFromBound(Continuous2D pC2dHerd, double x) // Calculate the Force orthogonal to lower bound
   {        
       double yleft=TissueController.getInstance().getTissueBorder().lowerBound(pC2dHerd.stx(x-5));
       double yright=TissueController.getInstance().getTissueBorder().lowerBound(pC2dHerd.stx(x+5));
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
       if (b==null || b.numObjs == 0 || cell.isInNirvana()) return hitResult;
       
       
       
              
       int i=0;
       double adxOpt = GOPTIMALKERATINODISTANCE; //KeratinoWidth-2+theEpidermis.cellSpace;                         was 4 originally then 5
       //double adxOpt = KeratinoWidth; //KeratinoWidth-2+theEpidermis.cellSpace;                        
       //double adyOpt = 5; // 3+theEpidermis.cellSpace;
       
       
       if (cell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal()==EpisimDifferentiationLevel.GRANUCELL) adxOpt=GOPTIMALKERATINODISTANCEGRANU; // was 3 // 4 in modified version
       
       double optDistSq = adxOpt*adxOpt; //+adyOpt*adyOpt;
       double optDist=Math.sqrt(optDistSq);
       //double outerCircleSq = (neigh_p*adxOpt)*(neigh_p*adxOpt)+(neigh_p*adyOpt)*(neigh_p*adyOpt);
       int neighbors=0;
      

       for(i=0;i<b.numObjs;i++)
       {
               if (!(b.objs[i] instanceof UniversalCell))
                   continue;
               
               if(!(((UniversalCell) b.objs[i]).getEpisimMechanicalModelObject() instanceof CenterBasedMechanicalModel)) continue;
       
           UniversalCell other = (UniversalCell)(b.objs[i]);
           if (other != cell)
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
                                       hitResult.otherMotherId=other.getMotherID();
                                       
                                       if ((other.getMotherID()==cell.getID()) || (other.getID()==cell.getMotherID()))
                                       {
                                           //fx*=1.5;// birth pressure is greater than normal pressure
                                           //fy*=1.5;
                                       }
                                       
                                       if (pressothers){
                                           ((CenterBasedMechanicalModel) other.getEpisimMechanicalModelObject()).extForce=((CenterBasedMechanicalModel) other.getEpisimMechanicalModelObject()).extForce.add(new Vector2D(-fx,-fy)); //von mir wegzeigende kraefte addieren
                                       }
                                       extForce=extForce.add(new Vector2D(fx,fy));

                                       
                                      
                               }

                  

                   // all the shit that happens in the neighborhood
                   // consistency = neighborhood momentum
                   if (actdistsq <= pBarrierMemberDist * pBarrierMemberDist)
                   {
                                       
                     Double2D m = ((CenterBasedMechanicalModel)((UniversalCell)b.objs[i]).getEpisimMechanicalModelObject()).momentum();
                     hitResult.otherMomentum.x+=m.x;
                     hitResult.otherMomentum.y+=m.y;
                     neighbors++;                         
                     
                     // lipids do not diffuse
                     if ((dy>0) && (other.isOuterCell())) hitResult.nextToOuterCell=true; // if the one above is an outer cell, I belong to the barrier 
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

	public void setPositionRespectingBounds(Continuous2D pC2dHerd, Double2D p_potentialLoc)
	{
	   // modelling a hole in the wall at position hole holeX with width  holeHalfWidth
	   if (cell.isInNirvana()) 
	           return;
	   double newx=p_potentialLoc.x;
	   double newy=p_potentialLoc.y;               
	   double maxy=TissueController.getInstance().getTissueBorder().lowerBound(p_potentialLoc.x);  
	   
	  
	   if (newy>maxy)
	   {
	       newy=maxy;          
	   
	      
	   }
	   else if (newy<10) newy=10;
	
	   Double2D newloc = new Double2D(newx,newy);
	   pC2dHerd.setObjectLocation(cell, newloc);
	}


	public Double2D calcBoundedPos(Continuous2D pC2dHerd, double xPos, double yPos)
	{
	
	   double newx=0, newy=0;
	   
	   
	   newx=xPos;
	   
	   
	   newy=yPos;
	   double maxy=TissueController.getInstance().getTissueBorder().lowerBound(newx);        
	           
	   if (newy>maxy)  // border crossed
	   {
	       if (newy>pC2dHerd.height) // unterste Auffangebene
	       {
	           newy=pC2dHerd.height; 
	           cell.setInNirvana(true);
	          
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
   
   public void newSimStep(){
   // ////////////////////////////////////////////////
		// calculate ACTION force
		// ////////////////////////////////////////////////
		if(!(cell.getEpisimMechanicalModelObject() instanceof CenterBasedMechanicalModel)) return;
		
		if(cell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.GRANUCELL){
   	 	setKeratinoWidth(getGKeratinoWidthGranu());
   		setKeratinoHeight(getGKeratinoHeightGranu());
   		cell.getCellEllipseObject().setMajorAxisAndMinorAxis(getGKeratinoWidthGranu(), getGKeratinoHeightGranu());
   	}
		if(cell.getCellEllipseObject() == null){
			CellEllipse ellipse = new CellEllipse(cell.getID(), ((int)cell.getEpisimCellBehavioralModelObject().getX()), ((int)cell.getEpisimCellBehavioralModelObject().getY()), keratinoWidth, keratinoHeight, Color.BLUE);
	      cell.setCellEllipseObject(ellipse);
		}
		
		
		
		
		
		// Double2D rand = randomness(flock.random);
		// Double2D mome = momentum();

		// calc potential location from gravitation and external pressures
		oldLoc = TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().getObjectLocation(cell);
		if(oldLoc != null){
		if(extForce.length() > 0.6)
			extForce = extForce.setLength(0.6);
		// extForce=extForce.setLength(2*(1-Math.exp(-0.5*extForce.length())));
		// extForce=extForce.setLength(sigmoid(extForce.length())-sigmoid(0));
		// //
		// die funktion muss 0 bei 0 liefern, daher absenkung auf sigmoid(0)
		Double2D gravi = new Double2D(0, ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getGravitation()); // Vector
		// which
		// avoidance
		// has
		// to
		// process
		Double2D randi = new Double2D(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getRandomness()
				* (TissueServer.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5), ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getRandomness()
				* (TissueServer.getInstance().getActEpidermalTissue().random.nextDouble() - 0.5));
		Vector2D actionForce = new Vector2D(gravi.x + extForce.x * ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getExternalPush()
				+ randi.x, gravi.y + extForce.y * ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getExternalPush());
		Double2D potentialLoc = null;
		
			potentialLoc = new Double2D(TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().stx(actionForce.x + oldLoc.x), 
					TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().sty(actionForce.y + oldLoc.y));
		
		extForce.x = 0; // alles einberechnet
		extForce.y = 0;

		//////////////////////////////////////////////////
		// try ACTION force
		//////////////////////////////////////////////////
		Bag b = TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().getObjectsWithinDistance(potentialLoc,
				ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getNeighborhood_�m(), false); // theEpidermis.neighborhood
		HitResultClass hitResult1;
		hitResult1 = hitsOther(b, TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D(), potentialLoc, true, NEXTTOOUTERCELL);

		//////////////////////////////////////////////////
		// estimate optimised POS from REACTION force
		//////////////////////////////////////////////////
		// optimise my own position by giving way to the calculated pressures
		Vector2D reactionForce = extForce;
		reactionForce = reactionForce.add(hitResult1.otherMomentum.amplify(CONSISTENCY));
		reactionForce = reactionForce.add(hitResult1.adhForce.amplify(ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getCohesion()));

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
		potentialLoc = new Double2D(TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().stx(potX), TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().sty(potY));
		potentialLoc = calcBoundedPos(TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D(), potentialLoc.x, potentialLoc.y);

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

		b = TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().getObjectsWithinDistance(potentialLoc,
				ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getNeighborhood_�m(), false); // theEpidermis.neighborhood
		HitResultClass hitResult2;
		hitResult2 = hitsOther(b, TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D(), potentialLoc, true, NEXTTOOUTERCELL);

		// move only on pressure when not stem cell
		if(cell.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() != EpisimDifferentiationLevel.STEMCELL){
			if((hitResult2.numhits == 0)
					|| ((hitResult2.numhits == 1) && ((hitResult2.otherId == cell.getMotherID()) || (hitResult2.otherMotherId == cell.getID())))){
				double dx = potentialLoc.x - oldLoc.x;
				lastd = new Double2D(potentialLoc.x - oldLoc.x, potentialLoc.y - oldLoc.y);
				setPositionRespectingBounds(TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D(), potentialLoc);
			}
		}

		newLoc = TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D().getObjectLocation(cell);
		double maxy = TissueController.getInstance().getTissueBorder().lowerBound(newLoc.x);
		if((maxy - newLoc.y) < ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getBasalLayerWidth())
			cell.setIsBasalStatisticsCell(true);
		else
			cell.setIsBasalStatisticsCell(false); // ABSOLUTE DISTANZ KONSTANTE

		if((maxy - newLoc.y) < ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getMembraneCellsWidth())
			cell.setIsMembraneCell(true);
		else
			cell.setIsMembraneCell(false); // ABSOLUTE DISTANZ KONSTANTE  
		
		neighbouringCells = b;
		finalHitResult = hitResult2;
   }
   
   }
   
   
   
   
   
   public double getExtForceX () { return extForce.x; }   // for inspector 
   public double getExtForceY () { return extForce.y; }   // for inspector
   public void setCell(AbstractCell cell){
   	this.cell = cell;
   }
   
   
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

	public Bag getNeighbouringCells() {return neighbouringCells;}
	
	 
}