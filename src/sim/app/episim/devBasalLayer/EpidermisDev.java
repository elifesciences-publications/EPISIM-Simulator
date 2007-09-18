package sim.app.episim.devBasalLayer;


//MASON
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;



public class EpidermisDev extends SimStateHack
{
 

        



 
 

 public Continuous2D basementContinous2D;
 

 
 
 
 

 
 
            
     
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
     super(new ec.util.MersenneTwisterFast(seed), new Schedule(1));
     
   
 }
 
 public void start()
     {
	 
   	  super.start(false);
   	  
     
     
    
     basementContinous2D = new Continuous2D(TissueBorderDev.getInstance().getWidth(),
   		                                   TissueBorderDev.getInstance().getWidth(),
   		                                   TissueBorderDev.getInstance().getHeight());
    
    basementContinous2D.setObjectLocation("DummyObjektForDrawingTheBasementMembrane", new Double2D(50, 50));
    
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





 }