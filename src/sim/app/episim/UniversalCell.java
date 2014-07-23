package sim.app.episim;
import sim.SimStateServer;
import sim.app.episim.datamonitoring.GlobalStatistics;
import sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel;
import sim.app.episim.model.biomechanics.vertexbased.calc.CellPolygonCalculator;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygon;
import sim.app.episim.model.biomechanics.vertexbased.geom.CellPolygonNetworkBuilder;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardCellType;
import sim.app.episim.model.cellbehavior.CellBehavioralModelFacade.StandardDiffLevel;
import sim.app.episim.model.controller.ModelController;
import sim.app.episim.model.misc.MiscalleneousGlobalParameters;
import sim.app.episim.model.sbml.SbmlModelConnector;
import sim.app.episim.model.visualization.CellEllipse;
import sim.app.episim.tissue.UniversalTissue;
import sim.app.episim.tissue.TissueServer;
import sim.app.episim.tissue.TissueController;
import sim.app.episim.tissue.TissueType.SchedulePriority;
import sim.app.episim.util.CellEllipseIntersectionCalculationRegistry;
import sim.app.episim.util.EllipseIntersectionCalculatorAndClipper;
import sim.app.episim.util.GenericBag;
import sim.app.episim.util.TysonRungeCuttaCalculator;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import ec.util.MersenneTwisterFast;
import episimbiomechanics.EpisimModelConnector.Hidden;
import episimexceptions.ZeroNeighbourCellsAccessException;
import episiminterfaces.EpisimBiomechanicalModel;
import episiminterfaces.EpisimCellBehavioralModel;
import episiminterfaces.EpisimCellType;
import episiminterfaces.NoExport;
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
   
   private final String NAME = "Cell";   
    
//-----------------------------------------------------------------------------------------------------------------------------------------   
  
