package sim.app.episim;
import sim.app.episim.charts.ChartController;
import sim.app.episim.charts.ChartMonitoredCellType;
import sim.app.episim.model.BioChemicalModelController;
import sim.app.episim.model.BioMechanicalModelController;
import sim.app.episim.model.ModelController;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import ec.util.*;
import episiminterfaces.EpisimCellDiffModel;
import episiminterfaces.EpisimCellDiffModelGlobalParameters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jfree.data.xy.XYSeries;
import sim.portrayal.*;

public class KCyte extends CellType implements ChartMonitoredCellType
{
//	-----------------------------------------------------------------------------------------------------------------------------------------   
// CONSTANTS
//	-----------------------------------------------------------------------------------------------------------------------------------------          
	
	private static final long serialVersionUID = 5212944079288103141L;
   
   private final String NAME = "Keratinocyte";
   
   public final int GOPTIMALKERATINODISTANCE=4; // Default: 4
   public final int GOPTIMALKERATINODISTANCEGRANU=4; // Default: 3
   public final int GINITIALKERATINOHEIGHT=5; // Default: 5
   public final int GINITIALKERATINOWIDTH=5; // Default: 5

//	-----------------------------------------------------------------------------------------------------------------------------------------   
// VARIABLES
//	-----------------------------------------------------------------------------------------------------------------------------------------          
   private transient ModelController modelController;
   private transient BioChemicalModelController biochemModelController;
   private transient BioMechanicalModelController biomechModelController;
   
   private int gKeratinoWidthGranu=9; // defauolt: 10
   private int gKeratinoHeightGranu=4;
                
   private Double2D lastd = new Double2D(0,0);
 
   private Epidermis epidermis;    
   
   
   
   
   private double lastDrawInfoX;
   private double lastDrawInfoY;
   private boolean lastDrawInfoAssigned=false;
   private double neighborDrawInfoX[]=new double[50];
   private double neighborDrawInfoY[]=new double[50];  
   private int voronoiStable=0; // count if a voroni is displayable, only display when at least several times stable, so avoid permanent switching back to standardform
   private GrahamPoint voronoihull[]=new GrahamPoint[50];
   private int voronoihullvertexes=0;
   
   private int formCount=0; // Number of neighbors

   private int keratinoWidth=-11; // breite keratino
   private int keratinoHeight=-1; // höhe keratino
   
   private int ownColor=0;
   private int motherIdentity=-1;   // -1 means not filled yet
   private boolean inNirvana=false; // unvisible and without action: only ageing is active
   
   private int spinosum_counter=0;
  
   // public boolean dead = false;
   private Vector2D extForce = new Vector2D(0,0);
   private int identity=0;
   private long local_maxAge;
   
   private boolean birthWish=false;
   
   private int hasGivenIons=0;

  
   
   private boolean isOuterCell=false;
   private boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
   private boolean isMembraneCell=false;    // cells directly sitting on membrane, very strict
   
   private Stoppable stoppable = null;
   
   ///////////////////////////////////////////////////////////
   // THE CELL DIFFERENTIATION MODEL
   ///////////////////////////////////////////////////////////
   
   private EpisimCellDiffModel cellDiffModelObjekt;
   
//-----------------------------------------------------------------------------------------------------------------------------------------   
//-----------------------------------------------------------------------------------------------------------------------------------------   
         
   public KCyte(){
   
   }
    public KCyte(Epidermis epidermis, EpisimCellDiffModel cellDiffModel)
    {
   	 	this.cellDiffModelObjekt = cellDiffModel;
   	 	modelController = ModelController.getInstance();
   	 	biochemModelController = modelController.getBioChemicalModelController();
   	 	biomechModelController = modelController.getBioMechanicalModelController();
   	 // always as first thing, set beholder
        this.epidermis=epidermis;
        // now local vars
        
       
        extForce=new Vector2D(0,0);
        inNirvana=false;        
        keratinoWidth=GINITIALKERATINOWIDTH; //theEpidermis.InitialKeratinoSize;
        keratinoHeight=GINITIALKERATINOHEIGHT; //theEpidermis.InitialKeratinoSize; 
       
        isOuterCell=false;        
               
        voronoihullvertexes=0;
        voronoiStable=0;
        
        
        
                      
        lastd=new Double2D(0.0,-3);
        
        
        epidermis.checkMemory();
        
       
        epidermis.getAllCells().add(this); // register this as additional one in Bag
       
        
        
       
    }

