package sim.app.episim;
import sim.app.episim.biomechanics.Calculators;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.datamonitoring.charts.ChartController;

import sim.app.episim.model.CellBehavioralModelController;
import sim.app.episim.model.BioMechanicalModelController;
import sim.app.episim.model.MiscalleneousGlobalParameters;
import sim.app.episim.model.ModelController;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.CellEllipse;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import ec.util.*;
import episimbiomechanics.EpisimModelIntegrator;
import episiminterfaces.CellDeathListener;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellBehavioralModelGlobalParameters;
import sim.app.episim.util.*;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jfree.data.xy.XYSeries;
import sim.portrayal.*;

public class KCyte extends CellType
{
//	-----------------------------------------------------------------------------------------------------------------------------------------   
// CONSTANTS
//	-----------------------------------------------------------------------------------------------------------------------------------------          
	
	private static final long serialVersionUID = 5212944079288103141L;
   
   private final String NAME = "Keratinocyte";
   
   public static final double GOPTIMALKERATINODISTANCE=4; // Default: 4
     public static final double GOPTIMALKERATINODISTANCEGRANU=4; // Default: 3
   //The width of the keratinocyte must be bigger or equals the hight
   public static final int GINITIALKERATINOHEIGHT=5; // Default: 5
   public static final int GINITIALKERATINOWIDTH=5; // Default: 5
   
   public final int NEXTTOOUTERCELL=7;
   private double MINDIST=0.1;   
   private static final double CONSISTENCY=0.0;

//	-----------------------------------------------------------------------------------------------------------------------------------------   
// VARIABLES
//	-----------------------------------------------------------------------------------------------------------------------------------------          
   
   
   private int gKeratinoWidthGranu=9; // default: 10
   private int gKeratinoHeightGranu=4;
                
   private Double2D lastd = new Double2D(0,0);
   

   
  

   
   private int keratinoWidth=-11; // breite keratino
   private int keratinoHeight=-1; // höhe keratino
   
   private int ownColor=0;
   
   // public boolean dead = false;
   private Vector2D extForce = new Vector2D(0,0);
   
   private long local_maxAge;
   
   private boolean birthWish=false;
   
   private int hasGivenIons=0;

  
   private List<CellDeathListener> cellDeathListeners;
  
   
   private Stoppable stoppable = null;
   
   ///////////////////////////////////////////////////////////
   // THE CELL DIFFERENTIATION MODEL
   ///////////////////////////////////////////////////////////
   
   private EpisimCellBehavioralModel cellBehavioralModelObjekt;
   
   private SimState actSimState;
   
   private static Set<String> methodsNamesBlockedForParameterInspector;
   
   {
   	methodsNamesBlockedForParameterInspector = new HashSet<String>();
   	methodsNamesBlockedForParameterInspector.add("getParameter");
   	methodsNamesBlockedForParameterInspector.add("getVoronoihullvertexes");
   }
   
   
   
//-----------------------------------------------------------------------------------------------------------------------------------------   
  
//-----------------------------------------------------------------------------------------------------------------------------------------   
         