//-----------------------------------------------------------------------------------------------------------------------------------------   
    
   public UniversalCell(boolean isSimulationCell){
   	this(null,  null, isSimulationCell);
   }
   public UniversalCell(long cellId, boolean isSimulationCell){
   	this(cellId, null,  null, isSimulationCell);
   }
   
   public UniversalCell(UniversalCell motherCell, EpisimCellBehavioralModel cellBehavioralModel, boolean isSimulationCell){   
   	this(Long.MIN_VALUE, motherCell, cellBehavioralModel, isSimulationCell);
    }  
    
   public UniversalCell(long cellId, UniversalCell motherCell, EpisimCellBehavioralModel cellBehavioralModel, boolean isSimulationCell){   
	  	 super(cellId, motherCell, cellBehavioralModel);      
	  	 TissueController.getInstance().getActEpidermalTissue().checkMemory();
	  	 if(isSimulationCell)TissueController.getInstance().getActEpidermalTissue().getAllCells().add(this); // register this as additional one in Bag       
   }
   public UniversalCell makeChild(EpisimCellBehavioralModel cellBehavioralModel)
   {       
   	 
   	 // Either we get use a currently unused cell oder we allocate a new one
        UniversalCell kcyte;   
        kcyte= new UniversalCell(this, cellBehavioralModel, true);       
         
        if(!ModeServer.useMonteCarloSteps()){   
	        Stoppable stoppable = TissueController.getInstance().getActEpidermalTissue().schedule.scheduleRepeating(kcyte, SchedulePriority.CELLS.getPriority(), 1);   // schedule only if not already running
	        kcyte.setStoppable(stoppable);
        }        
       
        //in the first two thousand sim steps homeostasis has to be achieved, cells max age is set to the sim step time to have more variation  
        if(EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT) != null &&
    				EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT).equals(EpisimProperties.ON)){
	        double maxAge= cellBehavioralModel.getMaxAge();
	        long simTime=SimStateServer.getInstance().getSimStepNumber();
	        if (simTime<(maxAge)){ 
	      	  cellBehavioralModel.setMaxAge((double)simTime);
	        }
        }
        return kcyte;
    }

    public void makeTACell(EpisimCellBehavioralModel cellBehavioralModel)
    {    
        UniversalCell taCell=makeChild(cellBehavioralModel);
        
        //TODO enable / disable random age for TA Cells
        if(EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT) != null &&
  				EpisimProperties.getProperty(EpisimProperties.MODEL_RANDOM_CELL_AGE_INIT).equals(EpisimProperties.ON)
  				&& isDemoModel(taCell)){
      	  int cellCycleDuration = 1;
      	  Object result = null;
				try {
					Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getCellCycleTA", new Class<?>[]{});
					result =m.invoke(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), new Object[0]);
					
				} catch (Exception e1) {
					try {
						Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getCellCycle", new Class<?>[]{});
						result =m.invoke(ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters(), new Object[0]);
						
					} catch (Exception e2) {}
				}
				if(result != null){
					if(result instanceof Integer){
						cellCycleDuration = ((Integer) result).intValue();
					}
					else if(result instanceof Double){
						cellCycleDuration = (int)((Double) result).doubleValue();
					}
				}    	  
      	  int randomAge = TissueController.getInstance().getActEpidermalTissue().random.nextInt(cellCycleDuration ==0?1:cellCycleDuration);
        
      	  taCell.getEpisimCellBehavioralModelObject().setAge(randomAge);
      	  if(taCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector()!= null
      		  	&& taCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector() instanceof SbmlModelConnector){
      	  	((SbmlModelConnector)taCell.getEpisimCellBehavioralModelObject().getEpisimSbmlModelConnector()).initializeSBMLModelsWithCellAge(randomAge);
      	  }
      	  boolean tysonCellCycleAvailable = false;
	  		  try {
	  				Method m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getT_k6", new Class<?>[]{});
	  				m = ModelController.getInstance().getEpisimCellBehavioralModelGlobalParameters().getClass().getMethod("getT_k4", new Class<?>[]{});
	  				tysonCellCycleAvailable = true;
	  		 } catch (NoSuchMethodException e) {
	  				tysonCellCycleAvailable = false;
	  		 }

  			if (tysonCellCycleAvailable) TysonRungeCuttaCalculator.assignRandomCellcyleState(taCell.getEpisimCellBehavioralModelObject(), randomAge); // on
        }
        // somewhere on the TA Cycle       
    }
   private boolean isDemoModel(UniversalCell cell){
   	boolean isDemoModel = false;
   	if(cell.getEpisimBioMechanicalModelObject() instanceof sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel){
   		isDemoModel = ((sim.app.episim.model.biomechanics.centerbased3d.CenterBased3DMechanicalModel)cell.getEpisimBioMechanicalModelObject()).isEpidermisDemoModel();
   	}
   	else if(cell.getEpisimBioMechanicalModelObject() instanceof sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel){
   		isDemoModel = ((sim.app.episim.model.biomechanics.centerbased.CenterBasedMechanicalModel)cell.getEpisimBioMechanicalModelObject()).isEpidermisDemoModel();
   	}   	
   	return isDemoModel;
   }
  
    
    private EpisimCellBehavioralModel[] getCellBehavioralModelArray(GenericBag<AbstractCell> neighbours){
   	 List<EpisimCellBehavioralModel> neighbourCellsDiffModel = new ArrayList<EpisimCellBehavioralModel>();
   	 for(AbstractCell actNeighbour: neighbours){
   		 if(actNeighbour != null && actNeighbour.getID() != this.getID()) neighbourCellsDiffModel.add(actNeighbour.getEpisimCellBehavioralModelObject());
   	 }
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
    public void newSimStepCellBehavioralModel(MersenneTwisterFast random)
    {
     	
     	 //Shuffling of the neighbour cells to avoid sequence dependency
   	 
   	GenericBag<AbstractCell> neighbours = this.getNeighbouringCells();
   	neighbours.shuffle(random);
   	 
     	 EpisimCellBehavioralModel[] realNeighboursDiffModel = getCellBehavioralModelArray(neighbours);  	 
  
   	// if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL) this.getEpisimCellBehavioralModelObject().setAge(0);
   	// else 
   		 this.getEpisimCellBehavioralModelObject().setAge(this.getEpisimCellBehavioralModelObject().getAge()+1);   	
   		 EpisimCellBehavioralModel[] children=null;
		try{
			
   	  children = this.getEpisimCellBehavioralModelObject().oneStep(realNeighboursDiffModel);
		}
		catch(ZeroNeighbourCellsAccessException e){
			ExceptionDisplayer.getInstance().displayException(e);
		}
   	 
   	 
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
   	 
   	 /*if(children != null && children.length >= 1){
   		 if(this.getEpisimCellBehavioralModelObject().getDiffLevel().ordinal() == EpisimDifferentiationLevel.STEMCELL){
   			cellDivisionCounter++;
   			System.out.println(cellDivisionCounter + ". Teilung im Alter von " + this.getEpisimCellBehavioralModelObject().getAge());
   		 }
   	 }*/
   	
   	 makeChildren(children);
   	 
      if(!this.getEpisimCellBehavioralModelObject().getIsAlive()) // && (isOuterCell))
      {
         killCell();
      }
  
   }
    
    private void makeChildren(EpisimCellBehavioralModel[] children){
   	 if(children!=null){
   		 for(EpisimCellBehavioralModel actChild: children){
   			 if(ModelController.getInstance().isStandardKeratinocyteModel()){
	   			 if(convertToStandardDiffLevel(actChild.getDiffLevel()) == StandardDiffLevel.TACELL) makeTACell(actChild);
	   			 else{
	   				 makeChild(actChild);
	   				
	   			 }
   			 }
   			 else{
   				 makeChild(actChild);
   			 }
   		 }
   	 }
    }
    
    


    // static  long actNumberSteps = 0;
    // static  long deltaTime = 0;
	public void step(SimState state) {
		
		super.step(state);
		
		if(!this.getEpisimCellBehavioralModelObject().getIsAlive()){		
			removeFromSchedule();			
		}
		else{
			
		
			getEpisimBioMechanicalModelObject().newSimStep(SimStateServer.getInstance().getSimStepNumber());
			
			//	long timeBefore = System.currentTimeMillis();
			/////////////////////////////////////////////////////////
			//   Differentiation: Calling the loaded Cell-Diff-Model
			/////////////////////////////////////////////////////////
			newSimStepCellBehavioralModel(state.random);
						
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

	@NoExport
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
	
  
  
   public String getCellName() { return NAME; }	
	
	
    
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
// SETTER-METHODS
//	--------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	
	 
//--------------------------------------------------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------------------------------------------------




}












