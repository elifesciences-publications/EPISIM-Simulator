package sim.app.episim;
import sim.app.episim.datamonitoring.GlobalStatistics;


import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonNetworkBuilder;
import sim.app.episim.model.controller.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.tissue.Epidermis;
import sim.app.episim.tissue.TissueServer;

import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueType.SchedulePriority;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.app.episim.visualization.CellEllipse;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModel;

import episiminterfaces.EpisimDifferentiationLevel;
import episiminterfaces.monitoring.CannotBeMonitored;

import java.awt.geom.Rectangle2D;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.List;
import java.util.Set;


import sim.portrayal.*;

public class UniversalCell extends AbstractCell
{
//	-----------------------------------------------------------------------------------------------------------------------------------------   
// CONSTANTS
//	-----------------------------------------------------------------------------------------------------------------------------------------          
	
	private static final long serialVersionUID = 5212944079288103141L;
   
   private final String NAME = "Keratinocyte";
   
   

//	-----------------------------------------------------------------------------------------------------------------------------------------   
// VARIABLES
//	-----------------------------------------------------------------------------------------------------------------------------------------          
     
   private long local_maxAge;
   
   private boolean birthWish=false;   
   private int hasGivenIons=0;  
   
//-----------------------------------------------------------------------------------------------------------------------------------------   
  
//-----------------------------------------------------------------------------------------------------------------------------------------   
         
   public UniversalCell(){
   	this(-1, -1,  null, null);
   }
   
   public UniversalCell(long id, long motherId, EpisimCellBehavioralModel cellBehavioralModel, SimState simState){   
   	 super(id, motherId, cellBehavioralModel, simState);      
   	 TissueController.getInstance().getActEpidermalTissue().checkMemory();
   	 TissueController.getInstance().getActEpidermalTissue().getAllCells().add(this); // register this as additional one in Bag       
    }  
    
    public UniversalCell makeChild(EpisimCellBehavioralModel cellBehavioralModel)
    {       
        
   	 Continuous2D cellContinous2D = TissueController.getInstance().getActEpidermalTissue().getCellContinous2D();
   	 
   	 // Either we get use a currently unused cell oder we allocate a new one
        UniversalCell kcyte;        
       
        kcyte= new UniversalCell(AbstractCell.getNextCellId(), getID(), cellBehavioralModel, getActSimState()); 
        cellBehavioralModel.setId((int)kcyte.getID());
         
            
        Stoppable stoppable = TissueController.getInstance().getActEpidermalTissue().schedule.scheduleRepeating(kcyte, SchedulePriority.CELLS.getPriority(), 1);   // schedule only if not already running
        kcyte.setStoppable(stoppable);
          
         
        
       
         //in the first two thousand sim steps homeostasis has to be achieved, cells max age is set to the sim step time to have more variation  
        kcyte.local_maxAge= ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getMaxAge();
        long pSimTime=(long) TissueController.getInstance().getActEpidermalTissue().schedule.time();
        if (pSimTime<(kcyte.local_maxAge)){ 
      	  kcyte.local_maxAge=pSimTime;
      	  cellBehavioralModel.setMaxAge((int)kcyte.local_maxAge);
        }        
              
        
        if(this.getEpisimBioMechanicalModelObject() instanceof CenterBasedMechanicalModel){
      	  double deltaX = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;
           double deltaY = TissueController.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.1; 
                  
           Double2D oldLoc=cellContinous2D.getObjectLocation(this);
           
        //   double deltaDrawX = newloc.
            
           Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y-deltaY); 
           
           cellContinous2D.setObjectLocation(kcyte, newloc);
           
      	  DrawInfo2D info = ((CenterBasedMechanicalModel)this.getEpisimBioMechanicalModelObject()).getCellEllipseObject().getLastDrawInfo2D();
      	  ((CenterBasedMechanicalModel)kcyte.getEpisimBioMechanicalModelObject()).setLastDrawInfo2DForNewCellEllipse(info, newloc, oldLoc);
        }
        else{
      	  cellContinous2D.setObjectLocation(kcyte, new Double2D(kcyte.getEpisimBioMechanicalModelObject().getX(), kcyte.getEpisimBioMechanicalModelObject().getY()));
        }
		              
