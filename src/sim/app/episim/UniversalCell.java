package sim.app.episim;
import sim.app.episim.datamonitoring.GlobalStatistics;


import sim.app.episim.model.biomechanics.vertexbased.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.CellPolygonNetworkBuilder;
import sim.app.episim.model.controller.MiscalleneousGlobalParameters;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.tissue.Epidermis;

import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueType.SchedulePriority;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.visualization.CellEllipse;

import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import episiminterfaces.EpisimCellBehavioralModel;

import episiminterfaces.EpisimDifferentiationLevel;

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
   
   private static Set<String> methodsNamesBlockedForParameterInspector;   
   {
   	methodsNamesBlockedForParameterInspector = new HashSet<String>();
   	methodsNamesBlockedForParameterInspector.add("getParameter");
   	methodsNamesBlockedForParameterInspector.add("getVoronoihullvertexes");
   }
   
   
//-----------------------------------------------------------------------------------------------------------------------------------------   
  
//-----------------------------------------------------------------------------------------------------------------------------------------   
         
   public UniversalCell(){
   this(-1, -1,  null);
   }
    public UniversalCell(long id, long motherId, EpisimCellBehavioralModel cellBehavioralModel)
    {   	 
   	 
   	 super(id, motherId, cellBehavioralModel);      
       TissueServer.getInstance().getActEpidermalTissue().checkMemory();
       TissueServer.getInstance().getActEpidermalTissue().getAllCells().add(this); // register this as additional one in Bag
       
    }  
    
    public UniversalCell makeChild(EpisimCellBehavioralModel cellBehavioralModel)
    {       
        
   	 Continuous2D cellContinous2D = TissueServer.getInstance().getActEpidermalTissue().getCellContinous2D();
   	 
   	 // Either we get use a currently unused cell oder we allocate a new one
        UniversalCell kcyte;        
       
        kcyte= new UniversalCell(AbstractCell.getNextCellId(), getID(), cellBehavioralModel); 
        cellBehavioralModel.setId((int)kcyte.getID());
           
            
        Stoppable stoppable = TissueServer.getInstance().getActEpidermalTissue().schedule.scheduleRepeating(kcyte, SchedulePriority.CELLS.getPriority(), 1);   // schedule only if not already running
        kcyte.setStoppable(stoppable);
          
        double deltaX = TissueServer.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.25;
        double deltaY = TissueServer.getInstance().getActEpidermalTissue().random.nextDouble()*0.5-0.1; 
               
        Double2D oldLoc=cellContinous2D.getObjectLocation(this);
        
     //   double deltaDrawX = newloc.
         
        Double2D newloc=new Double2D(oldLoc.x + deltaX, oldLoc.y-deltaY);   
        
       
         //in the first two thousand sim steps homeostasis has to be achieved, cells max age is set to the sim step time to have more variation  
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
        UniversalCell taCell=makeChild(cellBehavioralModel);
                    
        taCell.getEpisimCellBehavioralModelObject().setAge(TissueServer.getInstance().getActEpidermalTissue().random.nextInt(ModelController.getInstance().getCellBehavioralModelController().getEpisimCellBehavioralModelGlobalParameters().getCellCycleTA()));  // somewhere on the TA Cycle
       
       
    }
   
    public void makeSpiCell(EpisimCellBehavioralModel cellBehavioralModel)
    {       
   	 GlobalStatistics.getInstance().inkrementActualNumberKCytes();
       makeChild(cellBehavioralModel);
    }

    
    private UniversalCell[] getRealNeighbours(Bag neighbours, Continuous2D cellContinous2D, Double2D thisloc){
   	 List<UniversalCell> neighbourCells = new ArrayList<UniversalCell>();
   	 for(int i=0;i<neighbours.numObjs;i++)
       {
   		 UniversalCell actNeighbour = (UniversalCell)(neighbours.objs[i]);
     
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
   	 return neighbourCells.toArray(new UniversalCell[neighbourCells.size()]);
    }
    
    private EpisimCellBehavioralModel[] getCellBehavioralModelArray(UniversalCell[] neighbours){
   	 List<EpisimCellBehavioralModel> neighbourCellsDiffModel = new ArrayList<EpisimCellBehavioralModel>();
   	 for(UniversalCell actNeighbour: neighbours) neighbourCellsDiffModel.add(actNeighbour.getEpisimCellBehavioralModelObject());
   	 return neighbourCellsDiffModel.toArray(new EpisimCellBehavioralModel[neighbourCellsDiffModel.size()]);
    }
   
    private boolean isSurfaceCell(EpisimCellBehavioralModel[] neighbours){
   	 if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL) return false;
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
   	 return false;
    }
    static  long actNumberSteps = 0;
    static  long deltaTime = 0;
    public void differentiate(SimState state, Bag neighbours, Continuous2D cellContinous2D, Double2D thisloc, boolean nextToOuterCell, boolean hasCollision)
    {
     	 UniversalCell[] realNeighbours = getRealNeighbours(neighbours, cellContinous2D, thisloc);
     	 
     	 this.setNeighbouringCells(realNeighbours);
     	 EpisimCellBehavioralModel[] realNeighboursDiffModel = getCellBehavioralModelArray(realNeighbours);
   	// setIsOuterCell(isSurfaceCell(realNeighbours));
   	 this.getEpisimCellBehavioralModelObject().setX(thisloc.getX());
   	 this.getEpisimCellBehavioralModelObject().setY(TissueController.getInstance().getTissueBorder().getHeight()- thisloc.getY());
   	 this.getEpisimCellBehavioralModelObject().setIsMembrane(isMembraneCell());
   	 this.getEpisimCellBehavioralModelObject().setIsSurface(isOuterCell() || nextToOuterCell);
   	 this.getEpisimCellBehavioralModelObject().setHasCollision(hasCollision);
   	 if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL) this.getEpisimCellBehavioralModelObject().setAge(0);
   	 else this.getEpisimCellBehavioralModelObject().setAge(this.getEpisimCellBehavioralModelObject().getAge()+1);
   	 	  	 
   	
		
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
  
    
    private void calculateClippedCell(){
   	 
    	CellEllipse cellEllipseCell = this.getCellEllipseObject();
    	 
    	 
    	 if(this.getNeighbouringCells() != null && this.getNeighbouringCells().length > 0 && cellEllipseCell.getLastDrawInfo2D()!= null){
 	   	 for(AbstractCell neighbouringCell : this.getNeighbouringCells()){
 	   		 
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
		
		super.step(state);
		final Epidermis epiderm = (Epidermis) state;		
		
		if(isInNirvana() || !this.getEpisimCellBehavioralModelObject().getIsAlive()){
			

			removeFromSchedule();
			
		}
		else{
			hasGivenIons = 0;
			getEpisimMechanicalModelObject().newSimStep();
			
//			long timeBefore = System.currentTimeMillis();
			/////////////////////////////////////////////////////////
			//   Differentiation: Calling the loaded Cell-Diff-Model
			/////////////////////////////////////////////////////////
			
			
			DrawInfo2D info = this.getCellEllipseObject().getLastDrawInfo2D();
			DrawInfo2D newInfo = null;
			if( info != null){
				newInfo = new DrawInfo2D(new Rectangle2D.Double(info.draw.x, info.draw.y, info.draw.width,info.draw.height), info.clip);
				newInfo.draw.x = ((newInfo.draw.x - newInfo.draw.width*getEpisimMechanicalModelObject().getOldPosition().x) + newInfo.draw.width* getEpisimMechanicalModelObject().getNewPosition().x);
				newInfo.draw.y = ((newInfo.draw.y - newInfo.draw.height*getEpisimMechanicalModelObject().getOldPosition().y) + newInfo.draw.height*getEpisimMechanicalModelObject().getNewPosition().y);
				this.getCellEllipseObject().setLastDrawInfo2D(newInfo, true);
			}
			
			differentiate(state, getEpisimMechanicalModelObject().getNeighbouringCells(),epiderm.getCellContinous2D(), getEpisimMechanicalModelObject().getNewPosition(), 
					getEpisimMechanicalModelObject().nextToOuterCell(), getEpisimMechanicalModelObject().hitsOtherCell() != 0);
			
			
			//Ellipse Visualization is activated
			if(MiscalleneousGlobalParameters.instance().getTypeColor() ==8){
				calculateClippedCell();
			}
			
			//Polygon Visualization is activated
			if(MiscalleneousGlobalParameters.instance().getTypeColor() ==10){
				 calculateClippedCell();
				 CellPolygonNetworkBuilder.calculateCellPolygons(getCellEllipseObject(), new CellPolygonCalculator(new CellPolygon[]{}));
				 CellPolygonNetworkBuilder.cleanCalculatedVertices(CellEllipseIntersectionCalculationRegistry.getInstance().getCellPolygonByCellEllipseId(getCellEllipseObject().getId()));
				 CellPolygonNetworkBuilder.calculateEstimatedVertices(getCellEllipseObject(), new CellPolygonCalculator(new CellPolygon[]{}));
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

	public List<Method> getParameters() {
		List<Method> methods = new ArrayList<Method>();		
		for(Method m : this.getClass().getMethods()){
			if((m.getName().startsWith("get") && !methodsNamesBlockedForParameterInspector.contains(m.getName())) || m.getName().startsWith("is")) methods.add(m);
		}
		for(Method m : this.getEpisimCellBehavioralModelClass().getMethods()){
			if((m.getName().startsWith("get") && ! m.getName().equals("getParameters")) || m.getName().startsWith("is")) methods.add(m);
		}
		return methods;
	}
	
	
	
//-------------------------------------------------------------------------------------------------------------------------------------------------------------
// GETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
   public int getHasGivenIons() { return hasGivenIons; }
   
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












