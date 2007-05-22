package sim.app.episim1;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import ec.util.*;
import java.text.DecimalFormat;
import org.jfree.data.xy.XYSeries;
import sim.portrayal.*;

public class KCyteClass implements Steppable, Stoppable, sim.portrayal.Oriented2D, java.io.Serializable
    {
       
	
	
   
    
	private static final long serialVersionUID = 5212944079288103141L;

		private transient BioChemicalModelController modelController;
        
        final public int gOptimalKeratinoDistance=4; // Default: 4
        final public int gOptimalKeratinoDistanceGranu=4; // Default: 3
        final public int gInitialKeratinoHeight=5; // Default: 5
        final public int gInitialKeratinoWidth=5; // Default: 5
        public int gKeratinoWidthGranu=9; // defauolt: 10
        public int gKeratinoHeightGranu=4;
                
        public Double2D lastd = new Double2D(0,0);
        public boolean holePassed= false;
        public EpidermisClass theEpidermis;    
        public int KeratinoType;    
        public int getKeratinoType() { return KeratinoType; }// for inspector 
        public void setKeratinoType(int type){ KeratinoType = type;}
        public int KeratinoAge;
        public int getKeratinoAge() { return KeratinoAge; } // for inspector
        public void inkrementKeratinoAge() {  KeratinoAge += 1; } // for inspector 
        
        public boolean newborn;
        public double LastDrawInfoX;
        public double LastDrawInfoY;
        public boolean LastDrawInfoAssigned=false;
        public double NeighborDrawInfoX[]=new double[50];
        public double NeighborDrawInfoY[]=new double[50];  
        public int VoronoiStable=0; // count if a voroni is displayable, only display when at least several times stable, so avoid permanent switching back to standardform
        GrahamPoint voronoihull[]=new GrahamPoint[50];
        public int voronoihullvertexes=0;
        
        public int formCount=0; // Number of neighbors

        public int KeratinoWidth=-11; // breite keratino
        public int KeratinoHeight=-1; // höhe keratino
        
        public int ownColor=0;
        public int motherIdentity=-1;   // -1 means not filled yet
        public boolean inNirvana=false; // unvisible and without action: only ageing is active
        
        public int spinosum_counter=0;
        
        public int getSpinosumCounter(){ return spinosum_counter;}
        
        public void incrementSpinosumCounter(){ spinosum_counter +=1;}
        
        public void dekrementSpinosumCounter(){ spinosum_counter -=1;}
        
        //public boolean dead = false;
        Vector2D extForce = new Vector2D(0,0);
        public double getExtForceX () { return extForce.x; }   // for inspector 
        public double getExtForceY () { return extForce.y; }   // for inspector 
        
        public int identity=0;
        public int getIdentity() { return identity; }   // for inspector 
        
        public long local_maxAge;
        
        public boolean birthWish=false;
        public boolean getBirthWish() { return birthWish; }   // for inspector 
        public int hasGivenIons=0;

        // ENV
        public double ownSigExternalCalcium=0;
        public double getExternalCalcium() { return ownSigExternalCalcium; }   // for inspector 
        public double ownSigInternalCalcium=0;        
        public double getInternalCalcium() { return ownSigInternalCalcium; }   // for inspector 
        public double ownSigLipids=0;
        public double getLipids() { return ownSigLipids; }   // for inspector 
        public double ownSigLamella=0;
        public double getLamella() { return ownSigLamella; }   // for inspector 
        
        public boolean isOuterCell=false;
        public boolean getOutCell() { return isOuterCell; }   // for inspector 
        
        public boolean isBasalStatisticsCell=false; // for counting of growth fraction a wider range is necessary, not only membrane sitting cells
        public boolean isBasalStatisticsCell() { return isBasalStatisticsCell; }   // for inspector 
        
        public boolean isMembraneCell=false;    // cells directly sitting on membrane, very strict
        public boolean isMembraneCell() { return isMembraneCell; }   // for inspector        
        
        //public double getOrientation() { return orientation2D(); }

        //public boolean isDead() { return dead; }
        //public void setDead(boolean val) { dead = val; }
        
        

    public KCyteClass(EpidermisClass pFlock)
    {
   	 modelController = BioChemicalModelController.getInstance(); 
   	 // always as first thing, set beholder
        theEpidermis=pFlock;
        // now local vars
        theEpidermis.allocatedKCytes++;
        identity=theEpidermis.allocatedKCytes;        
        newborn();
        newborn=false;                    
        lastd=new Double2D(0.0,-3);
        // Memory Management
        if (theEpidermis.allocatedKCytes>theEpidermis.getAllCells().size()-2) // for safety -2
            theEpidermis.getAllCells().resize(theEpidermis.getAllCells().size()+500); // alloc 500 in advance
        theEpidermis.getAllCells().add(this); // register this as additional one in Bag
        //System.out.println("New Cell Nr."+theEpidermis.allocatedKCytes);
        
        
       
    }

    // reset information of this keratinocyte
    public void newborn()
    {
        holePassed=false;
        extForce=new Vector2D(0,0);
        inNirvana=false;        
        KeratinoWidth=gInitialKeratinoWidth; //theEpidermis.InitialKeratinoSize;
        KeratinoHeight=gInitialKeratinoHeight; //theEpidermis.InitialKeratinoSize; 
        KeratinoAge=0;
        KeratinoType=modelController.getGlobalIntConstant("KTYPE_UNASSIGNED");
        ownSigExternalCalcium=0;
        ownSigInternalCalcium=0;
        ownSigLipids=0;
        ownSigLamella=0;
        isOuterCell=false;        
        newborn=true;        
        voronoihullvertexes=0;
        VoronoiStable=0;
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
        double yleft=BasementMembrane.lowerBound(pC2dHerd.stx(x-5));
        double yright=BasementMembrane.lowerBound(pC2dHerd.stx(x+5));
        return new Double2D(-(yright-yleft),10);
    }   
 
    /*
    public Double2D newborn_ypos(double x, EpidermisClass flock) // return the y-position at position x
    {        
        double y=theEpidermis.lowerBound(x)+theEpidermis.birthOffset;// -flock.random.nextDouble()*5;     
	// y=20; // Oben geboren, wozu ??
        return new Double2D(x,y); 
    }
*/
    public void newbornRandomAge()
    {
        newborn();
        KeratinoAge=theEpidermis.random.nextInt(modelController.getIntField("maxCellAge_t"));
    }

    public void nirvanaAgeing(EpidermisClass flock)
    {
        ++KeratinoAge;
        if (KeratinoAge>=modelController.getIntField("maxCellAge_t")) newborn(); //{KeratinoType=0;} // stratum corneum
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
  
    public class hitResultClass
    {        
        int numhits;    // number of hits
        int otherId; // when only one hit, then how id of this hit (usually this will be the mother)
        int otherMotherId; // mother of other
        Vector2D adhForce;
        Vector2D otherMomentum;
        boolean nextToOuterCell;
                
        hitResultClass()
        {
            nextToOuterCell=false;
            numhits=0;
            otherId=0;
            otherMotherId=0;
            adhForce=new Vector2D(0,0);
            otherMomentum=new Vector2D(0,0);
        }
    }
        
    public hitResultClass hitsOther(Bag b, Continuous2D pC2dHerd, Double2D thisloc, boolean pressothers, double pBarrierMemberDist)
        {
            // check of actual position involves a collision, if so return TRUE, otherwise return FALSE
            // for each collision calc a pressure vector and add it to the other's existing one
            hitResultClass hitResult=new hitResultClass();            
            if (b==null || b.numObjs == 0 || this.inNirvana) return hitResult;
            
            double extSigCalcium=0; // first calculate calcium of below cells
            double extSigLamella=0;
            double extSigLipids=0;
            
            double x = 0; 
            double y = 0;            
            int i=0;
            double adxOpt = gOptimalKeratinoDistance; //KeratinoWidth-2+theEpidermis.cellSpace;                         was 4 originally then 5
            //double adxOpt = KeratinoWidth; //KeratinoWidth-2+theEpidermis.cellSpace;                        
            //double adyOpt = 5; // 3+theEpidermis.cellSpace;
            
            
            if (KeratinoType==modelController.getGlobalIntConstant("KTYPE_GRANULOSUM")) adxOpt=gOptimalKeratinoDistanceGranu; // was 3 // 4 in modified version
            
            double optDistSq = adxOpt*adxOpt; //+adyOpt*adyOpt;
            double optDist=Math.sqrt(optDistSq);
            //double outerCircleSq = (neigh_p*adxOpt)*(neigh_p*adxOpt)+(neigh_p*adyOpt)*(neigh_p*adyOpt);
            int neighbors=0;
            formCount=0;

            for(i=0;i<b.numObjs;i++)
                {
                    if (!(b.objs[i] instanceof KCyteClass))
                        continue;
            
                KCyteClass other = (KCyteClass)(b.objs[i]);
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
                                                    if (other.LastDrawInfoAssigned==true)
                                                        NeighborDrawInfoX[formCount]=other.LastDrawInfoX;
                                                        NeighborDrawInfoY[formCount]=other.LastDrawInfoY;
                                                    ++formCount;
                                                }
                                            }
                        
                        
                        if (optDist-actdist>theEpidermis.minDist) // ist die kollision signifikant ?
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
                            double adhfac=modelController.get2DDoubleArrayValue("adh_array", KeratinoType, other.KeratinoType);                           
                            if (actdist-optDist<modelController.getDoubleField("adhesionDist"))
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
                                            
                          Double2D m = ((KCyteClass)b.objs[i]).momentum();
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
        double maxy=BasementMembrane.lowerBound(p_potentialLoc.x);  
        
       
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
        Double2D thisloc = pC2dHerd.getObjectLocation(this); // update Global variable
        
        newx=xPos;
        
        
        newy=yPos;
        double maxy=BasementMembrane.lowerBound(newx);        
                
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
    
    public KCyteClass makeChild(Continuous2D pC2dHerd)
    {       
        // Either we get use a currently unused cell oder we allocate a new one
        KCyteClass kcyte;        
        if (theEpidermis.nirvanaHeapLoaded)
        {
            kcyte = theEpidermis.nirvanaHeap;
            theEpidermis.nirvanaHeapLoaded=false;
        }
        else
        {
            kcyte= new KCyteClass(theEpidermis); 
            theEpidermis.schedule.scheduleRepeating(kcyte);   // schedule only if not already running
        }

        Double2D newloc=pC2dHerd.getObjectLocation(this);
        newloc=new Double2D(newloc.x +theEpidermis.random.nextDouble()*0.5-0.25, newloc.y-theEpidermis.random.nextDouble()*0.5-0.1);
        //newloc=new Double2D(newloc.x - theEpidermis.random.nextInt(4)+2, newloc.y - theEpidermis.random.nextInt(2));
        //newloc=new Double2D(newloc.x +0.1, newloc.y-0.1);
        kcyte.motherIdentity=this.identity;
        kcyte.ownColor=this.theEpidermis.random.nextInt(200);
        kcyte.theEpidermis = this.theEpidermis;        // the herd
        kcyte.newborn();     
        kcyte.local_maxAge=modelController.getIntField("maxCellAge_t");
        long pSimTime=(long) theEpidermis.schedule.time();
        if (pSimTime<(kcyte.local_maxAge)) kcyte.local_maxAge=pSimTime;

        
        pC2dHerd.setObjectLocation(kcyte, newloc);        
        //System.out.println("New Cell:"+flocker.identity);
        return kcyte;
    }

    public void makeTACell(Continuous2D pC2dHerd)
    {
        theEpidermis.actualTA++;
        theEpidermis.actualKCytes++;
        KCyteClass TACell=makeChild(pC2dHerd);
        TACell.KeratinoType=modelController.getGlobalIntConstant("KTYPE_TA");        
        TACell.KeratinoAge=this.theEpidermis.random.nextInt(modelController.getIntField("tACycle_t"));  // somewhere on the TA Cycle
        // erben der signal concentrationen
        TACell.ownSigLipids=0;
        TACell.ownSigLamella=0;
        TACell.ownSigInternalCalcium=ownSigInternalCalcium/2; // ownSigInternalCalcium;
        ownSigInternalCalcium=ownSigInternalCalcium/2; // ownSigInternalCalcium;
        TACell.ownSigExternalCalcium=ownSigExternalCalcium/2; // ownSigExternalCalcium;
        ownSigExternalCalcium=ownSigExternalCalcium/2; // ownSigExternalCalcium;
    }

    public void makeSpiCell(Continuous2D pC2dHerd, long pSimTime)
    {
        theEpidermis.actualSpi++;
        theEpidermis.actualKCytes++;
        KCyteClass TACell=makeChild(pC2dHerd);
        TACell.ownSigInternalCalcium=this.ownSigInternalCalcium/2;
        ownSigInternalCalcium=ownSigInternalCalcium/2; // ownSigInternalCalcium;
        TACell.ownSigExternalCalcium=this.ownSigExternalCalcium/2;
        ownSigExternalCalcium=ownSigExternalCalcium/2; // ownSigExternalCalcium;
        TACell.ownSigLamella=0;
        TACell.ownSigLipids=0; 
        TACell.KeratinoType=modelController.getGlobalIntConstant("KTYPE_SPINOSUM");        
        TACell.KeratinoAge=0; // this.theEpidermis.random.nextInt(local_maxAge);  // somewhere on the TA Cycle
        // erben der signal concentrationen

    }

        
    public void Diffuser (Bag b, Continuous2D pC2dHerd, Double2D thisloc, boolean pBarrierMember)
    {
        if (b==null || b.numObjs == 0 || this.inNirvana) return;
        int i=0;
       
        int normalize=1; // b.numObjs;
        
        for(i=0;i<b.numObjs;i++)
            {
            KCyteClass other = (KCyteClass)(b.objs[i]);
            if (other != this )
                {
                    Double2D otherloc=pC2dHerd.getObjectLocation(other);
                    double dx = pC2dHerd.tdx(thisloc.x,otherloc.x); // dx, dy is what we add to other to get to this
                    double dy = pC2dHerd.tdy(thisloc.y,otherloc.y);

                    if (other.hasGivenIons==0)
                    {    
                        other.hasGivenIons++;    // only one time per simulation tick
                        
                        //
                        // undirected calcium diffusion
                        //

                        if ((ownSigExternalCalcium+ownSigInternalCalcium)<modelController.getDoubleField("calSaturation"))
                        {
                            ownSigExternalCalcium+=other.ownSigExternalCalcium*modelController.getDoubleField("epidermalDiffusion")/normalize; // collect signals
                            other.ownSigExternalCalcium=(1-modelController.getDoubleField("epidermalDiffusion")/normalize)*other.ownSigExternalCalcium;
                        }
                        
                        //
                        // directed trans-epidermal water flow                          
                        //
                    
                        if (dy<0) // oder > ?? auf jeden Fall, wenn unterhalb, da transcutaneous water flux drives particles up
                        {
                                
                                if ((ownSigExternalCalcium+ownSigInternalCalcium)<modelController.getDoubleField("calSaturation"))
                                {
                                    ownSigExternalCalcium+=other.ownSigExternalCalcium*modelController.getDoubleField("epidermalWaterflux")/normalize; // collect signals
                                    other.ownSigExternalCalcium=(1-modelController.getDoubleField("epidermalWaterflux")/normalize)*other.ownSigExternalCalcium;                                
                                }                              
                            
                                if (ownSigLamella<modelController.getDoubleField("lamellaSaturation"))
                                {
                                    ownSigLamella+=other.ownSigLamella*modelController.getDoubleField("epidermalWaterflux")/normalize;
                                    other.ownSigLamella=(1-modelController.getDoubleField("epidermalWaterflux")/normalize)*other.ownSigLamella;
                                }
                         }
                    }                    
                }
        }
        
        //
        //  Secretion of Calcium
        //

        if ((isMembraneCell) && ((ownSigExternalCalcium+ownSigInternalCalcium)<modelController.getDoubleField("calSaturation")))    // calcium enters model at the basal membrane        
        {                
                ownSigExternalCalcium+=modelController.getDoubleField("calBasalEntry_per_t");
        }
                
        //
        //  Uptake meaning Internalization of Calcium
        //        
        ownSigInternalCalcium=ownSigExternalCalcium*0.01;
       
        if ( (KeratinoType==modelController.getGlobalIntConstant("KTYPE_GRANULOSUM")) || (KeratinoType==modelController.getGlobalIntConstant("KTYPE_SPINOSUM")) 
      		  || (KeratinoType==modelController.getGlobalIntConstant("KTYPE_LATESPINOSUM")))
        {
            //
            // Secretion of Lamella
            //        
            if (ownSigLamella <modelController.getDoubleField("lamellaSaturation"))
                ownSigLamella+=modelController.getDoubleField("lamellaSecretion");
        
            //
            // Barrier function
            //
            // zum testen folgende zeile rausgenommen 29.7.05
            if (pBarrierMember)// flag is set by EpidermisClass airSurface Agent
            {
                    //
                    // Conversion of Lamella to Lipids
                    //
                    if (ownSigLipids<modelController.getDoubleField("lipSaturation"))
                    {
                        ownSigLipids+=modelController.getDoubleField("barrierLamellaUse_frac")*ownSigLamella;
                        ownSigLamella=(1-modelController.getDoubleField("barrierLamellaUse_frac"))*ownSigLamella;
                    }

                    //
                    // Is Barrier established or not ?
                    //
                    if (ownSigLipids<modelController.getDoubleField("minSigLipidsBarrier"))
                    {
                        ownSigExternalCalcium=ownSigExternalCalcium*(1-modelController.getDoubleField("epidermalWaterflux"));    // without barrier water with ions flows out
                        // ownSigLipids=ownSigLipids*(1-theEpidermis.waterflux);    // without barrier water with particles flows out
                    }                
                    else // Barrier reduces the waterflow to 1% e.g.s
                    {
                        ownSigExternalCalcium=ownSigExternalCalcium*(1-modelController.getDoubleField("epidermalWaterflux")*modelController.getDoubleField("barrierLossReduction_frac"));
                        //ownSigLipids=ownSigLipids*(1-theEpidermis.waterflux*theEpidermis.gBarrierResistance);
                    }
            }
        }
        
    }
    

    
    public void Differentiate(boolean pBarrierMember)
    {
      modelController.differentiate(this, theEpidermis, pBarrierMember);
 
        
    

        if ((KeratinoType==modelController.getGlobalIntConstant("KTYPE_NONUCLEUS"))) // && (isOuterCell))
        {
            killCell();
        }
  
   }

    
    public void killCell(){
   	 theEpidermis.nirvanaHeapLoaded=true;    // register in the Nirvana
			// Heap for resurrection
   	 theEpidermis.nirvanaHeap=this;
   	 theEpidermis.actualNoNucleus--;
   	 KeratinoType=modelController.getGlobalIntConstant("KTYPE_NIRVANA");
   	 theEpidermis.actualKCytes--;
   	 inNirvana=true;            
   	 Double2D newloc= new Double2D(0,0);
   	 theEpidermis.continous2D.setObjectLocation(this, newloc);
    }

    void cellcycle(boolean pNoCollision)
    {
        // ///////////////////////////////////////////////////
        // Cell Cycle
        /////////////////////////////////////////////////////
        
        // stem and TA cells divide
        // make child
        
        double ageFrac=(double)KeratinoAge / (double)modelController.getIntField("maxCellAge_t");
        if (KeratinoType==modelController.getGlobalIntConstant("KTYPE_STEM") || KeratinoType==modelController.getGlobalIntConstant("KTYPE_TA"))
        {           
                if (((KeratinoAge%modelController.getIntField("stemCycle_t"))==0) && (KeratinoType==modelController.getGlobalIntConstant("KTYPE_STEM")))                    
                    birthWish=true;
                if (((KeratinoAge%modelController.getIntField("tACycle_t"))==0) && (KeratinoType==modelController.getGlobalIntConstant("KTYPE_TA")))
                    birthWish=true;
               
                if (birthWish && (pNoCollision))    // numhits==0 means no overlap with any adjacent cell, not even a child
                {
                    if (KeratinoType==modelController.getGlobalIntConstant("KTYPE_STEM"))
                    {
                        makeTACell(theEpidermis.continous2D);                        
                        KeratinoAge=0; // begin new cycle, only stem cells do not age, TA cells do !
                    }
                    if ((KeratinoType==modelController.getGlobalIntConstant("KTYPE_TA")) && (ageFrac<modelController.getDoubleField("tAMaxBirthAge_frac")))
                    {
                        makeSpiCell(theEpidermis.continous2D, (long) theEpidermis.schedule.time());
                    }
                    birthWish=false;
                }

        }
    }
    


    //relocate basal and stem_cells bei veraenderter funktion fehlt noch.
    
    public void step(SimState state)
    {        
        final EpidermisClass epiderm = (EpidermisClass)state;

        //
        // Memory management
        //
        if (inNirvana)
        {
            // please for resurrection by registering as wating
            theEpidermis.nirvanaHeapLoaded=true;
            theEpidermis.nirvanaHeap=this;            
            return; 
        }
        
        hasGivenIons=0;

        //////////////////////////////////////////////////
        // calculate ACTION force
        //////////////////////////////////////////////////
        int ministep=1; 
        int maxmini=1;

        // Double2D rand = randomness(flock.random);
        //Double2D mome = momentum();

        // calc potential location from gravitation and external pressures
        Double2D oldLoc=epiderm.continous2D.getObjectLocation(this);       
        
        if (extForce.length()>0.6)
         extForce=extForce.setLength(0.6);
        //extForce=extForce.setLength(2*(1-Math.exp(-0.5*extForce.length())));
        //extForce=extForce.setLength(sigmoid(extForce.length())-sigmoid(0)); // die funktion muss 0 bei 0 liefern, daher absenkung auf sigmoid(0)
        Double2D gravi=new Double2D(0,modelController.getDoubleField("gravitation")); // Vector which avoidance has to process
        Double2D randi=new Double2D(modelController.getDoubleField("randomness")*(theEpidermis.random.nextDouble()-0.5), modelController.getDoubleField("randomness")*(theEpidermis.random.nextDouble()-0.5));
        Vector2D actionForce=new Vector2D(gravi.x+extForce.x*modelController.getDoubleField("externalPush")+randi.x, gravi.y+extForce.y*modelController.getDoubleField("externalPush"));
        Double2D potentialLoc=new Double2D (epiderm.continous2D.stx(actionForce.x+oldLoc.x),epiderm.continous2D.sty(actionForce.y+oldLoc.y));
        extForce.x=0;   // alles einberechnet
        extForce.y=0;

      

        //////////////////////////////////////////////////
        // try ACTION force
        //////////////////////////////////////////////////
        Bag b = epiderm.continous2D.getObjectsWithinDistance(potentialLoc, modelController.getDoubleField("neighborhood_µm"), false); //theEpidermis.neighborhood
        hitResultClass hitResult1;
        hitResult1 = hitsOther(b, epiderm.continous2D, potentialLoc, true, theEpidermis.NextToOuterCell);

        //////////////////////////////////////////////////
        // estimate optimised POS from REACTION force
        //////////////////////////////////////////////////
        // optimise my own position by giving way to the calculated pressures
        Vector2D reactionForce=extForce;
        reactionForce=reactionForce.add(hitResult1.otherMomentum.amplify(theEpidermis.consistency));
        reactionForce=reactionForce.add(hitResult1.adhForce.amplify(modelController.getDoubleField("cohesion")));

        // restrict movement if direction changes to quickly (momentum of a cell movement)
       
                 

        // bound optimised force
        // problem: each cell bounces back two times as much as it should (to be able to move around immobile objects)
        // but as we don't want bouncing the reaction force must never exceed the action force in its length 
        // dx/dy contain move to make
        if (reactionForce.length()>(actionForce.length()+0.1))
            reactionForce=reactionForce.setLength(actionForce.length()+0.1);

        extForce.x=0;
        extForce.y=0;

        // bound also by borders
        double potX=oldLoc.x+actionForce.x+reactionForce.x;
        double potY=oldLoc.y+actionForce.y+reactionForce.y;                               
        potentialLoc=new Double2D(epiderm.continous2D.stx(potX), epiderm.continous2D.sty(potY));                                
        potentialLoc=calcBoundedPos(epiderm.continous2D, potentialLoc.x, potentialLoc.y);

        //////////////////////////////////////////////////
        // try optimised POS 
        //////////////////////////////////////////////////              
        // check whether there is anything in the way at the new position

        // aufgrund der gnaedigen Kollisionspruefung, die bei Aktueller Zelle (2) und Vorgaenger(1) keine Kollision meldet, 
        // ueberlappen beide ein wenig, falls es eng wird. Wird dann die (2) selber nachgefolgt von (3), so wird (2) rausgeschoben, aber (3) nicht
        // damit ueberlappen 3 und 1 und es kommt zum Stillstand.

        b = epiderm.continous2D.getObjectsWithinDistance(potentialLoc, modelController.getDoubleField("neighborhood_µm"), false); //theEpidermis.neighborhood
        hitResultClass hitResult2;
        hitResult2 = hitsOther(b, epiderm.continous2D, potentialLoc, true, theEpidermis.NextToOuterCell);

        // move only on pressure when not stem cell
        if (KeratinoType!=modelController.getGlobalIntConstant("KTYPE_STEM"))
        { 
            if ((hitResult2.numhits==0) || 
                ((hitResult2.numhits==1) && ((hitResult2.otherId==this.motherIdentity) || (hitResult2.otherMotherId==this.identity))))
            {
                double dx=potentialLoc.x-oldLoc.x;
                lastd = new Double2D(potentialLoc.x-oldLoc.x,potentialLoc.y-oldLoc.y);
                setPositionRespectingBounds(epiderm.continous2D, potentialLoc);
            }
        }
        
        Double2D newLoc=epiderm.continous2D.getObjectLocation(this);
        double maxy=BasementMembrane.lowerBound(newLoc.x);  
        if ((maxy-newLoc.y)<modelController.getDoubleField("basalLayerWidth")) 
            isBasalStatisticsCell=true; 
        else 
            isBasalStatisticsCell=false; // ABSOLUTE DISTANZ KONSTANTE

        if ((maxy-newLoc.y)<modelController.getDoubleField("membraneCellsWidth")) 
            isMembraneCell=true; 
        else 
            isMembraneCell=false; // ABSOLUTE DISTANZ KONSTANTE
        
        //////////////////////////////////////////////////
        // Proliferation
        //////////////////////////////////////////////////
        
        cellcycle(hitResult2.numhits==0);
        
        //////////////////////////////////////////////////
        // Diffusion of signals
        //////////////////////////////////////////////////  
        
        Diffuser(b, epiderm.continous2D, newLoc, (isOuterCell || hitResult2.nextToOuterCell));
        
        //////////////////////////////////////////////////
        // Differentiation: Environment and Internal processing
        //////////////////////////////////////////////////  
      
        Differentiate(isOuterCell || hitResult2.nextToOuterCell);
        
        }

        private Stoppable stopper = null;
        public void setStopper(Stoppable stopperparam)   {this.stopper = stopperparam;}
        public void stop(){stopper.stop();}
		
		public int getGKeratinoHeightGranu() {
		
			return gKeratinoHeightGranu;
		}
		
		public void setGKeratinoHeightGranu(int keratinoHeightGranu) {
		
			gKeratinoHeightGranu = keratinoHeightGranu;
		}
		
		public int getGKeratinoWidthGranu() {
		
			return gKeratinoWidthGranu;
		}
		
		public void setGKeratinoWidthGranu(int keratinoWidthGranu) {
		
			gKeratinoWidthGranu = keratinoWidthGranu;
		}
		
		public int getKeratinoHeight() {
		
			return KeratinoHeight;
		}
		
		public void setKeratinoHeight(int keratinoHeight) {
		
			KeratinoHeight = keratinoHeight;
		}
		
		public int getKeratinoWidth() {
		
			return KeratinoWidth;
		}
		
		public void setKeratinoWidth(int keratinoWidth) {
		
			KeratinoWidth = keratinoWidth;
		}
		
		public long getLocal_maxAge() {
		
			return local_maxAge;
		}
		
		public void setLocal_maxAge(long local_maxAge) {
		
			this.local_maxAge = local_maxAge;
		}
		
		public void setModelController(BioChemicalModelController modelController) {
		
			this.modelController = modelController;
		}           
    }