        return kcyte;
    }

    public void makeTACell(EpisimCellBehavioralModel cellBehavioralModel)
    {
        
        GlobalStatistics.getInstance().inkrementActualNumberKCytes();
        UniversalCell taCell=makeChild(cellBehavioralModel);
         
        //TODO enable / disable random age for TA Cells
        int randomAge = TissueController.getInstance().getActEpidermalTissue().random.nextInt(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getCellCycleTA());
        
        taCell.getEpisimCellBehavioralModelObject().setAge(randomAge);
        if(taCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector()!= null
      		  && taCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector() instanceof SbmlModelConnector){
      	  ((SbmlModelConnector)taCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector()).initializeSBMLModelsWithCellAge(randomAge);
        }
        // somewhere on the TA Cycle       
    }
   
    public void makeSpiCell(EpisimCellBehavioralModel cellBehavioralModel)
    {       
   	 GlobalStatistics.getInstance().inkrementActualNumberKCytes();
       makeChild(cellBehavioralModel);
    }
 
    
    private EpisimCellBehavioralModel[] getCellBehavioralModelArray(GenericBag<AbstractCell> neighbours){
   	 List<EpisimCellBehavioralModel> neighbourCellsDiffModel = new ArrayList<EpisimCellBehavioralModel>();
   	 for(AbstractCell actNeighbour: neighbours) neighbourCellsDiffModel.add(actNeighbour.getEpisimCellBehavioralModelObject());
   	 return neighbourCellsDiffModel.toArray(new EpisimCellBehavioralModel[neighbourCellsDiffModel.size()]);
    }
   
    private boolean isSurfaceCell(EpisimCellBehavioralModel[] neighbours){
   	/* if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL) return false;
   	 else{
   		
   		 int leftSideNeighbours = 0;
   		 int rightSideNeighbours= 0;
   		 int upperNeighbours = 0;
   		 for(EpisimCellBehavioralModel actNeighbour :neighbours){
   			  double dx =actNeighbour.getDx();
   			  double dy =actNeighbour.getDy();
   			  if(dy <=0 && dx == 0) upperNeighbours++;
   			  else if(dy <=0 && dx < 0) leftSideNeighbours++;
   			  else if(dy <=0  && dx > 0) rightSideNeighbours++;
   		  }
   		 
   		 if(upperNeighbours == 0 || rightSideNeighbours == 0 || leftSideNeighbours == 0) return true;   		 
   	 }
   	 return false;*/
   	 return false;
    }
    
    
    static  long actNumberSteps = 0;
    static  long deltaTime = 0;
    int cellDivisionCounter = 0;
    public void newSimStepCellBehavioralModel()
    {
     	
     	 
     	 EpisimCellBehavioralModel[] realNeighboursDiffModel = getCellBehavioralModelArray(this.getNeighbouringCells());  	 
  
   	// if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL) this.getEpisimCellBehavioralModelObject().setAge(0);
   	// else 
   		 this.getEpisimCellBehavioralModelObject().setAge(this.getEpisimCellBehavioralModelObject().getAge()+1);   	
		
   	 EpisimCellBehavioralModel[] children = this.getEpisimCellBehavioralModelObject().oneStep(realNeighboursDiffModel);
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
   	 
   	 if(children != null && children.length >= 1){
   		 if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL){
   			cellDivisionCounter++;
   			System.out.println(cellDivisionCounter + ". Teilung im Alter von " + this.getEpisimCellBehavioralModelObject().getAge());
   		 }
   	 }
   	
   	 makeChildren(children);
   	 
      if (!this.getEpisimCellBehavioralModelObject().getIsAlive()) // && (isOuterCell))
      {
         killCell();
      }
  
   }
    
    private void makeChildren(EpisimCellBehavioralModel[] children){
   	 if(children!=null){
   		 for(EpisimCellBehavioralModel actChild: children){   			 
   			 if(actChild.getDiffLevel().ordinal() == EpisimDifferentiationLevel.TACELL) makeTACell(actChild);
   			 else if(actChild.getDiffLevel().ordinal() == EpisimDifferentiationLevel.EARLYSPICELL) makeSpiCell(actChild);
   		 }
   	 }
    }    

    // static  long actNumberSteps = 0;
    // static  long deltaTime = 0;
	public void step(SimState state) {
		
		super.step(state);
		
		final Epidermis epiderm = (Epidermis) state;		
		if(isInNirvana() || !this.getEpisimCellBehavioralModelObject().getIsAlive()){		
			removeFromSchedule();			
		}
		else{
			
			hasGivenIons = 0;			
			getEpisimBioMechanicalModelObject().newSimStep(state.schedule.getSteps());
			
			//	long timeBefore = System.currentTimeMillis();
			/////////////////////////////////////////////////////////
			//   Differentiation: Calling the loaded Cell-Diff-Model
			/////////////////////////////////////////////////////////
			newSimStepCellBehavioralModel();
						
			//newSimStepCellBehavioralModel();			
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

	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();		
		for(Method m : this.getClass().getMethods()){
			if((m.getName().startsWith("get") || m.getName().startsWith("is"))&& m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		}
		for(Method m : this.getEpisimCellBehavioralModelClass().getMethods()){
			if((m.getName().startsWith("get") && ! m.getName().equals("getParameters")) || m.getName().startsWith("is")) methods.add(m);
		}
		for(Method m : this.getEpisimBioMechanicalModelObject().getClass().getMethods()){
			if(((m.getName().startsWith("get") && ! m.getName().equals("getParameters")) || m.getName().startsWith("is")) && m.getAnnotation(CannotBeMonitored.class)==null) methods.add(m);
		}
		return methods;
	}
	
	
	
//-------------------------------------------------------------------------------------------------------------------------------------------------------------
// GETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
   public int getHasGivenIons() { return hasGivenIons; }
   
   @CannotBeMonitored
   public long getLocal_maxAge() {return local_maxAge;}
	
	public String getCellName() { return NAME; }	
	
	public boolean isBirthWish() { return birthWish; }   // for inspector
    
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
// SETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	public void setHasGivenIons(int hasGivenIons) {	this.hasGivenIons = hasGivenIons; }
	
	public void setLocal_maxAge(long local_maxAge) { this.local_maxAge = local_maxAge; } 
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------------------




}