    public void reloadControllers(){
   	 modelController = ModelController.getInstance();
 	 	biochemModelController = modelController.getBioChemicalModelController();
 	 	biomechModelController = modelController.getBioMechanicalModelController();
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
        double yleft=TissueBorder.lowerBound(pC2dHerd.stx(x-5));
        double yright=TissueBorder.lowerBound(pC2dHerd.stx(x+5));
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
        int otherId; // when only one hit, then how id of this hit (usually this will be the mother)
        int otherMotherId; // mother of other
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
            if (b==null || b.numObjs == 0 || this.inNirvana) return hitResult;
            
            
            
                   
            int i=0;
            double adxOpt = GOPTIMALKERATINODISTANCE; //KeratinoWidth-2+theEpidermis.cellSpace;                         was 4 originally then 5
            //double adxOpt = KeratinoWidth; //KeratinoWidth-2+theEpidermis.cellSpace;                        
            //double adyOpt = 5; // 3+theEpidermis.cellSpace;
            
            
            if (this.cellDiffModelObjekt.getDifferentiation()==EpisimCellDiffModelGlobalParameters.GRANUCELL) adxOpt=GOPTIMALKERATINODISTANCEGRANU; // was 3 // 4 in modified version
            
            double optDistSq = adxOpt*adxOpt; //+adyOpt*adyOpt;
            double optDist=Math.sqrt(optDistSq);
            //double outerCircleSq = (neigh_p*adxOpt)*(neigh_p*adxOpt)+(neigh_p*adyOpt)*(neigh_p*adyOpt);
            int neighbors=0;
            formCount=0;

            for(i=0;i<b.numObjs;i++)
                {
                    if (!(b.objs[i] instanceof KCyte))
                        continue;
            
                KCyte other = (KCyte)(b.objs[i]);
                if (other != this )
                    {
                        Double2D otherloc=pC2dHerd.getObjectLocation(other);
                        double dx = pC2dHerd.tdx(thisloc.x,otherloc.x); // dx, dy is what we add to other to get to this
                        double dy = pC2dHerd.tdy(thisloc.y,otherloc.y);
                                            //dx=Math.rint(dx*1000)/1000;
                                            //dy=Math.rint(dy*1000)/1000;
                        
                       
                        double actdistsq = dx*dx+dy*dy;                        
                        double actdist=Math.sqrt(actdistsq);
                        
                                            // Voronoi collision with other cells
                                            if (formCount<49) // enter all, also those which are only linked through left or right border
                                            {
                                                //double notorus_dx=thisloc.x-otherloc.x;
                                                //double notorus_dy=thisloc.y-otherloc.y;
                                                //if ((notorus_dx<(double)gOptimalKeratinoDistance*1.5) &&
//                                                    (notorus_dy<(double)gOptimalKeratinoDistance*1.5))
                                                if (actdist < (optDist*1.7))
                                                {
                                                    if (other.lastDrawInfoAssigned==true)
                                                        neighborDrawInfoX[formCount]=other.lastDrawInfoX;
                                                        neighborDrawInfoY[formCount]=other.lastDrawInfoY;
                                                    ++formCount;
                                                }
                                            }
                        
                        
                        if (optDist-actdist>epidermis.getMinDist()) // ist die kollision signifikant ?
                                    {
                                            double fx=(actdist>0)?(optDist+0.1)/actdist*dx-dx:0;    // nur die differenz zum jetzigen abstand draufaddieren
                                            double fy=(actdist>0)?(optDist+0.1)/actdist*dy-dy:0;                                            
                                            
                                            // berechneten Vektor anwenden
                                            //fx=elastic(fx);
                                            //fy=elastic(fy);
                                            hitResult.numhits++;
                                            hitResult.otherId=other.identity;
                                            hitResult.otherMotherId=other.motherIdentity;
                                            
                                            if ((other.motherIdentity==identity) || (other.identity==motherIdentity))
                                            {
                                                //fx*=1.5;// birth pressure is greater than normal pressure
                                                //fy*=1.5;
                                            }
                                            
                                            if (pressothers)
                                                other.extForce=other.extForce.add(new Vector2D(-fx,-fy)); //von mir wegzeigende kraefte addieren
                                            extForce=extForce.add(new Vector2D(fx,fy));

                                            
                                           
                                    }

                        else // attraction forces 
                        {
                            double adhfac=biomechModelController.getEpisimMechanicalModelGlobalParameters().gibAdh_array(this.cellDiffModelObjekt.getDifferentiation(), other.getEpisimCellDiffModelObject().getDifferentiation());                           
                            if (actdist-optDist<biomechModelController.getEpisimMechanicalModelGlobalParameters().getAdhesionDist())
                                        {                                                   
                                                double sx=dx-dx*optDist/actdist;    // nur die differenz zum jetzigen abstand draufaddieren
                                                double sy=dy-dy*optDist/actdist;
                                                //if (pressothers)
                                                //    other.extForce=other.extForce.add(new Vector2D(sx,sy)); //von mir wegzeigende kraefte addieren                                                                                      
                                                hitResult.adhForce=hitResult.adhForce.add(new Vector2D(-sx*adhfac,-sy*adhfac/5.0)); // minus, cause: dx,dy is way from other to this: minus=way to other
                                        }                                               
                        }

                        // all the shit that happens in the neighborhood
                        // consistency = neighborhood momentum
                        if (actdistsq <= pBarrierMemberDist * pBarrierMemberDist)
                        {
                                            
                          Double2D m = ((KCyte)b.objs[i]).momentum();
                          hitResult.otherMomentum.x+=m.x;
                          hitResult.otherMomentum.y+=m.y;
                          neighbors++;                         
                          
                          // lipids do not diffuse
                          if ((dy>0) && (other.isOuterCell)) hitResult.nextToOuterCell=true; // if the one above is an outer cell, I belong to the barrier 
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
        if (inNirvana) 
                return;
        double newx=p_potentialLoc.x;
        double newy=p_potentialLoc.y;               
        double maxy=TissueBorder.lowerBound(p_potentialLoc.x);  
        
       
        if (newy>maxy)
        {
            newy=maxy;          
        
           
        }
        else if (newy<10) newy=10;

        Double2D newloc = new Double2D(newx,newy);
        pC2dHerd.setObjectLocation(this, newloc);
    }

    
    public Double2D calcBoundedPos(Continuous2D pC2dHerd, double xPos, double yPos)
    {

        double newx=0, newy=0;
        
        
        newx=xPos;
        
        
        newy=yPos;
        double maxy=TissueBorder.lowerBound(newx);        
                
        if (newy>maxy)  // border crossed
        {
            if (newy>pC2dHerd.height) // unterste Auffangebene
            {
                newy=pC2dHerd.height; 
                inNirvana=true;
               
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
    
    public KCyte makeChild(EpisimCellDiffModel cellDiffModel)
    {       
        
   	 Continuous2D cellContinous2D = epidermis.getCellContinous2D();
   	 
   	 // Either we get use a currently unused cell oder we allocate a new one
        KCyte kcyte;        
       
            kcyte= new KCyte(epidermis, cellDiffModel); 
            epidermis.inkrementNumberOfKCytes();
            kcyte.identity = epidermis.getNumberOfKCytes();
            cellDiffModel.setId(kcyte.identity);
            Stoppable stoppable = epidermis.schedule.scheduleRepeating(kcyte);   // schedule only if not already running
            kcyte.setStoppable(stoppable);

        Double2D newloc=cellContinous2D.getObjectLocation(this);
        newloc=new Double2D(newloc.x +epidermis.random.nextDouble()*0.5-0.25, newloc.y-epidermis.random.nextDouble()*0.5-0.1);
        
        kcyte.motherIdentity=this.identity;
        kcyte.ownColor=this.epidermis.random.nextInt(200);
        kcyte.epidermis = this.epidermis;        // the herd
             
        kcyte.local_maxAge= biochemModelController.getEpisimCellDiffModelGlobalParameters().getMaxAge();
        long pSimTime=(long) epidermis.schedule.time();
        if (pSimTime<(kcyte.local_maxAge)){ 
      	  kcyte.local_maxAge=pSimTime;
      	  cellDiffModel.setMaxAge((int)kcyte.local_maxAge);
        }

        
        cellContinous2D.setObjectLocation(kcyte, newloc);        
       
        return kcyte;
    }

    public void makeTACell(EpisimCellDiffModel cellDiffModel)
    {
        epidermis.inkrementActualTA();
        epidermis.inkrementActualKCytes();
        KCyte taCell=makeChild(cellDiffModel);
                    
        taCell.getEpisimCellDiffModelObject()
        	.setAge(this.epidermis.random.nextInt(biochemModelController.getEpisimCellDiffModelGlobalParameters().getCellCycleTA()));  // somewhere on the TA Cycle
        // erben der signal concentrationen
        if(this.identity == 2 && !epidermis.alreadyfollow){
      	  taCell.follow = true;
      	  epidermis.alreadyfollow = true;
        }
    }
    public boolean follow = false;
    public void makeSpiCell(EpisimCellDiffModel cellDiffModel)
    {
        epidermis.inkrementActualSpi();
        epidermis.inkrementActualKCytes();
        KCyte spiCell=makeChild(cellDiffModel);
        
        
       

    }

    
    private EpisimCellDiffModel[] getRealNeighbours(Bag neighbours, Continuous2D cellContinous2D, Double2D thisloc){
   	 List<EpisimCellDiffModel> neighbourCells = new ArrayList<EpisimCellDiffModel>();
   	 for(int i=0;i<neighbours.numObjs;i++)
       {
   		 KCyte actNeighbour = (KCyte)(neighbours.objs[i]);
     
               Double2D otherloc=cellContinous2D.getObjectLocation(actNeighbour);
               double dx = cellContinous2D.tdx(thisloc.x,otherloc.x); // dx, dy is what we add to other to get to this
               double dy = cellContinous2D.tdy(thisloc.y,otherloc.y);
               
               actNeighbour.getEpisimCellDiffModelObject().setDy(-1*dy);
               actNeighbour.getEpisimCellDiffModelObject().setDx(dx);
               
         //      double distance = Math.sqrt(dx*dx + dy*dy);
               
             //  if(distance > 0 && distance <= biomechModelController.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm()){
               
               	neighbourCells.add(actNeighbour.getEpisimCellDiffModelObject());
               	
             //}
        }
   	 return neighbourCells.toArray(new EpisimCellDiffModel[neighbourCells.size()]);
    }
   
    private boolean isSurfaceCell(EpisimCellDiffModel[] neighbours){
   	 if(this.cellDiffModelObjekt.getDifferentiation() == EpisimCellDiffModelGlobalParameters.STEMCELL) return false;
   	 else{
   		
   		 int leftSideNeighbours = 0;
   		 int rightSideNeighbours= 0;
   		 int upperNeighbours = 0;
   		 double height = (double) this.getKeratinoHeight();
   		 double width = (double) this.getKeratinoWidth();
   		  for(EpisimCellDiffModel actNeighbour :neighbours){
   			  double dx =actNeighbour.getDx();
   			  double dy =actNeighbour.getDy();
   			  if(dy >=0) upperNeighbours++;
   			  else if(dy < height && dx < 0 && Math.abs(dx)>= width) leftSideNeighbours++;
   			  else if(dy < height  && dx > 0 && Math.abs(dx)>= width) rightSideNeighbours++;
   		  }
   		 
   		 if(upperNeighbours == 0 || rightSideNeighbours == 0 || leftSideNeighbours == 0) return true;
   		 
   	 }
   	 return false;
    }
    
    public void differentiate(Bag neighbours, Continuous2D cellContinous2D, Double2D thisloc, boolean nextToOuterCell, boolean hasCollision)
    {
     // modelController.getBioChemicalModelController().differentiate(this, epidermis, pBarrierMember);
   	 EpisimCellDiffModel[] realNeighbours = getRealNeighbours(neighbours, cellContinous2D, thisloc);
   	// this.isOuterCell = isSurfaceCell(realNeighbours);
   	 this.cellDiffModelObjekt.setX(thisloc.getX());
   	 this.cellDiffModelObjekt.setY(-1*thisloc.getY());
   	 this.cellDiffModelObjekt.setIsMembrane(this.isMembraneCell);
   	 this.cellDiffModelObjekt.setIsSurface(this.isOuterCell || nextToOuterCell);
   	 this.cellDiffModelObjekt.setHasCollision(hasCollision);
   	 if(this.cellDiffModelObjekt.getDifferentiation() == EpisimCellDiffModelGlobalParameters.STEMCELL) this.cellDiffModelObjekt.setAge(0);
   	 else this.cellDiffModelObjekt.setAge(this.cellDiffModelObjekt.getAge()+1);
   	 
   	 
   	
   	
   	   	  	 
   	 	makeChildren(this.cellDiffModelObjekt.oneStep(realNeighbours));
   	 	if(this.cellDiffModelObjekt.getDifferentiation() == EpisimCellDiffModelGlobalParameters.GRANUCELL){
   	 		setKeratinoWidth(getGKeratinoWidthGranu());
   			setKeratinoHeight(getGKeratinoHeightGranu());
   	 	}
        if (!this.cellDiffModelObjekt.getIsAlive()) // && (isOuterCell))
        {
            killCell();
        }
  
   }
    
    private void makeChildren(EpisimCellDiffModel[] children){
   	 if(children!=null){
   		 for(EpisimCellDiffModel actChild: children){
   			 if(actChild.getDifferentiation() == EpisimCellDiffModelGlobalParameters.TACELL) makeTACell(actChild);
   			 else if(actChild.getDifferentiation() == EpisimCellDiffModelGlobalParameters.EARLYSPICELL) makeSpiCell(actChild);
   		 }
   	 }
    }

    
    public void killCell(){
   	    	 
   	 epidermis.dekrementActualNoNucleus();
   	 this.cellDiffModelObjekt.setDifferentiation(EpisimCellDiffModelGlobalParameters.KTYPE_NIRVANA);
   	 epidermis.dekrementActualKCytes();
   	 inNirvana=true;            
   	 
   	 epidermis.getCellContinous2D().remove(this);
    }

    
	public void step(SimState state) {

		final Epidermis epiderm = (Epidermis) state;
		
	
		if(inNirvana){
			

			removeFromSchedule();
			
		}
		else{
			hasGivenIons = 0;

			// ////////////////////////////////////////////////
			// calculate ACTION force
			// ////////////////////////////////////////////////
			int ministep = 1;
			int maxmini = 1;

			// Double2D rand = randomness(flock.random);
			// Double2D mome = momentum();

			// calc potential location from gravitation and external pressures
			Double2D oldLoc = epiderm.getCellContinous2D().getObjectLocation(this);

			if(extForce.length() > 0.6)
				extForce = extForce.setLength(0.6);
			// extForce=extForce.setLength(2*(1-Math.exp(-0.5*extForce.length())));
			// extForce=extForce.setLength(sigmoid(extForce.length())-sigmoid(0));
			// //
			// die funktion muss 0 bei 0 liefern, daher absenkung auf sigmoid(0)
			Double2D gravi = new Double2D(0, biomechModelController.getEpisimMechanicalModelGlobalParameters().getGravitation()); // Vector
			// which
			// avoidance
			// has
			// to
			// process
			Double2D randi = new Double2D(biomechModelController.getEpisimMechanicalModelGlobalParameters().getRandomness()
					* (epidermis.random.nextDouble() - 0.5), biomechModelController.getEpisimMechanicalModelGlobalParameters().getRandomness()
					* (epidermis.random.nextDouble() - 0.5));
			Vector2D actionForce = new Vector2D(gravi.x + extForce.x * biomechModelController.getEpisimMechanicalModelGlobalParameters().getExternalPush()
					+ randi.x, gravi.y + extForce.y * biomechModelController.getEpisimMechanicalModelGlobalParameters().getExternalPush());
			Double2D potentialLoc = new Double2D(epiderm.getCellContinous2D().stx(actionForce.x + oldLoc.x), epiderm
					.getCellContinous2D().sty(actionForce.y + oldLoc.y));
			extForce.x = 0; // alles einberechnet
			extForce.y = 0;

			// ////////////////////////////////////////////////
			// try ACTION force
			// ////////////////////////////////////////////////
			Bag b = epiderm.getCellContinous2D().getObjectsWithinDistance(potentialLoc,
					biomechModelController.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm(), false); // theEpidermis.neighborhood
			HitResultClass hitResult1;
			hitResult1 = hitsOther(b, epiderm.getCellContinous2D(), potentialLoc, true, epidermis.NextToOuterCell);

			// ////////////////////////////////////////////////
			// estimate optimised POS from REACTION force
			// ////////////////////////////////////////////////
			// optimise my own position by giving way to the calculated pressures
			Vector2D reactionForce = extForce;
			reactionForce = reactionForce.add(hitResult1.otherMomentum.amplify(epidermis.getConsistency()));
			reactionForce = reactionForce.add(hitResult1.adhForce.amplify(biomechModelController.getEpisimMechanicalModelGlobalParameters().getCohesion()));

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
			potentialLoc = new Double2D(epiderm.getCellContinous2D().stx(potX), epiderm.getCellContinous2D().sty(potY));
			potentialLoc = calcBoundedPos(epiderm.getCellContinous2D(), potentialLoc.x, potentialLoc.y);

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

			b = epiderm.getCellContinous2D().getObjectsWithinDistance(potentialLoc,
					biomechModelController.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm(), false); // theEpidermis.neighborhood
			HitResultClass hitResult2;
			hitResult2 = hitsOther(b, epiderm.getCellContinous2D(), potentialLoc, true, epidermis.NextToOuterCell);

			// move only on pressure when not stem cell
			if(this.cellDiffModelObjekt.getDifferentiation() != EpisimCellDiffModelGlobalParameters.STEMCELL){
				if((hitResult2.numhits == 0)
						|| ((hitResult2.numhits == 1) && ((hitResult2.otherId == this.motherIdentity) || (hitResult2.otherMotherId == this.identity)))){
					double dx = potentialLoc.x - oldLoc.x;
					lastd = new Double2D(potentialLoc.x - oldLoc.x, potentialLoc.y - oldLoc.y);
					setPositionRespectingBounds(epiderm.getCellContinous2D(), potentialLoc);
				}
			}

			Double2D newLoc = epiderm.getCellContinous2D().getObjectLocation(this);
			double maxy = TissueBorder.lowerBound(newLoc.x);
			if((maxy - newLoc.y) < biomechModelController.getEpisimMechanicalModelGlobalParameters().getBasalLayerWidth())
				isBasalStatisticsCell = true;
			else
				isBasalStatisticsCell = false; // ABSOLUTE DISTANZ KONSTANTE

			if((maxy - newLoc.y) < biomechModelController.getEpisimMechanicalModelGlobalParameters().getMembraneCellsWidth())
				isMembraneCell = true;
			else
				isMembraneCell = false; // ABSOLUTE DISTANZ KONSTANTE

			

			// ///////////////////////////////////////////////////////
			// Differentiation: Calling the loaded Cell-Diff-Model
			// //////////////////////////////////////////////////////

			differentiate(b,epiderm.getCellContinous2D(), newLoc, hitResult2.nextToOuterCell, hitResult2.numhits != 0);
			
			
			
			if(this.follow && this.cellDiffModelObjekt.getIsAlive()){
           /*      try {
                  BufferedWriter out = new BufferedWriter(new FileWriter("d:\\simresults_neu.csv", true));
                  out.write((int) (state.schedule.time()) + ";");
                  out.write(NumberFormat.getInstance(Locale.GERMANY).format(this.identity)+ ";");
                  out.write(NumberFormat.getInstance(Locale.GERMANY).format(this.cellDiffModelObjekt.getDifferentiation())+ ";");
                  out.write(NumberFormat.getInstance(Locale.GERMANY).format(this.cellDiffModelObjekt.getAge())+ ";");
                  out.write(NumberFormat.getInstance(Locale.GERMANY).format(this.cellDiffModelObjekt.getCa())+ ";");
                  out.write(NumberFormat.getInstance(Locale.GERMANY).format(this.cellDiffModelObjekt.getLam())+ ";");
                  out.write(NumberFormat.getInstance(Locale.GERMANY).format(this.cellDiffModelObjekt.getLip())+";");
                  
                 
                  out.write("\n");
                  out.close();
                   } catch (IOException e) {}     */    
                 
			}
			
		}
	}

	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		
		for(Method m : this.getClass().getMethods()){
			if((m.getName().startsWith("get") && ! m.getName().equals("getParameters")) || m.getName().startsWith("is")) methods.add(m);
		}
		return methods;
	}
	
	
	public void stop(){	
	 System.out.println("Ich bin die Stopp-Methode, ja ja die Stopp-Methode");
	}
	public void removeFromSchedule(){
		if(stoppable != null) stoppable.stop();			
	}

//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
// INCREMENT-DECREMENT-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
		
	
	public void incrementSpinosumCounter(){ spinosum_counter +=1;}
	public void incrementVoronoiStable(){ voronoiStable +=1; }
	
	public void decrementSpinosumCounter(){ spinosum_counter -=1;}
	public void decrementVoronoiStable(){ voronoiStable -= 1; }
		
	
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
// GETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public Epidermis getEpidermis() {	return epidermis;	}
	
	public double getExtForceX () { return extForce.x; }   // for inspector 
   public double getExtForceY () { return extForce.y; }   // for inspector
 
   public int getFormCount() { return formCount; }
   
   public int getGKeratinoHeightGranu() {	return gKeratinoHeightGranu;}
   public int getGKeratinoWidthGranu() { return gKeratinoWidthGranu;	}
   
   public int getHasGivenIons() { return hasGivenIons; }
   
   public int getIdentity() { return identity; }   // for inspector
   
   
  
   public int getKeratinoHeight() {	return keratinoHeight; }
	
	public int getKeratinoWidth() {return keratinoWidth;}
	
	
	public double getLastDrawInfoX() { return lastDrawInfoX;	}
	public double getLastDrawInfoY() { return lastDrawInfoY; }

	public long getLocal_maxAge() {return local_maxAge;}
	
	public String getName() { return NAME; }
	public double[] getNeighborDrawInfoX() { return neighborDrawInfoX; }
	public double[] getNeighborDrawInfoY() { return neighborDrawInfoY; }
	
	public int getOwnColor() {	return ownColor; }

	
	public int getSpinosumCounter(){ return spinosum_counter;}
	
	public GrahamPoint[] getVoronoihull() { return voronoihull;	}
	public int getVoronoihullvertexes() { return voronoihullvertexes; }
   
	
	
	public boolean isBasalStatisticsCell() { return isBasalStatisticsCell; }   // for inspector 
	public boolean isBirthWish() { return birthWish; }   // for inspector
	public boolean isMembraneCell() { return isMembraneCell; }   // for inspector
	public boolean isInNirvana() { return inNirvana; }
	public boolean isLastDrawInfoAssigned() {	return lastDrawInfoAssigned; }
	public boolean isOuterCell() { return isOuterCell; }   // for inspector
        
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
// SETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public void setEpidermis(Epidermis epidermis) { this.epidermis = epidermis; }
	
	public void setFormCount(int formCount) {	this.formCount = formCount; }
	
	public void setGKeratinoHeightGranu(int keratinoHeightGranu) {	gKeratinoHeightGranu = keratinoHeightGranu; }
	public void setGKeratinoWidthGranu(int keratinoWidthGranu) { gKeratinoWidthGranu = keratinoWidthGranu; }
	
	public void setHasGivenIons(int hasGivenIons) {	this.hasGivenIons = hasGivenIons; }
	
	public void setInNirvana(boolean inNirvana) { this.inNirvana = inNirvana; }
	
	
	public void setKeratinoHeight(int keratinoHeight) { this.keratinoHeight = keratinoHeight;	}
	
	public void setKeratinoWidth(int keratinoWidth) { this.keratinoWidth = keratinoWidth; }
	
	public void setLastDrawInfoAssigned(boolean lastDrawInfoAssigned) { this.lastDrawInfoAssigned = lastDrawInfoAssigned; }
	public void setLastDrawInfoX(double lastDrawInfoX) { this.lastDrawInfoX = lastDrawInfoX; }
	public void setLastDrawInfoY(double lastDrawInfoY) { this.lastDrawInfoY = lastDrawInfoY; }
	public void setLocal_maxAge(long local_maxAge) { this.local_maxAge = local_maxAge; }
	
	public void setMembraneCell(boolean isMembraneCell) {	this.isMembraneCell = isMembraneCell; }
	public void setModelController(ModelController modelController) { this.modelController =modelController;	}
	
	public void setNeighborDrawInfoX(double[] neighborDrawInfoX) { this.neighborDrawInfoX = neighborDrawInfoX; }
   public void setNeighborDrawInfoY(double[] neighborDrawInfoY) { this.neighborDrawInfoY = neighborDrawInfoY; }
	
	public void setOuterCell(boolean isOuterCell) {	this.isOuterCell = isOuterCell;}
	public void setOwnColor(int ownColor) { this.ownColor = ownColor; }
	
	
	public void setStoppable(Stoppable stopperparam)   { this.stoppable = stopperparam;}
        
	public void setVoronoihull(GrahamPoint[] voronoihull) { this.voronoihull = voronoihull; }
	public void setVoronoihullvertexes(int voronoihullvertexes) { this.voronoihullvertexes = voronoihullvertexes; }

	public EpisimCellDiffModel getEpisimCellDiffModelObject(){
		return this.cellDiffModelObjekt;
	}
	
		           
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
}