   public KCyte(){
   this(-1, -1,  null);
   }
    public KCyte(long id, long motherId, EpisimCellBehavioralModel cellBehavioralModel)
    {   	 
   	 
   	 super(id, motherId);
   	        
    	 this.cellBehavioralModelObjekt = cellBehavioralModel;
    	 if(cellBehavioralModel == null) this.cellBehavioralModelObjekt = ModelController.getInstance().getCellBehavioralModelController().getNewEpisimCellBehavioralModelObject();
    	 else cellBehavioralModel.setEpisimModelIntegrator((EpisimModelIntegrator)ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModel());
    	 extForce=new Vector2D(0,0);
       cellDeathListeners = new LinkedList<CellDeathListener>();
       
       cellDeathListeners.add(TissueServer.getInstance().getActEpidermalTissue());
       
       cellDeathListeners.add(GlobalStatistics.getInstance());
       
       keratinoWidth=GINITIALKERATINOWIDTH; //theEpidermis.InitialKeratinoSize;
       keratinoHeight=GINITIALKERATINOHEIGHT; //theEpidermis.InitialKeratinoSize; 
         
               
       CellEllipse ellipse = new CellEllipse(this.getID(), ((int)this.cellBehavioralModelObjekt.getX()), ((int)this.cellBehavioralModelObjekt.getY()), keratinoWidth, keratinoHeight, Color.BLUE);
       this.setCellEllipseObject(ellipse);
                          
       lastd=new Double2D(0.0,-3);
      
       TissueServer.getInstance().getActEpidermalTissue().checkMemory();
       TissueServer.getInstance().getActEpidermalTissue().getAllCells().add(this); // register this as additional one in Bag
       
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
    public void removeCellDeathListener(){
   	 this.cellDeathListeners.clear();
    }    
    public void addCellDeathListener(CellDeathListener listener){
   	 this.cellDeathListeners.add(listener);
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
        if (b==null || b.numObjs == 0 || this.isInNirvana()) return hitResult;
        
        
        
               
        int i=0;
        double adxOpt = GOPTIMALKERATINODISTANCE; //KeratinoWidth-2+theEpidermis.cellSpace;                         was 4 originally then 5
        //double adxOpt = KeratinoWidth; //KeratinoWidth-2+theEpidermis.cellSpace;                        
        //double adyOpt = 5; // 3+theEpidermis.cellSpace;
        
        
        if (this.cellBehavioralModelObjekt.getDifferentiation()==EpisimCellBehavioralModelGlobalParameters.GRANUCELL) adxOpt=GOPTIMALKERATINODISTANCEGRANU; // was 3 // 4 in modified version
        
        double optDistSq = adxOpt*adxOpt; //+adyOpt*adyOpt;
        double optDist=Math.sqrt(optDistSq);
        //double outerCircleSq = (neigh_p*adxOpt)*(neigh_p*adxOpt)+(neigh_p*adyOpt)*(neigh_p*adyOpt);
        int neighbors=0;
       

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
                                        
                                        if ((other.getMotherID()==getID()) || (other.getID()==getMotherID()))
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
                        double adhfac=ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().gibAdh_array(this.cellBehavioralModelObjekt.getDifferentiation(), other.getEpisimCellBehavioralModelObject().getDifferentiation());                           
                        if (actdist-optDist<ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getAdhesionDist())
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
    if (isInNirvana()) 
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
    pC2dHerd.setObjectLocation(this, newloc);
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
            setInNirvana(true);
           
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
    
    public KCyte makeChild(EpisimCellBehavioralModel cellBehavioralModel)
    {       
        
   	 Continuous2D cellContinous2D = TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D();
   	 
   	 // Either we get use a currently unused cell oder we allocate a new one
        KCyte kcyte;        
       
        kcyte= new KCyte(CellType.getNextCellId(), getID(), cellBehavioralModel); 
        cellBehavioralModel.setId((int)kcyte.getID());
           
            
        Stoppable stoppable = TissueServer.getInstance().getActEpidermalTissue().schedule.scheduleRepeating(kcyte, 1, 1);   // schedule only if not already running
        kcyte.setStoppable(stoppable);
          
        double deltaX = TissueServer.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;
        double deltaY = TissueServer.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.1; 
               
        Double2D oldLoc=cellContinous2D.getObjectLocation(this);
        
     //   double deltaDrawX = newloc.
         
        Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y-deltaY);   
        
        kcyte.ownColor= TissueServer.getInstance().getActEpidermalTissue().random.nextInt(200);
               // the herd
             
        kcyte.local_maxAge= ModelController.getInstance().getCellBehavioralModelController().getEpisimCellBehavioralModelGlobalParameters().getMaxAge();
        long pSimTime=(long) TissueServer.getInstance().getActEpidermalTissue().schedule.time();
        if (pSimTime<(kcyte.local_maxAge)){ 
      	  kcyte.local_maxAge=pSimTime;
      	  cellBehavioralModel.setMaxAge((int)kcyte.local_maxAge);
        }

        
        cellContinous2D.setObjectLocation(kcyte, newloc);
        
        DrawInfo2D info = this.getCellEllipseObject().getLastDrawInfo2D();
			DrawInfo2D newInfo = null;
			if( info != null){
				newInfo = new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width,info.draw.height), info.clip);
				newInfo.draw.x = ((newInfo.draw.x - newInfo.draw.width*oldLoc.x) + newInfo.draw.width*newloc.x);
				newInfo.draw.y = ((newInfo.draw.y - newInfo.draw.height*oldLoc.y) + newInfo.draw.height*newloc.y);
				kcyte.getCellEllipseObject().setLastDrawInfo2D(newInfo, true);
			}                
        return kcyte;
    }

    public void makeTACell(EpisimCellBehavioralModel cellBehavioralModel)
    {
        
        GlobalStatistics.getInstance().inkrementActualNumberKCytes();
        KCyte taCell=makeChild(cellBehavioralModel);
                    
        taCell.getEpisimCellBehavioralModelObject().setAge(TissueServer.getInstance().getActEpidermalTissue().random.nextInt(ModelController.getInstance().getCellBehavioralModelController().getEpisimCellBehavioralModelGlobalParameters().getCellCycleTA()));  // somewhere on the TA Cycle
       
       
    }
   
    public void makeSpiCell(EpisimCellBehavioralModel cellBehavioralModel)
    {       
   	 GlobalStatistics.getInstance().inkrementActualNumberKCytes();
       makeChild(cellBehavioralModel);
    }

    
    private KCyte[] getRealNeighbours(Bag neighbours, Continuous2D cellContinous2D, Double2D thisloc){
   	 List<KCyte> neighbourCells = new ArrayList<KCyte>();
   	 for(int i=0;i<neighbours.numObjs;i++)
       {
   		 KCyte actNeighbour = (KCyte)(neighbours.objs[i]);
     
               Double2D otherloc=cellContinous2D.getObjectLocation(actNeighbour);
               double dx = cellContinous2D.tdx(thisloc.x,otherloc.x); // dx, dy is what we add to other to get to this
               double dy = cellContinous2D.tdy(thisloc.y,otherloc.y);
               
               actNeighbour.getEpisimCellBehavioralModelObject().setDy(-1*dy);
               actNeighbour.getEpisimCellBehavioralModelObject().setDx(dx);
               
             //  double distance = Math.sqrt(dx*dx + dy*dy);
               
             //  if(distance > 0 && distance <= biomechModelController.getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm()){
               
               	neighbourCells.add(actNeighbour);
               	
             //}
        }
   	 return neighbourCells.toArray(new KCyte[neighbourCells.size()]);
    }
    
    private EpisimCellBehavioralModel[] getCellBehavioralModelArray(KCyte[] neighbours){
   	 List<EpisimCellBehavioralModel> neighbourCellsDiffModel = new ArrayList<EpisimCellBehavioralModel>();
   	 for(KCyte actNeighbour: neighbours) neighbourCellsDiffModel.add(actNeighbour.getEpisimCellBehavioralModelObject());
   	 return neighbourCellsDiffModel.toArray(new EpisimCellBehavioralModel[neighbourCellsDiffModel.size()]);
    }
   
    private boolean isSurfaceCell(EpisimCellBehavioralModel[] neighbours){
   	 if(this.cellBehavioralModelObjekt.getDifferentiation() == EpisimCellBehavioralModelGlobalParameters.STEMCELL) return false;
   	 else{
   		
   		 int leftSideNeighbours = 0;
   		 int rightSideNeighbours= 0;
   		 int upperNeighbours = 0;
   		 double height = (double) this.getKeratinoHeight();
   		 double width = (double) this.getKeratinoWidth();
   		  for(EpisimCellBehavioralModel actNeighbour :neighbours){
   			  double dx =actNeighbour.getDx();
   			  double dy =actNeighbour.getDy();
   			  if(dy <=0 && dx == 0) upperNeighbours++;
   			  else if(dy <=0 && dx < 0) leftSideNeighbours++;
   			  else if(dy <=0  && dx > 0) rightSideNeighbours++;
   		  }
   		 
   		 if(upperNeighbours == 0 || rightSideNeighbours == 0 || leftSideNeighbours == 0) return true; 
   		 
   	 }
   	 return false;
    }
    static  long actNumberSteps = 0;
    static  long deltaTime = 0;
    public void differentiate(SimState state, Bag neighbours, Continuous2D cellContinous2D, Double2D thisloc, boolean nextToOuterCell, boolean hasCollision)
    {
     	 KCyte[] realNeighbours = getRealNeighbours(neighbours, cellContinous2D, thisloc);
     	 
     	 this.setNeighbouringCells(realNeighbours);
     	 EpisimCellBehavioralModel[] realNeighboursDiffModel = getCellBehavioralModelArray(realNeighbours);
   	// setIsOuterCell(isSurfaceCell(realNeighbours));
   	 this.cellBehavioralModelObjekt.setX(thisloc.getX());
   	 this.cellBehavioralModelObjekt.setY(TissueController.getInstance().getTissueBorder().getHeight()- thisloc.getY());
   	 this.cellBehavioralModelObjekt.setIsMembrane(isMembraneCell());
   	 this.cellBehavioralModelObjekt.setIsSurface(isOuterCell() || nextToOuterCell);
   	 this.cellBehavioralModelObjekt.setHasCollision(hasCollision);
   	 if(this.cellBehavioralModelObjekt.getDifferentiation() == EpisimCellBehavioralModelGlobalParameters.STEMCELL) this.cellBehavioralModelObjekt.setAge(0);
   	 else this.cellBehavioralModelObjekt.setAge(this.cellBehavioralModelObjekt.getAge()+1);
   	 	  	 
   	
		
   	 EpisimCellBehavioralModel[] children = this.cellBehavioralModelObjekt.oneStep(realNeighboursDiffModel);
		/*	long timeAfter = System.currentTimeMillis();
	        //  	long actSteps = state.schedule.getSteps();
			long deltaTimeTmp = timeAfter-timeBefore;
		
			if(state.schedule.getSteps() > actNumberSteps){
				actNumberSteps = state.schedule.getSteps();
			    		
		    		// if(this.follow && this.KeratinoAge <=2000){   		
		   			  	
				 
				   try {
		           BufferedWriter out = new BufferedWriter(new FileWriter("d:\\performance_neu_10000.csv", true));
		        //   out.write(NumberFormat.getInstance(Locale.GERMANY).format(actSteps)+ ";");
		           out.write(NumberFormat.getInstance(Locale.GERMANY).format(deltaTime)+ ";");
		      //     out.write(NumberFormat.getInstance(Locale.GERMANY).format(allCells.size())+ ";");
		                   
		           out.write("\n");
		           out.flush();
		           out.close();
		            } catch (IOException e) {}
				   
				  
				 deltaTime = 0;
			}
			deltaTime +=deltaTimeTmp;		*/
   	
   	 makeChildren(children);
   	 if(this.cellBehavioralModelObjekt.getDifferentiation() == EpisimCellBehavioralModelGlobalParameters.GRANUCELL){
   	 	setKeratinoWidth(getGKeratinoWidthGranu());
   		setKeratinoHeight(getGKeratinoHeightGranu());
   		this.getCellEllipseObject().setMajorAxisAndMinorAxis(getGKeratinoWidthGranu(), getGKeratinoHeightGranu());
   	}
      if (!this.cellBehavioralModelObjekt.getIsAlive()) // && (isOuterCell))
      {
            killCell();
      }
  
   }
    
    private void makeChildren(EpisimCellBehavioralModel[] children){
   	 if(children!=null){
   		 for(EpisimCellBehavioralModel actChild: children){
   			 
   			 if(actChild.getDifferentiation() == EpisimCellBehavioralModelGlobalParameters.TACELL) makeTACell(actChild);
   			 else if(actChild.getDifferentiation() == EpisimCellBehavioralModelGlobalParameters.EARLYSPICELL) makeSpiCell(actChild);
   		 }
   	 }
    }

    
    public void killCell(){
   	    	 
   	 for(CellDeathListener listener: cellDeathListeners) listener.cellIsDead(this);
   	 this.cellBehavioralModelObjekt.setDifferentiation(EpisimCellBehavioralModelGlobalParameters.KTYPE_NIRVANA);
   	 
   	 setInNirvana(true);
   	 this.cellBehavioralModelObjekt.setIsAlive(false);
   	 removeFromSchedule();
   	
    }
    
    private void calculatePolygons(){
   	 
    }
    
    private void calculateClippedCell(){
   	 
    	CellEllipse cellEllipseCell = this.getCellEllipseObject();
    	 
    	 
    	 if(this.getNeighbouringCells() != null && this.getNeighbouringCells().length > 0 && cellEllipseCell.getLastDrawInfo2D()!= null){
 	   	 for(CellType neighbouringCell : this.getNeighbouringCells()){
 	   		 
 	   		 if(!CellEllipseIntersectionCalculationRegistry.getInstance().isAreadyCalculated(cellEllipseCell.getId(), neighbouringCell.getCellEllipseObject().getId(), getActSimState().schedule.getSteps())){
 	   			 CellEllipseIntersectionCalculationRegistry.getInstance().addCellEllipseIntersectionCalculation(cellEllipseCell.getId(), neighbouringCell.getCellEllipseObject().getId());
 	   			
 	   			 EllipseIntersectionCalculatorAndClipper.getClippedEllipsesAndXYPoints(cellEllipseCell, neighbouringCell.getCellEllipseObject());
 	   		 }
 	   		 
 	   	 }
    	 }
     }
    
    
    

