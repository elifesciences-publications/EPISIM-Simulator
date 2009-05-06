package sim.app.episim.devBasalLayer;


//MASON
import ec.util.MersenneTwisterFast;
import sim.app.episim.tissue.TissueBorder;
import sim.app.episim.tissue.TissueController;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;



public class EpidermisDev extends SimStateHack
{
 

        
	
 
 

 private Continuous2D basementContinous2D;
 private Continuous2D rulerContinous2D;
 private Continuous2D gridContinous2D;

        
     
 //////////////////////////////////////
 // Proliferation
 //////////////////////////////////////
 
 public int basalY=80;          // y coordinate at which undulations start, the base line    
 public int basalPeriod=70;      // width of an undulation at the foot
     

 /////////////////////////////////////
 // Code Procedures
 /////////////////////////////////////
 
   

 
 
 /** Creates a EpidermisClass simulation with the given random number seed. */
 public EpidermisDev(long seed)
 {
     super(new ec.util.MersenneTwisterFast(seed), new Schedule());
     
     basementContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth(),
   		  TissueController.getInstance().getTissueBorder().getWidth(),
   		  TissueController.getInstance().getTissueBorder().getHeight());
   			
     rulerContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth(),
   		  TissueController.getInstance().getTissueBorder().getWidth(),
   		  TissueController.getInstance().getTissueBorder().getHeight());
     
     gridContinous2D = new Continuous2D(TissueController.getInstance().getTissueBorder().getWidth(),
   		  TissueController.getInstance().getTissueBorder().getWidth(),
   		  TissueController.getInstance().getTissueBorder().getHeight());
   			
     basementContinous2D.setObjectLocation("DummyObjektForDrawingTheBasementMembrane", new Double2D(50, 50));
     rulerContinous2D.setObjectLocation("DummyObjektForDrawingTheRuler", new Double2D(50, 50));
     gridContinous2D.setObjectLocation("DummyObjektForDrawingTheGrid", new Double2D(50, 50));
     
   
 }
 
 public void start()
     {
	 
   	  super.start(null);
   	  
     
     
    
   
    
    Steppable dummy = new Steppable()
    {
         public void step(SimState state)
         {   
             
         }
     };
     // Schedule the agent to update the chart
     schedule.scheduleRepeating(dummy, 1);
     
     //BackImageClass backImage=new BackImageClass(this);        
     //schedule.scheduleOnce(backImage);
     
     
     
                   
                
     }


public Continuous2D getBasementContinous2D() {

	return basementContinous2D;
}


public Continuous2D getRulerContinous2D() {

	return rulerContinous2D;
}


public Continuous2D getGridContinous2D() {

	return gridContinous2D;
}





 }