//    static  long actNumberSteps = 0;
 // static  long deltaTime = 0;
	public void step(SimState state) {
		
		this.actSimState = state;

		final Epidermis epiderm = (Epidermis) state;
		
		
		if(isInNirvana() || !this.cellBehavioralModelObjekt.getIsAlive()){
			

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
					* (epiderm.random.nextDouble() - 0.5), ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getRandomness()
					* (epiderm.random.nextDouble() - 0.5));
			Vector2D actionForce = new Vector2D(gravi.x + extForce.x * ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getExternalPush()
					+ randi.x, gravi.y + extForce.y * ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getExternalPush());
			Double2D potentialLoc = null;
			
				potentialLoc = new Double2D(epiderm.getCellContinous2D().stx(actionForce.x + oldLoc.x), 
						epiderm.getCellContinous2D().sty(actionForce.y + oldLoc.y));
			
			extForce.x = 0; // alles einberechnet
			extForce.y = 0;

			// ////////////////////////////////////////////////
			// try ACTION force
			// ////////////////////////////////////////////////
			Bag b = epiderm.getCellContinous2D().getObjectsWithinDistance(potentialLoc,
					ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm(), false); // theEpidermis.neighborhood
			HitResultClass hitResult1;
			hitResult1 = hitsOther(b, epiderm.getCellContinous2D(), potentialLoc, true, NEXTTOOUTERCELL);

			// ////////////////////////////////////////////////
			// estimate optimised POS from REACTION force
			// ////////////////////////////////////////////////
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
					ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getNeighborhood_µm(), false); // theEpidermis.neighborhood
			HitResultClass hitResult2;
			hitResult2 = hitsOther(b, epiderm.getCellContinous2D(), potentialLoc, true, NEXTTOOUTERCELL);

			// move only on pressure when not stem cell
			if(this.cellBehavioralModelObjekt.getDifferentiation() != EpisimCellBehavioralModelGlobalParameters.STEMCELL){
				if((hitResult2.numhits == 0)
						|| ((hitResult2.numhits == 1) && ((hitResult2.otherId == this.getMotherID()) || (hitResult2.otherMotherId == this.getID())))){
					double dx = potentialLoc.x - oldLoc.x;
					lastd = new Double2D(potentialLoc.x - oldLoc.x, potentialLoc.y - oldLoc.y);
					setPositionRespectingBounds(epiderm.getCellContinous2D(), potentialLoc);
				}
			}

			Double2D newLoc = epiderm.getCellContinous2D().getObjectLocation(this);
			double maxy = TissueController.getInstance().getTissueBorder().lowerBound(newLoc.x);
			if((maxy - newLoc.y) < ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getBasalLayerWidth())
				setIsBasalStatisticsCell(true);
			else
				setIsBasalStatisticsCell(false); // ABSOLUTE DISTANZ KONSTANTE

			if((maxy - newLoc.y) < ModelController.getInstance().getBioMechanicalModelController().getEpisimMechanicalModelGlobalParameters().getMembraneCellsWidth())
				setIsMembraneCell(true);
			else
				setIsMembraneCell(false); // ABSOLUTE DISTANZ KONSTANTE
			
		/*	EpisimCellBehavioralModel[] neighbours = new EpisimCellBehavioralModel[b.size()];
			Object[] cytes = b.toArray();
			for(int i=0; i < b.size(); i++){ 
				if(cytes[i] instanceof CellType){
					neighbours[i] = ((CellType) cytes[i]).getEpisimCellBehavioralModelObject();
				}
			}*/

			//	long timeBefore = System.currentTimeMillis();
			/////////////////////////////////////////////////////////
			//   Differentiation: Calling the loaded Cell-Diff-Model
			/////////////////////////////////////////////////////////
			
			
			DrawInfo2D info = this.getCellEllipseObject().getLastDrawInfo2D();
			DrawInfo2D newInfo = null;
			if( info != null){
				newInfo = new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width,info.draw.height), info.clip);
				newInfo.draw.x = ((newInfo.draw.x - newInfo.draw.width*oldLoc.x) + newInfo.draw.width*newLoc.x);
				newInfo.draw.y = ((newInfo.draw.y - newInfo.draw.height*oldLoc.y) + newInfo.draw.height*newLoc.y);
				this.getCellEllipseObject().setLastDrawInfo2D(newInfo, true);
			}
			
			differentiate(state, b,epiderm.getCellContinous2D(), newLoc, hitResult2.nextToOuterCell, hitResult2.numhits != 0);
			
			
			//Ellipse Visualization is activated
			if(MiscalleneousGlobalParameters.getInstance().getTypeColor() ==8){
				calculateClippedCell();
			}
			
			//Polygon Visualization is activated
			if(MiscalleneousGlobalParameters.getInstance().getTypeColor() ==10){
				 calculateClippedCell();
        	  Calculators.calculateCellPolygons(getCellEllipseObject());
        	  Calculators.cleanCalculatedVertices(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(getCellEllipseObject().getId()));
        	  Calculators.calculateEstimatedVertices(getCellEllipseObject());
			}
			
			
/*			long timeAfter = System.currentTimeMillis();
	        //  	long actSteps = state.schedule.getSteps();
			long deltaTimeTmp = timeAfter-timeBefore;
			
			if(state.schedule.getSteps() > actNumberSteps){
				actNumberSteps = state.schedule.getSteps();
			    		
		    		// if(this.follow && this.KeratinoAge <=2000){   		
		   			  	
				 if(deltaTime > 0){  
				   try {
		           BufferedWriter out = new BufferedWriter(new FileWriter("d:\\performance_neu.csv", true));
		        //   out.write(NumberFormat.getInstance(Locale.GERMANY).format(actSteps)+ ";");
		           out.write(NumberFormat.getInstance(Locale.GERMANY).format(deltaTime)+ ";");
		      //     out.write(NumberFormat.getInstance(Locale.GERMANY).format(allCells.size())+ ";");
		                   
		           out.write("\n");
		           out.flush();
		           out.close();
		            } catch (IOException e) {}
				   
				  }
				 deltaTime = 0;
			}
			deltaTime +=deltaTimeTmp;		*/	
			
			
                 
			}
			
		}
	}

	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();
		
		for(Method m : this.getClass().getMethods()){
			if((m.getName().startsWith("get") && !methodsNamesBlockedForParameterInspector.contains(m.getName())) || m.getName().startsWith("is")) methods.add(m);
		}
		for(Method m : this.cellBehavioralModelObjekt.getClass().getMethods()){
			if((m.getName().startsWith("get") && ! m.getName().equals("getParameters")) || m.getName().startsWith("is")) methods.add(m);
		}
		return methods;
	}
	
	
	public void stop(){	
	
	}
	public void removeFromSchedule(){
		if(stoppable != null) stoppable.stop();			
	}
//-------------------------------------------------------------------------------------------------------------------------------------------------------------
// GETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	
	public double getExtForceX () { return extForce.x; }   // for inspector 
   public double getExtForceY () { return extForce.y; }   // for inspector
 
   
   
   public int getGKeratinoHeightGranu() {	return gKeratinoHeightGranu;}
   public int getGKeratinoWidthGranu() { return gKeratinoWidthGranu;	}
   
   public int getHasGivenIons() { return hasGivenIons; }
  
   public int getKeratinoHeight() {	return keratinoHeight; }
	
	public int getKeratinoWidth() {return keratinoWidth;}	

	public long getLocal_maxAge() {return local_maxAge;}
	
	public String getCellName() { return NAME; }
	
	public int getOwnColor() {	return ownColor; }

	
	
	
	
   
	
	
	
	public boolean isBirthWish() { return birthWish; }   // for inspector
	
	
	

    
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
// SETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	
	
	public void setHasGivenIons(int hasGivenIons) {	this.hasGivenIons = hasGivenIons; }	
	
	public void setKeratinoHeight(int keratinoHeight) { this.keratinoHeight = keratinoHeight;	}
	
	public void setKeratinoWidth(int keratinoWidth) { this.keratinoWidth = keratinoWidth; }	
	
	public void setLocal_maxAge(long local_maxAge) { this.local_maxAge = local_maxAge; }	
		
	public void setOwnColor(int ownColor) { this.ownColor = ownColor; }	
	
	public void setStoppable(Stoppable stopperparam)   { this.stoppable = stopperparam;}
   	
	public EpisimCellBehavioralModel getEpisimCellBehavioralModelObject(){
		return this.cellBehavioralModelObjekt;
	}
	
   public Class<? extends EpisimCellBehavioralModel> getEpisimCellBehavioralModelClass() {
	  
	   return this.cellBehavioralModelObjekt.getClass();
   }
	
   public SimState getActSimState() { return this.actSimState; }
	
   
   
  // public KCyte 
   
   
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------------------




}












